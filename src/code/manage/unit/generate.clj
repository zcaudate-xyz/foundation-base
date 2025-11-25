(ns code.manage.unit.generate
  (:require [std.lib :as h]
            [std.string :as str]
            [code.project :as project]
            [code.framework :as base]
            [code.manage.unit.scaffold :as scaffold]))

(defn trace-function-calls
  "Traces all function calls in a given namespace while executing a function.
   Returns a map of var symbols to their captured traces."
  [ns f]
  (try
    (h/trace-ns ns)
    (eval f)
    (let [vars (vals (ns-interns ns))]
      (->> vars
           (map (fn [v]
                  (when-let [trace (h/get-trace v)]
                    [(h/var-sym v) @(.-history ^std.lib.trace.Trace trace)])))
           (filter identity)
           (into {})))
    (finally
      (h/untrace-ns ns))))

(defn generate-test-form
  "Creates a fact form from a function symbol and a trace map."
  [fn-sym {:keys [in out]}]
  (let [fn-name (name fn-sym)
        inputs (str/join " " (map pr-str in))
        output (pr-str out)]
    (str "(fact\n  ("
         fn-name " " inputs
         ") => " output ")")))

(defn generate-tests
  "Generates tests for a namespace by running a function and tracing calls."
  [ns f {:keys [write print] :as params} lookup project]
  (let [source-ns (project/source-ns ns)
        test-ns (project/test-ns ns)
        test-file (lookup test-ns)
        traces (trace-function-calls source-ns f)
        test-forms (->> traces
                        (mapcat (fn [[fn-sym trace-maps]]
                                  (map (partial generate-test-form fn-sym) trace-maps)))
                        (str/join "\n\n"))]
    (when (seq test-forms)
      (let [transform-fn (fn [original]
                           (let [scaffolded (scaffold/scaffold-append original source-ns [] " (generated)")]
                             (str scaffolded "\n\n" test-forms)))
            [original test-file]  (if test-file
                                    [(slurp test-file) test-file]
                                    ["" (scaffold/new-filename test-ns project write)])
            params (assoc params :transform transform-fn)
            result (base/transform-code test-ns params (assoc lookup test-ns test-file) project)]
        result))))
