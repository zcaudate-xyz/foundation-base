# TUI 004: Substrate Scratch

A React Blessed terminal view over the catalog, model descriptors, and example
events provided by `lang-demos.js-002-substrate-scratch.main`.

Build the separate Node project from the Foundation root:

```bash
lein run -m lang-demos.tui-004-substrate-scratch.build
```

Then run its generated app:

```bash
cd .build/demo/tui-004-substrate-scratch
make init
make dev
```

Webpack's Node plugin starts the TUI after it builds. Use the mouse or focus a
slice and press Enter. Press `q` to exit the TUI; Ctrl+C stops the watcher.
