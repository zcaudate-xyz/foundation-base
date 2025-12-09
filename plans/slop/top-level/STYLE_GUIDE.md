## Clojure Style Guide for `foundation-base`

This guide is based on the conventions observed in the `src/` directory of the `foundation-base` project. The goal is to maintain a consistent, readable, and idiomatic codebase that leverages Clojure's strengths, especially its macro system and functional programming paradigms, while also accommodating multi-platform code generation via `std.lang`.

### 1. Naming Conventions

Naming is a crucial part of writing clean, understandable code. The following conventions are used throughout the project.

#### 1.1. Files and Namespaces

*   **File Names:** File names use `spinal-case`.
    *   *Example:* `collection.clj`, `future.clj`, `emit_common.clj`

*   **Namespaces:** Namespaces follow the directory structure and use dots to separate components. The general pattern is `std.lib.<file-name>` or `std.lang.base.<sub-directory>.<file-name>`.
    *   *Example:* `std.lib.collection`, `std.lang.base.emit-common`

#### 1.2. Functions

*   **Public Functions:** Public functions use `spinal-case`.
    *   *Example:* `apply-in`, `swap-return!`, `create-directory`

*   **Namespaced Functions:** For groups of related functions within a single namespace, a colon (`:`) is used to create a "sub-namespace". This improves organization and readability.
    *   *Example:* `atom:get`, `atom:set`, `socket:port`, `res:spec-add`

*   **Predicate Functions:** Functions that return a boolean value should end with a question mark `?`.
    *   *Example:* `component?`, `started?`, `primitive?`, `hash-map?`, `qml-props?`

*   **Internal/Helper Functions:** Internal or helper functions that are not intended for public use often start with a hyphen (`-`) or are not explicitly exposed in the `ns` declaration.
    *   *Example:* `-write-value`, `tf-macroexpand`, `qml-props?`

#### 1.3. Variables and Constants

*   **Dynamic Variables:** Dynamic variables (those that can be rebound) follow the Clojure convention of using "earmuffs" (asterisks around the name).
    *   *Example:* `*registry*`, `*kill*`, `*current*`, `*macro-form*`

*   **Constants:** Constants or configuration values are denoted by surrounding the name with plus signs `+`.
    *   *Example:* `+primitives+`, `+hex-array+`, `+default-config+`, `+op-math+`

#### 1.4. Protocols, Records, and Types

*   **Protocols:** Protocols are defined in `CamelCase` and are prefixed with an `I`.
    *   *Example:* `IComponent`, `IApplicable`, `IString`, `IElement`

*   **Records and Types:** Records and types, often defined with `defimpl` or `defrecord`, are written in `CamelCase`.
    *   *Example:* `HostApplicative`, `Result`, `Link`, `Zipper`, `Image`, `Schema`, `Trace`, `NotifyServer`

### 2. Code Structure and Formatting

#### 2.1. Namespace Declarations

*   All `require` statements should be at the top of the file, immediately after the `ns` declaration.
*   Use aliases for required namespaces to keep the code clean and concise.
*   `(:refer-clojure :exclude [...])` is used to avoid name clashes with Clojure's built-in functions.

```clojure
(ns std.lib.apply
  (:require [std.protocol.apply  :as protocol.apply]
            [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.lib.return :as r]
            [std.lib.impl :refer [defimpl] :as impl]))
```

#### 2.2. Function Definitions

*   Public functions should have a docstring (`"..."`) explaining their purpose, arguments, and return value.
*   Use the `{:added "version"}` metadata to indicate when a function was introduced.
*   For functions with multiple arities, each arity should be clearly defined.

```clojure
(defn apply-in
  "runs the applicative within a context
 
   (apply-in (host-applicative {:form '+})
             nil
             [1 2 3 4 5])
   => 15"
  {:added "3.0"}
  ([app rt args]
   (let [input  (protocol.apply/-transform-in app rt args)
         output (protocol.apply/-apply-in app rt input)]
     (r/return-chain output (partial protocol.apply/-transform-out app rt args)))))
```

#### 2.3. Metadata

Metadata is used extensively to provide additional information about functions, macros, and variables. Common metadata keys include:

