#!/usr/bin/env bash
# One-time interactive login for the ChatGPT MCP profile.
#
# The MCP server runs Chromium under a headless Xvfb display, so you cannot
# type into it directly. Run this helper once from a shell that HAS a display:
#   - reconnect with `ssh -X` and run it (window appears on your laptop), or
#   - run it on the machine's desktop session (DISPLAY is usually :0 or :1).
# Log in to ChatGPT in the window that opens, then close the window. The
# session is persisted in ./profile and the MCP server reuses it afterwards.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
PROFILE="$HERE/profile"
CHROMIUM="${CHATGPT_CHROMIUM:-/snap/bin/chromium}"

if [ -z "${DISPLAY:-}" ]; then
    echo "ERROR: no DISPLAY set." >&2
    echo "Reconnect with 'ssh -X <host>' first, or run this on the desktop." >&2
    exit 1
fi

if [ ! -x "$CHROMIUM" ]; then
    echo "ERROR: chromium not found at $CHROMIUM (override with CHATGPT_CHROMIUM)." >&2
    exit 1
fi

mkdir -p "$PROFILE"
echo "Opening Chromium on DISPLAY=$DISPLAY with profile: $PROFILE"
echo "Log in to ChatGPT, then close the browser window."
exec "$CHROMIUM" --user-data-dir="$PROFILE" --no-first-run --window-size=1100,800 "https://chatgpt.com/"
