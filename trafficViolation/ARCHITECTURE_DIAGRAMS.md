# Upload Feature Architecture & Flow Diagrams

## System Architecture

```mermaid
graph TB
    Client["📱 Client Application"]
    Spring["🟢 Spring Boot Server<br/>Port 8080"]
    DB["🗄️ PostgreSQL Database"]
    Storage["💾 Local Storage<br/>Images/Videos"]
    Flask["🐍 Flask AI Service<br/>Port 5000"]
    Ngrok["🔗 Ngrok Tunnel<br/>Public Access"]
    
    Client -->|HTTP Request| Spring
    Spring -->|Save Session/MediaFile| DB
    Spring -->|Store Files| Storage
    Spring -->|HTTP Call| Flask
    Flask -->|Process Files<br/>YOLO + ResNet| Flask
    Flask -->|Public Endpoint| Ngrok
    Flask -->|Response JSON| Spring
    Spring -->|Save Results| DB
    Spring -->|HTTP Response| Client
    
    style Client fill:#FFD700
    style Spring fill:#4CAF50
    style DB fill:#2196F3
    style Storage fill:#FF9800
    style Flask fill:#9C27B0
    style Ngrok fill:#00BCD4
```

## Single Image Upload Flow

```mermaid
sequenceDiagram
    participant User as User/Client
    participant Spring as Spring Boot
    participant FileService as FileStorageService
    participant UploadService as UploadService
    participant DB as Database
    participant AIService as AIService
    participant Flask as Flask AI Service
    
    User->>Spring: POST /api/upload/image
    Spring->>Spring: Validate file (jpg/png/webp)
    Spring->>FileService: storeFile()
    FileService->>FileService: Create directories
    FileService->>FileService: Save with UUID filename
    Spring->>DB: Save UploadSession (PROCESSING)
    Spring->>DB: Save MediaFile
    Spring->>User: Return SessionId
    
    User->>Spring: POST /api/upload/{sessionId}/process
    Spring->>UploadService: processUploadedFiles()
    UploadService->>UploadService: processSingleImage()
    UploadService->>AIService: processImage(imagePath)
    AIService->>Flask: POST /api/ai/process-image
    Flask->>Flask: YOLO Detection
    Flask->>Flask: ResNet Classification
    Flask-->>AIService: JSON Response
    AIService-->>UploadService: AIProcessingResultDTO
    UploadService->>DB: Update MediaFile (processedUrl)
    UploadService->>DB: Save DetectedViolation (for each object)
    UploadService->>DB: Update UploadSession (AI_PROCESSED)
    Spring->>User: Return Success
```

## Folder Upload Flow

```mermaid
sequenceDiagram
    participant User as User/Client
    participant Spring as Spring Boot
    participant FileService as FileStorageService
    participant UploadService as UploadService
    participant DB as Database
    participant AIService as AIService
    participant Flask as Flask AI Service
    
    User->>Spring: POST /api/upload/folder
    Spring->>Spring: Validate N files (all images)
    Spring->>FileService: storeFile() x N
    FileService->>FileService: Create session folder
    FileService->>FileService: Save all files
    Spring->>DB: Save UploadSession (PROCESSING)
    Spring->>DB: Save N MediaFiles
    Spring->>User: Return SessionId + FileList
    
    User->>Spring: POST /api/upload/{sessionId}/process
    Spring->>UploadService: processUploadedFiles()
    UploadService->>UploadService: processFolder()
    loop For each MediaFile
        UploadService->>AIService: processImage(imagePath)
        AIService->>Flask: POST /api/ai/process-image
        Flask->>Flask: YOLO Detection + ResNet Classification
        Flask-->>AIService: JSON Response
        AIService-->>UploadService: AIProcessingResultDTO
        UploadService->>DB: Update MediaFile (processedUrl)
        UploadService->>DB: Save DetectedViolation objects
    end
    UploadService->>DB: Update UploadSession (AI_PROCESSED)
    Spring->>User: Return Success
```

## Video Upload Flow

```mermaid
sequenceDiagram
    participant User as User/Client
    participant Spring as Spring Boot
    participant FileService as FileStorageService
    participant UploadService as UploadService
    participant DB as Database
    participant AIService as AIService
    participant Flask as Flask AI Service
    
    User->>Spring: POST /api/upload/video
    Spring->>Spring: Validate file (mp4/mov/avi/mkv)
    Spring->>FileService: storeFile()
    FileService->>FileService: Save video with UUID filename
    Spring->>DB: Save UploadSession (PROCESSING)
    Spring->>DB: Save MediaFile
    Spring->>DB: Save videoUrl to UploadSession
    Spring->>User: Return SessionId
    
    User->>Spring: POST /api/upload/{sessionId}/process
    Spring->>UploadService: processUploadedFiles()
    UploadService->>UploadService: processVideo()
    UploadService->>AIService: processVideo(videoFile)
    AIService->>Flask: POST /api/ai/process-video
    Flask->>Flask: Open video capture
    Flask->>Flask: Extract frames (every 3 frames)
    Flask->>Flask: YOLO detection on each frame
    Flask->>Flask: Stop when object detected
    Flask->>Flask: Save detected frame
    Flask->>Flask: ResNet classification
    Flask-->>AIService: JSON Response
    AIService-->>UploadService: AIProcessingResultDTO
    UploadService->>DB: Update MediaFile (processedUrl)
    UploadService->>DB: Save DetectedViolation (with frameNumber)
    UploadService->>DB: Update UploadSession (AI_PROCESSED)
    Spring->>User: Return Success
```

## Data Model Relationships

