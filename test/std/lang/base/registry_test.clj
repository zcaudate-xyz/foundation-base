(ns std.lang.base.registry-test
  (:use code.test)
  (:require [std.lang.base.registry :refer :all]))

^{:refer std.lang.base.registry/registry-book-list :added "4.1"}
(fact "lists all registered books"
  (let [books (set (registry-book-list))]
    [(contains? books [:js :default])
      (contains? books [:lua :default])
      (contains? books [:lua.nginx :default])
      (contains? books [:lua.redis :default])
      (contains? books [:postgres :default])])
  => [true true true true true])

^{:refer std.lang.base.registry/registry-book-ns :added "4.1"}
(fact "gets the namespace for a registry entry"
  (registry-book-ns :js)
  => 'std.lang.model.spec-js)

^{:refer std.lang.base.registry/registry-book-info :added "4.1"}
(fact "gets the full registry entry"
  (registry-book-info :js)
  => '{:ns std.lang.model.spec-js
       :book +book+
       :parent :xtalk})

^{:refer std.lang.base.registry/registry-book :added "4.1"}
(fact "loads and returns a registered book"
  (-> (registry-book :js)
      :lang)
  => :js)
