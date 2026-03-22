(ns code.doc.link.chapter
  (:require [std.lib.collection :as collection]))

(defn link-chapters
  "links each chapter to each of the elements"
  {:added "3.0"}
  ([interim name]
   (let [apis (->> (get-in interim [:articles name :elements])
                   (filter #(-> % :type (= :api)))
                   (collection/map-juxt [:namespace :table]))]
     (update-in interim [:articles name :elements]
                (fn [elements]
                  (mapv (fn [{:keys [type link] :as element}]
                          (if (= type :chapter)
                            (assoc element :table (get apis link))
                            element))
                        elements))))))
