(ns documentation.std-task
  (:require [std.task :refer :all])
  (:use code.test))

[[:hero {:title "std.task"
         :subtitle "process and bulk task execution"
         :lead "`std.task` is a higher-level standard library family in foundation-base. This page explains when to use it, how it fits internally, and where to find the API surface."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Use this layer when application or tooling code needs the behavior described by the page title without reaching directly into implementation namespaces. The top-level namespace is the starting point; subnamespaces expose more focused building blocks."

[[:chapter {:title "How to use it" :link "usage"}]]

"Require the top-level namespace for common workflows, then move to subnamespaces when you need a lower-level primitive. Existing tests under `test/std/task` and `test/std/task_test.clj` are the best executable examples for edge cases."

(fact "create a task"
  (task? (task :namespace "list-interns" ns-interns))
  => true)

(fact "define a task with deftask"
  (macroexpand-1
   '(deftask -list-aliases-
      {:template :namespace
       :main clojure.core/ns-aliases
       :item {:post (comp vec sort keys)}
       :doc "returns all aliases"}))
  => '(def -list-aliases-
        (std.task/task
         :namespace "-list-aliases-"
         {:template :namespace,
          :main clojure.core/ns-aliases,
          :item {:post (comp vec sort keys)},
          :doc "returns all aliases"})))

(fact "parse CLI args"
  (process-ns-args [":only" "foo"])
  => {:ns 'foo}

  (process-ns-args [":verbose" ":timeout" "100"])
  => {:verbose true :timeout 100})

[[:chapter {:title "Internal usage" :link "internal"}]]

"This library family is used across source, tests, generated examples, and docs tooling. During detailed documentation passes, collect concrete usage with `code.manage/find-usages` and `code.manage/locate-code`, then keep only high-signal examples in the page narrative."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.task"}]]
[[:api {:namespace "std.task.process"}]]
[[:api {:namespace "std.task.bulk"}]]

;; BEGIN merged documentation: guides/std.task.md
;; sha256: 6356388ac4e8ecef9548c0d84fde20b4fd80948fd8780e96af3bc8a45f960444
[[:chapter {:title "std.task Guide" :link "merged-guides-std-task-md"}]]

"`std.task` defines a protocol and structure for executing operations, managing their inputs/outputs, and formatting their results. It is the engine behind `code.manage` commands."

[[:section {:title "Core Concepts" :link "merged-guides-std-task-md-core-concepts"}]]

"- **Task**: An invokable object with a defined lifecycle.\n- **Template**: A set of defaults for a class of tasks (e.g., `:namespace`, `:file`).\n- **Process**: The underlying execution logic (handling parallel/serial execution, argument parsing)."

[[:section {:title "Usage" :link "merged-guides-std-task-md-usage"}]]

[[:subsection {:title "Defining a Task" :link "merged-guides-std-task-md-defining-a-task"}]]

[[:code {:lang "clojure"} "(require '[std.task :as task])\n\n;; Simple task\n(task/deftask my-task\n  {:template :default\n   :main {:fn +}\n   :doc \"Adds numbers\"})\n\n(my-task 1 2) ;; => 3"]]

[[:subsection {:title "Scenarios" :link "merged-guides-std-task-md-scenarios"}]]

[[:subsubsection {:title "1. Defining a Custom Task Template" :link "merged-guides-std-task-md-1-defining-a-custom-task-template"}]]

"If you have a set of tasks that share common behavior (e.g., they all operate on database records), you can define a template."

[[:code {:lang "clojure"} ";; Define defaults for :db-task\n(defmethod task/task-defaults :db-task\n  [_]\n  {:main {:arglists '([id] [id options])}\n   :result {:columns [{:key :id :align :left}\n                      {:key :status :align :center}]}})\n\n;; Create a task using this template\n(task/deftask get-user\n  {:template :db-task\n   :main {:fn (fn [id] {:id id :status :active})}\n   :doc \"Fetches a user\"})"]]

[[:subsubsection {:title "2. Parallel Processing" :link "merged-guides-std-task-md-2-parallel-processing"}]]

"Tasks can be configured to run in parallel, which is useful for operations over many items (like files or namespaces)."

[[:code {:lang "clojure"} "(task/deftask process-files\n  {:template :default\n   :params {:parallel true\n            :title \"PROCESSING FILES\"}\n   :main {:fn (fn [file] (slurp file))} ;; Simplified example\n   })"]]

"When invoked with a collection, the task runner (via `std.task.process`) will utilize threads if `:parallel` is set."

[[:subsubsection {:title "3. Customizing Output/Result" :link "merged-guides-std-task-md-3-customizing-output-result"}]]

"You can control how the result is displayed, which is crucial for CLI tools."

[[:code {:lang "clojure"} "(task/deftask analyze-data\n  {:template :default\n   :main {:fn (fn [x] {:score (rand-int 100) :name x})}\n\n   ;; Define output columns\n   :result {:keys {:count count} ;; Summary key\n            :columns [{:key :name :length 20 :align :left}\n                      {:key :score :length 10 :align :right :color #{:bold}}]}})\n\n;; Invocation might look like:\n;; (analyze-data [\"A\" \"B\"])\n;; => Prints table with Name and Score"]]

[[:subsubsection {:title "4. Item Processing Configuration" :link "merged-guides-std-task-md-4-item-processing-configuration"}]]

"The `:item` key allows transformation of individual inputs or outputs."

[[:code {:lang "clojure"} "(task/deftask list-sorted\n  {:template :default\n   :main {:fn identity}\n   :item {:post (comp vec sort)} ;; Sort the output of the main function\n   })"]]

[[:subsubsection {:title "5. Handling Command Line Arguments" :link "merged-guides-std-task-md-5-handling-command-line-arguments"}]]

"If you are building a CLI entry point (like `code.manage/-main`), `process-ns-args` helps parse arguments."

[[:code {:lang "clojure"} "(defn -main [& args]\n  (let [opts (task/process-ns-args args)]\n    ;; opts will look like {:only \"my.ns\" :verbose true}\n    ;; if called as: ... :only \"my.ns\" :verbose\n    ...))"]]
;; END merged documentation: guides/std.task.md

;; BEGIN merged documentation: plans/slop/summary/std_task_summary.md
;; sha256: 8aa3acaa80eaaa5ecc94a18085bbf708bdffdf5a85d319b1752df3eda7cdd56b
[[:chapter {:title "std.task: A Comprehensive Summary" :link "merged-plans-slop-summary-std-task-summary-md"}]]

"The `std.task` module in `foundation-base` provides a framework for defining, executing, and managing tasks, particularly focusing on bulk operations and processing items in a structured manner. It offers utilities for handling task inputs, executing functions with transformations, and presenting results, warnings, errors, and summaries in a user-friendly format."

"The module is organized into two main sub-namespaces:"

[[:section {:title "std.task.bulk" :link "merged-plans-slop-summary-std-task-summary-md-std-task-bulk"}]]

"This namespace provides the core logic for performing bulk operations, processing items, and presenting the results. It's designed to handle collections of inputs, execute a given function for each, and then aggregate and display the outcomes."

"*   **`bulk-display [index-len input-len]`**: Constructs display options for bulk output, defining column widths, colors, and alignment for index, input, data, and time.\n*   **`bulk-process-item [f context params lookup env args]`**: Processes a single item within a bulk operation. It executes the function `f` with the item's input, captures exceptions, measures execution time, and optionally prints item-specific output.\n*   **`bulk-items-parallel [f inputs context params lookup env args]`**: Processes a collection of inputs in parallel using `pmap`.\n*   **`bulk-items-single [f inputs context params lookup env args]`**: Processes a collection of inputs sequentially.\n*   **`bulk-items [task f inputs params lookup env args]`**: The main function for orchestrating item processing. It handles input shuffling (if `random` is true), calculates display lengths, and dispatches to either parallel or single-threaded processing based on `parallel` parameter. It also prints a subtitle for the items section.\n*   **`bulk-warnings [params items]`**: Filters processed items for warnings (status `:warn`) and optionally prints them in a formatted column.\n*   **`bulk-errors [params items]`**: Filters processed items for errors (status `:critical` or `:error`) and optionally prints them in a formatted column.\n*   **`prepare-columns [columns outputs]`**: Prepares column definitions for printing, dynamically calculating column lengths based on output data if not explicitly provided.\n*   **`bulk-results [task params items]`**: Filters and sorts processed items to present the final results. It applies ignore functions, formats output based on task definitions, and optionally prints results in a formatted table.\n*   **`bulk-summary [task params items results warnings errors elapsed]`**: Generates and optionally prints a summary of the bulk operation, including counts of items, results, warnings, errors, and cumulative/elapsed times. It can also aggregate custom metrics defined in the task.\n*   **`bulk-package [task bundle return package]`**: Packages the results of the bulk operation into a specified format (e.g., `:all`, `:results`, `:summary`, `:items`) and structure (e.g., `:vector`, `:map`).\n*   **`bulk [task f inputs params lookup env & args]`**: The top-level function for executing a bulk task. It orchestrates the entire process: printing titles, processing items, collecting warnings/errors/results, generating a summary, and packaging the final output."

[[:section {:title "std.task.process" :link "merged-plans-slop-summary-std-task-summary-md-std-task-process"}]]

"This namespace provides utilities for constructing and invoking tasks, including handling function arity, input selection, and execution wrapping."

"*   **`*interrupt*`**: A dynamic var (atom) used as a flag to signal task interruption.\n*   **`main-function [func count]`**: Creates a wrapper function that adapts a given `func` to a standardized task execution signature (taking `input`, `params`, `lookup`, `env`, and `args`). It handles different arities of the original function.\n*   **`select-filter [selector id]`**: A flexible filtering mechanism that matches an `id` against various `selector` types (functions, vars, strings, symbols, keywords, regex, sets, forms, vectors).\n*   **`select-inputs [task lookup env selector]`**: Selects inputs for a task based on a `selector`. If `selector` is `:all`, it uses the task's `:item :list` function to get all possible inputs. Otherwise, it filters the list of inputs using `select-filter`.\n*   **`wrap-execute [f task]`**: Wraps a task's execution function `f` with pre- and post-processing steps defined in the task's `:item` configuration. It also handles converting results to `std.lib.result` format for bulk operations.\n*   **`wrap-input [f task]`**: Wraps a task's execution function `f` to handle different input types. If the input is a selector (keyword, vector, set, form), it triggers a bulk operation using `bulk/bulk`. If the input is `:list`, it returns the list of all possible inputs. Otherwise, it executes `f` with the single input.\n*   **`task-inputs [task & args]`**: Constructs the complete set of inputs (`input`, `params`, `lookup`, `env`) for a task based on its configuration and provided arguments.\n*   **`invoke [task & args]`**: The primary function for executing a task. It prepares the task's inputs, wraps the main function with execution and input handling logic, and then executes it. It also manages the `*interrupt*` flag."

"**Overall Importance:**"

"The `std.task` module is crucial for building automated workflows, command-line tools, and data processing pipelines within the `foundation-base` project. It provides:"

"*   **Structured Task Definition:** Allows tasks to be defined with clear configurations for input listing, execution logic, pre/post-processing, and result presentation.\n*   **Efficient Bulk Operations:** Facilitates processing large collections of data or performing repetitive actions, with options for parallel execution and detailed output.\n*   **User-Friendly Output:** Generates formatted tables for results, warnings, and errors, along with comprehensive summaries, enhancing the user experience for CLI tools.\n*   **Extensibility:** The modular design with configurable functions and wrappers allows for easy customization and extension of task behavior.\n*   **Robust Error Handling:** Integrates with `std.lib.result` to capture and report execution outcomes, including exceptions."

"By offering a robust and flexible task management framework, `std.task` significantly contributes to the `foundation-base` project's ability to automate complex software engineering tasks and manage its multi-language development ecosystem effectively."
;; END merged documentation: plans/slop/summary/std_task_summary.md
