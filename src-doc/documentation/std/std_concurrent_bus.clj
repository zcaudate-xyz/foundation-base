(ns documentation.std-concurrent-bus
  (:use code.test))

[[:hero {:title "std.concurrent.bus"
         :subtitle "request and response messaging between registered threads"
         :lead "A bus owns per-thread queues, dispatches messages to handlers, and completes futures when responses return."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"A bus combines thread registration, blocking queues, handler loops, and future-backed responses. Use it when work must be addressed to a specific long-running thread rather than submitted to an anonymous executor task."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Open a handler"}]]

"`bus:with-temp` starts a temporary bus and registers the current thread. `bus:open` then starts a named handler loop and returns its registration details."

(comment
  (require '[std.concurrent.bus :as bus])

  (bus/bus:with-temp message-bus
    (let [{:keys [id stopped]}
          @(bus/bus:open message-bus
                         (fn [{:keys [value]}]
                           {:value (inc value)}))]
      {:reply @(bus/bus:send message-bus id {:value 10})
       :info  (bus/info-bus message-bus)
       :stop  (bus/bus:close message-bus id)
       :stopped stopped})))

[[:section {:title "Inspect and control workers"}]]

"Use the registry functions to inspect IDs and queues. Prefer `bus:close` for cooperative shutdown; use `bus:kill` only when a handler thread must be interrupted."

(comment
  (bus/bus:all-ids message-bus)
  (bus/bus:get-count message-bus)
  (bus/bus:send-all message-bus {:op :refresh})
  (bus/bus:close-all message-bus))

[[:chapter {:title "Lifecycle" :link "lifecycle"}]]

"`bus` creates and starts a component. `bus:create` only constructs it. Component-aware code can use `start-bus`, `stop-bus`, `started?-bus`, and `info-bus` directly."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.bus"}]]
