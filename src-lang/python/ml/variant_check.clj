(ns python.ml.variant-check
  "Emitted, runnable check: soft k-measure at large beta over fixed bitstrings,
   printed as JSON. The Clojure test compares these integers to the validated
   hard `jvm.chisel.variant.measure/k-measure-ref`. Run from the directory
   containing the emitted `variant.py` (qualified `import variant`)."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["json" :as json]
            ["variant" :as variant]]})

(defn.py bits-to-tensor [bits]
  (return (. torch (tensor bits :dtype (. torch float32)))))

(defn.py soft-row [bits]
  (var z (bits-to-tensor bits))
  (var v (. variant (k-measure-soft z 0.5 1000.0)))
  (return (. (. v (round)) (tolist))))

(defn.py main []
  (var out [(soft-row [1 0 1])
            (soft-row [1 0 1 0 1 1])
            (soft-row [1 1 1 1 1 1 1 1])
            (soft-row [0 0 0 0 0])
            (soft-row [1 0 1 0 1 0 1 0])
            (soft-row [1 1 0 0 1 0 1 1 0 1])])
  (print (. json (dumps out))))
