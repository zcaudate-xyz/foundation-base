# `std.task` Guide

`std.task` defines a protocol and structure for executing operations, managing their inputs/outputs, and formatting their results. It is the engine behind `code.manage` commands.

## Core Concepts

- **Task**: An invokable object with a defined lifecycle.
- **Template**: A set of defaults for a class of tasks (e.g., `:namespace`, `:file`).
- **Process**: The underlying execution logic (handling parallel/serial execution, argument parsing).

## Usage

### Defining a Task

```clojure
(require '[std.task :as task])

;; Simple task
(task/deftask my-task
  {:template :default
   :main {:fn +}
   :doc "Adds numbers"})

(my-task 1 2) ;; => 3
```

### Scenarios

#### 1. Defining a Custom Task Template

If you have a set of tasks that share common behavior (e.g., they all operate on database records), you can define a template.

```clojure
;; Define defaults for :db-task
(defmethod task/task-defaults :db-task
  [_]
  {:main {:arglists '([id] [id options])}
   :result {:columns [{:key :id :align :left}
                      {:key :status :align :center}]}})

;; Create a task using this template
(task/deftask get-user
  {:template :db-task
   :main {:fn (fn [id] {:id id :status :active})}
   :doc "Fetches a user"})
```

#### 2. Parallel Processing

Tasks can be configured to run in parallel, which is useful for operations over many items (like files or namespaces).

```clojure
(task/deftask process-files
  {:template :default
   :params {:parallel true
            :title "PROCESSING FILES"}
   :main {:fn (fn [file] (slurp file))} ;; Simplified example
   })
```

When invoked with a collection, the task runner (via `std.task.process`) will utilize threads if `:parallel` is set.

#### 3. Customizing Output/Result

You can control how the result is displayed, which is crucial for CLI tools.

```clojure
(task/deftask analyze-data
  {:template :default
   :main {:fn (fn [x] {:score (rand-int 100) :name x})}

   ;; Define output columns
   :result {:keys {:count count} ;; Summary key
            :columns [{:key :name :length 20 :align :left}
                      {:key :score :length 10 :align :right :color #{:bold}}]}})

;; Invocation might look like:
;; (analyze-data ["A" "B"])
;; => Prints table with Name and Score
```

#### 4. Item Processing Configuration

The `:item` key allows transformation of individual inputs or outputs.

```clojure
(task/deftask list-sorted
  {:template :default
   :main {:fn identity}
   :item {:post (comp vec sort)} ;; Sort the output of the main function
   })
```

#### 5. Handling Command Line Arguments

If you are building a CLI entry point (like `code.manage/-main`), `process-ns-args` helps parse arguments.

```clojure
(defn -main [& args]
  (let [opts (task/process-ns-args args)]
    ;; opts will look like {:only "my.ns" :verbose true}
    ;; if called as: ... :only "my.ns" :verbose
    ...))
```