*   `{:added "version"}`: Indicates when the function/macro was added.
*   `{:refer ...}`: Used in tests to link to the function being tested.
*   `{:style/indent N}`: Specifies indentation for macros.
*   `{:macro-only true}`: For `std.lang` scripts, indicates the file contains only macros.
*   `{:static ...}`: Used in `std.lang` for static analysis or configuration.
*   `{:rt/redis ...}`: Specific to Redis runtime configurations.

#### 2.4. Polymorphism

*   The `defimpl` macro is the preferred way to create new types and records that implement one or more protocols.
*   Use `extend-protocol` or `extend-type` when extending existing types.
*   `defmulti` and `defmethod` are used for extensible functions based on dispatch values.

```clojure
(defimpl HostApplicative
  [function form async]
  :prefix   "host-"
  :invoke   invoke-as
  :protocols [std.protocol.apply/IApplicable
              :body {-apply-default  nil
                     -transform-in   args
                     -transform-out  return}])

(defmulti map->key
  "transforms a map into a key"
  {:added "3.0"}
  (fn [{:keys [mode]}] mode))

(defmethod map->key :default
  ([{:keys [type encoded]}]
   (SecretKeySpec. (to-bytes encoded) type)))
```

### 3. `std.lang` DSL for Multi-Platform Development

The `foundation-base` project heavily leverages a custom DSL provided by `std.lang` for writing code that can be compiled to multiple target languages (e.g., JavaScript, Lua, Python, R, SQL, Solidity). This DSL has its own conventions that differ significantly from standard Clojure.

#### 3.1. Script Definition

*   **`l/script`:** All platform-specific code must be defined within an `(l/script <lang> ...)` block. The `:lang` keyword specifies the target language.
*   **`l/script-`:** Used in test files for setting up platform-specific test environments.

```clojure
(l/script :js
  {:require [[xt.lang.base-lib :as k]]
   })

(l/script :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lang.base-lib :as k]]})
```

#### 3.2. Function and Variable Definitions

*   **`defn.<lang>`:** Functions intended for a specific target language are defined using `defn.js`, `defn.lua`, `defn.py`, `defn.r`, `defn.pg`, etc.
*   **`def.<lang>`:** Variables for a specific target language are defined using `def.js`, `def.lua`, etc.
*   **`def$.<lang>`:** Used for defining global variables in the target language.

```clojure
(defn.js my-function
  [x]
  (k/identity x))

(def.lua K_GROUP "__group__")

(def$.js GLTFLoader
  (. ThreeGLTF GLTFLoader))
```

#### 3.3. Macros for Code Generation

*   **`defmacro.<lang>`:** Macros specific to a target language are defined using `defmacro.js`, `defmacro.lua`, etc.
*   **`h/template-entries`:** This macro is used to generate multiple definitions from a template, often for binding external library functions or constants.

```clojure
(defmacro.js newOSC
  "creates a new OSC instance"
  {:added "4.0"}
  [& [m]]
  (list 'new 'OSC m))

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "ValtioCore"
                                   :tag "js"}]
  [getVersion
   proxy
   [proxyRef ref]
   snapshot
   subscribe])
```

#### 3.4. Cross-Platform Utilities (`xt.lang.base-lib`)

*   The `xt.lang.base-lib` namespace (aliased as `k`) provides a set of cross-platform utility functions that should be used whenever possible to ensure consistency across target languages.
    *   *Example:* `k/obj-keys`, `k/arr-map`, `k/identity`

#### 3.5. Platform-Specific Execution in Tests

*   **`!.<platform>`:** In test files, `!.<platform>` (e.g., `!.js`, `!.lua`, `!.py`, `!.r`) is used to execute a form directly in the context of the specified target language's runtime.

```clojure
(fact "identity function works on all platforms"
  (!.js (k/identity 1))
  => 1

  (!.lua (k/identity 1))
  => 1)
```

### 4. Component-based Architecture

The project extensively uses a component-based architecture for managing application lifecycle and resources.

*   **`std.protocol.component/IComponent`:** The core protocol for defining components.
*   **`component/start`, `component/stop`, `component/kill`:** Standard functions for managing component lifecycle.
*   **`std.lib.resource`:** Provides a registry and management system for various resources.
    *   `res:spec-add`: Registers a new resource specification.
    *   `res:create`: Creates a new resource instance.
    *   `res:start`, `res:stop`: Manages the lifecycle of registered resources.

