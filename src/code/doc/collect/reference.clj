(ns code.doc.collect.reference
  (:require [code.framework :as code.framework]
            [code.framework.common :as common]
            [std.fs :as fs]
            [std.lib.collection]))

(defn find-import-namespaces
  "finds forms with `(module/include ...)`
 
   ;; THIS NEEDS FIXING to use h/intern-all as well as module/include
   (find-import-namespaces (project/file-lookup (project/project))
                           'code.test)
   => () #_'(code.test.checker.common
             code.test.checker.collection
             code.test.checker.logic
             code.test.compile)"
  {:added "3.0"}
  ([lookup ns]
   (if-let [path (lookup ns)]
     (->> (fs/read-code path)
          (filter #(-> % first (= 'module/include)))
          (mapcat #(->> % rest (map first)))))))

(defn reference-namespaces
  "finds the referenced vars in the namespace
 
   (-> (reference-namespaces {}
                             (project/file-lookup (project/project))
                             '[jvm.artifact.common])
       (get 'jvm.artifact.common)
       keys
       sort)
   => '(resource-entry resource-entry-symbol)"
  {:added "3.0"}
  ([references lookup namespaces]
   (let [missing   (remove references namespaces)
         imported  (->> missing
                        (mapcat #(find-import-namespaces lookup %))
                        (remove references))
         sources   (concat missing imported)
         tests     (map #(symbol (str % "-test")) sources)]
     (reduce (fn [references [tag ns]]
               (binding [common/*test-full* true]
                 (if-let [file (lookup ns)]
                   (->> (code.framework/analyse-file [tag file])
                        (std.lib.collection/merge-nested references))
                   references)))
             references
             (concat  (map vector (repeat :source) sources)
                      (map vector (repeat :test)   tests))))))

(defn collect-references
  "collects all `:reference` tags of within an article
 
   (let [project (project/project)
         project (assoc project :lookup (project/file-lookup project))
         elems   (parse/parse-file \"src-doc/documentation/code_doc.clj\" project)
         bundle  {:articles {\"code.doc\" {:elements elems}}
                  :references {}
                  :project project}]
     (-> (collect-references bundle \"code.doc\")
         :references
        keys))
   => '(code.doc.parse)"
  {:added "3.0"}
  ([{:keys [articles project] :as interim} name]
   (let [all    (->> (get-in articles [name :elements])
                     (filter #(-> % :type (= :reference))))
         namespaces (-> (map (comp symbol namespace symbol :refer) all))]
     (-> interim
         (update-in [:references]
                    (fnil (fn [references]
                            (reference-namespaces references
                                                  (:lookup project)
                                                  namespaces))
                          {}))))))
