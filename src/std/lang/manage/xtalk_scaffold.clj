(ns std.lang.manage.xtalk-scaffold
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [std.lang.manage.xtalk-ops :as xtalk-ops]
            [code.project :as project]
            [std.fs :as fs]))

(def ^:dynamic *grammar-test-path*
  "test/std/lang/base/grammar_xtalk_ops_test.clj")

(def ^:dynamic *grammar-test-ns*
  'std.lang.base.grammar-xtalk-ops-test)

(def ^:dynamic *runtime-test-langs*
  [:js :lua :python :r])

(def +runtime-lang-config+
  {:js     {:script :js
            :dispatch '!.js
            :suffix "js"}
   :lua    {:script :lua
            :dispatch '!.lua
            :suffix "lua"}
   :python {:script :python
            :dispatch '!.py
            :suffix "python"}
   :r      {:script :r
            :dispatch '!.R
            :suffix "r"}
   :rb     {:script :ruby
            :dispatch '!.rb
            :suffix "rb"}
   :dart   {:script :dart
            :dispatch '!.dt
            :suffix "dt"}
   :php    {:script :php
            :dispatch '!.php
            :suffix "php"}
   :go    {:script :go
            :dispatch '!.go
            :suffix "go"}})

(def +runtime-lang-aliases+
  {:ruby :rb})

(def +runtime-executable-langs+
  #{:js :lua :python :r :rb})

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
      :content content})))

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

(defn runtime-lang-suffix
  [lang]
  (:suffix (runtime-lang-config lang)))

(defn read-top-level-forms
  [path]
  (read-string (str "[" (slurp path) "]")))

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
  (let [base-ns (project/source-ns test-ns)]
    (symbol (str base-ns "-" (runtime-lang-suffix lang) "-test"))))

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
      (str (fs/path (:root project)
                    (or (first (:test-paths project))
                        "test")
                    (str (fs/ns->file test-ns) ".clj")))))

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

(defn template-runtime-test-ns
  [source-test-ns from-lang to-lang]
  (let [from-suffix (runtime-lang-suffix from-lang)
        to-suffix (runtime-lang-suffix to-lang)
        source-name (str source-test-ns)
        pattern (re-pattern (str "-" (java.util.regex.Pattern/quote from-suffix) "-test$"))]
    (if (re-find pattern source-name)
      (symbol (str/replace source-name pattern (str "-" to-suffix "-test")))
      (runtime-test-ns source-test-ns to-lang))))

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
                      (replace-runtime-symbol from-dispatch to-dispatch)
                      (replace-string-value source-ns-str target-ns-str))))
          forms)))

(defn split-fact-form
  [fact-form langs]
  (let [[fact-sym title & body] fact-form]
    (if (empty? body)
      {:shared fact-form
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
      (let [{:keys [shared langs]} (split-fact-form fact-form langs)]
        (when shared
          (swap! shared-facts conj shared))
        (doseq [[lang split-form] langs]
          (swap! by-lang update lang conj split-form))))
    {:shared (vec (concat
                   [(replace-ns-name ns-form (second ns-form))]
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
                    outputs)})))

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
      :content content})))
