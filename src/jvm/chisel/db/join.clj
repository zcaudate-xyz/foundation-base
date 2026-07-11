(ns jvm.chisel.db.join
  "Direct-mapped hash join, split into a build stage (construct a hash table from
   build-side keys) and a probe stage (look a probe-side key up). Reuses the
   `jvm.chisel.db/mhash` bucket function and mirrors the `bloom`/`scan`/`reduce`
   convention: a pure `*-ref` reference model, composable `*-data` fragments, and
   `*-module` builders.

   Collision semantics are **last-writer-wins**: every build key maps to exactly one
   bucket; if two build keys collide, the later lane wins. The reference and the
   hardware implement the identical rule, so they agree by construction (see
   `join-test`). Chaining / open addressing is a later enhancement.

   This is the operator only. Join has two inputs (build + probe); wiring it into the
   plan/pipeline as a DAG node is a separate slice."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]
            [jvm.chisel.db.hash :as h]))

(defn- log2-of [n] (long (/ (Math/log n) (Math/log 2))))

;; reference model -----------------------------------------------------------

(defn join-build-ref
  "Build a direct-mapped hash table from `build-keys` (seq of ints) gated by
   `valid-mask` (int bitmask). Returns {:valid int :keys vector} where `:keys` is a
   length-`buckets` vector (0 for empty buckets) and `:valid` is the bucket bitmask.
   Last-writer-wins on collision. `buckets` must be a power of two."
  [build-keys valid-mask width buckets k]
  (let [v    (vec build-keys)
        logB (log2-of buckets)]
    (reduce
     (fn [{:keys [valid keys] :as t} i]
       (if (pos? (bit-and valid-mask (bit-shift-left 1 i)))
         (let [b (h/hash-ref (v i) width k logB)]
           {:valid (bit-or valid (bit-shift-left 1 b))
            :keys  (assoc keys b (v i))})
         t))
     {:valid 0 :keys (vec (repeat buckets 0))}
     (range (count v)))))

(defn join-probe-ref
  "Probe `table` (from `join-build-ref`) with `probe-key`. True iff the probe key's
   bucket is valid and holds `probe-key`."
  [probe-key table width buckets k]
  (let [b      (h/hash-ref probe-key width k (log2-of buckets))
        valid? (pos? (bit-and (:valid table) (bit-shift-left 1 b)))]
    (boolean (and valid? (= (long ((:keys table) b)) (long probe-key))))))

;; hardware fragments --------------------------------------------------------

(defn build-table-data
  "Hardware fragment: combinational direct-mapped hash-table build.
   `keys-vec` is a Vec of `lanes` UInt(width), `valid-mask` a UInt(lanes).
   Returns {:valid UInt(buckets) :keys clojure-vector-of-buckets-Data}, last-wins."
  [keys-vec valid-mask lanes width buckets k]
  (let [logB (log2-of buckets)
        per-bucket
        (mapv
         (fn [b]
           (reduce
            (fn [{:keys [v] acc-k :k} i]
              (let [bucket-i (db/mhash (ch/index keys-vec i) width k logB)
                    hit      (ch/and (ch/index valid-mask i)
                                     (ch/eq bucket-i (ch/u b logB)))]
                {:v (ch/or v hit)
                 :k (ch/mux hit (ch/index keys-vec i) acc-k)}))
            {:v (ch/b false) :k (ch/u 0 width)}
            (range lanes)))
         (range buckets))]
    {:valid (db/mask-pack (mapv :v per-bucket))
     :keys  (mapv :k per-bucket)}))

(defn join-probe-data
  "Hardware fragment: probe `table-valid` (UInt(buckets)) / `table-keys`
   (Vec(buckets,UInt(width))) with `probe-key` (UInt(width)). Returns a Bool Data."
  [probe-key table-valid table-keys width buckets k]
  (let [logB   (log2-of buckets)
        bucket (db/mhash probe-key width k logB)
        ;; table-keys may be a chisel Vec (probe-module input) or a Clojure
        ;; vector of Data (built in-pipeline); VecInit makes the latter indexable
        tkeys  (if (vector? table-keys) (ch/vec-init table-keys) table-keys)
        stored (ch/index tkeys bucket)
        valid? (ch/index table-valid bucket)]
    (ch/and valid? (ch/eq stored probe-key))))

;; modules -------------------------------------------------------------------

(defn join-build-module
  "Build module. opts: {:lanes n :width w :buckets B :k K :name \"JoinBuild\"}.
   Inputs: keys[lanes], validMask. Outputs: tableValid (UInt B), tableKeys (Vec B UInt w)."
  [{:keys [lanes width buckets k name] :or {name "JoinBuild" k 0x9E}}]
  (ch/module
   {:name name}
   (fn []
     (let [io (ch/io (ch/bundle [[:keys       (ch/input (ch/vec lanes (ch/uint width)))]
                                 [:validMask  (ch/input (ch/uint lanes))]
                                 [:tableValid (ch/output (ch/uint buckets))]
                                 [:tableKeys  (ch/output (ch/vec buckets (ch/uint width)))]]))
           {:keys [valid keys]} (build-table-data (ch/field io :keys) (ch/field io :validMask)
                                                  lanes width buckets k)]
       (ch/connect! (ch/field io :tableValid) valid)
       (doseq [b (range buckets)]
         (ch/connect! (ch/index (ch/field io :tableKeys) b) (nth keys b)))))))

(defn join-probe-module
  "Probe module. opts: {:width w :buckets B :k K :name \"JoinProbe\"}.
   Inputs: key, tableValid (UInt B), tableKeys (Vec B UInt w). Output: match (Bool)."
  [{:keys [width buckets k name] :or {name "JoinProbe" k 0x9E}}]
  (ch/module
   {:name name}
   (fn []
     (let [io (ch/io (ch/bundle [[:key        (ch/input (ch/uint width))]
                                 [:tableValid (ch/input (ch/uint buckets))]
                                 [:tableKeys  (ch/input (ch/vec buckets (ch/uint width)))]
                                 [:match      (ch/output (ch/bool))]]))]
       (ch/connect! (ch/field io :match)
                    (join-probe-data (ch/field io :key) (ch/field io :tableValid)
                                     (ch/field io :tableKeys) width buckets k))))))
