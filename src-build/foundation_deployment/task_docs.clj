(ns foundation-deployment.task-docs
  (:require [code.doc :as doc]
            [code.doc.check :as doc-check]))

(def ^:dynamic *exit*
  (fn [status]
    (System/exit status)))

(def ^:dynamic *codox-run*
  (fn []
    (require 'code.doc.codox)
    (let [options (deref (resolve 'code.doc.codox/project-options))
          generate (deref (resolve 'codox.main/generate-docs))]
      (generate (options)))))

(def +sites+ [:core :code :xt :std :hara])

^{:public true}
(defn codox-build
  "Generates the Codox API reference when the Codox profile is available."
  ([] (codox-build {}))
  ([{:keys [enabled?] :or {enabled? true}}]
   (if-not enabled?
     {:status :skipped}
     (try
       {:status :built
        :result (*codox-run*)}
       (catch java.io.FileNotFoundException error
         {:status :unavailable
          :message (.getMessage error)})
       (catch ClassNotFoundException error
         {:status :unavailable
          :message (.getMessage error)})))))

^{:public true}
(defn build
  "Builds the published documentation sites and Codox API reference."
  ([] (build {}))
  ([opts]
   (let [sites   (mapv keyword (or (:sites opts) +sites+))
         write?  (if (contains? opts :write) (:write opts) true)
         codox?  (if (contains? opts :codox) (:codox opts) true)
         results (mapv (fn [site]
                         {:site site
                          :template (doc/deploy-template site {:write write?})
                          :publish (doc/publish [(symbol (name site))]
                                                {:write write?
                                                 :parallel false})})
                       sites)]
     {:sites sites
      :results results
      :codox (when codox? (codox-build opts))})))

^{:public true}
(defn check
  "Checks published documentation pages and returns a shell-friendly result."
  ([] (check :all {}))
  ([input] (check input {}))
  ([input opts]
   (let [failures (doc-check/check-failures input (or (:params opts) {}))]
     {:input input
      :failures failures
      :ok? (zero? failures)})))

^{:public true}
(defn task-run
  "Dispatches a documentation deployment task."
  [task & args]
  (case task
    "build" (apply build args)
    "check" (apply check args)
    (throw (ex-info "Unknown documentation task"
                    {:task task
                     :tasks ["build" "check"]}))))

^{:public true}
(defn -main
  "Runs a documentation task and exits with a shell status."
  [& [task & args]]
  (try
    (let [result (apply task-run task args)
          failed? (and (= task "check") (not (:ok? result)))]
      (*exit* (if failed? 1 0)))
    (catch Throwable _
      (*exit* 1))))
