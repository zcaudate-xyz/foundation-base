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
      :clojure/string-foundation
      :clojure/set-foundation
      :foundation/env-print-native
      :foundation/env-pr-native
      :clojure/integer-max-native
      :clojure/volatile-atom-native
      :clojure/vreset-reset-native
      :foundation/std-lib-collection
      :foundation/string-common-native
      :foundation/string-trim-right-native
      :foundation/protocol-match-imatch
      :foundation/hash-map-predicate
      :foundation/form-predicate
      :foundation/zipper-class
      :foundation/indexing-reader-native
      :clojure/contains-predicate
      :foundation/nil-sentinel
      :clojure/walk-foundation
      :clojure/java-io-stream
      :clojure/edn-native
      :clojure/pprint-pretty
      :clojure/numeric-equality-native
      :foundation/env-with-out-string-native
      :foundation/env-foundation-native
      :foundation/prose-string-native])

^{:refer code.migrate.catalog/rules-for-pathway :added "4.1"}
(fact "keeps source and test rules in distinct documents"
  (let [catalog (load-catalog +catalog-path+)]
    [(mapv :rule/pathway (:migration/rule-documents catalog))
     (set (map :rule/drift (:migration/rules catalog)))
     (every? #(= :source (:rule/pathway %))
             (rules-for-pathway catalog :source))])
  => [[:source :test] #{:clojure :foundation} true])

^{:refer code.migrate.catalog/target-by-id :added "4.1"}
(fact "orders migration targets from least to most dependent"
  (let [catalog (load-catalog +catalog-path+)
        targets (sort-by :target/order (:migration/targets catalog))]
    [(mapv :target/order targets)
     (mapv #(or (:target/target-source-namespace %)
                (:target/source-namespace %))
           targets)])
  => [[1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35]
      '[std.lib.zip
        std.block.check
        std.block.protocol
        std.block.base
        std.block.reader
        std.block.value
        std.block.type
        std.block.construct
        std.block.parse
        std.block.navigate
        std.block.heal.parse
        std.block.heal.print
        std.block.heal.edit
        std.block.heal.indent
        std.block.heal.core
        std.block.heal
        std.block.layout.estimate
        std.block.layout.common
        std.block.layout
        std.block.layout.common
        std.block.layout
        std.block.template
        code.query.common
        code.query.match.impl
        code.query.match.optional
        code.query.match.pattern
        code.query.compile
        work.flow.make.makefile
        work.flow.make.common
        work.flow.make.artifact
        work.flow.make.common
        work.flow.make.readme
        work.flow.make.project
        work.flow.make.github
        work.flow.make.bulk]])

^{:refer code.migrate.catalog/target-by-id
  :added "4.1"
  :id zipper-extension-fields}
(fact "retains zipper extension fields required by block navigation"
  (get-in (target-by-id (load-catalog +catalog-path+)
                        :migration/std-lib-zip)
          [:target/struct-fields 'Zipper])
  => '[context prefix display parent left right depth changed? tag position])