```mermaid
erDiagram
    USER ||--o{ UPLOAD_SESSION : creates
    UPLOAD_SESSION ||--|{ MEDIA_FILE : contains
    MEDIA_FILE ||--o{ DETECTED_VIOLATION : "detects"
    
    USER {
        long id PK
        string username
        string password
    }
    
    UPLOAD_SESSION {
        long id PK
        long user_id FK
        enum upload_type
        enum status
        string video_url
        timestamp created_at
    }
    
    MEDIA_FILE {
        long id PK
        long session_id FK
        string original_url
        string processed_url
        string ai_status
    }
    
    DETECTED_VIOLATION {
        long id PK
        long media_id FK
        array violation_types
        jsonb bounding_box
        float confidence
        int frame_number
        boolean is_authority_corrected
    }
```

## Processing States

```mermaid
stateDiagram-v2
    [*] --> UPLOADING: User uploads file(s)
    UPLOADING --> UPLOADED: Files stored successfully
    UPLOADED --> PROCESSING: Trigger processing
    PROCESSING --> AI_PROCESSED: AI completes
    AI_PROCESSED --> FEEDBACKING: Authority review
    FEEDBACKING --> FINALIZED: Feedback recorded
    FINALIZED --> [*]
    
    PROCESSING --> PROCESSING: Process next file
    
    UPLOADED --> [*]: Session ends
    AI_PROCESSED --> [*]: Session ends
    FEEDBACKING --> [*]: Session ends
```

## AI Service Endpoints Called

```mermaid
graph LR
    UploadService["Upload Service"]
    
    UploadService -->|Single Image| Process1["POST /api/ai/process-image"]
    UploadService -->|Folder| Process2["POST /api/ai/process-folder"]
    UploadService -->|Video| Process3["POST /api/ai/process-video"]
    
    Process1 -->|Via Ngrok| Flask["Flask Port 5000"]
    Process2 -->|Via Ngrok| Flask
    Process3 -->|Via Ngrok| Flask
    
    Flask -->|YOLOv8| Detect["Object Detection"]
    Flask -->|ResNet18+CBAM| Classify["Violation Classification"]
    
    Detect --> Response["JSON Response"]
    Classify --> Response
    
    Response --> DB["Save to Database"]
    
    style UploadService fill:#4CAF50
    style Flask fill:#9C27B0
    style DB fill:#2196F3
```

## Error Handling Flow

```mermaid
flowchart TD
    Start["Upload Request"] --> Validate{Valid Files?}
    
    Validate -->|No| Error1["❌ 400 Bad Request"]
    Validate -->|Yes| Store["Store Files"]
    
    Store --> SaveDB{Database OK?}
    SaveDB -->|No| Error2["❌ 500 DB Error"]
    SaveDB -->|Yes| Return["✅ Return SessionId"]
    
    Return --> Process["Process Request"]
    Process --> AICall{Flask OK?}
    
    AICall -->|No| Error3["❌ 500 AI Error"]
    AICall -->|Yes| SaveResults["Save Results"]
    
    SaveResults --> Finish["✅ Processing Complete"]
    
    Error1 --> End["Client gets error"]
    Error2 --> End
    Error3 --> End
```

## File Organization in Storage

```
D:/MyDrive/DATN/storage/
├── image/
│   ├── uuid1.jpg (original image from session 1)
│   ├── uuid2.png (original image from session 2)
│   └── ...
├── folder/
│   ├── session-2/
│   │   ├── uuid3.jpg (image 1)
│   │   ├── uuid4.jpg (image 2)
│   │   └── uuid5.jpg (image 3)
│   ├── session-5/
│   │   ├── uuid6.jpg
│   │   └── ...
│   └── ...
└── video/
    ├── uuid7.mp4 (original video)
    ├── uuid8.avi (original video)
    └── ...

D:/MyDrive/DATN/storage/detected_image/ (Flask storage)
├── processed_uuid1.jpg (output from AI processing)
├── processed_uuid2.jpg
├── video_frame_uuid3.jpg (extracted frame from video)
└── ...
```

## API Response Time Estimates

| Operation | Duration | Notes |
|-----------|----------|-------|
| Single Image Upload | 100-500ms | File storage only |
| Single Image Processing | 2-5s | YOLO + ResNet inference |
| Folder (5 images) | 10-25s | Sequential processing |
| Video Processing | 5-15s | Frame extraction + detection |

## Database Query Patterns

```sql
-- Find all violations in a session
SELECT dv.* FROM detected_violations dv
JOIN media_files mf ON dv.media_id = mf.id
JOIN upload_sessions us ON mf.session_id = us.id
WHERE us.id = ?;

-- Get statistics by violation type
SELECT violation_types, COUNT(*) FROM detected_violations
GROUP BY violation_types;

-- Find violations by user
SELECT * FROM detected_violations dv
JOIN media_files mf ON dv.media_id = mf.id
JOIN upload_sessions us ON mf.session_id = us.id
JOIN users u ON us.user_id = u.id
WHERE u.id = ?;
```

## Security Considerations

```mermaid
graph TD
    Auth["Authentication Required"]
    Validate["File Validation"]
    Sanitize["Path Sanitization"]
    Access["Access Control"]
    
    Auth -->|Verify Token| Users["Spring Security"]
    Validate -->|Check Extensions| Extensions["jpg, png, webp, mp4..."]
    Validate -->|Check Size| Size["Max 500MB"]
    Sanitize -->|UUID Filenames| NoInjection["Prevent injection"]
    Access -->|User-specific Data| UserDB["Only see own uploads"]
    
    Users --> Success["Request Allowed"]
    Extensions --> Success
    Size --> Success
    NoInjection --> Success
    UserDB --> Success
```
