(ns jvm.chisel.db-pg-test
  (:use code.test)
  (:require [jvm.chisel.db.pg :as pg]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.observe :as observe]))

(def lane-data {:values [50 100 200 99 100 700 3 100] :validMask 2r11111111})

^{:refer jvm.chisel.db.pg/sql-preds :added "4.1" :id test-sql-preds-1}
(fact "sql-preds lowers Postgres filter strings to chip predicates"
  (pg/sql-preds "(amount >= 100)") => {:ok? true :preds [[:gte 100]]}
  (:preds (pg/sql-preds "(x = 1)"))  => [[:eq 1]]
  (:preds (pg/sql-preds "(x <> 5)")) => [[:neq 5]]
  (:preds (pg/sql-preds "(x < 5)"))  => [[:lt 5]]
  (:preds (pg/sql-preds "(x <= 5)")) => [[:lte 5]]
  (:preds (pg/sql-preds "(x > 5)"))  => [[:gt 5]]
  ;; AND-chains, redundant parens
  (:preds (pg/sql-preds "((amount >= 100) AND (amount <= 500))"))
  => [[:gte 100] [:lte 500]]
  ;; a constant on the left flips the operator
  (:preds (pg/sql-preds "(100 < amount)")) => [[:gt 100]]
  ;; qualified columns and integer casts pass through
  (:preds (pg/sql-preds "(o.cust_id >= 2)")) => [[:gte 2]]
  (:preds (pg/sql-preds "(price >= 100::numeric)")) => [[:gte 100]])

^{:refer jvm.chisel.db.pg/sql-preds :added "4.1" :id test-sql-preds-2}
(fact "unlowerable filters refuse with a reason"
  (:ok? (pg/sql-preds "((a = 1) OR (b = 2))")) => false
  (:ok? (pg/sql-preds "((amount * 2) > 100)")) => false
  (:ok? (pg/sql-preds "(region = 'EU'::text)")) => false
  (:ok? (pg/sql-preds "(a = b)")) => false)

^{:refer jvm.chisel.db.pg/plan->chip-plan :added "4.1" :id test-plan->chip-plan-1}
(fact "a bare Seq Scan translates to a one-node scan plan that runs"
  (let [r (pg/explain-file->chip-plan "test/jvm/chisel/db/pgplans/seq_scan_filter.json")]
    (:ok? r) => true
    (:plan r) => {:width 32 :lanes 8 :sources 1
                  :nodes [{:id :n0 :op :scan :inputs [[:src 0]]
                           :preds [[:gte 100] [:lte 500]]}]}
    (:actuals r) => [nil]                       ; no ANALYZE, no actuals
    ;; lanes 1,2,4,7 hold values in [100,500]
    (:mask (sched/run-plan (:plan r) lane-data)) => 2r10010110))

^{:refer jvm.chisel.db.pg/plan->chip-plan :added "4.1" :id test-plan->chip-plan-2}
(fact "Aggregate over Seq Scan becomes scan -> reduce"
  (let [r (pg/explain-file->chip-plan "test/jvm/chisel/db/pgplans/agg_scan_count.json")]
    (:plan r) => {:width 32 :lanes 8 :sources 1
                  :nodes [{:id :n0 :op :scan :inputs [[:src 0]] :preds [[:gte 100]]}
                          {:id :n1 :op :reduce :inputs [:n0] :reduce-op :count}]}
    (:result (sched/run-plan (:plan r) lane-data)) => 5))

^{:refer jvm.chisel.db.pg/plan->chip-plan :added "4.1" :id test-plan->chip-plan-3}
(fact "an inner Hash Join lowers to scan/scan/build/probe/reduce"
  (let [r (pg/explain-file->chip-plan "test/jvm/chisel/db/pgplans/hash_join_count.json"
                                      {:width 8})
        plan (:plan r)]
    plan => {:width 8 :lanes 8 :sources 2
             :nodes [{:id :n0 :op :scan :inputs [[:src 0]] :preds [[:gte 2]]}
                     {:id :n1 :op :scan :inputs [[:src 1]] :preds [[:lte 250]]}
                     {:id :n2 :op :join-build :inputs [:n1] :buckets 16 :k 0x9E}
                     {:id :n3 :op :join-probe :inputs [:n0 :n2]}
                     {:id :n4 :op :reduce :inputs [:n3] :reduce-op :count}]}
    ;; probe subtree is source 0, build subtree source 1
    (sched/demands plan) => {:scan 2 :join 2 :aggregate 1}
    (:ok? (sched/place plan {:scan 2 :join 2 :aggregate 1})) => true))

^{:refer jvm.chisel.db.pg/plan->chip-plan :added "4.1" :id test-plan->chip-plan-4}
(fact "chip-observed cardinalities reconcile with EXPLAIN ANALYZE actuals"
  (let [r (pg/explain-file->chip-plan "test/jvm/chisel/db/pgplans/hash_join_count.json"
                                      {:width 8})
        run (sched/run-plan
             (:plan r)
             {:sources [{:values [1 26 52 156 5 7 9 11] :validMask 2r11111111}
                        {:values [1 26 52 78 104 130 156 182] :validMask 2r11111111}]})]
    (:result run) => 3
    (:actuals r) => [7 8 8 3 1]
    (map (observe/actual-cardinalities run) [:n0 :n1 :n2 :n3 :n4])
    => (:actuals r)))

^{:refer jvm.chisel.db.pg/plan->chip-plan :added "4.1" :id test-plan->chip-plan-5}
(fact "unsupported plans refuse the whole offload, naming the cause"
  (let [r (pg/explain-file->chip-plan "test/jvm/chisel/db/pgplans/index_scan.json")]
    (:ok? r) => false
    (:reason r) => "unsupported node type: Index Scan")
  ;; grouped aggregation (no hash-aggregate block)
  (:ok? (pg/plan->chip-plan
         {"Node Type" "Aggregate" "Strategy" "Hashed" "Group Key" ["region_id"]
          "Output" ["region_id" "count(*)"] "Plans" [{"Node Type" "Seq Scan"}]}))
  => false
  ;; non-inner joins
  (:reason (pg/plan->chip-plan
            {"Node Type" "Hash Join" "Join Type" "Left" "Hash Cond" "(a.id = b.id)"
             "Plans" [{"Node Type" "Seq Scan"}
                      {"Node Type" "Hash" "Plans" [{"Node Type" "Seq Scan"}]}]}))
  => "join type not supported: Left"
  ;; composite join keys
  (boolean (re-find #"single col = col"
                    (:reason (pg/plan->chip-plan
                              {"Node Type" "Hash Join" "Join Type" "Inner"
                               "Hash Cond" "((a.id = b.id) AND (a.k = b.k))"
                               "Plans" [{"Node Type" "Seq Scan"}
                                        {"Node Type" "Hash" "Plans" [{"Node Type" "Seq Scan"}]}]}))))
  => true
  ;; unlowerable filter surfaces through the plan
  (boolean (re-find #"filter not lowerable"
                    (:reason (pg/plan->chip-plan
                              {"Node Type" "Seq Scan" "Filter" "(region = 'EU'::text)"}))))
  => true)
