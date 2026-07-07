(ns documentation.std-lib-sort
  (:require [std.lib.sort :refer :all])
  (:use code.test))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Overview"}]]

"`std.lib.sort` provides graph and hierarchy sorting utilities beyond clojure.core."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Hierarchical sorting"}]]

"A hierarchy maps each node to the set of all its descendants. `hierarchical-top` finds the root node, and `hierarchical-sort` prunes redundant edges so that only direct children remain."

(fact "find the top of a hierarchy"
  (hierarchical-top
   {1 #{2 3 4 5 6}
    2 #{3 5 6}
    3 #{5 6}
    4 #{}
    5 #{6}
    6 #{}})
  => 1)

(fact "prune a hierarchy to direct children only"
  (hierarchical-sort {1 #{2 3 4 5 6}
                      2 #{3 5 6}
                      3 #{5 6}
                      4 #{}
                      5 #{6}
                      6 #{}})
  => {1 #{4 2}
      2 #{3}
      3 #{5}
      4 #{}
      5 #{6}
      6 #{}})

[[:section {:title "Topological sorting"}]]

"`topological-sort` orders nodes so that every dependency appears before the node that depends on it. Use `topological-top` to find nodes with no dependents, and `find-cycle` to diagnose cyclic graphs."

(fact "find nodes with no dependents"
  (topological-top {:a #{} :b #{:a}})
  => #{:b})

(fact "sort a dependency graph"
  (topological-sort {:a #{:b :c},
                     :b #{:d :e},
                     :c #{:e :f},
                     :d #{},
                     :e #{:f},
                     :f nil})
  => [:f :d :e :b :c :a])

(fact "detect a cycle in a graph"
  (find-cycle {:a #{:b}, :b #{:a}})
  => (fn [c] (and (= (count c) 3)
                  (= (first c) (last c))
                  (= (set c) #{:a :b})))

  (find-cycle {:a #{:b}, :b #{}})
  => nil)

(fact "topological sort throws on cycles"
  (topological-sort {:a #{:b}, :b #{:a}})
  => (throws))

[[:section {:title "Stable ordering by dependency count"}]]

"`topological-sort-order-by-deps` takes an already topologically sorted list and reorders nodes at each level by how many dependencies they have, producing a stable, predictable ordering."

(fact "order nodes at each level by dependency count"
  (let [g {:a #{:b :c}
           :b #{:d :e}
           :c #{:e :f}
           :d #{}
           :e #{:f}
           :f nil}
        sorted (topological-sort g)]
    (topological-sort-order-by-deps g sorted))
  => [:d :f :e :b :c :a])

[[:section {:title "End-to-end: build and validate a task pipeline"}]]

"Combining the functions above lets you validate and order a task graph. First check for cycles, then topologically sort, and finally refine the order by dependency count."

(fact "validate, sort, and refine a task graph"
  (let [tasks {:compile #{:lint :test}
               :lint    #{}
               :test    #{:lint}
               :deploy  #{:compile}}]
    (find-cycle tasks)
    => nil

    (topological-sort tasks)
    => [:lint :test :compile :deploy]

    (topological-sort-order-by-deps tasks (topological-sort tasks))
    => [:lint :test :compile :deploy]))

[[:chapter {:title "API" :link "std.lib.sort"}]]

[[:api {:namespace "std.lib.sort"}]]

;; BEGIN merged documentation: plans/slop/summary/std_lib_sort_summary.md
;; sha256: 327e8302a921a7d6298905d636a399aafbf152311c60062233eea04cd496e68f
[[:chapter {:title "std.lib.sort: A Comprehensive Summary" :link "merged-plans-slop-summary-std-lib-sort-summary-md"}]]
"## std.lib.sort: A Comprehensive Summary\n\nThe `std.lib.sort` namespace provides specialized sorting algorithms for hierarchical and directed graph structures. It offers functions to identify top-level nodes in a hierarchy, prune hierarchies into directed graphs, and perform topological sorting to determine dependency order. This module is crucial for managing complex relationships and dependencies within the `foundation-base` project, such as module loading, build processes, or data flow analysis.\n\n**Key Features and Concepts:**\n\n1.  **Hierarchical Sorting:**\n    *   **`hierarchical-top [idx]`**: Identifies the top-most node in a hierarchy represented by a map `idx` where keys are nodes and values are sets of their direct or indirect descendants. The top node is one that is not a descendant of any other node.\n    *   **`hierarchical-sort [idx]`**: Transforms a hierarchy of descendants into a directed graph where edges represent direct dependencies. It prunes indirect dependencies, resulting in a cleaner representation of immediate relationships.\n\n2.  **Topological Sorting:**\n    *   **`topological-top [g]`**: Identifies nodes in a directed graph `g` that have no incoming dependencies (i.e., no other nodes depend on them). These are the starting points for a topological sort.\n    *   **`topological-sort [g & [l s]]`**: Sorts a directed graph `g` into a linear ordering of its nodes such that for every directed edge from node A to node B, A comes before B in the ordering.\n        *   It uses Kahn's algorithm (or a similar iterative approach).\n        *   It detects and throws an exception if the graph contains a circular dependency.\n        *   The `l` (sorted list) and `s` (set of nodes with no incoming edges) arguments are used for the recursive/iterative process.\n\n**Usage and Importance:**\n\nThe `std.lib.sort` module is essential for managing complex dependencies and ordering tasks within the `foundation-base` project. Its applications include:\n\n*   **Module Loading and Initialization**: Determining the correct order in which modules or components should be loaded to satisfy their dependencies.\n*   **Build System Dependencies**: Ordering compilation or build steps based on file or project dependencies.\n*   **Task Scheduling**: Sequencing tasks in a workflow where some tasks must complete before others can start.\n*   **Data Flow Analysis**: Understanding the flow of data through a system by ordering processing steps.\n*   **Graph Algorithms**: Providing foundational algorithms for working with directed acyclic graphs (DAGs).\n\nBy offering robust algorithms for hierarchical and topological sorting, `std.lib.sort` significantly enhances the `foundation-base` project's ability to manage complex interdependencies, ensuring correct execution order and detecting circular dependencies, which is vital for its multi-language development ecosystem.\n"
;; END merged documentation: plans/slop/summary/std_lib_sort_summary.md
