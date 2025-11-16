(ns std.lang.base.compile
  (:require [std.make.compile :as compile]
            [std.lang.base.emit :as emit]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-deps-imports :as deps-imports]
            [std.lang.base.impl-lifecycle :as lifecycle]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs]))

(defn compile-script
  "compiles a script"
  {:added "4.0"}
  [{:keys [header footer main root target name file] :as opts}]
  (let [opts  (merge {:layout :flat
                      :entry {:label true}}
                     opts)
        entry (compile/compile-resolve main)
        _ (if (not (book/book-entry? entry))
            (h/error "Not a library entry" {:main main}))
        meta   (ptr/ptr-invoke-meta entry
                                    (select-keys opts [:layout
                                                       :emit]))
        body   (impl/emit-script (:form entry) meta)
        full   (compile/compile-fullbody body opts)
        output (compile/compile-out-path opts)]
    (compile/compile-write output full)))

(def +install-script-fn+
  (compile/types-add :script #'compile-script))

(defn compile-module-single
  "compiles a single module"
  {:added "4.0"}
  ([{:keys [header lang footer main] :as opts}]
   (let [mopts   (last (impl/emit-options opts))
         {:keys [emit]} mopts
         body    (lifecycle/emit-module-setup main
                                              mopts)
         full    (compile/compile-fullbody body opts)

         ;; output transformations
         full    (reduce (fn [full transform]
                           (transform full (-> emit :static)))
                         full
                         (-> emit :code :transforms :full))
         output  (compile/compile-out-path opts)]
     (compile/compile-write output full))))

(def +install-module-single-fn+
  (compile/types-add :module.single #'compile-module-single))

(defn compile-module-directory-single
  "compiles a single directory file"
  {:added "4.0"}
  [ns
   {:keys [snapshot
           book]
    :as interim}
   {:keys [header footer lang
           main root target emit]
    :as opts}]
  (let [sub-path (deps/collect-module-directory-form
                  nil
                  ns
                  (assoc (get-in emit [:code :link])
                         :root-ns main
                         :root-prefix "."))
        root-output (if (empty? target)
                      root
                      (str root "/" target))
        module      (book/get-module book ns)
        
        output-path (str/join "/" (filter identity [root-output sub-path]))]
    (compile-module-single
     {:lang  lang
      :layout :module
      :header header
      :footer footer 
      :output output-path
      :main ns 
      :emit (assoc emit
                   :compile (merge (get-in emit [:code :link])
                                   {:type :directory
                                    :base ns
                                    :root-ns main})
                   :static  (:static module))
      :snapshot snapshot})))

(defn compile-module-directory-selected
  [ns-all {:keys [lang main emit] :as opts}]
  ;; for each file in the directory, find 'extra' deps
  (let [lib         (impl/runtime-library)
        snapshot    (lib/get-snapshot lib)
        book        (snap/get-book snapshot lang)
        ns-has?     (fn [ns]
                      (.startsWith (str ns)
                                   (str main)))
        ns-selected (filter ns-has? ns-all)
        ns-extras   (if (-> emit :code :extra-namespaces false?)
                      []
                      (->> (mapcat (fn [ns]
                                     (:all (deps-imports/module-code-deps book [ns])))
                                   ns-selected)
                           (set)
                           (filter (comp not ns-has?))))
        
        ;; generate for all namespaces
        files  (mapv (fn [ns]
                       (compile-module-directory-single
                        ns
                        {:book book
                         :snapshot snapshot}
                        opts))
                     (concat ns-selected
                             ns-extras))]
    (compile/compile-summarise files)))

(defn compile-module-directory
  "TODO"
  {:added "4.0"}
  ([{:keys [header footer lang main search root target emit] :as opts}]
   (let [all-paths   (->> (or search ["src"])
                          (map #(fs/path %))
                          (mapcat #(fs/select % {:include [".clj$"]})))
         ns-all      (pmap fs/file-namespace all-paths)]
     (compile-module-directory-selected ns-all opts))))

(def +install-module-directory-fn+
  (compile/types-add :module.directory #'compile-module-directory))

(defn compile-module-graph
  "compiles a module graph"
  {:added "4.0"}
  ([{:keys [lang main] :as opts}]
   (require main)
   (let [lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
         book        (snap/get-book snapshot lang)
         parent (->> (str/split (str main) #"\.")
                     (butlast)
                     (str/join ".")
                     (symbol))
         selected  (->> (h/do:prn (:all (deps-imports/module-code-deps book [main])))
                        (filter (fn [ns]
                                  (.startsWith (str ns)
                                               (str parent)))))
         _ (h/prn selected)]
     (compile-module-directory-selected selected
                                        (assoc opts :main parent)))))

(def +install-module-graph-fn+
  (compile/types-add :module.graph #'compile-module-graph)) 

(defn compile-module-schema
  "compiles all namespaces into a single file (for sql)"
  {:added "4.0"}
  ([{:keys [header footer lang main root target] :as opts}]
   (let [mopts   (last (impl/emit-options opts))
         lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
         book        (snap/get-book snapshot lang)
         deps        (h/deps:ordered book [main])
         full-arr    (map (fn [module-id]
                            (-> (lifecycle/emit-module-setup module-id
                                                             mopts)
                                (compile/compile-fullbody opts)))
                          deps)
         full        (str/join "\n\n" full-arr)
         output (compile/compile-out-path opts)]
     (compile/compile-write output full))))

(def +install-module-schema-fn+
  (compile/types-add :module.schema #'compile-module-schema))
