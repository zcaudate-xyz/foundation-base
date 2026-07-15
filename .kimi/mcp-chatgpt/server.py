#!/usr/bin/env python3
"""MCP server exposing the ChatGPT web app (chatgpt.com) via a browser.

Runs Chromium in real headed mode under a private Xvfb display (headless mode
is blocked by Cloudflare; headed under Xvfb is not) with a persistent profile
directory so the ChatGPT login survives restarts. No API key is used — the
tools drive the consumer web app with the user's own session.

Tools:
- chatgpt_open()        -> ensure browser + page up, report status
- chatgpt_ask(prompt)   -> send a prompt, wait for the answer, return its text
- chatgpt_read()        -> return the latest assistant message
- chatgpt_screenshot()  -> save a PNG of the window, return its path
- chatgpt_close()       -> shut down browser and Xvfb

First-time login: run ./login.sh once over `ssh -X` or on the desktop.
"""

import asyncio
import atexit
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import TextContent, Tool

BASE_DIR = Path(__file__).resolve().parent
PROFILE_DIR = BASE_DIR / "profile"
RUN_DIR = BASE_DIR / "run"
CHATGPT_URL = "https://chatgpt.com/"
CHROMIUM = os.environ.get("CHATGPT_CHROMIUM", "/snap/bin/chromium")
SCREEN = os.environ.get("CHATGPT_SCREEN", "1920x1080x24")
XVFB_DISPLAY_START = int(os.environ.get("CHATGPT_DISPLAY_START", "300"))

# chatgpt.com DOM hooks. These are the brittle part of web automation: if a
# tool starts failing after a site update, inspect a screenshot and patch here.
SELECTORS = {
    "composer": "#prompt-textarea",
    "send": 'button[data-testid="send-button"]',
    "stop": 'button[data-testid="stop-button"]',
    "assistant": '[data-message-author-role="assistant"]',
    "login_button": '[data-testid="login-button"]',
}

app = Server("chatgpt-web-kimi")

_state = {"xvfb": None, "pw": None, "ctx": None, "page": None, "display": None}
_lock = asyncio.Lock()


def _log(msg: str) -> None:
    print(f"[mcp-chatgpt] {msg}", file=sys.stderr, flush=True)


def _used_displays() -> set:
    used = set()
    x11_dir = Path("/tmp/.X11-unix")
    if x11_dir.is_dir():
        for entry in x11_dir.iterdir():
            m = re.fullmatch(r"X(\d+)", entry.name)
            if m:
                used.add(int(m.group(1)))
    for lock in Path("/tmp").glob(".X*-lock"):
        m = re.fullmatch(r"\.X(\d+)-lock", lock.name)
        if m:
            used.add(int(m.group(1)))
    return used


