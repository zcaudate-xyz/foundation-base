## std.protocol: A Comprehensive Summary

The `std.protocol` module in `foundation-base` defines a comprehensive set of Clojure protocols and multimethods that establish a standardized interface for various functionalities across the ecosystem. These protocols serve as contracts, enabling polymorphic behavior and allowing different implementations to adhere to a common API. This modular design promotes extensibility, maintainability, and interoperability within the project.

The module is organized into several sub-protocols, each addressing a specific domain:

### `std.protocol.apply`

Defines an interface for objects that can be "applied" or invoked within a runtime context, allowing for transformation of input and output.

*   **`IApplicable` Protocol:**
    *   `-apply-in [app rt args]`: Applies the object within a runtime `rt` with `args`.
    *   `-apply-default [app]`: Provides a default application behavior.
    *   `-transform-in [app rt args]`: Transforms input arguments before application.
    *   `-transform-out [app rt args return]`: Transforms the return value after application.

### `std.protocol.archive`

Defines an interface for interacting with archive files (e.g., ZIP, JAR), providing functionalities for listing, checking, archiving, extracting, inserting, removing, writing, and streaming entries.

*   **`IArchive` Protocol:**
    *   `-url [archive]`: Returns the URL of the archive.
    *   `-path [archive entry]`: Returns the path of an entry within the archive.
    *   `-list [archive]`: Lists all entries in the archive.
    *   `-has? [archive entry]`: Checks if an entry exists in the archive.
    *   `-archive [archive root inputs]`: Creates an archive from `inputs` at `root`.
    *   `-extract [archive output entries]`: Extracts `entries` from the archive to `output`.
    *   `-insert [archive entry input]`: Inserts an `input` as an `entry` into the archive.
    *   `-remove [archive entry]`: Removes an `entry` from the archive.
    *   `-write [archive entry stream]`: Writes an `entry` to a `stream`.
    *   `-stream [archive entry]`: Returns an input stream for an `entry`.
*   **`-open` Multimethod:**
    *   Dispatches on `type` and `path`, allowing different implementations for opening various archive types (e.g., `:zip`, `:jar`).

### `std.protocol.binary`

Defines interfaces for converting objects to and from binary representations, and for handling byte sources, sinks, and channels.

*   **`IBinary` Protocol:**
    *   `-to-bitstr [x]`: Converts `x` to a bit string.
    *   `-to-bitseq [x]`: Converts `x` to a sequence of bits.
    *   `-to-bitset [x]`: Converts `x` to a set of bits.
    *   `-to-bytes [x]`: Converts `x` to a byte array.
    *   `-to-number [x]`: Converts `x` to a number.
*   **`IByteSource` Protocol:**
    *   `-to-input-stream [obj]`: Converts `obj` to an `InputStream`.
*   **`IByteSink` Protocol:**
    *   `-to-output-stream [obj]`: Converts `obj` to an `OutputStream`.
*   **`IByteChannel` Protocol:**
    *   `-to-channel [obj]`: Converts `obj` to a `java.nio.channels.Channel`.

### `std.protocol.block`

Defines interfaces for representing and manipulating code as data structures ("blocks"), forming the Abstract Syntax Tree (AST) or Intermediate Representation (IR) of the transpiler.

*   **`IBlock` Interface:**
    *   `_type []`: Returns the block's type (e.g., `:list`, `:symbol`).
    *   `_tag []`: Returns the semantic tag of the block.
    *   `_string []`: Converts the block to its string representation.
    *   `_length []`: Returns the length of the block.
    *   `_width []`: Returns the width (rows) of the block.
    *   `_height []`: Returns the height (columns) of the block.
    *   `_prefixed []`: Returns the length of the prefix.
    *   `_suffixed []`: Returns the length of the suffix.
    *   `_verify []`: Checks if the block contains valid data.
*   **`IBlockModifier` Interface:**
    *   `_modify [accumulator input]`: Modifies an accumulator with an input block.
*   **`IBlockExpression` Interface:**
    *   `_value []`: Returns the semantic value of the block.
    *   `_value_string []`: Returns the string representation of the block's value.
*   **`IBlockContainer` Interface:**
    *   `_children []`: Returns the children of a container block.
    *   `_replace_children [children]`: Replaces the children of a container block.

### `std.protocol.component`

Defines interfaces for managing the lifecycle and querying the state of components within the system.

*   **`IComponent` Protocol:**
    *   `-start [component]`: Starts the component.
    *   `-stop [component]`: Stops the component gracefully.
    *   `-kill [component]`: Forcefully stops the component.
