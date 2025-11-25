# Dispatching Strategies in `std.concurrent.dispatch`

The `std.concurrent.dispatch` library provides a flexible and powerful set of tools for managing concurrent operations in Clojure. It offers several distinct dispatching strategies, each designed to address different use cases. This document provides a summary of the available strategies and their intended applications.

## 1. `CoreDispatch`

- **File:** `src/std/dispatch/core.clj`
- **Purpose:** Provides a basic asynchronous dispatching mechanism.
- **How it works:** `CoreDispatch` takes individual tasks, wraps them in a handler, and submits them to a thread pool for immediate execution. It serves as the foundational building block for other, more specialized dispatchers.
- **Use cases:** Ideal for simple, off-the-shelf asynchronous execution where tasks do not require batching or filtering.

## 2. `QueueDispatch`

- **File:** `src/std/dispatch/queue.clj`
- **Purpose:** Collects tasks in a queue and processes them in batches.
- **How it works:** Instead of executing tasks as they arrive, `QueueDispatch` accumulates them in a queue. The queue is then processed at regular intervals or when the batch size reaches a specified limit. This approach is optimized for high-throughput scenarios.
- **Use cases:** Efficiently handling a large volume of events, such as logging or analytics, where individual execution latency is less critical than overall system performance.

## 3. `DebounceDispatch`

- **File:** `src/std/dispatch/debounce.clj`
- **Purpose:** Filters and manages high-frequency events to control the rate of execution.
- **Strategies:**
  - **`:eager`:** Executes the first event in a series and ignores all subsequent events for a specified interval. This is useful for preventing multiple triggers when only the initial action is desired.
  - **`:delay`:** Waits for a period of inactivity before executing the most recent event. This is ideal for handling user input, such as typing in a search bar, where only the final value is relevant.
  - **`:notify`:** Executes the first event immediately and then enforces a cool-down period before the next event can be processed. This is useful for rate-limiting operations.
- **Use cases:** Managing user interface events, rate-limiting API requests, or any scenario where you need to prevent excessive or redundant task execution.

## 4. `HubDispatch`

- **File:** `src/std/dispatch/hub.clj`
- **Purpose:** Acts as a centralized dispatcher for routing events to different groups.
- **How it works:** `HubDispatch` uses a grouping function to categorize incoming events. It then uses a debouncer to trigger the processing of these groups, and all events within a group are processed in batches.
- **Use cases:** Managing events from multiple sources that need to be handled in a coordinated manner, such as real-time notifications or data synchronization from different clients.

These strategies can be used individually or in combination to build sophisticated and efficient concurrent systems. The library is designed with a clear separation of concerns and a consistent API, making it easy to choose the right tool for the job.
