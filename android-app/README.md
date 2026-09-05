# Fullstack Agent for Android

A native Android port of the desktop stack. It combines the original project's four ideas in one private mobile application:

- **Mind:** Room-backed persistent memories and conversation history.
- **Mouth:** Android speech recognition and offline-capable text-to-speech.
- **Face:** a reactive Compose circuit-board visualizer.
- **Hands:** on-device CameraX + ML Kit pose gestures.
- **Provider-agnostic chat:** Anthropic and OpenAI-compatible APIs (OpenAI, Ollama gateways, OpenRouter, Groq, etc.).

## Build

Requirements: Android Studio Ladybug or newer (JDK 17 and Android SDK 35).

1. Open the `android-app` directory in Android Studio.
2. Let Gradle sync, then run the `app` configuration on Android 8.0+.
3. Open **Settings** in the app and choose a provider, endpoint, model, and API key.

Command line, with SDK and JDK configured:

```bash
cd android-app
gradle assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Provider examples

| Provider | Type | Endpoint | Example model |
|---|---|---|---|
| Anthropic | Anthropic | `https://api.anthropic.com/v1` | `claude-sonnet-4-20250514` |
| OpenAI | OpenAI-compatible | `https://api.openai.com/v1` | `gpt-4o-mini` |
| OpenRouter | OpenAI-compatible | `https://openrouter.ai/api/v1` | Provider model ID |

Only HTTPS endpoints are accepted by Android's network security policy. API keys are encrypted using Android Keystore-backed encrypted preferences. Chat and memory remain in the app's private Room database. Android Auto Backup is disabled.

## Platform notes

This is a real Android implementation rather than a Termux wrapper. Android cannot run Claude Code or unrestricted shell tools safely, so desktop process execution is intentionally replaced by mobile-native capabilities. Speech recognition availability depends on the device's installed recognition service. Pose processing runs on-device; camera frames are not uploaded by this app.

## Current gestures

- Raise left hand
- Raise right hand
- Raise both hands

Gestures are surfaced live in the Hands screen and can be mapped to additional app actions in `CameraGestureView`.
