(ns jvm.chisel.db.observe-test
  (:use code.test)
  (:require [jvm.chisel.db.observe :as observe]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.cluster :as cl]))

(def plan-a
  {:width 8 :lanes 8
   :stages [{:op :scan :preds [[:eq 3]]}
            {:op :reduce :reduce-op :count}]})

;; skewed: the value 3 is 4 of 8 rows, not the 1/256 the uniform domain assumes
(def skewed {:values [3 3 5 3 7 8 3 9] :validMask 2r11111111})

(def join-plan
  {:width 8 :lanes 8 :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

(def join-input
  {:sources [{:values [1 26 52 78 104 130 156 0] :validMask 2r11111111}
             {:values [26 104 3 250 7 200 52 11] :validMask 2r11111111}]})

^{:refer jvm.chisel.db.observe/actual-cardinalities :added "4.1"}
(fact "observed cardinalities are popcounts of what actually ran"
  (observe/actual-cardinalities (sched/run-plan plan-a skewed))
  => {0 4 1 1}
  ;; the DAG: 7 build rows, 7 distinct buckets, 8 probe rows, 3 matches
  (observe/actual-cardinalities (sched/run-plan join-plan join-input))
  => {:b0 7 :bt 7 :p0 8 :jp 3 :r 1})

^{:refer jvm.chisel.db.observe/calibration :added "4.1"}
(fact "calibration pairs every estimate with what the run observed"
  (observe/calibration plan-a skewed)
  => {0 {:est 0.03125 :actual 4 :ratio 128.0}
      1 {:est 1.0 :actual 1 :ratio 1.0}}
  ;; a zero estimate has nothing to scale against -> ratio is nil
  (observe/calibration {:width 8 :lanes 8 :stages [{:op :scan :preds [[:lt 0]]}]}
                       skewed)
  => {0 {:est 0.0 :actual 0 :ratio nil}})

^{:refer jvm.chisel.db.observe/correction-factors :added "4.1"}
(fact "selectivity error folds into per-kind factors, charged to its cause"
  ;; 4 of 8 rows vs the 1/256 the uniform domain assumed: (4/8)/(1/256) = 128
  (observe/correction-factors plan-a skewed) => {:scan 128.0}
  (let [factors (observe/correction-factors join-plan join-input)]
    ;; pass-through (:join-build) and collapse (:reduce) nodes earn no factor
    (set (keys factors)) => #{:scan :join}
    ;; the scans were slightly over-estimated (7/8 and 8/8 vs ~0.98 sel);
    ;; the join hit rate was massively under-estimated (0.375 vs ~0.031)
    (< (:scan factors) 1.0) => true
    (> (:join factors) 10.0) => true)
  ;; a zero estimate carries no signal -> no factor
  (observe/correction-factors {:width 8 :lanes 8 :stages [{:op :scan :preds [[:lt 0]]}]}
                              skewed)
  => {})

^{:refer jvm.chisel.db.observe/make-cost-fn :added "4.1" :id test-make-cost-fn-1}
(fact "corrected cost drops into schedule's :cost-fn seam"
  (let [raw       (observe/make-cost-fn {})
        corrected (observe/make-cost-fn {:scan 128.0})]
    (raw plan-a) => 9
    (corrected plan-a) => 12
    (:cost (sched/schedule plan-a {:scan 1 :aggregate 1} {:cost-fn corrected}))
    => 12))

^{:refer jvm.chisel.db.observe/make-cost-fn :added "4.1" :id test-make-cost-fn-2}
(fact "the same corrected cost prices cluster admissions"
  (let [corrected (observe/make-cost-fn (observe/correction-factors plan-a skewed))
        r (cl/admit (cl/cluster {:scan 1 :aggregate 1}) :q1 plan-a
                    {:cost-fn corrected})]
    (:cost (:admission r)) => 12))
