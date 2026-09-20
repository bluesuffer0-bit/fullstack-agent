# fullstack-agent for Android: the launcher app

The desktop version puts four shortcuts on your Desktop — **Chat**, **Talk**, **barehands**, **Update** — named after your agent. Android has no desktop, so the same four actions live in this app: one screen, four buttons, each handing a command to Termux.

This is a companion to the [Termux side of the port](../fullstack-agent-android.md), not a replacement for it. The agent itself still runs in Termux; the app is a remote control.

## What the buttons do

| Button | Runs in Termux | Needs |
|---|---|---|
| **Talk to \<name\>** | `start.android.sh voice` — the voice and the face | voice + face installed |
| **\<name\> barehands** | `start.android.sh hands` — the voice and the board | voice + hands installed |
| **Everything** | `start.android.sh` — every piece installed | any |
| **Open the face** | opens `http://127.0.0.1:8790/faces/<face>/` in the browser | face installed |
| **Chat with \<name\>** | your agent's typed-session command (`claude` by default) | nothing |
| **Update \<name\>** | `update.android.sh` — pulls every piece, shows what changed | nothing |

Buttons whose pieces are not installed are hidden, not greyed. The voice is off by default because it is the one piece that may not build on a phone; when setup has watched it speak, switch it on in the settings screen and the Talk and barehands buttons appear.

## Requirements

- **Termux**, installed from [F-Droid](https://f-droid.org/packages/com.termux/) or the [GitHub releases](https://github.com/termux/termux-app/releases/latest). The Google Play copy is abandoned and its packages are broken. The app detects Termux's absence and offers both install links.
- The agent, set up in Termux by the wizard (`fullstack-agent-android.md`).

That is all. The app needs no Internet permission — it starts localhost servers inside Termux and hands URLs to the browser, which does the network work itself.

## Building

Android Studio is not required. With an Android SDK and JDK 17:

```
cd android
gradle assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`. Install it over USB (`adb install app-debug.apk`) or copy it to the phone and tap it (you will be asked to allow installs from that source once).

The project pins the versions the offline PocketDev toolchain carries — AGP 8.11.0, Kotlin 1.9.22, compileSdk 36, Java 17, AndroidX enabled — so it builds with no network when that cache is present. It also declares the standard Google and Maven Central repositories, so it builds on an ordinary machine too.

## Installing it without a build

If you would rather not build, the Termux:Widget shortcuts from `mkshortcuts.android.sh` cover exactly the same ground from the home screen. The app is the optional, prettier version of those four scripts.

## How it talks to Termux

Each button sends an intent to Termux's `RunCommandService` — `com.termux.RUN_COMMAND` with a path, an argument array, and a working directory. That is the same public entry point Termux:Widget uses, which is the reason it is safe to rely on: Termux:Widget is a separate app doing exactly this and nothing else.

Because the arguments travel as an array, no shell quoting is involved, and a home folder with a space in it cannot break a button.

The app cannot read Termux's private folder (`/data/data/com.termux/files/home` is invisible to every other app, by design), so it keeps its own copy of the four configuration values — agent name, home folder, chat command, face — in its settings screen. Those values describe what the buttons point at; each piece's own config file still describes what the piece does. Neither overwrites the other.

## Troubleshooting

**A button says Termux could not start.** Either Termux is not installed (the dialog offers both install sources) or the phone refused a cross-app service start. The toast prints the exact command; run it inside Termux by hand and it works.

**The face opens to a picker instead of the animation.** `http://127.0.0.1:8790/` is the gallery, not a face. The button opens `http://127.0.0.1:8790/faces/<face>/` using the face from the settings screen; check that it matches a face in `ai-visualizer/faces/`.

**Nothing happens after tapping Talk.** The voice is probably not installed or did not build. Open the launcher's settings and switch the voice off — the face, the hands, and the memory are complete without it — or ask your agent to set the voice up.

**The buttons vanished.** They hide when a piece is marked not installed. Launcher settings → Installed pieces.

## License

Same as the rest of the stack: AGPL-3.0-or-later. See the [LICENSE](../LICENSE) in the repo root.
