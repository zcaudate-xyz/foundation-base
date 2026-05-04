(ns hara.lang.registry-test
  (:use code.test)
  (:require [hara.lang.registry :refer :all]))

^{:refer hara.lang.registry/registry-book-list :added "4.1"}
(fact "lists all registered books"
  (let [books (set (registry-book-list))]
    [(contains? books [:js :default])
      (contains? books [:lua :default])
      (contains? books [:lua.nginx :default])
      (contains? books [:lua.redis :default])
      (contains? books [:postgres :default])])
  => [true true true true true])

^{:refer hara.lang.registry/registry-book-ns :added "4.1"}
(fact "gets the namespace for a registry entry"
  (registry-book-ns :js)
  => 'hara.model.spec-js

  (registry-book-ns :circom)
  => 'hara.model.annex.spec-circom

  (registry-book-ns :fortran)
  => 'hara.model.annex.spec-fortran

  (registry-book-ns :llvm)
  => 'hara.model.annex.spec-llvm

  (registry-book-ns :solidity)
  => 'hara.model.spec-solidity

  (registry-book-ns :ruby)
  => 'hara.model.spec-ruby

  (registry-book-ns :verilog)
  => 'hara.model.annex.spec-verilog)

^{:refer hara.lang.registry/registry-book-info :added "4.1"}
(fact "gets the full registry entry"
  (registry-book-info :js)
  => '{:ns hara.model.spec-js
       :book +book+
       :parent :xtalk})

(fact "gets variant lua book info"
  (registry-book-info :lua.redis)
  => '{:ns hara.model.spec-lua.variant-redis
       :book +book+
       :parent :lua}

  (registry-book-info :lua.nginx)
  => '{:ns hara.model.spec-lua.variant-nginx
       :book +book+
       :parent :lua})

^{:refer hara.lang.registry/registry-book :added "4.1"}
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
