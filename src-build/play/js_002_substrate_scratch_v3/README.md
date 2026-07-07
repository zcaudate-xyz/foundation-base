# js-002 substrate scratch-v3

This `src-build/play` project turns `postgres.sample.scratch-v3` into a set of Hara DSL `xt.substrate` examples.

The generated browser page includes four slices:

- active currency catalogue;
- user profile by account;
- wallet and asset projection;
- the same currency model through the SQLite caching source.

`main.clj` owns the platform-neutral schema bindings, source topology, dataview descriptors, substrate connection, model attachment, page proxy call, and event envelope. `app.clj` only renders those contracts.

## Build

From the repository root:

```bash
lein test :only std.make.play-js-002-substrate-scratch-v3-test
```

Or from a REPL:

```clojure
(require 'play.js-002-substrate-scratch-v3.build)
(play.js-002-substrate-scratch-v3.build/-main)
```

Artifacts are written to:

```text
.build/play-js-002-substrate-scratch-v3/public
```

Serve the generated page with:

```bash
cd .build/play-js-002-substrate-scratch-v3
make start
```

The live substrate functions exported by the generated module are `connect` and `attach-demo`. They use the generated `scratch_v3` schema and application lookup from `postgres.gen` through `pg/bind-schema` and `pg/bind-app`.
