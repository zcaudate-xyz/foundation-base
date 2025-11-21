## Function Design Guide for `foundation-base`

This guide outlines best practices and common patterns for designing functions within the `foundation-base` project, drawing examples directly from the codebase. The goal is to promote clarity, modularity, extensibility, and maintainability.

### 1. Clarity and Readability

Functions should be easy to understand at a glance. This is achieved through clear naming, comprehensive documentation, and consistent formatting.

#### 1.1. Docstrings and Metadata

Every public function must have a comprehensive docstring that explains its purpose, arguments, and return value. The `{:added "version"}` metadata is mandatory.

*   **Purpose:** Clearly state what the function does.
*   **Arguments:** Describe each argument, its type, and its role.
*   **Return Value:** Explain what the function returns.
*   **Examples:** Provide illustrative examples of how to use the function.
*   **Metadata:** Use `{:added "version"}` to track when the function was introduced. Other custom metadata can provide additional context.

**Example: `std.lib.collection/map-keys`**

```clojure
(defn map-keys
  "changes the keys of a map
 
   (map-keys inc {0 :a 1 :b 2 :c})
   => {1 :a, 2 :b, 3 :c}"
  {:added "3.0"}
  ([f m]
   (reduce (fn [out [k v]]
             (assoc out (f k) v))
           {} 
           m)))
```

**Example: `std.lib.network/port:check-available`**

```clojure
(defn port:check-available
  "check that port is available
 
   (port:check-available 51311)
   => anything"
  {:added "4.0"}
  ([port]
   (try
     (with-open [^ServerSocket s (ServerSocket. port)]
       (.setReuseAddress s true)
       (.getLocalPort s))
     (catch Throwable t
       false))))
```

#### 1.2. Naming Conventions

*   **Public Functions:** Use `spinal-case` (e.g., `create-directory`, `process-transform`).
*   **Namespaced Functions:** Use a colon (`:`) to create "sub-namespaces" for related functions (e.g., `atom:get`, `socket:port`).
*   **Predicate Functions:** End with a question mark `?` (e.g., `component?`, `hash-map?`).
*   **Internal/Helper Functions:** Often start with a hyphen (`-`) or are not explicitly exposed (e.g., `-write-value`, `tf-macroexpand`).

#### 1.3. Arity and Parameter Order

Functions should handle multiple arities gracefully, typically with the simpler arities calling the more complex ones. Positional arguments are preferred for mandatory inputs, while optional parameters are often passed in a map.

**Example: `std.fs.api/create-directory`**

```clojure
(defn create-directory
  "creates a directory on the filesystem"
  {:added "3.0"}
  ([path]
   (create-directory path {}))
  ([path attrs]
   (Files/createDirectories (path/path path)
                            (attr/map->attr-array attrs))))
```

### 2. Modularity and Single Responsibility

Functions should ideally do one thing and do it well. Complex tasks are broken down into smaller, composable functions.

**Example: `std.timeseries.compute/process-transform`**

This function focuses solely on transforming an array based on a given interval and template, delegating sub-tasks like `parse-transform-expr` and `transform-interval` to other functions.

```clojure
(defn process-transform
  "processes the transform stage"
  {:added "3.0"}
  ([arr {:keys [transform template merge-fn]} type time-opts]
   (let [len (count arr)
         empty (-> template :raw :empty)
         interval (range/range-op :to interval arr time-opts)
         [tag val] interval
         arr   (case tag
                 :time   (let [{:keys [key order]} time-opts
                               {:keys [op-fn comp-fn]} (common/order-fns order)
                               start  (key (first arr))
                               steps  (quot (math/abs (- start (key (last arr))))
                                            val)
                               sorted (group-by (fn [m]
                                                  (quot (math/abs (- start (key m)))
                                                        val))
                                                arr)]
                           (mapv (fn [i] 
                                   (or (get sorted i)
                                       [(case type
                                          :map  (assoc empty key (op-fn start (* i val)))
                                          :time (op-fn start (* i val)))]))
                                 (range steps)))
                 :ratio  (let [num (math/ceil (* len val))]
                           (partition num arr))
                 :array (partition val arr))]
     (mapv merge-fn arr))))
```

