(ns documentation.std-dispatch
  (:require [std.dispatch :refer :all]
            [std.lib.component :as component])
  (:use code.test))

[[:hero {:title "std.dispatch"
         :subtitle "boards, hubs, queues, debounce, hooks, and event dispatch"
         :lead "`std.dispatch` is a higher-level standard library family in foundation-base. This page explains when to use it, how it fits internally, and where to find the API surface."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Use this layer when application or tooling code needs the behavior described by the page title without reaching directly into implementation namespaces. The top-level namespace is the starting point; subnamespaces expose more focused building blocks."

[[:chapter {:title "How to use it" :link "usage"}]]

"Require the top-level namespace for common workflows, then move to subnamespaces when you need a lower-level primitive. Existing tests under `test/std/dispatch` and `test/std/dispatch_test.clj` are the best executable examples for edge cases."

(fact "create and use a core dispatcher"
  (let [d (dispatch {:type :core
                     :options {:pool {:size 1}}
                     :handler (fn [_ _])})]
    (try
      (submit d :entry)
      => anything
      (finally
        (component/stop d)))))

(fact "create a dispatcher without starting it"
  (create {:type :core
           :options {:pool {:size 1}}
           :handler (fn [_ _])})
  => dispatch?)

[[:chapter {:title "Internal usage" :link "internal"}]]

"This library family is used across source, tests, generated examples, and docs tooling. During detailed documentation passes, collect concrete usage with `code.manage/find-usages` and `code.manage/locate-code`, then keep only high-signal examples in the page narrative."

[[:chapter {:title "API" :link "api"}]]

;; BEGIN merged documentation: guides/DISPATCH_STRATEGIES.md
;; sha256: a8519e461a4670d9521766e8a94fd5a8d947fb1844fbf35dc63fef913561747a
[[:chapter {:title "Dispatching Strategies in std.concurrent.dispatch" :link "merged-guides-dispatch-strategies-md"}]]

"The `std.concurrent.dispatch` library provides a flexible and powerful set of tools for managing concurrent operations in Clojure. It offers several distinct dispatching strategies, each designed to address different use cases. This document provides a summary of the available strategies and their intended applications."

[[:section {:title "1. CoreDispatch" :link "merged-guides-dispatch-strategies-md-1-coredispatch"}]]

"- **File:** `src/std/dispatch/core.clj`\n- **Purpose:** Provides a basic asynchronous dispatching mechanism.\n- **How it works:** `CoreDispatch` takes individual tasks, wraps them in a handler, and submits them to a thread pool for immediate execution. It serves as the foundational building block for other, more specialized dispatchers.\n- **Use cases:** Ideal for simple, off-the-shelf asynchronous execution where tasks do not require batching or filtering."

[[:subsection {:title "Code Example" :link "merged-guides-dispatch-strategies-md-code-example"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n;; Create a handler function that processes a single task\n(defn handle-task [dispatcher task]\n  (println \"Processing task:\" task)\n  (.countDown (:latch (meta dispatcher))))\n\n;; Create a CoreDispatch\n(def core-dispatcher\n  (dispatch/dispatch {:type :core\n                      :handler handle-task}))\n\n;; Use a latch to wait for the task to complete in the example\n(alter-meta! core-dispatcher assoc :latch (CountDownLatch. 1))\n\n;; Submit a task for asynchronous execution\n(dispatch/submit core-dispatcher {:id 1 :data \"some data\"})\n\n;; Wait for the task to finish\n(.await (:latch (meta core-dispatcher)))\n\n;; Stop the dispatcher\n(dispatch/stop core-dispatcher)"]]

[[:subsection {:title "Real-world Examples" :link "merged-guides-dispatch-strategies-md-real-world-examples"}]]

"- **Location:** Primarily in test files, such as `test/std/dispatch/core_test.clj`.\n- **Usage:** The `CoreDispatch` is not widely used in the main application codebase. This suggests that for most real-world scenarios, the more specialized dispatchers are preferred."

[[:section {:title "2. QueueDispatch" :link "merged-guides-dispatch-strategies-md-2-queuedispatch"}]]

"- **File:** `src/std/dispatch/queue.clj`\n- **Purpose:** Collects tasks in a queue and processes them in batches.\n- **How it works:** Instead of executing tasks as they arrive, `QueueDispatch` accumulates them in a queue. The queue is then processed at regular intervals or when the batch size reaches a specified limit. This approach is optimized for high-throughput scenarios.\n- **Use cases:** Efficiently handling a large volume of events, such as logging or analytics, where individual execution latency is less critical than overall system performance."

[[:subsection {:title "Code Example" :link "merged-guides-dispatch-strategies-md-code-example-2"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n;; Create a handler function that processes a batch of tasks\n(defn handle-log-batch [dispatcher logs]\n  (println \"Processing log batch of size:\" (count logs))\n  (doseq [log logs]\n    (println \"  -\" log))\n  (.countDown (:latch (meta dispatcher))))\n\n;; Create a QueueDispatch that processes batches of up to 10 logs\n;; or every 500ms\n(def queue-dispatcher\n  (dispatch/dispatch {:type :queue\n                      :handler handle-log-batch\n                      :options {:queue {:max-batch 10 :interval 500}}}))\n\n;; Use a latch to wait for the batch to be processed\n(alter-meta! queue-dispatcher assoc :latch (CountDownLatch. 1))\n\n;; Submit multiple log entries\n(doseq [i (range 5)]\n  (dispatch/submit queue-dispatcher {:level :info :message (str \"Log message \" i)}))\n\n;; Wait for the batch to be processed\n(.await (:latch (meta queue-dispatcher)))\n\n;; Stop the dispatcher\n(dispatch/stop queue-dispatcher)"]]

[[:subsection {:title "Real-world Examples" :link "merged-guides-dispatch-strategies-md-real-world-examples-2"}]]

"- **Location:** `src/std/lang/base/library.clj`\n- **Usage:** In a (currently commented-out) section of the code, a `QueueDispatch` is configured to handle the bulk addition of entries to a language library. This is a perfect use case for batch processing, as it allows many small updates to be grouped into a single, more efficient transaction."

[[:section {:title "3. DebounceDispatch" :link "merged-guides-dispatch-strategies-md-3-debouncedispatch"}]]

"- **File:** `src/std/dispatch/debounce.clj`\n- **Purpose:** Filters and manages high-frequency events to control the rate of execution.\n- **Use cases:** Managing user interface events, rate-limiting API requests, or any scenario where you need to prevent excessive or redundant task execution."

[[:subsection {:title "Strategy: :eager" :link "merged-guides-dispatch-strategies-md-strategy-eager"}]]

"Executes the first event in a series and ignores all subsequent events for a specified interval. Useful for preventing multiple triggers when only the initial action is desired (e.g., clicking a button multiple times)."

[[:subsubsection {:title "Code Example (:eager)" :link "merged-guides-dispatch-strategies-md-code-example-eager"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n(defn handle-click [dispatcher event]\n  (println \"Handling click event:\" (:id event))\n  (.countDown (:latch (meta dispatcher))))\n\n(def eager-dispatcher\n  (dispatch/dispatch {:type :debounce\n                      :handler handle-click\n                      :options {:debounce {:strategy :eager :interval 500}}}))\n\n(alter-meta! eager-dispatcher assoc :latch (CountDownLatch. 1))\n\n;; Simulate multiple rapid clicks\n(dispatch/submit eager-dispatcher {:id 1}) ;; This one will be processed\n(dispatch/submit eager-dispatcher {:id 2}) ;; This one will be skipped\n(dispatch/submit eager-dispatcher {:id 3}) ;; This one will be skipped\n\n(.await (:latch (meta eager-dispatcher)))\n(dispatch/stop eager-dispatcher)"]]

[[:subsection {:title "Strategy: :delay" :link "merged-guides-dispatch-strategies-md-strategy-delay"}]]

"Waits for a period of inactivity before executing the most recent event. Ideal for handling user input, such as typing in a search bar, where only the final value is relevant."

[[:subsubsection {:title "Code Example (:delay)" :link "merged-guides-dispatch-strategies-md-code-example-delay"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n(defn handle-search-query [dispatcher query]\n  (println \"Executing search for:\" (:term query))\n  (.countDown (:latch (meta dispatcher))))\n\n(def delay-dispatcher\n  (dispatch/dispatch {:type :debounce\n                      :handler handle-search-query\n                      :options {:debounce {:strategy :delay :interval 300}}}))\n\n(alter-meta! delay-dispatcher assoc :latch (CountDownLatch. 1))\n\n;; Simulate rapid user typing\n(dispatch/submit delay-dispatcher {:term \"clo\"})\n(Thread/sleep 100)\n(dispatch/submit delay-dispatcher {:term \"cloju\"})\n(Thread/sleep 100)\n(dispatch/submit delay-dispatcher {:term \"clojure\"}) ;; Only this one will be processed\n\n(.await (:latch (meta delay-dispatcher)))\n(dispatch/stop delay-dispatcher)"]]

[[:subsection {:title "Strategy: :notify" :link "merged-guides-dispatch-strategies-md-strategy-notify"}]]

"Executes the first event immediately and then enforces a cool-down period before the next event can be processed. Useful for rate-limiting operations."

[[:subsubsection {:title "Code Example (:notify)" :link "merged-guides-dispatch-strategies-md-code-example-notify"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n(defn handle-api-call [dispatcher call]\n  (println \"Making API call:\" (:endpoint call))\n  (.countDown (:latch (meta dispatcher))))\n\n(def notify-dispatcher\n  (dispatch/dispatch {:type :debounce\n                      :handler handle-api-call\n                      :options {:debounce {:strategy :notify :interval 500}}}))\n\n(alter-meta! notify-dispatcher assoc :latch (CountDownLatch. 2))\n\n;; Simulate bursts of API calls\n(dispatch/submit notify-dispatcher {:endpoint \"/users\"}) ;; Processed immediately\n(dispatch/submit notify-dispatcher {:endpoint \"/products\"}) ;; Skipped (within 500ms)\n(Thread/sleep 600)\n(dispatch/submit notify-dispatcher {:endpoint \"/orders\"}) ;; Processed after cooldown\n\n(.await (:latch (meta notify-dispatcher)))\n(dispatch/stop notify-dispatcher)"]]

[[:subsection {:title "Real-world Examples" :link "merged-guides-dispatch-strategies-md-real-world-examples-3"}]]

"- **Location:** `src/std/dispatch/hub.clj`\n- **Usage:** The `HubDispatch` uses a `DebounceDispatch` with the `:notify` strategy internally. This is a clever example of composition, where the debouncer ensures that the hub's handler is not called too frequently, even when receiving a high volume of events."

[[:section {:title "4. HubDispatch" :link "merged-guides-dispatch-strategies-md-4-hubdispatch"}]]

"- **File:** `src/std/dispatch/hub.clj`\n- **Purpose:** Acts as a centralized dispatcher for routing events to different groups.\n- **How it works:** `HubDispatch` uses a grouping function to categorize incoming events. It then uses a debouncer to trigger the processing of these groups, and all events within a group are processed in batches.\n- **Use cases:** Managing events from multiple sources that need to be handled in a coordinated manner, such as real-time notifications or data synchronization from different clients."

[[:subsection {:title "Code Example" :link "merged-guides-dispatch-strategies-md-code-example-3"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n;; Handler for processing batches of notifications\n(defn handle-notification-batch [dispatcher notifications]\n  (let [user-id (-> notifications first :user-id)]\n    (println (format \"Sending %d notifications to user %s\" (count notifications) user-id)))\n  (.countDown (:latch (meta dispatcher))))\n\n;; Group notifications by user-id\n(defn group-by-user [dispatcher notification]\n  (:user-id notification))\n\n;; Create a HubDispatch\n(def hub-dispatcher\n  (dispatch/dispatch {:type :hub\n                      :handler handle-notification-batch\n                      :options {:hub {:group-fn group-by-user\n                                      :max-batch 5\n                                      :interval 200}}}))\n\n(alter-meta! hub-dispatcher assoc :latch (CountDownLatch. 2))\n\n;; Submit notifications for different users\n(dispatch/submit hub-dispatcher {:user-id \"user-a\" :message \"Message 1 for A\"})\n(dispatch/submit hub-dispatcher {:user-id \"user-b\" :message \"Message 1 for B\"})\n(dispatch/submit hub-dispatcher {:user-id \"user-a\" :message \"Message 2 for A\"})\n(dispatch/submit hub-dispatcher {:user-id \"user-b\" :message \"Message 2 for B\"})\n\n(.await (:latch (meta hub-dispatcher)))\n\n(dispatch/stop hub-dispatcher)"]]

[[:subsection {:title "Real-world Examples" :link "merged-guides-dispatch-strategies-md-real-world-examples-4"}]]

"- **Location:** Primarily in test files, such as `test/std/dispatch/hub_test.clj`.\n- **Usage:** The `HubDispatch` is a highly specialized dispatcher for complex scenarios where events need to be grouped and processed in batches."

[[:section {:title "5. BoardDispatch" :link "merged-guides-dispatch-strategies-md-5-boarddispatch"}]]

"- **File:** `src/std/dispatch/board.clj`\n- **Purpose:** Manages task dependencies and resource locking to ensure that tasks requiring access to the same shared resource are processed sequentially.\n- **How it works:** When a task is submitted, a `groups-fn` determines which resource groups it belongs to. The dispatcher ensures that only one task can be active for a given group at any time. A task will only run when *all* of its required groups are free. Tasks for different, independent groups can run in parallel.\n- **Use cases:** Resource locking (e.g., preventing concurrent writes to the same file), and ensuring sequential processing for operations related to a specific key (e.g., all updates for a single user account must happen in order)."

[[:subsection {:title "Code Example" :link "merged-guides-dispatch-strategies-md-code-example-4"}]]

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n;; Handler for processing a task that uses resources\n(defn handle-resource-task [dispatcher task]\n  (println (format \"Starting task %s, using resources %s\" (:id task) (:resources task)))\n  (Thread/sleep 200) ;; Simulate work\n  (println (format \"Finished task %s\" (:id task)))\n  (.countDown (:latch (meta dispatcher))))\n\n;; Group tasks by the resources they use\n(defn group-by-resource [dispatcher task]\n  (:resources task))\n\n;; Create a BoardDispatch with a thread pool to see parallelism\n(def board-dispatcher\n  (dispatch/dispatch {:type :board\n                      :handler handle-resource-task\n                      :options {:board {:group-fn group-by-resource}\n                                :pool {:size 2}}}))\n\n(alter-meta! board-dispatcher assoc :latch (CountDownLatch. 4))\n\n;; Task 1 and 2 share resource :a, so they must run sequentially.\n;; Task 3 uses :c, so it can run in parallel with Task 1 or 2.\n;; Task 4 uses :a and :d, so it must wait for both to be free.\n(dispatch/submit board-dispatcher {:id 1 :resources [:a :b]})\n(dispatch/submit board-dispatcher {:id 2 :resources [:a]})\n(dispatch/submit board-dispatcher {:id 3 :resources [:c]})\n(dispatch/submit board-dispatcher {:id 4 :resources [:a :d]})\n\n(.await (:latch (meta board-dispatcher)))\n\n(dispatch/stop board-dispatcher)"]]

[[:subsection {:title "Real-world Examples" :link "merged-guides-dispatch-strategies-md-real-world-examples-5"}]]

"- **Location:** Primarily in test files, such as `test/std/dispatch/board_test.clj`.\n- **Usage:** `BoardDispatch` is a powerful and highly specialized dispatcher for managing complex dependencies. Its usage is best illustrated by its test cases."

"---"

[[:section {:title "Dispatcher Hooks" :link "merged-guides-dispatch-strategies-md-dispatcher-hooks"}]]

"The dispatch library provides a comprehensive set of hooks to monitor the entire lifecycle of a task. These can be incredibly useful for logging, metrics, or debugging. You can provide them in the dispatcher's configuration map under the `:hooks` key."

"- **`:on-startup`**: Called when the dispatcher is started.\n- **`:on-shutdown`**: Called when the dispatcher is stopped.\n- **`:on-submit`**: Called for every entry that is submitted to the dispatcher.\n- **`:on-queued`**: Called for an entry that has been successfully accepted and queued for processing.\n- **`:on-skip`**: Called for an entry that is deliberately ignored by the dispatcher's strategy (e.g., in a debouncer).\n- **`:on-poll`**: Called when a dispatcher polls for more work (e.g., in `BoardDispatch`).\n- **`:on-batch`**: Called when a batch of entries is created for processing (e.g., in `QueueDispatch`).\n- **`:on-process-bulk`**: Called before a batch of entries is processed.\n- **`:on-complete-bulk`**: Called after a batch of entries has been processed.\n- **`:on-error`**: Called when an error occurs during the processing of an entry."

[[:subsection {:title "Hook Example (Monitoring :on-skip)" :link "merged-guides-dispatch-strategies-md-hook-example-monitoring-on-skip"}]]

"This example uses a `DebounceDispatch` and attaches hooks to see which entries are processed and which are skipped."

[[:code {:lang "clojure"} "(require '[std.dispatch :as dispatch])\n(import '[java.util.concurrent CountDownLatch])\n\n(def processed-items (atom []))\n(def skipped-items (atom []))\n\n(def eager-dispatcher-with-hooks\n  (dispatch/dispatch\n   {:type :debounce\n    :handler (fn [_ event]\n               (swap! processed-items conj event)\n               (println \"Processed:\" event))\n    :hooks {:on-skip (fn [_ event]\n                       (swap! skipped-items conj event)\n                       (println \"Skipped:\" event))}\n    :options {:debounce {:strategy :eager, :interval 300}}}))\n\n\n(dispatch/submit eager-dispatcher-with-hooks {:id 1})\n(dispatch/submit eager-dispatcher-with-hooks {:id 2})\n(dispatch/submit eager-dispatcher-with-hooks {:id 3})\n\n(Thread/sleep 500)\n\n(println \"\\n--- Results ---\")\n(println \"Processed:\" @processed-items)\n(println \"Skipped:\" @skipped-items)\n\n(dispatch/stop eager-dispatcher-with-hooks)"]]

"---"

[[:section {:title "Combining Dispatchers and Executors" :link "merged-guides-dispatch-strategies-md-combining-dispatchers-and-executors"}]]

"The behavior of a dispatcher is heavily influenced by the type of executor it uses for its underlying thread pool. The `:options` map in the dispatcher configuration allows you to specify the executor's properties (e.g., `:pool {:size 5}`). Here's a breakdown of how the different combinations work:"

"| Dispatcher | Executor | Behavior | Use Case |\n| :--- | :--- | :--- | :--- |\n| **Core** | **:single** | All tasks are executed sequentially in a single thread. | Simple background processing where order is important. |\n| **Core** | **:pool** | Tasks are executed concurrently by a fixed number of threads. | General-purpose asynchronous processing. |\n| **Core** | **:cached** | Tasks are executed concurrently, with the thread pool growing and shrinking as needed. | Handling a variable number of tasks with high performance. |\n| **Queue** | **:single** | Batches are processed sequentially in a single thread. | High-throughput logging where processing order is important. |\n| **Queue** | **:pool** | Batches are processed concurrently by a fixed number of threads. | High-throughput scenarios where batches can be processed in parallel. |\n| **Debounce** | **:single** | The debounced task is executed in a single thread. | UI event handling where the final event should be processed in the background. |\n| **Debounce** | **:pool** | The debounced task is executed by one of the threads in the pool. | Rate-limiting API calls where the calls themselves can be made concurrently. |\n| **Hub** | **:single** | All grouped batches are processed sequentially. | Not a typical combination, as it defeats the purpose of parallelizing by group. |\n| **Hub** | **:pool** | Batches for different groups are processed concurrently. | The standard use case for HubDispatch, allowing for parallel processing of different event groups. |\n| **Board** | **:single** | Tasks for different, unlocked groups will still be processed sequentially. | Guarantees strict ordering, but at the cost of parallelism. |\n| **Board** | **<:pool** | Tasks for different, unlocked groups can be processed concurrently. | The standard use case for BoardDispatch, enabling parallel execution while ensuring resource safety. |"

"**Note on `:scheduled` executors:** The `:scheduled` executor type is not typically used directly with these dispatchers. Instead, scheduling logic is handled at a higher level, for example, by using a separate process to submit tasks to a dispatcher at regular intervals."
;; END merged documentation: guides/DISPATCH_STRATEGIES.md

;; BEGIN merged documentation: plans/slop/summary/std_dispatch_summary.md
;; sha256: 951bdda110493364240adfc0fd469c190dada41c62b4c635cdbd74286b0c3674
[[:chapter {:title "std.dispatch: A Comprehensive Summary (including submodules)" :link "merged-plans-slop-summary-std-dispatch-summary-md"}]]

"The `std.dispatch` module provides a flexible and extensible framework for asynchronous task dispatching and execution in Clojure applications. It offers various dispatch strategies (core, queue, debounce, hub, board) to handle different concurrency patterns, from simple task submission to complex, dependency-aware processing. The module is built on top of `std.concurrent` and `std.lib.component`, ensuring robust lifecycle management and integration with other parts of the `foundation-base` ecosystem."

[[:section {:title "std.dispatch (Main Namespace)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-main-namespace"}]]

"This namespace serves as the primary entry point for the dispatch system, aggregating and re-exporting key functionalities from its submodules. It defines the core `IDispatch` protocol and provides generic functions for creating and submitting tasks to dispatchers."

"**Core Concepts:**"

"*   **`IDispatch` Protocol:** The central abstraction for dispatchers, defining methods like `-submit` for task submission and `-bulk?` to indicate if the dispatcher handles bulk operations.\n*   **`IComponent` Protocol:** Dispatchers are components, allowing them to be started, stopped, and managed within a larger system.\n*   **Dispatch Strategies:** The module supports various dispatch types (e.g., `:core`, `:queue`, `:debounce`, `:hub`, `:board`), each optimized for different use cases."

"**Key Functions:**"

"*   **`dispatch?`**: Checks if an object is a dispatcher.\n*   **`submit`**: Submits an entry to a dispatcher. It handles serial execution if specified.\n*   **`create`**: Creates a component-compatible dispatcher based on a type and options.\n*   **`dispatch`**: Creates and starts a dispatcher."

[[:section {:title "std.dispatch.common (Common Dispatcher Utilities)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-common-common-dispatcher-utilities"}]]

"This sub-namespace provides shared helper functions and protocols for all dispatcher implementations, ensuring consistency and reducing code duplication."

"**Key Functions:**"

"*   **`to-string`**: Generates a string representation for dispatchers.\n*   **`info-base`**: Returns basic information about a dispatcher.\n*   **`create-map`**: Creates a base map for dispatcher configuration, including pool options and runtime counters.\n*   **`handle-fn`**: A generic function for handling dispatched entries, incorporating hooks for processing, error handling, and completion.\n*   **`await-termination`**: Waits for an executor to terminate.\n*   **`start-dispatch`, `stop-dispatch`, `kill-dispatch`**: Generic lifecycle functions for dispatchers, managing the underlying `ExecutorService`.\n*   **`started?-dispatch`, `stopped?-dispatch`**: Checks the running status of a dispatcher.\n*   **`info-dispatch`, `health-dispatch`, `remote?-dispatch`, `props-dispatch`**: Generic introspection functions for dispatchers.\n*   **`check-hooks`**: Validates that custom hooks conform to expected argument lists."

[[:section {:title "std.dispatch.hooks (Dispatcher Hooks and Counters)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-hooks-dispatcher-hooks-and-counters"}]]

"This sub-namespace defines a system of hooks and counters that allow for fine-grained monitoring and customization of dispatcher behavior at various stages of task processing."

"**Key Functions:**"

"*   **`counter`**: Creates an atom-based counter map for tracking dispatcher statistics (submit, queued, process, complete, error, etc.).\n*   **`inc-counter`, `update-counter`**: Functions for incrementing and updating dispatcher counters.\n*   **`handle-entry`**: Invokes a registered hook function.\n*   **`on-submit`, `on-queued`, `on-batch`, `on-process`, `on-process-bulk`, `on-skip`, `on-poll`, `on-error`, `on-complete`, `on-complete-bulk`, `on-shutdown`, `on-startup`**: Specific hook functions that are called at different stages of task processing."

[[:section {:title "std.dispatch.core (Core Dispatcher)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-core-core-dispatcher"}]]

"This sub-namespace implements a basic dispatcher that submits tasks directly to an underlying `ExecutorService` for immediate execution. It's suitable for simple, fire-and-forget task submission."

"**Key Functions:**"

"*   **`submit-dispatch`**: Submits an entry to the core dispatcher, executing it on the executor.\n*   **`create-dispatch`**: Creates a `CoreDispatch` instance."

[[:section {:title "std.dispatch.queue (Queue Dispatcher)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-queue-queue-dispatcher"}]]

"This sub-namespace implements a dispatcher that queues tasks and processes them in batches using an underlying `hub` (from `std.concurrent.atom`). It's ideal for scenarios where tasks need to be processed efficiently in groups."

"**Key Functions:**"

"*   **`start-dispatch`**: Starts the queue dispatcher.\n*   **`handler-fn`**: Creates a handler function that processes batches of entries from the queue.\n*   **`submit-dispatch`**: Submits an entry to the queue dispatcher, adding it to the hub and triggering batch processing.\n*   **`create-dispatch`**: Creates a `QueueDispatch` instance."

[[:section {:title "std.dispatch.debounce (Debounce Dispatcher)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-debounce-debounce-dispatcher"}]]

"This sub-namespace implements a dispatcher that debounces task submissions, preventing a flood of rapid, repetitive tasks. It supports different debounce strategies: eager, delay, and notify."

"**Core Concepts:**"

"*   **Debounce Strategies:**\n    *   `:eager`: Executes the first task immediately, then ignores subsequent tasks for a specified interval.\n    *   `:delay`: Waits for a specified interval after the last task submission before executing the task.\n    *   `:notify`: Executes the first task immediately, and subsequent tasks within the interval update the task to be executed after the interval.\n*   **`wrap-min-time`**: Ensures a handler runs for a minimum duration."

"**Key Functions:**"

"*   **`submit-eager`**: Implements the eager debounce strategy.\n*   **`submit-delay`**: Implements the delay debounce strategy.\n*   **`submit-notify`**: Implements the notify debounce strategy.\n*   **`submit-dispatch`**: Dispatches entries based on the configured debounce strategy.\n*   **`start-dispatch`, `stop-dispatch`**: Lifecycle functions for the debounce dispatcher.\n*   **`create-dispatch`**: Creates a `DebounceDispatch` instance."

[[:section {:title "std.dispatch.hub (Hub Dispatcher)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-hub-hub-dispatcher"}]]

"This sub-namespace implements a dispatcher that uses a \"hub\" (from `std.concurrent.atom`) to group and process tasks, often in conjunction with a debounce mechanism. It's designed for scenarios where tasks need to be collected and processed in batches, potentially with dependencies or shared state."

"**Key Functions:**"

"*   **`process-hub`**: Processes a group of entries from a hub.\n*   **`put-hub`**: Adds an entry to a group's hub.\n*   **`create-hub-handler`**: Creates a handler for the hub, which processes entries from the hub.\n*   **`update-debounce-handler!`**: Updates the handler of the underlying debounce dispatcher.\n*   **`create-debounce`**: Creates the debounce dispatcher used by the hub.\n*   **`start-dispatch`, `stop-dispatch`, `kill-dispatch`**: Lifecycle functions for the hub dispatcher.\n*   **`submit-dispatch`**: Submits an entry to the hub dispatcher, adding it to the appropriate group's hub and triggering the debounce mechanism.\n*   **`info-dispatch`, `started?-dispatch`, `stopped?-dispatch`, `health-dispatch`, `props-dispatch`**: Introspection functions for the hub dispatcher.\n*   **`create-dispatch`**: Creates a `HubDispatch` instance."

[[:section {:title "std.dispatch.board (Board Dispatcher)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-board-board-dispatcher"}]]

"This sub-namespace implements a dispatcher that manages tasks on a \"board,\" allowing for dependency tracking and ordered execution of tasks based on groups. It's suitable for complex workflows where tasks have interdependencies and need to be processed in a specific order."

"**Core Concepts:**"

"*   **Board:** An atom holding the state of submitted tasks, including their return values, lookup maps, queues, busy status, and dependencies.\n*   **Tickets:** Unique identifiers for submitted tasks.\n*   **Groups:** Tasks can belong to one or more groups, which are used for dependency tracking."

"**Key Functions:**"

"*   **`get-ticket`**: Generates a unique ticket for a task.\n*   **`new-board`**: Creates a new board state.\n*   **`submit-ticket`**: Adds a task ticket to the board, associating it with its groups.\n*   **`submit-board`**: Submits an entry to the board, assigning a ticket and registering its groups.\n*   **`clear-board`**: Clears a task from the board after it's processed.\n*   **`add-dependents`**: Adds dependents to a group.\n*   **`poll-board`**: Polls the board for tasks that are ready to be executed (i.e., their dependencies are met).\n*   **`poll-dispatch`**: Polls the dispatcher for more work, submitting ready tasks to the executor.\n*   **`submit-dispatch`**: Submits an entry to the board dispatcher.\n*   **`start-dispatch`, `stop-dispatch`**: Lifecycle functions for the board dispatcher.\n*   **`create-dispatch`**: Creates a `BoardDispatch` instance."

[[:section {:title "std.dispatch.types (Contract Definitions for Dispatchers)" :link "merged-plans-slop-summary-std-dispatch-summary-md-std-dispatch-types-contract-definitions-for-dispatchers"}]]

"This sub-namespace defines Malli schemas (contracts) for various dispatcher configurations, ensuring type safety and valid input for dispatcher creation."

"**Key Functions:**"

"*   **`<queue>`, `<pool>`, `<executor>`**: Schemas for queue, pool, and executor configurations.\n*   **`+dispatch+`, `+dispatch:common+`**: Base schemas for dispatcher configurations.\n*   **`+options|interval+`, `+options|batch+`, `+options|group+`, `+options|delay+`, `+options|debounce+`**: Schemas for specific dispatcher options.\n*   **`<dispatch:core>`, `<dispatch:queue>`, `<dispatch:debounce>`, `<dispatch:hub>`, `<dispatch:board>`**: Specific schemas for each dispatcher type.\n*   **`<dispatch>` (multispec)**: A multispec that dispatches to the appropriate dispatcher schema based on the `:type` key."

[[:section {:title "Usage Pattern:" :link "merged-plans-slop-summary-std-dispatch-summary-md-usage-pattern"}]]

"The `std.dispatch` module is essential for building responsive and scalable applications that need to handle asynchronous operations efficiently. It provides:"

"*   **Flexible Task Execution:** Different dispatch strategies to match various concurrency requirements.\n*   **Resource Management:** Integration with `std.concurrent.executor` for managing thread pools.\n*   **Event-Driven Architectures:** Hooks for reacting to different stages of task processing.\n*   **Dependency Management:** The board dispatcher enables complex workflows with inter-task dependencies.\n*   **Type Safety:** Contracts ensure that dispatcher configurations are valid."

"By offering a rich set of dispatching tools, `std.dispatch` empowers developers to design and implement sophisticated asynchronous processing pipelines."
;; END merged documentation: plans/slop/summary/std_dispatch_summary.md
