(ns std.lang.manage.xtalk-ops-test
  (:use code.test)
  (:require [std.lang.manage.xtalk-ops :refer :all]))

(def +canonical-case+
  {:id :basic
   :input '(x:get-key obj "a")
   :expect {:xtalk '(. obj ["a"])}})

^{:refer std.lang.manage.xtalk-ops/var->symbol :added "4.1"}
(fact "normalizes vars to symbols"
  (symbol? (var->symbol #'var->symbol))
  => true)

^{:refer std.lang.manage.xtalk-ops/xtalk-category-map :added "4.1"}
(fact "returns category map"
  (map? (xtalk-category-map))
  => true)

^{:refer std.lang.manage.xtalk-ops/grammar-xtalk-entries :added "4.1"}
(fact "returns grammar xtalk entries"
  (vector? (grammar-xtalk-entries))
  => true)

^{:refer std.lang.manage.xtalk-ops/read-xtalk-ops :added "4.1"}
(fact "returns nil for missing ops file"
  (read-xtalk-ops "target/missing-xtalk-ops.edn")
  => nil)

^{:refer std.lang.manage.xtalk-ops/compact-entry :added "4.1"}
(fact "removes nil values"
  (compact-entry {:a 1 :b nil})
  => {:a 1})

^{:refer std.lang.manage.xtalk-ops/symbol-doc :added "4.1"}
(fact "returns doc for known symbol"
  (string? (symbol-doc 'std.lang.manage.xtalk-ops/var->symbol))
  => true)

^{:refer std.lang.manage.xtalk-ops/inventory-entry :added "4.1"}
(fact "builds inventory entry maps"
  (let [entry (first (grammar-xtalk-entries))]
    (map? (inventory-entry entry :xtalk-common {})))
  => true)

^{:refer std.lang.manage.xtalk-ops/inventory-entries :added "4.1"}
(fact "builds xtalk inventory entries from grammar tables"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries))]
    (select-keys entry [:op :category :canonical-symbol :macro :emit]))
  => {:op :x-get-key
      :category :xtalk-common
      :canonical-symbol 'x:get-key
      :macro 'std.lang.base.grammar-xtalk/tf-get-key
      :emit :macro})

(fact "preserves authored canonical cases when regenerating inventory"
  (let [entry (some #(when (= :x-get-key (:op %)) %)
                    (inventory-entries
                     [{:op :x-get-key
                       :doc "get-key transform"
                       :cases [+canonical-case+]}]))]
    (select-keys entry [:doc :cases]))
  => {:doc "get-key transform"
      :cases [+canonical-case+]})

^{:refer std.lang.manage.xtalk-ops/ops-path :added "4.1"}
(fact "builds ops path"
  (string? (ops-path {:root "."}))
  => true)

^{:refer std.lang.manage.xtalk-ops/render-xtalk-ops :added "4.1"}
(fact "renders entries as string"
  (string? (render-xtalk-ops [{:op :x}]))
  => true)

^{:refer std.lang.manage.xtalk-ops/generate-xtalk-ops :added "4.1"}
(fact "generates xtalk ops inventory"
  (map? (generate-xtalk-ops nil {:write false}))
  => true)