## std.concurrent: A Comprehensive Summary (including submodules)

The `std.concurrent` module provides a comprehensive suite of utilities for managing concurrency in Clojure applications, building upon Java's `java.util.concurrent` package. It offers abstractions for thread management, message passing, task execution, and resource pooling, aiming to simplify the development of robust and scalable concurrent systems.

### `std.concurrent` (Main Namespace)

This namespace acts as an aggregator, re-exporting key functions and macros from its sub-namespaces to provide a unified and convenient interface for concurrent programming.

**Key Re-exported Functions:**

*   From `std.concurrent.thread`: All `thread:*` functions.
*   From `std.concurrent.queue`: All `queue:*` functions, `take`, `put`, `peek`, `drain`, `pop`, `push`, `remove`.
*   From `std.concurrent.executor`: All `executor:*` and `exec:*` functions, `submit`, `schedule`.
*   From `std.concurrent.atom`: All `aq:*` and `hub:*` functions.
*   From `std.concurrent.bus`: All `bus:*` functions.
*   From `std.concurrent.pool`: All `pool:*` functions.
*   From `std.concurrent.request`: All `req:*` functions, `bulk`, `transact`.
*   From `std.concurrent.request-apply`: `req:applicative`.
*   From `std.concurrent.request-command`: `req:command`, `req:run`.
*   From `std.concurrent.relay`: `send`, `relay:create`, `relay`, `relay:bus`.

### `std.concurrent.thread` (Thread Management)

This sub-namespace provides low-level utilities for creating, inspecting, and manipulating `java.lang.Thread` objects.

**Key Functions:**

*   **`thread:current`**: Returns the current thread.
*   **`thread:id`**: Returns the ID of a thread.
*   **`thread:interrupt`**: Interrupts a thread.
*   **`thread:sleep`**: Pauses the current thread.
*   **`thread:spin`**: Hints to the processor that the current thread is busy-waiting.
*   **`thread:wait-on`, `thread:notify`, `thread:notify-all`**: Standard Java object wait/notify mechanisms.
*   **`thread:has-lock?`**: Checks if the current thread holds the monitor lock on an object.
*   **`thread:yield`**: Hints to the scheduler that the current thread is willing to yield its current use of a processor.
*   **`stacktrace`, `all-stacktraces`**: Retrieves stack trace information.
*   **`thread:all`, `thread:all-ids`**: Lists all active threads or their IDs.
*   **`thread:dump`**: Dumps the stack trace of the current thread.
*   **`thread:active-count`**: Returns the number of active threads in the current thread's thread group.
*   **`thread:alive?`**: Checks if a thread is alive.
*   **`thread:daemon?`**: Checks if a thread is a daemon thread.
*   **`thread:interrupted?`**: Checks if a thread has been interrupted.
*   **`thread:has-access?`**: Checks if a thread allows access to current.
*   **`thread:start`, `thread:run`**: Starts or runs a thread's execution.
*   **`thread:join`**: Waits for a thread to terminate.
*   **`thread:uncaught`, `thread:global-uncaught`**: Manages uncaught exception handlers.
*   **`thread:classloader`**: Manages the context class loader for a thread.
*   **`thread`**: A constructor function for creating and configuring new `Thread` instances.

### `std.concurrent.queue` (Blocking Queues and Deques)

This sub-namespace provides convenient wrappers and extensions for Java's `java.util.concurrent.BlockingQueue` and `BlockingDeque` interfaces, simplifying message passing and task buffering.

**Key Functions:**

