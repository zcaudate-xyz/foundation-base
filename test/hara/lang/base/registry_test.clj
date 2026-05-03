(ns hara.lang.base.registry-test
  (:use code.test)
  (:require [hara.lang.base.registry :refer :all]))

^{:refer hara.lang.base.registry/registry-book-list :added "4.1"}
(fact "lists all registered books"
  (let [books (set (registry-book-list))]
    [(contains? books [:js :default])
      (contains? books [:lua :default])
      (contains? books [:lua.nginx :default])
      (contains? books [:lua.redis :default])
      (contains? books [:postgres :default])])
  => [true true true true true])

^{:refer hara.lang.base.registry/registry-book-ns :added "4.1"}
(fact "gets the namespace for a registry entry"
  (registry-book-ns :js)
  => 'hara.lang.model.spec-js)

^{:refer hara.lang.base.registry/registry-book-info :added "4.1"}
(fact "gets the full registry entry"
  (registry-book-info :js)
  => '{:ns hara.lang.model.spec-js
       :book +book+
       :parent :xtalk})

(fact "gets variant lua book info"
  (registry-book-info :lua.redis)
  => '{:ns hara.lang.model.spec-lua.variant-redis
       :book +book+
       :parent :lua}

  (registry-book-info :lua.nginx)
  => '{:ns hara.lang.model.spec-lua.variant-nginx
       :book +book+
       :parent :lua})

^{:refer hara.lang.base.registry/registry-book :added "4.1"}
(fact "loads and returns a registered book"
  (-> (registry-book :js)
      :lang)
  => :js

  (-> (registry-book :lua.redis)
      :lang)
  => :lua.redis

  (-> (registry-book :lua.nginx)
      :lang)
  => :lua.nginx)
