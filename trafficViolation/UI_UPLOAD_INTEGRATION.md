# Upload Data UI Integration Guide

## 📱 User Interface Integration

The upload functionality has been fully integrated into both user and authority dashboards.

## 🎯 User Dashboard (`/user` - dashboard.html)

### Features Implemented

1. **Upload Section**
   - Select between IMAGE or VIDEO upload
   - Drag-and-drop interface
   - File selection with validation
   - Real-time upload status
   - Direct API integration with `/api/upload/image` or `/api/upload/video`

2. **History Section**
   - View all uploaded sessions
   - Real-time status updates
   - Status badges (PROCESSING, AI_PROCESSED, FEEDBACKING, FINALIZED)
   - View detailed results button
   - Fetches from `/api/upload/list` endpoint

3. **Appeal Section** (For Citizens)
   - Submit appeal for incorrect detections
   - Link appeal to specific upload session
   - Send appeal message to authority
   - One appeal per session constraint

### API Endpoints Used

```javascript
// User Upload
POST /api/upload/image              // Single image
POST /api/upload/video              // Single video

// After upload, trigger processing
POST /api/upload/{sessionId}/process

// View results
GET /api/upload/{sessionId}/result

// List sessions
GET /api/upload/list

// Send appeal (future endpoint)
POST /api/feedback
```

### JavaScript Functions

```javascript
// Main upload function
handleUploadReport(event)
  └─ Uploads file to /api/upload/{type}
  └─ Triggers /api/upload/{id}/process
  └─ Shows success/error alerts
  └─ Refreshes history

// View results
viewTaskResult(reportId)
  └─ Fetches /api/upload/{id}/result
  └─ Displays violations and processed image

// Render history
renderHistoryTable()
  └─ Fetches /api/upload/list
  └─ Populates table with live data
  └─ Updates status badges

// Submit appeal
handleUploadAppeal(event)
  └─ Sends POST /api/feedback
  └─ Logs appeal for authority review
```

---

## 👮 Authority Dashboard (`/authority` - authority.html)

### Features Implemented

1. **Upload Data Section**
   - Three upload types: SINGLE_IMAGE, FOLDER, VIDEO
   - Drag-and-drop support
   - Multi-file selection for folders
   - Two-step process: Upload → Process with AI
   - Real-time progress indicators

2. **Check Result Section**
   - List of upload sessions
   - Detailed violation information
   - Manual correction capability
   - Authority can modify AI predictions
   - Save corrected results

3. **Review Feedback Section**
   - View citizen appeals/feedback
   - Approve or reject appeals
   - Correct labels for dataset improvement
   - Confirm final decisions

4. **Monthly Statistics Section**
   - Total uploads count
   - Violation type distribution
   - Accuracy metrics
   - Monthly trend analysis

### API Endpoints Used

```javascript
// Authority Upload
POST /api/upload/image              // Single image
POST /api/upload/folder             // Multiple images
POST /api/upload/video              // Single video

// Process uploads
POST /api/upload/{sessionId}/process

// Get session details
GET /api/upload/{sessionId}/detail

// Get session results
GET /api/upload/{sessionId}/result

// List all sessions
GET /api/upload/list

// Delete session
DELETE /api/upload/{sessionId}
```

### JavaScript Functions

```javascript
// Upload handler with two-step process
submitUpload()
  └─ Step 1: POST to /api/upload/{type}
  └─ Step 2: POST to /api/upload/{id}/process
  └─ Shows alerts with session info
  └─ Auto-switch to result tab

// Load detail view
loadResultDetail(id)
  └─ Fetches /api/upload/{id}/detail
  └─ Displays violations for review

// Tab switching
switchTab(tabName)
  └─ Navigate between sections
  └─ Show/hide relevant sections
```

---

## 📊 Complete Data Flow

### Single Image Upload Flow (User)

```
User selects IMAGE type
       ↓
User selects file (file picker or drag-drop)
       ↓
User clicks "Gửi báo cáo"
       ↓
handleUploadReport() triggers
       ↓
POST /api/upload/image
       ├─ Validate file (jpg/png/webp)
       ├─ Store file locally
       ├─ Create UploadSession (PROCESSING)
       └─ Return { sessionId, mediaFiles, status }
       ↓
POST /api/upload/{sessionId}/process
       ├─ Call Flask AI service
       ├─ Get violations + processed image URL
       ├─ Save to database
       └─ Update UploadSession to AI_PROCESSED
       ↓
Show success alert with sessionId
       ↓
Auto-navigate to History tab
       ↓
renderHistoryTable() loads data
       ├─ GET /api/upload/list
       └─ Display with status badges
       ↓
User clicks "Xem kết quả"
       ↓
viewTaskResult() triggers
       ├─ GET /api/upload/{sessionId}/result
       ├─ Display violations
       └─ Show processed image URL
```

### Folder Upload Flow (Authority)

```
Authority selects FOLDER type
       ↓
Authority drags folder or selects multiple files
       ↓
renderSelectedFiles() shows count
       ↓
Authority clicks "Submit to AI Service"
       ↓
submitUpload() triggers two-step process
       ↓
Step 1: POST /api/upload/folder
       ├─ Validate all files are images
       ├─ Store files in session directory
       ├─ Create UploadSession (PROCESSING)
       ├─ Create N MediaFiles
       └─ Return { sessionId, mediaFiles[] }
       ↓
Step 2: POST /api/upload/{sessionId}/process
       ├─ For each MediaFile:
       │  ├─ Call Flask AI service
       │  ├─ Get violations
       │  └─ Save DetectedViolations
       ├─ Update UploadSession to AI_PROCESSED
       └─ Return success
       ↓
Show success alert
       ↓
Auto-navigate to "Check result" tab
       ↓
Authority can review and correct violations
```

