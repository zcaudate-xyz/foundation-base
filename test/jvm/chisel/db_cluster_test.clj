(ns jvm.chisel.db-cluster-test
  (:use code.test)
  (:require [jvm.chisel.db.cluster :as cl]
            [jvm.chisel.db.schedule :as sched]))

(def inv {:scan 2 :hash 1 :join 1 :aggregate 1})

(def plan-a
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:eq 3]]}
            {:op :reduce :reduce-op :count}]})

(def plan-b
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:gte 1]]}
            {:op :hash :buckets 16}]})

(def join-plan
  {:width 8 :lanes 8 :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

^{:refer jvm.chisel.db.cluster/cluster :added "4.1"}
(fact "a fresh cluster holds everything free and nothing used"
  (cl/free-counts (cl/cluster inv)) => inv
  (cl/used-units (cl/cluster inv)) => #{}
  (cl/utilization (cl/cluster inv))
  => {:scan 0.0 :hash 0.0 :join 0.0 :aggregate 0.0})

^{:refer jvm.chisel.db.cluster/admit :added "4.1" :id test-admit-1}
(fact "admitting two queries packs them onto disjoint units"
  (let [r1 (cl/admit (cl/cluster inv) :q1 plan-a)]
    (:ok? r1) => true
    (:placement (:admission r1))
    => [{:stage 0 :op :scan :kind :scan :unit :scan-0}
        {:stage 1 :op :reduce :kind :aggregate :unit :aggregate-0}]
    (:cost (:admission r1)) => 16
    (let [r2 (cl/admit (:cluster r1) :q2 plan-b)]
      (:ok? r2) => true
      (:placement (:admission r2))
      => [{:stage 0 :op :scan :kind :scan :unit :scan-1}
          {:stage 1 :op :hash :kind :hash :unit :hash-0}]
      (cl/used-units (:cluster r2))
      => #{:scan-0 :aggregate-0 :scan-1 :hash-0}
      (cl/free-counts (:cluster r2))
      => {:scan 0 :hash 0 :join 1 :aggregate 0})))

^{:refer jvm.chisel.db.cluster/admit :added "4.1" :id test-admit-2}
(fact "a contended query is refused and nothing leaks"
  (let [r1 (cl/admit (cl/cluster {:scan 1 :aggregate 1}) :q1 plan-a)
        r2 (cl/admit (:cluster r1) :q2 plan-a)]
    (:ok? r2) => false
    (boolean (re-find #"insufficient" (:reason r2))) => true
    ;; the failed admission allocated nothing
    (cl/used-units (:cluster r1)) => #{:scan-0 :aggregate-0}
    (cl/free-counts (:cluster r1)) => {:scan 0 :aggregate 0}))

^{:refer jvm.chisel.db.cluster/release :added "4.1"}
(fact "releasing frees the blocks, the next query reuses them"
  (let [cl1 (:cluster (cl/admit (cl/cluster {:scan 1 :aggregate 1}) :q1 plan-a))
        cl2 (cl/release cl1 :q1)
        r3  (cl/admit cl2 :q2 plan-a)]
    (cl/free-counts cl2) => {:scan 1 :aggregate 1}
    (:placement (:admission r3))
    => [{:stage 0 :op :scan :kind :scan :unit :scan-0}
        {:stage 1 :op :reduce :kind :aggregate :unit :aggregate-0}]
    ;; release of an unknown id is a no-op
    (cl/release cl2 :nope) => cl2))

^{:refer jvm.chisel.db.cluster/admit :added "4.1" :id test-admit-3}
(fact "duplicate ids and unsupported operators are refused"
  (let [cl1 (:cluster (cl/admit (cl/cluster inv) :q1 plan-a))]
    (cl/admit cl1 :q1 plan-a)
    => {:ok? false :reason "query-id already admitted"}
    (cl/admit cl1 :q2 {:width 8 :lanes 8 :stages [{:op :fft}]})
    => {:ok? false :reason "unsupported operator in plan"}))

^{:refer jvm.chisel.db.cluster/admit :added "4.1" :id test-admit-4}
(fact "a DAG join plan packs scan/join/aggregate units in node order"
  (let [r (cl/admit (cl/cluster {:scan 2 :join 2 :aggregate 1}) :q1 join-plan)]
    (:ok? r) => true
    (:placement (:admission r))
    => [{:stage 0 :op :scan       :kind :scan      :unit :scan-0}
        {:stage 1 :op :join-build :kind :join      :unit :join-0}
        {:stage 2 :op :scan       :kind :scan      :unit :scan-1}
        {:stage 3 :op :join-probe :kind :join      :unit :join-1}
        {:stage 4 :op :reduce     :kind :aggregate :unit :aggregate-0}]
    (:cost (:admission r)) => 40))

^{:refer jvm.chisel.db.cluster/utilization :added "4.1"}
(fact "utilization reflects the admitted share of each kind"
  (let [r (cl/admit (cl/cluster {:scan 2 :aggregate 2}) :q1 plan-a)]
    (cl/utilization (:cluster r)) => {:scan 0.5 :aggregate 0.5}))

^{:refer jvm.chisel.db.cluster/admit :added "4.1" :id test-admit-5}
(fact "a custom :cost-fn replaces the default estimate at admission"
  (let [r (cl/admit (cl/cluster inv) :q1 plan-a {:cost-fn (constantly 3)})]
    (:cost (:admission r)) => 3
    (:cost (:admission (cl/admit (cl/cluster inv) :q1 plan-a))) => 16))
