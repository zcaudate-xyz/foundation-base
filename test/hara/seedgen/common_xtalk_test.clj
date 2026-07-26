tahto/seedgen/common_xtalk_test.clj:1:(ns tahto.seedgen.common-xtalk-test
  (:use code.test)
tahto/seedgen/common_xtalk_test.clj:3:  (:require [tahto.seedgen.common-xtalk :refer :all]))

(def +canonical-case+
  {:id :basic
   :input '(x:get-key obj "a")
   :expect {:xtalk '(. obj ["a"])}})

(fact "runtime inventory exposes suite strategy for slow xt runtimes"
  (select-keys (get (runtime-inventory {:langs [:dart :js]})
                    :dart)
               [:runtime?
                :runtime-executable?
                :runtime-type
                :runtime-check-mode])
  => {:runtime? true
      :runtime-executable? false
      :runtime-type :twostep
      :runtime-check-mode :batched})

(fact "preserves authored canonical cases when regenerating inventory"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries
                     {:existing [{:op :x-get-key
                                  :doc "get-key transform"
                                  :cases [+canonical-case+]}]}))]
    (select-keys entry [:doc :cases]))
  => {:doc "get-key transform"
      :cases [+canonical-case+]})

tahto/seedgen/common_xtalk_test.clj:32:^{:refer tahto.seedgen.common-xtalk/grammar-entries :added "4.1"}
(fact "returns grammar xtalk entries"
  (vector? (grammar-entries))
  => true)

tahto/seedgen/common_xtalk_test.clj:37:^{:refer tahto.seedgen.common-xtalk/categories :added "4.1"}
(fact "returns xtalk categories"
  (vector? (categories))
  => true)

tahto/seedgen/common_xtalk_test.clj:42:^{:refer tahto.seedgen.common-xtalk/op-map :added "4.1"}
(fact "returns op map keyed by op"
  (map? (op-map))
  => true)

tahto/seedgen/common_xtalk_test.clj:47:^{:refer tahto.seedgen.common-xtalk/symbols :added "4.1"}
(fact "returns xtalk symbols vector"
  (vector? (symbols))
  => true)

tahto/seedgen/common_xtalk_test.clj:52:^{:refer tahto.seedgen.common-xtalk/installed-languages :added "4.1"}
(fact "returns installed languages vector"
  (vector? (installed-languages))
  => true)

tahto/seedgen/common_xtalk_test.clj:57:^{:refer tahto.seedgen.common-xtalk/parent-languages :added "4.1"}
(fact "returns xtalk parent languages vector"
  (vector? (parent-languages))
  => true)

tahto/seedgen/common_xtalk_test.clj:62:^{:refer tahto.seedgen.common-xtalk/languages :added "4.1"}
(fact "returns audit language selection"
  (vector? (languages))
  => true)

tahto/seedgen/common_xtalk_test.clj:67:^{:refer tahto.seedgen.common-xtalk/feature-status :added "4.1"}
(fact "feature-status returns known state keyword"
  (keyword? (feature-status :js 'x:get-key))
  => true)

tahto/seedgen/common_xtalk_test.clj:72:^{:refer tahto.seedgen.common-xtalk/support :added "4.1"}
(fact "support returns expected map keys"
  (-> (support)
      (keys)
      set)
  => #{:languages :features :status :summary})

tahto/seedgen/common_xtalk_test.clj:79:^{:refer tahto.seedgen.common-xtalk/model-inventory :added "4.1"}
(fact "returns xtalk model inventory"
  (map? (model-inventory))
  => true)

tahto/seedgen/common_xtalk_test.clj:84:^{:refer tahto.seedgen.common-xtalk/test-inventory :added "4.1"}
(fact "returns xtalk test inventory"
  (map? (test-inventory))
  => true)

tahto/seedgen/common_xtalk_test.clj:89:^{:refer tahto.seedgen.common-xtalk/runtime-inventory :added "4.1"}
(fact "returns xtalk runtime inventory"
  (map? (runtime-inventory))
  => true

  (get-in (runtime-inventory {:langs [:php]})
          [:php :runtime-executable?])
  => true)

tahto/seedgen/common_xtalk_test.clj:98:^{:refer tahto.seedgen.common-xtalk/spec-inventory :added "4.1"}
(fact "returns xtalk spec inventory"
  (map? (spec-inventory))
  => true)

tahto/seedgen/common_xtalk_test.clj:103:^{:refer tahto.seedgen.common-xtalk/language-status :added "4.1"}
(fact "returns merged xtalk language status"
  (map? (language-status))
  => true)

tahto/seedgen/common_xtalk_test.clj:108:^{:refer tahto.seedgen.common-xtalk/coverage-summary :added "4.1"}
(fact "returns xtalk coverage summary"
  (map? (coverage-summary))
  => true)

tahto/seedgen/common_xtalk_test.clj:113:^{:refer tahto.seedgen.common-xtalk/missing-by-language :added "4.1"}
(fact "missing-by-language returns map"
  (map? (missing-by-language))
  => true)

tahto/seedgen/common_xtalk_test.clj:118:^{:refer tahto.seedgen.common-xtalk/missing-by-feature :added "4.1"}
(fact "missing-by-feature returns map"
  (map? (missing-by-feature))
  => true)

tahto/seedgen/common_xtalk_test.clj:123:^{:refer tahto.seedgen.common-xtalk/inventory-path :added "4.1"}
(fact "builds ops path"
  (string? (inventory-path {:project {:root "."}}))
  => true)

tahto/seedgen/common_xtalk_test.clj:128:^{:refer tahto.seedgen.common-xtalk/read-inventory :added "4.1"}
(fact "returns nil for missing ops file"
  (read-inventory "target/missing-xtalk-ops.edn")
  => nil)

tahto/seedgen/common_xtalk_test.clj:133:^{:refer tahto.seedgen.common-xtalk/inventory-entries :added "4.1"}
(fact "builds xtalk inventory entries from grammar tables"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries))]
    (select-keys entry [:op :category :canonical-symbol :macro :emit]))
  => {:op :x-get-key
      :category :xtalk-common
      :canonical-symbol 'x:get-key
tahto/seedgen/common_xtalk_test.clj:141:      :macro 'tahto.common.grammar-xtalk/tf-get-key
      :emit :macro})

tahto/seedgen/common_xtalk_test.clj:144:^{:refer tahto.seedgen.common-xtalk/render-inventory :added "4.1"}
(fact "renders entries as string"
  (string? (render-inventory [{:op :x}]))
  => true)

tahto/seedgen/common_xtalk_test.clj:149:^{:refer tahto.seedgen.common-xtalk/generate-inventory :added "4.1"}
(fact "generates xtalk ops inventory"
  (map? (generate-inventory {:write false}))
  => true)

tahto/seedgen/common_xtalk_test.clj:154:^{:refer tahto.seedgen.common-xtalk/render-support :added "4.1"}
(fact "render-support returns printable output"
  (string? (render-support))
  => true)