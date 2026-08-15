(ns code.migrate.stage-test
  (:require [code.migrate.stage :refer :all])
  (:use code.test))

(defn temp-root
  []
  (.toFile
   (java.nio.file.Files/createTempDirectory
    "code-migrate-stage"
    (make-array java.nio.file.attribute.FileAttribute 0))))

^{:refer code.migrate.stage/stage-pair :added "4.1"}
(fact "writes a generated pair with an evidence manifest"
  (let [root (temp-root)
        pair {:source {:output "(ns demo)\n"
                       :output/checksum "source"}
              :test {:output "(ns demo-test)\n"
                     :output/checksum "test"}}
        manifest (stage-pair pair
                             {:stage-root root
                              :source-path "src/demo.hal"
                              :test-path "test/demo_test.hal"
                              :project-content "{:hara/type :project}\n"})]
    [(slurp (:source/path manifest))
     (select-keys manifest
                  [:source/checksum :test/checksum :manual-fixups])])
  => ["(ns demo)\n"
      {:source/checksum "source"
       :test/checksum "test"
       :manual-fixups 0}])

^{:refer code.migrate.stage/resolve-stage-path :added "4.1"}
(fact "rejects paths which escape the staging root"
  (resolve-stage-path (temp-root) "../escape.hal")
  => (throws clojure.lang.ExceptionInfo))
