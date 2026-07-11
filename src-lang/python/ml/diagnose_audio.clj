(ns python.ml.diagnose_audio
  "Audio variant diagnostic driver (emitted via the hara/xtalk DSL).

   Two experiments over a pretrained Demucs dns64 denoiser:

   A) NOISE INVARIANCE (per layer). A good denoiser should become invariant to
      the noise realisation: same utterance + different noise -> small distance;
      different utterance + matched noise -> large distance. We featurise every
      probe (input, 5 encoder blocks, 5 decoder blocks, output) with variant /
      l2 / cosine / tv and report per-layer AUC + selective-invariance ratio R.
      Prediction: R grows with depth.

   B) ARTIFACT DETECTION. A click or packet gap is a ground-truth break of
      invariance. We compute per-window variant/l2/tv distances between a clean
      and a corrupted pass (input waveform and denoised output) and ask whether
      the argmax window is the true artifact window (top-1 localisation).

   Reuses variant.clj (soft multiscale k-measure) and features.clj (distances)
   unchanged from the LLM diagnostic."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["sklearn.metrics" :as sklearn_metrics]
            ["variant" :as variant]
            ["features" :as features]
            ["audio" :as audio]]})

;; ---- config (rendered into the emitted file's preamble) ---------------------

(def SR 16000)
(def THRESHOLDS [-1.0 -0.5 0.0 0.5 1.0])
(def SCALES [1 2 4 8])
(def BETA 5.0)
(def METRICS ["variant" "l2" "cosine" "tv"])
(def EPS 1e-9)
(def OUT-METRICS ".build/py/ml/metrics_audio.json")
(def PROBE-NAMES ["input" "enc0" "enc1" "enc2" "enc3" "enc4"
                  "dec0" "dec1" "dec2" "dec3" "dec4" "output"])
(def RECIPES [[10.0 "white" 101] [5.0 "white" 202] [20.0 "color" 303]])
(def CMAP [[1 2 3] [2 3 4] [3 4 5] [4 5 0] [5 0 1] [0 1 2]])
(def U-PATHS [".build/py/ml/audio/u0.wav" ".build/py/ml/audio/u1.wav"
              ".build/py/ml/audio/u2.wav" ".build/py/ml/audio/u3.wav"
              ".build/py/ml/audio/u4.wav" ".build/py/ml/audio/u5.wav"])
(def WIN 4000)
(def SEED 0)
(def PROBE-SEQ [])

;; ---- small helpers -----------------------------------------------------------

(defn.py median [xs]
  (var n (len xs))
  (var s (sorted xs))
  (var h (int (/ n 2)))
  (var parity (- n (* 2 h)))
  (if (== parity 1)
    (return (. s [h]))
    (return (/ (+ (. s [(- h 1)]) (. s [h])) 2))))

(defn.py auc [labels scores]
  (try
    (return (. sklearn_metrics (roc_auc_score labels scores)))
    (catch err
      (return 0.5))))

(defn.py argmax [xs]
  (var bi 0)
  (var bv (. xs [0]))
  (var i 0)
  (for:array [v xs]
    (if (> v bv)
      (do
        (:= bv v)
        (:= bi i)))
    (:= i (+ i 1)))
  (return bi))

;; ---- hook + featurisation ----------------------------------------------------

(defn.py probe-hook [m i o]
  "Shared forward hook: appends each probe output to PROBE_SEQ in fire order
   (enc0..enc4 then dec0..dec4)."
  (. PROBE_SEQ (append o)))

(defn.py standardize [Z]
  "Per-channel standardisation over time so fixed thresholds are meaningful."
  (var mu (. Z (mean 0 :keepdim true)))
  (var sd (+ (. Z (std 0 :keepdim true)) 1e-6))
  (return (/ (- Z mu) sd)))

(defn.py prep [act]
  "Activation -> standardised [T, C] trajectory. Accepts [T], [1,C,T]."
  (var nd (len (. act shape)))
  (var a act)
  (if (== nd 3)
    (do
      (:= a (. act [0]))
      (:= a (. a (transpose 0 1)))))
  (if (== nd 1)
    (do
      (:= a (. act (reshape [(. act (size 0)) 1])))))
  (return (standardize a)))

(defn.py featurize [act]
  "Returns [variant mean-vector total-variation] for one probe activation."
  (var Z (prep act))
  (return [(. variant (variant-feature Z THRESHOLDS SCALES BETA))
           (. (. Z (abs)) (mean 0))
           (. features (total-variation (. Z (mean 1))))]))

(defn.py dist [a b metric]
  (if (== metric "variant")
    (return (. features (dist-l1 (. a [0]) (. b [0])))))
  (if (== metric "l2")
    (return (. features (dist-l2 (. a [1]) (. b [1])))))
  (if (== metric "cosine")
    (return (. features (dist-cosine (. a [1]) (. b [1])))))
  (if (== metric "tv")
    (return (. features (dist-l1 (. a [2]) (. b [2])))))
  (return (. features (dist-l1 (. a [0]) (. b [0])))))

(defn.py capture [model wav device]
  "Run one waveform through the denoiser; return dict probe-name -> activation."
  (var xw (. wav (to device)))
  (:= xw (. xw (reshape [1 (. xw (size 0))])))
  (. PROBE_SEQ (clear))
  (var out 0)
  (with [(. torch (no_grad))]
    (:= out (model xw)))
  (var d {})
  (. d (__setitem__ "input" wav))
  (. d (__setitem__ "enc0" (. PROBE_SEQ [0])))
  (. d (__setitem__ "enc1" (. PROBE_SEQ [1])))
  (. d (__setitem__ "enc2" (. PROBE_SEQ [2])))
  (. d (__setitem__ "enc3" (. PROBE_SEQ [3])))
  (. d (__setitem__ "enc4" (. PROBE_SEQ [4])))
  (. d (__setitem__ "dec0" (. PROBE_SEQ [5])))
  (. d (__setitem__ "dec1" (. PROBE_SEQ [6])))
  (. d (__setitem__ "dec2" (. PROBE_SEQ [7])))
  (. d (__setitem__ "dec3" (. PROBE_SEQ [8])))
  (. d (__setitem__ "dec4" (. PROBE_SEQ [9])))
  (. d (__setitem__ "output" out))
  (return d))

;; ---- experiment A: noise invariance per layer --------------------------------

(defn.py run-invariance [model U device]
  (var neg {})
  (var pos {})
  (for:array [pn PROBE-NAMES]
    (. neg (__setitem__ pn {"variant" [] "l2" [] "cosine" [] "tv" []}))
    (. pos (__setitem__ pn {"variant" [] "l2" [] "cosine" [] "tv" []})))
  (var fi 0)
  (for:array [u U]
    ;; anchor: this utterance + 10 dB white noise
    (var capa (capture model (. audio (add-noise u 10.0 (+ SEED fi) "white")) device))
    (var feata {})
    (for:array [pn PROBE-NAMES]
      (. feata (__setitem__ pn (featurize (. capa (get pn))))))
    ;; members: [waveform role], role 0 = benign (same utterance), 1 = counter
    (var members [])
    (for:array [rc RECIPES]
      (. members (append [(. audio (add-noise u (. rc [0]) (+ (. rc [2]) fi) (. rc [1]))) 0])))
    (var cmap (. CMAP [fi]))
    (for:array [ri (range 0 3)]
      (var j (. cmap [ri]))
      (var rc (. RECIPES [ri]))
      (var uj (. U [j]))
      ;; same noise recipe (matched stats) but independent realisation (+500)
      (. members (append [(. audio (add-noise uj (. rc [0]) (+ (. rc [2]) (+ fi 500)) (. rc [1]))) 1])))
    ;; per member: capture, featurise, distance-to-anchor per probe per metric
    (for:array [mem members]
      (var capm (capture model (. mem [0]) device))
      (var role (. mem [1]))
      (for:array [pn PROBE-NAMES]
        (var fm (featurize (. capm (get pn))))
        (var fa (. feata (get pn)))
        (for:array [metric METRICS]
          (var dd (. (dist fa fm metric) (item)))
          (if (== role 0)
            (do (. (. (. neg (get pn)) (get metric)) (append dd)))
            (do (. (. (. pos (get pn)) (get metric)) (append dd)))))))
    (:= fi (+ fi 1)))
  ;; aggregate per probe per metric
  (var report {})
  (for:array [pn PROBE-NAMES]
    (var pr {})
    (for:array [metric METRICS]
      (var bn (. (. neg (get pn)) (get metric)))
      (var cp (. (. pos (get pn)) (get metric)))
      (var labels [])
      (var scores [])
      (for:array [dd bn]
        (. labels (append 0))
        (. scores (append dd)))
      (for:array [dd cp]
        (. labels (append 1))
        (. scores (append dd)))
      (. pr (__setitem__ metric {"auc" (auc labels scores)
                                 "R" (/ (median cp) (+ (median bn) EPS))
                                 "median_benign" (median bn)
                                 "median_counter" (median cp)})))
    (. report (__setitem__ pn pr)))
  (return report))

;; ---- experiment B: artifact localisation -------------------------------------

(defn.py window-feats [Z w]
  "Feature triple of window w (WIN samples, non-overlapping) of Z [T, C]."
  (var s (* w WIN))
  (var Zw (. Z (narrow 0 s WIN)))
  (return [(. variant (variant-feature Zw THRESHOLDS SCALES BETA))
           (. (. Zw (abs)) (mean 0))
           (. features (total-variation (. Zw (mean 1))))]))

(defn.py localize [model clean corrupt device pos]
  "Per-window clean-vs-corrupt distances at input and output; argmax window per
   metric. Returns [[[pred_in pred_out] per metric] true-window]."
  (var capc (capture model corrupt device))
  (var capk (capture model clean device))
  (var T (. clean (size 0)))
  (var nwin (int (/ T WIN)))
  (var truew (int (/ pos WIN)))
  (var Zin-c (prep (. capc (get "input"))))
  (var Zin-k (prep (. capk (get "input"))))
  (var Zout-c (prep (. capc (get "output"))))
  (var Zout-k (prep (. capk (get "output"))))
  (var res {})
  (for:array [metric ["variant" "l2" "tv"]]
    (var din [])
    (var dout [])
    (for:array [w (range 0 nwin)]
      (var fi (window-feats Zin-c w))
      (var gi (window-feats Zin-k w))
      (. din (append (. (dist gi fi metric) (item))))
      (var fo (window-feats Zout-c w))
      (var go (window-feats Zout-k w))
      (. dout (append (. (dist go fo metric) (item)))))
    (. res (__setitem__ metric [(argmax din) (argmax dout)])))
  (return [res truew]))

(defn.py bump [d k hit]
  (var e (. d (get k)))
  (. e (__setitem__ "total" (+ 1 (. e (get "total")))))
  (if (== hit 1)
    (do (. e (__setitem__ "hits" (+ 1 (. e (get "hits"))))))))

(defn.py run-artifacts [model U device]
  (var M3 ["variant" "l2" "tv"])
  (var hits {})
  (for:array [metric M3]
    (for:array [lvl ["input" "output"]]
      (for:array [art ["click" "gap"]]
        (. hits (__setitem__ (+ (+ (+ metric "_") lvl) (+ "_" art))
                             {"hits" 0 "total" 0})))))
  (var i 0)
  (for:array [u U]
    (var pos (+ 4000 (* i 2800)))
    (var trials [[(. audio (add-click u pos)) "click"]
                 [(. audio (add-gap u pos 800)) "gap"]])
    (for:array [tr trials]
      (var loc (localize model u (. tr [0]) device pos))
      (var res (. loc [0]))
      (var truew (. loc [1]))
      (var art (. tr [1]))
      (for:array [metric M3]
        (var preds (. res (get metric)))
        (var ki (+ (+ (+ metric "_") "input") (+ "_" art)))
        (var ko (+ (+ (+ metric "_") "output") (+ "_" art)))
        (var hin 0)
        (if (== (. preds [0]) truew)
          (do (:= hin 1)))
        (var hout 0)
        (if (== (. preds [1]) truew)
          (do (:= hout 1)))
        (bump hits ki hin)
        (bump hits ko hout)))
    (:= i (+ i 1)))
  (return hits))

;; ---- main ---------------------------------------------------------------------

(defn.py main []
  (var device "cpu")
  (if (. torch.cuda (is_available))
    (:= device "cuda"))
  (var model (. audio (load-model device)))
  (for:array [i (range 0 5)]
    (. (. (. model encoder) [i]) (register_forward_hook probe-hook))
    (. (. (. model decoder) [i]) (register_forward_hook probe-hook)))
  (var U [])
  (for:array [p U-PATHS]
    (. U (append (. audio (load-wav p)))))
  (var rep-inv (run-invariance model U device))
  (var rep-art (run-artifacts model U device))
  (var report {"model" "dns64"
               "device" device
               "invariance" rep-inv
               "artifacts" rep-art})
  (var f (open OUT-METRICS "w"))
  (. json (dump report f))
  (. f (close))
  (print (. json (dumps report :indent 2))))
