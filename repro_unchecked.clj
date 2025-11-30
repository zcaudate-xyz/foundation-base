(ns repro-unchecked
  (:require [code.framework :as base]
            [code.project :as project]
            [code.manage.unit :as unit]
            [std.lib :as h]))

(defn check-scheduler []
  (let [ns 'std.scheduler
        params {}
        project (project/project)
        lookup (project/file-lookup project)]
    (if (not (base/no-test ns params lookup project))
      (let [source-ns (project/source-ns ns)
            test-ns   (project/test-ns ns)
            analysis  (base/analyse test-ns params lookup project)
            entries   (get analysis source-ns)]

        (doseq [[var entry] entries]
          (let [sexp (get-in entry [:test :sexp])
                has-arrow-top (->> sexp (filter '#{=>}) empty? not)
                has-arrow-nested (->> sexp flatten (filter '#{=>}) empty? not)]
            (when (or (= var 'runner:stopped?) (= var 'runner))
              (println "Var:" var)
              (println "Top level =>:" has-arrow-top)
              (println "Nested =>:" has-arrow-nested)
              (println "Sexp:" sexp)
              (println "--------------------------------------------------"))))))))

(check-scheduler)
