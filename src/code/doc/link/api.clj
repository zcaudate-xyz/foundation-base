(ns code.doc.link.api
  (:require [code.doc.collect.reference :as collect.reference]
            [code.framework.common :as common]
            [code.framework.docstring :as docstring]
            [std.fs :as fs]
            [std.lib.collection :as collection]))

(defn external-vars
  "grabs external vars from aggregate forms (`module/include`, `intern-in`, `intern-all`)

   (-> (external-vars (project/file-lookup (project/project))
                      'code.test)
       (get 'code.test.checker.common))
   => '[throws exactly approx satisfies stores anything capture]"
  {:added "3.0"}
  ([lookup ns]
   (:vars (collect.reference/aggregate-imports lookup ns))))

(defn resolve-import
  "resolves a var through one level of aggregate imports,
   returning `[source-ns source-var]` or nil

   (resolve-import {} '{std.block.heal.core [[heal heal-content]]} 'heal)
   => '[std.block.heal.core heal-content]"
  {:added "4.1"}
  [references imports var]
  (->> imports
       (keep (fn [[src-ns entries]]
               (if (= :all entries)
                 (if (get-in references [src-ns var :source :code])
                   [src-ns var])
                 (some (fn [e]
                         (let [[dst src] (if (symbol? e) [e e] e)]
                           (if (= dst var)
                             [src-ns src])))
                       entries))))
       first))

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
                        nested (delay (external-vars lookup ns))
                        vals (if (= :all vals)
                               (-> ns references keys)
                               vals)]
                    (reduce (fn [out v]
                              (binding [common/*test-full* true]
                                (let [[src dst] (if (symbol? v)
                                                  [v v]
                                                  [(last v) (first v)])
                                      [rns rsrc] (if (get-in references [ns src :source :code])
                                                   [ns src]
                                                   (or (resolve-import references @nested src)
                                                       [ns src]))
                                      resolved (get-in references [rns rsrc])
                                      entry (-> resolved
                                                (assoc :test
                                                       (if (get-in resolved [:test :code])
                                                         (:test resolved)
                                                         (get-in references [ns src :test])))
                                                (update-in [:test :code] docstring/->refstring)
                                                (update-in [:test :path] relative-to-root)
                                                (update-in [:source :path] relative-to-root)
                                                (assoc :origin (symbol (str rns "/" rsrc))
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
