(ns documentation.std-concurrent-executor
  (:use code.test))

[[:hero {:title "std.concurrent.executor"
         :subtitle "thread pools, scheduling, submission, and executor lifecycle"
         :lead "Construct JVM executors with consistent queue handling, tracking, health information, and component-compatible lifecycle operations."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace wraps `ExecutorService` and `ScheduledThreadPoolExecutor` with constructors for single, pooled, cached, scheduled, and shared executors. Submission functions return foundation futures and support delay, minimum runtime, timeout, and default values."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Submit work to a pool"}]]

(comment
  (require '[std.concurrent.executor :as executor])

  (def pool
    (executor/executor
     {:type :pool
      :size 2
      :max 4
      :keep-alive 1000
      :queue {:size 32}}))

  @(executor/submit pool
                    (fn [] {:status :complete})
                    {:max 5000})

  (executor/executor:info pool)
  (executor/exec:shutdown pool))

[[:section {:title "Schedule repeated work"}]]

"Create a scheduled executor for delayed or repeated tasks. Fixed-rate scheduling targets a wall-clock cadence, while fixed-delay scheduling waits after each completion."

(comment
  (def scheduler (executor/executor:scheduled 1))

  (executor/schedule scheduler #(println :once) 250)
  (executor/schedule:fixed-rate scheduler #(println :tick) 1000)
  (executor/schedule:fixed-delay scheduler #(println :poll) 1000)

  (executor/exec:shutdown-now scheduler))

[[:section {:title "Share an executor"}]]

"Register a long-lived executor under an application-specific ID and retrieve it through the generic `executor` constructor. Always unshare it before final shutdown."

(comment
  (executor/executor:share :application/background pool)
  (executor/executor {:type :shared :id :application/background})
  (executor/executor:unshare :application/background))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.executor"}]]
