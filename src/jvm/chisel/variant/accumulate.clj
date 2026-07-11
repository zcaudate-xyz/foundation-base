(ns jvm.chisel.variant.accumulate
  "Streaming (sequential) k-measure prefix / CDF.

   One bit per cycle in; the module maintains the running `[p [k0 k1 k2 k3]]`
   of every bit seen so far. This is the hardware form of
   `math.variant.algorithm.k/k-accumulate` (and the body of
   `k-stream/k-measure-stream`): a 1-bit previous-bit register plus five
   accumulators, updated in O(1) per cycle from the `{prev, cur}` pair.

   Outputs are registered: the cycle-`t` output is the measure of the bits
   consumed on cycles `0..t-1` (one-cycle latency); the all-zero state is
   presented before the first valid bit."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant :as v]))

;; reference model -----------------------------------------------------------

(defn k-accumulate-ref
  "Reference: per-prefix k-measure of `bits` (seq of 0/1). Matches
   `math.variant.algorithm.k/k-accumulate`.

   (k-accumulate-ref [1 0 1 1 1 1 0 1])
   => [[1 [0 0 0 0]] [1 [0 0 1 0]] [2 [0 1 1 0]] [3 [0 1 1 1]]
       [4 [0 1 1 2]] [5 [0 1 1 3]] [5 [0 1 2 3]] [6 [0 2 2 3]]]"
  [bits]
  (let [v (vec bits)]
    (when (seq v)
      (loop [i 1, prev (v 0), p (v 0), k [0 0 0 0], acc [[(v 0) [0 0 0 0]]]]
        (if (= i (count v))
          acc
          (let [cur (v i)
                p   (+ p cur)
                idx (+ cur (bit-shift-left prev 1))
                k   (update k idx inc)]
            (recur (inc i) cur p k (conj acc [p k]))))))))

;; module --------------------------------------------------------------------

(defn k-accumulate-module
  "Sequential running k-measure. opts {:keys [n-max name]}: counters are sized
   for streams up to `n-max` bits. IO: `bit : UInt<1>`, `valid : UInt<1>`
   inputs; `p : UInt<pw>`, `k0..k3 : UInt<kw>` registered outputs. While
   `valid` is low the registers hold their value."
  [{:keys [n-max name] :or {name "KAccumulate"}}]
  (let [pw (v/pw n-max)
        kw (v/kw n-max)]
    (ch/module
     {:name name}
     (fn []
       (let [io (ch/io (ch/bundle [[:bit   (ch/input  (ch/uint 1))]
                                   [:valid (ch/input  (ch/bool))]
                                   [:p     (ch/output (ch/uint pw))]
                                   [:k0    (ch/output (ch/uint kw))]
                                   [:k1    (ch/output (ch/uint kw))]
                                   [:k2    (ch/output (ch/uint kw))]
                                   [:k3    (ch/output (ch/uint kw))]]))
             bit   (ch/field io :bit)
             valid (ch/field io :valid)
             pR  (ch/reg-init (ch/u 0 pw))
             k0R (ch/reg-init (ch/u 0 kw))
             k1R (ch/reg-init (ch/u 0 kw))
             k2R (ch/reg-init (ch/u 0 kw))
             k3R (ch/reg-init (ch/u 0 kw))
             prev (ch/reg-init (ch/u 0 1))
             {:keys [k00 k01 k10 k11]} (v/pair-bools prev bit)]
         (ch/when valid
           (ch/connect! pR  (v/truncate (ch/add pR  bit) pw))
           (ch/connect! k0R (v/truncate (ch/add k0R k00) kw))
           (ch/connect! k1R (v/truncate (ch/add k1R k01) kw))
           (ch/connect! k2R (v/truncate (ch/add k2R k10) kw))
           (ch/connect! k3R (v/truncate (ch/add k3R k11) kw))
           (ch/connect! prev bit))
         (ch/connect! (ch/field io :p)  pR)
         (ch/connect! (ch/field io :k0) k0R)
         (ch/connect! (ch/field io :k1) k1R)
         (ch/connect! (ch/field io :k2) k2R)
         (ch/connect! (ch/field io :k3) k3R))))))
