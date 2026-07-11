(ns jvm.chisel.db.bloom
  "Bloom filter: a classic hash-join pre-filter.

   Combinational query-time probe against a preloaded bit-vector, plus a
   fragment that computes the bit-vector with a key inserted. `ks` is the list
   of hash multipliers (one per hash function); `bits-count` (= M, power of two)
   is the bit-vector width."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]
            [jvm.chisel.db.hash :as h]))

(defn- log2-of [n] (long (/ (Math/log n) (Math/log 2))))

(defn probe-data
  "Hardware fragment: probe `key` against bit-vector `bits`. Returns a Bool Data."
  [key bits width bits-count ks]
  (let [log-m (log2-of bits-count)]
    (reduce ch/and
            (map (fn [k]
                   (let [h (db/mhash key width k log-m)]
                     (ch/neq (ch/and bits (db/one-hot h bits-count))
                             (ch/u 0 bits-count))))
                 ks))))

(defn insert-data
  "Hardware fragment: bit-vector with `key` inserted = bits OR OR_k(1<<h_k)."
  [key bits width bits-count ks]
  (let [log-m (log2-of bits-count)
        sets  (map (fn [k] (db/one-hot (db/mhash key width k log-m) bits-count)) ks)]
    (reduce ch/or (cons bits sets))))

(defn bloom-probe-module
  "Probe module. opts: {:width w :bits-count M :ks [..] :name \"BloomProbe\"}."
  [{:keys [width bits-count ks name] :or {name "BloomProbe"}}]
  (ch/module
   {:name name}
   (fn []
     (let [io (ch/io (ch/bundle [[:key  (ch/input (ch/uint width))]
                                 [:bits (ch/input (ch/uint bits-count))]
                                 [:hit  (ch/output (ch/bool))]]))]
       (ch/connect! (ch/field io :hit)
                    (probe-data (ch/field io :key) (ch/field io :bits) width bits-count ks))))))

(defn bloom-probe-ref
  "Reference: true iff every hash position is set in `bits`."
  [key bits width bits-count ks]
  (let [log-m (log2-of bits-count)]
    (every? (fn [k]
              (let [h (h/hash-ref key width k log-m)]
                (pos? (bit-and bits (bit-shift-left 1 h)))))
            ks)))

(defn bloom-insert-ref
  "Reference: bit-vector with the hash positions of `key` set."
  [key bits width bits-count ks]
  (let [log-m (log2-of bits-count)]
    (reduce (fn [b k]
              (let [h (h/hash-ref key width k log-m)]
                (bit-or b (bit-shift-left 1 h))))
            bits ks)))
