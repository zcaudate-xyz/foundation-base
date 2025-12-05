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

(defn generate-score
  "Generates a score for a given code string (JS/TS)."
  [code]
  (let [tmp-in (fs/create-tmpfile code)
        tmp-out (fs/create-tmpfile)]
    (try
      ;; Ensure environment is ready (idempotent-ish)
      ;; Note: In a long loop, we might want to move initialise out.
      (build-ast/generate-ast (str tmp-in) (str tmp-out))
      (let [ast (json/read tmp-out)]
        (score-ast ast))
      (catch Throwable e
        (h/local :println "Error generating AST score:" (.getMessage e))
        0)
      (finally
        (fs/delete tmp-in)
        (fs/delete tmp-out)))))

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
        scores (map (fn [f]
                      (try
                        (let [content (get-file-content repo-path sha f)]
                          (generate-score content))
                        (catch Throwable _ 0)))
                    files)]
    (reduce + scores)))

(defn measure-history
  "Measures the complexity history of a git repository.

   Options:
   :limit - Max number of commits to analyze (defaults to 50 most recent).
   :sample-rate - Analyze every Nth commit (default 1).

   Note: This function starts from the most recent commit and works backwards,
   then reverses the result to show chronological growth."
  ([repo-path]
   (measure-history repo-path {}))
  ([repo-path {:keys [limit sample-rate] :or {limit 50 sample-rate 1}}]
   (build-ast/initialise) ;; Run npm install once
   (let [commits (->> (list-commits repo-path)
                      (take limit)
                      (reverse) ;; Start from oldest
                      (take-nth sample-rate))]
     (mapv (fn [{:keys [sha date]}]
             (let [score (analyse-commit repo-path sha)]
               (h/local :println (str "Analyzed " sha " : " score))
               {:sha sha :date date :score score}))
           commits))))
