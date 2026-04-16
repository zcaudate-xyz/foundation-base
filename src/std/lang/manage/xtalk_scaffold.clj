(ns std.lang.manage.xtalk-scaffold
  (:require [clojure.edn :as edn]
            [clojure.tools.reader :as reader]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [std.lang.manage.xtalk-ops :as xtalk-ops]
            [code.project :as project]
            [std.block.reader :as block-reader]
            [std.fs :as fs]))

(def ^:dynamic *grammar-test-path*
  "test/std/lang/base/grammar_xtalk_ops_test.clj")

(def ^:dynamic *grammar-test-ns*
  'std.lang.base.grammar-xtalk-ops-test)

(def ^:dynamic *runtime-test-langs*
  [:js :lua :python :r :php :dart])

(def ^:dynamic *canonical-runtime-lang*
  :lua)

(def +runtime-lang-config+
  {:js     {:script :js
            :dispatch '!.js
            :suffix "js"
            :runtime :basic
            :check-mode :realtime}
   :lua    {:script :lua
            :dispatch '!.lua
            :suffix "lua"
            :runtime :basic
            :check-mode :realtime}
   :python {:script :python
            :dispatch '!.py
            :suffix "python"
            :runtime :basic
            :check-mode :realtime}
   :r      {:script :r
            :dispatch '!.R
            :suffix "r"
            :runtime :basic
            :check-mode :realtime}
   :rb     {:script :ruby
            :dispatch '!.rb
            :suffix "rb"
            :runtime :basic
            :check-mode :realtime}
   :dart   {:script :dart
            :dispatch '!.dt
            :suffix "dt"
            :runtime :twostep
            :check-mode :batched}
   :php    {:script :php
            :dispatch '!.php
            :suffix "php"
            :runtime :basic
            :check-mode :realtime}
   :go    {:script :go
            :dispatch '!.go
            :suffix "go"
            :runtime :twostep
            :check-mode :batched}})

(def +runtime-lang-aliases+
  {:ruby :rb})

(def +runtime-executable-langs+
  #{:js :lua :python :r :rb :php})

(def ^:dynamic *xtlang-runtime-test-root*
  "test/xt/lang")

(def ^:dynamic *xtbench-root*
  "xtbench")

(def ^:dynamic *twostep-runtime-blockers*
  '#{notify/wait-on
     notify/wait-on-call
     l/annex:restart-all
     l/annex:get
     l/annex:stop-all
     server/run-server
     ws/service-register})

(declare runtime-expr-lang
          expand-top-level-form
          fact-form?
          test-file-path
          runtime-lang-suffix)

(defn runtime-bench-ns?
  [ns]
  (boolean (re-find #"^xtbench\.[^.]+\." (str ns))))

(defn canonical-runtime-source-test-ns
  [test-ns]
  (let [test-ns (project/test-ns test-ns)
        ns-str (str test-ns)]
    (cond
      (runtime-bench-ns? test-ns)
      (let [[_ tail] (re-find #"^xtbench\.[^.]+\.(.+)$" ns-str)]
        (symbol (str "xt." tail)))

      :else
      (or (some (fn [lang]
                  (let [suffix (runtime-lang-suffix lang)
                        pattern (re-pattern (str "-(" (java.util.regex.Pattern/quote suffix) ")-test$"))]
                    (when (re-find pattern ns-str)
                      (symbol (str (subs ns-str 0 (- (count ns-str)
                                                     (+ (count suffix) 6)))
                                   "-test")))))
                (keys +runtime-lang-config+))
          test-ns))))

(defn read-xtalk-ops
  [path]
  (edn/read-string (slurp path)))

(defn quoted-form-string
  [x]
  (if (or (symbol? x)
          (seq? x)
          (vector? x)
          (map? x)
          (set? x))
    (str "'" (pr-str x))
    (pr-str x)))

(defn grammar-entry?
  [entry]
  (let [macro-sym (:macro entry)]
    (and (symbol? macro-sym)
         (= "std.lang.base.grammar-xtalk"
            (namespace macro-sym))
         (not (:skip? entry)))))

(defn grammar-entries
  [entries]
  (->> entries
       (filter grammar-entry?)
       (sort-by (juxt (comp name :category)
                      (comp str :canonical-symbol)))
       vec))

(defn macro-added
  [sym]
  (when (symbol? sym)
    (require (symbol (namespace sym)))
    (when-let [v (resolve sym)]
      (:added (meta v)))))

(defn case-xtalk-expect
  [case-entry]
  (let [expect (:expect case-entry)]
    (if (map? expect)
      (or (:xtalk expect)
          (:grammar expect))
      expect)))

(defn render-grammar-assertion
  [macro-sym case-entry]
  (let [call-sym (symbol (name macro-sym))
        input (:input case-entry)
        expect (case-xtalk-expect case-entry)]
    (str "  (" call-sym " " (quoted-form-string input) ")\n"
         "  => " (quoted-form-string expect))))

(defn render-grammar-fact
  [entry]
  (let [macro-sym (:macro entry)
        added (macro-added macro-sym)
        meta-form (cond-> {:refer macro-sym}
                    added (assoc :added added))
        title (or (:doc entry)
                  (str "TODO " (:canonical-symbol entry)))
        cases (->> (:cases entry)
                   (filter #(and (contains? % :input)
                                 (some? (case-xtalk-expect %))))
                   vec)
        body (if (seq cases)
               (str "\n"
                    (str/join "\n\n"
                              (map #(render-grammar-assertion macro-sym %)
                                   cases)))
               "")]
    (str "^" (pr-str meta-form) "\n"
         "(fact " (pr-str title) body ")")))

(defn render-grammar-test-file
  [entries]
  (str "(ns " *grammar-test-ns* "\n"
       "  (:require [std.lang.base.grammar-xtalk :refer :all])\n"
       "  (:use code.test))\n\n"
       ";; generated from xtalk_ops.edn\n\n"
       (str/join "\n\n"
                 (map render-grammar-fact
                      (grammar-entries entries)))
       "\n"))

(defn grammar-test-path
  ([project]
   (grammar-test-path project nil))
  ([project path]
   (str (fs/path (:root project)
                 (or path *grammar-test-path*)))))

(defn scaffold-xtalk-grammar-tests
  "Renders a grammar xtalk test scaffold from xtalk_ops.edn."
  ([_ {:keys [ops-path output-path write]
        :or {write false}}]
   (let [proj (project/project)
         ops-path (xtalk-ops/ops-path proj ops-path)
         test-path (grammar-test-path proj output-path)
         entries (read-xtalk-ops ops-path)
         content (render-grammar-test-file entries)
         original (when (fs/exists? test-path)
                    (slurp test-path))
         updated (not= original content)]
     (when write
       (fs/create-directory (fs/parent test-path))
       (spit test-path content))
      {:path test-path
       :ops-path ops-path
       :count (count (grammar-entries entries))
       :updated updated
       :content content}))
  ([_ params _ _]
   (scaffold-xtalk-grammar-tests nil params)))

(def +runtime-dispatch-map+
  (into {}
        (map (fn [[lang {:keys [dispatch]}]]
               [(str dispatch) lang]))
        +runtime-lang-config+))

(defn normalize-runtime-lang
  [lang]
  (let [lang (cond (keyword? lang) lang
                   (symbol? lang) (keyword (name lang))
                   (string? lang) (keyword lang)
                   (nil? lang) :js
                   :else lang)]
    (or (get +runtime-lang-aliases+ lang)
        lang)))

(defn runtime-lang-config
  [lang]
  (let [lang (normalize-runtime-lang lang)]
    (or (get +runtime-lang-config+ lang)
        (throw (ex-info "Unsupported xtalk runtime language"
                        {:lang lang
                         :available (sort (keys +runtime-lang-config+))})))))

(defn runtime-script-lang
  [lang]
  (:script (runtime-lang-config lang)))

(defn runtime-dispatch-symbol
  [lang]
  (:dispatch (runtime-lang-config lang)))

(defn runtime-type
  [lang]
  (:runtime (runtime-lang-config lang)))

(defn runtime-check-mode
  [lang]
  (:check-mode (runtime-lang-config lang)))

(defn runtime-suite-groups
  ([] (runtime-suite-groups (keys +runtime-lang-config+)))
  ([langs]
   (->> langs
        (map normalize-runtime-lang)
        distinct
        sort
        (group-by runtime-check-mode)
        (map (fn [[mode grouped-langs]]
               [mode (vec (sort grouped-langs))]))
        (into (sorted-map)))))

(defn runtime-lang-suffix
  [lang]
  (:suffix (runtime-lang-config lang)))

(defn canonical-suite-path
  [path]
  (str/replace path #"(?:_test)?\.clj$" "_suite.edn"))

(defn runtime-bulk-path
  [path lang]
  (str/replace path #"\.edn$"
               (str "-" (runtime-lang-suffix lang) "-bulk.edn")))

(defn read-top-level-forms
  [path]
  (let [rdr (block-reader/create (slurp path))]
    (loop [forms []]
      (let [form (try
                   (reader/read {:read-cond :allow
                                 :eof ::eof}
                                rdr)
                   (catch Throwable t
                     (throw (ex-info "Unable to read runtime test forms"
                                     {:path path
                                      :line (first (block-reader/reader-position rdr))
                                      :column (second (block-reader/reader-position rdr))}
                                     t))))]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn form-line-info
  [form]
  (let [m (meta form)]
    (let [line (or (:line m) (:row m))
          column (or (:column m) (:col m))
          end-line (or (:end-line m) (:end-row m))
          end-column (or (:end-column m) (:end-col m))]
    (cond-> {}
      line (assoc :line line)
      column (assoc :column column)
      end-line (assoc :end-line end-line)
      end-column (assoc :end-column end-column)))))

(defn merge-language-exceptions
  [& inputs]
  (reduce (fn [out m]
            (merge-with merge out (or m {})))
          {}
          inputs))

(defn form-language-exceptions
  [form]
  (let [m (meta form)]
    (or (:lang-exceptions m)
        (:exceptions m)
        {})))

(defn strip-runtime-dispatch
  "Removes runtime dispatch wrappers like `!.lua` while preserving the underlying form."
  [form]
  (cond (seq? form)
        (let [head (first form)]
          (if-let [lang (get +runtime-dispatch-map+ (str head))]
            (let [items (map strip-runtime-dispatch (rest form))]
              (cond (empty? items) nil
                    (= 1 (count items)) (first items)
                    :else (with-meta (apply list 'do items) (meta form))))
            (with-meta (apply list (map strip-runtime-dispatch form))
              (meta form))))

        (vector? form)
        (with-meta (vec (map strip-runtime-dispatch form))
          (meta form))

        (set? form)
        (with-meta (into #{} (map strip-runtime-dispatch form))
          (meta form))

        (map? form)
        (with-meta (into (empty form)
                         (map (fn [[k v]]
                                [(strip-runtime-dispatch k)
                                 (strip-runtime-dispatch v)])
                              form))
          (meta form))

        :else
        form))

(defn fact-assertion-forms
  [fact-form]
  (let [[_ _ & body] fact-form]
    (loop [xs body
           out []]
      (if (empty? xs)
        out
        (let [[expr arrow expect & more] xs]
          (if (= '=> arrow)
            (recur more (conj out [expr expect]))
            (recur (rest xs) out)))))))

(defn canonical-case-id
  [source-ns title idx]
  (str source-ns "::"
       (str/replace (str title) #"\s+" "-")
       "::"
       idx))

(defn fact-runtime-cases
  [source-ns fact-form lang]
  (let [[_ title] fact-form
        fact-meta (meta fact-form)
        fact-line (form-line-info fact-form)
        fact-exceptions (form-language-exceptions fact-form)]
    (->> (fact-assertion-forms fact-form)
         (keep-indexed (fn [idx [expr expect]]
                         (when (= lang (runtime-expr-lang expr))
                           (let [expr-line (or (not-empty (form-line-info expr))
                                               fact-line)
                                 expr-exceptions (form-language-exceptions expr)]
                             {:id (canonical-case-id source-ns title idx)
                              :title title
                              :lang lang
                              :line expr-line
                              :expr expr
                              :form (strip-runtime-dispatch expr)
                              :expect expect
                              :exceptions (merge-language-exceptions fact-exceptions
                                                                    expr-exceptions)
                              :meta (dissoc fact-meta
                                            :line
                                            :column
                                            :end-line
                                            :end-column)}))))
         vec)))

(defn canonical-runtime-suite-forms
  ([forms]
   (canonical-runtime-suite-forms forms *canonical-runtime-lang*))
  ([forms lang]
   (let [lang (normalize-runtime-lang lang)
         expanded (mapcat expand-top-level-form forms)
         source-ns (some #(when (and (seq? %) (= 'ns (first %))) (second %)) forms)
         facts (filter fact-form? expanded)
         cases (mapcat #(fact-runtime-cases source-ns % lang) facts)]
     {:ns source-ns
      :lang lang
      :runtime-type (runtime-type lang)
      :check-mode (runtime-check-mode lang)
      :cases (vec cases)
      :exceptions (into (sorted-map)
                        (keep (fn [{:keys [id exceptions]}]
                                (when (seq exceptions)
                                  [id exceptions])))
                        cases)})))

(defn case-language-config
  [case lang]
  (let [lang (normalize-runtime-lang lang)
        exception (get (:exceptions case) lang)]
    {:skip (true? (:skip exception))
     :expect (if (contains? exception :expect)
               (:expect exception)
               (:expect case))
     :form (or (:form exception)
               (:form case))
     :exception exception}))

(defn compile-runtime-bulk-suite
  [suite lang]
  (let [lang (normalize-runtime-lang lang)
        prepared (mapv (fn [case]
                         (merge case (case-language-config case lang)))
                       (:cases suite))
        active (remove :skip prepared)]
    {:ns (:ns suite)
     :source-lang (:lang suite)
     :lang lang
     :runtime-type (runtime-type lang)
     :check-mode (runtime-check-mode lang)
     :bulk-form (vec
                 (for [{:keys [id line form]} active]
                   {:id id
                    :line line
                    :value form}))
     :verify (vec
              (for [{:keys [id title line expect]} active]
                {:id id
                 :title title
                 :line line
                 :expect expect}))
     :skipped (vec
               (for [{:keys [id title line exception skip]} prepared
                     :when skip]
                 {:id id
                  :title title
                  :line line
                  :exception exception}))}))

(defn export-runtime-suite
  "Exports a canonical runtime test namespace to EDN cases."
  ([_ {:keys [input-path output-path lang write]
        :or {lang *canonical-runtime-lang*
             write false}
       :as params}]
   (let [proj (project/project)
         source-test-ns (some-> (:ns params) project/test-ns)
         input-path (or input-path
                        (when source-test-ns
                          (test-file-path proj source-test-ns))
                        (throw (ex-info "Requires runtime suite input-path or a valid test :ns"
                                        {:params (dissoc params :write)})))
         suite (canonical-runtime-suite-forms (read-top-level-forms input-path) lang)
         output-path (or output-path
                         (canonical-suite-path input-path))
         content (pr-str suite)]
     (when write
       (fs/create-directory (fs/parent output-path))
       (spit output-path content))
      {:input-path input-path
       :output-path output-path
       :lang (:lang suite)
       :count (count (:cases suite))
       :suite suite
       :content content}))
  ([_ params _ _]
   (export-runtime-suite nil params)))

(defn compile-runtime-bulk
  "Compiles a canonical runtime EDN suite into a batched verification payload."
  ([_ {:keys [input-path output-path lang write]
        :or {write false}}]
   (let [lang (normalize-runtime-lang lang)
         input-path (or input-path
                        (throw (ex-info "Requires canonical suite input-path"
                                        {:lang lang})))
         suite (edn/read-string (slurp input-path))
         bulk (compile-runtime-bulk-suite suite lang)
         output-path (or output-path
                         (runtime-bulk-path input-path lang))
         content (pr-str bulk)]
     (when write
       (fs/create-directory (fs/parent output-path))
       (spit output-path content))
      {:input-path input-path
       :output-path output-path
       :lang lang
       :count (count (:verify bulk))
       :bulk bulk
       :content content}))
  ([_ params _ _]
   (compile-runtime-bulk nil params)))

(defn runtime-expr-lang
  [expr]
  (letfn [(collect-langs [form]
            (cond (seq? form)
                  (if-let [lang (get +runtime-dispatch-map+ (str (first form)))]
                    #{lang}
                    (reduce into #{} (map collect-langs form)))

                  (vector? form)
                  (reduce into #{} (map collect-langs form))

                  (set? form)
                  (reduce into #{} (map collect-langs form))

                  (map? form)
                  (reduce into #{}
                          (concat (map collect-langs (keys form))
                                  (map collect-langs (vals form))))

                  :else
                  #{}))]
    (let [langs (collect-langs expr)]
      (when (= 1 (count langs))
        (first langs)))))

(defn fact-form?
  [form]
  (and (seq? form)
       (= 'fact (first form))))

(defn fact-global-form?
  [form]
  (and (seq? form)
       (= 'fact:global (first form))))

(defn script-form?
  [form]
  (and (seq? form)
       (= 'l/script- (first form))))

(defn expand-top-level-form
  [form]
  (if (and (seq? form)
           (= 'do (first form)))
    (rest form)
    [form]))

(defn replace-ns-name
  [ns-form new-ns]
  (with-meta
    (apply list 'ns new-ns (drop 2 ns-form))
    (meta ns-form)))

(defn runtime-test-ns
  [test-ns lang]
  (let [lang (normalize-runtime-lang lang)
        source-test-ns (canonical-runtime-source-test-ns test-ns)
        source-str (str source-test-ns)]
    (symbol
     (cond
       (str/starts-with? source-str "xt.")
       (str "xtbench." (name lang) "." (subs source-str 3))

       :else
       (str "xtbench." (name lang) "." source-str)))))

(defn render-top-level-forms
  [forms]
  (binding [pprint/*print-right-margin* 100
            *print-meta* true
            *print-length* nil
            *print-level* nil]
    (with-out-str
      (doseq [form forms]
        (pprint/pprint form)
        (println)))))

(defn attach-leading-meta
  [clauses metadata]
  (if (and metadata (seq clauses))
    (let [[expr & more] clauses]
      (cons (if (seq? expr)
              (with-meta expr (merge metadata (meta expr)))
              expr)
            more))
    clauses))

(defn commented-form?
  [form]
  (boolean (:comment (meta form))))

(defn test-file-path
  [project test-ns]
  (or (project/get-path test-ns project)
      (if (runtime-bench-ns? test-ns)
        (str (fs/path (:root project)
                      (str (fs/ns->file test-ns) ".clj")))
        (str (fs/path (:root project)
                      (or (first (:test-paths project))
                          "test")
                      (str (fs/ns->file test-ns) ".clj"))))))

(defn infer-runtime-lang
  [forms]
  (let [expanded (mapcat expand-top-level-form forms)]
    (some (fn [form]
            (when (script-form? form)
              (let [[_ lang] form]
                (some (fn [[k {:keys [script]}]]
                        (when (= lang script) k))
                       +runtime-lang-config+))))
           expanded)))

(defn runtime-script-langs
  [forms]
  (let [expanded (mapcat expand-top-level-form forms)]
    (->> expanded
         (keep (fn [form]
                 (when (script-form? form)
                   (let [[_ script-val] form]
                     (some (fn [[k {:keys [script]}]]
                             (when (= script-val script)
                               k))
                            +runtime-lang-config+)))))
         distinct
         vec)))

(defn single-runtime-template-lang
  [forms]
  (let [langs (runtime-script-langs forms)]
    (when (= 1 (count langs))
      (first langs))))

(defn runtime-suffixed-test-ns?
  [test-ns]
  (let [suffixes (->> (keys +runtime-lang-config+)
                      (map runtime-lang-suffix)
                      distinct
                      sort)
        suffixed-pattern  (re-pattern
                           (str "-(" (str/join "|" (map #(java.util.regex.Pattern/quote %) suffixes))
                                ")-test$"))]
    (or (runtime-bench-ns? test-ns)
        (boolean (re-find suffixed-pattern (str test-ns))))))

(defn form-contains-symbol?
  [form targets]
  (let [targets (set targets)]
    (cond (symbol? form)
          (contains? targets form)

          (seq? form)
          (boolean (some #(form-contains-symbol? % targets) form))

          (vector? form)
          (boolean (some #(form-contains-symbol? % targets) form))

          (set? form)
          (boolean (some #(form-contains-symbol? % targets) form))

          (map? form)
          (boolean
           (or (some #(form-contains-symbol? % targets) (keys form))
               (some #(form-contains-symbol? % targets) (vals form))))

          :else
          false)))

(defn runtime-template-supported?
  [forms lang]
  (let [lang (normalize-runtime-lang lang)]
    (cond (nil? (single-runtime-template-lang forms))
          false

          (runtime-suffixed-test-ns?
           (some #(when (and (seq? %) (= 'ns (first %))) (second %)) forms))
          false

          (= :twostep (runtime-type lang))
          (not (some #(form-contains-symbol? % *twostep-runtime-blockers*) forms))

          :else
          true)))

(defn replace-runtime-symbol
  [form from-dispatch to-dispatch]
  (let [recur-form (fn recur-form [form]
                     (cond (seq? form)
                           (let [items (map recur-form form)
                                 updated (apply list items)
                                 updated (if (= from-dispatch (first updated))
                                           (cons to-dispatch (rest updated))
                                           updated)]
                             (with-meta updated (meta form)))

                           (vector? form)
                           (with-meta (vec (map recur-form form))
                             (meta form))

                           (set? form)
                           (with-meta (into #{} (map recur-form form))
                             (meta form))

                           (map? form)
                           (with-meta (into (empty form)
                                            (map (fn [[k v]]
                                                   [(recur-form k) (recur-form v)])
                                                 form))
                             (meta form))

                           :else
                           form))]
    (recur-form form)))

(defn replace-string-value
  [form from-value to-value]
  (let [recur-form (fn recur-form [form]
                     (cond (seq? form)
                           (with-meta (apply list (map recur-form form))
                             (meta form))

                           (vector? form)
                           (with-meta (vec (map recur-form form))
                             (meta form))

                           (set? form)
                           (with-meta (into #{} (map recur-form form))
                             (meta form))

                           (map? form)
                           (with-meta (into (empty form)
                                            (map (fn [[k v]]
                                                   [(recur-form k) (recur-form v)])
                                                 form))
                             (meta form))

                           (= from-value form)
                           to-value

                           :else
                           form))]
    (recur-form form)))

(defn transform-script-form
  [form from-script to-script]
  (if (and (script-form? form)
           (= from-script (second form)))
    (with-meta
       (apply list 'l/script- to-script (drop 2 form))
       (meta form))
     form))

(defn transform-script-runtime
  [form lang]
  (if (script-form? form)
    (let [[script-fn script opts & more] form
          opts (if (map? opts)
                 (assoc opts :runtime (runtime-type lang))
                 opts)]
      (with-meta
        (apply list script-fn script opts more)
        (meta form)))
    form))

(defn template-runtime-test-ns
  [source-test-ns from-lang to-lang]
  (runtime-test-ns (canonical-runtime-source-test-ns source-test-ns)
                   to-lang))

(defn template-runtime-test-forms
  [forms from-lang to-lang]
  (let [from-lang (normalize-runtime-lang from-lang)
        to-lang (normalize-runtime-lang to-lang)
        from-script (runtime-script-lang from-lang)
        to-script (runtime-script-lang to-lang)
        from-dispatch (runtime-dispatch-symbol from-lang)
        to-dispatch (runtime-dispatch-symbol to-lang)
        source-ns (some #(when (and (seq? %) (= 'ns (first %))) (second %)) forms)
        target-ns (template-runtime-test-ns source-ns from-lang to-lang)
        source-ns-str (str source-ns)
        target-ns-str (str target-ns)]
    (mapv (fn [form]
            (cond (and (seq? form) (= 'ns (first form)))
                  (replace-ns-name form target-ns)

                   :else
                   (-> form
                       (transform-script-form from-script to-script)
                       (transform-script-runtime to-lang)
                       (replace-runtime-symbol from-dispatch to-dispatch)
                       (replace-string-value source-ns-str target-ns-str))))
           forms)))

(defn split-fact-form
  [fact-form langs]
  (let [[fact-sym title & body] fact-form]
    (if (empty? body)
      {:shared fact-form
       :runtime? false
       :langs {}}
      (loop [xs body
             runtime? false
             prefix []
             shared []
             leading-meta nil
             by-lang (zipmap langs (repeat []))]
        (if (empty? xs)
           {:shared (cond
                      (not runtime?)
                      fact-form

                      (seq shared)
                      (with-meta
                        (apply list fact-sym title (concat prefix shared))
                        (meta fact-form))

                      :else
                      (with-meta
                        (list fact-sym title)
                        (meta fact-form)))
            :runtime? runtime?
            :langs (into {}
                        (keep (fn [[lang clauses]]
                                (when (seq clauses)
                                  [lang (with-meta
                                          (apply list
                                                 fact-sym
                                                 title
                                                 (concat prefix
                                                         (attach-leading-meta clauses leading-meta)))
                                          (meta fact-form))])))
                        by-lang)}
          (let [[expr arrow expect & more] xs]
            (if (= '=> arrow)
              (if-let [lang (runtime-expr-lang expr)]
                (recur more
                       true
                       prefix
                       shared
                       (or leading-meta (meta expr))
                       (update by-lang lang into [expr arrow expect]))
                (recur more
                       runtime?
                       prefix
                       (into shared [expr arrow expect])
                       leading-meta
                       by-lang))
              (recur (rest xs)
                     runtime?
                     (cond-> prefix
                       (not (commented-form? expr))
                       (conj expr))
                     shared
                     leading-meta
                     by-lang))))))))

(defn separate-runtime-test-forms
  [forms langs]
  (let [expanded (mapcat expand-top-level-form forms)
        ns-form (some #(when (and (seq? %) (= 'ns (first %))) %) forms)
        scripts (->> expanded
                     (filter script-form?)
                     (group-by #(second %)))
        fact-global (some #(when (fact-global-form? %) %) expanded)
        facts (filter fact-form? expanded)
        shared-facts (atom [])
        by-lang (atom (zipmap langs (repeat [])))]
    (doseq [fact-form facts]
      (let [{:keys [shared langs runtime?]} (split-fact-form fact-form langs)]
        (when (and shared
                   (or (not runtime?)
                       (> (count shared) 2)))
          (swap! shared-facts conj shared))
        (doseq [[lang split-form] langs]
          (swap! by-lang update lang conj split-form))))
    {:shared (vec (concat
                   [(replace-ns-name ns-form (second ns-form))]
                   (remove nil?
                           [(when fact-global fact-global)])
                   @shared-facts))
      :by-lang (into {}
                     (keep (fn [lang]
                             (let [script-form (first (get scripts lang))
                                  fact-forms (get @by-lang lang)]
                              (when (seq fact-forms)
                                [lang (vec (concat
                                            [(replace-ns-name ns-form
                                                              (runtime-test-ns (second ns-form)
                                                                               lang))]
                                            (when script-form [script-form])
                                            (when fact-global [fact-global])
                                            fact-forms))]))))
                    langs)}))

(defn separate-runtime-tests
  "Splits a multi-runtime test namespace into per-language test files."
  ([_ {:keys [input-path langs write]
        :or {langs *runtime-test-langs*
             write false}
       :as params}]
   (let [proj (project/project)
         test-ns (project/test-ns (:ns params))
         input-path (or input-path
                        (test-file-path proj test-ns))
         forms (read-top-level-forms input-path)
         {:keys [shared by-lang]} (separate-runtime-test-forms forms langs)
         shared-path (test-file-path proj test-ns)
         outputs (into []
                       (concat
                        [{:lang :shared
                          :path shared-path
                          :forms shared}]
                        (for [[lang out-forms] by-lang
                              :let [out-ns (runtime-test-ns test-ns lang)]]
                          {:lang lang
                           :path (test-file-path proj out-ns)
                           :forms out-forms})))
         rendered (mapv (fn [{:keys [path forms] :as entry}]
                          (assoc entry :content (render-top-level-forms forms)))
                        outputs)]
     (when write
       (doseq [{:keys [path content]} rendered]
         (fs/create-directory (fs/parent path))
         (spit path content)))
     {:input-path input-path
      :langs (vec langs)
      :outputs (mapv #(select-keys % [:lang :path]) rendered)
      :counts (into {}
                    (map (fn [{:keys [lang forms]}]
                           [lang (count (filter fact-form? forms))]))
                    outputs)}))
  ([_ params _ _]
   (separate-runtime-tests nil params)))

(defn scaffold-runtime-template
  "Generates a target runtime test file from a single-runtime template file."
  ([_ {:keys [input-path output-path lang from-lang write]
        :or {write false}
        :as params}]
   (let [proj (project/project)
         source-test-ns (project/test-ns (:ns params))
         input-path (or input-path
                        (test-file-path proj source-test-ns))
         forms (read-top-level-forms input-path)
         from-lang (or from-lang
                       (infer-runtime-lang forms)
                       (throw (ex-info "Cannot infer template runtime language"
                                       {:path input-path
                                        :ns source-test-ns})))
         to-lang (normalize-runtime-lang lang)
         target-ns (template-runtime-test-ns source-test-ns from-lang to-lang)
         output-path (or output-path
                         (test-file-path proj target-ns))
         rendered-forms (template-runtime-test-forms forms from-lang to-lang)
         content (render-top-level-forms rendered-forms)]
     (when write
       (fs/create-directory (fs/parent output-path))
       (spit output-path content))
      {:input-path input-path
       :output-path output-path
       :from-lang (normalize-runtime-lang from-lang)
       :lang to-lang
       :source-ns source-test-ns
       :target-ns target-ns
       :updated (not= (when (fs/exists? output-path)
                        (slurp output-path))
                      content)
       :content content}))
  ([_ params _ _]
   (scaffold-runtime-template nil params)))

(defn xtlang-runtime-suite-sources
  "Lists canonical xt.lang runtime test templates that can be exported and
   compiled for the target runtime."
  ([params]
   (xtlang-runtime-suite-sources nil params))
  ([_ {:keys [root input-root lang]
       :or {input-root *xtlang-runtime-test-root*
            lang :dart}}]
   (let [proj (merge (project/project)
                     (select-keys {:root root} [:root]))
         root (:root proj)
         scan-root (str (fs/path root input-root))
         paths (if (fs/exists? scan-root)
                 (->> (keys (fs/list scan-root {:recursive true
                                               :include [".clj$"]}))
                       (map str)
                       (filter #(str/ends-with? % "_test.clj"))
                       sort)
                 [])]
     (->> paths
          (keep (fn [path]
                  (let [forms (read-top-level-forms path)
                        source-ns (some #(when (and (seq? %) (= 'ns (first %)))
                                           (second %))
                                        forms)
                        from-lang (single-runtime-template-lang forms)]
                    (when (and source-ns
                               from-lang
                               (not (runtime-suffixed-test-ns? source-ns))
                               (runtime-template-supported? forms lang))
                      {:path path
                       :ns source-ns
                       :from-lang from-lang
                       :lang (normalize-runtime-lang lang)
                       :runtime-type (runtime-type lang)
                       :check-mode (runtime-check-mode lang)}))))
          vec)))
  ([_ params _ _]
   (xtlang-runtime-suite-sources nil params)))

(defn compile-xtlang-runtime-bulk-suites
  "Exports canonical runtime suites from eligible xt.lang tests and compiles
   batched twostep bulk payloads for the target runtime."
  ([params]
   (compile-xtlang-runtime-bulk-suites nil params))
  ([_ {:keys [root input-root lang write]
       :or {input-root *xtlang-runtime-test-root*
            lang :dart
            write false}
       :as params}]
   (let [sources (xtlang-runtime-suite-sources {:root root
                                                :input-root input-root
                                                :lang lang})
         outputs (mapv (fn [{:keys [path from-lang] :as source}]
                         (let [{suite-path :output-path
                                suite :suite}
                                (export-runtime-suite nil {:input-path path
                                                           :lang from-lang
                                                           :write write})
                               bulk (compile-runtime-bulk-suite suite lang)
                               bulk-path (runtime-bulk-path suite-path lang)
                               bulk-content (pr-str bulk)
                               _ (when write
                                   (fs/create-directory (fs/parent bulk-path))
                                   (spit bulk-path bulk-content))]
                            (assoc source
                                   :suite-path suite-path
                                   :bulk-path bulk-path
                                   :suite-count (count (:cases suite))
                                   :bulk-count (count (:verify bulk))
                                   :runtime-type (:runtime-type bulk)
                                   :check-mode (:check-mode bulk))))
                       sources)]
      {:lang (normalize-runtime-lang lang)
       :input-root input-root
       :count (count outputs)
       :outputs outputs}))
  ([_ params _ _]
   (compile-xtlang-runtime-bulk-suites nil params)))
