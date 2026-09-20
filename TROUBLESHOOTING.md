# Troubleshooting

This file covers only the problems that live BETWEEN the pieces. Each piece owns its own deeper guide: `ai-memory-vault/TROUBLESHOOTING.md`, `backtalk/TROUBLESHOOTING.md`, `barehands/TROUBLESHOOTING.md`, `ai-visualizer/TROUBLESHOOTING.md`.

## I closed the window in the middle of setup

Nothing is lost. Open a new terminal (PowerShell on Windows), go back to the toolbox folder (`cd ~/my-agent/fullstack-agent`, or on Windows `cd $HOME\my-agent\fullstack-agent`), and run:

```
claude --continue
```

That reopens your most recent session with its memory intact; tell it "we got cut off, keep going with the setup." If it can't find a session to continue, run `claude "set me up"` instead: the installer starts over, finds everything already downloaded, and skips ahead instead of redoing it.

## The install command opened Claude Code, but it acts like nothing's there

Then the download step failed before Claude Code started, and the error is in your terminal scrollback, right above where Claude opened. Type `/exit`, scroll up, and read it. On a Mac, a "developer tools" dialog may be waiting for an Open/Install click (that installs git; click Install and paste the command again). On Windows the command downloads a zip and needs no git, so a failure there is usually network. Fix what the message says, then paste the install command again.

## Windows says "claude is not recognized," or the Claude Code install "isn't doing anything"

