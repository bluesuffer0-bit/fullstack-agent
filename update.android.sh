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

# Android (Termux) port of update.sh.
#
# Pulls the newest version of every installed piece, and of this repo.
# Your own files (your CLAUDE.md, your vault, your configs) live outside
# the repos' tracked files, so updates never touch them. If git reports
# a conflict on a config you edited, your edit wins; keep your version.
#
# On Android the "Update <name>" desktop shortcut does not exist; the
# equivalent is a Termux:Widget script in ~/.shortcuts, installed by
# mkshortcuts.android.sh.

HERE="$(cd "$(dirname "$0")" && pwd)"
HOME_DIR="$(dirname "$HERE")"

# Everything runs from main() so the whole script is parsed before any of
# it executes; updating this very file mid-run can then never garble it.
# This is the same hardening update.sh carries, and the reason is the same:
# cmd and bash both read scripts by byte offset, so a script that replaces
# itself partway through runs garbage from that point on.
main() {
  if ! command -v git >/dev/null 2>&1; then
    cat <<'EOF'

  git is not installed. In Termux, one command installs it:

      pkg install git

  Then run this script again.

EOF
    return 1
  fi

  for repo in fullstack-agent ai-memory-vault backtalk barehands ai-visualizer; do
    CFG=""
    case "$repo" in
      backtalk) CFG="backtalk.json" ;;
      barehands) CFG="barehands.json" ;;
      ai-visualizer) CFG="ai-visualizer.json" ;;
    esac
    [ -d "$HOME_DIR/$repo" ] || continue
    if [ ! -d "$HOME_DIR/$repo/.git" ]; then
      # A folder that arrived by copy (or was hand-moved in) instead of by
      # clone. Wire it to updates in place rather than skipping it, and
      # carry the person's config across the reset, because a config file
      # is the one thing in a repo folder that is genuinely theirs.
      echo "== $repo (wiring to updates)"
      [ -n "$CFG" ] && [ -f "$HOME_DIR/$repo/$CFG" ] && cp "$HOME_DIR/$repo/$CFG" "$HOME_DIR/$repo/$CFG.mine"
      # Portable default branch: `git init -b main` needs git 2.28 (2020),
      # and an Android device carrying an older git is common enough that
      # the -b form has to be assumed broken. symbolic-ref sets the
      # initial branch on every version, with no commit required.
      git -C "$HOME_DIR/$repo" init -q
      git -C "$HOME_DIR/$repo" symbolic-ref HEAD refs/heads/main
      git -C "$HOME_DIR/$repo" remote add origin "https://github.com/jaredrhod/$repo"
      git -C "$HOME_DIR/$repo" fetch -q origin
      git -C "$HOME_DIR/$repo" reset -q --hard origin/main
      git -C "$HOME_DIR/$repo" branch -q --set-upstream-to=origin/main main
      [ -n "$CFG" ] && [ -f "$HOME_DIR/$repo/$CFG.mine" ] && mv "$HOME_DIR/$repo/$CFG.mine" "$HOME_DIR/$repo/$CFG"
      echo "   wired to updates. everything is current."
      continue
    fi
    echo "== $repo"
    # Show what is coming before it arrives, so an update never happens
    # silently. Suppressed errors: a repo whose upstream tracking is not
    # set yet still gets pulled below.
    git -C "$HOME_DIR/$repo" fetch -q origin 2>/dev/null
    git -C "$HOME_DIR/$repo" log --oneline "..@{u}" 2>/dev/null | sed "s/^/   new: /"
    # A config the person edited is tracked by git in these repos (the
    # .example is the shipped name, the real one is generated at setup).
    # Stage it aside, let the pull win the file, then put theirs back:
    # their config survives, and git stops reporting a conflict on it.
    MIGRATE=0
    if [ -n "$CFG" ] && [ -f "$HOME_DIR/$repo/$CFG" ] && \
       git -C "$HOME_DIR/$repo" ls-files --error-unmatch "$CFG" >/dev/null 2>&1; then
      cp "$HOME_DIR/$repo/$CFG" "$HOME_DIR/$repo/$CFG.mine" && \
        git -C "$HOME_DIR/$repo" checkout -q -- "$CFG" && MIGRATE=1
    fi
    git -C "$HOME_DIR/$repo" pull --ff-only || \
      echo "   (couldn't fast-forward; your local edits win. See the note at the top of this script.)"
    if [ "$MIGRATE" = 1 ] && [ -f "$HOME_DIR/$repo/$CFG.mine" ]; then
      mv "$HOME_DIR/$repo/$CFG.mine" "$HOME_DIR/$repo/$CFG"
    fi
  done
  echo "update complete."

  # The desktop version looks for a `~/Desktop/Update *.command` and hints
  # at one. Android has no desktop; the equivalent is a Termux:Widget
  # script, which the person runs from a home-screen widget instead.
  if ! ls "$HOME/.shortcuts/Update "*.sh >/dev/null 2>&1; then
    echo "Tip: want a home-screen Update button? Run ./mkshortcuts.android.sh once."
  fi
}
main "$@"
