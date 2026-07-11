(ns jvm.chisel.db.pipeline
  "Composed datapath for a placed logical plan: one Chisel module that chains the
   `jvm.chisel.db` operator fragments over the plan's node DAG.

   This is the physical half of the scheduler split (`jvm.chisel.db.schedule` is
   the logical/placement half). Plans are normalized by `schedule/plan->nodes`: a
   linear `:stages` plan is a node chain; a `:nodes` plan may fork (e.g. a
   `join-build` branch and a `join-probe` branch reading different sources).

     values ──▶ scan/bloom (mask) ──▶ join-build (table) ─┐
     values2 ─▶ scan (mask) ──▶ join-probe (mask) ◀────────┘ ──▶ reduce (scalar)

   The composition idiom matches `chisel.examples.db-primitives/scan-count-module`:
   one module, fragments threaded in a `let`, no `module-instance` nesting.

   IO (assembled from the nodes present):
     inputs : per source i: values/validMask for i = 0 (legacy names), else
              values2/validMask2, values3/validMask3, …;
              one constant c0..cK per scan predicate (global across scan nodes),
              bits (UInt M) when a bloom node is present.
     outputs: matchMask (UInt lanes) always,
              buckets[lanes] (UInt logB) when a :hash node is present,
              result (UInt ow) when a :reduce node is present."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]
            [jvm.chisel.db.bloom :as bloom]
            [jvm.chisel.db.join :as join]
            [jvm.chisel.db.schedule :as sched]))

(defn- out-width
  "Reduce result width (mirrors `jvm.chisel.db.reduce`)."
  [op lanes width]
  (case op
    :count (db/log2-ceil lanes)
    :sum   (+ width (db/log2 lanes))
    width))

(defn- widen-to
  "Zero-extend a `width`-bit UInt `x` to `w` bits (MSBs zero). `ch/add` is
   Chisel's width-preserving `+`, so the :sum tree must widen leaves to the full
   output width or the carries truncate (1020 -> 252)."
  [x width w]
  (if (<= w width) x (ch/cat (ch/u 0 (- w width)) x)))

(defn- reduce-expr
  "Tree-reduce Data for `op` over `vals` gated by `mask` (a UInt(lanes) Data)."
  [op mask vals width lanes]
  (case op
    :count (db/popcount mask lanes)
    :sum   (let [ow (out-width :sum lanes width)]
             (db/tree-reduce ch/add (ch/u 0 ow)
                             (mapv #(widen-to % width ow)
                                   (db/gated vals mask lanes (ch/u 0 width)))))
    :min   (let [maxv (ch/u (dec (bit-shift-left 1 width)) width)]
             (db/tree-reduce (fn [a b] (ch/mux (ch/lt a b) a b)) maxv
                             (db/gated vals mask lanes maxv)))
    :max   (db/tree-reduce (fn [a b] (ch/mux (ch/gt a b) a b)) (ch/u 0 width)
                           (db/gated vals mask lanes (ch/u 0 width)))))

(defn- src-value-port [i] (if (zero? i) :values (keyword (str "values" (inc i)))))
(defn- src-mask-port   [i] (if (zero? i) :validMask (keyword (str "validMask" (inc i)))))

(defn- eval-node-data
  "Build the Data value for one node. Relations are {:values Data :mask Data};
   a join-build produces a table {:valid Data :keys vec-of-Data}; a reduce
   produces {:result Data}."
  [env src-rels io pred-idx width lanes node]
  (let [resolve (fn [ref] (if (and (vector? ref) (= :src (first ref)))
                            (nth src-rels (second ref))
                            (env ref)))
        in0     (resolve (first (:inputs node)))]
    (case (:op node)
      :scan
      (let [vals (:values in0) mask (:mask in0)
            ops  (map first (:preds node))
            idxs (pred-idx (:id node))
            hits (mapv (fn [lane]
                         (reduce (fn [acc [op ci]]
                                   (ch/and acc ((db/op->fn op)
                                                (ch/index vals lane)
                                                (ch/field io (keyword (str "c" ci))))))
                                 (ch/b true)
                                 (map vector ops idxs)))
                       (range lanes))
            masked (mapv (fn [lane] (ch/and (ch/index mask lane) (hits lane)))
                         (range lanes))]
        (assoc in0 :mask (db/mask-pack masked)))

      :bloom-probe
      (let [{:keys [bits-count ks]} node
            vals (:values in0) mask (:mask in0)
            hit    (mapv (fn [lane]
                           (bloom/probe-data (ch/index vals lane)
                                             (ch/field io :bits)
                                             width bits-count ks))
                         (range lanes))
            masked (mapv (fn [lane] (ch/and (ch/index mask lane) (hit lane)))
                         (range lanes))]
        (assoc in0 :mask (db/mask-pack masked)))

      :hash
      (let [{:keys [k] :or {k 0x9E}} node
            logB (db/log2 (:buckets node))
            vals (:values in0)
            bks  (mapv (fn [lane] (db/mhash (ch/index vals lane) width k logB))
                       (range lanes))]
        (assoc in0 :buckets bks))

      :join-build
      (join/build-table-data (:values in0) (:mask in0) lanes width
                             (:buckets node) (:k node 0x9E))

      :join-probe
      (let [tbl  (resolve (second (:inputs node)))
            vals (:values in0) mask (:mask in0)
            nbuck (count (:keys tbl))
            kk   (:k node 0x9E)
            hit  (mapv (fn [lane]
                         (join/join-probe-data (ch/index vals lane)
                                               (:valid tbl) (:keys tbl)
                                               width nbuck kk))
                       (range lanes))
            masked (mapv (fn [lane] (ch/and (ch/index mask lane) (hit lane)))
                         (range lanes))]
        (assoc in0 :mask (db/mask-pack masked)))

      :reduce
      {:result (reduce-expr (:reduce-op node) (:mask in0) (:values in0) width lanes)})))

