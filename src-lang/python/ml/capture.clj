(ns python.ml.capture
  "Teacher-forced hidden-state capture from a HuggingFace causal model, emitted
   via the hara/xtalk DSL. Loads the model/tokenizer through qualified access
   (the DSL has no `from X import Y`), runs forward passes under
   `torch.no_grad()`, projects each chosen layer's token trajectory with a fixed
   random projection, and saves per-prompt projections to disk. Tensors never
   cross the Clojure boundary — only paths/metadata do."
  (:require [hara.lang :as l]))

(l/script :python
  {:import [["torch" :as torch]
            ["transformers" :as transformers]]})

(defn.py load-model [name device]
  (var tok (. (. transformers AutoTokenizer) (from_pretrained name)))
  (var model (. (. transformers AutoModel) (from_pretrained name)))
  (. model (to device))
  (. model (eval))
  (return [tok model]))

(defn.py model-dim [model]
  (return (. (. model config) hidden_size)))

(defn.py make-projection [dim proj-dim seed]
  (. torch (manual_seed seed))
  (var P (. torch (randn dim proj-dim)))
  (return (/ P (pow dim 0.5))))

(defn.py capture-one [model tok text layers P device]
  "Forward `text`; return a list of [layer Z] where Z is [T, proj-dim] for each
   requested layer (hidden_states index = layer + 1; 0 is the embedding layer)."
  (var enc (tok text :return_tensors "pt"))
  (:= enc (. enc (to device)))
  (var out nil)
  (with [(. torch (no_grad))]
    (:= out (model (:** enc) :output_hidden_states true)))
  (var hs (. out hidden_states))
  (var rows [])
  (for:array [layer layers]
    (var H (. hs [(+ layer 1)]))
    (var H0 (. (. H [0]) (float)))
    (var Z (. torch (matmul H0 P)))
    (. rows (append [layer Z])))
  (return rows))

(defn.py capture-all [model tok pairs layers P device cache-dir]
  "pairs = list of [id text]. Saves each capture to <cache-dir>/<id>.pt; returns
   a list of {\"id\" .. \"path\" ..} metadata dicts."
  (var meta [])
  (for:array [p pairs]
    (var cap (capture-one model tok (. p [1]) layers P device))
    (var path (+ cache-dir "/" (. p [0]) ".pt"))
    (. torch (save cap path))
    (. meta (append {"id" (. p [0]) "path" path})))
  (return meta))
