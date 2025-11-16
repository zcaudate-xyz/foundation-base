(ns xt.lang.base-text
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]]})

(defn.xt tag-string
  "gets the string description for a given tag"
  {:added "4.0"}
  [tag]
  (var [ns name] (k/sym-pair tag))
  (var desc (:? ns
                (k/cat (k/last (k/split ns "."))
                       " ")
                ""))
  (return (k/cat desc
                 (-> (or name "")
                     (k/replace "_" " ")
                     (k/replace "-" " ")
                     (k/replace (k/trim desc) "")))))

