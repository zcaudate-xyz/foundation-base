(ns python.ml.diagnose
  "End-to-end Phase-1 diagnostic driver (emitted via the hara/xtalk DSL).

   Correctness-free hypothesis: meaning-preserving paraphrases should sit CLOSE
   to the anchor in multi-scale variant space, while meaning-changing
   counterfactuals should be FAR. Per metric we report:
     AUC  — separation of benign (label 0) vs counterfactual (label 1) by distance
     R    — selective-invariance ratio = median(D_counter) / median(D_benign)
   If variant distance separates the two classes better than hidden-state
   L2/cosine/TV baselines, the variant representation carries stability signal.

   Scope: prompt-side hidden states (no generation). Default model is tiny
   (distilgpt2) for a fast smoke run; override MODEL for a capable model."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["sklearn.metrics" :as sklearn_metrics]
            ["variant" :as variant]
            ["capture" :as capture]
            ["features" :as features]]})

;; ---- config (override at the call site) -----------------------------------

(def MODEL "Qwen/Qwen2.5-0.5B-Instruct")
(def LAYERS [5 11 17 23])
(def PROJ-DIM 32)
(def THRESHOLDS [-1.0 -0.5 0.0 0.5 1.0])
(def SCALES [1 2 4])
(def BETA 5.0)
(def SEED 0)
(def CACHE-DIR ".build/py/ml/cache")
(def OUT-METRICS ".build/py/ml/metrics.json")
(def METRICS ["variant" "l2" "cosine" "tv"])
(def EPS 1e-9)

;; ---- prompt families: [anchor [benign ...] [counter ...]] ------------------

(def FAMILIES
  ;; Benign = same answer, full reword (lexically far from anchor).
  ;; Counter = DIFFERENT problem/answer, ALSO a full reword (lexically far).
  ;; This matches lexical distance between the two classes so only meaning
  ;; (same vs different answer) separates them.
  [["What is 7 plus 5?"
    ["Add seven and five." "Compute the sum of 7 and 5." "Seven plus five equals what?"]
    ["What is 12 minus 4?" "Compute 6 times 3." "Divide twenty by four."]]
   ["What is 12 minus 4?"
    ["Subtract four from twelve." "Compute 12 take away 4." "Twelve minus four is what?"]
    ["What is 9 plus 8?" "Multiply six by three." "What is 20 divided by 4?"]]
   ["What is 6 times 3?"
    ["Multiply six by three." "Compute the product of 6 and 3." "Six times three equals what?"]
    ["What is 15 minus 7?" "Add nine and eight." "Divide twenty by four."]]
   ["What is 20 divided by 4?"
    ["Divide twenty by four." "Compute 20 over 4." "Twenty split into four parts is?"]
    ["What is 7 plus 5?" "Subtract seven from fifteen." "Multiply six by three."]]
   ["What is 9 plus 8?"
    ["Add nine and eight." "Compute the sum of 9 and 8." "Nine plus eight equals what?"]
    ["What is 12 minus 4?" "Compute 6 times 3." "Divide twenty by four."]]
   ["What is 15 minus 7?"
    ["Subtract seven from fifteen." "Compute 15 take away 7." "Fifteen minus seven is what?"]
    ["What is 9 plus 8?" "Multiply six by three." "What is 20 divided by 4?"]]])

;; ---- small helpers ---------------------------------------------------------

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

;; ---- feature extraction ----------------------------------------------------

(defn.py prompt-features [cap]
  "cap = list of [layer Z] (Z: [T, proj]). Returns [vfeat hfeat tv] tensors."
  (var vp [])
  (var hp [])
  (var tp [])
  (for:array [lz cap]
    (var Z (. lz [1]))
    (. vp (append (. variant (variant-feature Z THRESHOLDS SCALES BETA))))
    (. hp (append (. Z (mean 0))))
    (. tp (append (. features (total-variation (. Z (mean 1)))))))
  (return [(. torch (cat vp)) (. torch (cat hp)) (. torch (stack tp))]))

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

;; ---- build the flat prompt list --------------------------------------------

