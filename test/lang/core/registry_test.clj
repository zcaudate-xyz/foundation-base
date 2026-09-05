(ns lang.core.registry-test
  (:use code.test)
  (:require [lang.core.registry :refer :all]))

^{:refer lang.core.registry/registry-book-list :added "4.1"}
(fact "lists all registered books"
  (let [books (set (registry-book-list))]
    [(contains? books [:js :default])
      (contains? books [:lua :default])
      (contains? books [:lua.nginx :default])
      (contains? books [:lua.redis :default])
      (contains? books [:postgres :default])])
  => [true true true true true])

^{:refer lang.core.registry/registry-book-ns :added "4.1"}
(fact "gets the namespace for a registry entry"
  (registry-book-ns :js)
  => 'lang.model.spec-js

  (registry-book-ns :circom)
  => 'lang.model.annex.spec-circom

  (registry-book-ns :fortran)
  => 'lang.model.annex.spec-fortran

  (registry-book-ns :llvm)
  => 'lang.model.spec-llvm

  (registry-book-ns :solidity)
  => 'lang.model.spec-solidity

  (registry-book-ns :ruby)
  => 'lang.model.spec-ruby

  (registry-book-ns :verilog)
  => 'lang.model.annex.spec-verilog)

^{:refer lang.core.registry/registry-book-info :added "4.1"}
(fact "gets the full registry entry"
  (registry-book-info :js)
  => '{:ns lang.model.spec-js
       :book +book+
       :parent :xtalk})

(fact "gets variant lua book info"
  (registry-book-info :lua.redis)
  => '{:ns lang.model.spec-lua.variant-redis
       :book +book+
       :parent :lua}

  (registry-book-info :lua.nginx)
  => '{:ns lang.model.spec-lua.variant-nginx
       :book +book+
       :parent :lua})

^{:refer lang.core.registry/registry-book :added "4.1"}
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
