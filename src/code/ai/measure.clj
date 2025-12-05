(ns code.ai.measure
  (:require [std.lib :as h]
            [std.string :as str]
            [std.fs :as fs]
            [std.json :as json]
            [indigo.build.build-ast :as build-ast]))

;; Metric Configuration
(def ^:dynamic *config*
  {:base-score 1
   :depth-factor 0.1
   :control-flow-bonus 2
   :control-flow-types #{"IfStatement" "ForStatement" "WhileStatement"
                         "DoWhileStatement" "SwitchStatement" "CatchClause"
                         "ConditionalExpression" "TryStatement" "SwitchCase"}
   :ignored-keys #{"loc" "start" "end" "comments" "errors" "extra"
                   "directives" "tokens" "range" "leadingComments"
                   "trailingComments" "innerComments"}})

(defn- score-node
  [node depth config]
  (let [{:keys [base-score depth-factor control-flow-bonus control-flow-types]} config
        type (get node "type")]
    (if type
      (+ base-score
         (* depth depth-factor)
         (if (contains? control-flow-types type) control-flow-bonus 0))
      0)))

(defn- walk-ast
  [ast depth config]
  (cond
    (map? ast)
    (let [node-score (score-node ast depth config)
          children-score (reduce-kv (fn [acc k v]
                                      (if (contains? (:ignored-keys config) k)
                                        acc
                                        (+ acc (walk-ast v (if (get ast "type") (inc depth) depth) config))))
                                    0
                                    ast)]
      (+ node-score children-score))

    (sequential? ast)
    (reduce (fn [acc v] (+ acc (walk-ast v depth config))) 0 ast)

    :else 0))

(defn score-ast
  "Calculates the structural complexity score of an AST map."
  ([ast]
   (score-ast ast *config*))
  ([ast config]
   (walk-ast ast 0 config)))

;; Surface Area Metric (Code City)

(defn- count-nodes
  "Counts the total number of AST nodes (Base Area proxy)."
  [ast config]
  (cond
    (map? ast)
    (let [self-count (if (get ast "type") 1 0)
          children-count (reduce-kv (fn [acc k v]
                                      (if (contains? (:ignored-keys config) k)
                                        acc
                                        (+ acc (count-nodes v config))))
                                    0
                                    ast)]
      (+ self-count children-count))

    (sequential? ast)
    (reduce (fn [acc v] (+ acc (count-nodes v config))) 0 ast)

    :else 0))

(defn calculate-surface
  "Calculates the 'surface area' of the code using a Code City metaphor.
   Base Area (Width) = Total Node Count (Size)
   Height = Structural Complexity Score
   Surface = Base + 4 * (sqrt(Base) * Height)
   (Represents top surface + 4 side walls of a square tower)"
  ([ast]
   (calculate-surface ast *config*))
  ([ast config]
   (let [base (count-nodes ast config)
         height (score-ast ast config)]
     (if (zero? base)
       0.0
       (+ base (* 4 (Math/sqrt base) height))))))

(defn generate-metrics
  "Generates both complexity score and surface area for a given code string."
  [code]
  (let [tmp-in (fs/create-tmpfile code)
        tmp-out (fs/create-tmpfile)]
    (try
      ;; Ensure environment is ready (idempotent-ish)
      (build-ast/generate-ast (str tmp-in) (str tmp-out))
      (let [ast (json/read tmp-out)]
        {:complexity (score-ast ast)
         :surface    (calculate-surface ast)})
      (catch Throwable e
        (h/local :println "Error generating AST metrics:" (.getMessage e))
        {:complexity 0 :surface 0})
      (finally
        (fs/delete tmp-in)
        (fs/delete tmp-out)))))

(defn generate-score
  "Legacy wrapper for generate-metrics, returns complexity score."
  [code]
  (:complexity (generate-metrics code)))

;; Git Interop

(defn sh-git
  [args repo-path]
  (let [res (h/sh {:args (into ["git"] args)
                   :root (str repo-path)})]
    (if (zero? (:exit res))
      (str/trim (:out res))
      (if (nil? (:exit res))
        (throw (ex-info "Git command failed to run (exit code nil)" {:args args :res res}))
        (throw (ex-info "Git command failed" {:args args :res res}))))))

(defn list-commits
  [repo-path]
  (let [out (sh-git ["log" "--pretty=format:%H|%ad" "--date=iso"] repo-path)]
    (->> (str/split-lines out)
         (map (fn [line]
                (let [[sha date] (str/split line #"\|")]
                  {:sha sha :date date}))))))

(defn list-files
  [repo-path sha]
  (let [out (sh-git ["ls-tree" "-r" "--name-only" sha] repo-path)]
    (str/split-lines out)))

(defn get-file-content
  [repo-path sha file-path]
  (sh-git ["show" (str sha ":" file-path)] repo-path))

(defn filter-js-files
  [files]
  (filter #(re-find #"\.(js|ts|jsx|tsx)$" %) files))

(defn analyse-commit
  [repo-path sha]
  (let [files (-> (list-files repo-path sha)
                  (filter-js-files))
        results (map (fn [f]
                       (try
                         (let [content (get-file-content repo-path sha f)]
                           (generate-metrics content))
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
   (let [commits (->> (list-commits repo-path)
                      (take limit)
                      (reverse) ;; Start from oldest
                      (take-nth sample-rate))]
     (mapv (fn [{:keys [sha date]}]
             (let [metrics (analyse-commit repo-path sha)]
               (h/local :println (str "Analyzed " sha " : " metrics))
               (merge {:sha sha :date date} metrics)))
           commits))))
