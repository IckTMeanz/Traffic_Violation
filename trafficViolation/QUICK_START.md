# Quick Start Guide - Upload Feature Setup

## Prerequisites

✅ **Before you start, ensure you have:**
- Java 17+
- Spring Boot 3.x
- PostgreSQL 12+
- Python 3.8+ (for Flask AI service)
- Visual Studio Code or IntelliJ IDEA

## 1. Database Setup

### Create PostgreSQL Database

```sql
-- Connect to PostgreSQL as admin
psql -U postgres

-- Create database
CREATE DATABASE traffic_violation;

-- Connect to database
\c traffic_violation

-- Tables will be auto-created by Hibernate/JPA when Spring Boot starts
```

### Verify Connection in application.properties

```properties
spring.datasource.url=jdbc:postgresql://ep-lucky-art-aoytd2yj-pooler.c-2.ap-southeast-1.aws.neon.tech/traffic1?sslmode=require&channel_binding=require  
spring.datasource.username=neondb_owner
spring.datasource.password=npg_50hocAGESrCm
```

## 2. Start Flask AI Service

### Prerequisites

```bash
# Install Python packages
pip install flask pyngrok opencv-python torch torchvision ultralytics pillow

# Or install from requirements.txt
pip install -r requirements.txt
```

### Run Flask Service

```bash
cd /path/to/AiModelService
python AiModelService.py
```

**Expected Output:**
```
✅ Models loaded. Optimal Thresholds: [0.5, 0.5, 0.5]

🚀 [NGROK] Public API Base URL: https://donnell-gangliar-luz.ngrok-free.dev
🔗 Single Image API : https://donnell-gangliar-luz.ngrok-free.dev/api/ai/process-image
🔗 Folder Images API: https://donnell-gangliar-luz.ngrok-free.dev/api/ai/process-folder
🔗 Video Stream API : https://donnell-gangliar-luz.ngrok-free.dev/api/ai/process-video
```

**Note:** Keep this terminal open while running tests!

## 3. Start Spring Boot Application

### From Command Line

```bash
cd trafficViolation
./mvnw spring-boot:run
```

### From IntelliJ IDEA

1. Open project
2. Right-click on `TrafficViolationApplication.java`
3. Click "Run"

**Expected Output:**
```
2025-05-28 10:00:00.000  INFO ... Application started in 8.234 seconds
```

## 4. Test the Upload Feature

### Using VS Code REST Client Extension

1. Install "REST Client" extension by Huachao Zheng
2. Open `UPLOAD_API_EXAMPLES.http`
3. Click "Send Request" above each API call

### Using Postman

1. Import the provided Postman collection (or create manually)
2. Set base URL: `http://localhost:8080`
3. Create requests for each endpoint

### Using curl

```bash
# Single Image Upload
curl -X POST "http://localhost:8080/api/upload/image" \
  -F "file=@/path/to/image.jpg"

# Get sessionId from response, e.g., "sessionId": 1

# Process the session
curl -X POST "http://localhost:8080/api/upload/1/process"
```

## 5. Verify Data in Database

### Using pgAdmin or psql

```sql
-- Check upload sessions
SELECT * FROM upload_sessions ORDER BY created_at DESC LIMIT 1;

-- Check media files
SELECT * FROM media_files ORDER BY id DESC LIMIT 5;

-- Check detected violations
SELECT * FROM detected_violations ORDER BY id DESC LIMIT 5;

-- Check detected violation details
SELECT 
  dv.id,
  dv.violation_types,
  dv.bounding_box,
  dv.confidence,
  dv.frame_number,
  mf.original_url,
  mf.processed_url
FROM detected_violations dv
JOIN media_files mf ON dv.media_id = mf.id
ORDER BY dv.id DESC LIMIT 10;
```

## 6. Directory Structure

Verify these directories exist:

```
D:/MyDrive/DATN/storage/
  ├── image/              # Single image uploads
  ├── folder/             # Batch image uploads
  ├── video/              # Video uploads
  └── detected_image/     # AI processed results
```

Create if missing:

```powershell
# PowerShell
$dirs = @(
  "D:/MyDrive/DATN/storage/image",
  "D:/MyDrive/DATN/storage/folder",
  "D:/MyDrive/DATN/storage/video",
  "D:/MyDrive/DATN/storage/detected_image"
)
$dirs | ForEach-Object { New-Item -ItemType Directory -Force -Path $_ }
```

## 7. Common Issues & Solutions

### Issue: "Connection refused" to Flask service

