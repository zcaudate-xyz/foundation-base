(ns code.manage.xtalk-scaffold
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [code.manage.xtalk-ops :as xtalk-ops]
            [code.project :as project]
            [std.fs :as fs]))

(def ^:dynamic *grammar-test-path*
  "test/std/lang/base/grammar_xtalk_ops_test.clj")

(def ^:dynamic *grammar-test-ns*
  'std.lang.base.grammar-xtalk-ops-test)

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