### Video Upload Flow (Authority)

```
Authority selects VIDEO type
       ↓
Authority selects .mp4/.mov file
       ↓
renderSelectedFiles() shows file name
       ↓
Authority clicks "Submit to AI Service"
       ↓
Step 1: POST /api/upload/video
       ├─ Validate video format
       ├─ Store video file locally
       ├─ Create UploadSession (PROCESSING)
       └─ Return { sessionId, mediaFiles, videoUrl }
       ↓
Step 2: POST /api/upload/{sessionId}/process
       ├─ Call Flask AI service
       ├─ Flask extracts frames (every 3 frames)
       ├─ Detects first violation
       ├─ Saves frame as processed_url
       ├─ Returns violations from that frame
       ├─ Save results to database
       └─ Update UploadSession to AI_PROCESSED
       ↓
Show success alert
       ↓
Auto-navigate to "Check result" tab
       ↓
Authority can review frame and violations
```

---

## 🔄 Status Management

### Upload Session States

```
PROCESSING
   ↓
   └─→ AI_PROCESSED (after POST /{id}/process)
       ↓
       ├─→ FEEDBACKING (if user appeals)
       │   ↓
       │   └─→ FINALIZED (after authority review)
       │
       └─→ FINALIZED (direct finalization)
```

### UI State Mapping

| Backend Status | User Dashboard | Authority Dashboard |
|---|---|---|
| PROCESSING | ⏳ Đang xếp hàng / AI đang phân tích | ⏳ Processing |
| AI_PROCESSED | ✅ Hoàn thành | ✅ Ready for review |
| FEEDBACKING | ⏳ Chờ phản hồi | 👁️ Reviewing appeal |
| FINALIZED | ✅ Hoàn tất | ✅ Completed |

---

## 📋 Form Validation

### Dashboard.html (User)

- Title field: Required, max 200 chars
- File selection: Required, one file for IMAGE/VIDEO
- File types: jpg, jpeg, png, webp (images), mp4, mov (video)
- Max file size: 500MB

### Authority.html

- Upload type: Required (SINGLE_IMAGE, FOLDER, VIDEO)
- Files: Required, at least one for FOLDER
- File validation by type
- Real-time file count display

---

## 🎨 UI Components

### Status Badges

```html
<!-- Processing -->
<span class="status-task task-processing">
  <i class="spinner"></i> AI đang phân tích
</span>

<!-- Done -->
<span class="status-task task-done">
  <i class="checkmark"></i> Hoàn thành
</span>

<!-- Queued -->
<span class="status-task task-queued">
  <i class="dot"></i> Đang xếp hàng
</span>

<!-- Failed -->
<span class="status-task task-failed">
  <i class="error"></i> Thất bại
</span>
```

### Drag-Drop Zone

- Border changes to primary color on hover
- Shows upload hint text
- Click to select file
- Drop files to upload

### Alert Messages

```javascript
showSuccess("✅ Upload & Processing successful!")
showError("❌ Upload failed. Please try again.")
```

---

## 🔐 Security Features

- File type validation on client + server
- File size validation (500MB limit)
- UUID-based file naming (no path traversal)
- Authentication required (Spring Security)
- CSRF protection enabled
- User-specific data access

---

## 📱 Responsive Design

- **Desktop (> 1024px)**: Full sidebar + content
- **Tablet (768-1024px)**: Collapsed sidebar (70px)
- **Mobile (< 768px)**: Hamburger menu (future enhancement)

---

## 🧪 Testing the UI

### Quick Test (User Dashboard)

1. Go to `http://localhost:8080/user`
2. Click "Create report" (already selected)
3. Choose IMAGE (default)
4. Click on upload zone or select file
5. Pick an image (jpg/png/webp)
6. Click "Gửi báo cáo lên hệ thống"
7. System uploads and processes
8. Auto-navigate to "See history"
9. Click "Xem kết quả" to view violations

### Quick Test (Authority Dashboard)

1. Go to `http://localhost:8080/authority`
2. Select upload type (SINGLE_IMAGE, FOLDER, or VIDEO)
3. Select file(s)
4. Click "Submit to AI Service"
5. System uploads and processes
6. Auto-navigate to "Check result"
7. Review violations and make corrections
8. Click "Save correction"

---

## 📝 Error Handling

| Error | User Message | Action |
|---|---|---|
| No file selected | "Vui lòng tải lên tệp tin" | Clear field, retry |
| Invalid file type | "Invalid file format" | Select correct type |
| File too large | Max 500MB | Use smaller file |
| Network error | "Network error: ..." | Retry upload |
| Session not found | 404 Not Found | Refresh page |
| AI processing failed | "AI service error" | Try again |

---

## 🚀 Next Steps for Deployment

1. ✅ Ensure Flask AI service is running on port 5000
2. ✅ Verify storage directories exist
3. ✅ Create test user accounts
4. ✅ Test single image upload
5. ✅ Test folder upload (authority)
6. ✅ Test video upload
7. ✅ Verify database records created
8. ✅ Test result viewing
9. ✅ Test appeal submission (future)
10. ✅ Load test with multiple users

---

## 📞 Support

### Common Issues

**Q: Upload button is disabled**
A: Select a file first, then button enables

**Q: Status shows "Đang xếp hàng" but not progressing**
A: Check if Flask AI service is running and accessible

**Q: "Network error" on submit**
A: Check:
   - Backend running on 8080
   - Flask running on 5000
   - Storage directories writable
   - Database connected

**Q: File uploaded but not appearing in history**
A: 
   - Refresh page
   - Check database for UploadSession records
   - Check server logs for errors

---

**Version:** 2.0.0 (UI Integration Complete)  
**Last Updated:** May 29, 2026  
**Status:** ✅ Ready for Testing
