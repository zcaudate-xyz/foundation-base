(ns python.ml.metrics
  "Denoising metrics + training losses (PyTorch), emitted via the hara/xtalk DSL.

   SI-SDR and STOI are the quality metrics (eval only, no grad needed through
   STOI). waveform-l1 and multi-resolution STFT magnitude L1 are the standard
   training losses used as the matched baseline for the variant-loss run."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["pystoi" :as pystoi]]})

;; ---- training losses ---------------------------------------------------------

(defn.py waveform-l1 [xhat x]
  "Mean |xhat - x| for batches [B, T]."
  (return (. (. (- xhat x) (abs)) (mean))))

(defn.py stft-mag [x nfft hop]
  "|STFT| magnitude for a batch [B, T]."
  (var w (. torch (hann_window nfft :device (. x device))))
  (var X (. torch (stft x nfft hop :window w :return_complex true)))
  (return (. X (abs))))

(defn.py stft-loss [xhat x]
  "Multi-resolution STFT magnitude L1 (1024/512/256)."
  (var l (waveform-l1 (stft-mag xhat 1024 256) (stft-mag x 1024 256)))
  (:= l (+ l (waveform-l1 (stft-mag xhat 512 128) (stft-mag x 512 128))))
  (:= l (+ l (waveform-l1 (stft-mag xhat 256 64) (stft-mag x 256 64))))
  (return l))

;; ---- quality metrics ----------------------------------------------------------

(defn.py si-sdr [xhat x]
  "Scale-invariant SDR (dB) for batches [B, T]; higher is better."
  (var xh (- xhat (. xhat (mean 1 :keepdim true))))
  (var xs (- x (. x (mean 1 :keepdim true))))
  (var dot (. (* xh xs) (sum 1 :keepdim true)))
  (var energy (. (* xs xs) (sum 1 :keepdim true)))
  (var proj (* (/ dot (+ energy 1e-8)) xs))
  (var e (- xh proj))
  (var ratio (/ (. (* proj proj) (sum 1)) (+ (. (* e e) (sum 1)) 1e-8)))
  (return (. (* 10.0 (. torch (log10 (+ ratio 1e-8)))) (mean))))

(defn.py stoi-score [clean hat]
  "Mean STOI over a batch [B, T] via pystoi (clean first)."
  (var B (. clean (size 0)))
  (var total 0.0)
  (for:array [i (range 0 B)]
    (var c (. (. (. clean [i]) (detach)) (cpu)))
    (var h (. (. (. hat [i]) (detach)) (cpu)))
    (:= total (+ total (. pystoi (stoi (. c (numpy)) (. h (numpy)) 16000)))))
  (return (/ total B)))
