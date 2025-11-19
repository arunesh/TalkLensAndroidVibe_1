# TalkLens - Android Document Translation App

## Design Document v1.0

---

## 1. Overview

**TalkLens** is an Android application that provides on-device document translation between foreign languages and English. The app leverages on-device ML models (Google MLKit and other available options) to enable fully offline translation capabilities after initial setup.

### 1.1 Key Features

- **Offline-First Translation**: Fully functional without internet connection after initial model download
- **Multi-Page Document Scanning**: Capture and translate multiple pages via camera
- **File Import Support**: Translate documents from device storage (images, PDFs)
- **Bidirectional Translation**: Foreign Language ↔ English
- **Modern Tab-Based UI**: Intuitive bottom navigation with three main tabs
- **Initial Setup Flow**: Guided model download with progress tracking

---

## 2. Architecture

### 2.1 Architecture Pattern

**MVVM (Model-View-ViewModel)** with Clean Architecture principles

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Compose    │  │  ViewModels  │  │  Navigation  │  │
│  │      UI      │  │              │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Use Cases   │  │ Repositories │  │   Entities   │  │
│  │              │  │ (Interfaces) │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  ML Models   │  │ Local Storage│  │   Camera/    │  │
│  │   (MLKit)    │  │  (Room DB)   │  │   Files      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture Components**: ViewModel, LiveData/StateFlow, Navigation
- **Dependency Injection**: Hilt (Dagger)
- **Camera**: CameraX
- **ML/Vision**:
  - Google ML Kit (Text Recognition, Translation)
  - TensorFlow Lite (backup option)
- **Local Storage**:
  - Room Database (translation history, settings)
  - SharedPreferences (user preferences)
  - File System (cached images, models)
- **Image Processing**:
  - Coil (image loading)
  - Android Bitmap APIs
- **Coroutines**: Kotlin Coroutines + Flow
- **Testing**: JUnit, Mockito, Espresso

---

## 3. UI/UX Design

### 3.1 Navigation Structure

```
App Entry
    │
    ├─> [First Launch] Setup Screen
    │        │
    │        └─> Model Download Progress
    │                  │
    │                  └─> Main App
    │
    └─> [Subsequent Launches] Main App
              │
              └─> Bottom Navigation (3 Tabs)
                      ├─> Camera Tab
                      ├─> Gallery Tab
                      └─> Settings Tab
```

### 3.2 Screen Definitions

#### 3.2.1 Setup Screen (First Launch Only)

**Purpose**: Download required ML models for offline functionality

**Components**:
- App branding/logo
- Welcome message
- List of models to download:
  - Text Recognition Model (OCR)
  - Translation Model(s) based on selected language
- Progress indicators (overall + per-model)
- Storage space indicator
- Action buttons:
  - "Download Models" (primary)
  - "Skip" (download later - reduced functionality)

**Flow**:
1. Display required storage space
2. User confirms download
3. Show progress bar for each model
4. Save completion status locally
5. Navigate to main app

**Implementation Notes**:
- Check if models already exist
- Handle download failures with retry logic
- Allow background downloads
- Persist download state

---

#### 3.2.2 Tab 1: Camera Scanner

**Purpose**: Capture and translate multiple document pages in real-time

**Components**:

**Camera View**:
- Full-screen camera preview
- Semi-transparent overlay showing document boundaries (auto-detect)
- Flash toggle
- Capture button (large, centered bottom)
- Page counter badge (e.g., "Page 3/5")
- Gallery icon (view captured pages)

**Captured Pages View**:
- Horizontal scrollable thumbnail strip
- Each thumbnail shows:
  - Page number
  - Delete button
  - Checkmark if processed
- "Translate All" button
- "Add More" button (return to camera)

**Translation View**:
- Split-pane view (scrollable):
  - Top: Original image with detected text highlighted
  - Bottom: Translated text (editable)
- Translation direction toggle (Foreign → English / English → Foreign)
- Language pair indicator
- Actions:
  - Copy text
  - Share
  - Save to history
  - Export as PDF

**Features**:
- Auto-capture when document boundaries detected
- Manual capture override
- Multi-page document support (up to 20 pages)
- Real-time text detection overlay
- Batch translation processing

