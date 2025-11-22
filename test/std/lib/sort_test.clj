(ns std.lib.sort-test
  (:use code.test)
  (:require [std.lib.sort :refer :all]))

^{:refer std.lib.sort/hierarchical-top :added "3.0"}
(fact "find the top node for the hierarchy of descendants"

  (hierarchical-top
   {1 #{2 3 4 5 6}
    2 #{3 5 6}
    3 #{5 6}
    4 #{}
    5 #{6}
    6 #{}}) => 1)

^{:refer std.lib.sort/hierarchical-sort :added "3.0"}
(fact "prunes a hierarchy of descendants into a directed graph"

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

^{:refer std.lib.sort/topological-top :added "3.0"}
(fact "nodes that have no other nodes that are dependent on them"
  (topological-top {:a #{} :b #{:a}})
  => #{:b})

^{:refer std.lib.sort/topological-sort :added "3.0"}
(fact "sorts a directed graph into its dependency order"

  (topological-sort {:a #{:b :c},
                     :b #{:d :e},
                     :c #{:e :f},
                     :d #{},
                     :e #{:f},
                     :f nil})
  => [:f :d :e :b :c :a]

  (topological-sort {:a #{:b},
                     :b #{:a}})
  => (throws))

^{:refer std.lib.sort/topological-sort-order-by-deps :added "4.0"}
(fact "sorts topological sort by dependency size"
  ^:hidden
  
  (let [g {:a #{:b :c}
           :b #{:d :e}
           :c #{:e :f}
           :d #{}
           :e #{:f}
           :f nil}
        sorted (topological-sort g)]
    (topological-sort-order-by-deps g sorted))
  => [:d :f :e :b :c :a]

  (let [g {:a #{:c}
           :b #{:c}
           :c #{}}
        sorted (topological-sort g)]
    (topological-sort-order-by-deps g sorted))
  => [:c :a :b])
