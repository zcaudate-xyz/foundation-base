(ns code.migrate.zip-probe-test
  (:require [code.migrate :as migrate])
  (:use code.test)
  (:import (java.util Base64)))

(defn encode-output
  [value]
  (.encodeToString (Base64/getEncoder)
                   (.getBytes (str value) "UTF-8")))

^{:refer code.migrate/migrate-pair
  :id std-lib-zip-migration-probe
  :added "4.1"}
(fact "emits the current std.lib.zip migration pair and evidence"
  (let [source-unit {:unit/kind :source
                     :source/path "src/std/lib/zip.clj"
                     :target/path "lib/src/std/lib/zip.hal"
                     :source/string (slurp "src/std/lib/zip.clj")}
        test-unit {:unit/kind :test
                   :source/path "test/std/lib/zip_test.clj"
                   :target/path "lib/test/std/lib/zip_test.hal"
                   :source/string (slurp "test/std/lib/zip_test.clj")}
        pair (migrate/migrate-pair source-unit test-unit)]
    (println "ZIP_MIGRATION_SOURCE_BASE64_BEGIN")
    (println (encode-output (get-in pair [:source :output])))
    (println "ZIP_MIGRATION_SOURCE_BASE64_END")
    (println "ZIP_MIGRATION_TEST_BASE64_BEGIN")
    (println (encode-output (get-in pair [:test :output])))
    (println "ZIP_MIGRATION_TEST_BASE64_END")
    (println "ZIP_MIGRATION_EVIDENCE_BEGIN")
    (prn (update-vals pair #(dissoc % :input :output)))
    (println "ZIP_MIGRATION_EVIDENCE_END")
    [(get-in pair [:source :diagnostics])
     (get-in pair [:test :diagnostics])])
  => [[] []])
