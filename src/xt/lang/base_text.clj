(ns xt.lang.base-text
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-string :as common-string]]})

(defspec.xt tag-string
  [:fn [:xt/str] :xt/str])

(defn.xt tag-string
  "gets the string description for a given tag"
  {:added "4.0"}
  [tag]
  (var [ns name] (common-string/sym-pair tag))
  (var parts (x:str-split (or ns "") "."))
  (var part-count (x:len parts))
  (var desc (:? ns
                (x:cat (x:get-idx parts (+ part-count
                                           (x:offset -1)))
                       " ")
                ""))
  (var clean-name (x:str-replace (or name "") "_" " "))
  (:= clean-name (x:str-replace clean-name "-" " "))
  (:= clean-name (x:str-replace clean-name (x:str-trim desc) ""))
  (return (x:cat desc clean-name)))