---

#### 3.2.3 Tab 2: Gallery/File Import

**Purpose**: Translate documents from existing images/files

**Components**:

**File Selection View**:
- Recent images grid (from device)
- "Browse Files" button (opens system picker)
- Supported formats indicator (JPEG, PNG, PDF)
- Quick filters:
  - Recent
  - Documents folder
  - Downloads
  - All files

**Processing View**:
- Selected file preview
- File info (name, size, pages for PDF)
- Language selection dropdown
- Translation direction toggle
- "Translate" button

**Results View** (same as Camera tab translation view):
- Original + Translated text
- Actions (Copy, Share, Save, Export)

**Features**:
- PDF page-by-page processing
- Multi-image selection support
- Image quality indicator
- Pre-processing options (rotate, crop)

---

#### 3.2.4 Tab 3: Settings

**Purpose**: Configure app preferences and manage models

**Components**:

**Language Settings**:
- Default Source Language (Foreign Language)
  - Dropdown/Picker with available languages
  - Shows download status per language
- Default Target Language (English by default)

**Model Management**:
- Installed Models section
  - List of downloaded models
  - Size information
  - Delete option per model
- Available Models section
  - Browse and download additional language models
  - Storage requirement display

**Translation Settings**:
- Translation quality preference (Fast/Balanced/Accurate)
- Auto-translate toggle
- Preserve formatting toggle

**Camera Settings**:
- Auto-capture toggle
- Flash default (On/Off/Auto)
- Image quality (Low/Medium/High)

**Storage & History**:
- Translation history toggle
- Clear history button
- Cache size display
- Clear cache button

**General**:
- App theme (Light/Dark/System)
- About section (version, licenses)
- Privacy policy
- Send feedback

---

## 4. Modular Architecture

### 4.1 Module Breakdown

```
app/
├── core/
│   ├── common/          # Shared utilities, extensions
│   ├── designsystem/    # Compose UI components, theme
│   ├── navigation/      # Navigation logic
│   └── model/           # Shared data models
│
├── data/
│   ├── repository/      # Repository implementations
│   ├── local/           # Room DB, SharedPrefs
│   ├── mlkit/           # ML Kit integration
│   └── model/           # Data models, DTOs
│
├── domain/
│   ├── usecase/         # Business logic use cases
│   ├── repository/      # Repository interfaces
│   └── model/           # Domain entities
│
└── feature/
    ├── setup/           # Model download setup
    ├── camera/          # Camera scanning feature
    ├── gallery/         # File import feature
    ├── settings/        # Settings management
    ├── translation/     # Shared translation UI
    └── history/         # Translation history
```

### 4.2 Core Modules

#### 4.2.1 Core Module

**Purpose**: Shared utilities and base classes

**Contents**:
- `common/`: Extensions, utilities, constants
- `designsystem/`:
  - Compose theme
  - Reusable UI components (buttons, cards, dialogs)
  - Typography, colors, spacing
- `navigation/`: Navigation graph, routes, deep links
- `model/`: Shared sealed classes, enums

---

#### 4.2.2 Data Module

**Purpose**: Data management and external integrations

**Key Components**:

**MLKit Integration** (`data/mlkit/`):
```kotlin
interface TextRecognizer {
    suspend fun recognizeText(image: Bitmap): RecognizedText
}

interface TextTranslator {
    suspend fun translate(
        text: String,
        sourceLang: Language,
        targetLang: Language
    ): TranslatedText

    suspend fun downloadModel(language: Language): Flow<DownloadProgress>
    suspend fun isModelDownloaded(language: Language): Boolean
    suspend fun deleteModel(language: Language)
}
```

**Repository Implementations** (`data/repository/`):
- `TranslationRepositoryImpl`: Manages translation operations
- `ModelRepositoryImpl`: Handles model downloads/management
- `DocumentRepositoryImpl`: Document storage and retrieval
- `SettingsRepositoryImpl`: User preferences

**Local Storage** (`data/local/`):
- Room entities and DAOs for:
  - Translation history
  - Cached documents
  - User settings
- SharedPreferences wrapper

---

#### 4.2.3 Domain Module

