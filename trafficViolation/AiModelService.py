import os
import cv2
import torch
import torch.nn as nn
import numpy as np
import uuid
from flask import Flask, request, jsonify
from pyngrok import ngrok
from ultralytics import YOLO
from torchvision import models, transforms
from PIL import Image

app = Flask(__name__)

# ============================================================
# 1. CẤU HÌNH ĐƯỜNG DẪN MODEL, STORAGE & KEY NGROK
# ============================================================
YOLO_PATH = "/content/drive/MyDrive/GR1/Yolov8s/runs/detect/yolov8n_custom_cctv_single_class/weights/best.pt"
RESNET_PATH = "/content/drive/MyDrive/DatasetBuilder/result/best_resnet18_v2.pth"
NGROK_AUTH_TOKEN = "QiwVWpHhNTRetAZgzs"

# Tạo thư mục local storage mô phỏng để lưu ảnh/khung hình đã vẽ bbox
STORAGE_DIR = "D:/MyDrive/DATN/storage/detected_image"
os.makedirs(STORAGE_DIR, exist_ok=True)

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"
LABEL_COLS = ["no_helmet", "using_mobile", "triple_riding"]
IMG_SIZE = 256

# Map nhãn chuẩn theo ENUM trong database của bạn
VIOLATION_MAP = {
    "no_helmet": "NO_HELMET",
    "using_mobile": "USING_PHONE",
    "triple_riding": "TRIPLE_RIDING"
}

