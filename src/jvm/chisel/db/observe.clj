(ns jvm.chisel.db.observe
  "The feedback half of the scheduler loop — the \"monitor execution, reconfigure
   the next query\" arc from the essay, built from pieces that already exist.

     estimate/cardinalities ─▶ schedule/run-plan ─▶ actual-cardinalities
              ▲                                           │
              │                                  correction-factors
              │                                           │
              └──────────── make-cost-fn ◀────────────────┘

   The cost model estimates per-node cardinalities from a uniform-domain
   assumption; real data is skewed, so the estimate is systematically off.
   One observed run of a query shape yields per-kind correction factors —
   plain data, persistable — that price the *next* query of that shape
   honestly. The factors map is deliberately the shape a learned model will
   later fill: same key space (resource kinds), same multiplication point
   (`estimate/estimate-cost`'s 2-arity), no control-plane changes.

   Nothing here touches hardware; `run-plan` is the golden reference, so the
   observations are exactly what the composed datapath would produce."
  (:require [jvm.chisel.db.estimate :as est]
            [jvm.chisel.db.schedule :as sched]))

(defn- value-card
  "Observed output cardinality of one env value: relation → popcount of :mask,
   hash table → popcount of :valid, scalar reduce result → 1."
  [v]
  (cond
    (contains? v :mask)   (Long/bitCount (:mask v))
    (contains? v :valid)  (Long/bitCount (:valid v))
    (contains? v :result) 1
    :else                 0))

(defn actual-cardinalities
  "Per-node observed output cardinality, read from a `run-plan` result's :env."
  [run]
  (into {} (map (fn [[id v]] [id (value-card v)]) (:env run))))

(defn calibration
  "Per-node estimate vs observation for one (plan, input) run:
   {node-id {:est e :actual a :ratio a/e}} — the human-facing report (\"you
   predicted 0.03 rows here, 4 came out\"). `:ratio` is nil where the estimate
   is zero. `correction-factors` is the machine-facing fold of the same run."
  [plan input]
  (let [estimated (est/cardinalities plan)
        observed  (actual-cardinalities (sched/run-plan plan input))]
    (into {}
          (map (fn [[id e]]
                 (let [a (get observed id 0)]
                   [id {:est e :actual a :ratio (when (pos? e) (/ a e))}]))
               estimated))))

(def ^:private filtering-ops
  "Operators whose output cardinality is genuinely estimated. Pass-through
   nodes (:hash, :join-build) only carry their input's error and :reduce's
   output is exact by definition, so none of them earn a factor."
  #{:scan :bloom-probe :join-probe})

(defn- source-counts
  "Actual rows per input source: popcount of each validMask."
  [input]
  (if-let [srcs (:sources input)]
    (mapv #(Long/bitCount (:validMask %)) srcs)
    [(Long/bitCount (:validMask input))]))

(defn correction-factors
  "Fold one observed run into per-kind multipliers. For each filtering node the
   factor is its selectivity error, (actual-out/actual-in) ÷ (est-out/est-in):
   charging the correction to the node that caused it, so an upstream
   mis-estimate is not re-charged to the pass-through nodes below it. Factors
   are averaged per resource kind; nodes with a zero estimate or zero observed
   input contribute nothing. Feed the result to `make-cost-fn`."
  [plan input]
  (let [lanes   (double (:lanes plan))
        srcs    (source-counts input)
        est-out (est/cardinalities plan)
        obs-out (actual-cardinalities (sched/run-plan plan input))
        in-of   (fn [est? node]
                  (let [ref (first (:inputs node))]
                    (if (and (vector? ref) (= :src (first ref)))
                      (if est? lanes (double (nth srcs (second ref))))
                      ((if est? est-out obs-out) ref))))
        per-node
        (keep (fn [node]
                (when (filtering-ops (:op node))
                  (let [ei (in-of true node)  eo (est-out (:id node))
                        ai (in-of false node) ao (obs-out (:id node))]
                    (when (and (pos? ei) (pos? eo) (pos? ai))
                      [(sched/op->kind (:op node)) (/ (/ ao ai) (/ eo ei))]))))
              (sched/plan->nodes plan))]
    (into {}
          (map (fn [[kind rs]] [kind (/ (reduce + (map second rs)) (count rs))]))
          (group-by first per-node))))

(defn make-cost-fn
  "Wrap correction `factors` into a drop-in `:cost-fn` for `schedule/schedule`
   or `cluster/admit`. `(make-cost-fn {})` is the uncorrected estimate."
  [factors]
  (fn [plan] (est/estimate-cost plan factors)))
