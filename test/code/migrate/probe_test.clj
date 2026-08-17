(ns code.migrate.probe-test
  (:require [clojure.string :as str]
            [code.migrate :as migrate]
            [code.migrate.probe :refer :all])
  (:use code.test))

(def +workspace+
  (or (System/getenv "HARA_WORKSPACE_ROOT")
      "../../workspace"))

(def +pair+
  (migrate/migrate-pair
   {:unit/kind :source
    :source/path "src/std/lib/zip.clj"
    :target/path "lib/src/std/lib/zip.hal"
    :source/string (slurp "src/std/lib/zip.clj")}
   {:unit/kind :test
    :source/path "test/std/lib/zip_test.clj"
    :target/path "lib/test/std/lib/zip_test.hal"
    :source/string (slurp "test/std/lib/zip_test.clj")}))

^{:refer code.migrate.probe/probe-program :added "4.1"}
(fact "lowers only the emitted Hara test artifact"
  (let [probe (probe-program (:output (:source +pair+))
                             (:output (:test +pair+)))]
    [(:operations probe)
     (:assertions probe)
     (:diagnostics probe)
     (str/includes? (:program probe) ":migration/tests-passed")
     (str/includes? (:program probe)
                    "std.lib.zip/register-type#fact-1/operation-1/assertion-1")])
  => [89 85 [] true true])

^{:refer code.migrate.probe/emitted-test-plan :added "4.1"}
(fact "reads setup and cases from generated Test/run bytes"
  (let [plan (emitted-test-plan (:output (:test +pair+)))]
    [(count (:setup plan))
     (count (:cases plan))
     (:checker plan)
     (:diagnostics plan)])
  => [4 85 'process/check []])

^{:refer code.migrate.probe/emitted-assertion-form :added "4.1"}
(fact "lowers emitted equality, matcher, and predicate expectations"
  (mapv first
        [(emitted-assertion-form
          {:operation/id "equal" :test '(fn [] (+ 1 1)) :expected 2}
          0)
         (emitted-assertion-form
          {:operation/id "throws" :test '(fn [] (throw (ex-info "x" {})))
           :expected '(checker/throws)}
          1)
         (emitted-assertion-form
          {:operation/id "predicate" :test '(fn [] value)
           :expected '(fn [actual] (= :std.native.Var (type actual)))}
          2)])
  => '[let let let])

^{:refer code.migrate.probe/verify-pair :added "4.1"}
(fact "runs lowered and native emitted tests in separate Hara processes"
  (let [root (str +workspace+ "/technology/hara/core")
        result (verify-pair +pair+
                            {:hara (str root "/hara")
                             :project-root root})]
    [(:passed result)
     (get-in result [:lowered :verification :exit])
     (get-in result [:native :verification :exit])
     (get-in result [:lowered :verification :stdout])
     (get-in result [:native :verification :stdout])])
  => [true 0 0
      ":migration/tests-passed\n"
      ":migration/tests-passed\n"])