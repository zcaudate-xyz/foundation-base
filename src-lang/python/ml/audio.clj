(ns python.ml.audio
  "Audio utilities + Demucs (dns64) loading for the variant denoiser diagnostic,
   emitted to Python via the hara/xtalk DSL.

   All corruptions run on CPU float32 mono waveforms of shape [T] at 16 kHz.
   Artifacts (click / gap / clip) are ground-truth breaks of invariance: they
   are what the variant measure should spike on."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["soundfile" :as sf]
            ["torchaudio.functional" :as taf]
            ["denoiser.pretrained" :as dpre]]})

;; ---- IO ---------------------------------------------------------------------

(defn.py load-wav [path]
  "Load a mono wav as a float32 tensor [T]."
  (var r (. sf (read path)))
  (return (. torch (tensor (. r [0]) :dtype (. torch float32)))))

;; ---- noise ------------------------------------------------------------------

(defn.py rms [x]
  (return (pow (. (* x x) (mean)) 0.5)))

(defn.py white-noise [n seed]
  (var g (. torch (Generator)))
  (. g (manual_seed seed))
  (return (. torch (randn [n] :generator g))))

(defn.py color-noise [n seed]
  "Lowpassed (2 kHz) white noise, i.e. a coloured noise texture."
  (var w (white-noise n seed))
  (return (. taf (lowpass_biquad w 16000 2000.0))))

(defn.py add-noise [x snr seed kind]
  "x + noise at `snr` dB. kind = 'white' or 'color'. Noise rms is scaled so
   that rms(x)/rms(noise) == 10^(snr/20)."
  (var T (. x (size 0)))
  (var n (white-noise T seed))
  (if (== kind "color")
    (do (:= n (color-noise T seed))))
  (var scale (/ (rms x) (* (pow 10.0 (/ snr 20.0)) (+ (rms n) 1e-8))))
  (return (+ x (* scale n))))

;; ---- artifacts (ground-truth invariance breaks) -----------------------------

(defn.py add-click [x pos]
  "One click (impulse, amplitude 0.9) at sample `pos`."
  (var y (. x (clone)))
  (. y (index_fill_ 0 (. torch (tensor [pos])) 0.9))
  (return y))

(defn.py add-gap [x pos glen]
  "Zero the span [pos, pos+glen) (packet loss)."
  (var y (. x (clone)))
  (. y (index_fill_ 0 (. torch (arange pos (+ pos glen))) 0.0))
  (return y))

(defn.py add-clip [x frac]
  "Hard-limit x to ±frac."
  (return (. x (clamp (* -1.0 frac) frac))))

;; ---- model ------------------------------------------------------------------

(defn.py load-model [device]
  "Pretrained Demucs dns64 denoiser on `device`, eval mode."
  (var m (. dpre (dns64)))
  (:= m (. m (to device)))
  (. m (eval))
  (return m))
