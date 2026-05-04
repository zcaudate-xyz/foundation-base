(ns hara.seedgen.common-xtalk-test
  (:use code.test)
  (:require [hara.seedgen.common-xtalk :refer :all]))

(def +canonical-case+
  {:id :basic
   :input '(x:get-key obj "a")
   :expect {:xtalk '(. obj ["a"])}})

^{:refer hara.seedgen.common-xtalk/categories :added "4.1"}
(fact "returns xtalk categories"
  (vector? (categories))
  => true)

^{:refer hara.seedgen.common-xtalk/op-map :added "4.1"}
(fact "returns op map keyed by op"
  (map? (op-map))
  => true)

^{:refer hara.seedgen.common-xtalk/symbols :added "4.1"}
(fact "returns xtalk symbols vector"
  (vector? (symbols))
  => true)

^{:refer hara.seedgen.common-xtalk/installed-languages :added "4.1"}
(fact "returns installed languages vector"
  (vector? (installed-languages))
  => true)

^{:refer hara.seedgen.common-xtalk/parent-languages :added "4.1"}
(fact "returns xtalk parent languages vector"
  (vector? (parent-languages))
  => true)

^{:refer hara.seedgen.common-xtalk/languages :added "4.1"}
(fact "returns audit language selection"
  (vector? (languages))
  => true)

^{:refer hara.seedgen.common-xtalk/feature-status :added "4.1"}
(fact "feature-status returns known state keyword"
  (keyword? (feature-status :js 'x:get-key))
  => true)

^{:refer hara.seedgen.common-xtalk/model-inventory :added "4.1"}
(fact "returns xtalk model inventory"
  (map? (model-inventory))
  => true)

^{:refer hara.seedgen.common-xtalk/test-inventory :added "4.1"}
(fact "returns xtalk test inventory"
  (map? (test-inventory))
  => true)

^{:refer hara.seedgen.common-xtalk/runtime-inventory :added "4.1"}
(fact "returns xtalk runtime inventory"
  (map? (runtime-inventory))
  => true

  (get-in (runtime-inventory {:langs [:php]})
          [:php :runtime-executable?])
  => true)

^{:refer hara.seedgen.common-xtalk/spec-inventory :added "4.1"}
(fact "returns xtalk spec inventory"
  (map? (spec-inventory))
  => true)

^{:refer hara.seedgen.common-xtalk/language-status :added "4.1"}
(fact "returns merged xtalk language status"
  (map? (language-status))
  => true)

^{:refer hara.seedgen.common-xtalk/coverage-summary :added "4.1"}
(fact "returns xtalk coverage summary"
  (map? (coverage-summary))
  => true)

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

^{:refer hara.seedgen.common-xtalk/support :added "4.1"}
(fact "support returns expected map keys"
  (-> (support)
      (keys)
      set)
  => #{:languages :features :status :summary})

^{:refer hara.seedgen.common-xtalk/missing-by-language :added "4.1"}
(fact "missing-by-language returns map"
  (map? (missing-by-language))
  => true)

^{:refer hara.seedgen.common-xtalk/missing-by-feature :added "4.1"}
(fact "missing-by-feature returns map"
  (map? (missing-by-feature))
  => true)

^{:refer hara.seedgen.common-xtalk/render-support :added "4.1"}
(fact "render-support returns printable output"
  (string? (render-support))
  => true)

^{:refer hara.seedgen.common-xtalk/grammar-entries :added "4.1"}
(fact "returns grammar xtalk entries"
  (vector? (grammar-entries))
  => true)

^{:refer hara.seedgen.common-xtalk/read-inventory :added "4.1"}
(fact "returns nil for missing ops file"
  (read-inventory "target/missing-xtalk-ops.edn")
  => nil)

^{:refer hara.seedgen.common-xtalk/inventory-entries :added "4.1"}
(fact "builds xtalk inventory entries from grammar tables"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries))]
    (select-keys entry [:op :category :canonical-symbol :macro :emit]))
  => {:op :x-get-key
      :category :xtalk-common
      :canonical-symbol 'x:get-key
      :macro 'hara.common.grammar-xtalk/tf-get-key
      :emit :macro})

(fact "preserves authored canonical cases when regenerating inventory"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries
                     {:existing [{:op :x-get-key
                                  :doc "get-key transform"
                                  :cases [+canonical-case+]}]}))]
    (select-keys entry [:doc :cases]))
  => {:doc "get-key transform"
      :cases [+canonical-case+]})

^{:refer hara.seedgen.common-xtalk/inventory-path :added "4.1"}
(fact "builds ops path"
  (string? (inventory-path {:project {:root "."}}))
  => true)

^{:refer hara.seedgen.common-xtalk/render-inventory :added "4.1"}
(fact "renders entries as string"
  (string? (render-inventory [{:op :x}]))
  => true)

^{:refer hara.seedgen.common-xtalk/generate-inventory :added "4.1"}
(fact "generates xtalk ops inventory"
  (map? (generate-inventory {:write false}))
  => true)
