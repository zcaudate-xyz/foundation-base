(ns jvm.chisel.variant.measure
  "Block (combinational) k-measure and c-measure of a bitstring.

   `k-measure` -> `[p [k0 k1 k2 k3]]`; `c-measure` -> `[p c]` with `c = k2`.
   These are the hardware forms of `math.variant.algorithm.k/k-measure` and
   `math.variant.algorithm.c/c-measure`: a popcount plus four (one) transition
   counters over the `n-1` adjacent pairs, all combinational adder trees."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant :as v]))

;; reference models ----------------------------------------------------------

(defn k-measure-ref
  "Reference for the k-measure of `bits` (seq of 0/1, element 0 = bit 0).
   Matches `math.variant.algorithm.k/k-measure`.

   (k-measure-ref [1 0 1])       => [2 [0 1 1 0]]
   (k-measure-ref [1 0 1 0 1 1]) => [4 [0 2 2 1]]"
  [bits]
  (let [v (vec bits)
        n (count v)]
    (loop [i 0, p 0, k [0 0 0 0]]
      (if (= i n)
        [p k]
        (let [cur (long (v i))
              p   (+ p cur)]
          (if (zero? i)
            (recur 1 p k)
            (let [prev (long (v (dec i)))
                  idx  (+ cur (bit-shift-left prev 1))]
              (recur (inc i) p (update k idx inc)))))))))

(defn c-measure-ref
  "Reference for the c-measure of `bits`: `[p c]` with `c = k2`
   (number of `10` falling edges). Matches `math.variant.algorithm.c/c-measure`.

   (c-measure-ref [1 0 1])       => [2 1]
   (c-measure-ref [1 0 1 0 1 1]) => [4 2]"
  [bits]
  (let [[p [_ _ k2 _]] (k-measure-ref bits)]
    [p k2]))

;; modules -------------------------------------------------------------------

(defn k-measure-module
  "Combinational k-measure. opts {:keys [n name]}. IO: `bits : UInt<n>` input;
   `p : UInt<pw>`, `k0..k3 : UInt<kw>` outputs."
  [{:keys [n name] :or {name "KMeasure"}}]
  (ch/module
   {:name name}
   (fn []
     (let [io (ch/io (ch/bundle [[:bits (ch/input  (ch/uint n))]
                                 [:p    (ch/output (ch/uint (v/pw n)))]
                                 [:k0   (ch/output (ch/uint (v/kw n)))]
                                 [:k1   (ch/output (ch/uint (v/kw n)))]
                                 [:k2   (ch/output (ch/uint (v/kw n)))]
                                 [:k3   (ch/output (ch/uint (v/kw n)))]]))
           bits (ch/field io :bits)
           [k0 k1 k2 k3] (v/transition-counts bits n)
           p (v/popcount bits n)]
       (ch/connect! (ch/field io :p)  p)
       (ch/connect! (ch/field io :k0) k0)
       (ch/connect! (ch/field io :k1) k1)
       (ch/connect! (ch/field io :k2) k2)
       (ch/connect! (ch/field io :k3) k3)))))

(defn c-measure-module
  "Combinational c-measure. opts {:keys [n name]}. IO: `bits : UInt<n>` input;
   `p : UInt<pw>`, `c : UInt<kw>` outputs (`c = k2`)."
  [{:keys [n name] :or {name "CMeasure"}}]
  (ch/module
   {:name name}
   (fn []
     (let [io (ch/io (ch/bundle [[:bits (ch/input  (ch/uint n))]
                                 [:p    (ch/output (ch/uint (v/pw n)))]
                                 [:c    (ch/output (ch/uint (v/kw n)))]]))
           bits (ch/field io :bits)
           [_ _ k2 _] (v/transition-counts bits n)
           p (v/popcount bits n)]
       (ch/connect! (ch/field io :p) p)
       (ch/connect! (ch/field io :c) k2)))))