**Purpose**: Business logic and abstractions

**Use Cases** (`domain/usecase/`):
- `TranslateDocumentUseCase`
- `ScanMultiPageDocumentUseCase`
- `DownloadLanguageModelUseCase`
- `GetTranslationHistoryUseCase`
- `ImportFileUseCase`
- `ExportTranslationUseCase`

**Repository Interfaces** (`domain/repository/`):
- Define contracts for data layer
- No implementation details

---

#### 4.2.4 Feature Modules

**Setup Feature** (`feature/setup/`):
- `SetupViewModel`: Manages model download state
- `SetupScreen`: UI for initial setup
- `ModelDownloadManager`: Coordinates downloads

**Camera Feature** (`feature/camera/`):
- `CameraViewModel`: Camera and capture logic
- `CameraScreen`: Camera UI
- `DocumentBoundaryDetector`: Auto-detect document edges
- `PageManager`: Multi-page handling

**Gallery Feature** (`feature/gallery/`):
- `GalleryViewModel`: File selection and import
- `GalleryScreen`: File picker UI
- `FileProcessor`: Handle different file formats

**Settings Feature** (`feature/settings/`):
- `SettingsViewModel`: Settings management
- `SettingsScreen`: Settings UI
- `ModelManagementScreen`: Model download/delete UI

**Translation Feature** (`feature/translation/`):
- `TranslationViewModel`: Shared translation logic
- `TranslationResultScreen`: Display translated content
- `TextFormatter`: Preserve document formatting

**History Feature** (`feature/history/`):
- `HistoryViewModel`: Access past translations
- `HistoryScreen`: Display history
- Can be accessed from multiple tabs

---

## 5. ML Model Strategy

### 5.1 Google ML Kit Integration

**Primary Models**:
1. **Text Recognition V2** (On-device)
   - Supports Latin, Chinese, Devanagari, Japanese, Korean scripts
   - Real-time text detection
   - ~10MB per script model

2. **Translation API** (On-device)
   - 59+ language pairs
   - ~30-50MB per language model
   - Bidirectional translation

### 5.2 Model Download Strategy

**Phase 1 - Initial Setup**:
- Download English + one user-selected language
- Text recognition model for relevant scripts
- Total: ~50-80MB

**On-Demand Downloads**:
- Additional languages downloaded from settings
- Background download support
- Resume capability

**Model Storage**:
- App-specific cache directory
- Automatic cleanup of unused models (configurable)
- Version checking and updates

### 5.3 Fallback Strategy

If ML Kit unavailable:
1. TensorFlow Lite models (custom integration)
2. Tesseract for OCR
3. Online API fallback (with user permission)

---

## 6. Data Models

### 6.1 Core Entities

```kotlin
data class Document(
    val id: String,
    val pages: List<Page>,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val createdAt: Long,
    val updatedAt: Long
)

data class Page(
    val id: String,
    val imageUri: String,
    val recognizedText: RecognizedText?,
    val translatedText: TranslatedText?,
    val pageNumber: Int
)

data class RecognizedText(
    val text: String,
    val blocks: List<TextBlock>,
    val confidence: Float
)

data class TextBlock(
    val text: String,
    val boundingBox: Rect,
    val confidence: Float
)

data class TranslatedText(
    val text: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val timestamp: Long
)

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Spanish"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    CHINESE("zh", "Chinese"),
    JAPANESE("ja", "Japanese"),
    // ... more languages
}

data class ModelInfo(
    val language: Language,
    val type: ModelType,
    val isDownloaded: Boolean,
    val size: Long,
    val version: String
)

enum class ModelType {
    TEXT_RECOGNITION,
    TRANSLATION
}
```

---

## 7. Phased Implementation Plan

### Phase 0: Project Setup (Week 1)

**Objectives**: Establish project foundation

**Tasks**:
- [ ] Initialize Android project with Kotlin
- [ ] Setup Gradle build configuration
- [ ] Configure Hilt for dependency injection
- [ ] Setup Jetpack Compose
- [ ] Create module structure (core, data, domain, feature)
- [ ] Configure version catalog for dependencies
- [ ] Setup Room database schema
- [ ] Implement basic navigation structure
- [ ] Create design system (theme, colors, typography)
- [ ] Setup Git workflow and branching strategy

