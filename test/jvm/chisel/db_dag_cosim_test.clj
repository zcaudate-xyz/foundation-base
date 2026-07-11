(ns jvm.chisel.db-dag-cosim-test
  "End-to-end value co-simulation of a join DAG through the composed
   `jvm.chisel.db.pipeline` datapath against the `run-plan` oracle, using
   `jvm.chisel.testing` (Verilator). Skipped unless `verilator` is on PATH.

   The plan forks over two sources — scan -> join-build on source 0,
   scan -> join-probe -> reduce :count on source 1 — so it exercises the
   last-wins table through the full datapath, not just the standalone operator."
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel.testing :as ct]
            [jvm.chisel.db.pipeline :as pipe]
            [jvm.chisel.db.schedule :as sched]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(def ^:private lanes 8)
(def ^:private width 8)

(def join-plan
  {:width width :lanes lanes :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

(defn- rand-vals []
  (vec (repeatedly lanes #(rand-int (bit-shift-left 1 width)))))

(defn- plan-consts
  "Flat list of scan-node constants in global c-index order (matches the
   pipeline's c0..cK assignment)."
  [nodes]
  (vec (mapcat (fn [n] (when (= :scan (:op n)) (map second (:preds n)))) nodes)))

(defn- input-cases
  "Two-source input rows: {:sources [{build} {probe}]}. Edge cases first, then
   random (colliding keys are likely with k=0x9E, which is the point)."
  []
  (concat
   [{:sources [{:values [3 7 11 42 99 200 0 1] :validMask 0xFF}
               {:values [3 60 7 255 42 11 5 200] :validMask 0xFF}]}
    {:sources [{:values (vec (range lanes)) :validMask 0x00}
               {:values (vec (range lanes)) :validMask 0xFF}]}
    {:sources [{:values (vec (range lanes)) :validMask 0xFF}
               {:values (vec (range lanes)) :validMask 0x00}]}
    {:sources [{:values [1 1 1 1 1 1 1 1] :validMask 0xFF}
               {:values [1 1 2 2 3 3 4 4] :validMask 0xAA}]}]
   (repeatedly 6
               (fn [] {:sources [{:values (rand-vals) :validMask (rand-int 256)}
                                 {:values (rand-vals) :validMask (rand-int 256)}]}))))

^{:refer jvm.chisel.db.pipeline/pipeline-module :added "4.1"}
(fact "join DAG co-sim: matchMask and match count equal run-plan over both sources"
  (ct/simulate
   (pipe/pipeline-module (assoc join-plan :name "JoinDagCo"))
   (fn [ctx]
     (let [{:keys [port poke expect step]} ctx
           consts (plan-consts (:nodes join-plan))]
       (doseq [input (input-cases)
               :let [[s0 s1] (:sources input)
                     oracle (sched/run-plan join-plan input)]]
         (ct/poke-vec! ctx "values" (:values s0))
         (poke (port "validMask") (long (:validMask s0)))
         (ct/poke-vec! ctx "values2" (:values s1))
         (poke (port "validMask2") (long (:validMask s1)))
         (doseq [[n c] (map-indexed vector consts)]
           (poke (port (str "c" n)) (long c)))
         (step)
         (expect (port "matchMask") (long (:mask oracle)))
         (expect (port "result") (long (:result oracle)))))))
  => nil)
