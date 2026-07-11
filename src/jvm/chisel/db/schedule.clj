(ns jvm.chisel.db.schedule
  "The hardware workflow scheduler: turns a *logical* operator graph (data, produced
   by the host) into a *physical* placement on a fixed pool of reusable operator
   blocks, and executes a pure-Clojure reference of the plan.

   This is deliberately decoupled from the datapath (`jvm.chisel.db.pipeline`):

     host                       chip
     graph  ──▶  logical plan  ──▶  place  ──▶  schedule  ──▶  composed module
                 (data only)      (which units) (order, cost)

   * `place` / `schedule` are pure functions over the plan and the resource
     inventory; `schedule` accepts a `:cost-fn`, the seam where a learned cost
     model (\"AI inside the chip\") plugs in without touching the datapath.
   * `run-plan` is the golden reference: it runs a plan over concrete lane input
     by composing the existing `*-ref` models, so both tests and
     `jvm.chisel.db.pipeline/pipeline-ref` share one source of truth.

   The scheduling logic itself is pure data; requiring the operator namespaces is
   only to reuse their reference models (single source of truth, no drift)."
  (:require [jvm.chisel.db.scan :as scan]
            [jvm.chisel.db.reduce :as red]
            [jvm.chisel.db.hash :as h]
            [jvm.chisel.db.bloom :as bloom]))

;; ---------------------------------------------------------------------------
;; plan shape
;;
;;   {:width 8 :lanes 8
;;    :stages [{:op :scan        :preds [[:gte 20] [:lte 50]]}
;;             {:op :bloom-probe :bits-count 64 :ks [0x9E 0x5A]}
;;             {:op :hash        :buckets 16 :k 0x9E}
;;             {:op :reduce      :reduce-op :sum}]}
;;
;; runtime input: {:values [...] :validMask int :bits int}  (:bits only for bloom)
;; ---------------------------------------------------------------------------

(def op->kind
  "Resource kind consumed by each supported logical operator."
  {:scan        :scan
   :bloom-probe :bloom
   :hash        :hash
   :reduce      :aggregate})

(def supported-ops (set (keys op->kind)))

(defn demands
  "Per-kind unit counts a plan needs (linear slice: one unit per stage)."
  [plan]
  (frequencies (map (comp op->kind :op) (:stages plan))))

(defn feasible?
  "True iff every stage op is known and, for every kind, demand <= inventory."
  [plan inventory]
  (and (every? #(supported-ops (:op %)) (:stages plan))
       (let [d (demands plan)]
         (every? (fn [[k n]] (<= n (get inventory k 0))) d))))

(defn place
  "Assign a concrete physical unit id to each stage, in plan order, drawing from
   `inventory`. Returns {:ok? bool :placement [...] :reason ...}. Each placement
   entry is {:stage i :op op :kind kind :unit :<kind>-<n>}."
  [plan inventory]
  (cond
    (not (every? #(supported-ops (:op %)) (:stages plan)))
    {:ok? false :reason "unsupported operator in plan"}

    (not (feasible? plan inventory))
    {:ok? false :reason (str "insufficient resources: need " (demands plan)
                             " have " inventory)}

    :else
    (let [[placement _]
          (reduce (fn [[acc used] [i s]]
                    (let [kind (op->kind (:op s))
                          n    (get used kind 0)]
                      [(conj acc {:stage i :op (:op s) :kind kind
                                  :unit (keyword (str (name kind) "-" n))})
                       (update used kind (fnil inc 0))]))
                  [[] {}]
                  (map-indexed vector (:stages plan)))]
      {:ok? true :placement placement})))

(defn estimate-cost
  "Default cycle estimate: one `lanes` pass per stage. Overridable via
   `schedule`'s `:cost-fn`."
  [plan]
  (* (long (:lanes plan)) (count (:stages plan))))

(defn schedule
  "Place `plan` onto `inventory` and annotate with a `:cost`. `opts` may carry
   `:cost-fn` (fn [plan] -> number) to replace the default estimate — the
   injection point for a learned cost model. On infeasible plans the placement
   failure map is returned (no `:cost`)."
  ([plan inventory] (schedule plan inventory {}))
  ([plan inventory {:keys [cost-fn]}]
   (let [p (place plan inventory)]
     (if (:ok? p)
       (assoc p :cost ((or cost-fn estimate-cost) plan))
       p))))

;; ---------------------------------------------------------------------------
;; reference executor
;; ---------------------------------------------------------------------------

(defn- log2-of [n] (long (/ (Math/log n) (Math/log 2))))

(defn- bit? [m i] (pos? (bit-and m (bit-shift-left 1 i))))

(defn run-plan
  "Execute `plan` over `input` {:values [...] :validMask int [:bits int]} by
   composing the operator reference models. Returns a state map:
   {:values [...] :mask int :buckets [...] :result x} where `:buckets` and
   `:result` are present only when a :hash / :reduce stage ran."
  [plan input]
  (let [width (:width plan)
        v0    (vec (:values input))]
    (reduce
     (fn [st s]
       (let [v (:values st) m (:mask st) n (count v)]
         (case (:op s)
           :scan
           (assoc st :mask (scan/scan-ref v m (:preds s)))

           :bloom-probe
           (let [{:keys [bits-count ks]} s
                 bits (get input :bits (dec (bit-shift-left 1 bits-count)))
                 m2   (reduce (fn [acc i]
                                (if (and (bit? m i)
                                         (bloom/bloom-probe-ref (v i) bits width bits-count ks))
                                  (bit-or acc (bit-shift-left 1 i))
                                  acc))
                              0 (range n))]
             (assoc st :mask m2))

           :hash
           (let [{:keys [buckets k] :or {k 0x9E}} s
                 logB (log2-of buckets)
                 bks  (mapv #(h/hash-ref % width k logB) v)]
             (assoc st :buckets bks))

           :reduce
           (assoc st :result (red/reduce-ref v m (:reduce-op s) width)))))
     {:values v0 :mask (:validMask input)}
     (:stages plan))))