*   **`IComponentQuery` Protocol:**
    *   `-started? [component]`: Checks if the component is started.
    *   `-stopped? [component]`: Checks if the component is stopped.
    *   `-info [component level]`: Returns information about the component at a given level of detail.
    *   `-remote? [component]`: Checks if the component is a remote resource.
    *   `-health [component]`: Returns the health status of the component.
*   **`IComponentProps` Protocol:**
    *   `-props [component]`: Returns the properties of the component.
*   **`IComponentOptions` Protocol:**
    *   `-get-options [component]`: Returns the options configured for the component.
*   **`IComponentTrack` Protocol:**
    *   `-get-track-path [component]`: Returns the tracking path for the component.

### `std.protocol.context`

Defines interfaces for managing execution contexts, pointers to resources within those contexts, and their lifecycle.

*   **`ISpace` Protocol:**
    *   `-context-set [sp ctx key options]`: Sets a context.
    *   `-context-unset [sp ctx]`: Unsets a context.
    *   `-context-list [sp]`: Lists available contexts.
    *   `-context-get [sp ctx]`: Retrieves a context.
    *   `-rt-active [sp]`: Returns the active runtime.
    *   `-rt-get [sp ctx]`: Retrieves a runtime.
    *   `-rt-start [sp ctx]`: Starts a runtime.
    *   `-rt-started? [sp ctx]`: Checks if a runtime is started.
    *   `-rt-stopped? [sp ctx]`: Checks if a runtime is stopped.
    *   `-rt-stop [sp ctx]`: Stops a runtime.
*   **`IPointer` Protocol:**
    *   `-ptr-context [_]`: Returns the context of a pointer.
    *   `-ptr-keys [ptr]`: Returns the keys associated with a pointer.
    *   `-ptr-val [ptr key]`: Returns the value for a key in a pointer.
*   **`IContext` Protocol:**
    *   `-raw-eval [rt string]`: Evaluates a raw string in the runtime.
    *   `-init-ptr [rt ptr]`: Initializes a pointer in the runtime.
    *   `-tags-ptr [rt ptr]`: Tags a pointer.
    *   `-deref-ptr [rt ptr]`: Dereferences a pointer.
    *   `-display-ptr [rt ptr]`: Displays a pointer.
    *   `-invoke-ptr [rt ptr args]`: Invokes a pointer with arguments.
    *   `-transform-in-ptr [rt ptr args]`: Transforms input arguments for a pointer.
    *   `-transform-out-ptr [rt ptr return]`: Transforms the return value from a pointer.
*   **`IContextLifeCycle` Protocol:**
    *   `-has-module? [rt module-id]`: Checks if a module exists in the runtime.
    *   `-setup-module [rt module-id]`: Sets up a module in the runtime.
    *   `-teardown-module [rt module-id]`: Tears down a module in the runtime.
    *   `-has-ptr? [rt ptr]`: Checks if a pointer exists in the runtime.
    *   `-setup-ptr [rt ptr]`: Sets up a pointer in the runtime.
    *   `-teardown-ptr [rt ptr]`: Tears down a pointer in the runtime.

### `std.protocol.deps`

Defines interfaces for managing dependencies, including retrieving entries, compiling, and tearing down dependency graphs.

*   **`IDeps` Protocol:**
    *   `-get-entry [context id]`: Retrieves a dependency entry.
    *   `-get-deps [context id]`: Retrieves dependencies for an entry.
    *   `-list-entries [context]`: Lists all dependency entries.
*   **`IDepsMutate` Protocol:**
    *   `-add-entry [context id entry deps]`: Adds a dependency entry.
    *   `-remove-entry [context id]`: Removes a dependency entry.
    *   `-refresh-entry [context id]`: Refreshes a dependency entry.
*   **`IDepsCompile` Protocol:**
    *   `-step-construct [context acc id]`: Constructs a dependency step.
    *   `-init-construct [context]`: Initializes dependency construction.
*   **`IDepsTeardown` Protocol:**
    *   `-step-deconstruct [context acc id]`: Deconstructs a dependency step.
*   **`IDepsLibrary` Protocol:**
    *   `-parse-native [context input opts]`: Parses native input for the dependency library.
*   **`IDepsProducer` Protocol:**
    *   `-produce [context opts]`: Produces dependencies.
*   **`-create` Multimethod:**
    *   Dispatches on `:path`, used for creating dependency contexts.

### `std.protocol.dispatch`

Defines interfaces for dispatching tasks, typically to executors or queues.

*   **`IDispatch` Protocol:**
    *   `-submit [dispatch entry]`: Submits an entry for dispatch.
    *   `-bulk? [dispatch]`: Checks if the dispatch mechanism supports bulk operations.
