(ns rt.postgres.client-impl-test
  (:use code.test)
  (:require [lib.postgres :as base]
            [lib.postgres.connection :as conn]
            [rt.postgres.client-impl :as client-impl]
            [rt.postgres.client :as client]
            [rt.postgres.script.builtin :as builtin]
            [rt.postgres.script.addon :as addon]
            [rt.postgres.script.scratch :as scratch]
            [std.lang.base.util :as ut]
            [lib.jdbc :as jdbc]
            [std.lib :as h]))

^{:refer rt.postgres.client-impl/raw-eval-pg-return :added "4.0"}
(fact "returns a regularised result"
  ^:hidden
  
  (client-impl/raw-eval-pg-return [{:pg_jit_available true}])
  => true

  (client-impl/raw-eval-pg-return [{:result "{\"a\": 1}"}])
  => {:a 1})

^{:refer rt.postgres.client-impl/raw-eval-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"
                                         :temp :create}))]
  :teardown (h/stop -pg-)}
(fact "executes a raw value"
  ^:hidden
  
  (binding [conn/*execute* jdbc/fetch]
    (client-impl/raw-eval-pg -pg- "select 1;"))
  => [{:?column? 1}])

^{:refer rt.postgres.client-impl/init-ptr-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"
                                         :mode :dev}))]
  :teardown (h/stop -pg-)}
(fact "initiates a pointer in the runtime"

  (do (client-impl/init-ptr-pg -pg- scratch/addf))
  => anything

  #_#_#_
  (client-impl/init-ptr-pg -pg- scratch/addf)
  => -2)

^{:refer rt.postgres.client-impl/prepend-select-check-form :added "4.0"}
(fact "checks if form needs a `SELECT` prepended"
  ^:hidden
  
  (client-impl/prepend-select-check builtin/acos
                             [])
  => true
  
  (client-impl/prepend-select-check addon/b:insert
                             [])
  => false
  
  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             [1])
  => true
  
  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ["hello"])
  => true
  
  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(:int 0.5)])
  => true
  
  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(++ 0.5 :int)])
  => true
  
  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(if a 1 2)])
  => false

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:block 1 2 3)])
  => false

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:assert 1 2 3)])
  => false

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             ['(do:run 1 2 3)])
  => nil

  (client-impl/prepend-select-check (ut/lang-pointer :postgres)
                             [[:anything]])
  => false)

^{:refer rt.postgres.client-impl/prepend-select-check :added "4.0"}
(fact "checks if values needs a `SELECT` prepended"
  ^:hidden
  
  (client-impl/prepend-select-check builtin/cot [40])
  => true)

^{:refer rt.postgres.client-impl/invoke-ptr-pg :added "4.0"
  :setup [(def -pg- (client/rt-postgres {:dbname "test-scratch"}))]
  :teardown (h/stop -pg-)}
(fact "invokes a pointer in runtime"
  ^:hidden
  
  (client-impl/invoke-ptr-pg -pg- builtin/cot [40])
  => -0.8950829176379128)


^{:refer rt.postgres.client-impl/invoke-ptr-pg-single :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.client-impl/invoke-ptr-pg-transform-let :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.client-impl/invoke-ptr-pg-block :added "4.0"}
(fact "TODO")