(ns documentation.xt-db-walkthrough
  (:use code.test))

[[:hero {:title "xt.db walkthrough"
         :subtitle "Binding framework and substrate walkthrough."
         :lead "The database layer ties together schema text builders, backend systems, node adapters, and substrate transports into an application-facing data layer. This page walks the moving parts and points at the executable scenarios that exercise them."}]]

[[:chapter {:title "Overview" :link "overview"}]]

"xt.db is organised in three layers, each of which maps to a concrete namespace family:"

"- **schema and query text builders** (`xt.db.text`) — SQL views, calls, and schema definitions authored as Clojure data and emitted per backend."
"- **system backends** (`xt.db.system`) — a common implementation protocol with adapters for Supabase/Postgres and SQLite, exposed as named services on a node."
"- **node and page integration** (`xt.db.node`, `xt.substrate.page-core`) — kernels that install the services on a substrate node and page models that read through caching or primary services."

"On top of these, `docs/xt_db_binding_framework_readme.md` sketches a declarative *binding framework*: a plain-data **screen spec** (views, inputs, actions), a **binding instance** (`get-state`, `subscribe`, `commands`, `dispose!`) that any UI runtime can attach to, and thin **framework adapters** (React-like or Dart-like). The binding layer is a design document; the three layers beneath it are implemented and tested."

[[:chapter {:title "Example flow" :link "flow"}]]

"A typical flow starts with schema/query text builders, chooses a system backend, exposes a kernel through the node layer, and connects clients through memory, websocket, worker, or Supabase transports. The proof-of-concept tests exercise exactly this path:"

"1. **Bind the schema.** `postgres.core/bind-schema` and `bind-app` produce the schema and lookup used by every service, saved as book entries."

"2. **Initialise the kernel.** `xt.db.node.kernel-base/kernel-init-main` installs two services on a fresh substrate node — a `db/primary` (Supabase) and a `db/caching` (in-memory SQLite) — against the shared schema:"

[[:code {:lang "clojure"}
  "(kernel-init-main\n  node\n  {\"primary\" {\"type\" \"supabase\"\n              \"defaults\" supabase-anon-config}\n   \"caching\" {\"type\" \"sqlite\"\n              \"defaults\" {\"filename\" \":memory:\"}}}\n  -/Schema\n  -/SchemaLookup)\n;; => {\"status\" \"setup\"\n;;     \"data\" {\"caching\" {\"id\" \"db/caching\" \"type\" \"sqlite\" ...}\n;;             \"primary\" {\"id\" \"db/primary\" ...}\n;;             \"common\"  {\"id\" \"db/common\"}}}"]]

"3. **Attach a page model.** A model spec names a local `handler` (reads from `db/caching`) and a `pipeline.remote.handler` (pulls asynchronously from `db/primary`). `page-core/group-add-attach` creates the models; `page-model-update` refreshes output; `model-remote-call` forces the remote path."

"4. **Read current state.** `xt.event.base-model/get-current` returns the model's public snapshot — the same shape a UI binding would render from."

[[:callout {:tone :info
              :title "Execution requirements"
              :content "The poc scenarios run the `:basic` JavaScript runtime (Node.js) and a local Supabase/Postgres stack via `scaffold.supabase.local-min`. They are integration tests: run one namespace at a time and only when the stack is available."}]]

[[:chapter {:title "Executable scenarios" :link "examples"}]]

"Each scenario is a seed test under `test-lang/xt/db/poc/`. Run them individually, for example `lein test :only xt.db.poc.n00-basic-test`."

"| File | What it demonstrates |
| --- | --- |
| `n00_basic_test.clj` | kernel init with primary + caching services, page model attach, local vs remote reads |
| `n01_webworker_test.clj` | the same node stack hosted inside a WebWorker boundary |
| `n03_webworker_custom_test.clj` | custom worker wiring for the db node |
| `n04_adaptor_test.clj` | backend adaptor behaviour across system implementations |
| `s01_worker_min_test.clj` | minimal worker-hosted substrate client |
| `s02_shared_tree_test.clj` | shared tree state across workers |
| `s03_shared_rpc_test.clj` | RPC over a shared worker |
| `s04_sharedworker_test.clj` | SharedWorker transport between tabs |
| `s05_sharedworker_custom_test.clj` | custom SharedWorker endpoints |
| `s06_supabase_auth_test.clj` | authenticated Supabase access from the client |"

"Start with `n00` for the kernel and page-model flow, then pick the row matching the transport or backend boundary you are working on."
