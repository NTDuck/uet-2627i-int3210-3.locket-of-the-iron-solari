# Architecture Overview: Solari Android App

Solari is built using modern Android development practices, emphasizing a clean, modular, and reactive architecture.

## Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (100% declarative UI)
- **Asynchronous Programming:** Kotlin Coroutines & Flow
- **Dependency Injection:** Manual Dependency Injection (AppContainer pattern)
- **Networking:** Retrofit with OkHttp
- **Local Persistence:** Room Database
- **Image Loading:** Coil
- **Real-time Communication:** WebSockets (OkHttp implementation)
- **Security:** EncryptedSharedPreferences, Custom Token Cipher (AES), Credential Manager (Google ID Token)

## Architectural Patterns
The app follows a **Unidirectional Data Flow (UDF)** pattern within an **MVVM (Model-View-ViewModel)** structure.

### Layers
1.  **UI Layer (Screens & ViewModels):**
    - Compose Screens observe state from ViewModels.
    - ViewModels expose state via `mutableStateOf` or `Flow` and handle user interactions.
2.  **Domain/Repository Layer:**
    - Repositories act as the single source of truth.
    - They coordinate data between remote (API) and local (Room/Preferences) sources.
3.  **Data Layer (Remote & Local):**
    - **Remote:** Retrofit services for REST APIs and WebSocket manager for real-time events.
    - **Local:** Room DAOs for structured data and Preference Stores for key-value settings.

## Key Components
- **AuthRepository:** Manages user sessions, token refreshing, and Google Sign-In.
- **ConversationRepository:** Handles chat history, friend requests, and messaging.
- **FeedRepository:** Manages social posts, media uploads, and feed browsing.
- **WebSocketManager:** Manages the persistent socket connection for real-time updates.
- **PostUploadCoordinator:** Handles the complex lifecycle of capturing and uploading media with metadata.
