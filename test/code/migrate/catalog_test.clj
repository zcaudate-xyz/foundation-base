(ns code.migrate.catalog-test
  (:require [code.migrate.catalog :refer :all])
  (:use code.test))

(def +catalog-path+
  (str (or (System/getenv "HARA_WORKSPACE_ROOT")
           "../../workspace")
       "/technology/hara-specs-registry/01-lang/007-code-migration/draft/code-migration.edn"))

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

^{:refer code.migrate.catalog/target-by-id :added "4.1"}
(fact "locates the first automated port target"
  (:target/source-namespace
   (target-by-id (load-catalog +catalog-path+) :migration/std-lib-zip))
  => 'std.lib.zip)
