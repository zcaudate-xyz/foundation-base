(ns std-lib-zip-conformance
  (:require [clojure.edn :as edn]))

(defn evaluate-fixture
  [fixture]
  (let [ns-sym (:ns fixture)
        expr-str (:expr fixture)]
    (require ns-sym)
    (binding [*ns* (find-ns ns-sym)]
      (eval (read-string expr-str)))))

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
    (do (println "Usage: lein run -m clojure.main/main scripts/std_lib_zip_conformance.clj <fixture-path>")
        (println "Received:" args)
        (System/exit 1))
    (run (first args))))
