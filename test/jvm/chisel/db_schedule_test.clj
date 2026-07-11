(ns jvm.chisel.db-schedule-test
  (:use code.test)
  (:require [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.scan :as scan]
            [jvm.chisel.db.reduce :as red]))

(def ^:private values [10 20 30 40 50 60 70 80])

^{:refer jvm.chisel.db.schedule/demands :added "4.1"}
(fact "demands counts one unit per stage per kind"
  (sched/demands {:stages [{:op :scan} {:op :scan} {:op :hash} {:op :reduce}]})
  => {:scan 2 :hash 1 :aggregate 1})

^{:refer jvm.chisel.db.schedule/feasible? :added "4.1"}
(fact "feasible? checks support and inventory"
  (sched/feasible? {:stages [{:op :scan} {:op :hash} {:op :reduce}]}
                   {:scan 1 :hash 1 :aggregate 1}) => true
  (sched/feasible? {:stages [{:op :scan} {:op :scan}]}
                   {:scan 1}) => false
  (sched/feasible? {:stages [{:op :join}]} {:scan 1}) => false)

^{:refer jvm.chisel.db.schedule/place :added "4.1"}
(fact "place assigns sequential unit ids per kind"
  (sched/place {:stages [{:op :scan} {:op :hash} {:op :reduce}]}
               {:scan 2 :hash 2 :aggregate 2})
  => {:ok? true
      :placement [{:stage 0 :op :scan   :kind :scan      :unit :scan-0}
                  {:stage 1 :op :hash   :kind :hash      :unit :hash-0}
                  {:stage 2 :op :reduce :kind :aggregate :unit :aggregate-0}]}
  (:ok? (sched/place {:stages [{:op :scan} {:op :scan}]} {:scan 1})) => false
  (:ok? (sched/place {:stages [{:op :join}]} {:scan 1})) => false)

^{:refer jvm.chisel.db.schedule/schedule :added "4.1"}
(fact "schedule annotates cost and honors an injected :cost-fn"
  (:cost (sched/schedule {:width 8 :lanes 8 :stages [{:op :scan} {:op :hash} {:op :reduce}]}
                         {:scan 1 :hash 1 :aggregate 1})) => 24
  (:cost (sched/schedule {:width 8 :lanes 8 :stages [{:op :scan}]}
                         {:scan 1} {:cost-fn (constantly 42)})) => 42
  (:ok? (sched/schedule {:width 8 :lanes 8 :stages [{:op :scan} {:op :scan}]}
                        {:scan 1})) => false)

^{:refer jvm.chisel.db.schedule/run-plan :added "4.1"}
(fact "run-plan scan-only reproduces scan-ref"
  (:mask (sched/run-plan {:width 8 :lanes 8 :stages [{:op :scan :preds [[:eq 30]]}]}
                         {:values values :validMask 2r11111111}))
  => (scan/scan-ref values 2r11111111 [[:eq 30]])
  => 2r00000100)

^{:refer jvm.chisel.db.schedule/run-plan :added "4.1"}
(fact "run-plan scan->reduce reproduces reduce-ref over the filtered mask"
  (let [plan {:width 8 :lanes 8
              :stages [{:op :scan :preds [[:gte 20] [:lte 80]]}
                       {:op :reduce :reduce-op :sum}]}
        out  (sched/run-plan plan {:values values :validMask 2r11111111})]
    (:mask out)   => 2r11111110
    (:result out) => 350
    (:result out) => (red/reduce-ref values 2r11111110 :sum 8)))

^{:refer jvm.chisel.db.schedule/run-plan :added "4.1"}
(fact "run-plan bloom stage gates the mask by the probe bit-vector"
  ;; no :bits supplied -> default all-ones -> probe always hits -> mask preserved
  (:mask (sched/run-plan {:width 8 :lanes 8
                          :stages [{:op :bloom-probe :bits-count 16 :ks [0x9E]}]}
                         {:values values :validMask 2r10101010}))
  => 2r10101010
  ;; :bits 0 -> probe always misses -> mask cleared
  (:mask (sched/run-plan {:width 8 :lanes 8
                          :stages [{:op :bloom-probe :bits-count 16 :ks [0x9E]}]}
                         {:values values :validMask 2r10101010 :bits 0}))
  => 0)

^{:refer jvm.chisel.db.schedule/run-plan :added "4.1"}
(fact "run-plan hash stage tags each lane with a bucket"
  (:buckets (sched/run-plan {:width 8 :lanes 8
                             :stages [{:op :hash :buckets 16 :k 0x9E}]}
                            {:values values :validMask 2r11111111}))
  => (mapv #(jvm.chisel.db.hash/hash-ref % 8 0x9E 4) values))
