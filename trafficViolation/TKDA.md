Tên đề tài: hệ thống nhận diện lỗi giao thông

1. Mô tả: Hệ thống web sử dụng để nhận diện lỗi giao thông trên dữ liệu mà người dùng upload.Hệ thống có thể nhận diện trong các đối tượng trong file dữ liệu mà user upload lên có vi phạm các lỗi [“không đội mũ”, “dùng điện thoại”, “đèo 3 người hay không. Hệ thống có 3 role với main role là ROLE\_AUTHORITY dành cho cơ quan chức năng, 2 role còn lại là ROLE\_USER dành cho người dân bình thường muốn đóng góp phát triển hệ thống, ROLE\_ADMIN dùng cho quản trị viên dùng để quản lí tài khoản.
1. Công nghệ sử dụng:
- FE: html, css, js, bootstrap
- BE: Spring boot 
- AI model service: Python (Flask)
- Database: Postgresql
1. Chức năng chính:
- Authority

  + Upload dữ liệu (ảnh, thư mục, video)

  + Kiểm tra kết quả được trả về từ AI

  + Review feedback của người dân (feedback về việc AI xử lí sai hoặc thiếu)

  + Xem thống kê

- User

  + Upload dữ liệu

  + Xem kết quả trả về từ AI model

  + Tạo feedback về AI

- Admin

  + Quản lí tài khoản user

  + Tạo tài khoản authority cho cơ quan chức năng

  + Retrain AI model với dữ liệu được sau khi thêm (đủ)

1. Mô tả chi tiết chức năng
- Authority

  + Upload dữ liệu: cơ quan chức năng có thể lựa chọn upload ảnh/folder ảnh/video rồi upload hoặc kéo thả dữ liệu vào. Mỗi lần gửi dữ liệu chỉ có thể là 1 ảnh, 1 folder ảnh hoặc 1 video. Sau đó có thể gửi đi để AI xử lí

  + Kiểm tra kết quả được trả về từ AI: Kết quả trả về từ AI sẽ hiện thị dưới dạng danh sách với mỗi hàng là 1 lần gửi dữ liệu đi, khi click vào sẽ hiển thị chi tiết dữ liệu và kết quả trả về. Ví dụ: sau khi gửi đi 1 folder gồm 100 ảnh, khi cơ quan chức năng sẽ vào xem kết quả sẽ thấy danh sách các lần gửi dữ liệu và sẽ thấy 1 dòng mới nhất với trạng thái đã xử lí, khi click vào để xem chi tiết thì sẽ xem được danh sách 100 ảnh với nhãn sau khi AI đã xử lí.

  + Review feedback từ người dân: Sau khi người dân upload dữ liệu và kiểm tra thấy kết quả sai thì sẽ tạo feedback gửi đi. Cơ quan chức năng sau khi thấy feedback sẽ check lại, nếu sai thì sẽ chỉnh sữa nhãn cho đúng rồi trả về cho người dân, còn đúng rồi thì sẽ reject feedback.

  + Xem thống kê: cơ quan chức năng sẽ xem được thống kê trong tháng, có tổng cộng bao nhiêu ảnh/video được upload, xem được tổng số vi phạm mỗi loại (không đội mũ, dùng điện thoại, đèo 3 người)

- User:

  + Upload dữ liệu: cũng tương tự cơ quan chức năng (ROLE\_AUTHORITY).

  + Xem kết quả được trả về từ AI model: Sau khi upload dữ liệu, người dân có thể xem được kết quả AI xử lí ảnh/video của mình. Người dân chỉ có thể xem mà không được chỉnh sửa. Nếu kết quả trả về đúng, user có thể lưu lại, dữ liệu này sẽ được lưu để sau cải thiện AI model.

  + Tạo feedback: trong trường hợp thấy AI xử lí sai, người dân có thể tạo feedback rồi gửi đi, để cơ quan chức năng review và chỉnh sửa. Lúc này dữ liệu từ trạng thái AI xử lí xong chuyển sang trạng thái đang feedback. Sau khi cơ quan chức năng chỉnh sửa và trả về quả thì user check lại, nếu đúng thì lưu còn vẫn sai thì bỏ qua. Trạng thái sau khi cơ quan chức năng đã review là trạng thái cuối cùng.

- Admin:

  + Quản lí user: quản lí danh sách user và có thể khóa/mở khóa tài khoản

  + Tạo tài khoản cho cơ quan chức năng: admin có thể tạo tài khoản có role = ROLE\_AUTHORITY dành cho cơ quan chức năng. Ở giao diện đăng ký chỉ có thể tạo tài khoản có role = ROLE\_USER. 

  + Retrain AI model: admin có thể retrain AI model khi có lượng dữ liệu tăng để cải thiện độ chính xác.                                                                           

1. Non-functional Requirements
- Performance

  + Xử lí thư mục ảnh < 10mb

  + Xử lí video < 10s

- Maintainability

  + Có thể thay model mới hơn sau này

  + Có thể thêm loại vi phạm mới sau này

- Security

  + Giới hạn file upload

- Reliability

  + Không crash khi ảnh lỗi

  + Handel exception đầy đủ

1. Database design
- **Bảng users** (Quản lý tài khoản)

id (bigint, PK)

username, password, full\_name

is\_active (boolean) - Dùng cho Admin khóa/mở khóa tài khoản.

- **Bảng user\_roles, roles** (users-roles quan hệ N-N)

role (ROLE\_ADMIN, ROLE\_AUTHORITY, ROLE\_USER)

- **Bảng upload\_sessions** (Quản lý lượt gửi dữ liệu)

**/Thay thế cho bảng reports cũ để quản lý cả Folder và Video./**

id (bigint, PK)

user\_id (bigint, FK) - Người upload (User hoặc Authority).

upload\_type (varchar) - SINGLE\_IMAGE, FOLDER, VIDEO.

status (varchar) - PROCESSING (Đang xử lý), AI\_PROCESSED (AI đã xử lý xong), FEEDBACKING (Người dân đang feedback), FINALIZED (Đã duyệt/Sửa đổi xong từ Authority).

created\_at (timestamp)

- **Bảng media\_files** (Chi tiết từng ảnh trong folder hoặc khung hình video)

id (bigint, PK)

session\_id (bigint, FK -> upload\_sessions.id)

original\_url (varchar) - Link ảnh gốc.

processed\_url (varchar) - Link ảnh đã vẽ bounding box lỗi.

ai\_status (varchar) - CORRECT, INCORRECT (Đánh dấu sau khi hậu kiểm để gom dữ liệu retrain).

- **Bảng detected\_violations** (Chi tiết lỗi do AI hoặc Authority xác nhận)

id (bigint, PK)

media\_id (bigint, FK -> media\_files.id)

violation\_type (text[]) - NO\_HELMET, USING\_PHONE, TRIPLE\_RIDING.

bounding\_box (jsonb) - Toạ độ vẽ khung.

is\_authority\_corrected (boolean) - Đánh dấu nếu lỗi này do con người sửa lại chứ không phải AI tìm ra.

- **Bảng feedbacks** (Quản lý phản hồi của người dân)

id (bigint, PK)

session\_id (bigint, FK -> upload\_sessions.id) - Feedback theo lượt upload.

user\_id (bigint, FK) - Người tạo feedback.

description (text) - Lý do feedback (AI sót lỗi, AI nhận diện sai...).

status (varchar) - PENDING, APPROVED (Authority chấp nhận và đã sửa), REJECTED (Authority bác bỏ).

handled\_by (bigint, FK -> users.id) - Cán bộ xử lý.

- **Bảng monthly\_statistic**: Thống kê 
- **Bảng notification**: thông báo

#### **Bảng users (Thông tin tài khoản toàn hệ thống)**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**id**|bigint|PRIMARY KEY, IDENTITY|Khóa chính tự tăng định danh tài khoản.|
|**username**|varchar(255)|NOT NULL, UNIQUE|Tài khoản đăng nhập của người dùng.|
|**password**|varchar(255)|NOT NULL|Mật khẩu đã được mã hóa Bcrypt an toàn.|
|**full\_name**|varchar(255)|NOT NULL|Họ và tên đầy đủ của người sử dụng hoặc cán bộ.|
|**is\_active**|boolean|NOT NULL, DEFAULT true|Trạng thái hoạt động (true: Mở khóa, false: Bị Admin khóa).|
|**created\_at**|timestamp|DEFAULT CURRENT\_TIMESTAMP|Thời điểm tạo tài khoản.|
#### **Bảng roles**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**name**|<p>varchar(20)</p><p></p>|PRIMARY KEY, IDENTITY|Khóa chính|
|**description**|varchar(255)|NOT NULL|Mô tả role|

**Bảng trung gian: user\_roles**

#### **Bảng upload\_sessions (Quản lý các đợt tải dữ liệu)**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**id**|bigint|PRIMARY KEY, IDENTITY|Khóa chính định danh phiên xử lý dữ liệu.|
|**user\_id**|bigint|FOREIGN KEY REFERENCES users(id)|ID của tài khoản thực hiện tải lên dữ liệu.|
|**upload\_type**|varchar(50)|NOT NULL|Hình thức đóng gói dữ liệu: SINGLE\_IMAGE, FOLDER, VIDEO.|
|**status**|varchar(50)|NOT NULL|Vòng đời trạng thái: PROCESSING, AI\_PROCESSED, FEEDBACKING, FINALIZED.|
|**created\_at**|timestamp|DEFAULT CURRENT\_TIMESTAMP|Thời gian tải lên hệ thống.|
#### **Bảng media\_files (Quản lý chi tiết từng tệp ảnh con/khung hình)**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**id**|bigint|PRIMARY KEY, IDENTITY|Khóa chính định danh ảnh đơn hoặc frame trích xuất từ video.|
|**session\_id**|bigint|FOREIGN KEY REFERENCES upload\_sessions(id) ON DELETE CASCADE|Liên kết trực thuộc phiên upload nào (Bảo toàn quan hệ 1-N).|
|**original\_url**|varchar(500)|NOT NULL|Đường dẫn URL truy cập ảnh gốc chưa qua xử lý.|
|**processed\_url**|varchar(500)|Nullable|Đường dẫn URL truy cập ảnh kết quả đã được AI/Cán bộ vẽ bounding box.|
|**ai\_status**|varchar(50)|DEFAULT 'UNCHECKED'|Đánh dấu kiểm định phục vụ gom dữ liệu học máy: CORRECT, INCORRECT, UNCHECKED.|
#### **Bảng detected\_violations (Chi tiết tọa độ và nhãn lỗi vi phạm)**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**id**|bigint|PRIMARY KEY, IDENTITY|Khóa chính định danh lỗi.|
|**media\_id**|bigint|FOREIGN KEY REFERENCES media\_files(id) ON DELETE CASCADE|Lỗi nằm trong tệp tin hình ảnh/khung hình nào.|
|**violation\_type**|text[]|NOT NULL|Loại vi phạm: NO\_HELMET (Không mũ), USING\_PHONE (Dùng điện thoại), TRIPLE\_RIDING (Chở ba).|
|**bounding\_box**|jsonb|NOT NULL|Dữ liệu tọa độ đối tượng định dạng JSON: {"xmin": int, "ymin": int, "xmax": int, "ymax": int}.|
|**is\_authority\_corrected**|boolean|DEFAULT false|Cờ đánh dấu (false: Do AI nhận diện tự động, true: Do cán bộ sửa đổi/thêm thủ công).|
#### **Bảng feedbacks (Quản lý các phản hồi từ người dân)**

|Tên trường|Kiểu dữ liệu|Ràng buộc|<p>Mô tả chức năng</p><p> </p>|
| :-: | :-: | :-: | :-: |
|**id**|bigint|PRIMARY KEY, IDENTITY|Khóa chính định danh đơn phản hồi.|
|**session\_id**|bigint|FOREIGN KEY REFERENCES upload\_sessions(id), UNIQUE|Liên kết đến đợt dữ liệu bị phản hồi (Mỗi đợt chỉ có tối đa 1 feedback).|
|**user\_id**|bigint|FOREIGN KEY REFERENCES users(id)|ID của người dân tạo đơn phản hồi lỗi hệ thống.|
|**description**|text|NOT NULL|Nội dung người dân mô tả lỗi sai (ví dụ: AI bỏ sót lỗi không mũ bảo hiểm).|
|**status**|varchar(50)|NOT NULL|Trạng thái xử lý phản hồi: PENDING, APPROVED, REJECTED.|
|**handled\_by**|bigint|FOREIGN KEY REFERENCES users(id)|ID của cán bộ thực hiện duyệt/bác bỏ đơn phản hồi này.|
|**handled\_at**|timestamp|Nullable|Thời điểm cán bộ hoàn thành xử lý đơn phản hồi.|

Quan hệ: 

- users — user\_roles — roles (Quan hệ Nhiều - Nhiều / N:M):

Giải quyết thông qua bảng trung gian user\_roles. Một người dùng có thể sở hữu một hoặc nhiều quyền.

- users — upload\_sessions (Quan hệ Một - Nhiều / 1:N):

Một tài khoản người dùng (hoặc cán bộ chức năng) có thể thực hiện tải lên dữ liệu rất nhiều lần (upload\_sessions). 

- upload\_sessions — media\_files (Quan hệ Một - Nhiều / 1:N):

Một đợt gửi dữ liệu (upload\_session) có thể chứa nhiều tệp tin con bên trong nếu người dùng chọn phương thức upload theo Thư mục ảnh (FOLDER)

- media\_files — detected\_violations (Quan hệ Một - Nhiều / 1:N):

Trên một bức ảnh hoặc một khung hình cụ thể (media\_file), mô hình AI hoặc cán bộ chức năng có thể phát hiện và khoanh vùng nhiều đối tượng khác nhau cùng một lúc. 

- upload\_sessions — feedbacks (Quan hệ Một - Một / 1:1 Tuyệt đối):

Được biểu diễn qua liên kết từ upload\_sessions đến feedbacks với khóa ngoại mang ràng buộc UNIQUE (FK, U). Ý nghĩa: Để tránh việc người dân gửi spam dồn dập, mỗi đợt tải dữ liệu lên chỉ được phép gắn liền với tối đa 01 đơn phản hồi (Feedback) duy nhất. 

- users — feedbacks (Quan hệ Một - Nhiều / 1:N):

user\_id: Một người dân bình thường có thể tạo nhiều đơn feedback gửi lên hệ thống. 

handled\_by: Một cán bộ kiểm duyệt (ROLE\_AUTHORITY) có thẩm quyền tiếp nhận và bấm xử lý phê duyệt/bác bỏ cho nhiều đơn feedback khác nhau của người dân.


**\*Mô tả chi tiết chức năng chính:**

\- Chức năng upload data (ROLE\_AUTHORITY + ROLE\_USER)

\+ Khi cơ quan chức năng (authority) upload data, data có thể là ảnh đơn, folder ảnh, hoặc video. Trường hợp là single image thì 1 upload\_session chỉ có 1 ảnh, khi upload sẽ lưu vào storage (hiện tại chạy trên local thì chỉ lưu trong bộ nhớ, vị trí lưu ảnh sẽ lưu trong database tại trường original\_url trong bảng media\_files), sau đó sẽ call AI model service bên Flask để xử lí hình ảnh, Phía Ai service, sau khi xử lí ảnh xong, vẽ bbox lên ảnh và lưu vào storage,sau đó sẽ trả về file json với thông tin gồm: object\_id, violation\_type[], bbox, processed\_url(vị trí ảnh được lưu sau khi vẽ bbox). Mỗi đối tượng được detect trong 1 media\_file sẽ tương ứng với 1 bản ghi trong bảng detected\_violation.

\+ Trường hợp user upload folder ảnh thì cũng xử lí từng ảnh như single image và trà về json với đầy đủ thông tin của mỗi single image

\+ Trường hợp upload video, AI model service chỉ xử lí frame đầu tiên detect được đối tượng, nên json trả về sẽ giống single image, nhưng original\_url khi lưu trong lần upload sẽ lưu video, còn processed\_url trả về sẽ là vị trí lưu frame đã được vẽ bbox.

\- Chức năng xem kết quả(ROLE\_USER)

Sau khi upload, user chọn chức năng xem kết quả, có thể thấy trạng thái của upload\_session, nếu chuyển từ PROCESSING sang AI\_PROCESSED là AI đã xử lí xong, ấn vào đúng upload\_session sẽ hiển thị media\_files (tùy theo những loại dữ liệu đã upload). Nếu là singe image thì ấn vào sẽ hiển thị ảnh đã được vẽ bbox, nhãn dự đoán sẽ ở bên cạnh phải. Trường hợp folder cũng tương tự nhưng có nhiều ảnh, với mỗi ảnh ở bên phải sẽ là nhãn đã được dự đoán. Ví dụ upload ảnh 1.jpg, ảnh được xử lí, vẽ bbox và hiển thị ở giữa, bên phải sẽ là 

object\_id: 1, vi phạm: [“không đội mũ”, “dùng điện thoại”],

` `object\_id: 2, vi phạm []\
Vì id trong detected\_violation là khóa chính nên không thể trùng nhau, nên khi lưu thêm 1 object sẽ cộng 1 vào id, khi hiển thị sẽ hiển thị id đó.

\- Chức năng kiểm tra kết quả trả về từ AI (ROLE\_AUTHORITY)

Cũng tương tự như chức năng “Xem kết quả” của ROLE\_USER, nhưng khi hiển thị nhãn được dữ đoán sẽ có dạng 

Object\_id: 1, vi phạm [1, 1, 0] ( tương tự [“không đội mũ”, “dùng điện thoại”]), nhưng cơ quan chức năng có thể chính sửa. Ô hiển thị [1, 1, 0] sẽ là text box, authority có thể sửa nhãn và bên cạnh phải có nút save và cancel, nếu ấn nút save sẽ lưu trực tiếp nhãn mới vào database, còn cancel thì bỏ qua không chỉnh sửa gì.

\- Chức năng review feedback (ROLE\_AUTHORITY)\
Khi user gửi feedback về 1 upload\_session nào đó, user có thể xem và chính sửa, giao diện sẽ giống chức năng “xem kết quả trả về từ AI”. Nhưng khác 1 chút là authority có thể reject để bỏ qua feedback này, còn nếu feedback đúng (nghĩa là kết quả từ AI sai hoặc thiếu) thì cơ quan chức năng có thể check lại ảnh đã vẽ bbox, và sửa nhãn rồi save lại.

\* ENUM

public enum ViolationType {

`    `NO\_HELMET,

`    `USING\_PHONE,

`    `TRIPLE\_RIDING

}

\
public enum FeedbackStatus {

`    `PENDING,

`    `APPROVED,

`    `REJECTED

}

public enum SessionStatus {

`    `PROCESSING,

`    `AI\_PROCESSED,

`    `FEEDBACKING,

`    `FINALIZED

}

public enum UploadType {

`    `SINGLE\_IMAGE,

`    `FOLDER,

`    `VIDEO

}\
\
**Lưu ý: Trong code sẽ sử dụng enum, nhưng trong database vẫn sẽ dùng VARCHAR, violation\_type dùng text[]**

API upload image
@app.route('/api/ai/process-image', methods=['POST'])
def process_image():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "Empty filename"}), 400

    try:
        # Đọc dữ liệu ảnh từ luồng Request
        file_bytes = np.frombuffer(file.read(), np.uint8)
        img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        orig_img = img.copy()

        # Pha 1: Trích xuất vật thể qua YOLOv8
        yolo_results = yolo_model(img, conf=0.25)

        detected_violations_list = []
        object_counter = 1

        for result in yolo_results:
            boxes = result.boxes.xyxy.cpu().numpy()
            confidences = result.boxes.conf.cpu().numpy()

            for box, yolo_conf in zip(boxes, confidences):
                x1, y1, x2, y2 = map(int, box)
                x1, y1 = max(0, x1), max(0, y1)
                x2, y2 = min(img.shape[1], x2), min(img.shape[0], y2)

                # Trích xuất vùng ảnh đối tượng
                cropped_img = orig_img[y1:y2, x1:x2]
                if cropped_img.size == 0:
                    continue

                # Tiền xử lý giữ nguyên tỷ lệ qua hàm Letterbox
                pil_img = letterbox_resize(cropped_img, target_size=(IMG_SIZE, IMG_SIZE))
                input_tensor = val_transform(pil_img).unsqueeze(0).to(DEVICE)

                # Pha 2: Đưa vào ResNet18 + CBAM phân loại đa nhãn
                with torch.no_grad():
                    outputs = resnet_model(input_tensor)
                    probs = torch.sigmoid(outputs).squeeze().cpu().numpy()

                # So sánh độc lập điểm xác suất với mảng ngưỡng tối ưu (saved_thresholds)
                active_violations = []
                for idx, prob in enumerate(probs):
                    if prob >= saved_thresholds[idx]:
                        # Chuẩn hóa chuỗi nhãn viết hoa trùng khớp cấu trúc PostgreSQL ENUM của bạn
                        active_violations.append(LABEL_COLS[idx].upper())

                # Nếu không dính lỗi, ghi nhận trạng thái bình thường
                if len(active_violations) == 0:
                    active_violations.append("NORMAL")

                # Cấu trúc trả về đồng bộ hoàn chỉnh với JSONB Schema trong DB
                detected_violations_list.append({
                    "object_tracking_id": object_counter,
                    "violation_types": active_violations,
                    "confidence": float(yolo_conf),
                    "bounding_box": {
                        "x1": x1,
                        "y1": y1,
                        "x2": x2,
                        "y2": y2
                    }
                })
                object_counter += 1

        return jsonify({
            "status": "SUCCESS",
            "total_detected_objects": len(detected_violations_list),
            "detections": detected_violations_list
        }), 200

    except Exception as e:
        return jsonify({"status": "FAILED", "error": str(e)}), 500

[NGROK] Public API Endpoint: NgrokTunnel: "https://donnell-gangliar-luz.ngrok-free.dev" -> "http://localhost:5000"/api/ai/process-image
