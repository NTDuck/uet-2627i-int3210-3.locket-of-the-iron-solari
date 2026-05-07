# Current Problems & Areas for Improvement

This document identifies architectural, technical, and code quality issues discovered during the codebase audit.

## 1. Architectural Issues
- **God Files:** `MainActivity.kt`, `FeedScreen.kt`, and `ChatScreen.kt` have grown excessively large. Logic for navigation, complex UI states, and side effects are tightly coupled within these files.
- **Manual Dependency Injection:** While functional, the current `AppContainer` approach is becoming difficult to manage as the number of repositories and viewmodels increases. Migrating to Hilt or Koin would reduce boilerplate.
- **Lack of Offline Support:** While Room is used for some caching, the app primarily relies on an active network connection for most screens (Feed, Chat). A robust "Offline First" strategy is missing.

## 2. Technical Debt & Potential Bugs
- **WebSocket Thread Safety:** The `WebSocketManager` handles concurrent events, but there are potential race conditions in how listeners are registered and how state is updated during rapid connection/disconnection cycles.
- **Media Upload Reliability:** If a post upload is interrupted (e.g., app kill), the current `PostUploadCoordinator` does not persist the "in-flight" state, leading to lost posts.
- **UI State Management:** Some screens use multiple independent state variables instead of a single `UiState` data class, leading to inconsistent UI states during loading or error conditions.
- **Confirmation Dialog Bug (Fixed):** A systemic pattern was found where `SolariConfirmationDialog` was used without properly resetting its trigger state, causing dialogs to "get stuck." This has been surgically corrected across the codebase.

## 3. Code Quality
- **Inconsistent Error Handling:** Error handling varies between repositories; some return `ApiResult`, others throw exceptions, and some use `AuthSessionInvalidationNotifier` implicitly.
- **Resource Management:** Hardcoded strings and dimensions are present in several Compose components, making localization and multi-screen support difficult.
- **Test Coverage:** Unit tests are sparse, particularly for complex logic in ViewModels and the WebSocket event parser.
