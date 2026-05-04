(ns hara.lang.base.compile
  (:require [clojure.string]
            [std.fs :as fs]
            [hara.lang.base.book :as book]
            [hara.lang.base.compile-links :as links]
            [hara.common.emit :as emit]
            [hara.lang.base.impl :as impl]
            [hara.lang.base.impl-deps :as deps]
            [hara.lang.base.impl-deps-imports :as deps-imports]
            [hara.lang.base.impl-lifecycle :as lifecycle]
            [hara.lang.base.library :as lib]
            [hara.lang.base.library-snapshot :as snap]
            [hara.lang.base.pointer :as ptr]
            [hara.common.util :as ut]
            [std.lib.deps]
            [std.lib.foundation :as f]
            [std.make.compile :as compile]))


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
             (f/error "Not a library entry" {:main main}))
         meta   (ptr/ptr-invoke-meta entry
                                     (select-keys opts [:layout
                                                        :emit]))
         entry  (if (:form entry)
                  entry
                  (book/get-code-entry-view (:book meta)
                                            (ut/sym-full entry)))
         body   (impl/emit-script (:form entry) meta)
         full   (compile/compile-fullbody body opts)
         output (compile/compile-out-path opts)]
     (compile/compile-write output full)))

(def +install-script-fn+
  (compile/types-add :script #'compile-script))

(defn resolve-artifact-producer
  [producer]
  (cond
    (var? producer) @producer
    (symbol? producer) (some-> producer resolve deref)
    (fn? producer) producer
    :else nil))

(defn artifact-descriptor-seq
  [artifacts]
  (cond
    (nil? artifacts)
    []

    (map? artifacts)
    [artifacts]

    (sequential? artifacts)
    (mapcat artifact-descriptor-seq artifacts)

    :else
    (f/error "Invalid artifact descriptor" {:artifacts artifacts})))

(defn compile-module-artifacts
  [full output {:keys [emit main] :as opts} mopts]
  (let [ctx (merge opts
                   {:emit emit
                    :runtime-body full
                    :runtime-output output
                    :mopts mopts})
        extra (->> (or (:artifacts emit) [])
                   (map resolve-artifact-producer)
                   (remove nil?)
                   (mapcat (fn [producer]
                             (artifact-descriptor-seq (producer ctx)))))]
    (vec (concat [{:output output
                   :body full}]
                 extra))))

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
          output  (compile/compile-out-path opts)
          artifacts (compile-module-artifacts full output opts mopts)
          written   (compile/compile-write-artifacts artifacts)]
      (if (= 1 (count written))
        (first written)
        written))))

