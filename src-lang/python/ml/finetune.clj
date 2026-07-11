(ns python.ml.finetune
  "Option A causal test: fine-tune dns64 with a soft variant reconstruction
   loss, emitted via the hara/xtalk DSL.

   Encoder is frozen; BLSTM + decoder are trained. Loss per step:
     L = waveform-l1 + stft-loss + LAMBDA * variant-loss
   where variant-loss = mean over the batch of
     |variant_feature(standardize(xhat)) - variant_feature(standardize(clean))|_1
   reusing the differentiable multiscale soft k-measure from variant.clj.

   LAMBDA = 0  -> matched baseline run (identical data/noise seeds).
   LAMBDA = 0.1 -> variant run. CKPT-OUT selects where the weights land."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["glob" :as glob]
            ["audio" :as audio]
            ["variant" :as variant]
            ["metrics" :as metrics]]})

;; ---- config (rendered into the emitted file's preamble) ---------------------

(def LAMBDA 0.1)
(def STEPS 1000)
(def BATCH 8)
(def LR 1e-4)
(def SEED 0)
(def TRAIN-DIR ".build/py/ml/audio/train")
(def CKPT-OUT ".build/py/ml/ckpt/variant.pt")
(def LOSSES-OUT ".build/py/ml/ckpt/variant_losses.json")
(def THRESHOLDS [-1.0 -0.5 0.0 0.5 1.0])
(def SCALES [1 2 4 8])
(def BETA 5.0)
(def SNR-LO 0.0)
(def SNR-HI 20.0)

;; ---- helpers -----------------------------------------------------------------

(defn.py imod [a n]
  (return (- a (* n (int (/ a n))))))

(defn.py freeze-encoder [model]
  "requires_grad=False on every encoder.* parameter (BLSTM+decoder stay live)."
  (for:array [npair (. model (named_parameters))]
    (var pname (. npair [0]))
    (var p (. npair [1]))
    (if (. pname (startswith "encoder"))
      (do (. p (requires_grad_ false))))))

(defn.py load-dir [d]
  (var paths (sorted (. glob (glob (+ d "/*.wav")))))
  (var out [])
  (for:array [p paths]
    (. out (append (. audio (load-wav p)))))
  (return out))

(defn.py standardize1 [z]
  "Standardise a [T, 1] trajectory over time (differentiable)."
  (var mu (. z (mean 0 :keepdim true)))
  (var sd (+ (. z (std 0 :keepdim true)) 1e-6))
  (return (/ (- z mu) sd)))

(defn.py variant-loss [xhat clean]
  "Mean over batch of |V(xhat) - V(clean)|_1 on standardised waveforms."
  (var B (. xhat (size 0)))
  (var T (. xhat (size 1)))
  (var total 0.0)
  (for:array [i (range 0 B)]
    (var zh (standardize1 (. (. xhat [i]) (reshape [T 1]))))
    (var zc (standardize1 (. (. clean [i]) (reshape [T 1]))))
    (var fh (. variant (variant-feature zh THRESHOLDS SCALES BETA)))
    (var fc (. variant (variant-feature zc THRESHOLDS SCALES BETA)))
    (:= total (+ total (. (. (- fh fc) (abs)) (mean)))))
  (return (/ total B)))

;; ---- training -----------------------------------------------------------------

(defn.py train-step [model opt clips step device]
  "One matched training step: deterministic noise seeds per (step, clip) so the
   baseline and variant runs see identical data."
  (var noisy-list [])
  (var clean-list [])
  (var k 0)
  (for:array [c clips]
    (var u (. (. torch (rand [1])) (item)))
    (var snr (+ SNR-LO (* (- SNR-HI SNR-LO) u)))
    (var kind "white")
    (var parity (imod k 2))
    (if (== parity 1)
      (do (:= kind "color")))
    (. noisy-list (append (. audio (add-noise c snr (+ (* 1000 step) k) kind))))
    (. clean-list (append c))
    (:= k (+ k 1)))
  (var nb (. (. torch (stack noisy-list)) (to device)))
  (var cb (. (. torch (stack clean-list)) (to device)))
  (. opt (zero_grad))
  (var out (model nb))
  (var xhat (. out (reshape [(. nb (size 0)) (. nb (size 1))])))
  (var loss (+ (. metrics (waveform-l1 xhat cb)) (. metrics (stft-loss xhat cb))))
  (if (> LAMBDA 0.0)
    (do (:= loss (+ loss (* LAMBDA (variant-loss xhat cb))))))
  (. loss (backward))
  (. opt (step))
  (return (. loss (item))))

(defn.py main []
  (var device "cpu")
  (if (. torch.cuda (is_available))
    (:= device "cuda"))
  (var model (. audio (load-model device)))
  (. model (train))
  (freeze-encoder model)
  (var opt (. torch.optim (Adam (. model (parameters)) :lr LR)))
  (var wavs (load-dir TRAIN-DIR))
  (var n (len wavs))
  (var g (. torch (Generator)))
  (. g (manual_seed SEED))
  (var order (. torch (randperm n :generator g)))
  (var losses [])
  (for:array [s (range 0 STEPS)]
    (var clips [])
    (for:array [j (range 0 BATCH)]
      (var oi (imod (+ (* s BATCH) j) n))
      (var wi (int (. (. order [oi]) (item))))
      (. clips (append (. wavs [wi]))))
    (var l (train-step model opt clips s device))
    (if (== (imod s 50) 0)
      (do
        (. losses (append [s l]))
        (print s l))))
  (. torch (save (. model (state_dict)) CKPT-OUT))
  (var f (open LOSSES-OUT "w"))
  (. json (dump losses f))
  (. f (close))
  (print (+ "saved " CKPT-OUT)))
