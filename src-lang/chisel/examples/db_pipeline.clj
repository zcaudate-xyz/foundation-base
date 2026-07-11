(ns chisel.examples.db-pipeline
  "End-to-end: a logical relational graph is placed by the hardware workflow
   scheduler onto a pool of reusable operator blocks, then elaborated as one
   composed module (`jvm.chisel.db.pipeline`).

   The host produced the logical plan (SQL/graph already parsed, rewritten and
   costed); the chip only sees the operator graph and the resource inventory."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.pipeline :as pipe]))

;; a logical plan: scan/filter -> hash (group key) -> sum
(def group-sum-plan
  {:width 8 :lanes 8
   :stages [{:op :scan   :preds [[:gte 20] [:lte 80]]}
            {:op :hash   :buckets 16 :k 0x9E}
            {:op :reduce :reduce-op :sum}]})

;; the chip's floorplan: counts of each reusable block
(def inventory
  {:scan 16 :bloom 8 :hash 32 :aggregate 32})

(comment
  ;; 1. place + schedule (pure, deterministic)
  (sched/schedule group-sum-plan inventory)
  ;; => {:ok? true
  ;;     :placement [{:stage 0 :op :scan   :kind :scan      :unit :scan-0}
  ;;                 {:stage 1 :op :hash   :kind :hash      :unit :hash-0}
  ;;                 {:stage 2 :op :reduce :kind :aggregate :unit :aggregate-0}]
  ;;     :cost 24}

  ;; 2. reference result over concrete lane input
  (sched/run-plan group-sum-plan
                  {:values [10 20 30 40 50 60 70 80] :validMask 2r11111111})
  ;; mask keeps lanes 1..7 (>=20 and <=80), sum = 20+30+40+50+60+70+80 = 350

  ;; 3. elaborate the composed datapath
  (println (ch/emit-firrtl
            (pipe/pipeline-module (assoc group-sum-plan :name "GroupSum8"))))
  (println (ch/emit-system-verilog
            (pipe/pipeline-module (assoc group-sum-plan :name "GroupSum8")))))
