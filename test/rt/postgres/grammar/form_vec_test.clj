(ns rt.postgres.grammar.form-vec-test
  (:require [rt.postgres.grammar :as g]
            [rt.postgres.grammar.form-vec :refer :all]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres.grammar.form-vec/pg-section-query-pair :added "4.0"}
(fact "converts to a pair expression"
  ^:hidden
  
  (pg-section-query-pair '[a [:neq (+ 1 2)]])
  => '(a (:- "!=") (+ 1 2)))

^{:refer rt.postgres.grammar.form-vec/pg-section-query-set-and :added "4.0"}
(fact "sets up the query string only for and"
  ^:hidden

  (l/with:emit 
   (pg-section-query-set-and '[a 1 :and b 2]
                             g/+grammar+ {}))
  => "(a = 1 AND b = 2)")

^{:refer rt.postgres.grammar.form-vec/pg-section-query-set :added "4.0"}
(fact "sets up the query string"
  ^:hidden

  (l/with:emit
   (pg-section-query-set '[a 1 :or b 2]
                         g/+grammar+ {}))
  => "(a = 1) OR (b = 2)"
  

  (l/with:emit
   (pg-section-query-set '[a 1 :and b 2 :or c 3 d 4]
                         g/+grammar+ {}))
  => "(a = 1 AND b = 2)\nOR (  \n  c = 3\n  AND\n  d = 4)")

^{:refer rt.postgres.grammar.form-vec/pg-section-query-map :added "4.0"}
(fact "query string"
  ^:hidden
  
  (l/with:emit
   (pg-section-query-map {"a" 1} g/+grammar+ {}))
  => "\"a\" = 1")

^{:refer rt.postgres.grammar.form-vec/pg-section-fn :added "4.0"}
(fact "rendering function for a section entry"
  ^:hidden

  (l/with:emit
   (pg-section-fn '#{[a 1 :and b 2 :or c 3 d 4]}
                  g/+grammar+
                  {}))
  => "(a = 1 AND b = 2)\nOR (  \n  c = 3\n  AND\n  d = 4)")

^{:refer rt.postgres.grammar.form-vec/pg-section :added "4.0"}
(fact "rendering function for entire section"
  ^:hidden

  (l/with:emit
   (pg-section '[(js [1 2 3 4 5])]
               g/+grammar+
               {}))
  => "jsonb_build_array(1,2,3,4,5)")

(comment
  (./import))
