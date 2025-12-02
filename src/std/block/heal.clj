(ns std.block.heal
  (:require [std.block.heal.core :as core]
            [std.block.heal.print :as print]
            [std.block.heal.parse :as parse]
            [std.lib :as h :refer [definvoke]]))

(h/intern-in [heal core/heal-content])

(defn print-rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))

(defn rainbow
  "prints out the code in rainbow"
  {:added "4.0"}
  [content]
  (h/with-out-str
    (print-rainbow content)))