### 3. Parameter Design

#### 3.1. Positional vs. Map Arguments

*   **Positional Arguments:** Used for mandatory and frequently used parameters.
*   **Map Arguments:** Preferred for optional parameters, configurations, or when there are many parameters. This improves readability and allows for easy extension.

**Example: `std.lib.future/submit`**

```clojure
(defn submit
  "submits a task to an executor"
  {:added "3.0"}
  ([^ExecutorService service ^Callable f]
   (submit service f nil))
  ([^ExecutorService service f {:keys [min max delay default] :as m}]
   (let [^Callable f (cond-> f
                       min (wrap-min-time min (or delay 0)))
         opts (cond-> {:pool service}
                max     (assoc :timeout max)
                default (assoc :default default)
                delay   (assoc :delay delay))]
     (f/future:run f opts))))
```


### 4. Error Handling

Robust error handling is crucial. The `h/error` function is the standard way to throw exceptions with structured data, making debugging easier.

**Example: `std.lib.foundation/error`**

```clojure
(defmacro error
  "throws an error with message
 
   (error "Error")
   => (throws)"
  {:added "3.0"}
  ([message]
   `(throw (ex-info ~message {})))
  ([message data]
   `(throw (ex-info ~message ~data))))
```

**Example: `std.lang.base.book/assert-module`**

```clojure
(defn assert-module
  "asserts that module exists"
  {:added "4.0"}
  [book module-id]
  (or (has-module? book module-id)
      (h/error "No module found." {:available (set (list-entries book :module)) 
                                   :module module-id})))
```

### 5. Immutability vs. Mutability

Clojure favors immutability, but mutable state is managed explicitly when necessary.

*   **Persistent Data Structures:** Clojure's default data structures are immutable.
*   **Atoms and Volatiles:** `atom` and `volatile!` are used for managing mutable state with clear semantics.
*   **`defmutable`:** For defining mutable data structures when performance or specific behavior requires it.

**Example: `std.lib.mutable/defmutable`**

```clojure
(defmacro defmutable
  "allows definition of a mutable datastructure"
  {:added "3.0"}
  ([tp-name fields & protos] 
   {:pre [(symbol? tp-name)
         (every? symbol? fields)]} (let [fields (mapv (fn [sym] 
                       (with-meta sym 
                                  (assoc (meta sym) :volatile-mutable true)))
                     fields)]
    
    `(deftype ~tp-name ~fields 
       IMutable
       (-set [~'this ~'k ~'v] 
         (case ~'k 
           ~@(mapcat 
              (fn [x] 
                `[~(keyword (name x)) 
                  (~'set! ~x ~'v)]) 
              fields)) 
         ~'this)
       
       (-set-new [~'this ~'k ~'v] 
         (assert (not (~'k ~'this)) (str ~'k " is already set.")) 
         (case ~'k 
           ~@(mapcat 
              (fn [x] 
                `[~(keyword (name x)) 
                  (~'set! ~x ~'v)]) 
              fields)) 
         ~'this)

       (-fields [~'this] 
         ~(mapv (comp keyword name) fields))

       (-clone [~'this] 
         ~(let [cstr (symbol (str tp-name "."))] 
            `(~cstr ~@fields)))
       
       clojure.lang.ILookup 
       (~'valAt [~'this ~'k ~'default] 
         (case ~'k 
           ~@(mapcat 
               (fn [x] 
                 `[~(keyword (name x)) 
                   ~x]) 
               fields) 
           ~'default))
       (~'valAt [~'this ~'k] 
         (.valAt ~'this ~'k nil))
       ~@protos))))
```

### 6. Extensibility (Multimethods and Protocols)

Functions are often designed to be extensible, allowing new behaviors to be added without modifying existing code.

*   **`defmulti` and `defmethod`:** Used for dispatching behavior based on the type or value of arguments.
*   **Protocols (`defprotocol`, `extend-type`, `defimpl`):** Define interfaces that can be implemented by different types.

**Example: `net.http.common/-write-value` (Multimethod)**

```clojure
(defmulti -write-value
  "writes the string value of the datastructure according to format"
  {:added "0.5"}
  (fn [s format] format))

(defmethod -write-value :edn
  ([s _]
   (pr-str s)))
```

**Example: `std.lib.component/IComponent` (Protocol with `defimpl`)**

```clojure
(defimpl LuceneSearch [type instance]
  :string common/to-string
  :protocols [std.protocol.component/IComponent
              :body {-start impl/start-lucene
                     -stop  impl/stop-lucene}])
```

### 7. `std.lang` DSL Integration

Functions designed for the `std.lang` DSL have specific patterns for cross-platform compatibility and code generation.

*   **`defn.<lang>` and `defmacro.<lang>`:** Functions and macros are defined with a language-specific tag (e.g., `defn.js`, `defmacro.lua`).
*   **Cross-Platform Utilities (`xt.lang.base-lib`):** The `k/` alias is commonly used for `xt.lang.base-lib` functions, which provide platform-agnostic operations.
*   **Code Generation:** Functions often take `grammar` and `mopts` (macro options) as arguments to facilitate code generation.

**Example: `js.blessed.layout/LayoutMain` (React Component in JS DSL)**

```clojure
(defn.js LayoutMain
  "constructs the main page"
  {:added "4.0"}
  ([#{[(:= header  {:menu []
                    :toggle nil
                    :user  nil})
       (:= footer  {:menu []
                    :toggle nil})
       init
       route
       setRoute
       index
       setIndex
       sections
       status
       setStatus
       busy
       setBusy
       notify
       setNotify
       menuWidth
       menuContent
       menuFooter
       menuHide
       console
       consoleHeight]}] ; ... rest of function
