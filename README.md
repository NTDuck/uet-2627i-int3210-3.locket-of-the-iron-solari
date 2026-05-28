# Solari Android

Solari is a native Android social camera app built with Kotlin and Jetpack Compose. It lets users capture moments, edit media, publish posts, browse friends' activity, and chat with friends in real time.

## Main Features

- Email/password and Google sign-in with session refresh.
- Camera capture and upload from gallery for photos and videos.
- Feed browsing with friend filters, post navigation, reactions, comments, and media playback.
- Friend management, friend requests, nicknames, blocked accounts, and public profile previews.
- Profile settings for avatar, display name, password, theme, notifications, and home-screen widget.
- Push notification and widget routing into correct app screens.

## Special Features

- Chat: 1-to-1 conversations with typing indicators, reply previews, emoji reactions, unsend support, read/delivery footer text, infinite scrolling, post previews, mute, and partner profile preview.
- Image editing: crop/rotate/flip, drawing, eraser, color/brush controls, text overlays, and undo editing before publishing.
- Capturing and post composing: photo/video capture, gallery import, caption styles, and location/weather/rating captions

## Tech Stack

- Kotlin, Jetpack Compose, Navigation Compose
- Retrofit, OkHttp, kotlinx.serialization
- Room, DataStore, manual dependency injection through `AppContainer`
- Firebase Messaging, Google Credential Manager, Coil, Media3
- Gradle Android application module: `app`

## Configuration

Create `local.properties` with Android SDK and runtime configuration:

```properties
sdk.dir=/path/to/android/sdk
SOLARI_BACKEND_URL=https://your-api.example.com/
SOLARI_GOOGLE_SERVER_CLIENT_ID=your-web-oauth-client-id.apps.googleusercontent.com
```

`SOLARI_BACKEND_URL` can also be supplied as a Gradle property or environment variable. The Google client ID must be the Web OAuth client ID used by the backend for Google token audience validation.

## Build and Run

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

Install and launch on a connected device:

```bash
./gradlew :app:installDebug
adb shell monkey -p com.solari.app -c android.intent.category.LAUNCHER 1
```

Run a Kotlin compile check:

```bash
./gradlew :app:compileDebugKotlin
```

Build an unsigned release APK:

```bash
./gradlew :app:assembleRelease
```

The unsigned APK is written to:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

Sign the release APK with your private Android keystore before sharing or installing it.
