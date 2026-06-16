---
name: clojure-repl
description: Use clojure-mcp-light (delimiter-safe nREPL evaluation) for REPL-first work in foundation-base. Prefer these tools over raw shell when evaluating Clojure, repairing generated code, or discovering running REPLs.
---

# Clojure REPL (clojure-mcp-light)

This skill wires Bruce Hauman's `clojure-mcp-light` toolchain into Kimi Code CLI for this project. It provides delimiter-safe Clojure evaluation and repair via MCP tools.

## Available MCP tools

Tool names are prefixed by Kimi as `mcp__clojure-light__<tool>`:

- `clojure_eval(code, port?)` — evaluate a Clojure form against a running nREPL server.
- `clojure_repair(code)` — repair unbalanced delimiters and apply `cljfmt` formatting.
- `list_nrepl_ports()` — discover running nREPL servers in the current directory.

## Setup (one-time)

The following are already installed for this project:

- `parinfer-rust` (delimiter repair backend)
- `clj-nrepl-eval` (nREPL evaluator)
- `clj-paren-repair` (delimiter repair + formatting)
- `.kimi/mcp-clojure-light/server.py` (MCP wrapper, Python venv)
- `~/.kimi/mcp.json` is registered with the `clojure-light` MCP server

Verify the server is reachable:

```bash
kimi mcp test clojure-light
```

If you need to point Kimi at a different config file for testing:

```bash
kimi --mcp-config-file /path/to/mcp.json mcp test clojure-light
```

## Core workflow

1. **Before evaluating**, check for a running REPL:
   - Use `list_nrepl_ports`.
   - Or check `.nrepl-port` in the project root.

2. **If no REPL is running**, start one:
   ```bash
   lein repl :headless
   ```
   Wait for `.nrepl-port` to appear.

3. **Evaluate small forms** with `clojure_eval`.
   - Prefer returning data (maps, vectors, strings) over printing.
   - `require` or `ns` load the namespace first if needed.

4. **Repair before writing** — when generating multi-line Clojure code, pass it through `clojure_repair` first and only write the repaired output.

5. **Validate** with the narrowest test command:
   ```bash
   ./lein test :only <namespace>
   ./lein test :with <subsystem>
   ```

## Namespace discipline

- Evaluate the `ns` form or `require` the namespace before calling vars from it.
- Use fully-qualified vars when moving between namespaces would add confusion.
- Prefer `user` for one-off setup and small probes.
- Avoid mutating production namespaces during exploration; use scratch namespaces or small pure probes.

## Examples

```clojure
;; Probe a pure function
(require 'hara.typed.xtalk-common)
(:name (hara.typed.xtalk-common/primitive-type :xt/int))
```

```clojure
;; Run a type check after editing
(hara.typed.xtalk-analysis/check-namespace 'hara.model.spec-xtalk-typed-fixture)
```

## Important notes

- This project uses the custom `code.test` framework (`fact` + `=>`), not `clojure.test`.
- `clojure_eval` sessions persist per host:port; state (defs, requires) survives across calls.
- If evaluation returns a huge object, the tool truncates aggressively. Prefer returning reduced summaries.
- The MCP wrapper uses newline-delimited JSON-RPC over stdio (the transport expected by the current `mcp` Python SDK and Kimi CLI).