*   **`->timeunit`**: Converts a keyword (e.g., `:ms`, `:s`) to a `java.util.concurrent.TimeUnit` enum.
*   **`deque`**: Creates a `LinkedBlockingDeque`.
*   **`queue`**: Creates a `LinkedBlockingQueue`.
*   **`queue?`, `deque?`**: Predicates to check if an object is a `BlockingQueue` or `BlockingDeque`.
*   **`queue:fixed`**: Creates an `ArrayBlockingQueue` with a fixed capacity.
*   **`queue:limited`**: Creates a `hara.lib.concurrent.LimitedQueue` (a custom limited queue).
*   **`take`**: Retrieves and removes the head of the queue, with optional timeouts.
*   **`drain`**: Removes all available elements from the queue and adds them to a collection.
*   **`put`**: Inserts an element at the tail of the queue, with optional timeouts.
*   **`peek`**: Retrieves, but does not remove, the head of the queue.
*   **`remaining-capacity`**: Returns the remaining capacity of the queue.
*   **`peek-first`, `peek-last`**: Retrieves, but does not remove, the first or last element of a deque.
*   **`put-first`, `put-last`**: Inserts an element at the head or tail of a deque, with optional timeouts.
*   **`take-first`, `take-last`**: Retrieves and removes the first or last element of a deque, with optional timeouts.
*   **`push`**: Inserts an element at the front of a deque.
*   **`pop`**: Retrieves and removes the first element of a deque.
*   **`remove`**: Removes a specific element from the queue.
*   **`process-bulk`**: Processes elements from a queue in batches.

### `std.concurrent.executor` (Executor Services)

This sub-namespace provides functions for creating and managing various types of `java.util.concurrent.ExecutorService` instances, including single-threaded, pooled, cached, and scheduled executors. It integrates with `std.lib.component` for lifecycle management and `std.lib.component.track` for tracking.

**Key Functions:**

*   **`wrap-min-time`**: Wraps a `Callable` to ensure its execution takes at least a minimum amount of time, with an optional initial delay.
*   **`exec:queue`**: Creates a `BlockingQueue` suitable for use with executors.
*   **`executor:single`**: Creates an `ExecutorService` that uses a single worker thread.
*   **`executor:pool`**: Creates a `ThreadPoolExecutor` with a fixed or bounded pool size.
*   **`executor:cached`**: Creates a cached thread pool that reuses existing threads.
*   **`exec:shutdown`, `exec:shutdown-now`**: Initiates an orderly or immediate shutdown of an executor.
*   **`exec:get-queue`**: Retrieves the underlying `BlockingQueue` of a `ThreadPoolExecutor`.
*   **`submit`**: Submits a `Callable` task for execution, returning a `Future`. Supports `min`, `max` (timeout), and `delay` options.
*   **`submit-notify`**: Submits a task only if the executor's queue has remaining capacity.
*   **`executor:scheduled`**: Creates a `ScheduledThreadPoolExecutor`.
*   **`schedule`**: Schedules a task for one-time execution after a delay.
*   **`schedule:fixed-rate`**: Schedules a task to run at a fixed rate.
*   **`schedule:fixed-delay`**: Schedules a task to run with a fixed delay between executions.
*   **`exec:await-termination`**: Blocks until all tasks have completed execution after a shutdown request.
*   **`exec:shutdown?`, `exec:terminated?`, `exec:terminating?`**: Checks the shutdown status of an executor.
*   **`exec:current-size`, `exec:current-active`, `exec:current-submitted`, `exec:current-completed`**: Retrieves statistics about the executor's threads and tasks.
*   **`exec:pool-size`, `exec:pool-max`, `exec:keep-alive`**: Manages core pool size, maximum pool size, and thread keep-alive time.
*   **`exec:rejected-handler`**: Gets or sets the handler for tasks that cannot be executed.
*   **`exec:at-capacity?`, `exec:increase-capacity`**: Checks and adjusts executor capacity.
*   **`executor:type`**: Returns the type of executor (e.g., `:single`, `:pool`, `:scheduled`, `:cached`).
*   **`executor:info`**: Returns detailed information about an executor.
*   **`executor:props`**: Returns component properties for an executor.
*   **`executor:health`**: Returns the health status of an executor.
*   **`executor:start`, `executor:stop`, `executor:kill`, `executor:started?`, `executor:stopped?`**: Component lifecycle functions.
*   **`executor` (multimethod)**: A multimethod for creating executors based on a `:type` keyword.
*   **`executor:shared`, `executor:share`, `executor:unshare`**: Manages a registry of named, shared `ExecutorService` instances.

### `std.concurrent.atom` (Atom Queues and Hubs)

This sub-namespace provides specialized atom-based queueing mechanisms for batch processing and managing asynchronous task submissions.

**Key Functions:**