(def +install-module-single-fn+
  (compile/types-add :module.single #'compile-module-single))

;;
;; DIRECTORY
;;

(defn compile-module-create-links
  [link-all link-root link-opts]
  (->> link-all
       (map (fn [ns]
              [ns (links/link-attributes
                   link-root
                   ns
                   link-opts)]))
       (into {})))

(defn compile-module-directory-specialization-conflicts
  [book modules]
  (reduce (fn [out module-id]
            (if-let [module (book/get-module book module-id)]
              (reduce-kv (fn [out source {:keys [bindings backend]}]
                           (reduce-kv (fn [out contract target-backend]
                                        (let [target-backend (or target-backend backend)]
                                          (if-let [prev (get out contract)]
                                            (do (when (not= (:backend prev) target-backend)
                                                  (f/error "Conflicting specialization contract backend in module.directory build"
                                                           {:contract contract
                                                            :module module-id
                                                            :source source
                                                            :backend target-backend
                                                            :previous-module (:module prev)
                                                            :previous-source (:source prev)
                                                            :previous-backend (:backend prev)}))
                                                out)
                                            (assoc out contract
                                                   {:backend target-backend
                                                    :module module-id
                                                    :source source}))))
                                      out
                                      (or bindings {})))
                         out
                         (or (:specialize module) {}))
              out))
          {}
          modules))

(defn compile-module-directory-selected
  "compiles the directory based on sorted imports"
  {:added "4.0"}
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
         links      (compile-module-create-links
                     (concat ns-selected
                             ns-extras)
                     main
                    (:link (:code emit)))
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
                                    :output (str (fs/path (str root-path "/" (get-in links [ns :path])))))
                             (update :emit merge emit-opts)))))

         ns-compile (if compile/*compile-filter*
                      (filter compile/*compile-filter* (concat ns-selected ns-extras))
                      (concat ns-selected ns-extras))
         _         (when (= :directory type)
                     (compile-module-directory-specialization-conflicts
                      book
                      ns-compile))

         ;; generate for all namespaces
         files  (mapcat (comp compile/compile-result-seq compile-fn) ns-compile)]
     (compile/compile-summarise files)))

(defn compile-module-directory
  "compiles a directory"
  {:added "4.0"}
  ([{:keys [header footer lang main search root target emit] :as opts}]
   (let [all-paths   (->> (or search ["src"])
                          (map #(fs/path %))
                          (mapcat #(fs/select % {:include [".clj$"]})))
         ns-all      (pmap fs/file-namespace all-paths)]
     (doseq [[path ns] (map vector all-paths ns-all)]
       (if ns
         (try (require ns)
              (catch Throwable t
                (println "WARN: Failed to load" ns "from" path (.getMessage t))))
         (println "WARN: Could not determine namespace for file:" path)))
     (compile-module-directory-selected :directory (filter identity ns-all) opts))))

(def +install-module-directory-fn+
  (compile/types-add :module.directory #'compile-module-directory))

(declare compile-module-root
         compile-module-graph)

(defn specialization-descriptor
  "normalizes a module specialization descriptor"
  {:added "4.1"}
  [{:keys [bindings compile-type descriptor lang runtime source source-module target target-module]
    :as m}]
  (let [m      (merge descriptor m)
        source (or source source-module)
        target (or target target-module)]
    (or source
        (f/error "Specialization source required" {:input m}))
    (or target
        (f/error "Specialization target required" {:input m}))
    (assoc m
           :lang lang
           :runtime runtime
           :source source
           :target target
           :bindings (or bindings {})
           :compile-type (or compile-type :graph))))

(defn compile-module-specialization
  "installs and compiles a specialized module descriptor"
  {:added "4.1"}
  [descriptor]
  (let [{:keys [bindings compile-type lang library source target]
         :as descriptor} (specialization-descriptor descriptor)
        library  (or library (impl/default-library))
        _        (lib/install-module-specialized! library
                                                  lang
                                                  source
                                                  target
                                                  {:bindings bindings})
        compile-fn (case compile-type
                     :single #'compile-module-single
                     :root   #'compile-module-root
                     :directory #'compile-module-directory
                     #'compile-module-graph)]
    (compile-fn (-> descriptor
                    (dissoc :bindings
                            :compile-type
                            :descriptor
                            :library
                            :runtime
                            :source
                            :source-module
                            :target
                            :target-module)
                    (assoc :main target)))))

(defn compile-module-specializations
  "installs and compiles a batch of module specialization descriptors"
  {:added "4.1"}
  ([descriptors]
   (compile-module-specializations descriptors {}))
  ([descriptors opts]
   (mapv (fn [descriptor]
           (compile-module-specialization (merge opts descriptor)))
         descriptors)))


;;
;; ROOT
;;

(defn compile-module-prep
  "precs the single entry point setup"
  {:added "4.0"}
  [{:keys [lang main] :as opts}]
  (let [lib         (impl/runtime-library)
        snapshot    (lib/get-snapshot lib)
        book        (snap/get-book snapshot lang)
        parent (->> (clojure.string/split (str main) #"\.")
                    (butlast)
                    (clojure.string/join ".")
                    (symbol))
        selected  (->> (:all (deps-imports/module-code-deps book [main]))
                       (filter (fn [ns]
                                 (.startsWith (str ns)
                                              (str parent)))))]
    [selected (assoc opts :main parent)]))

(defn compile-module-root
  "compiles module.root"
  {:added "4.0"}
  ([{:keys [lang main] :as opts}]
   (require main)
   (let [[selected opts] (compile-module-prep opts)]
     (compile-module-directory-selected :graph selected opts))))

(def +install-module-root-fn+
  (compile/types-add :module.root #'compile-module-root))

;;
;; GRAPH
;;

(defn compile-module-graph
  "compiles a module graph"
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
   (require main)
   (let [mopts   (last (impl/emit-options opts))
         lib         (impl/runtime-library)
         snapshot    (lib/get-snapshot lib)
         book        (snap/get-book snapshot lang)
         deps        (std.lib.deps/deps-ordered book [main])
         full-arr    (map (fn [module-id]
                            (-> (lifecycle/emit-module-setup module-id
                                                             mopts)
                                (compile/compile-fullbody opts)))
                          deps)
         full        (clojure.string/join "\n\n" full-arr)
         output (compile/compile-out-path opts)]
     (compile/compile-write output full))))

(def +install-module-schema-fn+
  (compile/types-add :module.schema #'compile-module-schema))
