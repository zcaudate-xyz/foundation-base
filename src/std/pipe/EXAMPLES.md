# `std.pipe` Examples

This document provides examples of asynchronous pipeline execution patterns using `std.pipe`.

## Task Definition

Tasks are defined as maps (or using `std.task/task`). The main function signature defaults to `[input params env]` (arity 3).

```clojure
(require '[std.task :as task]
         '[std.pipe :refer [pipe]])

(def +my-task+
  (task/task :default "my-task"
             {:main {:fn (fn [input params env]
                           ;; Task logic here
                           (* input 2))}
              :item {:list (fn [env] [1 2 3 4 5])} ;; Default input list
              }))
```

## Execution Patterns

### 1. Parallel Execution

Execute a task in parallel across multiple inputs. Use `:bulk true` and `:parallel true`.

```clojure
(pipe +my-task+
      :list ;; Use the list defined in the task
      {:bulk true
       :parallel true})
;; => {1 2, 2 4, 3 6, 4 8, 5 10}
```

The underlying implementation uses `std.lib.stream.async` primitives to manage concurrency.

### 2. FIFO Execution (Sequential)

Execute a task sequentially (First-In-First-Out). Use `:bulk true` and `:mode :fifo`.

```clojure
(pipe +my-task+
      :list
      {:bulk true
       :mode :fifo})
;; => {1 2, 2 4, 3 6, 4 8, 5 10}
```

This guarantees that items are processed one after another in the order they appear in the input list.

### 3. Task Chaining

Chain multiple tasks together to form a pipeline. The output of one task becomes the input of the next. Pass a vector of tasks to `pipe`.

```clojure
(def +inc-task+
  (task/task :default "inc"
             {:main {:fn (fn [x _ _] (inc x))}}))

(def +double-task+
  (task/task :default "double"
             {:main {:fn (fn [x _ _] (* x 2))}}))

;; Pipeline: input -> inc -> double -> output
(pipe [+inc-task+ +double-task+]
      10)
;; => 22  ((10 + 1) * 2)
```

### 4. Customizing Arguments

You can pass additional arguments to the task function.

```clojure
(def +add-task+
  (task/task :default "add"
             {:main {:fn (fn [x _ _ y] (+ x y))}}))

(pipe +add-task+
      10
      {} ;; params
      {} ;; env
      :args 5) ;; Additional argument 'y'
;; => 15
```

### 5. Bulk Processing with Custom Input

You can provide a custom list of inputs directly instead of using the task's `:list` function.

```clojure
(pipe +my-task+
      [10 20 30] ;; Custom input list
      {:bulk true
       :parallel true})
;; => {10 20, 20 40, 30 60}
```