*   **`aq:new`**: Creates an atom holding a vector to act as a queue.
*   **`aq:process`**: Processes elements from an atom queue in batches, applying a function to each batch.
*   **`aq:submit`**: Returns a submission function that adds entries to an atom queue and triggers batch processing on an executor.
*   **`aq:executor`**: Creates an executor specifically designed to work with an atom queue, handling batch submissions.
*   **`hub:new`**: Creates a "hub" atom, which is an atom containing a map with a `ticket` (an incomplete future) and a `queue`.
*   **`hub:process`**: Processes elements from a hub's queue in batches, fulfilling the hub's ticket when done.
*   **`hub:add-entries`**: Adds entries to a hub's queue.
*   **`hub:submit`**: Returns a submission function for a hub, which adds entries and triggers batch processing on an executor.
*   **`hub:executor`**: Creates an executor that uses a hub for batch processing.
*   **`hub:wait`**: Waits for a hub's processing to complete.

### `std.concurrent.bus` (Message Bus)

This sub-namespace implements a message bus system for inter-thread communication, allowing threads to register, send messages, and receive responses. It's built on `std.concurrent.queue` and `std.concurrent.thread`.

**Key Functions:**

*   **`bus:get-thread`, `bus:get-id`**: Retrieves thread objects or their registered IDs.
*   **`bus:has-id?`, `bus:all-ids`, `bus:all-threads`, `bus:get-count`**: Query bus state.
*   **`bus:get-queue`**: Retrieves the message queue for a specific thread.
*   **`bus:register`, `bus:deregister`**: Registers or deregisters threads with the bus.
*   **`bus:send`**: Sends a message to a specific thread's queue, returning a `Future` for the response.
*   **`bus:wait`**: Waits for a message to arrive in the current thread's queue.
*   **`handler-thunk`, `run-handler`**: Internal functions for managing message handler loops in separate threads.
*   **`bus:send-all`**: Sends a message to all registered threads.
*   **`bus:open`**: Starts a new message handler loop in a separate thread, returning a `Future` that completes when the handler starts.
*   **`bus:close`, `bus:close-all`**: Sends an `:exit` message to gracefully stop handler loops.
*   **`bus:kill`, `bus:kill-all`**: Forcefully interrupts handler threads.
*   **`main-thunk`, `main-loop`**: Internal functions for the bus's main message processing loop.
*   **`started?-bus`, `start-bus`, `stop-bus`, `info-bus`**: Component lifecycle and info functions for the bus.
*   **`Bus` (defimpl record)**: The concrete record type for the message bus.
*   **`bus:create`, `bus`**: Creates and starts a message bus instance.
*   **`bus?`**: Checks if an object is a `Bus`.
*   **`bus:with-temp` (macro)**: A macro for creating a temporary bus, registering the current thread, and ensuring proper cleanup.
*   **`bus:reset-counters`**: Resets message sent/received counters.

### `std.concurrent.pool` (Resource Pooling)

This sub-namespace provides a generic resource pooling mechanism for managing a collection of reusable resources (e.g., database connections, network sockets). It ensures efficient resource utilization and proper lifecycle management.

**Key Functions:**

*   **`resource-info`, `resource-string`**: Provides information and string representation for individual pooled resources.
*   **`pool-resource`**: Creates a `PoolResource` record, which tracks the state of a single resource.
*   **`pool:acquire`**: Acquires a resource from the pool. If no idle resources are available and the pool is not at its maximum capacity, it attempts to create a new resource.
*   **`pool:dispose`**: Disposes of an idle resource.
*   **`pool:dispose-over`**: Disposes of resources if the idle/busy count exceeds a limit.
*   **`pool:release`**: Releases a resource back to the pool.
*   **`pool:cleanup`**: Periodically cleans up idle resources based on `keep-alive` and health checks.
*   **`pool-handler`**: The handler function for the pool's cleanup thread.
*   **`pool:started?`, `pool:stopped?`, `pool:start`, `pool:stop`, `pool:kill`**: Component lifecycle functions for the pool.
*   **`pool:info`**: Returns detailed information about the pool's state (idle, busy, stats).
*   **`pool:props`**: Returns component properties for the pool.
*   **`pool:health`**: Returns the health status of the pool.
*   **`pool:track-path`**: Returns the tracking path for the pool.
*   **`Pool` (defimpl record)**: The concrete record type for the resource pool.
*   **`pool?`**: Checks if an object is a `Pool`.
*   **`pool:create`, `pool`**: Creates and starts a resource pool instance.
*   **`pool:resources:thread`, `pool:resources:busy`, `pool:resources:idle`**: Retrieves resources associated with a thread, or all busy/idle resources.
*   **`pool:dispose:mark`, `pool:dispose:unmark`**: Marks a resource for disposal or unmarks it.
*   **`wrap-pool-resource`**: Wraps a function to automatically acquire and release a resource from the pool.
*   **`pool:with-resource` (macro)**: A macro for using a resource from the pool within a lexical scope, ensuring it's released afterwards.

