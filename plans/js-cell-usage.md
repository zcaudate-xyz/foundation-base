# js.cell intended usage

## Purpose

`js.cell` is intended to be a small event-centered runtime for coordinating:

- local routes
- remote calls
- cache/store adapters
- higher-level model and view layers

The `v2` direction keeps the current `js.cell` intact while introducing a cleaner kernel that new systems can compose around.

## Architecture

```text
                  +----------------------+
                  |     application      |
                  |  models / views / UI |
                  +----------+-----------+
                             |
                             v
                  +----------------------+
                  |       js.cell        |
                  |   system instance    |
                  +----------+-----------+
                             |
        +--------------------+--------------------+
        |                    |                    |
        v                    v                    v
+---------------+   +----------------+   +----------------+
|   event bus   |   | route registry |   | system state   |
| topics        |   | local handlers |   | runtime data   |
| listeners     |   | dispatch       |   | shared context |
+-------+-------+   +--------+-------+   +----------------+
        |                    |
        |                    |
        v                    v
+---------------+   +----------------+
| store registry|   |remote registry |
| cache adapter |   | HTTP / RPC /   |
| sqlite / mem  |   | worker / custom|
+-------+-------+   +--------+-------+
        |                    |
        +---------+----------+
                  |
                  v
           +-------------+
           | adaptors    |
           | concrete    |
           | protocols   |
           +-------------+
```

## Core ideas

### 1. Event bus is the kernel

Everything interesting in `cell` should be expressible as an event:

- lifecycle events like `@/::INIT` and `@/::STATE`
- local work like `cell/::LOCAL`
- remote work like `cell/::REMOTE`
- store synchronization like `db/::SYNC`

The event bus should remain generic. It decides which listeners fire, but it does not own transport-specific behavior.

### 2. Registries hold capabilities

The system should register named capabilities rather than assume a single implementation:

- routes under a route id
- stores under a store key
- remotes under a remote key

That lets one `cell` instance support multiple stores and multiple remote protocols at the same time.

### 3. Adaptors own wire shapes

The core should not hardcode the payload format for HTTP, workers, or any external system.

Instead, adaptors should translate from the core registry APIs into concrete protocols:

- a worker adaptor can translate route calls into worker messages
- an HTTP adaptor can map remote calls into fetch requests
- a SQLite adaptor can map store sync events into database operations

## Cell protocol

The `cell` protocol should be transport-agnostic. The same protocol should work over:

- worker messages
- WebSocket connections
- HTTP request/response plus event side-channel
- in-memory test harnesses

The protocol should stay close enough to the current `js.cell` message shape that a worker adaptor is straightforward, while being structured enough to support long-running tasks and richer remote behavior.

### Protocol goals

1. Keep the frame small and regular
2. Support both request/response and unsolicited events
3. Let remote return values drive event emission
4. Support asynchronous progress and cancellation
5. Keep store synchronization as a first-class concern

### Base frame

Every protocol message should fit into a common envelope:

```clojure
{:op     "hello" | "call" | "result" | "emit" | "subscribe" | "unsubscribe" | "task"
 :id     "correlation-id"
 :ref    "optional-parent-id"
 :action "optional-call-target"
 :signal "optional-event-name"
 :status "pending" | "accepted" | "ok" | "error" | "cancelled" | "closed"
 :body   ...
 :meta   {...}}
```

### Meaning of the common fields

- `:op` identifies the protocol operation
- `:id` identifies the message or request
- `:ref` links follow-up messages back to an originating request or task
- `:action` identifies a local or remote call target
- `:signal` identifies an emitted event name
- `:status` captures lifecycle state
- `:body` carries the payload
- `:meta` carries transport-independent metadata

For compatibility with the existing `js.cell` worker model:

- `:action` corresponds to the older `:route` idea
- `:signal` corresponds to the older `:topic` idea

### Core operations

#### 1. `hello`

Used for capability negotiation.

```clojure
{:op "hello"
 :id "h1"
 :body {:version 1
        :capabilities {:routes true
                       :events true
                       :stores ["cache" "memory"]
                       :remotes ["http" "rpc"]}}}
```

#### 2. `call`

Used to invoke a route or remote capability.

```clojure
{:op "call"
 :id "c1"
 :action "remote/http"
 :body {:remote "http"
        :input {:url "/ping"
                :method "POST"}}}
```

#### 3. `result`

Used to answer a `call`.

```clojure
{:op "result"
 :id "c1"
 :status "ok"
 :body {:data ...}
 :meta {...}}
```

