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