### `std.concurrent.print` (Asynchronous Printing)

This sub-namespace provides an asynchronous printing mechanism, allowing print operations to be offloaded to a separate executor, preventing blocking of the main application thread.

**Key Functions:**

*   **`print-handler`**: The handler function for processing print items.
*   **`get-executor`**: Retrieves the print executor.
*   **`submit`**: Submits items for asynchronous printing.
*   **`print`, `println`, `prn`, `pprint`**: Asynchronous versions of standard Clojure print functions.
*   **`pprint-str`**: Pretty-prints an item to a string.
*   **`with-system` (macro)**: Binds `env/*local*` to `false` to force system-level printing.
*   **`with-out-str` (macro)**: Captures output from asynchronous print operations.

### `std.concurrent.relay` (Process/Socket Communication Relay)

This sub-namespace provides a generic relay mechanism for communicating with external processes or network sockets. It uses a message bus (`std.concurrent.bus`) to manage asynchronous I/O and command sending.

**Key Functions:**

*   **`get-bus`**: Retrieves the common stream bus used by relays.
*   **`with:bus` (macro)**: Binds the default relay bus.
*   **`attach-read-passive`**: Attaches a passive reader to an input stream, processing messages asynchronously.
*   **`attach-interactive`**: Attaches an interactive reader to an input stream, using the bus for communication.
*   **`relay-stream`**: Creates a `RelayStream` record, representing an input or output stream with associated metadata.
*   **`relay-stream?`**: Checks if an object is a `RelayStream`.
*   **`make-socket-instance`, `make-process-instance`, `make-instance`**: Creates instances for socket or process communication, setting up input/output streams.
*   **`relay-start`, `relay-stop`**: Component lifecycle functions for the relay.
*   **`Relay` (defimpl record)**: The concrete record type for the relay.
*   **`relay:create`, `relay`**: Creates and starts a relay instance.
*   **`send`**: Sends commands (e.g., `:raw`, `:partial`, `:read-line`) to the relay, which are then processed by the external process/socket.

### `std.concurrent.relay.transport` (Relay Transport Layer)

This sub-namespace provides the low-level implementation for reading from and writing to input/output streams in the `std.concurrent.relay` module.

**Key Functions:**

*   **`bytes-output`**: Creates a `ByteArrayOutputStream`.
*   **`mock-input-stream`**: Creates a mock `InputStream` for testing.
*   **`read-bytes-limit`, `read-bytes-line`, `read-bytes-some`**: Functions for reading bytes from an `InputStream` with various strategies (limited amount, line by line, until timeout).
*   **`op-count`, `op-clean`, `op-clean-some`**: Operations for querying stream status or cleaning its content.
*   **`op-read-all-bytes`, `op-read-all`, `op-read-some-bytes`, `op-read-some`, `op-read-line`, `op-read-limit`**: Operations for reading data from the stream in various formats and with limits/timeouts.
*   **`process-by-line`, `process-by-handler`**: Functions for processing stream content line by line or with a custom handler.
*   **`process-op`**: Dispatches to the appropriate stream operation based on the command.
*   **`send-write-raw`, `send-write-flush`, `send-write-line`**: Functions for writing raw bytes, flushing, or writing a line with a newline to an `OutputStream`.
*   **`send-command`**: Sends a command to the output stream and optionally waits for a response from the input stream.

### `std.concurrent.request` (Request/Response Abstraction)

This sub-namespace provides a generic request/response abstraction for interacting with various "clients" (e.g., database connections, API endpoints). It supports single requests, bulk requests, and transactional requests, with options for pre/post-processing, chaining, and asynchronous execution.

**Core Concepts:**

