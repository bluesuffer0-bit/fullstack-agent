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

# The Android counterpart of the desktop's Phase 6: where macOS gets four
# .command files on the Desktop and Windows gets four .bat files, Android
# gets four scripts that Termux:Widget runs from a home-screen widget.
#
# Termux:Widget reads any *.sh in ~/.shortcuts and lists it in its widget;
# the file's name IS the label. Tapping one runs it, so a script here does
# the same job as a double-clicked desktop shortcut.
#
#   ./mkshortcuts.android.sh          # name taken from your configs, or Jarvis
#   ./mkshortcuts.android.sh Myra     # name it yourself
#
# Requires Termux:Widget (install it alongside Termux from the same source),
# and run it once more if you rename your agent, so the labels follow it.
# Re-running is safe: it overwrites its own four scripts and touches nothing
# else in ~/.shortcuts.

HERE="$(cd "$(dirname "$0")" && pwd)"
HOME_DIR="$(dirname "$HERE")"
NAME="${1:-}"

# The name is used in the labels, so find it before writing anything. Try
# the configs the wizard wrote, which is where the name actually lives; a
# guess is worse than the default, because a mislabeled shortcut is a
# shortcut to a stranger.
if [ -z "$NAME" ]; then
  for f in backtalk/backtalk.json ai-visualizer/ai-visualizer.json barehands/barehands.json; do
    if [ -f "$HOME_DIR/$f" ] && command -v python3 >/dev/null 2>&1; then
      FOUND="$(python3 -c "
import json
try:
    print(json.load(open('$HOME_DIR/$f')).get('name') or '')
except Exception:
    pass
" 2>/dev/null)"
      [ -n "$FOUND" ] && NAME="$FOUND" && break
    fi
  done
fi
NAME="${NAME:-Jarvis}"

SHORTCUTS="$HOME/.shortcuts"
mkdir -p "$SHORTCUTS"

# Every script carries this for the same reason the macOS .command files
# carry their PATH export: a launcher does not inherit the interactive
# shell's environment, and without it the programs it needs are not found.
# Termux's prefix is where python3, git, and uv live.
HEADER='#!/data/data/com.termux/files/usr/bin/bash
export PATH="$PREFIX/bin:/system/bin:$PATH"
'

write_shortcut() {
  # $1 = label, $2 = body
  local label="$1" body="$2"
  local target="$SHORTCUTS/$label.sh"
  {
    printf '%s\n' "$HEADER"
    printf '%s\n' "$body"
  } > "$target"
  chmod +x "$target"
  echo "  wrote: $target"
}

echo "fullstack-agent: making home-screen shortcuts for $NAME"

# 1. Chat -- a typed session in the agent's home folder.
# On Android, the desktop's `claude` may not be the agent you use; the
# script tries it and says plainly if it is not there, rather than
# silently doing nothing.
write_shortcut "Chat with $NAME" "cd \"$HOME_DIR\"
if command -v claude >/dev/null 2>&1; then
  exec claude
fi
echo 'The claude command is not installed in Termux.'
echo 'This shortcut runs whatever agent you keep here; edit it to match.'
echo 'Your agent lives in: $HOME_DIR'"

# 2. Talk -- the voice and the face.
write_shortcut "Talk to $NAME" "cd \"$HOME_DIR\"
exec ./fullstack-agent/start.android.sh voice"

# 3. Barehands -- the voice and the hands board; the board IS the screen.
write_shortcut "$NAME barehands" "cd \"$HOME_DIR\"
exec ./fullstack-agent/start.android.sh hands"

# 4. Update -- pull every piece, showing what changed before applying it.
write_shortcut "Update $NAME" "cd \"$HOME_DIR/fullstack-agent\"
exec ./update.android.sh"

cat <<EOF

Done. Four scripts are in $SHORTCUTS .

Now add the widget once:
  1. Long-press a home screen, choose Widget.
  2. Find Termux:Widget and place its "Termux shortcut" (or "Termux
     shortcuts") widget.
  3. The list shows all four by name: Chat with $NAME, Talk to $NAME,
     $NAME barehands, Update $NAME.

Deleting a shortcut here is safe: it only removes the launcher, never the
agent or anything it remembers.

EOF
