(ns documentation.std-concurrent-thread
  (:use code.test))

[[:hero {:title "std.concurrent.thread"
         :subtitle "thread construction, inspection, and coordination"
         :lead "Use a consistent Clojure interface for common `java.lang.Thread` operations and configurable thread creation."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace wraps current-thread access, IDs, joining, locks, stack traces, daemon state, uncaught handlers, context classloaders, and thread construction."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create and join a thread"}]]

(comment
  (require '[std.concurrent.thread :as thread])

  (def worker
    (thread/thread
     {:name "example-worker"
      :daemon true
      :handler (fn [] (println :running))
      :start true}))

  (thread/thread:join worker 1000)
  (thread/thread:alive? worker))

[[:section {:title "Inspect runtime threads"}]]

(comment
  (thread/thread:current)
  (thread/thread:id)
  (thread/thread:all-ids)
  (thread/thread:active-count)
  (thread/stacktrace worker))

[[:section {:title "Coordinate through a lock"}]]

(comment
  (def lock (Object.))
  (future (thread/thread:wait-on lock))
  (thread/thread:notify-all lock))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.thread"}]]
