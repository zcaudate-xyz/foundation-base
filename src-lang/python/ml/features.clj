(ns python.ml.features
  "Distance / baseline features over hidden-state trajectories and variant
   feature vectors (PyTorch), emitted via the hara/xtalk DSL. All vectorised
   (no Python loops); callers loop over channels/layers."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]]})

;; --- distances between two equal-shaped feature vectors ---

(defn.py dist-l1 [a b]
  (return (. (. (- a b) (abs)) (sum))))

(defn.py dist-l2 [a b]
  (return (. torch (norm (- a b)))))

(defn.py dist-cosine [a b]
  (var na (. torch (norm a)))
  (var nb (. torch (norm b)))
  (return (- 1 (/ (. torch (dot a b)) (* na nb)))))

;; --- single-trajectory baselines over a 1-D tensor z ---

(defn.py total-variation [z]
  (return (. (. (. torch (diff z)) (abs)) (sum))))

(defn.py traj-l2 [a b]
  "L2 between two 1-D trajectories (hidden-state L2 baseline)."
  (return (. torch (norm (- a b)))))

;; --- distribution baselines over next-token logits ---

(defn.py entropy [logits]
  (var p (. torch (softmax logits :dim -1)))
  (return (- (. (* p (. torch (log p))) (sum)))))

(defn.py kl [p-logits q-logits]
  "KL(softmax(p) || softmax(q))."
  (var p (. torch (softmax p-logits :dim -1)))
  (var q (. torch (softmax q-logits :dim -1)))
  (return (. (* p (- (. torch (log p)) (. torch (log q)))) (sum))))
