# ChatGPT-web MCP server for Kimi CLI

Exposes the ChatGPT web app (chatgpt.com) as Kimi tools by driving a real
Chromium window with Playwright. Uses your logged-in ChatGPT session (e.g. a
Plus subscription) — no API key involved.

## How it works

- Kimi CLI spawns `server.py` as a stdio MCP server (registered in
  `.kimi/mcp.json` under the key `chatgpt`).
- On the first tool call the server starts a private `Xvfb` display and
  launches Chromium (from `/snap/bin/chromium`) in real headed mode on it.
  Headless mode is blocked by Cloudflare; headed-under-Xvfb is not.
- The browser profile lives in `./profile/` (gitignored) so login persists
  across Kimi sessions. Browser and Xvfb are shut down when the Kimi session
  ends or `chatgpt_close` is called.

## Tools

- `chatgpt_open()` — bring up the browser window on chatgpt.com, report status.
- `chatgpt_ask(prompt, new_chat=true, timeout_ms=120000)` — send a prompt,
  wait for the streamed answer to finish, return its text.
- `chatgpt_read()` — return the latest assistant message on the current page.
- `chatgpt_screenshot()` — save the window to `./run/last.png`, return the path.
- `chatgpt_close()` — shut down browser + Xvfb.

## Setup

```bash
python3 -m venv .kimi/mcp-chatgpt/.venv
.kimi/mcp-chatgpt/.venv/bin/pip install mcp playwright
```

No `playwright install` is needed — the system Chromium is used. If the snap
Chromium misbehaves, run `.kimi/mcp-chatgpt/.venv/bin/playwright install chromium`
and point `CHATGPT_CHROMIUM` at the downloaded binary.

## First-time login

The Xvfb window is not visible, so login is a one-time interactive step:

```bash
# from your laptop:
ssh -X <this-host>
cd <repo>
.kimi/mcp-chatgpt/login.sh
```

Log in in the window that appears, then close it. The session is stored in
`./profile/` and reused by the MCP server from then on. (Running `login.sh`
directly on the machine's desktop works too.)

## Manual verification

```bash
{
  echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
  echo '{"jsonrpc":"2.0","method":"notifications/initialized"}'
  echo '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
} | .kimi/mcp-chatgpt/.venv/bin/python3 .kimi/mcp-chatgpt/server.py
```

## Troubleshooting

- **Login wall reported by tools** — run `login.sh` again; the session may have
  expired.
- **Cloudflare challenge** — call `chatgpt_screenshot` and view `run/last.png`.
  Solve it once via `login.sh` on a visible display; the clearance cookie is
  persisted in the profile.
- **Selectors broken after a chatgpt.com update** — all DOM hooks live in the
  `SELECTORS` dict at the top of `server.py`; patch them there.
- **Display collisions** — the server scans for a free X display starting at
  `CHATGPT_DISPLAY_START` (default 300).
- This automates the consumer web app with your own session; credentials are
  never stored by the server — everything lives in the gitignored `profile/`.

## Files

- `server.py` — MCP server implementation.
- `login.sh` — one-time interactive login helper.
- `.venv/` — Python virtual environment with `mcp` and `playwright`.
- `profile/` — persistent Chromium profile (gitignored, contains login cookies).
- `run/` — runtime artifacts such as `last.png` (gitignored).
