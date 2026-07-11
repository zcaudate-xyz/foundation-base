(ns jvm.chisel.db-dag-test
  (:use code.test)
  (:require [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.scan :as scan]
            [jvm.chisel.db.join :as join]))

(def join-plan
  {:width 8 :lanes 8 :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

^{:refer jvm.chisel.db.schedule/plan->nodes :added "4.1"}
(fact "plan->nodes lifts a linear :stages plan to a chain"
  (sched/plan->nodes {:stages [{:op :scan :preds [[:eq 3]]}
                               {:op :reduce :reduce-op :count}]})
  => [{:op :scan :preds [[:eq 3]] :id 0 :inputs [[:src 0]]}
      {:op :reduce :reduce-op :count :id 1 :inputs [0]}]
  (sched/plan->nodes join-plan) => (:nodes join-plan)
  (sched/plan->sources join-plan) => 2
  (sched/plan->sources {:stages []}) => 1)

^{:refer jvm.chisel.db.schedule/demands :added "4.1"}
(fact "demands counts join nodes as :join units"
  (sched/demands join-plan) => {:scan 2 :join 2 :aggregate 1})

^{:refer jvm.chisel.db.schedule/place :added "4.1"}
(fact "place assigns units per kind across the DAG, in node order"
  (sched/place join-plan {:scan 2 :join 2 :aggregate 1})
  => {:ok? true
      :placement [{:stage 0 :op :scan       :kind :scan      :unit :scan-0}
                  {:stage 1 :op :join-build :kind :join      :unit :join-0}
                  {:stage 2 :op :scan       :kind :scan      :unit :scan-1}
                  {:stage 3 :op :join-probe :kind :join      :unit :join-1}
                  {:stage 4 :op :reduce     :kind :aggregate :unit :aggregate-0}]}
  (:ok? (sched/place join-plan {:scan 2 :join 0 :aggregate 1})) => false)

^{:refer jvm.chisel.db.schedule/schedule :added "4.1"}
(fact "schedule cost is lanes x node count for the DAG"
  (:cost (sched/schedule join-plan {:scan 2 :join 2 :aggregate 1})) => 40)

^{:refer jvm.chisel.db.schedule/run-plan :added "4.1"}
(fact "run-plan executes a join DAG: scan -> build, scan -> probe -> count"
  (let [src0 [3 7 11 42 99 200 0 1]
        src1 [3 60 7 255 42 11 5 200]
        out  (sched/run-plan join-plan
                             {:sources [{:values src0 :validMask 2r11111111}
                                        {:values src1 :validMask 2r11111111}]})
        ;; hand-rolled oracle
        build-mask (scan/scan-ref src0 2r11111111 [[:gte 1]])
        table      (join/join-build-ref src0 build-mask 8 16 0x9E)
        probe-m0   (scan/scan-ref src1 2r11111111 [[:lte 250]])
        probe-mask (reduce (fn [acc i]
                             (if (and (bit-test probe-m0 i)
                                      (join/join-probe-ref (src1 i) table 8 16 0x9E))
                               (bit-set acc i)
                               acc))
                           0 (range 8))]
    (:mask out)            => probe-mask
    (:result out)          => (Long/bitCount probe-mask)
    (get-in out [:env :bt]) => table))
