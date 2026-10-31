# Optional xt.db backends

`xt.db.system.main` includes memory only. SQLite, Postgres, Supabase and Neon are explicit providers; importing the factory or generic node runtime does not import their native drivers. Provider selection is local to an application or node, with no shared registry or import-time registration.

## Direct factory calls

Require only the providers you need in your `l/script` namespace:

```clojure
[xt.db.system.main :as main]
[xt.db.system.backend-neon :as neon]

(main/create-impl "neon" defaults schema lookup
                  {"neon" (neon/provider)})
```

`create-impl(type, defaults, schema, lookup, backends)` accepts an optional provider map. Four-argument calls continue to work for memory. Other types now require the map: there is no automatic fallback to all installed backends. `main-client/create-client(type, defaults, backends)` uses the same map. A missing backend produces an error containing the requested type and selected names. An unsupported runtime or invalid provider also fails explicitly.

Provider namespaces are `backend-sqlite`, `backend-postgres`, `backend-supabase`, and `backend-neon`, under `xt.db.system`. A descriptor contains `create_client(defaults)` and `create_impl(client, schema, lookup)`. Each provider imports only its backend's runtime clients. Custom descriptors use the same interface. Memory is reserved as the built-in backend.

`create-impl-init(impl)` uses `impl-common/ISourceInit.init-db` when implemented, otherwise it resolves the implementation unchanged. SQLite and Postgres implement this protocol using their existing initialization routines; their direct initialization functions remain available.

## Node setup

```clojure
(runtime/init-server node
  {"backends" {"supabase" (supabase/provider)
               "sqlite" (sqlite/provider)}
   "init_handlers" [kernel-supabase/init-handlers]})
```

The providers and handler installers must be required explicitly in the application namespace. `kernel-base/set-backends(node, backends)` can also install the map directly. It is held under `node.state.db_backends` and is used for subsequent service construction. Selecting providers does not replace already-created services.

Keep ordinary setup configuration unchanged, for example `{"primary" {"type" "supabase" ...}, "caching" {"type" "sqlite" ...}}`. Factory functions never belong in this configuration, a site map, a JSON request, or a returned setup result. Remote setup selects among the providers already installed on its receiving node.

The generic runtime installs only base database and page-proxy handlers. Supply backend-specific server installers through `init_handlers`. On proxy nodes, pass `{"init_proxy_handlers" [proxy-supabase/init-proxy-handlers]}` to `init-server-proxy` or the runtime connection helper's final options argument. Neon uses its corresponding kernel/proxy installers.

React `ext-table` runtime contexts accept these local initialization options under `runtime.local`; they are separate from serializable `runtime.config`.

## Workers

Default generated workers support memory only. Build a worker with a local options **code form** as the emitter's second argument:

```clojure
(runtime/sharedworker-init-string overrides
  '{"backends" {"neon" (xt.db.system.backend-neon/provider)}
    "init_handlers" [xt.db.node.kernel-neon/init-handlers]})
```

Load those provider/kernel namespaces in the Clojure build namespace before emission. The supplied form is compiled into the worker bootstrap; it is not sent across its transport. WebWorker and Node worker emitters accept the same second argument. Runtime worker initialization functions accept the options map after node, transport ID and worker ID. Connection helpers accept local proxy options as their final argument, independently of the worker's server-side selection.

StatsTrade explicitly selects Supabase plus SQLite in its SharedWorker builder, and Supabase plus memory in its direct browser node. Applications using other worker bootstraps must make the same selection in their own bootstrap.

## Verification

`xt.db.system.backend-selection-test` checks node-local maps, factory failures, protocol initialization, handler selection, and emitted dependency isolation. The Neon tests exercise HTTP Auth/RPC and replay through an explicitly selected provider. Regenerate target tests from canonical seeds; no generated bench files should be edited manually.
