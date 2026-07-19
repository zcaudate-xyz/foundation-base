(ns code.doc.collect.reference
  (:require [code.framework :as code.framework]
            [code.framework.common :as common]
            [std.fs :as fs]
            [std.lib.collection :as collection]))

(def ^:dynamic *doc-toplevel-forms*
  "toplevel forms indexed when analysing sources for documentation"
  (conj code.framework/*toplevel-forms*
        'def 'defonce 'defn- 'definvoke 'impl/defimpl))

(defn- require-spec-aliases
  "returns alias -> namespace pairs for a single `:require` spec"
  [prefix spec]
  (cond (symbol? spec)
        []

        (and (vector? spec) (symbol? (first spec)))
        (let [lib  (first spec)
              full (if prefix (symbol (str prefix "." lib)) lib)]
          (if (some keyword? (rest spec))
            (let [as (->> (rest spec)
                          (drop-while #(not= :as %))
                          second)]
              (if (symbol? as)
                [[as full]]
                []))
            (mapcat #(require-spec-aliases full %) (rest spec))))

        :else []))

(defn ns-form-aliases
  "returns the alias -> namespace map for a namespace form

   (ns-form-aliases '(ns example (:require [std.block.base :as base]
                                      [std.string [case :as c]])))
   => '{base std.block.base, c std.string.case}"
  {:added "4.1"}
  [ns-form]
  (->> (filter #(and (sequential? %) (= :require (first %))) ns-form)
       (mapcat rest)
       (mapcat #(require-spec-aliases nil %))
       (into {})))

(defn- intern-arg-entry
  "parses a single `intern-in` argument into [source-ns var-entry]"
  [resolve-ns arg]
  (cond (and (symbol? arg) (namespace arg))
        [(resolve-ns (symbol (namespace arg)))
         (symbol (name arg))]

        (and (vector? arg)
             (symbol? (first arg))
             (symbol? (second arg))
             (namespace (second arg)))
        [(resolve-ns (symbol (namespace (second arg))))
         [(first arg) (symbol (name (second arg)))]]))

(defn aggregate-imports
  "finds vars imported into an aggregate namespace via `module/include`,
   `intern-in` and `intern-all` forms, returning `{:namespaces #{...} :vars {...}}`

   (-> (aggregate-imports (project/file-lookup (project/project)) 'std.block)
       :namespaces
       (contains? 'std.block.base))
   => true"
  {:added "4.1"}
  [lookup ns]
  (if-let [path (lookup ns)]
    (let [forms      (fs/read-code path)
          aliases    (ns-form-aliases (first forms))
          resolve-ns (fn [s] (get aliases s s))]
      (reduce
       (fn [out form]
         (if-not (sequential? form)
           out
           (let [head (first form)]
             (cond (= 'module/include head)
                   (reduce (fn [out spec]
                             (let [spec (remove map? spec)]
                               (if (and (sequential? spec) (symbol? (first spec)))
                                 (let [src-ns (resolve-ns (first spec))]
                                   (-> out
                                       (update :namespaces conj src-ns)
                                       (update-in [:vars src-ns]
                                                  (fn [existing]
                                                    (vec (concat (or existing [])
                                                                 (filter symbol? (flatten (rest spec)))))))))
                                 out)))
                           out
                           (rest form))

                   (and (symbol? head) (= "intern-in" (name head)))
                   (reduce (fn [out arg]
                             (if-let [[src-ns entry] (intern-arg-entry resolve-ns arg)]
                               (-> out
                                   (update :namespaces conj src-ns)
                                   (update-in [:vars src-ns]
                                              (fnil conj []) entry))
                               out))
                           out
                           (rest form))

                   (and (symbol? head) (= "intern-all" (name head)))
                   (reduce (fn [out arg]
                             (if (symbol? arg)
                               (let [src-ns (resolve-ns arg)]
                                 (-> out
                                     (update :namespaces conj src-ns)
                                     (assoc-in [:vars src-ns] :all)))
                               out))
                           out
                           (rest form))

                   :else out))))
       {:namespaces #{} :vars {}}
       (rest forms)))
    {:namespaces #{} :vars {}}))

(defn find-import-namespaces
  "finds namespaces imported via `module/include`, `intern-in` and `intern-all`

   (find-import-namespaces (project/file-lookup (project/project))
                           'code.test)
   => '(code.test.base.context
        code.test.checker.collection
        code.test.checker.common
        code.test.checker.logic
        code.test.compile
        code.test.manage
        code.test.task)"
  {:added "3.0"}
  ([lookup ns]
   (seq (sort (:namespaces (aggregate-imports lookup ns))))))

(defn imported-namespaces
  "returns all namespaces transitively imported by the given namespaces"
  {:added "4.1"}
  [lookup namespaces]
  (loop [seen  #{}
         queue (seq namespaces)
         out   []]
    (if (empty? queue)
      out
      (let [n (first queue)]
        (if (contains? seen n)
          (recur seen (next queue) out)
          (let [imports (find-import-namespaces lookup n)]
            (recur (conj seen n)
                   (concat (next queue) imports)
                   (into out imports))))))))

(defn reference-namespaces
  "finds the referenced vars in the namespace

   (-> (reference-namespaces {}
                             (project/file-lookup (project/project))
                             '[jvm.artifact.common])
       (get 'jvm.artifact.common)
       keys
       sort)
   => '(*java-class-path* *java-home* *java-runtime-jar* *local-repo* *sep*
        resource-entry resource-entry-symbol)"
  {:added "3.0"}
  ([references lookup namespaces]
   (let [missing   (remove references namespaces)
         imported  (->> (imported-namespaces lookup missing)
                        (remove references)
                        distinct)
         sources   (concat missing imported)
         tests     (map #(symbol (str % "-test")) sources)]
     (binding [code.framework/*toplevel-forms* *doc-toplevel-forms*]
       (reduce (fn [references [tag ns]]
                 (binding [common/*test-full* true]
                   (if-let [file (lookup ns)]
                     (->> (code.framework/analyse-file [tag file])
                          (collection/merge-nested references))
                     references)))
               references
               (concat  (map vector (repeat :source) sources)
                        (map vector (repeat :test)   tests)))))))

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
   => '(code.doc code.doc.manage))"
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