**Solution:**
```bash
# Check if Flask is running
curl http://localhost:5000

# If not running, start Flask service (see step 2)
python AiModelService.py

# Check if port 5000 is in use
netstat -ano | findstr :5000
```

### Issue: "File storage error" or "Access denied"

**Solution:**
```bash
# Check if storage directories exist and are writable
ls -la D:/MyDrive/DATN/storage/

# Create missing directories
mkdir -p D:/MyDrive/DATN/storage/{image,folder,video,detected_image}

# Check permissions (on Windows)
icacls "D:/MyDrive/DATN/storage" /grant "%USERNAME%:F" /T
```

### Issue: "Authenticated user not found"

**Solution:**
```sql
-- Make sure user exists in database
SELECT * FROM users WHERE username = 'your_username';

-- If not, create one (adjust as needed)
INSERT INTO users (username, password, full_name, role, is_active)
VALUES ('testuser', '$2a$10$...', 'Test User', 'ADMIN', true);
```

### Issue: Database connection timeout

**Solution:**
```properties
# Update connection string if needed
spring.datasource.url=jdbc:postgresql://localhost:5432/traffic_violation
```

### Issue: Models not found in Flask service

**Solution:**
```python
# Update paths in AiModelService.py
YOLO_PATH = "path/to/best.pt"
RESNET_PATH = "path/to/best_resnet18_v2.pth"

# Ensure models are downloaded before running Flask
```

## 8. Testing Checklist

- [ ] Flask service running on port 5000
- [ ] Spring Boot running on port 8080
- [ ] PostgreSQL database connected
- [ ] Storage directories exist and accessible
- [ ] Can upload single image via `/api/upload/image`
- [ ] Can upload folder via `/api/upload/folder`
- [ ] Can upload video via `/api/upload/video`
- [ ] Can trigger processing via `/api/upload/{id}/process`
- [ ] Data saved in database correctly
- [ ] Processed images generated
- [ ] Violations detected and stored

## 9. Next Steps

### Testing in Detail
1. Read [UPLOAD_FEATURE_GUIDE.md](UPLOAD_FEATURE_GUIDE.md) for detailed API documentation
2. Check [ARCHITECTURE_DIAGRAMS.md](ARCHITECTURE_DIAGRAMS.md) for system design
3. Review [UPLOAD_API_EXAMPLES.http](UPLOAD_API_EXAMPLES.http) for example requests

### Integration
1. Integrate with UI/Frontend
2. Add authorization checks for endpoints
3. Implement feedback collection for false positives
4. Add progress tracking for batch operations

### Monitoring
1. Add logging and metrics
2. Setup error alerting
3. Monitor database growth
4. Track AI model performance

## 10. File Reference

### Core Implementation Files
- [AIServiceImpl.java](src/main/java/vn/icktmeanz/trafficViolation/service/implement/AIServiceImpl.java) - AI integration
- [UploadServiceImpl.java](src/main/java/vn/icktmeanz/trafficViolation/service/implement/UploadServiceImpl.java) - Upload logic
- [UploadApiController.java](src/main/java/vn/icktmeanz/trafficViolation/controller/api/UploadApiController.java) - REST endpoints

### Documentation Files
- [UPLOAD_FEATURE_GUIDE.md](UPLOAD_FEATURE_GUIDE.md) - Complete feature documentation
- [ARCHITECTURE_DIAGRAMS.md](ARCHITECTURE_DIAGRAMS.md) - System architecture
- [UPLOAD_API_EXAMPLES.http](UPLOAD_API_EXAMPLES.http) - API usage examples
- [AiModelService.py](AiModelService.py) - Flask AI service

### Entity/DTO Files
- [UploadSession.java](src/main/java/vn/icktmeanz/trafficViolation/entity/UploadSession.java)
- [MediaFile.java](src/main/java/vn/icktmeanz/trafficViolation/entity/MediaFile.java)
- [DetectedViolation.java](src/main/java/vn/icktmeanz/trafficViolation/entity/DetectedViolation.java)
- [AIProcessingResultDTO.java](src/main/java/vn/icktmeanz/trafficViolation/dto/response/AIProcessingResultDTO.java)

## Support

For issues or questions:
1. Check [Common Issues & Solutions](#7-common-issues--solutions)
2. Review [UPLOAD_FEATURE_GUIDE.md](UPLOAD_FEATURE_GUIDE.md)
3. Check log files for error details
4. Verify Flask API is responding: `curl http://localhost:5000`
