(ns code.project.lein
  (:require [code.project.common :as common]
            [std.fs :as fs]))

(def ^:dynamic *project-file* "project.clj")

(defn project
  "returns the root project map
 
   (project)
   => map?"
  {:added "3.0"}
  ([] (project *project-file*))
  ([project-file]
   (let [proj-form (-> project-file
                       slurp
                       (#(str "[" % "]"))
                       read-string
                       (->> (filter (fn [form]
                                      (and (list? form)
                                           (= (first form) 'defproject)))))
                       first)
         root  (-> project-file fs/path fs/parent str)
         [_ full version] (take 3 proj-form)
         entry (common/artifact full)
         proj  (-> (apply hash-map (drop 3 proj-form))
                   (merge entry
                          {:version version
                           :root root})
                   (->> (merge common/+defaults+)))]
     proj)))

(defn project-name
  "returns the project name
 
   (project-name)
   => symbol?"
  {:added "3.0"}
  ([] (project-name *project-file*))
  ([project-file]
   (:name (project project-file))))