*   **`IRequest` Protocol:** Defines methods for `request-single`, `request-bulk`, `process-single`, `process-bulk`.
*   **`IRequestTransact` Protocol:** Defines methods for `transact-start`, `transact-end`, `transact-combine`.
*   **Request Contexts:** Manages `*bulk*` and `*transact*` dynamic variables to track bulk and transactional operations.
*   **Options (`req:opts`)**: A rich set of options for controlling request behavior, including `pre`/`post` hooks, `chain` functions, `async` execution, `measure` (timing), and `debug`.

**Key Functions:**

*   **`bulk-context`, `transact-context`**: Creates contexts for bulk and transactional operations.
*   **`req:opts-clean`, `req:opts`, `req:opts-init`**: Functions for managing and initializing request options.
*   **`opts:timer`, `opts:wrap-measure`, `measure:debug`**: Utilities for timing and debugging requests.
*   **`req:return`**: Handles returning results, potentially dereferencing futures.
*   **`req:single-prep`, `req:single-complete`, `req:single`**: Functions for handling single request execution.
*   **`req:unit`**: Executes a single request within a bulk context.
*   **`bulk:inputs`**: Captures all inputs submitted within a bulk context.
*   **`bulk-collect`, `bulk-process`, `bulk`**: Functions for managing and processing bulk requests.
*   **`transact`**: Enables transactional execution for a block of requests.
*   **`transact-prep`**: Prepares request options for transactional contexts.
*   **`req-fn`**: The core function for dispatching requests based on current context (`*bulk*`, `*transact*`).
*   **`bulk:transact`**: Combines bulk and transactional behavior.
*   **`req` (macro)**: The primary macro for submitting requests.
*   **`req:in` (macro)**: Captures all requests submitted within its body.
*   **`req:bulk` (macro)**: Defines a block of requests to be executed in bulk.
*   **`req:transact` (macro)**: Defines a block of requests to be executed transactionally within a bulk context.
*   **`bulk:map`, `transact:map`**: Apply a function across a collection of inputs in bulk or transactionally.
*   **`fn-request-single`, `fn-request-bulk`, `fn-process-bulk`, `fn-process-single`**: Default implementations of `IRequest` for Clojure functions.

### `std.concurrent.request-apply` (Applicative Request)

This sub-namespace integrates the `std.concurrent.request` framework with the `std.lib.apply` applicative pattern, allowing requests to be defined and executed as applicatives.

**Key Functions:**

*   **`req-call` (multimethod)**: An extensible multimethod for dispatching request calls based on their `:type` (e.g., `:single`, `:bulk`, `:transact`, `:retry`).
*   **`req-apply-in`**: Runs a request applicative, handling transformations and options.
*   **`ReqApplicative` (defimpl record)**: A record that implements `std.protocol.apply/IApplicable` for requests, allowing them to be invoked like functions.
*   **`req:applicative`**: Constructs a `ReqApplicative` instance.

### `std.concurrent.request-command` (Command-based Requests)

This sub-namespace provides a framework for defining and running "commands" as a structured way to encapsulate requests. Commands can have predefined functions, options, and formatting rules for inputs and outputs.

**Key Functions:**

*   **`format-input`, `format-output`**: Helper functions for formatting command inputs and outputs.
*   **`run-request` (multimethod)**: An extensible multimethod for running commands based on their `:type` (e.g., `:single`, `:bulk`, `:transact`, `:retry`).
*   **`req:run`**: The main function for executing a command, applying input/output formatting and dispatching to `run-request`.
*   **`Command` (defimpl record)**: A record that encapsulates a command's definition (type, name, arguments, function, options, format, process).
*   **`req:command`**: Constructs a `Command` instance.

### Usage Pattern:

The `std.concurrent` module is essential for building high-performance, responsive, and scalable Clojure applications. It provides:
*   **Structured Concurrency:** Tools for managing threads, executors, and message queues in an organized manner.
*   **Asynchronous Operations:** Support for non-blocking I/O and task execution.
*   **Resource Management:** Efficient pooling of expensive resources.
*   **Inter-process Communication:** A message bus for communication between different parts of an application or external processes.
*   **Request Abstraction:** A flexible framework for defining and executing requests against various services, with advanced features like bulk processing and transactions.

By offering these powerful concurrent programming primitives, `std.concurrent` empowers developers to tackle complex concurrency challenges with greater ease and reliability.