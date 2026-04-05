(ns code.doc.manage
  (:require [code.doc.parse :as parse]
            [code.project :as project]))

(defn source-namespaces
  "returns all source namespaces for code.doc coverage"
  {:added "4.1"}
  ([project]
   (let [inclusions [(str (project/file-suffix) "$")]]
     (sort (keys (project/all-files (:source-paths project)
                                    {:include inclusions}
                                    project))))))

(defn element-namespaces
  "extracts documented namespaces from a parsed element"
  {:added "4.1"}
  [{:keys [type namespace refer ns link]}]
  (cond-> #{}
    namespace (conj (symbol namespace))
    refer     (conj (symbol (.getNamespace (symbol refer))))
    (#{:ns :ns-form} type) (conj ns)
    link      (conj (symbol link))))

(defn documented-coverage
  "returns a namespace -> pages coverage map"
  {:added "4.1"}
  ([{:keys [publish root] :as project}]
   (reduce-kv
    (fn [coverage site site-config]
      (reduce-kv
       (fn [coverage page page-config]
         (let [page-id (symbol (name site) (name page))
               elements (parse/parse-file (:input page-config) {:root root})]
           (reduce (fn [coverage ns]
                     (update coverage ns (fnil conj []) page-id))
                   coverage
                   (mapcat element-namespaces elements))))
       coverage
       (:pages site-config)))
    {}
    (:sites publish))))

(defn missing-namespaces
  "returns a marker for namespaces that are not referenced by code.doc pages"
  {:added "4.1"}
  ([ns _ _ {:code.doc/keys [coverage]}]
   (if-not (contains? coverage ns)
     [:missing-code-doc])))