# ============================================================
# 2. ĐỊNH NGHĨA MÔ HÌNH (Giữ nguyên cấu trúc CBAM)
# ============================================================
class ChannelAttention(nn.Module):
    def __init__(self, in_channels, reduction=16):
        super().__init__()
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.max_pool = nn.AdaptiveMaxPool2d(1)
        self.fc = nn.Sequential(
            nn.Linear(in_channels, in_channels // reduction, bias=False),
            nn.ReLU(inplace=True),
            nn.Linear(in_channels // reduction, in_channels, bias=False),
        )
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        b, c, _, _ = x.size()
        avg = self.fc(self.avg_pool(x).view(b, c))
        mx  = self.fc(self.max_pool(x).view(b, c))
        return self.sigmoid(avg + mx).view(b, c, 1, 1) * x

class SpatialAttention(nn.Module):
    def __init__(self, kernel_size=7):
        super().__init__()
        self.conv = nn.Conv2d(2, 1, kernel_size, padding=kernel_size // 2, bias=False)
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        avg = x.mean(dim=1, keepdim=True)
        mx, _ = x.max(dim=1, keepdim=True)
        return self.sigmoid(self.conv(torch.cat([avg, mx], dim=1))) * x

class CBAM(nn.Module):
    def __init__(self, in_channels, reduction=16):
        super().__init__()
        self.ca = ChannelAttention(in_channels, reduction)
        self.sa = SpatialAttention()

    def forward(self, x):
        return self.sa(self.ca(x))

class ResNet18CBAM(nn.Module):
    def __init__(self, num_classes=3):
        super().__init__()
        base = models.resnet18(weights=None)
        in_features = base.fc.in_features
        self.stem   = nn.Sequential(base.conv1, base.bn1, base.relu, base.maxpool)
        self.layer1 = base.layer1
        self.layer2 = base.layer2
        self.layer3 = base.layer3
        self.layer4 = base.layer4
        self.cbam   = CBAM(in_features, reduction=16)
        self.avgpool = base.avgpool
        self.fc = nn.Sequential(
            nn.Dropout(p=0.4),
            nn.Linear(in_features, 256),
            nn.ReLU(inplace=True),
            nn.Dropout(p=0.3),
            nn.Linear(256, num_classes),
        )

    def forward(self, x):
        x = self.stem(x)
        x = self.layer1(x)
        x = self.layer2(x)
        x = self.layer3(x)
        x = self.layer4(x)
        x = self.cbam(x)
        x = self.avgpool(x)
        x = torch.flatten(x, 1)
        return self.fc(x)

# ============================================================
# 3. LOAD WEIGHTS & TRANSFORMS
# ============================================================
print("--> Loading YOLOv8...")
yolo_model = YOLO(YOLO_PATH)

print("--> Loading ResNet18 + CBAM...")
resnet_model = ResNet18CBAM(num_classes=len(LABEL_COLS))
checkpoint = torch.load(RESNET_PATH, map_location=torch.device(DEVICE))
resnet_model.load_state_dict(checkpoint["state_dict"])
resnet_model.to(DEVICE).eval()

saved_thresholds = checkpoint.get("thresholds", [0.5, 0.5, 0.5])
print(f"✅ Models loaded. Optimal Thresholds: {saved_thresholds}")

MEAN, STD = [0.485, 0.456, 0.406], [0.229, 0.224, 0.225]
val_transform = transforms.Compose([
    transforms.Resize(int(IMG_SIZE * 1.1)),
    transforms.CenterCrop(IMG_SIZE),
    transforms.ToTensor(),
    transforms.Normalize(MEAN, STD),
])

def letterbox_resize(image, target_size=(256, 256)):
    ih, iw, _ = image.shape
    tw, th = target_size
    scale = min(tw / iw, th / ih)
    nw, nh = int(iw * scale), int(ih * scale)
    image_resized = cv2.resize(image, (nw, nh))
    background = np.full((th, tw, 3), 128, dtype=np.uint8)
    dx, dy = (tw - nw) // 2, (th - nh) // 2
    background[dy:dy+nh, dx:dx+nw] = image_resized
    return Image.fromarray(cv2.cvtColor(background, cv2.COLOR_BGR2RGB))

# ============================================================
# 4. HÀM CORE XỬ LÝ NHẬN DIỆN TRÊN MỘT KHUNG HÌNH/ẢNH
# ============================================================
def core_process_frame(cv2_img, frame_number=0):
    """
    Xử lý detect qua YOLOv8 và phân loại lỗi qua ResNet18.
    Đồng thời vẽ trực tiếp bounding box lên bản sao của ảnh gốc.
    """
    orig_img = cv2_img.copy()
    draw_img = cv2_img.copy() # Dùng để vẽ bounding box kết quả
    
    yolo_results = yolo_model(cv2_img, conf=0.25, verbose=False)
    objects_list = []
    object_counter = 1

    for result in yolo_results:
        boxes = result.boxes.xyxy.cpu().numpy()
        confidences = result.boxes.conf.cpu().numpy()

        for box, yolo_conf in zip(boxes, confidences):
            x1, y1, x2, y2 = map(int, box)
            x1, y1 = max(0, x1), max(0, y1)
            x2, y2 = min(cv2_img.shape[1], x2), min(cv2_img.shape[0], y2)

            cropped_img = orig_img[y1:y2, x1:x2]
            if cropped_img.size == 0:
                continue

            # Phân loại đa nhãn qua ResNet
            pil_img = letterbox_resize(cropped_img, target_size=(IMG_SIZE, IMG_SIZE))
            input_tensor = val_transform(pil_img).unsqueeze(0).to(DEVICE)

            with torch.no_grad():
                outputs = resnet_model(input_tensor)
                probs = torch.sigmoid(outputs).squeeze().cpu().numpy()

            active_violations = []
            for idx, prob in enumerate(probs):
                if prob >= saved_thresholds[idx]:
                    active_violations.append(VIOLATION_MAP[LABEL_COLS[idx]])

            # Chỉ vẽ box và tạo label text nếu phát hiện ra vi phạm
            if len(active_violations) > 0:
                label_text = f"ID:{object_counter} | {','.join(active_violations)}"
                color = (0, 0, 255) # Màu đỏ cho vi phạm
            else:
                label_text = f"ID:{object_counter} | OK"
                color = (0, 255, 0) # Màu xanh cho bình thường

            # Vẽ lên ảnh draw_img
            cv2.rectangle(draw_img, (x1, y1), (x2, y2), color, 2)
            cv2.putText(draw_img, label_text, (x1, max(y1 - 10, 15)), 
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

            # Map chuẩn xác theo từng trường của DetectedObjectDTO
            objects_list.append({
                "object_id": object_counter,
                "violation_types": active_violations, # Trả về mảng rỗng [] nếu không dính lỗi
                "confidence": round(float(yolo_conf), 2),
                "frame_number": frame_number,
                "bbox": {
                    "xmin": x1,
                    "ymin": y1,
                    "xmax": x2,
                    "ymax": y2
                }
            })
            object_counter += 1

    return objects_list, draw_img

# ============================================================
# 5. CÁC API ENDPOINTS
# ============================================================

# --- 5.1. CHỨC NĂNG 1: UPLOAD SINGLE IMAGE ---
@app.route('/api/ai/process-image', methods=['POST'])
def process_image():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "Empty filename"}), 400

    try:
        file_bytes = np.frombuffer(file.read(), np.uint8)
        img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)

        # Gọi hàm core xử lý ảnh và vẽ box
        detected_objects, processed_img = core_process_frame(img, frame_number=0)

        # Lưu ảnh đã vẽ bounding box ra storage cục bộ
        filename = f"processed_{uuid.uuid4().hex}.jpg"
        saved_path = os.path.join(STORAGE_DIR, filename)
        cv2.imwrite(saved_path, processed_img)

        # Khớp định dạng AIProcessingResultDTO của Spring Boot
        return jsonify({
            "processed_url": saved_path,
            "objects": detected_objects
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# --- 5.2. CHỨC NĂNG 2: UPLOAD FOLDER ẢNH ---
@app.route('/api/ai/process-folder', methods=['POST'])
def process_folder():
    """
    Xử lý danh sách nhiều ảnh từ Folder. Spring Boot sẽ gửi danh sách MultipartFile.
    """
    if 'files' not in request.files:
        return jsonify({"error": "No files uploaded"}), 400

    files = request.files.getlist('files')
    if not files or files[0].filename == '':
        return jsonify({"error": "Empty folder files"}), 400

    folder_results = []

    try:
        for file in files:
            file_bytes = np.frombuffer(file.read(), np.uint8)
            img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)

            detected_objects, processed_img = core_process_frame(img, frame_number=0)

            filename = f"processed_{uuid.uuid4().hex}.jpg"
            saved_path = os.path.join(STORAGE_DIR, filename)
            cv2.imwrite(saved_path, processed_img)

            # Đóng gói kết quả cho từng ảnh đơn trong folder
            folder_results.append({
                "original_filename": file.filename,
                "processed_url": saved_path,
                "objects": detected_objects
            })

        # Trả về mảng JSON chứa danh sách kết quả xử lý của từng ảnh
        return jsonify(folder_results), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# --- 5.3. CHỨC NĂNG 3: UPLOAD VIDEO (Bước nhảy 3 frames) ---
@app.route('/api/ai/process-video', methods=['POST'])
def process_video():
    """
    Xử lý video: Quét cách tuần tự cứ mỗi 3 frames kiểm tra 1 lần.
    Nếu tìm thấy BẤT KỲ đối tượng nào dính lỗi hoặc bình thường, dừng quét ngay lập tức,
    lưu frame đó lại làm processed_url và trả về thông tin đối tượng.
    """
    if 'file' not in request.files:
        return jsonify({"error": "No video file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "Empty filename"}), 400

    # Lưu tạm file video nhận được để OpenCV đọc
    temp_video_path = f"temp_{uuid.uuid4().hex}_{file.filename}"
    file.save(temp_video_path)

    cap = cv2.VideoCapture(temp_video_path)
    frame_idx = 0
    detected_objects = []
    processed_frame_path = None
    found_target = False

    try:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break # Hết video

            # Thực hiện bước nhảy: chỉ kiểm tra khi frame_idx chia hết cho 3
            if frame_idx % 3 == 0:
                # Gọi hàm core để tìm kiếm thực thể
                objects, draw_img = core_process_frame(frame, frame_number=frame_idx)

                # Theo yêu cầu: Chỉ cần detect ĐƯỢC đối tượng (bất kể dính lỗi hay không) là dừng luôn
                if len(objects) > 0:
                    detected_objects = objects
                    
                    # Lưu frame đầu tiên phát hiện đối tượng
                    filename = f"video_frame_{uuid.uuid4().hex}.jpg"
                    processed_frame_path = os.path.join(STORAGE_DIR, filename)
                    cv2.imwrite(processed_frame_path, draw_img)
                    
                    found_target = True
                    break # Tìm thấy rồi, dừng vòng lặp quét video luôn

            frame_idx += 1

        cap.release()
        
        # Xóa file video tạm sau khi xử lý xong để giải phóng không gian bộ nhớ
        if os.path.exists(temp_video_path):
            os.remove(temp_video_path)

        # Trường hợp chạy hết video mà không tìm thấy bất kỳ phương tiện/đối tượng nào
        if not found_target:
            return jsonify({
                "processed_url": None,
                "objects": []
            }), 200

        # Khớp chuẩn xác cấu trúc AIProcessingResultDTO
        return jsonify({
            "processed_url": processed_frame_path,
            "objects": detected_objects
        }), 200

    except Exception as e:
        if cap.isOpened():
            cap.release()
        if os.path.exists(temp_video_path):
            os.remove(temp_video_path)
        return jsonify({"error": str(e)}), 500

# ============================================================
# 6. KHỞI CHẠY NGROK & SERVER FLASK
# ============================================================
if __name__ == '__main__':
    ngrok.set_auth_token(NGROK_AUTH_TOKEN)
    public_url = ngrok.connect(5000)
    print(f"\n🚀 [NGROK] Public API Base URL: {public_url}")
    print(f"🔗 Single Image API : {public_url}/api/ai/process-image")
    print(f"🔗 Folder Images API: {public_url}/api/ai/process-folder")
    print(f"🔗 Video Stream API : {public_url}/api/ai/process-video\n")

    app.run(host='0.0.0.0', port=5000)

# Single Image API : NgrokTunnel: "https://donnell-gangliar-luz.ngrok-free.dev" -> "http://localhost:5000"/api/ai/process-image
# Folder Images API: NgrokTunnel: "https://donnell-gangliar-luz.ngrok-free.dev" -> "http://localhost:5000"/api/ai/process-folder
# Video Stream API : NgrokTunnel: "https://donnell-gangliar-luz.ngrok-free.dev" -> "http://localhost:5000"/api/ai/process-video