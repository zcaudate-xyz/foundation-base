(ns code.layout
  (:require [std.block.base :as base]
            [std.block.construct :as construct]
            [std.block.type :as type]
            [std.block.parse :as parse]
            [std.block :as block])
  (:refer-clojure :exclude [next replace type]))

(def *max-row-length* 80)


(defn layout-form)





























(comment

  (block/parse-string
   "\"
\" ")

  (= (block/info (block/block
                  "

"))

     (block/info
      (block/block
       "\n\n")))

  (block/parse-string
   "\"\\n\""))
