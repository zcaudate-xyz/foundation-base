(ns jvm.chisel.variant.squash
  "Block majority / downsample (`k-squash`).

   Partitions a bitstring into blocks of `resolution` bits and emits one bit per
   block: `1` iff the block's popcount is strictly greater than `resolution/2`
   (ties go to `1`). This is the hardware form of
   `math.variant.algorithm.k/k-squash` — a popcount-threshold per block."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant :as v]))

;; reference model -----------------------------------------------------------

(defn k-squash-ref
  "Reference: majority vote per `resolution`-bit block of `bits`
   (seq of 0/1). Matches `math.variant.algorithm.k/k-squash`.

   (k-squash-ref [1 0 1 1 1 1 0 1 0] 3) => [1 1 0]"
  [bits resolution]
  (let [v (vec bits)
        n (count v)
        thr (quot resolution 2)]
    (mapv (fn [b]
            (let [ones (reduce + (subvec v (* b resolution)
                                         (min n (* (inc b) resolution))))]
              (if (> ones thr) 1 0)))
          (range (quot n resolution)))))

;; module --------------------------------------------------------------------

(defn k-squash-module
  "Combinational block majority. opts {:keys [blocks resolution name]}. IO:
   `bits : UInt<blocks*resolution>` input (block b occupies bits
   b*R .. b*R+R-1, lsb-first within the block); `out : UInt<blocks>` output,
   bit b = 1 iff block b popcount > resolution/2."
  [{:keys [blocks resolution name] :or {name "KSquash"}}]
  (ch/module
   {:name name}
   (fn []
     (let [width (* blocks resolution)
           io    (ch/io (ch/bundle [[:bits (ch/input  (ch/uint width))]
                                    [:out  (ch/output (ch/uint blocks))]]))
           bits  (ch/field io :bits)
           thr   (ch/u (quot resolution 2) (v/sum-width resolution))
           outs  (mapv (fn [b]
                         (let [base (* b resolution)
                               blk  (ch/bits-at bits (+ base resolution -1) base)
                               pc   (v/popcount blk resolution)]
                           (ch/gt pc thr)))
                       (range blocks))]
       (ch/connect! (ch/field io :out) (ch/vec-as-uint outs))))))