*   **`-create` Multimethod:**
    *   Dispatches on `:type`, used for creating dispatch executors.

### `std.protocol.invoke`

Defines multimethods for handling function invocation, package loading, and anonymous function body generation, particularly relevant for code generation and dynamic execution.

*   **`-invoke-intern` Multimethod:**
    *   Dispatches on `label`, `name`, `config`, `body`, used for constructing forms for interning invoked functions.
*   **`-invoke-package` Multimethod:**
    *   Dispatches on `identity`, used for loading invoke-intern types.
*   **`-fn-body` Multimethod:**
    *   Dispatches on `label` and `body`, used for defining anonymous function bodies (e.g., for different target languages).
*   **`-fn-package` Multimethod:**
    *   Dispatches on `identity`, used for loading `fn-body` types.

### `std.protocol.log`

Defines interfaces for logging, including writing log entries and processing them.

*   **`ILogger` Protocol:**
    *   `-logger-write [logger entry]`: Writes a log entry.
*   **`ILoggerProcess` Protocol:**
    *   `-logger-process [logger entries]`: Processes a collection of log entries.
*   **`-create` Multimethod:**
    *   Dispatches on `:type`, used for creating logger instances.

### `std.protocol.match`

Defines an interface for template matching.

*   **`ITemplate` Protocol:**
    *   `-match [template obj]`: Matches an object against a template.

### `std.protocol.object`

Defines multimethods for accessing and manipulating object metadata, particularly for reading and writing.

*   **`-meta-read` Multimethod:**
    *   Dispatches on `identity`, used for accessing class meta information for reading from an object.
*   **`-meta-write` Multimethod:**
    *   Dispatches on `identity`, used for accessing class meta information for writing to an object.

### `std.protocol.request`

Defines interfaces for making single or bulk requests and processing their responses.

*   **`IRequest` Protocol:**
    *   `-request-single [client command opts]`: Makes a single request.
    *   `-process-single [client output opts]`: Processes a single request's output.
    *   `-request-bulk [client commands opts]`: Makes multiple requests in bulk.
    *   `-process-bulk [client inputs outputs opts]`: Processes bulk request inputs and outputs.
*   **`IRequestTransact` Protocol:**
    *   `-transact-start [client]`: Starts a transaction.
    *   `-transact-end [client]`: Ends a transaction.
    *   `-transact-combine [client commands]`: Combines commands within a transaction.

### `std.protocol.return`

Defines an interface for handling return values, including checking for errors and retrieving metadata.

*   **`IReturn` Protocol:**
    *   `-get-value [obj]`: Retrieves the value from a return object.
    *   `-get-error [obj]`: Retrieves any error from a return object.
    *   `-has-error? [obj]`: Checks if the return object contains an error.
    *   `-get-status [obj]`: Retrieves the status of the return object.
    *   `-get-metadata [obj]`: Retrieves metadata associated with the return object.
    *   `-is-container? [obj]`: Checks if the return object is a container.

### `std.protocol.state`

Defines interfaces and multimethods for managing state, including creation, retrieval, update, and cloning.

*   **`IStateGet` Protocol:**
    *   `-get-state [obj opts]`: Retrieves the state of an object.
*   **`IStateSet` Protocol:**
    *   `-update-state [obj f args opts]`: Updates the state of an object using a function `f`.
    *   `-set-state [obj v opts]`: Sets the state of an object to `v`.
    *   `-empty-state [obj opts]`: Empties the state of an object.
    *   `-clone-state [obj opts]`: Clones the state of an object.
*   **`-create-state` Multimethod:**
    *   Dispatches on `type`, `data`, `opts`, used for creating state objects.
*   **`-container-state` Multimethod:**
    *   Dispatches on `identity`, used for returning a type for a label.

### `std.protocol.stream`

Defines interfaces for stream-like operations, including collecting, producing, blocking, and staging.

*   **`ISink` Protocol:**
    *   `-collect [sink xf supply]`: Collects items into a sink.
*   **`ISource` Protocol:**
    *   `-produce [source]`: Produces items from a source.
*   **`IBlocking` Protocol:**
    *   `-take-element [source]`: Takes an element from a blocking source.
*   **`IStage` Protocol:**
    *   `-stage-unit [stage]`: Returns the unit of a stage.
    *   `-stage-realized? [stage]`: Checks if a stage is realized.
    *   `-stage-realize [stage]`: Realizes a stage.

### `std.protocol.string`

Defines interfaces and multimethods for string conversions and path separation.

*   **`IString` Protocol:**
    *   `-to-string [x]`: Converts `x` to a string.
