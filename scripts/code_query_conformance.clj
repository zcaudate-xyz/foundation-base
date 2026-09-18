(ns code-query-conformance
  (:require [clojure.edn :as edn]))

(defn- function-symbol
  "Returns the qualified symbol for a function value if it matches a public
   var in any loaded namespace, otherwise returns the value unchanged."
  [value]
  (or (some (fn [ns]
              (some (fn [[sym var]]
                      (if (= value (var-get var))
                        (symbol (str ns) (str sym))))
                    (ns-publics ns)))
            (all-ns))
      value))

(defn- normalize-result
  "Walks a result value and replaces function objects with their qualified
   symbols so the output is readable EDN and comparable across runtimes."
  [value]
  (cond
    (fn? value)
    (function-symbol value)

    (map? value)
    (into {} (map (fn [[k v]] [k (normalize-result v)]) value))

    (vector? value)
    (mapv #(normalize-result %) value)

    (seq? value)
    (doall (map #(normalize-result %) value))

    :else value))

(defn evaluate-fixture
  [fixture]
  (let [ns-sym (:ns fixture)
        expr-str (:expr fixture)]
    (require ns-sym)
    (binding [*ns* (find-ns ns-sym)]
      (normalize-result (eval (read-string expr-str))))))

(defn run
  [fixture-path]
  (let [fixtures (edn/read-string (slurp fixture-path))
        results (mapv (fn [fixture]
                        (try
                          {:id (:id fixture)
                           :actual (evaluate-fixture fixture)}
                          (catch Throwable error
                            {:id (:id fixture)
                             :error (str error)})))
                      fixtures)]
    (prn (into {} (map (fn [result]
                         [(:id result)
                          (dissoc result :id)]))
               results))))

(let [args *command-line-args*]
  (if (empty? args)
    (do (println "Usage: lein run -m clojure.main/main scripts/code_query_conformance.clj <fixture-path>")
        (println "Received:" args)
        (System/exit 1))
    (run (first args))))
