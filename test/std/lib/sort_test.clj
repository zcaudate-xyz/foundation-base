(ns std.lib.sort-test
  (:require [std.lib.sort :refer :all])
  (:use code.test))

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

^{:refer std.lib.sort/find-cycle :added "4.0"}
(fact "finds a cycle path in a directed graph"

  (find-cycle {:a #{:b}, :b #{:a}})
  => (fn [c] (and (= (count c) 3)
                  (= (first c) (last c))
                  (= (set c) #{:a :b})))

  (find-cycle {:a #{:b}, :b #{:c}, :c #{:b}})
  => (fn [c] (and (= (count c) 3)
                  (= (first c) (last c))
                  (= (set c) #{:b :c})
                  (every? (fn [[a b]] (get-in {:a #{:b} :b #{:c} :c #{:b}} [a b]))
                          (partition 2 1 c))))

  (find-cycle {:a #{:b}, :b #{}})
  => nil)

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
  => (throws)

  (try (topological-sort {:a #{:b}, :b #{:a}})
       (catch clojure.lang.ExceptionInfo e
         (:cycle (ex-data e))))
  => (fn [c] (and (= (count c) 3)
                  (= (first c) (last c))
                  (= (set c) #{:a :b}))))

^{:refer std.lib.sort/topological-sort-order-by-deps :added "4.0"}
(fact "sorts topological sort by dependency size"

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
