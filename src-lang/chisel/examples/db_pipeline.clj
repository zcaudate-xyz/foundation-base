(ns chisel.examples.db-pipeline
  "End-to-end: a logical relational graph is placed by the hardware workflow
   scheduler onto a pool of reusable operator blocks, then elaborated as one
   composed module (`jvm.chisel.db.pipeline`).

   The host produced the logical plan (SQL/graph already parsed, rewritten and
   costed); the chip only sees the operator graph and the resource inventory."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.cluster :as cl]
            [jvm.chisel.db.estimate :as est]
            [jvm.chisel.db.observe :as observe]
            [jvm.chisel.db.pg :as pg]
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

;; multi-query admission: allocate cores, don't synthesise hardware
(comment
  (def chip (cl/cluster {:scan 2 :hash 2 :aggregate 1}))

  ;; q1 (scan -> hash -> sum) pins scan-0, hash-0, aggregate-0
  (def r1 (cl/admit chip :q1 group-sum-plan))
  (:placement (:admission r1))

  ;; q2 cannot reuse q1's units; it draws scan-1 + hash-1
  (def probe-plan {:width 8 :lanes 8
                   :stages [{:op :scan :preds [[:gte 1]]}
                            {:op :hash :buckets 16}]})
  (def r2 (cl/admit (:cluster r1) :q2 probe-plan))
  (cl/free-counts (:cluster r2))  ;; => {:scan 0, :hash 0, :aggregate 0}
  (cl/utilization (:cluster r2))  ;; => {:scan 1.0, :hash 1.0, :aggregate 1.0}

  ;; q3 is refused until something finishes — nothing is allocated
  (cl/admit (:cluster r2) :q3 probe-plan)
  ;; => {:ok? false :reason "insufficient free resources: ..."}

  ;; q1 finishes; its blocks return to the pool; q3 now fits and reuses them
  (def chip2 (cl/release (:cluster r2) :q1))
  (cl/free-counts chip2)          ;; => {:scan 1, :hash 1, :aggregate 1}
  (:admission (cl/admit chip2 :q3 probe-plan)))

;; cardinality-driven costing: the non-ML precursor of the learned model,
;; plugged into the :cost-fn seam both control planes already expose
(comment
  ;; where does the plan actually narrow?
  (est/cardinalities group-sum-plan)
  ;; => {0 <rows after gte20/lte80>, 1 <same>, 2 1.0}

  ;; selective plans bid the default lanes*nodes estimate down
  (sched/schedule group-sum-plan inventory {:cost-fn est/estimate-cost})
  (sched/estimate-cost group-sum-plan)   ;; default: 24 (8 lanes x 3 nodes)

  ;; same seam on the admission control plane
  (:admission (cl/admit (cl/cluster inventory) :q1 group-sum-plan
                        {:cost-fn est/estimate-cost})))

;; the feedback loop: run, observe, correct, price the next query
(comment
  (def data {:values [10 20 30 40 50 60 70 80] :validMask 2r11111111})

  ;; estimate said 2.33 rows out of the scan; 7 actually passed (ratio ~3x)
  (observe/calibration group-sum-plan data)

  ;; fold the run into per-kind factors (plain data — persist it between queries)
  (def factors (observe/correction-factors group-sum-plan data))
  ;; => {:scan 2.9997907512031805}   (hash/reduce earn nothing: pass-through/exact)

  ;; the next query of this shape is priced from observed reality:
  ;;   default lanes*nodes = 24, raw estimate = 14, corrected = 22
  (def learned-cost (observe/make-cost-fn factors))
  (sched/schedule group-sum-plan inventory {:cost-fn learned-cost})
  (:admission (cl/admit (cl/cluster inventory) :q2 group-sum-plan
                        {:cost-fn learned-cost})))

;; postgres seam: EXPLAIN (FORMAT JSON) plan trees translate straight in
(comment
  (pg/plan->chip-plan
   {"Node Type" "Aggregate" "Strategy" "Plain" "Output" ["count(*)"]
    "Plans" [{"Node Type" "Seq Scan" "Relation Name" "orders"
              "Filter" "(amount >= 100)"}]})
  ;; => {:ok? true
  ;;     :plan {:width 32 :lanes 8 :sources 1
  ;;            :nodes [{:id :n0 :op :scan :inputs [[:src 0]] :preds [[:gte 100]]}
  ;;                    {:id :n1 :op :reduce :inputs [:n0] :reduce-op :count}]}}
  ;; unsupported nodes (Index Scan, Sort, grouped agg, outer joins) refuse the
  ;; plan with a reason; what translates flows through schedule/admit/run-plan.
  )