#### 4. `emit`

Used for events, including lifecycle, remote, store, and stream notifications.

```clojure
{:op "emit"
 :id "e1"
 :signal "cell/::REMOTE"
 :status "ok"
 :body {:request-id "c1"
        :data ...}
 :meta {...}}
```

#### 5. `subscribe` / `unsubscribe`

Used to receive future event messages.

```clojure
{:op "subscribe"
 :id "s1"
 :signal "cell/::REMOTE"}
```

```clojure
{:op "unsubscribe"
 :id "s1"
 :signal "cell/::REMOTE"}
```

#### 6. `task`

Used for long-running asynchronous work.

```clojure
{:op "task"
 :id "c1"
 :ref "task-1"
 :status "accepted"
 :body {:action "remote/http"}}
```

Later updates use the same `:ref`:

```clojure
{:op "task"
 :id "u1"
 :ref "task-1"
 :status "pending"
 :body {:progress 0.5}}
```

```clojure
{:op "task"
 :id "u2"
 :ref "task-1"
 :status "ok"
 :body {:data ...}}
```

### Event topics

The initial canonical topics are:

- `@/::INIT`
- `@/::STATE`
- `cell/::LOCAL`
- `cell/::REMOTE`
- `db/::SYNC`
- `db/::REMOVE`
- `db/::VIEW`

These cover the generic cases:

- lifecycle
- local route activity
- remote route activity
- store/cache synchronization

### Remote result envelope

To support the existing behavior where remote call results generate events, remote adaptors should return a normalized result envelope:

```clojure
{:status "ok" | "error" | "accepted"
 :body   data
 :meta   {...}
 :events [{:signal "cell/::REMOTE"
           :status "ok"
           :body data
           :meta {...}}]
 :store  {:sync   ...
          :remove ...}
 :task   {:id "task-1"
          :status "accepted"}}
```

### How remote calls produce events

`cell-v2/remote-call` should process the remote adaptor result in this order:

1. call the selected remote adaptor
2. emit a default `cell/::REMOTE` success or error event
3. emit any explicit events listed in `:events`
4. project `:store.sync` and `:store.remove` into store events or store adaptor calls
5. if `:task` is present, track follow-up async updates under that task id

This preserves the useful pattern from existing systems where the remote result drives the event stream, but keeps it generic.

### Asynchronous events

There are two kinds of async behavior:

#### 1. Deferred completion of a call

A call may return `:status "accepted"` with a task id, then later send `task` updates.

#### 2. Independent event sources

Some events are not direct responses to a call:

- stream messages
- worker notifications
- timers
- store watchers
- server-pushed updates

These should emit `{:op "emit" ...}` frames directly onto the bus. They are event sources, not call results.

### Database and store operations

Database work can be modeled in two layers:

- **generic store actions** for lightweight pluggable state adaptors
- **dedicated `db/...` actions** for `xt.db`-backed databases registered with the cell system

In other words, `store/...` remains the generic capability surface, while `db/...` is the canonical path for structured database operations and synchronization when the adaptor is backed by `xt.db`.

That means the protocol should separate:

- **commands and queries** sent to a store
- **events** emitted after the store has changed or produced query data

#### Store operations

At the protocol level, database-like operations should usually be expressed as `call` frames targeting a store capability:

```clojure
{:op "call"
 :id "db1"
 :action "store/query"
 :body {:store "cache"
        :input {:table "User"
                :select-method "default"
                :select-args ["u1"]}}}
```

or:

```clojure
{:op "call"
 :id "db2"
 :action "store/sync"
 :body {:store "cache"
        :input {:sync {"User" [{:id "u1" :name "Chris"}]}}}}
```

The main store-style operations are:

- `store/read`
- `store/write`
- `store/remove`
- `store/sync`
- `store/clear`
- `store/query` when richer database reads are needed

The dedicated `xt.db` operations are:

- `db/query`
- `db/sync`
- `db/remove`
- `db/delete`
- `db/clear`

#### Result versus event

The `result` of a store call tells the caller what happened directly:

```clojure
{:op "result"
 :id "db2"
 :status "ok"
 :body {:changed {"User" ["u1"]}}}
```

The event stream tells the rest of the system what to react to:

```clojure
{:op "emit"
 :id "e-db-1"
 :signal "db/::SYNC"
 :status "ok"
 :body {"User" ["u1"]}}
```

This distinction matters:

- the **call/result** path is for control flow
- the **event** path is for coordination, subscriptions, refreshes, and propagation

