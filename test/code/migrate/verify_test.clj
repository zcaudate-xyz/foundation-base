(ns code.migrate.verify-test
  (:require [clojure.string :as str]
            [code.migrate.verify :refer :all])
  (:use code.test))

(def +workspace+
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(def +hara-root+
  (str +workspace+ "/technology/hara/core"))

^{:refer code.migrate.verify/verify-source :added "4.1"}
(fact "evaluates generated source in a fresh native Hara process"
  (let [result (verify-source
                "(ns migration.verify-probe)\n(defn value [] 42)\n"
                {:hara (str +hara-root+ "/hara")
                 :project-root +hara-root+})]
    [(:passed result)
     (:exit result)
     (str/includes? (:stdout result)
                    "migration.verify-probe/value")])
  => [true 0 true])
