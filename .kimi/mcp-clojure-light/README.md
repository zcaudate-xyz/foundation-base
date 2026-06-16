# clojure-mcp-light wrapper for Kimi CLI

A small MCP server that exposes the `clojure-mcp-light` binaries (`clj-nrepl-eval`, `clj-paren-repair`) as Kimi tools.

## Tools

- `clojure_eval(code, port?)` — evaluate a Clojure form via an nREPL server.
- `clojure_repair(code)` — repair unbalanced Clojure delimiters and apply `cljfmt` formatting.
- `list_nrepl_ports()` — discover running nREPL servers in the current directory.

## Running the server directly

```bash
.kimi/mcp-clojure-light/.venv/bin/python3 .kimi/mcp-clojure-light/server.py
```

The server reads/writes newline-delimited JSON-RPC over stdio (matching the `mcp` Python SDK and Kimi CLI).

## Manual verification

```bash
# 1. Check that Kimi can see the tools
kimi mcp test clojure-light

# 2. Send raw JSON-RPC messages
{
  echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
  echo '{"jsonrpc":"2.0","method":"notifications/initialized"}'
  echo '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
} | .kimi/mcp-clojure-light/.venv/bin/python3 .kimi/mcp-clojure-light/server.py
```

## Files

- `server.py` — MCP server implementation.
- `.venv/` — Python virtual environment with the `mcp` package.
- `README.md` — this file.
