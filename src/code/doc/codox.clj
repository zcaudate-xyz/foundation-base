(ns code.doc.codox
  "runner for codox API documentation with a clean process exit
   
   the `lein codox` plugin task leaves the JVM hanging on non-daemon
   threads after generation; this runner exits explicitly"
  {:added "4.1"}
  (:require [codox.main :as codox]))

(defn project-options
  "reads the `:codox` options from project.clj, adding project metadata"
  {:added "4.1"}
  ([]
   (let [[_ pname version & kvs] (read-string (slurp "project.clj"))
         project-map (apply hash-map kvs)]
     (assoc (:codox project-map)
            :name (str pname)
            :version version
            :description (:description project-map)))))

(defn -main
  "generates codox documentation, exiting cleanly on completion"
  {:added "4.1"}
  ([]
   (codox/generate-docs (project-options))
   (shutdown-agents)
   (System/exit 0)))
