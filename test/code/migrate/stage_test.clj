(ns code.migrate.stage-test
  (:require [clojure.edn :as edn]
            [code.migrate.stage :refer :all])
  (:use code.test))

(defn temp-root
  []
  (.toFile
   (java.nio.file.Files/createTempDirectory
    "code-migrate-stage"
    (make-array java.nio.file.attribute.FileAttribute 0))))

^{:refer code.migrate.stage/stage-pair :added "4.1"}
(fact "writes a generated pair with complete manifest evidence"
  (let [root (temp-root)
        correspondence [{:operation/id "demo#fact-1/operation-1/assertion-1"
                         :source/order 1
                         :emitted/order 1}]
        pair {:source {:source/path "src/demo.clj"
                       :target/path "lib/src/demo.hal"
                       :output "(ns demo)\n"
                       :source/checksum "source-input"
                       :output/checksum "source-output"
                       :applied [:source/rule]
                       :diagnostics []}
              :test {:source/path "test/demo_test.clj"
                     :target/path "lib/test/demo_test.hal"
                     :output "(ns demo-test)\n"
                     :source/checksum "test-input"
                     :output/checksum "test-output"
                     :applied [:test/rule]
                     :diagnostics []
                     :operations 1
                     :assertions 1
                     :operation-correspondence correspondence}}
        manifest (stage-pair pair
                             {:stage-root root
                              :source-path "src/demo.hal"
                              :test-path "test/demo_test.hal"
                              :project-content "{:hara/type :project}\n"})
        stored (edn/read-string (slurp (:manifest/path manifest)))]
    [(slurp (get-in manifest [:source :staged/path]))
     (.getName (java.io.File. (:manifest/path manifest)))
     (select-keys stored [:manifest/type :manifest/version :manual-fixups])
     (get-in stored [:source :input/checksum])
     (get-in stored [:test :operation-correspondence])])
  => ["(ns demo)\n"
      "manifest.edn"
      {:manifest/type :code-migration
       :manifest/version 1
       :manual-fixups 0}
      "source-input"
      [{:operation/id "demo#fact-1/operation-1/assertion-1"
        :source/order 1
        :emitted/order 1}]])

^{:refer code.migrate.stage/resolve-stage-path :added "4.1"}
(fact "rejects paths which escape the staging root"
  (resolve-stage-path (temp-root) "../escape.hal")
  => (throws clojure.lang.ExceptionInfo))