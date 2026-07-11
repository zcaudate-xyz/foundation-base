(ns jvm.chisel.db.estimate
  "Cardinality-driven cost estimation: the hand-written precursor of the
   learned cost model (the \"AI inside the chip\"). Pure data in, number out,
   so it drops straight into the `:cost-fn` seam of `schedule/schedule` and
   `cluster/admit` — neither control plane changes, and a learned model can
   later replace this one function.

   The model is deliberately textbook:
     * every value lives in the width-bit domain D = 2^width, uniform;
     * a predicate keeps a fraction of the domain (:eq 1/D, :gte c → (D-c)/D, …);
     * multiple predicates multiply (independence assumption);
     * :bloom-probe keeps the false-positive fraction of a filter loaded with
       the estimated input rows;
     * :join-probe keeps probe-rows × min(1, build-rows/D) — the fraction of
       the key domain present in the build table;
     * :hash and :join-build pass rows through; :reduce emits one row.

   Cost unit = row-cycles: the sum over nodes of the (ceiled) *input*
   cardinality — what the work costs one pass wide. For a full-selectivity
   linear plan this equals `schedule/estimate-cost` (lanes × nodes), so a
   selective plan can only ever bid the default estimate down."
  (:require [jvm.chisel.db.schedule :as sched]))

(defn pred-selectivity
  "Fraction of the width-bit domain one predicate keeps, assuming a uniform
   domain. Unknown ops are conservative (1.0 — keep everything)."
  [width [op c]]
  (let [dom (Math/pow 2.0 width)]
    (min 1.0
         (max 0.0
              (case op
                :eq  (/ 1.0 dom)
                :neq (- 1.0 (/ 1.0 dom))
                :lt  (/ c dom)
                :lte (/ (inc c) dom)
                :gt  (/ (- (dec dom) c) dom)
                :gte (/ (- dom c) dom)
                1.0)))))

(defn- bloom-sel
  "False-positive rate of a 2^bits-count-bit filter with (count ks) hash
   functions after n inserted keys: 1 − (1 − 2^-bits)^(k·n)."
  [bits-count ks n]
  (- 1.0 (Math/pow (- 1.0 (Math/pow 2.0 (- bits-count)))
                   (* (count ks) n))))

(defn- src? [ref] (and (vector? ref) (= :src (first ref))))

(defn- node-flows
  "One topological walk of the plan; returns {node-id {:in c :out c}}.
   Sources are taken at `:lanes` rows."
  [plan]
  (let [width (:width plan)
        lanes (double (:lanes plan))
        dom   (Math/pow 2.0 width)]
    (reduce
     (fn [env node]
       (let [card-of (fn [ref] (if (src? ref) lanes (:out (env ref))))
             ins     (:inputs node)
             in0     (card-of (first ins))
             out     (case (:op node)
                       :scan        (* in0 (reduce (fn [s p]
                                                     (* s (pred-selectivity width p)))
                                                   1.0 (:preds node)))
                       :bloom-probe (* in0 (bloom-sel (:bits-count node) (:ks node) in0))
                       :hash        in0
                       :join-build  in0
                       :join-probe  (* in0 (min 1.0 (/ (card-of (second ins)) dom)))
                       :reduce      1.0
                       in0)]
         (assoc env (:id node) {:in in0 :out out})))
     {} (sched/plan->nodes plan))))

(defn cardinalities
  "Estimated output cardinality per node id — the inspectable intermediate a
   scheduler (or a human) uses to see where the plan actually narrows."
  [plan]
  (into {} (map (fn [[id flow]] [id (:out flow)]) (node-flows plan))))

(defn estimate-cost
  "Drop-in `:cost-fn`: row-cycles = Σ ceil(input cardinality) over all nodes.
   Full-selectivity linear plans reproduce `schedule/estimate-cost`."
  [plan]
  (long (reduce (fn [acc flow] (+ acc (Math/ceil (:in flow))))
                0.0 (vals (node-flows plan)))))
