(ns code.tool.measure.algo-js
  (:require [std.lib :as h]
            [std.fs :as fs]
            [std.json :as json]
            [code.tool.translate.js-ast :as js-ast]
            [code.tool.measure.common :as common]))

;; JS Specific Configuration
(def ^:dynamic *js-config*
  (merge common/*config*
         {:control-flow-types #{"IfStatement" "ForStatement" "WhileStatement"
                                "DoWhileStatement" "SwitchStatement" "CatchClause"
                                "ConditionalExpression" "TryStatement" "SwitchCase"}
          :ignored-keys #{"loc" "start" "end" "comments" "errors" "extra"
                          "directives" "tokens" "range" "leadingComments"
                          "trailingComments" "innerComments"}}))

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
   (score-ast ast *js-config*))
  ([ast config]
   (walk-ast ast 0 config)))

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

(defn generate-metrics
  "Generates both complexity score and surface area for a given JS code string."
  [code]
  (let [tmp-in (fs/create-tmpfile code)
        tmp-out (fs/create-tmpfile)]
    (try
      ;; Ensure environment is ready (idempotent-ish)
      (js-ast/translate-ast (str tmp-in) (str tmp-out))
      (let [ast (json/read tmp-out)
            complexity (score-ast ast *js-config*)
            base (count-nodes ast *js-config*)
            surface (common/calculate-surface base complexity)]
        {:complexity complexity
         :surface    surface})
      (catch Throwable e
        (h/local :println "Error generating JS AST metrics:" (.getMessage e))
        {:complexity 0 :surface 0})
      (finally
        (fs/delete tmp-in)
        (fs/delete tmp-out)))))
