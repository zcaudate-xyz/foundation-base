(ns python.ml.evaluate
  "Eval harness for the Option A causal test, emitted via the hara/xtalk DSL.

   Scores three models on the held-out test clips at fixed SNRs (5, 10 dB) with
   fixed noise seeds: untouched dns64, baseline fine-tune (lambda=0), variant
   fine-tune (lambda=0.1). Metrics: SI-SDR (dB) and STOI. Decision rule lives
   in the plan; this module only produces the table."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["glob" :as glob]
            ["audio" :as audio]
            ["metrics" :as metrics]]})

;; ---- config (rendered into the emitted file's preamble) ---------------------

(def TEST-DIR ".build/py/ml/audio/test")
(def SNRS [5.0 10.0])
(def SEED 1234)
(def BASE-CKPT ".build/py/ml/ckpt/baseline.pt")
(def VAR-CKPT ".build/py/ml/ckpt/variant.pt")
(def VAR10-CKPT ".build/py/ml/ckpt/variant10.pt")
(def OUT-EVAL ".build/py/ml/metrics_finetune.json")

;; ---- helpers ------------------------------------------------------------------

(defn.py load-dir [d]
  (var paths (sorted (. glob (glob (+ d "/*.wav")))))
  (var out [])
  (for:array [p paths]
    (. out (append (. audio (load-wav p)))))
  (return out))

(defn.py load-ckpt [device path]
  (var m (. audio (load-model device)))
  (. m (load_state_dict (. torch (load path))))
  (return m))

(defn.py eval-model [model wavs snr seed device]
  "Returns [mean SI-SDR (dB), mean STOI] over wavs at a fixed SNR."
  (var s-sum 0.0)
  (var t-sum 0.0)
  (var n 0)
  (for:array [u wavs]
    (var noisy (. audio (add-noise u snr (+ seed n) "white")))
    (var xw (. (. noisy (to device)) (reshape [1 (. noisy (size 0))])))
    (var out 0)
    (with [(. torch (no_grad))]
      (:= out (model xw)))
    (var xhat (. out (reshape [1 (. noisy (size 0))])))
    (var xc (. (. u (reshape [1 (. u (size 0))])) (to device)))
    (:= s-sum (+ s-sum (. (. metrics (si-sdr xhat xc)) (item))))
    (:= t-sum (+ t-sum (. metrics (stoi-score xc xhat))))
    (:= n (+ n 1)))
  (return [(/ s-sum n) (/ t-sum n)]))

;; ---- main ----------------------------------------------------------------------

(defn.py main []
  (var device "cpu")
  (if (. torch.cuda (is_available))
    (:= device "cuda"))
  (var wavs (load-dir TEST-DIR))
  (var models {})
  (. models (__setitem__ "untouched" (. audio (load-model device))))
  (. models (__setitem__ "baseline" (load-ckpt device BASE-CKPT)))
  (. models (__setitem__ "variant_0.1" (load-ckpt device VAR-CKPT)))
  (. models (__setitem__ "variant_1.0" (load-ckpt device VAR10-CKPT)))
  (var report {})
  (for:array [name ["untouched" "baseline" "variant_0.1" "variant_1.0"]]
    (var m (. models (get name)))
    (var row {})
    (for:array [snr SNRS]
      (var scores (eval-model m wavs snr SEED device))
      (. row (__setitem__ (str snr) {"si_sdr" (. scores [0]) "stoi" (. scores [1])}))
      (print name snr (. scores [0]) (. scores [1])))
    (. report (__setitem__ name row)))
  (var f (open OUT-EVAL "w"))
  (. json (dump report f))
  (. f (close))
  (print (. json (dumps report :indent 2))))