def _start_xvfb() -> tuple:
    """Spawn Xvfb on a free display. Returns (process, display_number)."""
    used = _used_displays()
    for n in range(XVFB_DISPLAY_START, XVFB_DISPLAY_START + 50):
        if n in used:
            continue
        proc = subprocess.Popen(
            ["Xvfb", f":{n}", "-screen", "0", SCREEN, "-nolisten", "tcp"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        socket = Path(f"/tmp/.X11-unix/X{n}")
        for _ in range(100):
            if socket.exists():
                _log(f"Xvfb up on :{n}")
                return proc, n
            if proc.poll() is not None:
                break
            time.sleep(0.1)
        proc.terminate()
        _log(f"Xvfb failed on :{n}, trying next")
    raise RuntimeError("could not start Xvfb on any free display")


async def _launch() -> None:
    from playwright.async_api import async_playwright

    RUN_DIR.mkdir(exist_ok=True)
    PROFILE_DIR.mkdir(exist_ok=True)
    xvfb, display = _start_xvfb()
    _state["xvfb"] = xvfb
    _state["display"] = display
    pw = await async_playwright().start()
    _state["pw"] = pw
    env = dict(os.environ)
    env["DISPLAY"] = f":{display}"
    ctx = await pw.chromium.launch_persistent_context(
        str(PROFILE_DIR),
        executable_path=CHROMIUM,
        headless=False,
        env=env,
        args=["--no-first-run", "--disable-dev-shm-usage", "--window-size=1280,900"],
        viewport={"width": 1280, "height": 900},
        timeout=60_000,
    )
    _state["ctx"] = ctx
    page = ctx.pages[0] if ctx.pages else await ctx.new_page()
    _state["page"] = page
    await page.goto(CHATGPT_URL, wait_until="domcontentloaded", timeout=60_000)
    # give any Cloudflare interstitial a moment to auto-resolve
    await page.wait_for_timeout(3000)


async def _teardown() -> None:
    ctx = _state.get("ctx")
    if ctx is not None:
        try:
            await ctx.close()
        except Exception:
            pass
    pw = _state.get("pw")
    if pw is not None:
        try:
            await pw.stop()
        except Exception:
            pass
    xvfb = _state.get("xvfb")
    if xvfb is not None and xvfb.poll() is None:
        xvfb.terminate()
        try:
            xvfb.wait(timeout=5)
        except subprocess.TimeoutExpired:
            xvfb.kill()
    _state.update({"xvfb": None, "pw": None, "ctx": None, "page": None, "display": None})


def _atexit_cleanup() -> None:
    xvfb = _state.get("xvfb")
    if xvfb is not None and xvfb.poll() is None:
        xvfb.terminate()


atexit.register(_atexit_cleanup)


async def _ensure_page():
    """Return a live page, launching the browser stack if needed."""
    async with _lock:
        page = _state.get("page")
        ctx = _state.get("ctx")
        if page is not None and not page.is_closed():
            return page
        if ctx is None:
            try:
                await _launch()
            except Exception:
                await _teardown()
                raise
            return _state["page"]
        page = await ctx.new_page()
        _state["page"] = page
        await page.goto(CHATGPT_URL, wait_until="domcontentloaded", timeout=60_000)
        await page.wait_for_timeout(3000)
        return page


def _err(text: str):
    return [TextContent(type="text", text=f"ERROR: {text}")]


async def _status(page) -> dict:
    url = page.url
    title = await page.title()
    on_login_wall = "auth.openai.com" in url
    if not on_login_wall:
        try:
            on_login_wall = await page.locator(SELECTORS["login_button"]).count() > 0
        except Exception:
            pass
    composer = False
    try:
        composer = await page.locator(SELECTORS["composer"]).count() > 0
    except Exception:
        pass
    return {
        "url": url,
        "title": title,
        "display": f":{_state.get('display')}",
        "logged_in_guess": (not on_login_wall) and composer,
        "login_wall": on_login_wall,
        "composer_present": composer,
    }


def _login_help(status: dict) -> str:
    return (
        "Not logged in to ChatGPT (login wall detected).\n"
        "Run the one-time interactive login helper from a shell with a display:\n"
        "  .kimi/mcp-chatgpt/login.sh\n"
        "(e.g. reconnect with `ssh -X` first, or run it on the desktop.)\n"
        "You can also call chatgpt_screenshot to see the current page.\n"
        f"status: {json.dumps(status)}"
    )


async def _last_assistant_text(page) -> str:
    locator = page.locator(SELECTORS["assistant"])
    count = await locator.count()
    if count == 0:
        return ""
    return (await locator.nth(count - 1).inner_text(timeout=5000)).strip()


async def _wait_answer_stable(page, timeout_ms: int) -> str:
    """Wait until streaming ends and the last answer text stops changing."""
    deadline = time.monotonic() + timeout_ms / 1000.0
    # wait for streaming to start (stop button appears), briefly
    try:
        await page.wait_for_selector(SELECTORS["stop"], timeout=10_000, state="visible")
    except Exception:
        pass
    last = ""
    stable = 0
    while time.monotonic() < deadline:
        try:
            streaming = await page.locator(SELECTORS["stop"]).count() > 0
        except Exception:
            streaming = False
        text = await _last_assistant_text(page)
        if text and text == last and not streaming:
            stable += 1
            if stable >= 3:  # ~1.5 s unchanged
                return text
        else:
            stable = 0
            last = text
        await asyncio.sleep(0.5)
    return last


@app.call_tool()
async def call_tool(name: str, arguments: dict):
    try:
        if name == "chatgpt_open":
            page = await _ensure_page()
            status = await _status(page)
            if status["login_wall"]:
                return [TextContent(type="text", text=_login_help(status))]
            return [TextContent(type="text", text=json.dumps(status, indent=2))]

        if name == "chatgpt_ask":
            prompt = arguments.get("prompt", "")
            if not prompt.strip():
                return _err("prompt is empty")
            new_chat = arguments.get("new_chat", True)
            timeout_ms = int(arguments.get("timeout_ms", 120_000))
            page = await _ensure_page()
            if new_chat:
                await page.goto(CHATGPT_URL, wait_until="domcontentloaded", timeout=60_000)
                await page.wait_for_timeout(2000)
            status = await _status(page)
            if status["login_wall"]:
                return [TextContent(type="text", text=_login_help(status))]
            try:
                await page.wait_for_selector(SELECTORS["composer"], timeout=30_000)
            except Exception:
                return _err(
                    "composer not found — the page layout may have changed or a "
                    "challenge is showing. Call chatgpt_screenshot to inspect."
                )
            composer = page.locator(SELECTORS["composer"])
            await composer.click()
            await composer.fill(prompt)
            await page.wait_for_timeout(500)
            try:
                send = page.locator(SELECTORS["send"])
                await send.wait_for(state="visible", timeout=5000)
                if await send.is_enabled():
                    await send.click()
                else:
                    await page.keyboard.press("Enter")
            except Exception:
                await page.keyboard.press("Enter")
            answer = await _wait_answer_stable(page, timeout_ms)
            if not answer:
                return _err(
                    "no assistant answer detected within timeout — "
                    "call chatgpt_screenshot to inspect the page state."
                )
            return [TextContent(type="text", text=answer)]

        if name == "chatgpt_read":
            page = await _ensure_page()
            text = await _last_assistant_text(page)
            if not text:
                return _err("no assistant message found on the current page")
            return [TextContent(type="text", text=text)]

        if name == "chatgpt_screenshot":
            page = await _ensure_page()
            RUN_DIR.mkdir(exist_ok=True)
            out = RUN_DIR / "last.png"
            await page.screenshot(path=str(out))
            return [TextContent(type="text", text=str(out))]

        if name == "chatgpt_close":
            async with _lock:
                await _teardown()
            return [TextContent(type="text", text="browser and Xvfb shut down")]

        raise RuntimeError(f"Unknown tool: {name}")
    except Exception as e:
        _log(f"{name} failed: {e}")
        return _err(f"{name} failed: {e}")


@app.list_tools()
async def list_tools():
    return [
        Tool(
            name="chatgpt_open",
            description=(
                "Open the ChatGPT web app in the managed browser window and report "
                "status (URL, title, login state). Call this first; if it reports a "
                "login wall, the user must run .kimi/mcp-chatgpt/login.sh once."
            ),
            inputSchema={"type": "object", "properties": {}},
        ),
        Tool(
            name="chatgpt_ask",
            description=(
                "Send a prompt to ChatGPT (chatgpt.com, logged-in Plus session) and "
                "return the answer text. Waits until the response finishes streaming. "
                "Use this to consult ChatGPT from within Kimi CLI."
            ),
            inputSchema={
                "type": "object",
                "properties": {
                    "prompt": {
                        "type": "string",
                        "description": "The prompt to send to ChatGPT.",
                    },
                    "new_chat": {
                        "type": "boolean",
                        "description": "Start a fresh chat first (default true). Set false for follow-ups in the current conversation.",
                    },
                    "timeout_ms": {
                        "type": "integer",
                        "description": "Max wait for the answer in ms (default 120000).",
                    },
                },
                "required": ["prompt"],
            },
        ),
        Tool(
            name="chatgpt_read",
            description="Return the latest assistant message from the current ChatGPT page without sending anything.",
            inputSchema={"type": "object", "properties": {}},
        ),
        Tool(
            name="chatgpt_screenshot",
            description=(
                "Save a screenshot of the ChatGPT browser window to "
                ".kimi/mcp-chatgpt/run/last.png and return the path. Use to inspect "
                "login walls, Cloudflare challenges, or unexpected page states."
            ),
            inputSchema={"type": "object", "properties": {}},
        ),
        Tool(
            name="chatgpt_close",
            description="Shut down the managed ChatGPT browser window and its Xvfb display.",
            inputSchema={"type": "object", "properties": {}},
        ),
    ]


async def main():
    async with stdio_server() as (read_stream, write_stream):
        await app.run(
            read_stream,
            write_stream,
            app.create_initialization_options(),
        )


if __name__ == "__main__":
    asyncio.run(main())
