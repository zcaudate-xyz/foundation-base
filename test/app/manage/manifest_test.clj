(ns app.manage.manifest-test
  (:require [app.manage.manifest :as m])
  (:use code.test))
(def P {:id "one" :routes ["/one"] :models {"rows" {"operation" "list" "operation-spec" {"write" false}}}})
^{:refer app.manage.manifest/compile-manifest :id stable}
(fact "declaration order cannot change manifest bytes"
  (= (pr-str (m/compile-manifest [P (assoc P :id "two" :routes ["/two"])]))
     (pr-str (m/compile-manifest [(assoc P :id "two" :routes ["/two"]) P]))) => true
  (get (m/compile-manifest [P]) "version") => 1)
^{:refer app.manage.manifest/compile-manifest :id invalid}
(fact "invalid declarations fail compilation"
  (mapv (fn [pages] (try (m/compile-manifest pages) false (catch Exception _ true)))
        [[P P] [P (assoc P :id "two")] [(assoc P :order ["missing"])]
         [(assoc-in P [:models "rows" "operation-spec" "write"] true)]])
  => [true true true true])
