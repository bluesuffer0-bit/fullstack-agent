#!/bin/bash
# fullstack-agent (Android): give your AI a full stack — memory, voice, face, hands.
# Copyright (C) 2026 Jared Rhodenizer
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published
# by the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#
# SPDX-License-Identifier: AGPL-3.0-or-later

# Android (Termux) port of start.sh. Same pieces, same order:
#   ai-visualizer (the face)  -> served locally, opened in the Android browser
#   barehands     (the hands) -> URL printed (and copied); open it in Chrome
#   backtalk      (the voice) -> runs in this terminal; Ctrl-C stops EVERYTHING
# Pieces you didn't install are skipped automatically.
#
#   ./start.android.sh          everything installed
#   ./start.android.sh voice    the voice and the face (no hands)
#   ./start.android.sh hands    the voice and the hands board (no face)
#
# WHY THIS FILE EXISTS APART FROM start.sh, because the differences are all
# real and none of them are cosmetic:
#
#   - Python. On a desktop it ships with the OS. On Android it does not, so
#     a missing interpreter is a normal first-run state and gets the one
#     command that fixes it instead of a stack trace.
#   - Opening a browser. `webbrowser.open()` -- which the face uses on a
#     desktop to bring its own page up -- has no browser to talk to in
#     Termux, so the face is started with --no-open and this script raises
#     the page itself, through Termux:API if you have it and the system
#     activity manager if you do not.
#   - Activity manager. `am` is a shell script on /system/bin that calls
#     `cmd`, and Termux's PATH does not include /system/bin, so it has to
#     be put on PATH before it is called or it dies claiming `cmd` is
#     missing while the real problem is a PATH lookup.
#   - Staying alive. Android will happily kill a backgrounded Termux session
#     to reclaim memory, taking both servers with it, so a wake lock is
#     taken for the life of this script when Termux:API is present. It is
#     released on the way out.

HERE="$(cd "$(dirname "$0")" && pwd)"
HOME_DIR="$(dirname "$HERE")"
MODE="${1:-all}"
PIDS=()

# /system/bin is deliberately NOT on Termux's PATH; see the header. Adding
# it changes nothing on a desktop, and makes `am` work on Android.
export PATH="/system/bin:$PATH"

# Android has no global keyboard hook, which the voice line's push-to-talk
# depends on. This is an environment fact, not a config knob, so it is said
# once, up front, instead of being discovered as a confusing silence later.
ON_ANDROID=0
if [ -n "$PREFIX" ] || [ -d /system/bin ]; then
  ON_ANDROID=1
fi

cleanup() {
  trap - EXIT INT TERM
  for p in "${PIDS[@]}"; do kill "$p" 2>/dev/null; done
  # Hand the wake lock back. Without this the phone stays awake for no
  # reason after the agent is stopped, which is a battery complaint that
  # looks like a bug in the face.
  if [ "$WAKE_LOCK_TAKEN" = "1" ] && command -v termux-wake-unlock >/dev/null 2>&1; then
    termux-wake-unlock 2>/dev/null
  fi
  echo
  echo "agent stopped."
}
trap cleanup EXIT INT TERM
WAKE_LOCK_TAKEN=0

# Raise a URL in the Android browser. Tries the polite way first
# (Termux:API, which respects the person's default browser and asks before
# opening unknown apps) and falls back to the system activity manager.
# Neither is guaranteed to be allowed -- some phones refuse a start from a
# backgrounded shell -- so the URL is always printed as the last resort,
# because a URL the person can tap is never a failure.
open_url() {
  local url="$1"
  if command -v termux-open-url >/dev/null 2>&1; then
    termux-open-url "$url" 2>/dev/null && return 0
  fi
  if command -v am >/dev/null 2>&1; then
    am start -a android.intent.action.VIEW -d "$url" >/dev/null 2>&1 && return 0
  fi
  return 1
}

# Put a URL on the clipboard so it survives the trip to Chrome, which on a
# phone is a task switch and not a new tab. Best effort only.
copy_url() {
  command -v termux-clipboard-set >/dev/null 2>&1 && \
    termux-clipboard-set "$1" 2>/dev/null
}

require_python() {
  if command -v python3 >/dev/null 2>&1; then
    return 0
  fi
  cat <<'EOF'

  Python is not installed, and the face and the hands are Python programs.

  In Termux, one command installs it:

      pkg install python

  Then run this script again. (Termux's Python is a current 3.11+, which is
  also what the voice line needs.)

EOF
  return 1
}

echo "fullstack-agent: starting from $HOME_DIR"

# The face and the hands both need Python; the voice line needs more still.
# Check once, before anything starts, so a missing interpreter is not
# reported by a server that is already half up.
if ! require_python; then
  exit 1
fi

# Keep the phone awake while the agent is running. Termux:API is optional
# (nothing else here depends on it); without it, Android may still kill the
# session if it is backgrounded, and the honest answer to that is the
# notification Termux shows, not a second program.
if [ "$ON_ANDROID" = "1" ] && command -v termux-wake-lock >/dev/null 2>&1; then
  termux-wake-lock 2>/dev/null && WAKE_LOCK_TAKEN=1
fi

if [ -d "$HOME_DIR/ai-visualizer" ] && [ "$MODE" != "hands" ]; then
  # --no-open: webbrowser.open() has nothing to talk to in Termux. The
  # script opens the face itself, on the face the config actually names,
  # because the gallery page it would otherwise land on is a picker and
  # not a face (a confusion documented in TROUBLESHOOTING.md).
  (cd "$HOME_DIR/ai-visualizer" && exec python3 server.py --no-open) &
  PIDS+=($!)
  FACE="board"
  if [ -f "$HOME_DIR/ai-visualizer/ai-visualizer.json" ]; then
    FACE="$(python3 -c '
import json
try:
    print(json.load(open("ai-visualizer.json")).get("face") or "board")
except Exception:
    print("board")
' 2>/dev/null)"
  fi
  FACE_URL="http://127.0.0.1:8790/faces/${FACE}/"
  echo "  face:  starting (opening your browser on the $FACE face)"
  # The server needs a moment to bind the port. Give it the moment, then
  # raise the page; opening before the port is live lands on a blank tab
  # that never reloads.
  (
    sleep 2
    if ! open_url "$FACE_URL"; then
      echo
      echo "  Could not open the browser automatically. Open this URL yourself:"
      echo "    $FACE_URL"
    fi
  ) &
  PIDS+=($!)
fi

if [ -d "$HOME_DIR/barehands" ] && [ "$MODE" != "voice" ]; then
  (cd "$HOME_DIR/barehands" && exec python3 server.py) &
  PIDS+=($!)
  HANDS_URL="http://127.0.0.1:8794/stage.html"
  # Deliberately NOT auto-opened, same as on a desktop: the camera page is
  # something the person opens on purpose. The URL is copied as well as
  # printed, because Chrome is a task switch away and the address bar is
  # not where anyone wants to type 127.0.0.1.
  copy_url "$HANDS_URL"
  echo "  hands: starting (open this URL in Chrome when you want the board):"
  echo "    $HANDS_URL"
  echo "    (copied to your clipboard)"
fi

if [ -d "$HOME_DIR/backtalk" ]; then
  if [ ! -x "$HOME_DIR/backtalk/run.sh" ]; then
    chmod +x "$HOME_DIR/backtalk/run.sh" 2>/dev/null
  fi
  echo "  voice: starting (hold your talk key and speak; Ctrl-C here stops everything)"
  cd "$HOME_DIR/backtalk" && ./run.sh
else
  echo
  echo "No voice installed; servers are up. Ctrl-C stops everything."
  wait
fi