(defn.py build-prompts []
  "Returns [ids texts]; ids encode family/role: f{i}_a (anchor), f{i}_b{j}
   (benign), f{i}_c{j} (counter)."
  (var ids [])
  (var texts [])
  (var fi 0)
  (for:array [famdata FAMILIES]
    (var anchor (. famdata [0]))
    (var benign (. famdata [1]))
    (var counter (. famdata [2]))
    (var aid (+ (+ "f" (str fi)) "_a"))
    (. ids (append aid))
    (. texts (append anchor))
    (var bj 0)
    (for:array [txt benign]
      (var bid (+ (+ (+ "f" (str fi)) "_b") (str bj)))
      (. ids (append bid))
      (. texts (append txt))
      (:= bj (+ bj 1)))
    (var cj 0)
    (for:array [txt counter]
      (var cid (+ (+ (+ "f" (str fi)) "_c") (str cj)))
      (. ids (append cid))
      (. texts (append txt))
      (:= cj (+ cj 1)))
    (:= fi (+ fi 1)))
  (return [ids texts]))

(defn.py role-of [pid]
  "1 = counterfactual, 0 = benign (by id convention)."
  (var parts (. pid (split "_")))
  (var tag (. parts [1]))
  (if (. tag (startswith "c"))
    (return 1)
    (return 0)))

;; ---- main ------------------------------------------------------------------

(defn.py main []
  (var device "cpu")
  (if (. torch.cuda (is_available))
    (:= device "cuda"))
  (var lm (. capture (load-model MODEL device)))
  (var tok (. lm [0]))
  (var model (. lm [1]))
  (var dim (. capture (model-dim model)))
  (var P (. capture (make-projection dim PROJ-DIM SEED)))
  (:= P (. P (to device)))
  (var built (build-prompts))
  (var ids (. built [0]))
  (var texts (. built [1]))
  (var layers LAYERS)
  ;; capture everything to disk
  (var pairs [])
  (var k 0)
  (for:array [txt texts]
    (. pairs (append [(. ids [k]) txt]))
    (:= k (+ k 1)))
  (. capture (capture-all model tok pairs layers P device CACHE-DIR))
  ;; load + featurise every prompt
  (var feats {})
  (for:array [pid ids]
    (var cap (. torch (load (+ (+ CACHE-DIR "/") (+ pid ".pt")))))
    (. feats (__setitem__ pid (prompt-features cap))))
  ;; per family: distances of benign/counter to the anchor, per metric
  (var neg {"variant" [] "l2" [] "cosine" [] "tv" []})
  (var pos {"variant" [] "l2" [] "cosine" [] "tv" []})
  (var fi 0)
  (for:array [famdata FAMILIES]
    (var aid (+ (+ "f" (str fi)) "_a"))
    (var afeat (. feats (get aid)))
    (var bj 0)
    (for:array [txt (. famdata [1])]
      (var bid (+ (+ (+ "f" (str fi)) "_b") (str bj)))
      (var bfeat (. feats (get bid)))
      (for:array [metric METRICS]
        (var d (. (dist afeat bfeat metric) (item)))
        (. (. neg (get metric)) (append d)))
      (:= bj (+ bj 1)))
    (var cj 0)
    (for:array [txt (. famdata [2])]
      (var cid (+ (+ (+ "f" (str fi)) "_c") (str cj)))
      (var cfeat (. feats (get cid)))
      (for:array [metric METRICS]
        (var d (. (dist afeat cfeat metric) (item)))
        (. (. pos (get metric)) (append d)))
      (:= cj (+ cj 1)))
    (:= fi (+ fi 1)))
  ;; aggregate: AUC + selective-invariance ratio R per metric
  (var report {"model" MODEL "device" device "n_benign" 0 "n_counter" 0 "metrics" {}})
  (for:array [metric METRICS]
    (var bn (. neg (get metric)))
    (var cp (. pos (get metric)))
    (var labels [])
    (var scores [])
    (for:array [d bn]
      (. labels (append 0))
      (. scores (append d)))
    (for:array [d cp]
      (. labels (append 1))
      (. scores (append d)))
    (var a (auc labels scores))
    (var r (/ (median cp) (+ (median bn) EPS)))
    (. (. report (get "metrics"))
       (__setitem__ metric {"auc" a
                            "R" r
                            "median_benign" (median bn)
                            "median_counter" (median cp)})))
  (. report (__setitem__ "n_benign" (len (. neg (get "variant")))))
  (. report (__setitem__ "n_counter" (len (. pos (get "variant")))))
  (var f (open OUT-METRICS "w"))
  (. json (dump report f))
  (. f (close))
  (print (. json (dumps report :indent 2))))
