# Upload Image/Folder/Video Functionality

## Overview

This document describes the complete upload functionality that integrates Spring Boot with the Python Flask AI Model Service via NgrokTunnel.

## Architecture

### Components

1. **UploadApiController** - REST endpoints for file uploads
2. **UploadService** - Business logic for managing upload sessions
3. **AIService** - Interface to call Flask AI Model Service
4. **FileStorageService** - Local file storage management
5. **DTOs** - Data transfer objects for requests/responses

### Flow

```
User Upload → UploadApiController → UploadService → FileStorage 
                                  ↓
                           Upload to DB
                                  ↓
                           Process request
                                  ↓
                           AIService → Flask API (via Ngrok)
                                  ↓
                           Save AI results to DB
                                  ↓
                           Return response
```

## API Endpoints

### Base URL
```
http://localhost:8080/api/upload
```

### 1. Upload Single Image

**Endpoint:** `POST /api/upload/image`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/upload/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/image.jpg"
```

**Response:**
```json
{
  "sessionId": 1,
  "uploadType": "SINGLE_IMAGE",
  "status": "PROCESSING",
  "videoUrl": null,
  "createdAt": "2025-05-28T10:30:00",
  "mediaFiles": [
    {
      "id": 1,
      "originalUrl": "D:/MyDrive/DATN/storage/image/uuid.jpg",
      "aiStatus": "UNCHECKED"
    }
  ]
}
```

### 2. Upload Folder of Images

**Endpoint:** `POST /api/upload/folder`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/upload/folder" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "files=@/path/to/image3.jpg"
```

**Response:**
```json
{
  "sessionId": 2,
  "uploadType": "FOLDER",
  "status": "PROCESSING",
  "videoUrl": null,
  "createdAt": "2025-05-28T10:35:00",
  "mediaFiles": [
    {
      "id": 2,
      "originalUrl": "D:/MyDrive/DATN/storage/folder/session-2/uuid1.jpg",
      "aiStatus": "UNCHECKED"
    },
    {
      "id": 3,
      "originalUrl": "D:/MyDrive/DATN/storage/folder/session-2/uuid2.jpg",
      "aiStatus": "UNCHECKED"
    },
    {
      "id": 4,
      "originalUrl": "D:/MyDrive/DATN/storage/folder/session-2/uuid3.jpg",
      "aiStatus": "UNCHECKED"
    }
  ]
}
```

### 3. Upload Video

**Endpoint:** `POST /api/upload/video`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/upload/video" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/video.mp4"
```

**Response:**
```json
{
  "sessionId": 3,
  "uploadType": "VIDEO",
  "status": "PROCESSING",
  "videoUrl": "D:/MyDrive/DATN/storage/video/uuid.mp4",
  "createdAt": "2025-05-28T10:40:00",
  "mediaFiles": [
    {
      "id": 5,
      "originalUrl": "D:/MyDrive/DATN/storage/video/uuid.mp4",
      "aiStatus": "UNCHECKED"
    }
  ]
}
```

### 4. Process Upload Session with AI

**Endpoint:** `POST /api/upload/{sessionId}/process`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/upload/1/process"
```

**Response:**
```
Processing completed successfully for session: 1
```

## Database Schema

### UploadSession Table
- `id` - Primary key
- `user_id` - Foreign key to User
- `upload_type` - ENUM: SINGLE_IMAGE, FOLDER, VIDEO
- `status` - ENUM: PROCESSING, AI_PROCESSED, FEEDBACKING, FINALIZED
- `video_url` - URL of video if upload_type is VIDEO
- `created_at` - Timestamp

### MediaFile Table
- `id` - Primary key
- `session_id` - Foreign key to UploadSession
- `original_url` - Path to original file
- `processed_url` - Path to processed file (set after AI processing)
- `ai_status` - Status: UNCHECKED, CORRECT, INCORRECT

### DetectedViolation Table
- `id` - Primary key
- `media_id` - Foreign key to MediaFile
- `violation_types` - Array of violation types (PostgreSQL text[])
- `bounding_box` - JSON object with coordinates {xmin, ymin, xmax, ymax}
- `confidence` - Confidence score from AI model
- `frame_number` - Frame number if from video
- `is_authority_corrected` - Boolean for authority feedback

