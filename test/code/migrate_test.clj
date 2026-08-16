(ns code.migrate-test
  (:require [clojure.string :as str]
            [code.migrate :refer :all]
            [code.migrate.catalog :as catalog])
  (:use code.test))

(def +cases+
  (:cases
   (catalog/read-edn
    "resources/code/migrate/conformance/bootstrap-pairs.edn")))

(def +source-unit+
  {:unit/kind :source
   :source/string (:case/input (first +cases+))})

(def +test-unit+
  {:unit/kind :test
   :source/string (:case/input (second +cases+))})

^{:refer code.migrate/migrate-pair :added "4.1"}
(fact "migrates source and test as one required pair"
  (let [pair (migrate-pair +source-unit+ +test-unit+)]
    [(set (keys pair))
     (mapv :diagnostics (vals pair))
     (str/includes? (get-in pair [:test :output]) "(Test/run")
     (str/includes? (get-in pair [:test :output]) "process/check")])
  => [#{:source :test} [[] []] true true])

^{:refer code.migrate/migrate-pair
  :id rejects-invalid-pair
  :added "4.1"}
(fact "rejects reversed or incomplete unit pairs"
  (migrate-pair +test-unit+ +source-unit+)
  => (throws clojure.lang.ExceptionInfo))