Both are the same story. The Claude Code installer on Windows (the [start page](https://jaredrhod.com/start) command) downloads about 330 MB and prints nothing while it does: no progress bar, just a blinking cursor, for a few minutes, longer on slow wifi. People close the window because it looks dead, and then nothing is installed, so the next paste says `claude` is not recognized. Paste the start page command again and leave the window alone until it prints "Installation complete!" and then "All set." Then come back here and paste the install command again. It is safe to re-run: it skips the download it already did.

## The Mac says "xcrun: error: invalid active developer path"

Your Mac is missing Apple's Command Line Tools, which git needs. One command fixes it: run `xcode-select --install` in the same terminal, click Install on the popup, wait the few minutes it takes, then paste the install command again. This also shows up on Macs that recently upgraded macOS, because the upgrade can clear the tools; the same command puts them back.

## Claude opened a welcome screen (or asked me to log in) instead of setting up

Then this is your first-ever launch of Claude Code, and it runs its own one-time setup before anything else can happen: pick a text style, choose "Claude account with subscription" as the sign-in method (not the Console option, that's pay-per-use developer billing), and log in through your browser. Your "set me up" from the install command didn't survive that detour. No harm done: once you're signed in, paste the install command again and the wizard starts talking.

## The face sits at idle while the voice talks

The wiring is one config line, plus a restart. Check both:

1. `ai-visualizer/ai-visualizer.json` should have `"bus_dir"` pointing at your backtalk folder. (The same wire can run from the other side instead: `"signals_dir"` in `backtalk/backtalk.json` pointing at the visualizer folder. One direction, not both.)
2. Restart the visualizer server after any config change (Ctrl-C the stack, run start.sh again). Config edits only take effect on restart.

While the agent speaks, the backtalk folder should contain fresh `.voice_state` and `.voice_waveform` files. If they are not appearing, the problem is on the voice side; work backtalk's own guide.

## The greeting doesn't speak on launch

The greeting line lives in `backtalk/backtalk.json` under `"greeting"`. If it is missing or empty, the launch is silent by configuration. The voice piece itself failing to start is a different problem; its terminal output says why, and its guide covers the classics.

## start.sh says a piece is starting but nothing appears

- The face opens a browser tab automatically, on whichever face your `ai-visualizer.json` names. If no tab appears, open `http://127.0.0.1:8790/` yourself and click your face from the gallery. That address is the picker, not a face, so going straight there and expecting the animation is the usual confusion.
- The hands never open a tab automatically (the camera page should be opened deliberately): `http://127.0.0.1:8794/` in Chrome.
- Two stacks can't run at once. If a port is already busy from an earlier session, Ctrl-C the old terminal or close it, then start again.

## My agent forgot who it is

Your agent's identity lives in the `CLAUDE.md` in your HOME folder (the folder containing all the tool folders), and Claude Code only reads it when you open Claude Code IN that folder. Opening Claude Code inside one of the tool subfolders boots the tool's own instructions instead. Daily habit: work from the home folder.

## I moved my agent folder somewhere else

Everything is wired with paths, so a move breaks the wires. Open Claude Code in the new location and say: "read fullstack-agent/fullstack-agent.md and re-run the wiring phase." Rewiring takes a minute and touches only the config paths.

## Updates

`./fullstack-agent/update.sh` pulls every piece. Your files (your CLAUDE.md, your vault, your notes) are never inside the repos' tracked files, so updates cannot touch them. If git complains about a config file you edited (backtalk.json, ai-visualizer.json), your edit wins; keep your version.

---

# Android

Everything above still applies on a phone — the pieces are the same, the configs are the same, the seams are the same. This section covers only what Android changes. The setup wizard for Android is `fullstack-agent-android.md` in this repo.

## Termux is from the Google Play Store

That is the problem. The Play Store copy is abandoned and its packages are broken in ways that present as our bug — a Python that will not import, a package that will not install. Uninstall it and install Termux from [F-Droid](https://f-droid.org/packages/com.termux/) or the [GitHub releases](https://github.com/termux/termux-app/releases/latest). Same for Termux:API and Termux:Widget if you use them.

## `python3: command not found` in Termux

Phones ship no Python. It is one command, and the wizard runs it, but if you are starting something by hand:

```
pkg install python
```

The face and the hands run on any Python 3.8+. The voice line requires 3.11 to 3.13 (`python3 --version`); `pkg upgrade python` fixes a too-old one.

## The face's URL opens to a picker, not the animation

`http://127.0.0.1:8790/` is the gallery — the picker, not a face — and that is the usual confusion on every platform. Your face is `http://127.0.0.1:8790/faces/<face>/`, where `<face>` is the one `ai-visualizer.json` names (`board` by default). `start.android.sh` opens the right one for you; tapping the wrong address by hand is how this happens.

## The browser did not open at all

Three things can do this, and the script handles the first two:

1. **No Termux:API.** The script falls back to the system activity manager (`am start`) and then to printing the URL. If you see the URL printed, open it yourself; that is a supported path, not a failure.
2. **`am` claims `cmd` is not found.** Termux's PATH does not include `/system/bin`, where `am` lives; `start.android.sh` puts it on PATH before calling `am`. If you are calling `am` yourself: `export PATH="/system/bin:$PATH"` first.
3. **The phone refused the start.** Some phones deny an activity start from a backgrounded shell. Nothing to fix in the script; open the URL from the notification Termux shows, or keep Termux in the foreground when you launch.

## The servers die when I switch apps

Android reclaims memory aggressively, and a backgrounded Termux session is a target. Termux's own notification is the lifeline: swiping it away stops the agent. `start.android.sh` takes a wake lock via `termux-wake-lock` when Termux:API is installed, which asks Android to let it run. Without Termux:API installed, keep the Termux session in the foreground while the agent works.

## The voice does not work

This is the one honest limit on Android, and it is a property of the platform rather than a bug. The voice line depends on native libraries for speech-to-text, text-to-speech, and audio I/O that ship as wheels built for glibc desktops; Termux is not glibc, so those do not run as-is and must build from source, which may fail. And push-to-talk's global key hook has no implementation on Android at all — a phone does not let one app watch the whole keyboard.

What to do:

- **Switch to always-listening** (`mic_mode` open in `backtalk.json`), and use a tap — the home-screen button, a Termux:Widget shortcut, or the browser's mic control — where you would have held a key.
- **Try the install and read the error**: the failure names the library that will not build, and your agent can work it with you.
- **The rest is complete without the voice.** The face, the hands, and the memory are unaffected, and the wizard delivers the first greeting in text when the voice is missing. Nobody's first hello is silent.

Never accept "the voice works" from a successful `uv sync` alone. It works only after you have heard it speak.

## Obsidian cannot see my vault

The vault has to be in shared storage, not in Termux's private folder, because Obsidian cannot read `/data/data/com.termux/files/home` — no app can read another app's private data. The wizard puts it at `~/storage/shared/<name>` (that is `/storage/emulated/0/<name>`), and `~/storage` only exists after you have run `termux-setup-storage` once and granted the permission. If you skipped that, run it now, then move the vault into `~/storage/shared/` and point Obsidian at it from the app's vault picker.

## The launcher app's buttons do nothing

The app hands commands to Termux through its `RunCommandService`. If nothing happens:

- **Is Termux installed?** The app offers both install sources when it is not.
- **Is the home folder right?** Launcher settings → the agent home folder. It defaults to `/data/data/com.termux/files/home/my-agent`; if your agent lives elsewhere, point it there.
- **Did a phone refuse the start?** The toast prints the exact command; run it inside Termux and it works. The four buttons are convenience over the same scripts, never a dependency.

## `start.android.sh` says the face is starting but nothing appears

Same story as on a desktop, plus the browser: two stacks cannot run at once, so if a port is busy from an earlier session, stop the old Termux session (its notification has a Stop) and start again. Then open the printed URL yourself if the browser did not come up.

## Updating on Android

Tap the **Update \<name\>** widget button or run `./fullstack-agent/update.android.sh`. It pulls every piece and shows what changed before applying it, and your files are never inside the repos' tracked files, so updates cannot touch them. If `git init -b main` ever errors on an old device, the script already avoids it (it uses `symbolic-ref` instead, which works on every git version).
