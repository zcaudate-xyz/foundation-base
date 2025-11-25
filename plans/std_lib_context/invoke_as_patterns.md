# The `invoke-as` Pattern: Analysis & Use Cases

## The Pattern Definition

The `invoke-as` pattern (defined in `std.lib.apply`) is a structural design pattern that decouples **definition** from **execution**.

```clojure
(invoke-as applicative & args)
;; ↓
(let [runtime (resolve-runtime applicative)]
  (apply-in applicative runtime args))
```

It turns an object into an "executable" entity that knows *how* to run itself but needs to find *where* (the context/runtime) to run.

## Core Characteristics

1.  **Self-Resolution**: The object determines its own execution context (via `*runtime*` binding, metadata, or default logic).
2.  **Late Binding**: The specific implementation of the execution (the Runtime) is decided at the moment of invocation, not at definition time.
3.  **Uniform Interface**: Consumers just call `(obj arg1 arg2)` (via Clojure's `IFn` interface if `defimpl` is used with `:invoke`), treating complex, context-dependent operations as simple function calls.

## Potential Use Cases

### 1. "Smart" API Endpoints

Instead of writing functions that take a `client` or `token` argument, define API endpoints as Applicatives.

*   **Concept**: An `Endpoint` object holds the method, path, and serialization rules.
*   **Runtime**: The `Session` or `HttpClient` (holding the auth token and base URL).
*   **Execution**:
    ```clojure
    (def get-user (endpoint :get "/users/:id"))

    ;; Usage:
    (binding [*runtime* my-auth-session]
      (get-user 123))
    ;; => Fetches from https://api.site.com/users/123 using session's token
    ```

### 2. Database Query Objects (The "Repository" Pattern)

Decouple SQL/Datalog queries from the database connection.

*   **Concept**: A `Query` object holds the SQL string or data structure.
*   **Runtime**: The `Connection` or `Transaction`.
*   **Execution**:
    ```clojure
    (def find-active-users (sql-query "SELECT * FROM users WHERE active = true"))

    ;; Usage:
    (with-transaction [tx]
      (binding [*runtime* tx]
        (find-active-users)))
    ```
    *Benefit*: The same `find-active-users` object can be run against a Mock DB, a Read Replica, or the Primary DB just by changing the bound runtime.

### 3. Workflow & Task Orchestration

Define tasks as data, execute them on workers.

*   **Concept**: A `Task` object describes the job (type, payload).
*   **Runtime**: The `WorkerPool` or `JobQueue`.
*   **Execution**:
    ```clojure
    (def resize-image (task :image/resize {:width 100 :height 100}))

    ;; Usage:
    (binding [*runtime* (get-worker-pool :gpu-cluster)]
      (resize-image "s3://bucket/img.jpg"))
    ```

### 4. UI Signals & Effects

In a frontend architecture (like Re-frame or React), represent effects as executable objects.

*   **Concept**: A `Signal` object represents an intent (e.g., `Navigate`, `ShowNotification`).
*   **Runtime**: The `Dispatcher` or `Store`.
*   **Execution**:
    ```clojure
    (def go-home (navigate "/home"))

    ;; Usage:
    ;; Instead of (dispatch [:navigate "/home"]), you treat the intent as callable
    (invoke-as go-home)
    ```

### 5. Hardware/Device Abstraction

Control physical devices where the specific driver implementation varies.

*   **Concept**: An `Instruction` object (e.g., `MoveArm`, `ReadSensor`).
*   **Runtime**: The `DeviceDriver` (e.g., `SerialPortDriver`, `MockDriver`, `UsbDriver`).
*   **Execution**:
    ```clojure
    (def move-up (move-arm :up 10))

    (binding [*runtime* (connect-serial "/dev/ttyUSB0")]
      (move-up))
    ```

### 6. Feature Toggles & A/B Testing

Objects that execute differently based on user context.

*   **Concept**: A `Feature` object.
*   **Runtime**: The `UserContext` (containing flags/permissions).
*   **Execution**:
    ```clojure
    (def checkout-flow (feature :checkout-v2))

    ;; If the runtime (user) has the flag, it runs the new flow.
    ;; Otherwise, it might fallback to the old flow or no-op.
    (checkout-flow cart-data)
    ```

## Summary Table

| Pattern | Applicative Object | Runtime Context | `apply-in` Action |
| :--- | :--- | :--- | :--- |
| **Foundation** | `Pointer` | `Process` / `Interpreter` | Remote Function Call |
| **API** | `Endpoint` | `AuthSession` | HTTP Request |
| **Data** | `Query` | `DBConnection` | JDBC Execute |
| **Compute** | `Task` | `WorkerPool` | Queue Job |
| **UI** | `Signal` | `Dispatcher` | State Mutation |
| **IoT** | `Instruction` | `Driver` | Serial/IO Command |
