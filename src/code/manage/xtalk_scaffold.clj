(ns code.manage.xtalk-scaffold
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [code.manage.xtalk-ops :as xtalk-ops]
            [code.project :as project]
            [std.fs :as fs]))

(def ^:dynamic *grammar-test-path*
  "test/std/lang/base/grammar_xtalk_ops_test.clj")

(def ^:dynamic *grammar-test-ns*
  'std.lang.base.grammar-xtalk-ops-test)

(def ^:dynamic *runtime-test-langs*
  [:js :lua :python :r])

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
  {"!.js" :js
   "!.lua" :lua
   "!.py" :python
   "!.R" :r})

(defn read-top-level-forms
  [path]
  (read-string (str "[" (slurp path) "]")))

(defn runtime-expr-lang
  [expr]
  (when (seq? expr)
    (get +runtime-dispatch-map+ (str (first expr)))))

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
    (symbol (str base-ns "-" (name lang) "-test"))))

(defn render-top-level-forms
  [forms]
  (binding [pprint/*print-right-margin* 100
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

(defn test-file-path
  [project test-ns]
  (or (project/get-path test-ns project)
      (str (fs/path (:root project)
                    (or (first (:test-paths project))
                        "test")
                    (str (fs/ns->file test-ns) ".clj")))))

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
                     (conj prefix expr)
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
        fact-global (some fact-global-form? expanded)
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
  ([_ {:keys [langs write]
       :or {langs *runtime-test-langs*
            write false}
       :as params}]
   (let [proj (project/project)
         test-ns (project/test-ns (:ns params))
         input-path (test-file-path proj test-ns)
         forms (read-top-level-forms input-path)
         {:keys [shared by-lang]} (separate-runtime-test-forms forms langs)
         shared-path input-path
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
