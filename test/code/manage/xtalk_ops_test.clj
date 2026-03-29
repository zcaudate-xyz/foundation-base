(ns code.manage.xtalk-ops-test
  (:require [code.manage.xtalk-ops :as xtalk-ops])
  (:use code.test))

(def +canonical-case+
  {:id :basic
   :input '(x:get-key obj "a")
   :expect {:xtalk '(. obj ["a"])}})

(fact "builds xtalk inventory entries from the grammar tables"
  (let [entry (some #(when (= :x-get-key (:op %))
                       %)
                    (xtalk-ops/inventory-entries))]
    (select-keys entry [:op :category :canonical-symbol :macro :emit]))
  => {:op :x-get-key
      :category :xtalk-access
      :canonical-symbol 'x:get-key
      :macro 'std.lang.base.grammar-xtalk/tf-get-key
      :emit :macro})

(fact "preserves authored canonical cases when regenerating inventory"
  (let [entry (some #(when (= :x-get-key (:op %))
                       %)
                    (xtalk-ops/inventory-entries
                     [{:op :x-get-key
                       :doc "get-key transform"
                       :cases [+canonical-case+]}]))]
    (select-keys entry [:doc :cases]))
  => {:doc "get-key transform"
      :cases [+canonical-case+]})
