(ns code.migrate.workflow-test
  (:require [clojure.java.io :as io]
            [code.migrate.engine :as engine]
            [code.migrate.workflow :refer :all])
  (:use code.test))

^{:refer code.migrate.workflow/ordered-targets :added "4.1"}
(fact "orders migration targets from least to most dependent"
  (mapv :target/order
        (ordered-targets {:migration/targets
                          [{:target/order 2} {:target/order 1}]}))
  => [1 2])

^{:refer code.migrate.workflow/install-target! :added "4.1"}
(fact "installs verified staged bytes and rejects stale targets"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory
                       "code-migrate-workflow"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        stage-root (io/file root "stage")
        hara-root (io/file root "hara")
        staged (io/file stage-root "lib/src/demo.hal")
        target (io/file hara-root "lib/src/demo.hal")
        _ (io/make-parents staged)
        _ (io/make-parents target)
        _ (spit staged "generated")
        _ (spit target "existing")
        manifest {:target/id :demo
                  :verification/status :passed
                  :manual-fixups 0
                  :stage/root (.getPath stage-root)
                  :hara/root (.getPath hara-root)
                  :source/path (.getPath staged)
                  :test/path nil
                  :target/source-path (.getPath target)
                  :target/test-path nil
                  :source/checksum (engine/sha256 "generated")
                  :test/checksum nil
                  :target/source-checksum (engine/sha256 "existing")
                  :target/test-checksum nil}]
    [(select-keys (install-target! manifest) [:installation/status :audit])
     (slurp target)
     (do (spit target "changed")
         (try
           (install-target! manifest)
           :unexpected
           (catch clojure.lang.ExceptionInfo _ :stale)))])
  => [{:installation/status :installed
       :audit {:source true :test true}}
      "generated"
      :stale])
