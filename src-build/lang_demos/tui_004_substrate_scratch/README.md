# TUI 004: Substrate Scratch

A React Blessed terminal view that connects to the scratch_v3 substrate worker
and queries currencies, profiles, wallets, and wallet assets through generated
RPCs. The cache slice uses the generated Currency dataview with the paired
SQLite cache. Each selection displays the returned model data and an example
event; connection and query errors appear in the output panel.

Build the separate Node project from the Foundation root:

```bash
lein run -m lang-demos.tui-004-substrate-scratch.build
```

Start the local Supabase stack configured for `scratch_v3` on port `55121`:

```bash
supabase start --workdir docker/local-min
```

Then run its generated app:

```bash
cd .build/demo/tui-004-substrate-scratch
make init
make dev
```

Webpack's Node plugin starts the TUI after it builds. Use the mouse or focus a
slice and press Enter to query it. Press `q` to disconnect and exit the TUI;
Ctrl+C stops the watcher.
