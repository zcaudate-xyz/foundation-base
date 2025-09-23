(ns std.lang.base.compile
  (:require [std.make.compile :as compile]
            [std.lang.base.emit :as emit]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.impl :as impl]
            [std.lang.base.impl-deps :as deps]
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
   
   #_(h/pp (:emit opts))
   
   (let [mopts   (last (impl/emit-options opts))
         {:keys [emit]} mopts
         body    (lifecycle/emit-module-setup main
                                              mopts)
         full    (compile/compile-fullbody body opts)

         ;; output transformations
         full    (reduce (fn [full transform]
                           (transform full (-> emit :static)))
                         full
                         (-> emit :code :transforms))
         output  (compile/compile-out-path opts)]
     (compile/compile-write output full))))

(def +install-module-single-fn+
  (compile/types-add :module.single #'compile-module-single))

(defn compile-module-graph-rel
  "compiles a module graph"
  {:added "4.0"}
  [path]
  (subs (str path)
        0
        (.lastIndexOf (str path) ".")))

(defn compile-module-graph-single
  "compiles a single module file"
  {:added "4.0"}
  [ns
   {:keys [root-path
           root-output
           snapshot
           book]
    :as interim}
   {:keys [header footer lang
           main root target emit]
    :as opts}]
  (let [module-id   main
        is-ext      (str/starts-with? (name ns) (compile-module-graph-rel (str module-id)))
        ns-path     (if-not is-ext
                      (str/replace (compile-module-graph-rel (name ns)) #"\." "/")
                      (->> (str/replace (name ns) #"\." "/")
                           (fs/parent)
                           (fs/relativize root-path)))
        ns-path     (if (= "" (str ns-path))
                      nil
                      ns-path)
        module      (book/get-module book ns)
        ns-file     (or (:file module)
                        (book/module-create-filename
                         book
                         (or (:id module)
                             (h/error "MODULE NOT FOUND" {:ns ns}))))
        output-path (str/join "/" (filter identity [root-output ns-path ns-file]))]
    (compile-module-single
     {:lang  lang
      :layout :module
      :header header
      :footer footer 
      :output output-path
      :main ns 
      :emit (assoc emit
                   :compile {:type :graph
                             :base ns :root-ns module-id}
                   :static  (:static module))
      :snapshot snapshot})))

(defn compile-module-graph
  "compiles a module graph"
  {:added "4.0"}
  ([{:keys [header footer lang main root target emit] :as opts}]
   (let [module-id   main
         lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
         book        (snap/get-book snapshot lang)
         deps        (-> (h/deps:resolve book [module-id])
                         :all)
         root-path   (-> (str/replace (name module-id) #"\." "/")
                         (fs/parent))
         root-output (if (empty? target)
                       root
                       (str root "/" target))
         #_#__           (h/prn {:module-id module-id
                                 :deps deps
                                 :root-path root-path
                                 :root-output root-output})
         files       (mapv (fn [ns]
                             (compile-module-graph-single
                              ns
                              {:book book
                               :snapshot snapshot
                               :root-path   root-path
                               :root-output root-output}
                              opts))
                           deps)]
     (compile/compile-summarise files))))

(def +install-module-graph-fn+
  (compile/types-add :module.graph #'compile-module-graph)) ;

(defn compile-module-directory-single
  "TODO"
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

(defn compile-module-directory
  "TODO"
  {:added "4.0"}
  ([{:keys [header footer lang main search root target emit] :as opts}]
   (let [lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
         book        (snap/get-book snapshot lang)
         all-paths   (->> (or search ["src"])
                          (map #(fs/path %))
                          (mapcat #(fs/select % {:include [".clj$"]})))
         
         ;; get paths of all the clojure files with the root namespace
         ns-all      (pmap fs/file-namespace all-paths)
         ns-has?     (fn [ns]
                       (.startsWith (str ns)
                                    (str main)))
         ns-selected (filter ns-has? ns-all)

         ;; output the libs
         _  (h/prn search ns-selected)
         
         ;; require all the paths of the file.
         _  (doseq [ns ns-selected]
              (require ns))
         
         ;; for each file in the directory, find 'extra' deps 
         ns-extras   (if (-> emit :code :output-extra false?)
                       []
                       (->> (mapcat (fn [ns]
                                      (-> (h/deps:resolve book [ns])
                                          :all))
                                    ns-selected)
                            (set)
                            (filter (comp not ns-has?))))
         
         ;; output the libs
         #_#__  (h/prn opts)
         #_#__  (h/prn ns-extras)
         
         ;; generate for all namespaces
         files  (mapv (fn [ns]
                        (compile-module-directory-single
                         ns
                         {:book book
                          :snapshot snapshot}
                         opts))
                      (concat ns-selected
                              ns-extras))]
     (compile/compile-summarise files))))

(def +install-module-directory-fn+
  (compile/types-add :module.directory #'compile-module-directory))

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
