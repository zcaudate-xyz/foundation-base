(ns code.migrate.test-test
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [code.migrate :as migrate]
            [code.migrate.catalog :as catalog]
            [code.migrate.engine :as engine]
            [code.migrate.test :refer :all])
  (:use code.test))

(def +catalog+
  (migrate/load-catalog))

(def +unit+
  {:unit/kind :test
   :source/path "test/std/lib/zip_test.clj"
   :target/path "lib/test/std/lib/zip_test.hal"
   :source/string (slurp "test/std/lib/zip_test.clj")})

(def +source-rule-ids+
  (set (map :rule/id (catalog/rules-for-pathway +catalog+ :source))))

^{:refer code.migrate.test/emit-test-run :added "4.1"}
(fact "preserves all std.lib.zip operations with stable one-to-one ids"
  (let [target (engine/target-for-unit +unit+ +catalog+)
        emitted (emit-test-run (:source/string +unit+) +catalog+ target)
        correspondence (:operation-correspondence emitted)
        ids (mapv :operation/id correspondence)]
    [(count correspondence)
     (count (distinct ids))
     (count (:setup emitted))
     (count (:cases emitted))
     (:operation/id (first correspondence))
     (some #(= [:zip/element] (get-in % [:meta :class]))
           (:cases emitted))
     (some #(= "element directly left of current position" (:name %))
           (:cases emitted))])
  => [89 89 4 85
      "std.lib.zip/register-type#fact-1/operation-1/assertion-1"
      true true])

^{:refer code.migrate.test/migrate-unit :added "4.1"}
(fact "uses only test-owned rules and regenerates identical bytes"
  (let [first-pass (migrate/migrate-test +unit+ +catalog+)
        second-pass (migrate/migrate-test +unit+ +catalog+)
        applied (set (:applied first-pass))]
    [(:output/checksum first-pass)
     (= (:output first-pass) (:output second-pass))
     (= (:operation-correspondence first-pass)
        (:operation-correspondence second-pass))
     (empty? (set/intersection applied +source-rule-ids+))
     [(:operations first-pass) (:assertions first-pass)]
     (str/includes? (:output first-pass) ":operation/id")])
  => [(:output/checksum (migrate/migrate-test +unit+ +catalog+))
      true true true [89 85] true])

^{:refer code.migrate/analyze-source :added "4.1"}
(fact "exposes separate source and test migration operations"
  (mapv #(boolean (resolve %))
        '[migrate/analyze-source migrate/plan-source migrate/migrate-source
          migrate/analyze-test migrate/plan-test migrate/migrate-test
          migrate/migrate-pair migrate/verify-pair])
  => [true true true true true true true true])