*   **`-from-string` Multimethod:**
    *   Dispatches on `string`, `type`, `opts`, used for extending string-like objects.
*   **`-path-separator` Multimethod:**
    *   Dispatches on `identity`, used for finding the path separator for a given data type.

### `std.protocol.time`

Defines interfaces and multimethods for handling time instants, durations, representations, and formatting/parsing.

*   **`IInstant` Protocol:**
    *   `-to-long [t]`: Converts a time instant `t` to a long.
    *   `-has-timezone? [t]`: Checks if `t` has timezone information.
    *   `-get-timezone [t]`: Gets the timezone of `t`.
    *   `-with-timezone [t tz]`: Sets the timezone of `t`.
*   **`IRepresentation` Protocol:**
    *   `-millisecond [t opts]`: Gets the millisecond component of `t`.
    *   `-second [t opts]`: Gets the second component of `t`.
    *   `-minute [t opts]`: Gets the minute component of `t`.
    *   `-hour [t opts]`: Gets the hour component of `t`.
    *   `-day [t opts]`: Gets the day component of `t`.
    *   `-day-of-week [t opts]`: Gets the day of the week component of `t`.
    *   `-month [t opts]`: Gets the month component of `t`.
    *   `-year [t opts]`: Gets the year component of `t`.
*   **`IDuration` Protocol:**
    *   `-to-length [d opts]`: Converts a duration `d` to a length.
*   **`-time-meta` Multimethod:**
    *   Dispatches on `cls`, used for accessing meta properties of a class related to time.
*   **`-from-long` Multimethod:**
    *   Dispatches on `long`, `opts` (specifically `:type`), used for creating time representations from a long.
*   **`-now` Multimethod:**
    *   Dispatches on `opts` (specifically `:type`), used for creating a representation of the current time.
*   **`-from-length` Multimethod:**
    *   Dispatches on `long`, `opts` (specifically `:type`), used for creating a representation of a duration.
*   **`-formatter` Multimethod:**
    *   Dispatches on `pattern`, `opts` (specifically `:type`), used for creating time formatters.
*   **`-format` Multimethod:**
    *   Dispatches on `formatter`, `t`, `opts`, used for formatting time objects.
*   **`-parser` Multimethod:**
    *   Dispatches on `pattern`, `opts` (specifically `:type`), used for creating time parsers.
*   **`-parse` Multimethod:**
    *   Dispatches on `parser`, `s`, `opts` (specifically `:type`), used for parsing time strings.

### `std.protocol.track`

Defines an interface for tracking components.

*   **`ITrack` Protocol:**
    *   `-track-path [component]`: Returns the tracking path for a component.

### `std.protocol.watch`

Defines an interface for adding, removing, and listing watches on objects.

*   **`IWatch` Protocol:**
    *   `-add-watch [obj k f opts]`: Adds a watch to an object.
    *   `-has-watch [obj k opts]`: Checks if an object has a specific watch.
    *   `-remove-watch [obj k opts]`: Removes a watch from an object.
    *   `-list-watch [obj opts]`: Lists all watches on an object.

### `std.protocol.wire`

Defines interfaces and multimethods for wire-level communication, including reading, writing, closing, and serialization/deserialization of data.

*   **`IWire` Protocol:**
    *   `-read [remote]`: Reads from a remote connection.
    *   `-write [remote command]`: Writes a command to a remote connection.
    *   `-close [remote]`: Closes a remote connection.
*   **`-as-input` Multimethod:**
    *   Dispatches on `val`, `type`, used for converting an object to an input format.
*   **`-serialize-bytes` Multimethod:**
    *   Dispatches on `val`, `type`, used for converting an object to bytes.
*   **`-deserialize-bytes` Multimethod:**
    *   Dispatches on `bytes`, `type`, used for converting bytes back to an object.

**Overall Importance:**

The `std.protocol` module is central to the `foundation-base` project's architectural design. By defining clear, extensible interfaces, it enables:

*   **Polymorphism:** Different data types and implementations can adhere to the same behavioral contracts, promoting code reuse and flexibility.
*   **Modularity:** Functionalities are decoupled from concrete implementations, making it easier to swap out or add new components without affecting the entire system.
*   **Extensibility:** New types can easily extend existing protocols, allowing the system to evolve and adapt to new requirements.
*   **Testability:** Protocols provide well-defined boundaries, simplifying the testing of individual components.
*   **Clarity and Documentation:** Protocols serve as self-documenting APIs, clearly outlining the expected behavior of various system parts.

This comprehensive set of protocols forms the backbone of `foundation-base`, facilitating its advanced capabilities in transpilation, runtime management, and multi-language support.
