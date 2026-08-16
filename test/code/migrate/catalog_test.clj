(ns code.migrate.catalog-test
  (:require [code.migrate.catalog :refer :all])
  (:use code.test))

(def +catalog-path+
  "resources/code/migrate/catalog.edn")

^{:refer code.migrate.catalog/validate-catalog :added "4.1"}
(fact "validates the executable migration catalog"
  (validate-catalog (load-catalog +catalog-path+))
  => []

  (mapv :rule/id
        (rules-for-phase (load-catalog +catalog-path+) :dependency))
  => [:foundation/std-lib-foundation
      :clojure/core-qualified
      :foundation/std-lib-walk
      :foundation/nil-sentinel])

^{:refer code.migrate.catalog/rules-for-pathway :added "4.1"}
(fact "keeps source and test rules in distinct documents"
  (let [catalog (load-catalog +catalog-path+)]
    [(mapv :rule/pathway (:migration/rule-documents catalog))
     (set (map :rule/drift (:migration/rules catalog)))
     (every? #(= :source (:rule/pathway %))
             (rules-for-pathway catalog :source))])
  => [[:source :test] #{:clojure :foundation} true])

^{:refer code.migrate.catalog/target-by-id :added "4.1"}
(fact "locates the first automated port target"
  (:target/source-namespace
   (target-by-id (load-catalog +catalog-path+) :migration/std-lib-zip))
  => 'std.lib.zip)
