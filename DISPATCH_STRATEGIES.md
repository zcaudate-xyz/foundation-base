# Dispatching Strategies in `std.concurrent.dispatch`

The `std.concurrent.dispatch` library provides a flexible and powerful set of tools for managing concurrent operations in Clojure. It offers several distinct dispatching strategies, each designed to address different use cases. This document provides a summary of the available strategies and their intended applications.

## 1. `CoreDispatch`

- **File:** `src/std/dispatch/core.clj`
- **Purpose:** Provides a basic asynchronous dispatching mechanism.
- **How it works:** `CoreDispatch` takes individual tasks, wraps them in a handler, and submits them to a thread pool for immediate execution. It serves as the foundational building block for other, more specialized dispatchers.
- **Use cases:** Ideal for simple, off-the-shelf asynchronous execution where tasks do not require batching or filtering.

### Code Example

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

;; Create a handler function that processes a single task
(defn handle-task [dispatcher task]
  (println "Processing task:" task)
  (.countDown (:latch (meta dispatcher))))

;; Create a CoreDispatch
(def core-dispatcher
  (dispatch/dispatch {:type :core
                      :handler handle-task}))

;; Use a latch to wait for the task to complete in the example
(alter-meta! core-dispatcher assoc :latch (CountDownLatch. 1))

;; Submit a task for asynchronous execution
(dispatch/submit core-dispatcher {:id 1 :data "some data"})

;; Wait for the task to finish
(.await (:latch (meta core-dispatcher)))

;; Stop the dispatcher
(dispatch/stop core-dispatcher)
```

### Real-world Examples

- **Location:** Primarily in test files, such as `test/std/dispatch/core_test.clj`.
- **Usage:** The `CoreDispatch` is not widely used in the main application codebase. This suggests that for most real-world scenarios, the more specialized dispatchers are preferred.

## 2. `QueueDispatch`

- **File:** `src/std/dispatch/queue.clj`
- **Purpose:** Collects tasks in a queue and processes them in batches.
- **How it works:** Instead of executing tasks as they arrive, `QueueDispatch` accumulates them in a queue. The queue is then processed at regular intervals or when the batch size reaches a specified limit. This approach is optimized for high-throughput scenarios.
- **Use cases:** Efficiently handling a large volume of events, such as logging or analytics, where individual execution latency is less critical than overall system performance.

### Code Example

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

;; Create a handler function that processes a batch of tasks
(defn handle-log-batch [dispatcher logs]
  (println "Processing log batch of size:" (count logs))
  (doseq [log logs]
    (println "  -" log))
  (.countDown (:latch (meta dispatcher))))

;; Create a QueueDispatch that processes batches of up to 10 logs
;; or every 500ms
(def queue-dispatcher
  (dispatch/dispatch {:type :queue
                      :handler handle-log-batch
                      :options {:queue {:max-batch 10 :interval 500}}}))

;; Use a latch to wait for the batch to be processed
(alter-meta! queue-dispatcher assoc :latch (CountDownLatch. 1))

;; Submit multiple log entries
(doseq [i (range 5)]
  (dispatch/submit queue-dispatcher {:level :info :message (str "Log message " i)}))

;; Wait for the batch to be processed
(.await (:latch (meta queue-dispatcher)))

;; Stop the dispatcher
(dispatch/stop queue-dispatcher)
```

### Real-world Examples

- **Location:** `src/std/lang/base/library.clj`
- **Usage:** In a (currently commented-out) section of the code, a `QueueDispatch` is configured to handle the bulk addition of entries to a language library. This is a perfect use case for batch processing, as it allows many small updates to be grouped into a single, more efficient transaction.

## 3. `DebounceDispatch`

- **File:** `src/std/dispatch/debounce.clj`
- **Purpose:** Filters and manages high-frequency events to control the rate of execution.
- **Use cases:** Managing user interface events, rate-limiting API requests, or any scenario where you need to prevent excessive or redundant task execution.