## Configuration

### application.properties

```properties
# AI Service Configuration
app.ai.service.url=http://localhost:5000

# Local storage paths
app.storage.image-path=D:/MyDrive/DATN/storage/image
app.storage.folder-path=D:/MyDrive/DATN/storage/folder
app.storage.video-path=D:/MyDrive/DATN/storage/video

# Multipart upload limits
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

## Supported File Types

### Images
- jpg, jpeg
- png
- webp

### Videos
- mp4
- mov
- avi
- mkv

## Processing Flow

### Single Image Processing
1. Upload image via `/api/upload/image`
2. Receive session ID and upload response
3. Call `/api/upload/{sessionId}/process` to trigger AI
4. AI Service calls Flask endpoint: `POST /api/ai/process-image`
5. Results are saved to database

### Folder Processing
1. Upload multiple images via `/api/upload/folder`
2. Receive session ID and upload response with all media files
3. Call `/api/upload/{sessionId}/process` to trigger AI
4. AI Service calls Flask endpoint: `POST /api/ai/process-folder`
5. Results for each image are saved to database

### Video Processing
1. Upload video via `/api/upload/video`
2. Receive session ID and upload response
3. Call `/api/upload/{sessionId}/process` to trigger AI
4. AI Service calls Flask endpoint: `POST /api/ai/process-video`
5. Flask service extracts frames (every 3 frames) until it detects violations
6. Results from the first detected frame are saved to database

## AI Model Response Structure

The Flask AI service responds with:

```json
{
  "processed_url": "/path/to/processed/image.jpg",
  "objects": [
    {
      "object_id": 1,
      "violation_types": ["NO_HELMET", "USING_PHONE"],
      "bbox": {
        "xmin": 100,
        "ymin": 150,
        "xmax": 300,
        "ymax": 450
      },
      "confidence": 0.95,
      "frame_number": null
    }
  ]
}
```

## Error Handling

### Validation Errors
- Empty files: 400 Bad Request
- Invalid file types: 400 Bad Request
- Wrong number of files for upload type: 400 Bad Request

### Processing Errors
- Session not found: 404 Not Found
- AI service unavailable: 500 Internal Server Error
- File storage error: 500 Internal Server Error

## Running the Application

### Prerequisites
1. Java 17+
2. Spring Boot 3.x
3. PostgreSQL database
4. Python Flask AI service running on port 5000
5. Ngrok tunnel configured (if using public endpoint)

### Starting the Application

```bash
mvn clean install
mvn spring-boot:run
```

### Starting Flask AI Service

```bash
cd path/to/AiModelService
python AiModelService.py
```

The Flask service will print:
```
🚀 [NGROK] Public API Base URL: https://xxxxx-ngrok-free.dev
🔗 Single Image API : https://xxxxx-ngrok-free.dev/api/ai/process-image
🔗 Folder Images API: https://xxxxx-ngrok-free.dev/api/ai/process-folder
🔗 Video Stream API : https://xxxxx-ngrok-free.dev/api/ai/process-video
```

## Example Usage Scenario

```bash
# Step 1: Upload image
curl -X POST "http://localhost:8080/api/upload/image" \
  -F "file=@image.jpg" \
  > upload_response.json

# Extract sessionId from response (e.g., 1)
SESSION_ID=1

# Step 2: Process the uploaded image with AI
curl -X POST "http://localhost:8080/api/upload/${SESSION_ID}/process"

# Step 3: Query database to see results
# DetectedViolation table will have entries with violation_types and bounding_box
```

## Troubleshooting

### Flask service not responding
- Check if Flask is running: `http://localhost:5000`
- Verify Ngrok tunnel is active: `https://xxxxx-ngrok-free.dev`
- Check Flask logs for errors

### File storage errors
- Verify storage paths exist and are writable
- Check disk space
- Check user permissions

### Database errors
- Verify PostgreSQL connection
- Check if tables are created
- Review database logs

## Future Enhancements

- [ ] Async processing with message queue (Kafka/RabbitMQ)
- [ ] Progress tracking for large batch operations
- [ ] Retry mechanism for failed AI requests
- [ ] Caching of AI results
- [ ] Support for additional file formats
- [ ] Webhook notifications for processing completion