```

**Example: `kmi.queue.common/mq-do-key` (Lua DSL Function)**

```clojure
(defn.lua mq-do-key
  "helper function for multi key ops"
  {:added "3.0"}
  ([key f acc]
   (local k-space   (cat key ":_"))
   (local k-pattern (cat k-space ":[^\\:]+$"))
   (local k-partitions (r/scan-regex k-pattern (cat k-space ":*")))
   (k/for:array [[i pfull]  k-partitions]
     (local p (. pfull (sub (+ (len key) 4))))
     (f key p acc))
   (return acc)))
```

### 8. Higher-Order Functions and Function Composition

Functions are often designed to be composed, taking other functions as arguments or returning functions.

*   **`comp`, `partial`:** Used for creating new functions from existing ones.
*   **Threading Macros (`->`, `->>`):** Used for chaining function calls, improving readability of sequential operations.

**Example: `std.lib.transform.apply/wrap-hash-set`**

```clojure
(defn wrap-hash-set
  "allows operations to be performed on sets"
  {:added "3.0"}
  ([f]
   (fn [val datasource]
     (cond (set? val)
           (set (map #(f % datasource) val))

           :else
           (f val datasource)))))
```

### 9. Macros for Abstraction and Code Generation

Macros are used to reduce boilerplate, create DSLs, and generate code at compile time.

*   **`defmacro`:** Standard Clojure macro definition.
*   **`h/template-entries`:** A powerful macro for generating multiple definitions from a template, often used for binding external library functions or constants.

**Example: `std.lib.template/deftemplate` and `h/template-entries`**

```clojure
(deftemplate res-api-tmpl
  ([[sym res-sym config]]
   (let [extra    (count (:args config))
         args     (map :name (:args config))
         default  (:default (first (:args config)))])
   ;; ... generates def form
   ))

(h/template-entries [res-api-tmpl] 
  [[res:exists? res-access-get {:post boolean}]
   [res:set     res-access-set {:args [{:name instance}]}]])
```

This guide provides a deeper insight into the function design principles and patterns prevalent in the `foundation-base` codebase. By understanding and applying these principles, developers can contribute functions that are consistent, robust, and easily integrated into the existing architecture.