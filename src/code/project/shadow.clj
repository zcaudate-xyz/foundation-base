(ns code.project.shadow
  (:require [code.project.common :as common]
            [std.fs :as fs]))

(def ^:dynamic *shadow-file* "shadow-cljs.edn")

(defn project
  "opens a shadow.edn file as the project
 
   (project \"../yin/shadow-cljs.edn\")"
  {:added "3.0"}
  ([] (project *shadow-file*))
  ([shadow-file]
   (if-let [proj (-> shadow-file
                     slurp
                     read-string)]
     (let [root    (-> shadow-file fs/path fs/parent str)
           proj (-> proj
                    (assoc :root root)
                    (merge (common/artifact (:name proj)))
                    (->> (merge common/+defaults+)))]
       proj))))
