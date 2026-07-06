(ns documentation.std-concurrent
  (:use code.test))

[[:hero {:title "std.concurrent"
         :subtitle "concurrency primitives, queues, executors, requests, relays, and threads"
         :lead "Start with the facade for common operations, then use the focused pages for lifecycle guidance, walkthroughs, and complete API references."}]]

[[:chapter {:title "Choose a primitive" :link "choose"}]]

[[:card-grid
  {:title "Concurrency libraries"
   :lead "Each focused namespace has its own walkthrough and API page."
   :items
   [{:meta "Batching" :title "std.concurrent.atom" :text "Atom-backed batch queues and completion-aware hubs." :href "std-concurrent-atom.html"}
    {:meta "Messaging" :title "std.concurrent.bus" :text "Addressed request and response loops between registered threads." :href "std-concurrent-bus.html"}
    {:meta "Execution" :title "std.concurrent.executor" :text "Thread pools, submission, scheduling, metrics, and shutdown." :href "std-concurrent-executor.html"}
    {:meta "Resources" :title "std.concurrent.pool" :text "Reusable resources with acquisition, release, cleanup, and health." :href "std-concurrent-pool.html"}
    {:meta "Output" :title "std.concurrent.print" :text "Serialized asynchronous printing from concurrent workers." :href "std-concurrent-print.html"}
    {:meta "Coordination" :title "std.concurrent.queue" :text "Blocking queues, deques, timeouts, draining, and bulk processing." :href "std-concurrent-queue.html"}
    {:meta "Streams" :title "std.concurrent.relay" :text "Interactive process and socket streams over a shared bus." :href "std-concurrent-relay.html"}
    {:meta "Requests" :title "std.concurrent.request" :text "Single, asynchronous, bulk, and transactional request orchestration." :href "std-concurrent-request.html"}
    {:meta "Applicatives" :title "std.concurrent.request-apply" :text "Invokable request definitions with transforms and execution modes." :href "std-concurrent-request-apply.html"}
    {:meta "Commands" :title "std.concurrent.request-command" :text "Reusable command templates for request clients." :href "std-concurrent-request-command.html"}
    {:meta "Threads" :title "std.concurrent.thread" :text "Thread construction, inspection, joining, and coordination." :href "std-concurrent-thread.html"}]}]]

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

"The layers are designed to compose. A blocking queue provides back-pressure, an executor runs anonymous tasks, a bus owns addressed handler loops, and a pool manages reusable resources. Request and relay abstractions build higher-level workflows on those primitives."

(comment
  (require '[std.concurrent :as concurrent])

  (def work (concurrent/queue:fixed 32))
  (def executor (concurrent/executor:pool 2 4 1000 work))

  @(concurrent/submit executor
                      (fn [] {:status :complete}))

  (concurrent/exec:shutdown executor))

[[:chapter {:title "Facade API" :link "api"}]]

"The facade re-exports the most commonly used thread, queue, executor, pool, bus, request, and relay operations. The focused pages document the full source namespaces."

[[:api {:namespace "std.concurrent"}]]
