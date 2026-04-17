(ns std.lang.manage.xtalk-scaffold
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [code.manage.fn-format :as fn-format]
            [code.manage.ns-format :as ns-format]
            [code.project :as project]
            [clojure.tools.reader :as reader]
            [std.block :as block]
            [std.block.navigate :as nav]
            [std.block.reader :as block-reader]
            [std.fs :as fs]
            [std.lang.manage.xtalk-ops :as xtalk-ops]))

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

(def ^:dynamic *template-runtime-symbol-blockers*
  {:lua '#{setmetatable
           getmetatable
           rawget
           rawset
           rawequal}})

(declare runtime-expr-lang
            expand-top-level-form
            fact-form?
            script-form?
            fact-global-form?
            clean-render-form
             test-file-path
             transform-script-form
             transform-script-runtime
             runtime-lang-suffix
            template-runtime-test-ns
            template-runtime-blockers
            xtlang-runtime-suite-sources)

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

(defn form-skipped-for-lang?
  [form lang]
  (true? (get-in (form-language-exceptions form)
                 [(normalize-runtime-lang lang) :skip])))

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

(defn source-test-ns-from-forms
  [forms]
  (some #(when (and (seq? %) (= 'ns (first %))) (second %)) forms))

(defn namespace-pattern?
  [x]
  (boolean (and x
                (str/includes? (str x) "*"))))

(defn namespace-pattern-regex
  [pattern]
  (re-pattern
   (str "^"
        (->> (str/split (str pattern) #"\*" -1)
             (map #(java.util.regex.Pattern/quote %))
             (str/join ".*"))
        "$")))

(defn namespace-pattern-match?
  [pattern ns]
  (boolean (re-find (namespace-pattern-regex pattern) (str ns))))

(defn canonical-runtime-suite-forms
  ([forms]
   (canonical-runtime-suite-forms forms *canonical-runtime-lang*))
  ([forms lang]
   (let [lang (normalize-runtime-lang lang)
          expanded (mapcat expand-top-level-form forms)
          source-ns (source-test-ns-from-forms forms)
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

(defn runtime-dispatch-langs
  [form]
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
    (->> (collect-langs form)
         sort
         vec)))

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

(defn clean-render-meta-map
  [m]
  (let [m (not-empty
           (reduce dissoc (or m {})
                   [:line :column :end-line :end-column
                    :row :col :end-row :end-col :file
                    :lang-exceptions :exceptions]))]
    (when m
      (into (empty m)
            (map (fn [[k v]]
                   [(clean-render-form k) (clean-render-form v)]))
            m))))

(defn clean-render-iobj
  [obj original]
  (if (instance? clojure.lang.IObj obj)
    (if-let [m (clean-render-meta-map (meta original))]
      (with-meta obj m)
      (with-meta obj nil))
    obj))

(defn clean-render-form
  [form]
  (cond (seq? form)
        (clean-render-iobj (apply list (map clean-render-form form)) form)

        (vector? form)
        (clean-render-iobj (vec (map clean-render-form form)) form)

        (set? form)
        (clean-render-iobj (into #{} (map clean-render-form form)) form)

        (map? form)
        (clean-render-iobj
         (into (empty form)
               (map (fn [[k v]]
                      [(clean-render-form k) (clean-render-form v)]))
               form)
         form)

        (instance? clojure.lang.IObj form)
        (clean-render-iobj form form)

        :else
        form))

(defn render-top-level-forms
  [forms]
  (->> forms
       (map clean-render-form)
       (map (fn [form]
              (binding [*print-meta* true]
                (with-out-str
                  (pprint/pprint form)))))
       (str/join "\n")
       (#(if (str/ends-with? % "\n")
           %
           (str % "\n")))))

(defn format-generated-namespace!
  [proj target-ns output-path]
  (let [proj (or proj (project/project))
        lookup (project/file-lookup proj)
        default-output-path (test-file-path proj target-ns)]
    (when (= output-path default-output-path)
      (ns-format/ns-format target-ns {:write true} lookup proj)
      (fn-format/fn-format target-ns {:write true} lookup proj))))

(defn delete-skipped-top-level-forms
  [source forms lang]
  (let [lang (normalize-runtime-lang lang)
        root (nav/parse-root source)
        start (nav/down root)]
    (loop [state-nav root
           current start
           remaining forms]
      (cond (or (nil? current)
                (empty? remaining))
            (nav/root-string state-nav)

            (form-skipped-for-lang? (first remaining) lang)
            (let [updated (nav/delete current)]
              (recur updated updated (rest remaining)))

            :else
            (recur current (nav/right current) (rest remaining))))))

(defn render-template-runtime-source
  [source forms source-test-ns from-lang to-lang]
  (let [from-lang (normalize-runtime-lang from-lang)
        to-lang (normalize-runtime-lang to-lang)
        from-script (runtime-script-lang from-lang)
        to-script (runtime-script-lang to-lang)
        from-dispatch (runtime-dispatch-symbol from-lang)
        to-dispatch (runtime-dispatch-symbol to-lang)
        target-ns (template-runtime-test-ns source-test-ns from-lang to-lang)
        source-ns-str (str source-test-ns)
        target-ns-str (str target-ns)
        root (nav/parse-root source)
        rewritten
        (loop [current (nav/down root)
               last-nav root]
          (if current
            (let [form (nav/value current)
                  updated (cond (and (seq? form)
                                     (= 'ns (first form))
                                     (= source-test-ns (second form)))
                                (if-let [ns-name-nav (some-> current nav/down nav/right)]
                                  (-> ns-name-nav
                                      (nav/replace target-ns)
                                      nav/up)
                                  current)

                                (script-form? form)
                                (let [head-nav (nav/down current)
                                      script-lang-nav (some-> head-nav nav/right)
                                      current (if script-lang-nav
                                                (-> script-lang-nav
                                                    (nav/replace to-script)
                                                    nav/up)
                                                current)
                                      opts-nav (some-> current nav/down nav/right nav/right)
                                      runtime-key-nav (when (and opts-nav
                                                                 (map? (nav/value opts-nav)))
                                                        (loop [entry (nav/down opts-nav)]
                                                          (cond
                                                            (nil? entry)
                                                            nil

                                                            (= :runtime (nav/value entry))
                                                            entry

                                                            :else
                                                            (recur (some-> entry nav/right nav/right)))))]
                                  (if-let [runtime-val-nav (some-> runtime-key-nav nav/right)]
                                    (-> runtime-val-nav
                                        (nav/replace (runtime-type to-lang))
                                        nav/up
                                        nav/up)
                                    current))

                                (and (instance? clojure.lang.IObj form)
                                     (seq (select-keys (meta form) [:lang-exceptions :exceptions])))
                                (nav/replace current (vary-meta (clean-render-form form)
                                                                dissoc
                                                                :lang-exceptions
                                                                :exceptions))

                                (= form from-dispatch)
                                (nav/replace current to-dispatch)

                                (= form source-ns-str)
                                (nav/replace current target-ns-str)

                                :else
                                current)]
              (recur (nav/next updated) updated))
            (nav/root-string last-nav)))]
    (delete-skipped-top-level-forms rewritten forms to-lang)))

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
      (str (fs/path (:root project)
                    (or (first (:test-paths project))
                        "test")
                    (str (fs/ns->file test-ns) ".clj")))))

(defn derived-test-file-path
  [project input-path source-test-ns target-test-ns]
  (let [project-path (test-file-path project target-test-ns)
        source-project-path (test-file-path project source-test-ns)]
    (if (= input-path source-project-path)
      project-path
      (str (fs/path (fs/parent input-path)
                    (str (fs/ns->file target-test-ns) ".clj"))))))

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
  (let [lang (normalize-runtime-lang lang)
        from-lang (single-runtime-template-lang forms)
        template-blockers (when from-lang
                            (template-runtime-blockers forms from-lang lang))]
    (cond (nil? (single-runtime-template-lang forms))
          false

          (runtime-suffixed-test-ns?
            (some #(when (and (seq? %) (= 'ns (first %))) (second %)) forms))
          false

          (or (seq (:twostep template-blockers))
              (seq (:runtime-specific template-blockers)))
          false

          (= :twostep (runtime-type lang))
          (not (some #(form-contains-symbol? % *twostep-runtime-blockers*) forms))

          :else
          true)))

(defn runtime-setup-langs
  [form]
  (letfn [(collect-langs [form]
            (cond (seq? form)
                  (if (= 'l/rt:scaffold (first form))
                    (let [[_ lang] form]
                      #{(normalize-runtime-lang lang)})
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
    (->> (collect-langs form)
         sort
         vec)))

(defn template-runtime-blockers
  ([forms]
   (template-runtime-blockers forms (single-runtime-template-lang forms) nil))
  ([forms from-lang to-lang]
   (let [from-lang (some-> from-lang normalize-runtime-lang)
         to-lang (some-> to-lang normalize-runtime-lang)
         twostep-blockers (->> *twostep-runtime-blockers*
                               (filter #(some (fn [form]
                                                (form-contains-symbol? form [%]))
                                              forms))
                               (sort-by str)
                               vec)
         runtime-specific (if (and from-lang
                                   to-lang
                                   (not= from-lang to-lang))
                            (->> (get *template-runtime-symbol-blockers* from-lang)
                                 (filter #(some (fn [form]
                                                  (form-contains-symbol? form [%]))
                                                forms))
                                 (sort-by str)
                                 vec)
                            [])]
     {:twostep twostep-blockers
      :runtime-specific runtime-specific})))

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

(defn retarget-runtime-dispatch
  [form lang]
  (let [target-dispatch (runtime-dispatch-symbol lang)
        dispatches (->> +runtime-lang-config+
                        vals
                        (map :dispatch)
                        set)
        recur-form (fn recur-form [form]
                     (cond (seq? form)
                           (let [items (map recur-form form)
                                 updated (apply list items)
                                 updated (if (contains? dispatches (first updated))
                                           (cons target-dispatch (rest updated))
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

(defn normalize-runtime-setup-form
  [form lang]
  (cond (nil? form)
        nil

        (and (seq? form)
             (= 'l/rt:scaffold (first form)))
        (let [[_ runtime] form]
          (when (= (normalize-runtime-lang runtime)
                   (normalize-runtime-lang lang))
            (list 'l/rt:scaffold (normalize-runtime-lang lang))))

        (and (seq? form)
             (= 'do (first form)))
        (let [items (keep #(normalize-runtime-setup-form % lang) (rest form))]
          (cond (empty? items) nil
                (= 1 (count items)) (first items)
                :else (with-meta (apply list 'do items) (meta form))))

        (seq? form)
        (with-meta (apply list (map #(normalize-runtime-setup-form % lang) form))
          (meta form))

        (vector? form)
        (with-meta (vec (keep #(normalize-runtime-setup-form % lang) form))
          (meta form))

        (set? form)
        (with-meta (into #{} (keep #(normalize-runtime-setup-form % lang) form))
          (meta form))

        (map? form)
        (with-meta (into (empty form)
                         (keep (fn [[k v]]
                                 (let [v' (normalize-runtime-setup-form v lang)]
                                   (when (some? v')
                                     [k v']))))
                         form)
          (meta form))

        :else
        form))

(defn normalize-fact-global-form
  [form lang]
  (if (fact-global-form? form)
    (let [[fact-global-sym opts & more] form
          opts (normalize-runtime-setup-form opts lang)]
      (with-meta
        (apply list fact-global-sym opts more)
        (meta form)))
    form))

(defn classify-split-form
  [form]
  (cond (fact-form? form)
        (let [[_ _ & body] form
              has-runtime-assertion (some true?
                                          (for [[expr arrow _] (partition 3 1 body)
                                                :when (= arrow '=>)]
                                            (some? (runtime-expr-lang expr))))
              has-foreign-runtime-prefix (some #(some? (runtime-expr-lang %))
                                               (take-while #(not= '=> %)
                                                           body))]
          (cond-> #{}
            has-runtime-assertion
            (conj :runtime-fact)

            has-foreign-runtime-prefix
            (conj :runtime-prefix)))

        (fact-global-form? form)
        #{:fact-global}

        (script-form? form)
        #{:script}

        :else
        #{:top-level-shared}))

(defn retarget-generated-form
  [form lang]
  (-> form
      (retarget-runtime-dispatch lang)
      (normalize-fact-global-form lang)))

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
         source-ns (source-test-ns-from-forms forms)
         target-ns (template-runtime-test-ns source-ns from-lang to-lang)
         source-ns-str (str source-ns)
         target-ns-str (str target-ns)]
    (->> forms
         (remove #(form-skipped-for-lang? % to-lang))
         (mapv (fn [form]
                 (cond (and (seq? form) (= 'ns (first form)))
                       (replace-ns-name form target-ns)

                       :else
                       (-> form
                           (transform-script-form from-script to-script)
                           (transform-script-runtime to-lang)
                           (replace-runtime-symbol from-dispatch to-dispatch)
                           (replace-string-value source-ns-str target-ns-str))))))))

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
                                                  (concat (map #(retarget-generated-form % lang) prefix)
                                                          (attach-leading-meta
                                                           (map #(retarget-generated-form % lang) clauses)
                                                           leading-meta)))
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
         shared-forms (atom [])
         by-lang (atom (zipmap langs (repeat [])))]
     (doseq [form expanded]
       (cond (or (and (seq? form) (= 'ns (first form)))
                 (script-form? form))
             nil

             (fact-global-form? form)
             (do
               (swap! shared-forms conj form)
               (doseq [lang langs]
                 (swap! by-lang update lang conj (normalize-fact-global-form form lang))))

             (fact-form? form)
             (let [{:keys [shared langs runtime?]} (split-fact-form form langs)]
               (when (and shared
                          (or (not runtime?)
                              (> (count shared) 2)))
                 (swap! shared-forms conj shared))
               (doseq [[lang split-form] langs]
                 (swap! by-lang update lang conj split-form)))

             :else
             (do
               (swap! shared-forms conj form)
               (doseq [lang langs]
                 (swap! by-lang update lang conj (retarget-generated-form form lang))))))
      {:shared (vec (concat
                     [(replace-ns-name ns-form (second ns-form))]
                     @shared-forms))
       :by-lang (into {}
                      (keep (fn [lang]
                              (let [script-form (first (get scripts lang))
                                    out-forms (get @by-lang lang)]
                                (when (seq out-forms)
                                  [lang (vec (concat
                                              [(replace-ns-name ns-form
                                                                (runtime-test-ns (second ns-form)
                                                                                 lang))]
                                              (when script-form
                                                [(-> script-form
                                                     (transform-script-runtime lang))])
                                              out-forms))])))
                            langs))}))

(defn separate-runtime-tests
  "Splits a multi-runtime test namespace into per-language test files."
  ([_ {:keys [input-path langs write write-shared]
         :or {langs *runtime-test-langs*
              write false
              write-shared false}
         :as params}]
   (let [proj (project/project)
         test-ns-param (some-> (:ns params) project/test-ns)
         input-path (or input-path
                        (when test-ns-param
                          (test-file-path proj test-ns-param))
                        (throw (ex-info "Requires runtime test input-path or a valid test :ns"
                                        {:params (dissoc params :write)})))
         forms (read-top-level-forms input-path)
         test-ns (or test-ns-param
                     (source-test-ns-from-forms forms)
                     (throw (ex-info "Unable to determine runtime test namespace"
                                     {:input-path input-path})))
         {:keys [shared by-lang]} (separate-runtime-test-forms forms langs)
         shared-path (derived-test-file-path proj input-path test-ns test-ns)
         outputs (into []
                       (concat
                        [{:lang :shared
                          :path shared-path
                          :forms shared}]
                         (for [[lang out-forms] by-lang
                               :let [out-ns (runtime-test-ns test-ns lang)]]
                           {:lang lang
                            :path (derived-test-file-path proj input-path test-ns out-ns)
                            :forms out-forms})))
         rendered (mapv (fn [{:keys [path forms] :as entry}]
                          (assoc entry :content (render-top-level-forms forms)))
                        outputs)]
      (when write
        (doseq [{:keys [lang path content]} rendered
                :when (or write-shared
                          (not= :shared lang))]
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

(defn fact-mixed-runtime-issues
  [fact-form]
  (let [[_ title] fact-form]
    (->> (fact-assertion-forms fact-form)
         (keep-indexed (fn [idx [expr _]]
                         (let [langs (runtime-dispatch-langs expr)]
                           (when (> (count langs) 1)
                             {:code :mixed-runtime-assertion
                              :message "Assertion expression dispatches multiple runtimes and cannot be split safely."
                              :title title
                              :assertion idx
                              :langs langs
                              :line (or (not-empty (form-line-info expr))
                                        (form-line-info fact-form))}))))
         vec)))

(defn split-form-diagnostic
  [form idx langs]
  (let [classification (cond-> (classify-split-form form)
                         (and (seq? form) (= 'ns (first form)))
                         (conj :ns))
        dispatch-langs (runtime-dispatch-langs form)
        setup-langs (runtime-setup-langs form)
        split-result (when (fact-form? form)
                       (split-fact-form form langs))
        shared-action (cond
                        (contains? classification :ns) :preserved
                        (contains? classification :script) :omitted
                        (fact-form? form)
                        (let [{:keys [shared runtime?]} split-result]
                          (cond (and shared
                                     (or (not runtime?)
                                         (> (count shared) 2)))
                                :preserved

                                runtime?
                                :omitted

                                :else
                                :preserved))

                        :else
                        :preserved)
        lang-actions (cond
                       (fact-global-form? form)
                       (into {}
                             (map (fn [lang]
                                    [lang (if (or (empty? setup-langs)
                                                  (= [lang] setup-langs))
                                            :copied
                                            :normalized)]))
                             langs)

                       (fact-form? form)
                       (into {}
                             (map (fn [[lang _]]
                                    [lang (if (contains? classification :runtime-prefix)
                                            :retargeted
                                            :split)]))
                             (:langs split-result))

                       (> (count dispatch-langs) 1)
                       {}

                       (= 1 (count dispatch-langs))
                       (let [source-lang (first dispatch-langs)]
                         (into {}
                               (map (fn [lang]
                                      [lang (if (= lang source-lang)
                                              :copied
                                              :retargeted)]))
                               langs))

                       :else
                       (into {}
                             (map (fn [lang] [lang :copied]))
                             langs))
        warnings (vec
                  (concat
                   (when (and (fact-global-form? form)
                              (seq setup-langs))
                     [{:code :normalized-runtime-setup
                       :message "fact:global setup will be normalized per target runtime."
                       :langs setup-langs}])
                   (when (and (contains? classification :runtime-prefix)
                              (seq (:langs split-result)))
                     [{:code :retargeted-runtime-prefix
                       :message "Shared fact prefix contains runtime dispatch and will be retargeted per output language."
                       :langs (->> (keys (:langs split-result))
                                   sort
                                   vec)}])
                   (when (and (not (fact-form? form))
                              (= 1 (count dispatch-langs))
                              (> (count langs) 1))
                     [{:code :retargeted-top-level-runtime
                       :message "Top-level shared form contains runtime dispatch and will be retargeted per output language."
                       :langs dispatch-langs}])))
        unsupported (vec
                     (concat
                      (when (and (contains? classification :top-level-shared)
                                 (> (count dispatch-langs) 1))
                        [{:code :mixed-runtime-top-level
                          :message "Top-level shared form dispatches multiple runtimes and cannot be retargeted safely."
                          :langs dispatch-langs}])
                      (when (fact-form? form)
                        (fact-mixed-runtime-issues form))))]
    {:index idx
     :line (form-line-info form)
     :classification (vec (sort classification))
     :dispatch-langs dispatch-langs
     :setup-langs setup-langs
     :shared-action shared-action
     :lang-actions lang-actions
     :warnings warnings
     :unsupported unsupported}))

(defn diagnose-split-runtime-generation
  [proj input-path test-ns forms langs]
  (let [langs (->> langs
                   (map normalize-runtime-lang)
                   distinct
                   vec)
        script-langs (runtime-script-langs forms)
        script-lang-set (set script-langs)
        expanded (vec (mapcat expand-top-level-form forms))
        form-diagnostics (vec (map-indexed (fn [idx form]
                                             (split-form-diagnostic form idx langs))
                                           expanded))
        {:keys [by-lang]} (separate-runtime-test-forms forms langs)
        output-diagnostics (vec
                            (for [lang langs
                                  :let [out-forms (get by-lang lang)]
                                  :when (seq out-forms)]
                              {:lang lang
                               :path (test-file-path proj (runtime-test-ns test-ns lang))
                               :script-present (contains? script-lang-set lang)
                               :fact-count (count (filter fact-form? out-forms))
                               :status (if (contains? script-lang-set lang)
                                         :expected-pass
                                         :needs-review)
                               :unsupported (vec
                                             (when-not (contains? script-lang-set lang)
                                               [{:code :missing-runtime-script
                                                 :message "Generated output has runtime facts but the source seed does not define l/script- for this runtime."
                                                 :lang lang}]))}))
        unsupported (vec
                     (concat
                      (mapcat :unsupported form-diagnostics)
                      (mapcat :unsupported output-diagnostics)))
        warnings (vec (mapcat :warnings form-diagnostics))]
    {:mode :split
     :input-path input-path
     :source-ns test-ns
     :langs langs
     :script-langs script-langs
     :expected-success? (empty? unsupported)
     :summary {:form-count (count expanded)
               :rewritten-count (count (filter #(or (seq (:warnings %))
                                                    (some #{:normalized :retargeted}
                                                          (vals (:lang-actions %))))
                                              form-diagnostics))
               :unsupported-count (count unsupported)
               :output-count (count output-diagnostics)}
     :forms form-diagnostics
     :outputs output-diagnostics
     :warnings warnings
     :unsupported unsupported}))

(defn scaffold-runtime-template-single
  [proj {:keys [input-path output-path lang from-lang write]
          :or {write false}
          :as params}]
  (let [source-test-ns-param (some-> (:ns params) project/test-ns)
        input-path (or input-path
                       (when source-test-ns-param
                         (test-file-path proj source-test-ns-param))
                       (throw (ex-info "Requires runtime template input-path or a valid test :ns"
                                       {:params (dissoc params :write)})))
        source (slurp input-path)
        forms (read-top-level-forms input-path)
        source-test-ns (or source-test-ns-param
                           (source-test-ns-from-forms forms)
                           (throw (ex-info "Unable to determine runtime template namespace"
                                           {:input-path input-path})))
        from-lang (or from-lang
                      (infer-runtime-lang forms)
                      (throw (ex-info "Cannot infer template runtime language"
                                      {:path input-path
                                       :ns source-test-ns})))
         to-lang (normalize-runtime-lang lang)
         blockers (template-runtime-blockers forms from-lang to-lang)
         _ (when-not (runtime-template-supported? forms to-lang)
             (throw (ex-info "Template cannot be scaffolded safely for the target runtime"
                            {:input-path input-path
                             :source-ns source-test-ns
                             :from-lang (normalize-runtime-lang from-lang)
                              :lang to-lang
                              :blockers blockers})))
         proj (or proj (project/project))
         target-ns (template-runtime-test-ns source-test-ns from-lang to-lang)
         output-path (or output-path
                         (test-file-path proj target-ns))
         previous-content (when (fs/exists? output-path)
                            (slurp output-path))
         content (render-template-runtime-source source
                                                forms
                                                source-test-ns
                                                from-lang
                                                to-lang)]
     (when write
       (fs/create-directory (fs/parent output-path))
       (spit output-path content)
       (format-generated-namespace! proj target-ns output-path))
     (let [written-content (if (fs/exists? output-path)
                             (slurp output-path)
                             content)]
       {:input-path input-path
        :output-path output-path
        :from-lang (normalize-runtime-lang from-lang)
        :lang to-lang
        :source-ns source-test-ns
        :target-ns target-ns
        :updated (not= previous-content written-content)
        :content written-content})))

(defn scaffold-runtime-template-pattern
  [proj {:keys [output-path ns pattern root input-root lang]
         :as params}]
  (when output-path
    (throw (ex-info "Pattern scaffolding does not support a single output-path"
                    {:output-path output-path
                     :pattern (or pattern ns)})))
  (let [pattern (or pattern
                    (when (namespace-pattern? ns) ns))
        sources (->> (xtlang-runtime-suite-sources {:root (or root (:root proj))
                                                    :input-root input-root
                                                    :lang lang})
                     (filter #(namespace-pattern-match? pattern (:ns %)))
                     vec)
        outputs (mapv (fn [{:keys [path from-lang ns]}]
                        (scaffold-runtime-template-single
                         proj
                         (assoc params
                                :ns ns
                                :input-path path
                                :from-lang from-lang
                                :output-path nil)))
                      sources)]
    {:pattern (str pattern)
     :lang (normalize-runtime-lang lang)
     :count (count outputs)
     :outputs outputs}))

(defn scaffold-runtime-template
  "Generates runtime test files from a single-runtime template or namespace pattern."
  ([_ {:keys [input-path output-path lang from-lang write]
         :or {write false}
         :as params}]
   (let [proj (project/project)]
     (if (or (namespace-pattern? (:ns params))
             (namespace-pattern? (:pattern params)))
       (scaffold-runtime-template-pattern proj params)
       (scaffold-runtime-template-single proj params))))
  ([_ params _ _]
   (scaffold-runtime-template nil params)))

(defn diagnose-template-runtime-generation
  [proj input-path source-test-ns forms lang]
  (let [from-lang (single-runtime-template-lang forms)
        requested-lang (some-> lang normalize-runtime-lang)
        requested-blockers (when (and from-lang requested-lang)
                             (template-runtime-blockers forms from-lang requested-lang))
        eligible-langs (->> *runtime-test-langs*
                            (map normalize-runtime-lang)
                            distinct
                            (filter #(runtime-template-supported? forms %))
                            vec)
        twostep-blockers (:twostep requested-blockers)
        runtime-blockers (:runtime-specific requested-blockers)
        unsupported (vec
                     (concat
                      (when-not from-lang
                        [{:code :not-single-runtime-template
                          :message "Template diagnostics require a canonical single-runtime seed."}])
                      (when (runtime-suffixed-test-ns? source-test-ns)
                        [{:code :generated-runtime-namespace
                          :message "Seed namespace already looks like a generated runtime test namespace."
                          :ns source-test-ns}])
                      (when (and requested-lang
                                 (not (runtime-template-supported? forms requested-lang)))
                        [(cond
                           (seq twostep-blockers)
                           {:code :twostep-runtime-blocker
                            :message "Template uses runtime-coupled helpers that are blocked for twostep generation."
                            :blockers twostep-blockers
                            :lang requested-lang}

                           (seq runtime-blockers)
                           {:code :runtime-specific-template-blocker
                            :message "Template uses source-runtime-specific symbols that cannot be ported safely to the requested runtime."
                            :blockers runtime-blockers
                            :from-lang from-lang
                            :lang requested-lang}

                           :else
                           {:code :unsupported-target-runtime
                            :message "Template cannot be scaffolded directly for the requested runtime."
                            :lang requested-lang})])))
        output (when (and requested-lang from-lang)
                 {:lang requested-lang
                  :path (test-file-path proj
                                        (template-runtime-test-ns source-test-ns
                                                                  from-lang
                                                                  requested-lang))
                  :supported? (runtime-template-supported? forms requested-lang)})]
    {:mode :template
     :input-path input-path
     :source-ns source-test-ns
     :from-lang from-lang
     :requested-lang requested-lang
     :eligible-langs eligible-langs
     :expected-success? (empty? unsupported)
     :output output
     :warnings []
     :unsupported unsupported}))

(defn diagnose-runtime-generation
  "Reports whether a runtime seed is safe to generate directly and which forms
   will be rewritten, preserved, or need manual review."
  ([_ {:keys [input-path lang langs]
        :or {langs *runtime-test-langs*}
        :as params}]
   (let [proj (project/project)
         source-test-ns-param (some-> (:ns params) project/test-ns)
         input-path (or input-path
                        (when source-test-ns-param
                          (test-file-path proj source-test-ns-param))
                        (throw (ex-info "Requires runtime generation input-path or a valid test :ns"
                                        {:params params})))
         forms (read-top-level-forms input-path)
         source-test-ns (or source-test-ns-param
                            (source-test-ns-from-forms forms)
                            (throw (ex-info "Unable to determine runtime test namespace"
                                            {:input-path input-path})))
         template? (and (single-runtime-template-lang forms)
                        (not (runtime-suffixed-test-ns? source-test-ns)))]
     (if template?
       (diagnose-template-runtime-generation proj input-path source-test-ns forms lang)
       (diagnose-split-runtime-generation proj input-path source-test-ns forms langs))))
  ([_ params _ _]
   (diagnose-runtime-generation nil params)))

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
                        source-ns (source-test-ns-from-forms forms)
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
