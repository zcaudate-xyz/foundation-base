(ns tahto.core.registry-test
  (:use code.test)
  (:require [tahto.core.registry :refer :all]))

^{:refer tahto.core.registry/registry-book-list :added "4.1"}
(fact "lists all registered books"
  (let [books (set (registry-book-list))]
    [(contains? books [:js :default])
      (contains? books [:lua :default])
      (contains? books [:lua.nginx :default])
      (contains? books [:lua.redis :default])
      (contains? books [:postgres :default])])
  => [true true true true true])

^{:refer tahto.core.registry/registry-book-ns :added "4.1"}
(fact "gets the namespace for a registry entry"
  (registry-book-ns :js)
tahto/lang/registry_test.clj:18:  => 'tahto.model.spec-js

  (registry-book-ns :circom)
tahto/lang/registry_test.clj:21:  => 'tahto.model.annex.spec-circom

  (registry-book-ns :fortran)
tahto/lang/registry_test.clj:24:  => 'tahto.model.annex.spec-fortran

  (registry-book-ns :llvm)
tahto/lang/registry_test.clj:27:  => 'tahto.model.spec-llvm

  (registry-book-ns :solidity)
tahto/lang/registry_test.clj:30:  => 'tahto.model.spec-solidity

  (registry-book-ns :ruby)
tahto/lang/registry_test.clj:33:  => 'tahto.model.spec-ruby

  (registry-book-ns :verilog)
tahto/lang/registry_test.clj:36:  => 'tahto.model.annex.spec-verilog)

^{:refer tahto.core.registry/registry-book-info :added "4.1"}
(fact "gets the full registry entry"
  (registry-book-info :js)
tahto/lang/registry_test.clj:41:  => '{:ns tahto.model.spec-js
       :book +book+
       :parent :xtalk})

(fact "gets variant lua book info"
  (registry-book-info :lua.redis)
tahto/lang/registry_test.clj:47:  => '{:ns tahto.model.spec-lua.variant-redis
       :book +book+
       :parent :lua}

  (registry-book-info :lua.nginx)
tahto/lang/registry_test.clj:52:  => '{:ns tahto.model.spec-lua.variant-nginx
       :book +book+
       :parent :lua})

^{:refer tahto.core.registry/registry-book :added "4.1"}
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