#### Database event meanings

The current canonical database topics should mean:

- `db/::SYNC` — records were added or updated in a store
- `db/::REMOVE` — records were removed from a store
- `db/::VIEW` — a store-backed view or query returned data

In practice:

- `store/query` may produce a `result` plus `db/::VIEW`
- `store/sync` may produce a `result` plus `db/::SYNC`
- `store/remove` may produce a `result` plus `db/::REMOVE`

#### How remote and database behavior connect

A remote adaptor can return normalized store instructions inside the remote result envelope:

```clojure
{:status "ok"
 :body data
 :store {:sync {"User" [{:id "u1"}]}
         :remove {"Session" ["s1"]}}}
```

Then `cell-v2/remote-call` can:

1. emit `cell/::REMOTE`
2. forward `:store.sync` and `:store.remove` into the selected store adaptor
3. emit `db/::SYNC` and `db/::REMOVE` as the observable side effects

That keeps the old useful behavior — remote returns driving DB-related events — while making the mechanism explicit and reusable.

### Current worker mapping

The existing `js.cell` worker protocol already has a useful minimal shape:

```clojure
{:op "route" | "eval" | "stream"
 :id ...
 :route ...
 :topic ...
 :status ...
 :body ...}
```

The worker adaptor should map that into the protocol as follows:

- worker `route` request -> protocol `call` with `:action`
- worker `route` response -> protocol `result`
- worker `stream` message -> protocol `emit` with `:signal`
- worker `eval` message -> protocol `call`/`result` in a debug annex only

That keeps the new protocol aligned with what already works.

### Recommended first implementation

Implement the protocol in layers:

1. base frame and envelope helpers
2. `call` / `result` / `emit`
3. remote result normalization
4. `subscribe` / `unsubscribe`
5. `task` lifecycle

This keeps the worker adaptor and HTTP adaptor simple when they are added.

## Intended usage

### Create a system

```clojure
(!.js
 (var system (cell-v2/make-system {})))
```

### Register local behavior

```clojure
(!.js
 (cell-v2/register-route
  system
  "local/echo"
  (fn [ctx arg]
    (return [(. ctx ["tag"]) arg]))
  {:kind "query"}))
```

### Register a store

```clojure
(!.js
 (cell-v2/register-store
  system
  "cache"
  {:read  (fn [input] (return ["cache" (. input ["id"])]))
   :write (fn [input] (return ["cache-write" (. input ["id"])]))
   :sync  (fn [input] (return (. input ["payload"])))
   :clear (fn [] (return "cache-cleared"))}))
```

### Register a remote

```clojure
(!.js
 (cell-v2/register-remote
  system
  "http"
  {:call (fn [input]
           (return {:url (. input ["url"])
                    :method (. input ["method"])}))}))
```

### Listen to events

```clojure
(!.js
 (cell-v2/add-event-listener
  system
  "remote-events"
  event/event-remote?
  (fn [input topic bus]
    (k/set-key (. bus ["owner"] ["state"]) "last-topic" topic))))
```

### Emit events

```clojure
(!.js
 (cell-v2/emit
  system
  (event/event event/EV_REMOTE
               {:request-id "r1"}
               nil)))
```

## Usage guidance

- Use the event bus for coordination.
- Use routes for local synchronous or logical commands.
- Use remotes for protocol-bound calls.
- Use stores for caching and synchronization concerns.
- Keep protocol-specific data shaping inside adaptors.

## Current implementation note

The current implementation now includes:

- `js.cell-v2.transport` for generic protocol frame send/receive, pending call tracking, task-ref tracking, subscription tracking, and event forwarding
- `js.cell-v2.transport-worker` for Worker-like `postMessage` / `message` channels
- `js.cell-v2.transport-socket` for websocket-like string/message channels
- `js.cell-v2.transport-http` for request/reply HTTP-style frame exchange
- `js.cell-v2.transport-legacy` for the legacy `js.cell` worker message format
- `js.cell-v2.control` for the `v1`-style worker control routes (`@/eval`, `@/final-set`, `@/route-list`, `@/ping`, `@/trigger`, and async variants)

## Near-term next step

The next meaningful piece is now to add the higher-level client/runtime layers on top of the validated transport and control kernel, for example:

- a `cell-v2` link/client layer analogous to `link_raw` / `link_fn`
- a container/runtime layer for model ownership, init lifecycle, and listener management
- optional task persistence or task inspection APIs for long-running remote workflows
