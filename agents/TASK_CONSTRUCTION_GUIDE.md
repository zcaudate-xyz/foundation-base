### Plan for Constructing `definvoke :task` Functions

The `definvoke` macro, when used with the `:task` type, defines a structured, executable unit of work within the `foundation-base` project. It's designed for consistency in task definition, execution, and reporting.

Here's a breakdown of its components and how to construct similar tasks:

#### 1. Function Definition and Documentation

*   **Function Name:** Choose a descriptive name for your task function (e.g., `docstrings`, `compile-project`).
*   **Docstring:** Provide a clear, concise docstring explaining the task's purpose, arguments, and expected return value. Include examples where appropriate.
*   **Metadata:** Always include `{:added "version"}` to track when the task was introduced.

    ```clojure
    (definvoke my-new-task
      "A brief description of what this task does.
    
       (my-new-task \"arg1\" {:option true})
       => expected-output"
      {:added "X.Y"}
      [:task ...])
    ```

#### 2. The `:task` Definition

The core of the task is a vector starting with `:task`, followed by a map containing various configuration keys.

```clojure
[:task {:template :code
        :params   {...}
        :main     {...}
        :item     {...}
        :result   {...}}]
```

#### 3. Key Components of the Task Map

Each key within the task map configures a specific aspect of the task:

*   **`:template` (Required)**
    *   **Purpose:** Specifies a base template for the task, providing default behaviors and structures. Common templates include `:code` (for code-related tasks), `:system`, etc.
    *   **Value:** A keyword representing the template (e.g., `:code`).
    *   **Example:** `:template :code`

*   **`:params` (Optional, but highly recommended)**
    *   **Purpose:** Defines parameters for the task's execution, including display options and parallelism.
    *   **Keys:**
        *   `:title` (String): A human-readable title for the task, displayed during execution.
        *   `:parallel` (Boolean): If `true`, task items will be processed in parallel. Defaults to `false`.
        *   `:print` (Map): Controls what output is printed.
            *   `:result` (Boolean): If `true`, prints the final result summary.
            *   `:summary` (Boolean): If `true`, prints a summary of items processed.
    *   **Example:**
        ```clojure
        :params {:title "MY NEW TASK"
                 :parallel true
                 :print {:result true :summary false}}
        ```

*   **`:main` (Required)**
    *   **Purpose:** Defines the primary function or logic that the task will execute. 
    *   **Keys:**
        *   `:fn` (Var or Function): The actual Clojure function to be invoked. This function typically takes arguments that represent the "items" the task operates on.
    *   **Example:** `:main {:fn #'my.namespace/my-processing-function}`

*   **`:item` (Optional)**
    *   **Purpose:** Configures how individual items processed by the task are displayed or handled.
    *   **Keys:**
        *   `:display` (Function): A function that takes an item and returns a formatted string or data structure for display. Often uses `std.lib.template/empty-status` for consistent status messages.
    *   **Example:**
        ```clojure
        :item {:display (comp (template/empty-status :info :none) vec keys)}
        ```

*   **`:result` (Optional)**
    *   **Purpose:** Defines how the overall results of the task are aggregated, transformed, and displayed.
    *   **Keys:**
        *   `:keys` (Map): A map where keys are desired output keys and values are functions to extract/transform data from the raw task results.
        *   `:columns` (Vector or Map): Defines how results should be displayed in a tabular format. Can specify columns and their styling (e.g., `#{:bold}`). Often uses `std.lib.template/code-default-columns`.
    *   **Example:**
        ```clojure
        :result {:keys  {:total-items (comp count keys)
                         :functions (comp vec keys)}
                 :columns (template/code-default-columns :functions #{:bold})}
        ```

#### 4. Generic Template

Here's a generic template you can use as a starting point:

```clojure
(ns my-project.my-tasks
  (:require [std.lib.system.task :refer [definvoke]]
            [std.lib.template :as template]
            [my-project.core :as core] ; Your core logic namespace
            [clojure.string :as str]))

(definvoke my-new-task
  "A concise description of what this task accomplishes.
   
   (my-new-task \"some-input\" {:option-key \"value\"})
   => expected-output-structure"
  {:added "0.1"}
  [:task {:template :code ; Or another appropriate template like :system
          :params   {:title "MY NEW TASK TITLE"
                     :parallel true ; Set to false if order matters or items are interdependent
                     :print {:result true :summary true}}
          :main     {:fn #'core/my-main-processing-function} ; The function that processes each item
          :item     {:display (fn [item] (str "Processing: " item))} ; How to display each item
          :result   {:keys    {:total-items (comp count :items)
                               :processed-count (comp count :processed-items)}
                     :columns (template/code-default-columns :total-items #{:bold})}}])
```

#### 5. Structure of `:main :fn` Input

The function specified by `:main :fn` is the core logic that processes each unit of work within the task. Its input signature generally depends on the task's design, but common patterns emerge:

1.  **Single Item Processing:**
    *   **`[item]`**: The most common scenario. The function receives a single `item` that represents the current unit of work. This `item` could be a file path, a namespace symbol, a data record, or any other data relevant to the task.

2.  **Item with Context/Options:**
    *   **`[item opts]`**: In more complex tasks, the function might also receive an `opts` map. This map typically contains:
        *   **Task-specific parameters:** Any additional configuration or arguments passed to the `definvoke` task itself.
        *   **Runtime context:** Information about the overall task execution, such as the current task configuration, shared resources, or other metadata.

**Key Points:**

*   **Arity:** The function can have different arities (number of arguments) to handle simpler or more complex processing needs. The task system will attempt to call the appropriate arity.
*   **Item Type:** The type of `item` is entirely dependent on the task's purpose and how the task is designed to feed data to the `:main :fn`.
*   **Return Value:** The `:main :fn` should return a value that the task system can collect and process further, especially for the `:result` aggregation.

**Example of a `:main :fn` signature:**

```clojure
(defn my-processing-function
  "Processes a single item with optional context."
  ([item]
   ;; Simple processing, no extra context needed
   (println "Processing item:" item)
   {:processed-item item :status :success})
  ([item {:keys [config-param debug-mode] :as opts}]
   ;; More complex processing with configuration
   (if debug-mode
     (println "Debug processing item:" item "with config:" config-param))
   (if config-param
     {:processed-item item :status :success :config-used config-param}
     {:processed-item item :status :failed :reason "No config"})))
```

#### 6. Key Considerations

*   **Dependencies:** Ensure you `require` necessary namespaces, especially `std.lib.system.task` for `definvoke` and `std.lib.template` for display utilities.
*   **Main Function (`:main :fn`):** The function referenced here should be designed to process a single "item" or a collection of items, depending on whether `:parallel` is true and how the task is invoked.
*   **Result Transformation:** The `:result :keys` and `:result :columns` are powerful for customizing the final output report, making it easy to understand the task's outcome.
*   **Error Handling:** The underlying functions (`:main :fn`) should ideally handle errors gracefully, or the task system will report exceptions.

By following this structure, you can create consistent and well-defined tasks within the `foundation-base` project.