**Deliverables**:
- Compilable project with all modules
- Basic app shell with bottom navigation
- Design system components

---

### Phase 1: ML Kit Integration & Setup Screen (Week 2-3)

**Objectives**: Integrate ML models and create setup flow

**Tasks**:
- [ ] Integrate Google ML Kit SDK
- [ ] Implement `TextRecognizer` interface
- [ ] Implement `TextTranslator` interface
- [ ] Create model download manager
- [ ] Build Setup screen UI (Compose)
- [ ] Implement setup ViewModel with download state
- [ ] Add progress tracking for downloads
- [ ] Implement model verification logic
- [ ] Add error handling and retry mechanism
- [ ] Store setup completion flag
- [ ] Create unit tests for ML integration

**Deliverables**:
- Working model download functionality
- Complete setup screen with progress tracking
- Models stored and ready for offline use

---

### Phase 2: Camera Feature - Basic (Week 4-5)

**Objectives**: Implement camera capture and OCR

**Tasks**:
- [ ] Setup CameraX integration
- [ ] Create camera preview UI
- [ ] Implement single page capture
- [ ] Integrate text recognition on captured image
- [ ] Create basic image preprocessing
- [ ] Display recognized text
- [ ] Add camera permissions handling
- [ ] Implement flash toggle
- [ ] Add captured image preview
- [ ] Create basic document boundary detection
- [ ] Add unit and instrumentation tests

**Deliverables**:
- Functional camera that captures images
- Text recognition working on single page
- Basic UI showing captured and recognized text

---

### Phase 3: Translation Core (Week 6)

**Objectives**: Implement translation functionality

**Tasks**:
- [ ] Implement translation use case
- [ ] Create translation repository
- [ ] Build translation UI components
- [ ] Add language selection logic
- [ ] Implement bidirectional translation
- [ ] Create translation result screen
- [ ] Add copy/share functionality
- [ ] Implement text formatting preservation
- [ ] Add loading states and error handling
- [ ] Create translation quality indicators
- [ ] Add unit tests for translation logic

**Deliverables**:
- Working translation from recognized text
- UI showing original and translated text
- Share/copy functionality

---

### Phase 4: Camera Feature - Multi-Page (Week 7)

**Objectives**: Extend camera to handle multiple pages

**Tasks**:
- [ ] Implement page management system
- [ ] Create thumbnail strip UI
- [ ] Add page counter and navigation
- [ ] Implement "add more pages" flow
- [ ] Build batch translation processing
- [ ] Add page deletion functionality
- [ ] Create page reordering capability
- [ ] Implement auto-capture with document detection
- [ ] Add multi-page translation queue
- [ ] Create progress indicator for batch processing
- [ ] Add tests for multi-page handling

**Deliverables**:
- Capture multiple pages in sequence
- View all pages as thumbnails
- Batch translate all pages
- Manage page order and deletions

---

### Phase 5: Gallery/File Import Feature (Week 8)

**Objectives**: Import and translate existing files

**Tasks**:
- [ ] Implement file picker integration
- [ ] Create gallery UI
- [ ] Add image selection from device
- [ ] Implement PDF parsing (page-by-page)
- [ ] Add file preview functionality
- [ ] Integrate with existing translation flow
- [ ] Add image preprocessing (rotate, crop)
- [ ] Implement multi-image selection
- [ ] Add file format validation
- [ ] Create file import use cases
- [ ] Add tests for file processing

**Deliverables**:
- Browse and select images/PDFs
- Import files for translation
- Process multi-page PDFs
- Reuse translation UI from camera feature

---

### Phase 6: Settings & Model Management (Week 9)

**Objectives**: Complete settings and model management

**Tasks**:
- [ ] Build settings screen UI
- [ ] Implement language preferences
- [ ] Create model management UI
- [ ] Add download additional models functionality
- [ ] Implement model deletion
- [ ] Add camera settings
- [ ] Create translation preferences
- [ ] Implement theme selection
- [ ] Add storage management (clear cache/history)
- [ ] Create settings repository
- [ ] Add tests for settings logic

