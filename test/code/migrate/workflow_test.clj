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

^{:refer code.migrate.workflow/namespace-source-path :added "4.1"}
(fact "maps embedded bootstrap namespaces to staged source paths"
  [(namespace-source-path 'std.foundation)
   (namespace-source-path 'std.foundation.string)]
  => ["std/foundation.hal" "std/foundation/string.hal"])

^{:refer code.migrate.workflow/source-unit :added "4.1"}
(fact "supports independently ordered source-only and test-only targets"
  [(nil? (source-unit {:target/test-path "test/demo.clj"}))
   (nil? (test-unit {:target/source-path "src/demo.clj"}))]
  => [true true])

^{:refer code.migrate.workflow/standalone-source-verification? :added "4.1"}
(fact "uses project test loading as the source check for paired migrations"
  [(standalone-source-verification?
    {:source/path "source.hal" :test/path nil})
   (standalone-source-verification?
    {:source/path "source.hal" :test/path "test.hal"})
   (boolean
    (standalone-source-verification?
     {:source/path nil :test/path "test.hal"}))]
  => [true false false])

^{:refer code.migrate.workflow/verification-prefix :added "4.1"}
(fact "grants process access only to targets which declare it"
  [(verification-prefix "hara" "stage" {})
   (verification-prefix "hara" "stage"
                        {:verification/allow-process true})]
  => [["hara" "--project" "stage" "--offline" "--allow-file"]
      ["hara" "--project" "stage" "--offline" "--allow-file"
       "--allow-process"]])

^{:refer code.migrate.workflow/verification-project-root :added "4.1"}
(fact "verifies test-only migrations in the installed dependency project"
  [(verification-project-root
    {:source/path "source.hal" :stage/root "stage" :hara/root "hara"})
   (verification-project-root
    {:source/path nil :stage/root "stage" :hara/root "hara"})]
  => ["stage" "hara"])

^{:refer code.migrate.workflow/verification-source-expression :added "4.1"}
(fact "loads standalone source dependencies in their declared order"
  (verification-source-expression ["a.hal" "b.hal"] "c.hal")
  => "(do (load-file \"a.hal\") (load-file \"b.hal\") (load-file \"c.hal\"))")

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

^{:refer code.migrate.workflow/install-target!
  :id test-only
  :added "4.1"}
(fact "installs a verified test-only migration"
  (let [root (.toFile (java.nio.file.Files/createTempDirectory
                       "code-migrate-test-only"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        stage-root (io/file root "stage")
        hara-root (io/file root "hara")
        staged (io/file stage-root "lib/test/demo_test.hal")
        target (io/file hara-root "lib/test/demo_test.hal")
        _ (io/make-parents staged)
        _ (io/make-parents target)
        _ (spit staged "generated-test")
        manifest {:target/id :test-only
                  :verification/status :passed
                  :manual-fixups 0
                  :stage/root (.getPath stage-root)
                  :hara/root (.getPath hara-root)
                  :source/path nil
                  :test/path (.getPath staged)
                  :target/source-path nil
                  :target/test-path (.getPath target)
                  :source/checksum nil
                  :test/checksum (engine/sha256 "generated-test")
                  :target/source-checksum nil
                  :target/test-checksum nil
                  :test-data []}
        installed (install-target! manifest)]
    [(:audit installed) (slurp target)])
  => [{:source true :test true} "generated-test"])
