(ns code.doc.link.api
  (:require [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [std.fs :as fs]
            [std.lib.collection]))

(defn external-vars
  "grabs external vars from the `module/include` form
 
   ;; THIS NEEDS FIXING to use h/intern-all as well as module/include
   (external-vars (project/file-lookup (project/project))
                  'code.test)
   => {}
   #_'{code.test.checker.common [throws exactly approx satisfies stores anything]
       code.test.checker.collection [contains just contains-in just-in throws-info]
       code.test.checker.logic [any all is-not]
       code.test.compile [fact facts =>]}"
  {:added "3.0"}
  ([lookup ns]
   (if-let [path (lookup ns)]
     (->> (fs/read-code path)
          (filter #(-> % first (= 'module/include)))
          (mapcat #(->> % rest (remove map?)))
          (map (juxt first rest))
          (group-by first)
          (std.lib.collection/map-vals (comp flatten #(map rest %)))))))

(defn create-api-table
  "creates a api table for publishing"
  {:added "3.0"}
  ([references project namespace]
   (let [lookup  (:lookup project)
         all-vars (-> (external-vars lookup namespace)
                      (assoc namespace :all))
         live-vars (do (require namespace)
                       (ns-interns namespace))]
     (reduce-kv (fn [table ns vals]
                  (let [relative-to-root #(if % (->> % (fs/relativize (:root project)) str))
                        vals (if (= :all vals)
                               (-> ns references keys)
                               vals)]
                    (reduce (fn [out v]
                              (binding [common/*test-full* true]
                                (let [[src dst] (if (symbol? v)
                                                  [v v]
                                                  [(last v) (first v)])
                                      entry (-> (get-in references [ns src])
                                                (update-in [:test :code] docstring/->refstring)
                                                (update-in [:test :path] relative-to-root)
                                                (update-in [:source :path] relative-to-root)
                                                (assoc :origin (symbol (str ns "/" src))
                                                       :arglists (-> (get live-vars dst)
                                                                     meta
                                                                     :arglists)))]
                                  (assoc out dst entry))))
                            table
                            vals)))
                {}
                all-vars))))

(defn link-apis
  "links all the api source and test files to the elements"
  {:added "3.0"}
  ([{:keys [references project] :as interim} name]
   (update-in interim [:articles name :elements]
              (fn [elements]
                (mapv (fn [{:keys [type namespace] :as element}]
                        (if (= type :api)
                          (-> element
                              (assoc :project project)
                              (assoc :table
                                     (create-api-table references
                                                       project
                                                       (symbol namespace))))
                          element))
                      elements)))))
