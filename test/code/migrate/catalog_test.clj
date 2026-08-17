(ns code.migrate.catalog-test
  (:require [code.migrate.catalog :refer :all])
  (:use code.test))

(def +catalog-path+
  "resources/code/migrate/catalog.edn")

^{:refer code.migrate.catalog/validate-catalog :added "4.1"}
(fact "validates rules, nested targets, invariants, and promotion gates"
  (validate-catalog (load-catalog +catalog-path+))
  => []

  (mapv :rule/id
        (rules-for-phase (load-catalog +catalog-path+) :dependency))
  => [:foundation/std-lib-foundation
      :clojure/core-qualified
      :foundation/std-lib-walk
      :foundation/nil-sentinel
      :foundation.test/std-lib-foundation
      :clojure.test/core-qualified
      :foundation.test/std-lib-walk
      :foundation.test/nil-sentinel])

^{:refer code.migrate.catalog/rules-for-pathway :added "4.1"}
(fact "keeps source and test rules in distinct documents"
  (let [catalog (load-catalog +catalog-path+)]
    [(mapv :rule/pathway (:migration/rule-documents catalog))
     (set (map :rule/drift (:migration/rules catalog)))
     (every? #(= :source (:rule/pathway %))
             (rules-for-pathway catalog :source))
     (every? #(= :test (:rule/pathway %))
             (rules-for-pathway catalog :test))])
  => [[:source :test] #{:clojure :foundation} true true])

^{:refer code.migrate.catalog/target-for-pathway :added "4.1"}
(fact "normalizes source and test pathways independently"
  (let [catalog (load-catalog +catalog-path+)
        target (first (:migration/targets catalog))
        source (target-for-pathway target :source)
        test (target-for-pathway target :test)]
    [[(:target/input-path source)
      (:target/output-path source)
      (:target/source-namespace source)]
     [(:target/input-path test)
      (:target/output-path test)
      (:target/test-namespace test)
      (:target/source-namespace test)]])
  => [["src/std/lib/zip.clj" "lib/src/std/lib/zip.hal" 'std.lib.zip]
      ["test/std/lib/zip_test.clj" "lib/test/std/lib/zip_test.hal"
       'std.lib.zip-test 'std.lib.zip]])

^{:refer code.migrate.catalog/validate-catalog
  :id rejects-cross-pathway-target-rules
  :added "4.1"}
(fact "rejects a target which references a rule from the other pathway"
  (let [catalog (load-catalog +catalog-path+)
        invalid (assoc-in catalog
                          [:migration/targets 0 :target/source :rules]
                          [:foundation/code-test-to-native-test])]
    (some #(= :catalog/cross-pathway-rule (:type %))
          (validate-catalog invalid)))
  => true)

^{:refer code.migrate.catalog/target-by-id :added "4.1"}
(fact "locates the first automated port target"
  (:target/source-namespace
   (target-by-id (load-catalog +catalog-path+) :migration/std-lib-zip))
  => 'std.lib.zip)