(defn pipeline-module
  "Build the composed module. opts = the plan map (see `jvm.chisel.db.schedule`
   for the node grammar) plus :name."
  [{:keys [width lanes name] :as plan :or {name "DbPipeline"}}]
  (let [nodes      (sched/plan->nodes plan)
        nsrc       (sched/plan->sources plan)
        bloom-node (some (fn [n] (when (= :bloom-probe (:op n)) n)) nodes)
        hash-node  (some (fn [n] (when (= :hash (:op n)) n)) nodes)
        reduce-node (last (filter (fn [n] (= :reduce (:op n))) nodes))
        logB (when hash-node  (db/log2 (:buckets hash-node)))
        ow   (when reduce-node (out-width (:reduce-op reduce-node) lanes width))]
    (ch/module
     {:name name}
     (fn []
       (let [;; assign a global constant index to every scan predicate, in node order
             {:keys [const-fields pred-idx]}
             (reduce (fn [{:keys [const-fields pred-idx k]} node]
                       (if (= :scan (:op node))
                         (let [idx (vec (range k (+ k (count (:preds node)))))]
                           {:const-fields (into const-fields
                                                (map (fn [j] [(keyword (str "c" j))
                                                              (ch/input (ch/uint width))])
                                                     idx))
                            :pred-idx     (assoc pred-idx (:id node) idx)
                            :k            (+ k (count (:preds node)))})
                         {:const-fields const-fields :pred-idx pred-idx :k k}))
                     {:const-fields [] :pred-idx {} :k 0}
                     nodes)
             src-fields (vec (mapcat (fn [i] [[(src-value-port i)
                                               (ch/input (ch/vec lanes (ch/uint width)))]
                                              [(src-mask-port i)
                                               (ch/input (ch/uint lanes))]])
                                     (range nsrc)))
             io-fields (cond-> src-fields
                         true        (conj [:matchMask (ch/output (ch/uint lanes))])
                         bloom-node  (conj [:bits (ch/input (ch/uint (:bits-count bloom-node)))])
                         true        (into const-fields)
                         hash-node   (conj [:buckets (ch/output (ch/vec lanes (ch/uint logB)))])
                         reduce-node (conj [:result  (ch/output (ch/uint ow))]))
             io   (ch/io (ch/bundle io-fields))
             src-rels (mapv (fn [i] {:values (ch/field io (src-value-port i))
                                     :mask   (ch/field io (src-mask-port i))})
                            (range nsrc))
             env  (reduce (fn [env node]
                            (assoc env (:id node)
                                   (eval-node-data env src-rels io pred-idx width lanes node)))
                          {} nodes)
             terminal (peek nodes)
             t-rel (if (= :reduce (:op terminal))
                     (let [resolve (fn [ref] (if (and (vector? ref) (= :src (first ref)))
                                               (nth src-rels (second ref))
                                               (env ref)))]
                       (resolve (first (:inputs terminal))))
                     (env (:id terminal)))]
         (ch/connect! (ch/field io :matchMask) (:mask t-rel))
         (when hash-node
           (let [bks (:buckets (env (:id hash-node)))]
             (doseq [lane (range lanes)]
               (ch/connect! (ch/index (ch/field io :buckets) lane) (bks lane)))))
         (when reduce-node
           (ch/connect! (ch/field io :result) (:result (env (:id reduce-node))))))))))

(defn pipeline-ref
  "Reference for the composed pipeline: delegates to `schedule/run-plan`."
  [plan input]
  (sched/run-plan plan input))