### Strategy: `:eager`
Executes the first event in a series and ignores all subsequent events for a specified interval. Useful for preventing multiple triggers when only the initial action is desired (e.g., clicking a button multiple times).

#### Code Example (:eager)
```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

(defn handle-click [dispatcher event]
  (println "Handling click event:" (:id event))
  (.countDown (:latch (meta dispatcher))))

(def eager-dispatcher
  (dispatch/dispatch {:type :debounce
                      :handler handle-click
                      :options {:debounce {:strategy :eager :interval 500}}}))

(alter-meta! eager-dispatcher assoc :latch (CountDownLatch. 1))

;; Simulate multiple rapid clicks
(dispatch/submit eager-dispatcher {:id 1}) ;; This one will be processed
(dispatch/submit eager-dispatcher {:id 2}) ;; This one will be skipped
(dispatch/submit eager-dispatcher {:id 3}) ;; This one will be skipped

(.await (:latch (meta eager-dispatcher)))
(dispatch/stop eager-dispatcher)
```

### Strategy: `:delay`
Waits for a period of inactivity before executing the most recent event. Ideal for handling user input, such as typing in a search bar, where only the final value is relevant.

#### Code Example (:delay)
```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

(defn handle-search-query [dispatcher query]
  (println "Executing search for:" (:term query))
  (.countDown (:latch (meta dispatcher))))

(def delay-dispatcher
  (dispatch/dispatch {:type :debounce
                      :handler handle-search-query
                      :options {:debounce {:strategy :delay :interval 300}}}))

(alter-meta! delay-dispatcher assoc :latch (CountDownLatch. 1))

;; Simulate rapid user typing
(dispatch/submit delay-dispatcher {:term "clo"})
(Thread/sleep 100)
(dispatch/submit delay-dispatcher {:term "cloju"})
(Thread/sleep 100)
(dispatch/submit delay-dispatcher {:term "clojure"}) ;; Only this one will be processed

(.await (:latch (meta delay-dispatcher)))
(dispatch/stop delay-dispatcher)
```

### Strategy: `:notify`
Executes the first event immediately and then enforces a cool-down period before the next event can be processed. Useful for rate-limiting operations.

#### Code Example (:notify)
```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

(defn handle-api-call [dispatcher call]
  (println "Making API call:" (:endpoint call))
  (.countDown (:latch (meta dispatcher))))

(def notify-dispatcher
  (dispatch/dispatch {:type :debounce
                      :handler handle-api-call
                      :options {:debounce {:strategy :notify :interval 500}}}))

(alter-meta! notify-dispatcher assoc :latch (CountDownLatch. 2))

;; Simulate bursts of API calls
(dispatch/submit notify-dispatcher {:endpoint "/users"}) ;; Processed immediately
(dispatch/submit notify-dispatcher {:endpoint "/products"}) ;; Skipped (within 500ms)
(Thread/sleep 600)
(dispatch/submit notify-dispatcher {:endpoint "/orders"}) ;; Processed after cooldown

(.await (:latch (meta notify-dispatcher)))
(dispatch/stop notify-dispatcher)
```

### Real-world Examples

- **Location:** `src/std/dispatch/hub.clj`
- **Usage:** The `HubDispatch` uses a `DebounceDispatch` with the `:notify` strategy internally. This is a clever example of composition, where the debouncer ensures that the hub's handler is not called too frequently, even when receiving a high volume of events.


## 4. `HubDispatch`

- **File:** `src/std/dispatch/hub.clj`
- **Purpose:** Acts as a centralized dispatcher for routing events to different groups.
- **How it works:** `HubDispatch` uses a grouping function to categorize incoming events. It then uses a debouncer to trigger the processing of these groups, and all events within a group are processed in batches.
- **Use cases:** Managing events from multiple sources that need to be handled in a coordinated manner, such as real-time notifications or data synchronization from different clients.

### Code Example

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