**Deliverables**:
- Complete settings screen
- Manage downloaded models
- Configure app preferences
- Theme switching

---

### Phase 7: Translation History (Week 10)

**Objectives**: Store and retrieve translation history

**Tasks**:
- [ ] Implement history database schema
- [ ] Create history repository
- [ ] Build history screen UI
- [ ] Add history list with search
- [ ] Implement history item detail view
- [ ] Add delete history functionality
- [ ] Create history export feature
- [ ] Add history filtering (by language, date)
- [ ] Implement history cleanup (old entries)
- [ ] Add tests for history management

**Deliverables**:
- View past translations
- Search and filter history
- Re-use or share past translations
- Manage history storage

---

### Phase 8: Export & Sharing (Week 11)

**Objectives**: Export translations in various formats

**Tasks**:
- [ ] Implement PDF export functionality
- [ ] Create formatted text export
- [ ] Add image + text export
- [ ] Implement share sheet integration
- [ ] Create export format options
- [ ] Add email/messaging integration
- [ ] Implement cloud storage sharing (Drive, etc.)
- [ ] Create export preview
- [ ] Add export history
- [ ] Add tests for export functionality

**Deliverables**:
- Export as PDF with original + translation
- Share via system share sheet
- Multiple export format options

---

### Phase 9: Polish & Optimization (Week 12)

**Objectives**: Performance optimization and UI polish

**Tasks**:
- [ ] Optimize image processing pipeline
- [ ] Improve translation speed
- [ ] Add animations and transitions
- [ ] Implement loading skeletons
- [ ] Optimize memory usage
- [ ] Add comprehensive error messages
- [ ] Implement offline detection
- [ ] Create onboarding tutorial
- [ ] Add accessibility features (TalkBack)
- [ ] Optimize battery usage
- [ ] Add analytics (privacy-friendly)
- [ ] Performance profiling

**Deliverables**:
- Smooth, polished UI
- Fast translation processing
- Optimized resource usage
- Better user feedback

---

### Phase 10: Testing & Release (Week 13-14)

**Objectives**: Comprehensive testing and release preparation

**Tasks**:
- [ ] Comprehensive unit test coverage (>80%)
- [ ] Integration tests for critical flows
- [ ] UI/Espresso tests for all screens
- [ ] Manual testing on multiple devices
- [ ] Test different Android versions (API 24+)
- [ ] Test with various document types
- [ ] Accessibility testing
- [ ] Performance testing
- [ ] Create release build configuration
- [ ] Generate signed APK/AAB
- [ ] Prepare Play Store assets
- [ ] Create privacy policy
- [ ] Beta testing with small group
- [ ] Bug fixing based on feedback

**Deliverables**:
- Fully tested application
- Release-ready build
- Play Store listing ready
- Beta version deployed

---

## 8. Technical Considerations

### 8.1 Performance

**Image Processing**:
- Compress images before ML processing (max 2048x2048)
- Use WorkManager for background processing
- Implement image caching strategy

**Translation**:
- Process pages in parallel (Kotlin Coroutines)
- Cache common translations
- Optimize model loading (keep in memory when active)

**UI**:
- Use LazyColumn for lists
- Implement pagination for history
- Use remember/derivedStateOf for expensive computations

### 8.2 Storage Management

**Model Storage**:
- App cache directory for models
- Automatic cleanup of old model versions
- User-configurable storage limits

**Document Storage**:
- Temporary storage for processed images
- Configurable retention period
- Manual cleanup option

### 8.3 Privacy & Security

**Data Privacy**:
- All processing on-device (no cloud upload)
- Optional telemetry with opt-in
- No personal data collection
- Clear privacy policy

**Permissions**:
- Camera permission (required)
- Storage permission (required for import)
- Minimal permission scope

### 8.4 Accessibility

- TalkBack support for all screens
- High contrast mode
- Large text support
- Keyboard navigation
- Content descriptions for all UI elements

### 8.5 Localization

- Support for UI localization
- RTL language support
- Localized error messages
- Region-specific language variants

### 8.6 Error Handling