```clojure
(defimpl LuceneSearch [type instance]
  :string common/to-string
  :protocols [std.protocol.component/IComponent
              :body {-start impl/start-lucene
                     -stop  impl/stop-lucene}])

(res:spec-add
 {:type :hara/lang.library
  :mode {:allow #{:global}
         :default :global}
  :instance {:create #'lib/library:create
             :start h/start
             :stop h/stop}})
```

### 5. Concurrency and Asynchronicity

The `std.concurrent` and `std.lib.future` namespaces are used for managing concurrent operations and asynchronous workflows.

*   **`h/future`:** Creates a `CompletableFuture` for asynchronous execution.
*   **`f/on:complete`, `f/on:success`, `f/on:exception`:** Callbacks for handling future completion, success, or exceptions.
*   **`cc/pool`:** Manages thread pools for concurrent tasks.
*   **`cc/submit`:** Submits tasks to an executor.

```clojure
(defn submit
  "submits a task to an executor"
  {:added "3.0"}
  ([^ExecutorService service ^Callable f]
   (submit service f nil)))

(defn ^CompletableFuture on:complete
  "process both the value and exception"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:complete future f {})))
```

### 6. Data Structures and Transformations

The `std.lib.collection` and `std.lib.transform` namespaces provide utilities for manipulating data structures.

*   **`merge-nested`:** Recursively merges maps.
*   **`tree-nestify`:** Nests keys in a map based on a separator.
*   **`wrap-model-pre-transform`, `wrap-model-post-transform`:** Functions for applying transformations in a data processing pipeline.

```clojure
(defn merge-nested
  "Merges nested values from left to right."
  {:added "3.0"}
  ([& maps]
   (apply merge-with (fn [& args]
                       (if (every? #(or (map? %) (nil? %)) args)
                         (apply merge-nested args)
                         (last args)))
          maps)))

(defn wrap-model-pre-transform
  "Applies a function transformation in the :pre-transform step"
  {:added "3.0"}
  ([f]
   (fn [tdata tsch nsv interim fns datasource]
     (let [strans (:pre-transform interim)
           output (process-transform strans tdata nsv interim tsch datasource)]
       (f output tsch nsv (update-in interim [:ref-path]
                                     #(-> %
                                          (pop)
                                          (conj output)))
          fns datasource)))))
```

### 7. Domain-Specific Languages (DSLs)

The project defines several internal DSLs for specific domains.

*   **SQL Generation (`script.sql.table.select`):** Functions like `build-query-select`, `build-query-from`, `build-query-where` are used to construct SQL queries programmatically.
*   **Redis Commands (`kmi.redis`):** Macros like `flushdb`, `zscoremin`, `zscoremax` provide a Clojure-like interface to Redis commands.
*   **QML (`std.lang.model.spec-js.qml`):** Functions like `emit-qml`, `classify-container` are used to generate QML code.

```clojure
(defn sql:query
  "builds select query"
  {:added "3.0"}
  ([query]
   (sql:query query common/*options*)))

(defmacro.lua flushdb
  "clears the redis db"
  {:added "4.0"}
  ([]
   (list 'redis.call "FLUSHDB")))

(defn emit-qml
  "emits a qml string"
  {:added "4.0"}
  [form grammar mopts]
  (let [tree (classify form)]
    (emit-node tree grammar mopts)))
```

### 8. Code Generation (`code.gen`)

The `code.gen` namespace provides tools for generating code from templates.

*   **`gen/template-generator`:** Creates a function that can generate code from a template file.
*   **`gen/generate`:** Generates code by applying bindings to a template.

```clojure
(def greeter-template-fn
  (gen/template-generator "resources/my/templates/def_greeter.block.clj"))

(def generated-code-list
  (f/template-entries [greeter-template-fn]
                      greeter-entries))
```

### 9. Comments and Documentation

*   **Docstrings:** All public functions must have a clear and comprehensive docstring, explaining their purpose, arguments, and return value, along with examples.
*   **Inline Comments:** Inline comments (`;`) should be used sparingly. Use them to explain *why* a particular piece of code is necessary, especially if the logic is complex. Avoid comments that simply restate what the code is doing.
*   **`^:hidden`:** Used in docstrings to indicate examples that are not meant to be displayed in generated documentation.

This comprehensive style guide aims to capture the essence of the `foundation-base` project's coding philosophy, promoting consistency, readability, and maintainability across its diverse codebase. By adhering to these guidelines, developers can contribute effectively and ensure the long-term health of the project.
