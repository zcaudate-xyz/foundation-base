(ns jvm.chisel.db
  "Fragment builders shared by the hardware database operators.

   These functions do **not** build modules; they return Chisel `Data` values
   (or Clojure vectors of them) assembled from `jvm.chisel` primitives, meant to
   be called inside a `jvm.chisel/module` body. Keeping them as fragments lets
   the operators in `jvm.chisel.db.*` compose into larger datapaths."
  (:require [jvm.chisel :as ch]))

(defn cmp-vec
  "Lane-parallel compare of a Vec `values` against `constant` with `op`
   (a `jvm.chisel` comparison fn: eq/neq/lt/lte/gt/gte). Returns a Clojure
   vector of `n` Bool Data (lsb-first lane order)."
  [values constant op n]
  (mapv (fn [i] (op (ch/index values i) constant)) (range n)))

(defn mask-pack
  "Pack a Clojure seq of Bool/Bits into a UInt bitmask (lsb first)."
  [bits]
  (ch/vec-as-uint bits))

(defn tree-reduce
  "Balanced binary tree reduction of a Clojure vector of Data using `combine`
   (a fn of two Data -> Data). `zero` is the identity for empty input."
  [combine zero elems]
  (let [v (vec elems)
        n (count v)]
    (cond
      (zero? n) zero
      (= 1 n)  (v 0)
      :else    (combine (tree-reduce combine zero (subvec v 0 (quot n 2)))
                        (tree-reduce combine zero (subvec v (quot n 2)))))))

(defn popcount
  "Sum of the set bits of a bitmask `mask` of width `n` (adder tree)."
  [mask n]
  (tree-reduce ch/add (ch/u 0 1)
               (mapv #(ch/index mask %) (range n))))

(defn gated
  "Gate each lane of `values` (Vec n UInt(w)) by a bitmask: lane i becomes
   `(mux mask[i] values[i] identity)`. `identity-data` is a Data of width w."
  [values mask n identity-data]
  (mapv (fn [i] (ch/mux (ch/index mask i) (ch/index values i) identity-data))
        (range n)))

(defn mhash
  "Multiplicative hash: high `log-n` bits of (key * K) viewed as 2*width bits.
   `key` is UInt(width); returns UInt(log-n)."
  [key width k log-n]
  (let [prod (ch/mul key (ch/u k width))]
    (ch/bits-at prod (dec (* 2 width)) (- (* 2 width) log-n))))

(defn one-hot
  "UInt(width) with the single (dynamic) bit `idx` set: 1 << idx."
  [idx width]
  (ch/shl (ch/u 1 width) idx))

(defn log2-ceil
  "Smallest number of bits that can represent values 0..n (i.e. ceil(log2(n+1)))."
  [n]
  (long (Math/ceil (/ (Math/log (inc n)) (Math/log 2)))))

(defn log2
  "ceil(log2 n): depth of an n-leaf reduction tree (for sum-width bookkeeping)."
  [n]
  (long (Math/ceil (/ (Math/log n) (Math/log 2)))))

(def op->fn
  "map from comparison keyword to the `jvm.chisel` operator"
  {:eq ch/eq :neq ch/neq :lt ch/lt :lte ch/lte :gt ch/gt :gte ch/gte})
