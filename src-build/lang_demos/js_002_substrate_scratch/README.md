# js-002 substrate scratch-v3

This `src-build/lang_demos` project turns `postgres.sample.scratch-v3` into a set of Lang DSL `xt.substrate` examples.

The generated browser page includes four slices:

- active currency catalogue;
- user profile by account;
- wallet and asset projection;
- the same currency model through the SQLite caching source.

`main.clj` owns the platform-neutral schema bindings, source topology, dataview descriptors, substrate connection, model attachment, page proxy call, and event envelope. `app.clj` only renders those contracts.

## Build

From the repository root:

```bash
lein test :only std.make.play-js-002-substrate-scratch-test
```

Or from a REPL:

```clojure
(require 'lang-demos.js-002-substrate-scratch.build)
(lang-demos.js-002-substrate-scratch.build/-main)
```

Artifacts are written to:

```text
.build/demo/js-002-substrate-scratch/public
```

Serve the generated page with:

```bash
cd .build/demo/js-002-substrate-scratch
make start
```

The live substrate functions exported by the generated module are `connect` and `attach-demo`. They use the generated `scratch_v3` schema and application lookup from `postgres.gen` through `pg/bind-schema` and `pg/bind-app`.
