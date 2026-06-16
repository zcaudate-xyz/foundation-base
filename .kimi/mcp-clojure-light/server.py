#!/home/hoebat/Development/greenways/gw-v2/ref/foundation/.kimi/mcp-clojure-light/.venv/bin/python3
"""MCP server wrapping clojure-mcp-light binaries for Kimi CLI.

Exposes three tools:
- clojure_eval(code: str, port: int | None) -> str
- clojure_repair(code: str) -> str
- list_nrepl_ports() -> str
"""

import shutil
import subprocess
import sys
import tempfile

from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import TextContent, Tool


app = Server("clojure-mcp-light-kimi")


def find_bin(name: str) -> str:
    path = shutil.which(name)
    if not path:
        raise RuntimeError(f"Required binary not found on PATH: {name}")
    return path


@app.call_tool()
async def call_tool(name: str, arguments: dict):
    if name == "clojure_eval":
        code = arguments.get("code", "")
        port = arguments.get("port")
        if port is None:
            try:
                with open(".nrepl-port", "r", encoding="utf-8") as f:
                    port = int(f.read().strip())
            except Exception:
                pass
        if port is None:
            return [TextContent(
                type="text",
                text="ERROR: no port provided and .nrepl-port not found. Start a REPL or pass a port.",
            )]
        bin_path = find_bin("clj-nrepl-eval")
        cmd = [bin_path, "--port", str(port)]
        result = subprocess.run(
            cmd,
            input=code,
            text=True,
            capture_output=True,
            timeout=120,
        )
        output = result.stdout
        if result.stderr:
            output += "\n" + result.stderr
        if result.returncode != 0:
            output = f"ERROR (exit {result.returncode}):\n{output}"
        return [TextContent(type="text", text=output)]

    if name == "clojure_repair":
        code = arguments.get("code", "")
        bin_path = find_bin("clj-paren-repair")
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".clj", delete=False, encoding="utf-8"
        ) as f:
            f.write(code)
            tmp_path = f.name
        try:
            result = subprocess.run(
                [bin_path, tmp_path],
                text=True,
                capture_output=True,
                timeout=30,
            )
            with open(tmp_path, "r", encoding="utf-8") as f:
                repaired = f.read()
            if result.returncode != 0:
                text = f"ERROR (could not repair):\n{result.stderr or result.stdout}\n--- original ---\n{code}"
            elif repaired != code:
                text = f"REPAIRED:\n{repaired}"
            else:
                text = f"OK (no change):\n{repaired}"
            return [TextContent(type="text", text=text)]
        finally:
            import os

            try:
                os.unlink(tmp_path)
            except Exception:
                pass

    if name == "list_nrepl_ports":
        bin_path = find_bin("clj-nrepl-eval")
        result = subprocess.run(
            [bin_path, "--discover-ports"],
            text=True,
            capture_output=True,
            timeout=30,
        )
        output = result.stdout
        if result.stderr:
            output += "\n" + result.stderr
        if result.returncode != 0:
            output = f"ERROR (exit {result.returncode}):\n{output}"
        return [TextContent(type="text", text=output or "No nREPL servers discovered.")]

    raise RuntimeError(f"Unknown tool: {name}")


@app.list_tools()
async def list_tools():
    return [
        Tool(
            name="clojure_eval",
            description="Evaluate a Clojure form against a running nREPL server. Prefer small forms; require/reload the namespace first if needed.",
            inputSchema={
                "type": "object",
                "properties": {
                    "code": {
                        "type": "string",
                        "description": "Clojure code to evaluate.",
                    },
                    "port": {
                        "type": ["integer", "null"],
                        "description": "nREPL port. If omitted, clj-nrepl-eval will discover via .nrepl-port/NREPL_PORT.",
                    },
                },
                "required": ["code"],
            },
        ),
        Tool(
            name="clojure_repair",
            description="Repair unbalanced Clojure delimiters and apply cljfmt formatting. Use before writing generated code to a file.",
            inputSchema={
                "type": "object",
                "properties": {
                    "code": {
                        "type": "string",
                        "description": "Clojure code with possible delimiter errors.",
                    },
                },
                "required": ["code"],
            },
        ),
        Tool(
            name="list_nrepl_ports",
            description="List running nREPL servers in the current directory.",
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
    import asyncio

    asyncio.run(main())
