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
            [jvm.chisel.db.bloom :as bloom]
            [jvm.chisel.db.join :as join]))

;; ---------------------------------------------------------------------------
;; plan shape
;;
;; Linear (special case of the DAG form; normalized by `plan->nodes`):
;;   {:width 8 :lanes 8
;;    :stages [{:op :scan :preds [[:gte 20] [:lte 50]]}
;;             {:op :bloom-probe :bits-count 64 :ks [0x9E 0x5A]}
;;             {:op :hash :buckets 16 :k 0x9E}
;;             {:op :reduce :reduce-op :sum}]}
;;
;; DAG (topological node vector; inputs are [:src i] or another node's :id):
;;   {:width 8 :lanes 8 :sources 2
;;    :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
;;            {:id :bt :op :join-build :inputs [:b0]      :buckets 16 :k 0x9E}
;;            {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
;;            {:id :jp :op :join-probe :inputs [:p0 :bt]}
;;            {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]}
;;
;; runtime input: {:values [...] :validMask int [:bits int]}              (1 source)
;;          or    {:sources [{:values [...] :validMask int} …] [:bits int]}
;; ---------------------------------------------------------------------------

(def op->kind
  "Resource kind consumed by each supported logical operator."
  {:scan        :scan
   :bloom-probe :bloom
   :hash        :hash
   :reduce      :aggregate
   :join-build  :join
   :join-probe  :join})

(def supported-ops (set (keys op->kind)))

;; normalization --------------------------------------------------------------

(defn plan->nodes
  "Normalize a plan to a topological node vector. A plan with `:nodes` is used
   as-is; a linear `:stages` plan is lifted to a chain: node 0 reads source 0,
   node i reads node i-1."
  [plan]
  (if-let [nodes (:nodes plan)]
    (vec nodes)
    (vec (map-indexed
          (fn [i s] (assoc s :id i :inputs (if (zero? i) [[:src 0]] [(dec i)])))
          (:stages plan)))))

(defn plan->sources
  "Number of input relations a plan consumes (`:sources` int or vector, default 1)."
  [plan]
  (let [s (:sources plan)]
    (cond (nil? s) 1 (integer? s) s :else (count s))))

;; placement ------------------------------------------------------------------

(defn demands
  "Per-kind unit counts a plan needs (one unit per node)."
  [plan]
  (frequencies (map (comp op->kind :op) (plan->nodes plan))))

(defn feasible?
  "True iff every node op is known and, for every kind, demand <= inventory."
  [plan inventory]
  (and (every? #(supported-ops (:op %)) (plan->nodes plan))
       (let [d (demands plan)]
         (every? (fn [[k n]] (<= n (get inventory k 0))) d))))

(defn place
  "Assign a concrete physical unit id to each node, in plan order, drawing from
   `inventory`. Returns {:ok? bool :placement [...] :reason ...}. Each placement
   entry is {:stage i :op op :kind kind :unit :<kind>-<n>} where `i` is the node's
   index in the (normalized) node vector."
  [plan inventory]
  (let [nodes (plan->nodes plan)]
    (cond
      (not (every? #(supported-ops (:op %)) nodes))
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
                    (map-indexed vector nodes))]
        {:ok? true :placement placement}))))

(defn estimate-cost
  "Default cycle estimate: one `lanes` pass per node. Overridable via
   `schedule`'s `:cost-fn`."
  [plan]
  (* (long (:lanes plan)) (count (plan->nodes plan))))

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

(defn- input-sources
  "Per-source relation inputs {:values v :mask m} from the runtime input map."
  [input]
  (if-let [srcs (:sources input)]
    (mapv (fn [s] {:values (vec (:values s)) :mask (:validMask s)}) srcs)
    [{:values (vec (:values input)) :mask (:validMask input)}]))

(defn- resolve-in
  "Resolve one node input: [:src i] -> source relation, otherwise a node id in `env`."
  [env srcs ref]
  (if (and (vector? ref) (= :src (first ref)))
    (nth srcs (second ref))
    (env ref)))

(defn- eval-node
  "Evaluate one node against its resolved inputs (vector). Returns the env value:
   a relation {:values :mask [:buckets]}, a table {:valid :keys}, or {:result x}."
  [width bits node in]
  (let [rel0 (nth in 0)]
    (case (:op node)
      :scan
      (assoc rel0 :mask (scan/scan-ref (:values rel0) (:mask rel0) (:preds node)))

      :bloom-probe
      (let [{:keys [bits-count ks]} node
            v (:values rel0) m (:mask rel0) n (count v)
            b  (or bits (dec (bit-shift-left 1 bits-count)))
            m2 (reduce (fn [acc i]
                         (if (and (bit? m i)
                                  (bloom/bloom-probe-ref (v i) b width bits-count ks))
                           (bit-or acc (bit-shift-left 1 i))
                           acc))
                       0 (range n))]
        (assoc rel0 :mask m2))

      :hash
      (let [{:keys [buckets k] :or {k 0x9E}} node
            logB (log2-of buckets)
            bks  (mapv #(h/hash-ref % width k logB) (:values rel0))]
        (assoc rel0 :buckets bks))

      :join-build
      (join/join-build-ref (:values rel0) (:mask rel0)
                           width (:buckets node) (:k node 0x9E))

      :join-probe
      (let [tbl    (nth in 1)
            nbuck  (count (:keys tbl))
            kk     (:k node 0x9E)
            v (:values rel0) m (:mask rel0) n (count v)
            m2 (reduce (fn [acc i]
                         (if (and (bit? m i)
                                  (join/join-probe-ref (v i) tbl width nbuck kk))
                           (bit-or acc (bit-shift-left 1 i))
                           acc))
                       0 (range n))]
        (assoc rel0 :mask m2))

      :reduce
      {:result (red/reduce-ref (:values rel0) (:mask rel0) (:reduce-op node) width)})))

(defn run-plan
  "Execute `plan` over `input` by composing the operator reference models in
   topological order. Returns the behavior-preserving summary map
   {:values [...] :mask int [:buckets ...] [:result x] :env {...}} where `:env`
   maps every node id to its value (relation / table / scalar)."
  [plan input]
  (let [width (:width plan)
        bits  (:bits input)
        nodes (plan->nodes plan)
        srcs  (input-sources input)
        env   (reduce (fn [env node]
                        (assoc env (:id node)
                               (eval-node width bits node
                                          (mapv #(resolve-in env srcs %) (:inputs node)))))
                      {} nodes)
        terminal (peek nodes)
        t-rel (if (= :reduce (:op terminal))
                (resolve-in env srcs (first (:inputs terminal)))
                (env (:id terminal)))
        last-reduce (last (filter #(= :reduce (:op %)) nodes))
        last-hash   (last (filter #(= :hash (:op %)) nodes))]
    (cond-> {:env env}
      (:values t-rel) (assoc :values (:values t-rel) :mask (:mask t-rel))
      last-reduce     (assoc :result (:result (env (:id last-reduce))))
      last-hash       (assoc :buckets (:buckets (env (:id last-hash)))))))
