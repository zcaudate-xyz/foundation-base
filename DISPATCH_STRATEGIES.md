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
- **Usage:** The `CoreDispatch` is not widely used in the main application codebase. This suggests that for most real-world scenarios, the more specialized dispatchers (`QueueDispatch`, `DebounceDispatch`, and `HubDispatch`) are preferred.

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
- **Strategies:**
  - **`:eager`:** Executes the first event in a series and ignores all subsequent events for a specified interval. This is useful for preventing multiple triggers when only the initial action is desired.
  - **`:delay`:** Waits for a period of inactivity before executing the most recent event. This is ideal for handling user input, such as typing in a search bar, where only the final value is relevant.
  - **`:notify`:** Executes the first event immediately and then enforces a cool-down period before the next event can be processed. This is useful for rate-limiting operations.
- **Use cases:** Managing user interface events, rate-limiting API requests, or any scenario where you need to prevent excessive or redundant task execution.

### Code Example (:delay strategy)

```clojure
(require '[std.dispatch :as dispatch])
(import '[java.util.concurrent CountDownLatch])

;; Handler for processing a search query
(defn handle-search-query [dispatcher query]
  (println "Executing search for:" (:term query))
  (.countDown (:latch (meta dispatcher))))

;; Create a DebounceDispatch with a :delay strategy
;; This will wait for 300ms of inactivity before executing the handler
(def debounce-dispatcher
  (dispatch/dispatch {:type :debounce
                      :handler handle-search-query
                      :options {:debounce {:strategy :delay :interval 300}}}))

(alter-meta! debounce-dispatcher assoc :latch (CountDownLatch. 1))

;; Simulate rapid user typing
(dispatch/submit debounce-dispatcher {:term "clo"})
(Thread/sleep 100)
(dispatch/submit debounce-dispatcher {:term "cloju"})
(Thread/sleep 100)
(dispatch/submit debounce-dispatcher {:term "clojure"})

;; The handler will only be called for the last submitted query ("clojure")
;; after the 300ms interval has passed.

(.await (:latch (meta debounce-dispatcher)))

(dispatch/stop debounce-dispatcher)
```

### Real-world Examples

- **Location:** `src/std/dispatch/hub.clj`
- **Usage:** The `HubDispatch` uses a `DebounceDispatch` internally to trigger the processing of its event groups. This is a clever example of composition, where the debouncer ensures that the hub's handler is not called too frequently, even when receiving a high volume of events.

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
- **Usage:** The `HubDispatch` is a highly specialized dispatcher and is not currently used in the main application codebase. It is intended for complex scenarios where events need to be grouped and processed in batches, and its usage is best understood by examining its test cases.

These strategies can be used individually or in combination to build sophisticated and efficient concurrent systems. The library is designed with a clear separation of concerns and a consistent API, making it easy to choose the right tool for the job.
