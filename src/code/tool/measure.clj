(ns code.tool.measure
  (:require [std.lib :as h]
            [code.tool.measure.common :as common]
            [code.tool.measure.js :as js]
            [code.tool.measure.clj :as clj]
            [indigo.build.build-ast :as build-ast]))

(defn generate-score
  "Legacy wrapper for generate-metrics (JS), returns complexity score."
  [code]
  (:complexity (js/generate-metrics code)))

(defn generate-metrics
  "Dispatches metric generation based on content/type (defaulting to JS for now)."
  [code]
  (js/generate-metrics code))

(defn detect-type
  [filename]
  (cond
    (re-find #"\.(js|ts|jsx|tsx)$" filename) :js
    (re-find #"\.(clj|cljs|cljc|edn)$" filename) :clj
    :else nil))

(defn filter-supported-files
  [files]
  (filter detect-type files))

(defn analyse-file
  [repo-path sha file-path]
  (let [type (detect-type file-path)
        content (common/get-file-content repo-path sha file-path)]
    (case type
      :js  (js/generate-metrics content)
      :clj (clj/generate-metrics content)
      {:complexity 0 :surface 0})))

(defn analyse-commit
  [repo-path sha]
  (let [files (-> (common/list-files repo-path sha)
                  (filter-supported-files))
        results (map (fn [f]
                       (try
                         (analyse-file repo-path sha f)
                         (catch Throwable _ {:complexity 0 :surface 0})))
                     files)]
    (reduce (fn [acc curr]
              (-> acc
                  (update :complexity + (:complexity curr))
                  (update :surface + (:surface curr))))
            {:complexity 0 :surface 0}
            results)))

(defn measure-history
  "Measures the complexity and surface history of a git repository.

   Options:
   :limit - Max number of commits to analyze (defaults to 50 most recent).
   :sample-rate - Analyze every Nth commit (default 1).

   Returns a vector of maps:
   [{:sha ... :date ... :complexity ... :surface ...} ...]"
  ([repo-path]
   (measure-history repo-path {}))
  ([repo-path {:keys [limit sample-rate] :or {limit 50 sample-rate 1}}]
   (build-ast/initialise) ;; Run npm install once
   (let [commits (->> (common/list-commits repo-path)
                      (take limit)
                      (reverse) ;; Start from oldest
                      (take-nth sample-rate))]
     (mapv (fn [{:keys [sha date]}]
             (let [metrics (analyse-commit repo-path sha)]
               (h/local :println (str "Analyzed " sha " : " metrics))
               (merge {:sha sha :date date} metrics)))
           commits))))
