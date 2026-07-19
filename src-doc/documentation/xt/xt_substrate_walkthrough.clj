(ns documentation.xt-substrate-walkthrough
  (:use code.test))

[[:hero {:title "xt.substrate walkthrough"
         :subtitle "Memory, websocket, fanout, multiplex, and page examples."
         :lead "The substrate walkthroughs demonstrate how frames, spaces, transports, proxy utilities, and pages compose into running systems."}]]

[[:chapter {:title "Walkthrough sequence" :link "sequence"}]]

"The walkthrough is executable documentation. Read the scenarios in order and run only the namespace you are studying. The first six stages are self-contained; websocket and browser stages add external runtime requirements."

"```bash
lein test :only xt.substrate.s01-basic-test
lein test :only xt.substrate.walkthrough.s02-transport-memory-test
lein test :only xt.substrate.walkthrough.s03-transport-test
lein test :only xt.substrate.walkthrough.s04-fanout-test
lein test :only xt.substrate.walkthrough.s05-multiplex-test
lein test :only xt.substrate.walkthrough.s06-page-test
lein test :only xt.substrate.walkthrough.s07-wsserver-test
```"

"Some historical namespace names contain `walkthroug` in fact metadata, but the `:only` values above come from each file's actual `ns` declaration."

[[:callout {:tone :warning
              :title "Runtime requirements"
              :content "Stages `s01` through `s06` run the `:basic` JavaScript runtime and need Node.js on `PATH`. `s07` additionally needs a free local port and the websocket import. The `walkthrough_js` browser stages need Chromedriver and a browser. The excerpts below are trimmed for reading; the linked test files are the executable source of truth."}]]

[[:chapter {:title "Core sequence" :link "core-sequence"}]]

[[:section {:title "1. Local handlers and spaces"}]]

"`s01_basic_test.clj` starts without a transport. It shows the two equivalent ways to install a handler: declaratively in `node-create`, or imperatively with `register-handler`. A handler receives `(space, args, request, node)` and may return a plain value or promise."

"The counter example introduces spaces as named state containers. Three requests to `counter/inc` share the same `default::space` state and produce counts 1, 2, and 3. This is the smallest useful substrate program: an action name, arguments, and state scoped by a space id."

[[:section {:title "2. A memory request round-trip"}]]

"`s02_transport_memory_test.clj` creates a server, a client, and a `memory-pair`. Each endpoint is wrapped by `text-endpoint` and attached under the id of the remote peer. The client request is encoded, delivered to the server, handled there, returned over the reverse endpoint, and resolved on the client."

"```text
client -- request --> server
client <-- response -- server
```"

"The transport ids are local names, not node ids discovered from the wire. Keeping them aligned (`server` on the client and `client` on the server) makes explicit `transport_id` routing easy to understand. `link-pair` is the shorter helper when a test does not need to inspect the endpoints."

[[:section {:title "3. Frames and direct delivery"}]]

"`s03_transport_test.clj` opens the abstraction and constructs frames directly. A stream frame is fire-and-forget, so a one-way `send_fn` is enough. A request frame needs a return path because the server uses the inbound context's `transport_id` to select the response transport."

"```text
request(...) -> pending[id] -> send_fn -> receive-frame
  -> receive-request -> handler -> response(reply_to=id)
  -> return send_fn -> receive-response -> settle pending[id]
```"

"Calling `transport-send` delivers exactly the supplied frame and bypasses subscription selection. Use `publish` when the router should select recipients; use direct send when implementing protocol control or testing a transport boundary."

[[:section {:title "4. Subscription fanout"}]]

"`s04_fanout_test.clj` connects two clients to one server through a `memory-network`. Each client sends a subscribe control frame for `room/a` and `event/pinged`. The server records the inbound transport for each subscription, and one `publish` then routes the stream to both clients."

"```text
client-a -- subscribe(room/a, event/pinged) --+
                                               server -- publish --> client-a
client-b -- subscribe(room/a, event/pinged) --+                   +-> client-b
```"

"Subscriptions live on the node receiving the control frame. They are indexed by space, then signal, then transport id. Detaching a transport removes its connection and prunes its subscription entries."

[[:section {:title "5. Multiplexing requests"}]]

"`s05_multiplex_test.clj` builds a client, a mux node, and two backend servers. The client knows only the mux. The mux exposes `demo/proxy`, extracts a target id from the arguments, and issues a second request with explicit `transport_id` metadata."

"```text
                         +--> server-a