;; Handler for processing batches of notifications
(defn handle-notification-batch [dispatcher notifications]
  (let [user-id (-> notifications first :user-id)]
    (println (format "Sending %d notifications to user %s" (count notifications) user-id)))
  (.countDown (:latch (meta dispatcher))))

;; Group notifications by user-id
(defn group-by-user [dispatcher notification]
  (:user-id notification))

;; Create a HubDispatch
(def hub-dispatcher
  (dispatch/dispatch {:type :hub
                      :handler handle-notification-batch
                      :options {:hub {:group-fn group-by-user
                                      :max-batch 5
                                      :interval 200}}}))

(alter-meta! hub-dispatcher assoc :latch (CountDownLatch. 2))

;; Submit notifications for different users
(dispatch/submit hub-dispatcher {:user-id "user-a" :message "Message 1 for A"})
(dispatch/submit hub-dispatcher {:user-id "user-b" :message "Message 1 for B"})
(dispatch/submit hub-dispatcher {:user-id "user-a" :message "Message 2 for A"})
(dispatch/submit hub-dispatcher {:user-id "user-b" :message "Message 2 for B"})

(.await (:latch (meta hub-dispatcher)))

(dispatch/stop hub-dispatcher)
```
### Real-world Examples

- **Location:** Primarily in test files, such as `test/std/dispatch/hub_test.clj`.
- **Usage:** The `HubDispatch` is a highly specialized dispatcher for complex scenarios where events need to be grouped and processed in batches.

## 5. `BoardDispatch`

- **File:** `src/std/dispatch/board.clj`
- **Purpose:** Manages task dependencies and resource locking to ensure that tasks requiring access to the same shared resource are processed sequentially.
- **How it works:** When a task is submitted, a `groups-fn` determines which resource groups it belongs to. The dispatcher ensures that only one task can be active for a given group at any time. A task will only run when *all* of its required groups are free. Tasks for different, independent groups can run in parallel.
- **Use cases:** Resource locking (e.g., preventing concurrent writes to the same file), and ensuring sequential processing for operations related to a specific key (e.g., all updates for a single user account must happen in order).

### Code Example

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

;; Handler for processing a task that uses resources
(defn handle-resource-task [dispatcher task]
  (println (format "Starting task %s, using resources %s" (:id task) (:resources task)))
  (Thread/sleep 200) ;; Simulate work
  (println (format "Finished task %s" (:id task)))
  (.countDown (:latch (meta dispatcher))))

;; Group tasks by the resources they use
(defn group-by-resource [dispatcher task]
  (:resources task))

;; Create a BoardDispatch with a thread pool to see parallelism
(def board-dispatcher
  (dispatch/dispatch {:type :board
                      :handler handle-resource-task
                      :options {:board {:group-fn group-by-resource}
                                :pool {:size 2}}}))

(alter-meta! board-dispatcher assoc :latch (CountDownLatch. 4))

;; Task 1 and 2 share resource :a, so they must run sequentially.
;; Task 3 uses :c, so it can run in parallel with Task 1 or 2.
;; Task 4 uses :a and :d, so it must wait for both to be free.
(dispatch/submit board-dispatcher {:id 1 :resources [:a :b]})
(dispatch/submit board-dispatcher {:id 2 :resources [:a]})
(dispatch/submit board-dispatcher {:id 3 :resources [:c]})
(dispatch/submit board-dispatcher {:id 4 :resources [:a :d]})

(.await (:latch (meta board-dispatcher)))

(dispatch/stop board-dispatcher)
```

### Real-world Examples

- **Location:** Primarily in test files, such as `test/std/dispatch/board_test.clj`.
- **Usage:** `BoardDispatch` is a powerful and highly specialized dispatcher for managing complex dependencies. Its usage is best illustrated by its test cases.

---

## Dispatcher Hooks

The dispatch library provides a comprehensive set of hooks to monitor the entire lifecycle of a task. These can be incredibly useful for logging, metrics, or debugging. You can provide them in the dispatcher's configuration map under the `:hooks` key.

