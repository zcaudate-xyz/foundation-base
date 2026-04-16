(ns std.lang.manage
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
            [std.lang.manage.xtalk-audit :as audit]
            [std.lang.manage.xtalk-ops :as ops]
            [std.lang.manage.xtalk-scaffold :as scaffold]
            [code.project :as project]
            [std.fs :as fs]
            [std.lib.collection :as collection]
            [std.lib.invoke :as invoke]
            [std.task :as task]))

(def ^:dynamic *xtalk-model-roots*
  ["src/std/lang/model/spec_xtalk"
   "src/std/lang/model_annex/spec_xtalk"])

(def ^:dynamic *xtalk-test-roots*
  ["test/std/lang/model/spec_xtalk"
   "test/std/lang/model_annex/spec_xtalk"])

(def ^:dynamic *xtalk-runtime-langs*
  (vec (sort (keys scaffold/+runtime-lang-config+))))

(defn- safe-installed-languages
  []
  (try
    (set (audit/installed-languages))
    (catch Throwable _
      #{})))

(defn- safe-support-matrix
  ([langs]
   (safe-support-matrix langs nil))
  ([langs features]
   (try
     (audit/support-matrix langs features)
     (catch Throwable _
       {:languages []
        :features (or features [])
        :status {}
        :summary {}}))))

(defn- list-xtalk-files
  [root]
  (if (fs/exists? root)
    (->> (keys (fs/list root {:recursive true
                              :include [".clj$"]}))
         (map str)
         sort
         vec)
    []))

(defn- parse-lang-file
  [path]
  (let [name (str (fs/file-name path))]
    (when-let [[_ kind lang] (re-find #"(?i)^(fn|com)_([a-z0-9]+)(?:_test)?\\.clj$" name)]
      {:kind (keyword (str/lower-case kind))
       :lang (keyword (str/lower-case lang))})))

(defn- collect-lang-files
  [root roots files-key forms-key]
  (reduce
   (fn [out rel-root]
     (let [scan-root (str (fs/path root rel-root))]
       (reduce
        (fn [m path]
          (if-let [{:keys [kind lang]} (parse-lang-file path)]
            (let [entry (get m lang {files-key []
                                     forms-key #{}})
                  files (conj (get entry files-key) (str (fs/relativize (fs/path root)
                                                                        (fs/path path))))
                  forms (conj (get entry forms-key) kind)]
              (assoc m lang (assoc entry
                                   files-key (vec (sort files))
                                   forms-key forms)))
            m))
        out
        (list-xtalk-files scan-root))))
   (sorted-map)
   roots))

(defn xtalk-model-inventory
  "returns model specification coverage grouped by xtalk language"
  {:added "4.1"}
  ([]
   (xtalk-model-inventory {}))
  ([{:keys [roots]
     :or {roots *xtalk-model-roots*}}]
   (let [proj (project/project)
         by-lang (collect-lang-files (:root proj) roots :model-files :model-forms)]
     (into (sorted-map)
           (for [[lang entry] by-lang]
             [lang (assoc entry
                          :lang lang
                          :model-count (count (:model-files entry))
                          :model-forms (vec (sort (:model-forms entry))))])))))

(defn xtalk-test-inventory
  "returns xtalk test coverage grouped by language"
  {:added "4.1"}
  ([]
   (xtalk-test-inventory {}))
  ([{:keys [roots]
     :or {roots *xtalk-test-roots*}}]
   (let [proj (project/project)
         by-lang (collect-lang-files (:root proj) roots :test-files :test-forms)]
     (into (sorted-map)
           (for [[lang entry] by-lang]
             [lang (assoc entry
                          :lang lang
                          :test-count (count (:test-files entry))
                          :test-forms (vec (sort (:test-forms entry))))])))))

(defn xtalk-runtime-inventory
  "returns runtime inventory with installation and spec support status"
  {:added "4.1"}
  ([]
   (xtalk-runtime-inventory {}))
  ([{:keys [langs]
     :or {langs *xtalk-runtime-langs*}}]
   (let [langs (->> langs (map scaffold/normalize-runtime-lang) distinct sort vec)
         matrix (safe-support-matrix langs)
         installed (safe-installed-languages)]
     (into (sorted-map)
           (for [lang langs
                 :let [{:keys [script dispatch suffix]} (scaffold/runtime-lang-config lang)
                       summary (get-in matrix [:summary lang] {})]]
             [lang {:lang lang
                    :script script
                    :dispatch dispatch
                    :suffix suffix
                    :runtime? true
                    :runtime-type (scaffold/runtime-type lang)
                    :runtime-check-mode (scaffold/runtime-check-mode lang)
                    :runtime-installed? (contains? installed lang)
                    :runtime-executable? (contains? scaffold/+runtime-executable-langs+ lang)
                    :spec-implemented (or (:implemented summary) 0)
                    :spec-abstract (or (:abstract summary) 0)
                    :spec-missing (or (:missing summary) 0)}])))))

(defn xtalk-spec-inventory
  "returns xtalk support matrix summary grouped by language"
  {:added "4.1"}
  ([]
   (xtalk-spec-inventory {}))
  ([{:keys [langs features]}]
   (let [matrix (safe-support-matrix langs features)
         tracked (set (:languages matrix))
         langs (->> (or langs (:languages matrix)) (map keyword) distinct sort vec)
         feature-count (count (:features matrix))]
     (into (sorted-map)
           (for [lang langs
                 :let [summary (get-in matrix [:summary lang] {})]]
             [lang {:lang lang
                    :spec-tracked? (contains? tracked lang)
                    :spec-feature-count feature-count
                    :spec-implemented (or (:implemented summary) 0)
                    :spec-abstract (or (:abstract summary) 0)
                    :spec-missing (or (:missing summary) 0)}])))))

(defn xtalk-language-status
  "returns unified model/runtime/spec/test status by language"
  {:added "4.1"}
  ([]
   (xtalk-language-status {}))
  ([{:keys [langs features]
     :as opts}]
   (let [model (xtalk-model-inventory opts)
         tests (xtalk-test-inventory opts)
         runtime (xtalk-runtime-inventory {:langs langs})
         spec (xtalk-spec-inventory {:langs langs :features features})
         all-langs (or langs
                       (->> (concat (keys model)
                                    (keys tests)
                                    (keys runtime)
                                    (keys spec))
                            distinct
                            sort
                            vec))]
     (into (sorted-map)
           (for [lang all-langs
                 :let [entry (merge {:lang lang
                                     :runtime-installed? false
                                     :runtime-executable? false
                                     :model-count 0
                                     :test-count 0
                                     :spec-implemented 0
                                     :spec-abstract 0
                                     :spec-missing 0}
                                    (get model lang)
                                    (get tests lang)
                                    (get runtime lang)
                                    (get spec lang))
                       model-count (:model-count entry)
                       test-count (:test-count entry)
                       coverage (if (pos? model-count)
                                  (double (/ test-count model-count))
                                  0.0)
                       ready? (and (:runtime-installed? entry)
                                   (zero? (:spec-missing entry))
                                   (pos? test-count))]]
             [lang (assoc entry
                          :coverage coverage
                          :ready? ready?)])))))

(defn xtalk-coverage-summary
  "summarises language readiness and test/spec coverage"
  {:added "4.1"}
  ([]
   (xtalk-coverage-summary {}))
  ([opts]
   (let [status (vals (xtalk-language-status opts))
         total (count status)]
     {:languages total
      :runtime-installed (count (filter :runtime-installed? status))
      :runtime-executable (count (filter :runtime-executable? status))
      :ready (count (filter :ready? status))
      :spec-missing-total (reduce + (map :spec-missing status))
      :models-total (reduce + (map :model-count status))
      :tests-total (reduce + (map :test-count status))})))

(def manage-template
  {:params {:print {:function false
                    :item false
                    :result true
                    :summary true}}
   :warning {:output :data}
   :error {:output :data}
   :item {:pre identity
          :post identity
          :output identity
          :display identity}
   :result {:keys {:count (constantly 1)}
            :ignore nil
            :output identity
            :columns [{:key :key
                       :align :left}
                      {:key :data
                       :align :left
                       :length 100}]}
   :summary {:aggregate {:total [:count + 0]}}})

(defmethod task/task-defaults :lang.manage
  ([_]
   (collection/merge-nested
    manage-template
    {:construct {:env (fn [_]
                        {:status (xtalk-language-status {})
                         :summary (xtalk-coverage-summary {})})
                 :input (constantly :all)
                 :lookup (fn [_ {:keys [status]}] status)}
     :item {:list (fn [lookup _]
                    (sort (keys lookup)))}
     :main {:argcount 4}})))

(defmethod task/task-defaults :lang.manage.action
  ([_]
   (collection/merge-nested
    manage-template
    {:construct {:env (constantly {})
                 :input (constantly nil)
                 :lookup (constantly {})}
     :params {:print {:item false
                      :result false
                      :summary false}}
     :main {:argcount 4}})))

(invoke/definvoke ^{:arglists '([] [lang] [lang params])}
  xtalk-status
  "returns unified xtalk status for each language"
  {:added "4.1"}
  [:task {:template :lang.manage
          :params {:title "XTALK LANGUAGE STATUS"}
          :main {:fn (fn [lang _ lookup _]
                       (get lookup lang))}}])

(invoke/definvoke ^{:arglists '([] [lang] [lang params])}
  xtalk-model-status
  "returns xtalk model coverage by language"
  {:added "4.1"}
  [:task {:template :lang.manage
          :params {:title "XTALK MODEL STATUS"}
          :main {:fn (fn [lang _ lookup _]
                       (select-keys (get lookup lang)
                                    [:lang
                                     :model-count
                                     :model-forms
                                     :model-files]))}}])

(invoke/definvoke ^{:arglists '([] [lang] [lang params])}
  xtalk-runtime-status
  "returns xtalk runtime availability by language"
  {:added "4.1"}
  [:task {:template :lang.manage
          :params {:title "XTALK RUNTIME STATUS"}
          :main {:fn (fn [lang _ lookup _]
                       (select-keys (get lookup lang)
                                    [:lang
                                     :script
                                     :dispatch
                                     :suffix
                                     :runtime-installed?
                                     :runtime-executable?]))}}])

(invoke/definvoke ^{:arglists '([] [lang] [lang params])}
  xtalk-spec-status
  "returns xtalk spec fulfillment by language"
  {:added "4.1"}
  [:task {:template :lang.manage
          :params {:title "XTALK SPEC STATUS"}
          :main {:fn (fn [lang _ lookup _]
                       (select-keys (get lookup lang)
                                    [:lang
                                     :spec-feature-count
                                     :spec-implemented
                                     :spec-abstract
                                     :spec-missing]))}}])

(invoke/definvoke ^{:arglists '([] [lang] [lang params])}
  xtalk-test-status
  "returns xtalk test coverage by language"
  {:added "4.1"}
  [:task {:template :lang.manage
          :params {:title "XTALK TEST STATUS"}
          :main {:fn (fn [lang _ lookup _]
                       (select-keys (get lookup lang)
                                    [:lang
                                     :test-count
                                     :test-forms
                                     :test-files
                                     :coverage
                                     :ready?]))}}])

(invoke/definvoke ^{:arglists '([] [params])}
  xtalk-categories
  "returns all xtalk categories in declaration order"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK CATEGORIES"}
          :main {:fn #'audit/xtalk-categories}}])

(invoke/definvoke ^{:arglists '([] [params])}
  xtalk-op-map
  "returns xtalk op definitions keyed by op"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK OP MAP"}
          :main {:fn #'audit/xtalk-op-map}}])

(invoke/definvoke ^{:arglists '([] [params])}
  xtalk-symbols
  "returns all x:* symbols in xtalk grammar"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK SYMBOLS"}
          :main {:fn #'audit/xtalk-symbols}}])

(invoke/definvoke ^{:arglists '([] [params])}
  installed-languages
  "loads all default book namespaces and returns installed languages"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK INSTALLED LANGUAGES"}
          :main {:fn #'audit/installed-languages}}])

(invoke/definvoke ^{:arglists '([] [params])}
  audit-languages
  "returns languages to include in xtalk support audits"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK AUDIT LANGUAGES"}
          :main {:fn #'audit/audit-languages}}])

(invoke/definvoke ^{:arglists '([] [params])}
  support-matrix
  "returns xtalk support data per language and feature"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK SUPPORT MATRIX"}
          :main {:fn #'audit/support-matrix}}])

(invoke/definvoke ^{:arglists '([] [params])}
  missing-by-language
  "returns non-implemented xtalk features grouped by language"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK MISSING BY LANGUAGE"}
          :main {:fn #'audit/missing-by-language}}])

(invoke/definvoke ^{:arglists '([] [params])}
  missing-by-feature
  "returns languages missing or leaving features abstract"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK MISSING BY FEATURE"}
          :main {:fn #'audit/missing-by-feature}}])

(invoke/definvoke ^{:arglists '([] [params])}
  visualize-support
  "renders a summary or matrix view of xtalk support"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XTALK VISUALIZE SUPPORT"}
          :main {:fn #'audit/visualize-support}}])

(invoke/definvoke ^{:arglists '([] [params])}
  generate-xtalk-ops
  "generates an xtalk operator inventory EDN from grammar tables"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "GENERATE XTALK OPS"}
          :main {:fn #'ops/generate-xtalk-ops}}])

(invoke/definvoke ^{:arglists '([] [params])}
  scaffold-xtalk-grammar-tests
  "renders grammar xtalk tests from xtalk_ops.edn"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "SCAFFOLD XTALK GRAMMAR TESTS"}
          :main {:fn #'scaffold/scaffold-xtalk-grammar-tests}}])

(invoke/definvoke ^{:arglists '([] [params])}
  separate-runtime-tests
  "splits a multi-runtime test namespace into per-language test files"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "SEPARATE RUNTIME TESTS"}
          :main {:fn #'scaffold/separate-runtime-tests}}])

(invoke/definvoke ^{:arglists '([] [params])}
  scaffold-runtime-template
  "generates a runtime test file from a single-runtime template"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "SCAFFOLD RUNTIME TEMPLATE"}
          :main {:fn #'scaffold/scaffold-runtime-template}}])

(invoke/definvoke ^{:arglists '([] [params])}
  diagnose-runtime-generation
  "reports whether a runtime seed can be generated directly and why it may fail"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "DIAGNOSE RUNTIME GENERATION"}
          :main {:fn #'scaffold/diagnose-runtime-generation}}])

(invoke/definvoke ^{:arglists '([] [params])}
  export-runtime-suite
  "exports a canonical runtime test namespace to EDN cases"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "EXPORT RUNTIME SUITE"}
          :main {:fn #'scaffold/export-runtime-suite}}])

(invoke/definvoke ^{:arglists '([] [params])}
  compile-runtime-bulk
  "compiles a canonical runtime EDN suite into a batched verification payload"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "COMPILE RUNTIME BULK"}
          :main {:fn #'scaffold/compile-runtime-bulk}}])

(invoke/definvoke ^{:arglists '([] [params])}
  xtlang-runtime-suite-sources
  "lists xt.lang runtime test templates eligible for a target runtime bulk suite"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "XT.LANG RUNTIME SUITE SOURCES"}
          :main {:fn #'scaffold/xtlang-runtime-suite-sources}}])

(invoke/definvoke ^{:arglists '([] [params])}
  compile-xtlang-runtime-bulk-suites
  "exports and compiles xt.lang runtime suites into batched payloads for a target runtime"
  {:added "4.1"}
  [:task {:template :lang.manage.action
          :params {:title "COMPILE XT.LANG RUNTIME BULKS"}
          :main {:fn #'scaffold/compile-xtlang-runtime-bulk-suites}}])

;;
;; Task Registry and Main Entry Point
;;

(def +tasks+
  "all available tasks for the std.lang.manage interface"
  {:inventory              xtalk-model-inventory
   :test-inventory         xtalk-test-inventory
   :runtime-inventory      xtalk-runtime-inventory
   :spec-inventory         xtalk-spec-inventory
   :language-status        xtalk-language-status
   :coverage-summary       xtalk-coverage-summary
   :status                 xtalk-status
   :model-status           xtalk-model-status
   :runtime-status         xtalk-runtime-status
   :spec-status            xtalk-spec-status
   :test-status            xtalk-test-status
   :categories             xtalk-categories
   :op-map                 xtalk-op-map
   :symbols                xtalk-symbols
   :installed-languages    installed-languages
   :audit-languages        audit-languages
   :support-matrix         support-matrix
   :missing-by-language    missing-by-language
   :missing-by-feature     missing-by-feature
   :visualize-support      visualize-support
   :generate-xtalk-ops     generate-xtalk-ops
   :scaffold-xtalk-grammar-tests  scaffold-xtalk-grammar-tests
   :separate-runtime-tests separate-runtime-tests
   :scaffold-runtime-template scaffold-runtime-template
   :diagnose-runtime-generation diagnose-runtime-generation
   :export-runtime-suite    export-runtime-suite
   :compile-runtime-bulk    compile-runtime-bulk
   :xtlang-runtime-suite-sources xtlang-runtime-suite-sources
   :compile-xtlang-runtime-bulk-suites compile-xtlang-runtime-bulk-suites})

(def +direct-tasks+
  #{:inventory
    :test-inventory
    :runtime-inventory
    :spec-inventory
    :language-status
    :coverage-summary
    :categories
    :op-map
    :symbols
    :installed-languages
    :audit-languages
    :support-matrix
    :missing-by-language
    :missing-by-feature
    :visualize-support
    :generate-xtalk-ops
    :scaffold-xtalk-grammar-tests
    :separate-runtime-tests
    :scaffold-runtime-template
    :diagnose-runtime-generation
    :export-runtime-suite
    :compile-runtime-bulk
    :xtlang-runtime-suite-sources
    :compile-xtlang-runtime-bulk-suites})

(defn- parse-main-arg
  [x]
  (try (read-string x)
       (catch Throwable _ x)))

(defn- supports-arity?
  [f n]
  (boolean
   (some (fn [arglist]
           (or (= n (count arglist))
               (and (seq arglist)
                    (= '& (nth arglist (dec (count arglist)) nil))
                    (<= (dec (count arglist)) n))))
         (:arglists (meta f)))))

(defn -main
  "main entry point for std.lang.manage

   (std.lang.manage/-main \"inventory\")
   (std.lang.manage/-main \"support-matrix\" \"{:langs [:python :go]}\")"
  {:added "4.1"}
  [& [cmd & args]]
  (let [print-fn (fn []
                   (do (println "Available Tasks:")
                       (doseq [task-name (map name (sort (keys +tasks+)))]
                         (println (str "  - " task-name)))))
        print-params {:print {:function false
                              :item false
                              :result true
                              :summary true}}]
    (if (not cmd)
      (print-fn)

      (let [task-key (keyword cmd)
            opts (task/process-ns-args args)
            params (merge print-params (dissoc opts :ns))
            func (or (get +tasks+ task-key)
                     (ns-resolve (find-ns 'std.lang.manage) (symbol cmd)))
            parsed-args (mapv parse-main-arg args)]
        (if func
          (let [result (cond
                         (seq parsed-args)
                         (apply func parsed-args)

                         (supports-arity? func 1)
                         (func params)

                         (supports-arity? func 2)
                         (func (or (:ns opts) :all) params)

                         :else
                         (func))]
            (when (and (contains? +direct-tasks+ task-key)
                       (some? result))
              (pprint/pprint result)))
          (print-fn))))))
