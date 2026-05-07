# Implementation Caveats & Technical Decisions

This document provides answers to common "How" and "Why" questions regarding the implementation of Solari.

## Why Jetpack Compose?
Compose was chosen for its declarative nature, which significantly speeds up UI development and simplifies state management compared to the traditional View system. It allows for complex animations and custom layouts (like the "BeReal-style" feed) with much less code.

## How is Authentication handled?
We use a dual-token system (Access + Refresh tokens).
- **Access Tokens:** Stored in memory and sent via `AuthInterceptor`.
- **Refresh Tokens:** Encrypted using a custom `TokenCipher` (AES-GCM) before being stored in `EncryptedSharedPreferences`.
- **Token Refresh:** Handled automatically by an OkHttp `Authenticator`, ensuring seamless user sessions.

## Why Room for a social app?
Room is used to cache "recent" data (like the user's own profile and basic conversation metadata) to provide a fast initial launch experience. It also serves as a staging area for complex objects like `Post` before they are fully uploaded.

## How do real-time updates work?
We implement a custom WebSocket protocol over OkHttp. The `WebSocketManager` maintains a persistent connection. Incoming JSON payloads are parsed by `WebSocketEventParser` into typed `WebSocketEvent` objects, which are then dispatched to relevant repositories to update the UI state reactively.

## Why manual DI instead of Hilt?
For the current scale of the project, manual DI in `AppContainer` provides full transparency and avoids the compilation overhead of annotation processing. However, we acknowledge that as we scale, the benefits of Hilt (standardization, scoping) will eventually outweigh the current simplicity.

## How was the Feed UI implemented?
The feed uses a custom `LazyColumn` with complex item composables. The "dual-camera" preview and captured media utilize the CameraX library, with custom transformation logic to overlay front and back camera frames into a single cohesive post.
