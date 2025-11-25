# Proposals for `std.lib.context`

Based on the analysis of `std.lib.context` and its protocols (`std.protocol.context`), here are several advanced use cases and architectural patterns that this library can support.

## 1. Multi-Tenant Sandbox Environments

### Concept
Use `Space` to represent a tenant isolation boundary. In a SaaS application, you might want to run user-defined scripts or queries (e.g., Lua, Python, SQL) without them interfering with each other.

### Implementation Sketch
- **Tenants as Spaces**: Create a `Space` for each tenant (e.g., `(space:create {:namespace (str "tenant-" tenant-id)})`).
- **Isolated Runtimes**: Configure each space with its own isolated runtimes.
    -   *Database*: Each space gets a JDBC runtime configured with a specific schema or user role limiting access to that tenant's data.
    -   *Scripting*: If users run scripts (e.g., Lua), each space starts a fresh Lua state or a sandboxed Python interpreter.
- **Resource Management**: The `space-stop` function becomes a "kill switch" for a tenant's entire session, releasing DB connections and killing processes instantly.

```clojure
(defn create-tenant-space [tenant-id]
  (let [sp (space/space-create {:namespace (str "tenant-" tenant-id)})]
    (space/space-context-set sp :db :default {:config {:schema (str "schema_" tenant-id)}})
    (space/space-context-set sp :lua :default {:config {:sandbox true}})
    sp))

(defn run-tenant-task [tenant-id task-fn]
  (let [sp (create-tenant-space tenant-id)]
    (try
      (space/space-rt-start sp :db)
      (space/space-rt-start sp :lua)
      ;; Bind *runtime* or similar dynamic var if needed, or pass sp explicitly
      (task-fn sp)
      (finally
        (space/space-stop sp)))))
```

## 2. Distributed Actor / Remote Object Proxy

### Concept
The `IPointer` and `IContext` protocols naturally support a proxy pattern. `std.lib.context` can be the backbone of a distributed object system where "local" objects are actually pointers to objects residing on remote nodes.

### Implementation Sketch
- **Remote Runtime**: Implement a `RemoteRuntime` (implementing `IContext`) that communicates via TCP/HTTP/WebSocket/MCP with a remote server.
- **Pointers as References**: A `Pointer` in this context contains a UUID reference to an object on the remote node.
- **Transparent Invocation**:
    -   `-invoke-ptr`: Serializes arguments, sends an RPC call to the remote node, and deserializes the result.
    -   `-deref-ptr`: Fetches the current state of the remote object.
-   **Usage**: The consumer code doesn't need to know it's calling a remote service; it just uses `(apply ptr args)` or `@ptr`.

```clojure
(defimpl RemoteRuntime [endpoint]
  :protocols [std.protocol.context/IContext
              :body {-invoke-ptr (network/rpc-call endpoint (:id ptr) args)
                     -deref-ptr  (network/rpc-get endpoint (:id ptr))
                     ;; ... other methods
                     }])
```

## 3. Simulation & "Time Travel" Debugging

### Concept
Since `Space` captures the state of all external interactions (DB, Time, external services), you can create deterministic simulation environments.

### Implementation Sketch
- **Mock Runtimes**: Implement "Recording" or "Replay" versions of standard runtimes.
    -   `TimeRuntime`: Instead of system clock, use a controllable clock that the simulation advances.
    -   `DbRuntime`: An in-memory, rollback-able version or a recording proxy.
- **Scenario Testing**:
    -   Spin up a `Space`.
    -   Inject `MockTimeRuntime` and `MockDbRuntime`.
    -   Run complex business logic.
    -   Assert on the state of the "World" (the Space).
- **Time Travel**:
    -   Because the `Space` encapsulates the "current runtime", you can swap the runtime out.
    -   Run a process, capture the state changes in the runtimes.
    -   Reset the Space to a previous state by re-initializing the runtimes with historical data.

## 4. Hot-Swappable Plugin Architecture

### Concept
Use the Registry and Space to manage application plugins that can be enabled, disabled, or reconfigured at runtime without restarting the JVM.

### Implementation Sketch
- **Plugins as Contexts**: Each plugin is registered as a Context type (e.g., `:plugin.payment-processor`).
- **Implementations as Runtimes**: Different providers (Stripe, PayPal, Mock) are different Runtimes for that Context.
- **Dynamic Switching**:
    -   The main application runs in a specific `Space`.
    -   Admin config changes trigger `(space-context-set sp :plugin.payment-processor :stripe ...)` followed by a restart of that runtime.
    -   The application code uses a generic Pointer to the "Payment Processor", which dynamically resolves to the currently active runtime in the Space.

## 5. "Context-Aware" Data Structures

### Concept
`Pointer`s can be used to create data structures that "know" where they live. This is useful for large datasets that reside in external systems (e.g., a DataFrame in Python, a large Graph in a specialized DB).

### Implementation Sketch
-   **Lazy Loading**: The Pointer represents the dataset. `deref` triggers a fetch.
-   **Operation Pushdown**:
    -   Instead of fetching data to Clojure to filter it, `(filter fn ptr)` could be intercepted.
    -   The `IContext` `-invoke-ptr` or a custom protocol could recognize functional operations and transpile them (using `std.lang`) to the target environment (e.g., generating a SQL query or a Pandas command) and return a *new* Pointer to the result, without moving data.

## 6. A "Meta-REPL"

### Concept
A REPL that doesn't just evaluate Clojure, but evaluates code in *any* active runtime within the current Space.

### Implementation Sketch
-   The REPL maintains a `*current-space*` and `*current-context*` (e.g., `:python`).
-   Input is passed to `-raw-eval` of the active runtime.
-   This unifies interacting with SQL, Python, Shell, and Clojure in a single session, managed by `std.lib.context`.

---

These proposals demonstrate that `std.lib.context` is a powerful structural pattern for managing **environmental side-effects**, **polyglot interoperability**, and **resource lifecycles** in a decoupled way.