client --> demo/proxy --> mux
                         +--> server-b
```"

"This is application-level multiplexing rather than automatic request routing: the proxy handler decides the backend and returns its promise, so the original request resolves only after the nested request completes. The same scenario runs in JavaScript, Lua, and Python to demonstrate portable behavior."

[[:section {:title "6. Page groups and models"}]]

"`s06_page_test.clj` adds the reactive page layer. A group contains named models; a model has defaults, a handler, optional dependencies, and optional remote pipeline behavior. `group-add-attach` creates the models and initializes them, while `page-model-set-input` updates input and refreshes output."

"The examples progress through five behaviors: initial computation from default args, input updates, dependent model refresh, a handler that makes a remote substrate request, and distinct local versus remote handlers. A dependent model names its source ids in `deps`; refreshing the source causes the derived model to refresh after it."

"Keep the roles separate: substrate spaces scope the page runtime, groups organize related models, and models own reactive input/output. A model handler can remain local or call any service reachable through the node."

[[:section {:title "7. Websocket transport"}]]

"`s07_wsserver_test.clj` replaces the in-memory wire with a real websocket. The test starts an HTTP Kit server on port 29632, creates a native websocket client lazily, waits for the socket to open during `attach-transport`, and then performs the same request/response protocol used by memory transport."

"The application-facing code is intentionally unchanged: create a node, attach a transport as `server`, then call `request` with that `transport_id`. Only endpoint construction and runtime setup differ. If this test fails before an assertion, first check that port 29632 is free and that the JavaScript websocket import is available."

[[:chapter {:title "Browser and worker sequence" :link "browser-sequence"}]]

"The `walkthrough_js` scenarios require the Chromedriver runtime and a browser context. Several are marked `:seedgen/skip` because they exercise browser-specific APIs rather than portable code generation. `s31_proxy_test.clj` is skipped when `CI` is set."

"| File | Boundary exercised | Result to observe |
| --- | --- | --- |
| `s30_workers_test.clj` | Browser to WebWorker and SharedWorker | ready handshake followed by a remote echo |
| `s31_proxy_test.clj` | Browser to SharedWorker page server | list and open a remote group snapshot |
| `s32_proxy_full_test.clj` | Bidirectional page proxy | server output deltas update the client model and fire listeners |
| `s33_react_test.clj` | Local substrate page to `js.react.ext-page` | view helper reads initial and updated model output |
| `s34_react_test.clj` | Two browser tabs through one SharedWorker | an update from tab 2 becomes visible in tab 1 |
| `s35_playground_test.clj` | Full playground-served browser | evaluation and reactive model updates reach the page |
| `s36_playmin_test.clj` | Minimal playground | browser evaluation canary |"

[[:section {:title "Worker handshake"}]]

"In `s30`, the worker calls `boot-self` with its message target and a ready payload. The browser calls `connect-worker` or `connect-sharedworker`, waits for that ready payload, attaches the returned transport, makes a normal substrate request, and finally calls `disconnect`. Blob URLs are revoked after worker construction so they do not accumulate."

[[:section {:title "Page proxy synchronization"}]]

"The proxy scenarios install `page-proxy` on both nodes. The server owns the real page group. `group-open-proxy` requests a serialized snapshot and creates proxy models on the client. Later input operations are forwarded to the server; refreshed output is published back as a model delta, applied to the proxy model, and emitted through its normal listener interface."

"This yields one data path for both UI and remote state: React-facing code reads an event model, regardless of whether that model is local or backed by a SharedWorker. The two-tab scenario proves that the worker is the shared authority rather than either browser tab."

[[:chapter {:title "Selecting an example" :link "selecting"}]]

"Use `s01` when designing handlers or state, `s02` for ordinary client/server tests, `s03` when implementing a new transport, `s04` for event distribution, `s05` for gateways or backend selection, and `s06` for reactive application state. Move to `s07` only after the same flow passes over memory. Use `s30` through `s34` for browser architecture; `s35` and `s36` primarily validate playground integration."
