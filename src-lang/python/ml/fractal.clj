(ns python.ml.fractal
  "Fractal validation: recover the Hurst exponent of synthetic fGn three ways,
   emitted via the hara/xtalk DSL.

   (a) welch-h   — spectral slope of S(f) ~ f^(1-2H) (the established spectral
                   fractal test; reference row).
   (b) amp-stats — std of the block-averaged (squashed) trajectory vs scale r;
                   the aggregated-variance law std ~ r^(H-1) gives H = 1+slope.
                   Validates the variant squash machinery against a known law.
   (c) trans-stats — variant-native: soft-threshold the squashed trajectory at
                   0, take k01+k10 (threshold crossings) per unit length vs r;
                   the log-log slope should be monotone in H (fractal crossing
                   structure). Reuses variant.clj's soft-threshold +
                   k-measure-from-b-2d unchanged."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["numpy" :as np]
            ["scipy.signal" :as sps]
            ["variant" :as variant]]})

;; ---- config (rendered into the emitted file's preamble) ---------------------

(def FIXTURES ".build/py/ml/fractal_fixtures.pt")
(def OUT ".build/py/ml/metrics_fractal.json")
(def SCALES [1 2 4 8 16 32])
(def BETA 100.0)
(def HS [0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9])
(def FS 1.0)

;; ---- helpers ------------------------------------------------------------------

(defn.py linfit [xs ys]
  "Least-squares line -> [slope intercept]."
  (var fit (. np (polyfit xs ys 1)))
  (return [(. fit [0]) (. fit [1])]))

(defn.py r-squared [xs ys a b]
  (var xa (. np (array xs)))
  (var ya (. np (array ys)))
  (var pred (+ (* a xa) b))
  (var ssres (. np (sum (pow (- ya pred) 2))))
  (var sstot (. np (sum (pow (- ya (. np (mean ya))) 2))))
  (return (- 1.0 (/ ssres sstot))))

(defn.py mae [xs ys]
  (return (. np (mean (. np (abs (- (. np (array xs)) (. np (array ys)))))))))

(defn.py monotone [xs]
  "1 if all successive diffs share one sign (either direction), else 0."
  (var ok 1)
  (var prev (. xs [0]))
  (var direction 0)
  (for:array [v xs]
    (var d (- v prev))
    (if (== direction 0)
      (do
        (if (> d 0)
          (do (:= direction 1)))
        (if (< d 0)
          (do (:= direction -1)))))
    (if (== direction 1)
      (do
        (if (< d 0)
          (do (:= ok 0)))))
    (if (== direction -1)
      (do
        (if (> d 0)
          (do (:= ok 0)))))
    (:= prev v))
  (return ok))

(defn.py block-mean [z r]
  "Block-average a [T] tensor by r (drops trailing partial block)."
  (var T (. z (size 0)))
  (var nb (int (/ T r)))
  (var keep (* nb r))
  (var zc (. z (narrow 0 0 keep)))
  (return (. (. zc (reshape [nb r])) (mean 1))))

;; ---- the three estimators ------------------------------------------------------

(defn.py welch-h [z]
  "H from the low-band spectral slope of fGn: S(f) ~ f^(1-2H)."
  (var za (. (. z (cpu)) (numpy)))
  (var w (. sps (welch za FS :nperseg 4096)))
  (var f (. w [0]))
  (var P (. w [1]))
  (var mask (. np (logical_and (. np (greater f 0.0)) (. np (less f (* 0.02 FS))))))
  (var fit (linfit (. (. np (log (. f [mask]))) (tolist))
                   (. (. np (log (. P [mask]))) (tolist))))
  (return (/ (- 1.0 (. fit [0])) 2.0)))

(defn.py amp-stats [z]
  "std of block-means vs scale -> log-log slope. H = 1 + slope."
  (var xs [])
  (var ys [])
  (for:array [r SCALES]
    (var m (block-mean z r))
    (. xs (append (. np (log r))))
    (. ys (append (. np (log (. (. m (std)) (item)))))))
  (return (linfit xs ys)))

(defn.py trans-stats [z]
  "Variant-native: crossings (k01+k10) per unit length of the squashed,
   0-thresholded trajectory vs scale -> log-log slope."
  (var xs [])
  (var ys [])
  (for:array [r SCALES]
    (var m (block-mean z r))
    ;; re-standardise per scale: the squash shrinks variance, so a fixed
    ;; threshold degenerates; the crossing structure lives in the correlation.
    (:= m (/ (- m (. m (mean))) (+ (. m (std)) 1e-9)))
    (var b (. variant (soft-threshold m 0.0 BETA)))
    (var b2 (. b (reshape [(. b (size 0)) 1])))
    (var k (. variant (k-measure-from-b-2d b2)))
    (var crossings (+ (. (. (. k [0]) [2]) (item)) (. (. (. k [0]) [3]) (item))))
    (var rate (/ crossings (. m (size 0))))
    (. xs (append (. np (log r))))
    (. ys (append (. np (log rate)))))
  (return (linfit xs ys)))

(defn.py trans-value-h [z]
  "H from the scale-1 crossing rate itself (not its scaling): Rice's formula
   rate = arccos(rho1)/pi inverts to rho1 = cos(pi*rate), and for fGn the
   lag-1 law rho1 = 2^(2H-1) - 1 gives H = (1 + log2(1 + rho1))/2. The k-measure
   at one threshold and scale carries H directly."
  (var b (. variant (soft-threshold z 0.0 BETA)))
  (var b2 (. b (reshape [(. b (size 0)) 1])))
  (var k (. variant (k-measure-from-b-2d b2)))
  (var crossings (+ (. (. (. k [0]) [2]) (item)) (. (. (. k [0]) [3]) (item))))
  (var rate (/ crossings (- (. z (size 0)) 1)))
  (var rho1 (. np (cos (* (. np pi) rate))))
  (return (/ (+ 1.0 (. np (log2 (+ 1.0 rho1)))) 2.0)))

;; ---- main -----------------------------------------------------------------------

(defn.py main []
  (var fx (. torch (load FIXTURES)))
  (var report {})
  (var h-all [])
  (var hw-all [])
  (var ha-all [])
  (var st-all [])
  (var tv-all [])
  (var st-means [])
  (for:array [H HS]
    (var sigs (. fx (get H)))
    (var n (. sigs (size 0)))
    (var hw-l [])
    (var ha-l [])
    (var st-l [])
    (var tv-l [])
    (for:array [i (range 0 n)]
      (var z (. sigs [i]))
      (:= z (/ (- z (. z (mean))) (+ (. z (std)) 1e-9)))
      (var hw (welch-h z))
      (var af (amp-stats z))
      (var ha (+ 1.0 (. af [0])))
      (var tf (trans-stats z))
      (var st (. tf [0]))
      (var tv (trans-value-h z))
      (. hw-l (append hw))
      (. ha-l (append ha))
      (. st-l (append st))
      (. tv-l (append tv))
      (. h-all (append H))
      (. hw-all (append hw))
      (. ha-all (append ha))
      (. st-all (append st))
      (. tv-all (append tv)))
    (var hw-m (. np (mean hw-l)))
    (var ha-m (. np (mean ha-l)))
    (var st-m (. np (mean st-l)))
    (var tv-m (. np (mean tv-l)))
    (. st-means (append st-m))
    (. report (__setitem__ (str H)
                           {"welch_H" hw-m
                            "welch_sd" (. np (std hw-l))
                            "amp_H" ha-m
                            "amp_sd" (. np (std ha-l))
                            "trans_slope" st-m
                            "trans_sd" (. np (std st-l))
                            "trans_H" tv-m
                            "trans_H_sd" (. np (std tv-l))}))
    (print H hw-m ha-m st-m tv-m))
  (var reg (linfit h-all st-all))
  (var summary
    {"amp_mae" (mae ha-all h-all)
     "amp_r2" (r-squared h-all ha-all 1.0 0.0)
     "welch_mae" (mae hw-all h-all)
     "welch_r2" (r-squared h-all hw-all 1.0 0.0)
     "trans_a" (. reg [0])
     "trans_b" (. reg [1])
     "trans_r2" (r-squared h-all st-all (. reg [0]) (. reg [1]))
     "trans_monotone" (monotone st-means)
     "trans_value_mae" (mae tv-all h-all)
     "trans_value_r2" (r-squared h-all tv-all 1.0 0.0)})
  (. report (__setitem__ "summary" summary))
  ;; ---- control: AR(1) is strongly correlated but NOT self-similar ----------
  (var ar1 (. fx (get "ar1")))
  (var cn (. ar1 (size 0)))
  (var c-amp [])
  (var c-trans [])
  (var c-tv [])
  (for:array [i (range 0 cn)]
    (var z (. ar1 [i]))
    (:= z (/ (- z (. z (mean))) (+ (. z (std)) 1e-9)))
    (. c-amp (append (+ 1.0 (. (amp-stats z) [0]))))
    (. c-trans (append (. (trans-stats z) [0])))
    (. c-tv (append (trans-value-h z))))
  (. report (__setitem__ "ar1_control"
                         {"amp_H" (. np (mean c-amp))
                          "trans_slope" (. np (mean c-trans))
                          "trans_H" (. np (mean c-tv))}))
  (print "ar1" (. np (mean c-amp)) (. np (mean c-trans)) (. np (mean c-tv)))
  (var f (open OUT "w"))
  (. json (dump report f))
  (. f (close))
  (print (. json (dumps summary :indent 2))))
