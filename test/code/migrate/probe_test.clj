(ns code.migrate.probe-test
  (:require [clojure.string :as str]
            [code.migrate :as migrate]
            [code.migrate.catalog :as catalog]
            [code.migrate.probe :refer :all])
  (:use code.test))

(def +workspace+
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(def +cases+
  (:cases
   (catalog/read-edn
    (str +workspace+
         "/technology/hara-specs-registry/01-lang/007-code-migration/draft/conformance/bootstrap-pairs.edn"))))

(def +pair+
  (migrate/migrate-pair
   {:unit/kind :source
    :source/string (:case/input (first +cases+))}
   {:unit/kind :test
    :source/string (:case/input (second +cases+))}))

^{:refer code.migrate.probe/probe-program :added "4.1"}
(fact "lowers Foundation facts into a self-contained Hara program"
  (let [probe (probe-program (:output (:source +pair+))
                             (:output (:test +pair+))
                             (:input (:test +pair+)))]
    [(:operations probe)
     (:assertions probe)
     (:diagnostics probe)
     (str/includes? (:program probe) ":migration/tests-passed")])
  => [1 1 [] true])

^{:refer code.migrate.probe/assertion-form :added "4.1"}
(fact "lowers zip matcher expectations without loading native code.test"
  (mapv (fn [expected]
          (-> (assertion-form {:type :test-equal
                               :input {:form '(source-form)}
                               :output {:form expected}}
                              0)
              first))
        ['var? '(throws) '(contains {:right (1)})])
  => '[if let let])

^{:refer code.migrate.probe/migrate-operation-form :added "4.1"}
(fact "migrates compiled fact forms through their owning target"
  (let [migration-catalog (migrate/load-catalog)
        target (catalog/target-by-id migration-catalog :migration/std-lib-zip)]
    (migrate-operation-form '(:context zip) migration-catalog target))
  => '(:context zip))

^{:refer code.migrate.probe/verify-pair :added "4.1"}
(fact "runs the generated pair in a fresh native Hara process"
  (let [root (str +workspace+ "/technology/hara/core")
        result (verify-pair +pair+
                            {:hara (str root "/hara")
                             :project-root root})]
    [(:passed result)
     (get-in result [:verification :exit])
     (get-in result [:verification :stdout])])
  => [true 0 ":migration/tests-passed\n"])
