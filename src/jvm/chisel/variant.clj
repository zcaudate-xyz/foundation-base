(ns jvm.chisel.variant
  "Fragment builders for the binary \"variant\" measure kernels.

   The fundamental measure of a bitstring is `[p [k0 k1 k2 k3]]`:
   `p` = popcount (number of 1-bits); `k0..k3` count the adjacent-pair
   transitions `00 / 01 / 10 / 11`. The pair `{prev, cur}` has 2-bit index
   `cur + 2*prev`, so `k0={00} k1={01} k2={10} k3={11}`. The coarse `[p c]`
   measure is the subset with `c = k2` (number of `10` falling edges).

   These functions do **not** build modules; they return Chisel `Data` (or
   Clojure vectors of it) assembled from `jvm.chisel` primitives, meant to be
   called inside a `jvm.chisel/module` body. Bit order is lsb-first: sequence
   element 0 maps to bit 0 of the input `UInt(n)`, and adjacent pair `i` is
   `(bit[i], bit[i+1])`. The Clojure reference models use the same order."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db :as db]))

(declare truncate)

(defn sum-width
  "Width of the adder tree that sums `m` one-bit values. Chisel `+` widens by
   one bit per tree level, so summing `m` 1-bit operands yields
   `1 + ceil(log2(m))` bits (1 bit when m <= 1). Sizing output ports to this
   avoids truncation when connecting the tree result."
  [m]
  (if (<= m 1) 1 (inc (db/log2 m))))

(defn pw
  "Port width (bits) for `p` = popcount of an `n`-bit value."
  [n]
  (sum-width n))

(defn kw
  "Port width (bits) for each `k_j` transition counter over the `n-1` adjacent
   pairs of an `n`-bit value (min 1 bit)."
  [n]
  (sum-width (max 1 (dec n))))

(defn pair-bools
  "The four transition indicators for one adjacent pair `{prev cur}` (each a
   1-bit Data): `{:k00 {00} :k01 {01} :k10 {10} :k11 {11}}`."
  [prev cur]
  (let [np (ch/not prev)
        nc (ch/not cur)]
    {:k00 (ch/and np nc)
     :k01 (ch/and np cur)
     :k10 (ch/and prev nc)
     :k11 (ch/and prev cur)}))

(defn- widen
  "Zero-extend a 1-bit value to `w` bits (see `jvm.chisel.db/popcount`)."
  [b w]
  (if (<= w 1) b (ch/cat (ch/u 0 (dec w)) b)))

(defn transition-counts
  "Given `bits` : UInt(n) (lsb-first), return `[k0 k1 k2 k3]` — the adder-tree
   sums of the four `pair-bools` over the `n-1` adjacent pairs. For `n < 2`
   there are no pairs and all counts are zero. Indicators are zero-extended to
   the counter width before summing (see `jvm.chisel.db/popcount`)."
  [bits n]
  (if (< n 2)
    (let [z (ch/u 0 1)] [z z z z])
    (let [w     (kw n)
          pairs (map (fn [i]
                       (pair-bools (ch/index bits i) (ch/index bits (inc i))))
                     (range (dec n)))
          sum   (fn [k] (truncate (db/tree-reduce ch/add (ch/u 0 w)
                                                  (mapv #(widen (get % k) w) pairs))
                                  w))]
      [(sum :k00) (sum :k01) (sum :k10) (sum :k11)])))

(defn popcount
  "Popcount of `bits` : UInt(n) (adder tree over the n bits)."
  [bits n]
  (db/popcount bits n))

(defn truncate
  "Low `w` bits of `x` (bits-at (w-1) .. 0). Used to keep running counters at a
   fixed width after a widening `+`."
  [x w]
  (ch/bits-at x (dec w) 0))
