(ns jvm.chisel.db.pipeline
  "Composed datapath for a placed logical plan: one Chisel module that chains the
   `jvm.chisel.db` operator fragments in plan order.

   This is the physical half of the scheduler split (`jvm.chisel.db.schedule` is
   the logical/placement half). For the linear slice the stages run in the order
   given by the plan and share a single running valid-mask:

     values ──▶ scan/bloom (mask) ──▶ hash (per-lane bucket) ──▶ reduce (scalar)

   The composition idiom matches `chisel.examples.db-primitives/scan-count-module`:
   one module, fragments threaded in a `let`, no `module-instance` nesting. A
   future valid/ready streaming fabric or crossbar reuses the same scheduler and
   operator modules behind registered ports.

   IO (assembled from the stages present):
     inputs : values[lanes] (UInt w), validMask (UInt lanes),
              one constant c0..cK per scan predicate, bits (UInt M) when a bloom
              stage is present.
     outputs: matchMask (UInt lanes) always,
              buckets[lanes] (UInt logB) when a :hash stage is present,
              result (UInt ow) when a terminal :reduce stage is present."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]
            [jvm.chisel.db.bloom :as bloom]
            [jvm.chisel.db.schedule :as sched]))

(defn- out-width
  "Reduce result width (mirrors `jvm.chisel.db.reduce`)."
  [op lanes width]
  (case op
    :count (db/log2-ceil lanes)
    :sum   (+ width (db/log2 lanes))
    width))

(defn- reduce-expr
  "Tree-reduce Data for `op` over `vals` gated by `mask` (a UInt(lanes) Data)."
  [op mask vals width lanes]
  (case op
    :count (db/popcount mask lanes)
    :sum   (db/tree-reduce ch/add (ch/u 0 width)
                           (db/gated vals mask lanes (ch/u 0 width)))
    :min   (let [maxv (ch/u (dec (bit-shift-left 1 width)) width)]
             (db/tree-reduce (fn [a b] (ch/mux (ch/lt a b) a b)) maxv
                             (db/gated vals mask lanes maxv)))
    :max   (db/tree-reduce (fn [a b] (ch/mux (ch/gt a b) a b)) (ch/u 0 width)
                           (db/gated vals mask lanes (ch/u 0 width)))))

(defn- first-stage [stages op]
  (some (fn [s] (when (= op (:op s)) s)) stages))

(defn pipeline-module
  "Build the composed module. opts: {:width w :lanes n :stages [...] :name \"Pipe\"}.
   See `jvm.chisel.db.schedule` for the stage grammar."
  [{:keys [width lanes stages name] :or {name "DbPipeline"}}]
  (let [bloom-stage (first-stage stages :bloom-probe)
        hash-stage  (first-stage stages :hash)
        reduce-stage (first-stage stages :reduce)
        logB    (when hash-stage  (db/log2 (:buckets hash-stage)))
        ow      (when reduce-stage (out-width (:reduce-op reduce-stage) lanes width))]
    (ch/module
     {:name name}
     (fn []
       (let [;; assign a global constant index to every scan predicate, in order
             {:keys [const-fields pred-idx]}
             (reduce (fn [{:keys [const-fields pred-idx k]} s]
                       (if (= :scan (:op s))
                         (let [idx (vec (range k (+ k (count (:preds s)))))]
                           {:const-fields (into const-fields
                                                (map (fn [j] [(keyword (str "c" j))
                                                              (ch/input (ch/uint width))])
                                                     idx))
                            :pred-idx     (conj pred-idx idx)
                            :k            (+ k (count (:preds s)))})
                         {:const-fields const-fields
                          :pred-idx     (conj pred-idx nil)
                          :k            k}))
                     {:const-fields [] :pred-idx [] :k 0}
                     stages)
             io-fields (cond-> [[:values    (ch/input (ch/vec lanes (ch/uint width)))]
                                [:validMask (ch/input (ch/uint lanes))]
                                [:matchMask (ch/output (ch/uint lanes))]]
                         bloom-stage (conj [:bits (ch/input (ch/uint (:bits-count bloom-stage)))])
                         true        (into const-fields)
                         hash-stage  (conj [:buckets (ch/output (ch/vec lanes (ch/uint logB)))])
                         reduce-stage (conj [:result  (ch/output (ch/uint ow))]))
             io   (ch/io (ch/bundle io-fields))
             vals (ch/field io :values)
             final
             (reduce
              (fn [{:keys [mask] :as st} [i s]]
                (case (:op s)
                  :scan
                  (let [ops  (map first (:preds s))
                        idxs (pred-idx i)
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
                    (assoc st :mask (db/mask-pack masked)))

                  :bloom-probe
                  (let [{:keys [bits-count ks]} s
                        hit    (mapv (fn [lane]
                                       (bloom/probe-data (ch/index vals lane)
                                                         (ch/field io :bits)
                                                         width bits-count ks))
                                     (range lanes))
                        masked (mapv (fn [lane] (ch/and (ch/index mask lane) (hit lane)))
                                     (range lanes))]
                    (assoc st :mask (db/mask-pack masked)))

                  :hash
                  (let [{:keys [k] :or {k 0x9E}} s
                        bks (mapv (fn [lane] (db/mhash (ch/index vals lane) width k logB))
                                  (range lanes))]
                    (assoc st :buckets bks))

                  :reduce
                  (assoc st :result (reduce-expr (:reduce-op s) mask vals width lanes))))
              {:mask (ch/field io :validMask)}
              (map-indexed vector stages))]
         (ch/connect! (ch/field io :matchMask) (:mask final))
         (when-let [bks (:buckets final)]
           (doseq [lane (range lanes)]
             (ch/connect! (ch/index (ch/field io :buckets) lane) (bks lane))))
         (when-let [res (:result final)]
           (ch/connect! (ch/field io :result) res)))))))

(defn pipeline-ref
  "Reference for the composed pipeline: delegates to `schedule/run-plan`."
  [plan input]
  (sched/run-plan plan input))
