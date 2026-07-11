(ns jvm.chisel.db-estimate-test
  (:use code.test)
  (:require [jvm.chisel.db.estimate :as est]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.cluster :as cl]))

(def plan-a
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:eq 3]]}
            {:op :reduce :reduce-op :count}]})

(def full-sel-plan
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 0]]}
            {:op :reduce :reduce-op :sum}]})

(def two-pred-plan
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 128] [:lt 192]]}
            {:op :reduce :reduce-op :count}]})

(def bloom-plan
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 0]]}
            {:op :bloom-probe :bits-count 4 :ks [0x9E 0x5A]}
            {:op :reduce :reduce-op :count}]})

(def join-plan
  {:width 8 :lanes 8 :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

^{:refer jvm.chisel.db.estimate/pred-selectivity :added "4.1"}
(fact "predicate selectivity is a fraction of the width-bit domain"
  (est/pred-selectivity 8 [:eq 3])   => (/ 1.0 256)
  (est/pred-selectivity 8 [:neq 3])  => (- 1.0 (/ 1.0 256))
  (est/pred-selectivity 8 [:gte 0])  => 1.0
  (est/pred-selectivity 8 [:lte 255]) => 1.0
  (est/pred-selectivity 8 [:gte 128]) => 0.5
  (est/pred-selectivity 8 [:lt 192]) => 0.75
  ;; clamped at the edges, conservative on unknown ops
  (est/pred-selectivity 8 [:lt 0])   => 0.0
  (est/pred-selectivity 8 [:gte 300]) => 0.0
  (est/pred-selectivity 8 [:bogus 1]) => 1.0)

^{:refer jvm.chisel.db.estimate/cardinalities :added "4.1"}
(fact "cardinalities track how each node narrows the stream"
  (est/cardinalities plan-a) => {0 0.03125 1 1.0}
  (est/cardinalities full-sel-plan) => {0 8.0 1 1.0}
  ;; [:gte 128] (0.5) * [:lt 192] (0.75) = 0.375 of 8 rows
  (est/cardinalities two-pred-plan) => {0 3.0 1 1.0})

^{:refer jvm.chisel.db.estimate/estimate-cost :added "4.1"}
(fact "cost sums ceiled input cardinalities; selective plans bid down"
  (est/estimate-cost plan-a) => 9        ;; 8 + ceil(0.03125)
  (est/estimate-cost two-pred-plan) => 11 ;; 8 + 3
  ;; full selectivity reproduces the default lanes * nodes estimate
  (est/estimate-cost full-sel-plan) => 16
  (est/estimate-cost full-sel-plan) => (sched/estimate-cost full-sel-plan)
  ;; an empty plan costs nothing
  (est/estimate-cost {:width 8 :lanes 8 :stages []}) => 0)

^{:refer jvm.chisel.db.estimate/cardinalities :added "4.1"}
(fact "bloom-probe keeps the false-positive fraction of its input"
  (let [cards (est/cardinalities bloom-plan)]
    ;; 1 - (15/16)^(2*8) of 8 rows ~= 5.15
    (Math/round (* 100.0 (cards 1))) => 515
    (cards 2) => 1.0)
  ;; scan in 8, bloom in 8, reduce in ~5.15 -> 22 < default 24
  (est/estimate-cost bloom-plan) => 22
  (< (est/estimate-cost bloom-plan) (sched/estimate-cost bloom-plan)) => true)

^{:refer jvm.chisel.db.estimate/estimate-cost :added "4.1"}
(fact "join-probe is costed from the build table's domain coverage"
  (let [cards (est/cardinalities join-plan)]
    (cards :b0) => 7.96875              ;; 8 * 255/256
    (cards :bt) => 7.96875
    (cards :p0) => 7.84375              ;; 8 * 251/256
    (cards :r)  => 1.0)
  ;; 8 + 8 + 8 + 8 + ceil(~0.244) = 33 < default 40
  (est/estimate-cost join-plan) => 33
  (< (est/estimate-cost join-plan) (sched/estimate-cost join-plan)) => true)

^{:refer jvm.chisel.db.estimate/estimate-cost :added "4.1"}
(fact "correction factors scale a node's output before it feeds downstream"
  (est/estimate-cost plan-a {}) => 9
  ;; scan out 0.03125 * 128 = 4.0 rows into the reduce -> 8 + 4
  (est/estimate-cost plan-a {:scan 128.0}) => 12
  (est/estimate-cost full-sel-plan {:scan 0.5}) => 12
  ;; a factor on the terminal node scales its own out, nothing downstream
  (est/estimate-cost plan-a {:aggregate 5.0}) => 9)

^{:refer jvm.chisel.db.schedule/schedule :added "4.1"}
(fact "the estimator drops into schedule's :cost-fn seam unchanged"
  (sched/schedule plan-a {:scan 1 :aggregate 1} {:cost-fn est/estimate-cost})
  => {:ok? true
      :placement [{:stage 0 :op :scan :kind :scan :unit :scan-0}
                  {:stage 1 :op :reduce :kind :aggregate :unit :aggregate-0}]
      :cost 9})

^{:refer jvm.chisel.db.cluster/admit :added "4.1"}
(fact "the same :cost-fn seam prices admissions on the cluster"
  (let [r (cl/admit (cl/cluster {:scan 1 :aggregate 1}) :q1 plan-a
                    {:cost-fn est/estimate-cost})]
    (:cost (:admission r)) => 9))
