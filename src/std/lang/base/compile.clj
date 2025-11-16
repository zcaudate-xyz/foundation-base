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
            [std.lang.base.compile-links :as links]
            [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs]))


;;
;; SCRIPT
;;

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


;;
;; SINGLE
;;

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

;;
;; DIRECTORY
;;

(defn compile-module-directory-selected
  [type ns-all {:keys [lang main emit root target] :as opts}]
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
        links       (->> (concat ns-selected
                                 ns-extras)
                         (map (fn [ns]
                                [ns (links/link-attributes
                                     main
                                     ns
                                     (:link (:code emit)))]))
                         (into {}))
        
        root-path  (if (empty? target)
                     root
                     (str root "/" target))
        
        compile-fn (fn [ns]
                     (let [emit-opts
                           {:static  (:static (book/get-module book ns))
                            :compile {:type type
                                      :links links}}]
                       (compile-module-single
                        (-> opts
                            (assoc :layout :module
                                   :main ns
                                   :snapshot snapshot
                                   :output (str (fs/path (str root-path "/"(get-in links [ns :path])))))
                            (update :emit merge emit-opts)))))
        ;; generate for all namespaces
        files  (mapv compile-fn (concat ns-selected ns-extras))]
    (compile/compile-summarise files)))

(defn compile-module-directory
  "TODO"
  {:added "4.0"}
  ([{:keys [header footer lang main search root target emit] :as opts}]
   (let [all-paths   (->> (or search ["src"])
                          (map #(fs/path %))
                          (mapcat #(fs/select % {:include [".clj$"]})))
         ns-all      (pmap fs/file-namespace all-paths)]
     (compile-module-directory-selected :directory ns-all opts))))

(def +install-module-directory-fn+
  (compile/types-add :module.directory #'compile-module-directory))


;;
;; ROOT
;;

(defn compile-module-prep
  [{:keys [lang main] :as opts}]
  (let [lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
        book        (snap/get-book snapshot lang)
        parent (->> (str/split (str main) #"\.")
                    (butlast)
                    (str/join ".")
                    (symbol))
        selected  (->> (:all (deps-imports/module-code-deps book [main]))
                       (filter (fn [ns]
                                 (.startsWith (str ns)
                                              (str parent)))))]
    [selected (assoc opts :main parent)]))

(defn compile-module-root
  "compiles a module root"
  {:added "4.0"}
  ([{:keys [lang main] :as opts}]
   (require main)
   (let [[selected opts] (compile-module-prep opts)]
     (compile-module-directory-selected :directory selected opts))))

(def +install-module-root-fn+
  (compile/types-add :module.root #'compile-module-root)) 

;;
;; GRAPH
;;

(defn compile-module-graph
  "compiles a module root"
  {:added "4.0"}
  ([{:keys [lang main] :as opts}]
   (require main)
   (let [[selected opts] (compile-module-prep opts)]
     (compile-module-directory-selected :graph selected opts))))

(def +install-module-graph-fn+
  (compile/types-add :module.graph #'compile-module-graph)) ;



;;
;; SCHEMA
;;

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