**Common Scenarios**:
- Model download failure → Retry with exponential backoff
- Text recognition failure → Show clear error, suggest better lighting
- Translation failure → Fallback to showing original text
- Storage full → Prompt cleanup options
- Permission denied → Show rationale and settings link

### 8.7 Testing Strategy

**Unit Tests**:
- ViewModels (business logic)
- Use cases
- Repositories
- Utilities

**Integration Tests**:
- Repository + Local DB
- ML Kit integration
- File processing

**UI Tests**:
- Critical user flows
- Navigation
- Form validation

---

## 9. Dependencies

### 9.1 Core Dependencies

```gradle
// Kotlin
kotlin = "1.9.20"
kotlinx-coroutines = "1.7.3"

// Android
android-gradle-plugin = "8.1.2"
androidx-core-ktx = "1.12.0"
androidx-lifecycle = "2.6.2"
androidx-activity-compose = "1.8.0"

// Compose
compose-bom = "2023.10.01"
compose-compiler = "1.5.4"

// Navigation
androidx-navigation = "2.7.5"

// Dependency Injection
hilt = "2.48"
hilt-navigation-compose = "1.1.0"

// CameraX
camerax = "1.3.0"

// ML Kit
mlkit-text-recognition = "16.0.0"
mlkit-translation = "17.0.1"
mlkit-language-id = "17.0.5"

// Database
room = "2.6.0"

// Image Loading
coil = "2.5.0"

// Networking (for model downloads)
okhttp = "4.12.0"

// Testing
junit = "4.13.2"
androidx-test = "1.5.0"
espresso = "3.5.1"
mockito = "5.6.0"
turbine = "1.0.0" // Flow testing
```

---

## 10. Future Enhancements

### 10.1 Phase 11+ (Post-MVP)

**Advanced Features**:
- Real-time camera translation overlay
- Handwriting recognition
- Table/form recognition and preservation
- Multi-language document support (detect language per page)
- Document editing capabilities
- Cloud sync (optional, with encryption)
- Collaboration features (share projects)
- Custom dictionary/glossary
- Translation memory (learn from corrections)

**Additional Languages**:
- Expand to 100+ languages
- Dialect support
- Regional variations

**Platform Expansion**:
- Tablet-optimized UI
- Wear OS companion app
- Cross-device sync

**Integrations**:
- Google Drive integration
- Dropbox integration
- Microsoft Office integration
- WhatsApp/Telegram direct sharing

---

## 11. Success Metrics

### 11.1 Technical Metrics

- App startup time: < 2 seconds
- Translation time (per page): < 5 seconds
- OCR accuracy: > 90%
- Translation accuracy: > 85%
- Crash-free rate: > 99.5%
- App size: < 50MB (excluding models)

### 11.2 User Metrics

- Setup completion rate: > 90%
- Daily active users retention: > 40% (30 days)
- Average translations per user: > 5/week
- User rating: > 4.5 stars

---

## 12. Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| ML Kit model quality insufficient | High | Integrate TensorFlow Lite as backup |
| Large app size due to models | Medium | On-demand model downloads, only required languages |
| Poor OCR on low-quality images | High | Implement preprocessing, provide user guidance |
| Device storage limitations | Medium | Automatic cleanup, user notifications |
| Performance on low-end devices | Medium | Optimize processing, offer quality settings |
| Complex PDF parsing | Medium | Start with images, add PDF gradually |

---

## 13. Appendix

### 13.1 Supported Languages (Initial Release)

**Priority Tier 1** (Most common):
- Spanish
- French
- German
- Chinese (Simplified)
- Japanese
- Korean
- Arabic
- Hindi
- Portuguese
- Russian

**Priority Tier 2** (Add in Phase 11):
- Italian, Dutch, Polish, Turkish, Vietnamese, Thai, Indonesian, etc.

### 13.2 Supported File Formats

**Images**:
- JPEG
- PNG
- WebP

**Documents** (Phase 5+):
- PDF (multi-page support)

### 13.3 Minimum Device Requirements

- Android 8.0 (API 26) or higher
- 2GB RAM minimum (4GB recommended)
- 500MB free storage (for models)
- Camera (for scanning feature)

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-19 | Initial | Initial design document |

---

**End of Design Document**
