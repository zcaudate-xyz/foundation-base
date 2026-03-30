(ns std.lang.model.spec-xtalk.mixer
  (:require [std.lang.typed.xtalk-analysis :as analysis]
            [std.lang.typed.xtalk-parse :as parse]))

(defn mix-analysis
  "Target-side seam for producing typed declaration input from xtalk analysis."
  {:added "4.1"}
  ([analysis]
   (mix-analysis analysis {}))
  ([analysis _opts]
   (parse/attach-specs analysis)))

(defn mix-file
  {:added "4.1"}
  ([file-path]
   (mix-file file-path {}))
  ([file-path opts]
   (-> file-path
       analysis/analyze-file-raw
       (mix-analysis opts))))

(defn mix-namespace
  {:added "4.1"}
  ([ns-sym]
   (mix-namespace ns-sym {}))
  ([ns-sym opts]
   (-> ns-sym
        analysis/analyze-namespace-raw
        (mix-analysis opts))))
