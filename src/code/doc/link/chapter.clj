(ns code.doc.link.chapter
  (:require [code.doc.engine.plugin.api :as api]))

(defn link-chapters
  "links each chapter to the api tables contained within it"
  {:added "3.0"}
  ([interim name]
   (update-in interim [:articles name :elements]
              (fn [elements]
                (loop [i 0
                       current nil
                       current-idx nil
                       elements elements]
                  (cond (>= i (count elements))
                        elements

                        (= :chapter (:type (nth elements i)))
                        (recur (inc i) (nth elements i) i elements)

                        (and (= :api (:type (nth elements i))) current)
                        (let [api (nth elements i)
                              entries (api/select-entries api)
                              filtered-table (select-keys (:table api) entries)
                              updated (update current :table merge filtered-table)]
                          (recur (inc i) updated current-idx (assoc elements current-idx updated)))

                        :else
                        (recur (inc i) current current-idx elements)))))))
