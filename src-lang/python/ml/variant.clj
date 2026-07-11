(ns python.ml.variant
  "Soft, differentiable multiscale variant features (PyTorch), emitted to Python
   via the hara/xtalk DSL.

   In the hard limit (large beta) `k-measure-soft` equals the canonical hard
   k-measure (`math.variant.algorithm.k/k-measure`,
   `jvm.chisel.variant.measure/k-measure-ref`): p = popcount; each adjacent pair
   (prev,cur) contributes to [k00 k01 k10 k11] by idx = cur + (prev<<1).

   Soft relaxation: b = sigmoid(beta*(z - tau)); the four pair memberships become
   (1-bp)(1-bc), (1-bp)bc, bp(1-bc), bp*bc and p = sum(b). Multi-scale = soft
   majority (`k-squash`) over blocks of `r` before measuring."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]]})

(defn.py soft-threshold
  "Soft event state b_t = sigmoid(beta * (z_t - tau))."
  [z tau beta]
  (return (. torch (sigmoid (* beta (- z tau))))))

(defn.py k-measure-from-b
  "Soft [p k00 k01 k10 k11] (a 5-vector) from a 1-D soft-bit tensor `b`."
  [b]
  (var p (. b (sum)))
  (var T (. b (size 0)))
  (var bp (. b (narrow 0 0 (- T 1))))
  (var bc (. b (narrow 0 1 (- T 1))))
  (var k00 (. (* (- 1 bp) (- 1 bc)) (sum)))
  (var k01 (. (* (- 1 bp) bc) (sum)))
  (var k10 (. (* bp (- 1 bc)) (sum)))
  (var k11 (. (* bp bc) (sum)))
  (return (. torch (stack [p k00 k01 k10 k11]))))

(defn.py k-measure-soft
  "Soft k-measure of a 1-D trajectory `z` at threshold `tau`."
  [z tau beta]
  (return (k-measure-from-b (soft-threshold z tau beta))))

(defn.py squash-soft
  "Soft majority over non-overlapping blocks of `r` (matches `k-squash`):
   coarse bit ~ sigmoid(beta * (block_mean - 0.5)). Drops the trailing partial
   block (Clojure `partition` semantics)."
  [z r beta]
  (var T (. z (size 0)))
  (var nblocks (int (/ T r)))
  (var keep (* nblocks r))
  (var zc (. z (narrow 0 0 keep)))
  (var m (. (. zc (reshape [nblocks r])) (mean 1)))
  (return (soft-threshold m 0.5 beta)))

(defn.py k-measure-soft-scale
  "Soft k-measure at squash scale `r` (r = 1 is the base scale)."
  [z tau beta r]
  (if (== r 1)
    (return (k-measure-soft z tau beta))
    (return (k-measure-from-b (squash-soft z r beta)))))

;; ---------------------------------------------------------------------------
;; batched 2-D forms: Z is [T, C] (T tokens, C channels). Vectorised over C so
;; the driver does not need a Python channel loop.
;; ---------------------------------------------------------------------------

(defn.py k-measure-from-b-2d [b]
  (var p (. b (sum 0)))
  (var T (. b (size 0)))
  (var bp (. b (narrow 0 0 (- T 1))))
  (var bc (. b (narrow 0 1 (- T 1))))
  (var k00 (. (* (- 1 bp) (- 1 bc)) (sum 0)))
  (var k01 (. (* (- 1 bp) bc) (sum 0)))
  (var k10 (. (* bp (- 1 bc)) (sum 0)))
  (var k11 (. (* bp bc) (sum 0)))
  (return (. torch (stack [p k00 k01 k10 k11] 1))))

(defn.py k-measure-soft-2d [Z tau beta]
  (var b (. torch (sigmoid (* beta (- Z tau)))))
  (return (k-measure-from-b-2d b)))

(defn.py squash-2d [Z r beta]
  (var T (. Z (size 0)))
  (var C (. Z (size 1)))
  (var nblocks (int (/ T r)))
  (var keep (* nblocks r))
  (var Zc (. Z (narrow 0 0 keep)))
  (var m (. (. Zc (reshape [nblocks r C])) (mean 1)))
  (return (. torch (sigmoid (* beta (- m 0.5))))))

(defn.py variant-at-scale
  "Normalised, flattened soft variant feature for one (threshold, scale): r = 1
   thresholds the raw trajectory at `tau`; r > 1 soft-majority-squashes (at 0.5)
   then measures. Returns a flat tensor (C * 5,)."
  [Z tau beta r]
  (if (== r 1)
    (do
      (var f (k-measure-soft-2d Z tau beta))
      (var L (. Z (size 0))))
    (do
      (var b (squash-2d Z r beta))
      (var f (k-measure-from-b-2d b))
      (var L (. b (size 0)))))
  (return (. (/ f L) (reshape [-1]))))

(defn.py variant-feature
  "Multiscale soft variant feature vector for a [T, C] trajectory: concatenated
   per-(threshold, scale) flattened features."
  [Z thresholds scales beta]
  (var parts [])
  (for:array [tau thresholds]
    (for:array [r scales]
      (. parts (append (variant-at-scale Z tau beta r)))))
  (return (. torch (cat parts))))