- **`:on-startup`**: Called when the dispatcher is started.
- **`:on-shutdown`**: Called when the dispatcher is stopped.
- **`:on-submit`**: Called for every entry that is submitted to the dispatcher.
- **`:on-queued`**: Called for an entry that has been successfully accepted and queued for processing.
- **`:on-skip`**: Called for an entry that is deliberately ignored by the dispatcher's strategy (e.g., in a debouncer).
- **`:on-poll`**: Called when a dispatcher polls for more work (e.g., in `BoardDispatch`).
- **`:on-batch`**: Called when a batch of entries is created for processing (e.g., in `QueueDispatch`).
- **`:on-process-bulk`**: Called before a batch of entries is processed.
- **`:on-complete-bulk`**: Called after a batch of entries has been processed.
- **`:on-error`**: Called when an error occurs during the processing of an entry.

### Hook Example (Monitoring `:on-skip`)

This example uses a `DebounceDispatch` and attaches hooks to see which entries are processed and which are skipped.

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

(def processed-items (atom []))
(def skipped-items (atom []))

(def eager-dispatcher-with-hooks
  (dispatch/dispatch
   {:type :debounce
    :handler (fn [_ event]
               (swap! processed-items conj event)
               (println "Processed:" event))
    :hooks {:on-skip (fn [_ event]
                       (swap! skipped-items conj event)
                       (println "Skipped:" event))}
    :options {:debounce {:strategy :eager, :interval 300}}}))


(dispatch/submit eager-dispatcher-with-hooks {:id 1})
(dispatch/submit eager-dispatcher-with-hooks {:id 2})
(dispatch/submit eager-dispatcher-with-hooks {:id 3})

(Thread/sleep 500)

(println "\n--- Results ---")
(println "Processed:" @processed-items)
(println "Skipped:" @skipped-items)

(dispatch/stop eager-dispatcher-with-hooks)
```

---

## Combining Dispatchers and Executors

The behavior of a dispatcher is heavily influenced by the type of executor it uses for its underlying thread pool. The `:options` map in the dispatcher configuration allows you to specify the executor's properties (e.g., `:pool {:size 5}`). Here's a breakdown of how the different combinations work:

| Dispatcher | Executor | Behavior | Use Case |
| :--- | :--- | :--- | :--- |
| **Core** | **:single** | All tasks are executed sequentially in a single thread. | Simple background processing where order is important. |
| **Core** | **:pool** | Tasks are executed concurrently by a fixed number of threads. | General-purpose asynchronous processing. |
| **Core** | **:cached** | Tasks are executed concurrently, with the thread pool growing and shrinking as needed. | Handling a variable number of tasks with high performance. |
| **Queue** | **:single** | Batches are processed sequentially in a single thread. | High-throughput logging where processing order is important. |
| **Queue** | **:pool** | Batches are processed concurrently by a fixed number of threads. | High-throughput scenarios where batches can be processed in parallel. |
| **Debounce** | **:single** | The debounced task is executed in a single thread. | UI event handling where the final event should be processed in the background. |
| **Debounce** | **:pool** | The debounced task is executed by one of the threads in the pool. | Rate-limiting API calls where the calls themselves can be made concurrently. |
| **Hub** | **:single** | All grouped batches are processed sequentially. | Not a typical combination, as it defeats the purpose of parallelizing by group. |
| **Hub** | **:pool** | Batches for different groups are processed concurrently. | The standard use case for HubDispatch, allowing for parallel processing of different event groups. |
| **Board** | **:single** | Tasks for different, unlocked groups will still be processed sequentially. | Guarantees strict ordering, but at the cost of parallelism. |
| **Board** | **<:pool** | Tasks for different, unlocked groups can be processed concurrently. | The standard use case for BoardDispatch, enabling parallel execution while ensuring resource safety. |

**Note on `:scheduled` executors:** The `:scheduled` executor type is not typically used directly with these dispatchers. Instead, scheduling logic is handled at a higher level, for example, by using a separate process to submit tasks to a dispatcher at regular intervals.
