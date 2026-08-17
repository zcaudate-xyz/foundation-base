(ns code.migrate.zip-probe-test
  (:require [code.migrate :as migrate])
  (:use code.test)
  (:import (java.util Base64)))

(defn encode-output
  [value]
  (.encodeToString (Base64/getEncoder)
                   (.getBytes (str value) "UTF-8")))

(defn emit-base64
  [label value]
  (println (str label "_BASE64_BEGIN"))
  (doseq [chunk (partition-all 4000 (encode-output value))]
    (println (apply str chunk)))
  (println (str label "_BASE64_END")))

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
    (emit-base64 "ZIP_MIGRATION_SOURCE" (get-in pair [:source :output]))
    (emit-base64 "ZIP_MIGRATION_TEST" (get-in pair [:test :output]))
    (emit-base64 "MIGRATION_ENGINE" (slurp "src/code/migrate/engine.clj"))
    (emit-base64 "MIGRATION_ENGINE_TEST" (slurp "test/code/migrate/engine_test.clj"))
    (emit-base64 "MIGRATION_SOURCE_RULES" (slurp "resources/code/migrate/rules/source.edn"))
    (emit-base64 "MIGRATION_TEST_ENGINE" (slurp "src/code/migrate/test.clj"))
    (emit-base64 "MIGRATION_STAGE" (slurp "src/code/migrate/stage.clj"))
    (emit-base64 "MIGRATION_PROBE" (slurp "src/code/migrate/probe.clj"))
    (emit-base64 "MIGRATION_VERIFY" (slurp "src/code/migrate/verify.clj"))
    (println "ZIP_MIGRATION_EVIDENCE_BEGIN")
    (prn (update-vals pair #(dissoc % :input :output)))
    (println "ZIP_MIGRATION_EVIDENCE_END")
    [(get-in pair [:source :diagnostics])
     (get-in pair [:test :diagnostics])])
  => [[] []])
