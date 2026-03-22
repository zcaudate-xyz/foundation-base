(ns rt.postgres.grammar.form-defn-hydrate
  (:require [rt.postgres.grammar.typed-parse :as parse]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [std.json :as json]))

(defn- infer-entry-namespace
  [entry]
  (let [{:keys [namespace id]} entry
        fn-name (name id)]
    (when namespace
      (analyze/reset-cache!)
      (let [analysis (-> namespace
                         parse/analyze-namespace
                         parse/register-types!)]
        (when-let [fn-def (some #(when (= fn-name (:name %)) %)
                                (:functions analysis))]
          (analyze/infer-report fn-def))))))

(defn- infer-entry-fallback
  [entry]
  (let [{:keys [form-input namespace]
         :static/keys [schema]} entry]
    (when (and (seq? form-input)
               namespace)
      (-> form-input
          (parse/parse-defn (str namespace) schema)
          analyze/infer-report))))

(defn pg-defn-hydrate-hook
  "Attaches a JSON-friendly infer report to a hydrated postgres function entry."
  {:added "4.1"}
  [entry]
  (if (not= :function (:static/dbtype entry))
    entry
    (let [{:keys [namespace id]} entry
          report (or (when (and namespace id)
                       (infer-entry-namespace entry))
                     (infer-entry-fallback entry))]
      (if report
        (assoc entry
               :rt.postgres/infer report
               :rt.postgres/infer-json (json/write-pp report))
        entry))))
