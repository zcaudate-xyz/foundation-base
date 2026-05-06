(ns xtbench.ruby.protocol.redis-connection-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.protocol.redis-connection :as redisp]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.redis-connection/IRedisConnectionDriver :added "4.1"}
(fact "defines the Redis connection protocol surfaces"

  (!.rb
    [redisp/IRedisConnectionDriver
     redisp/IRedisConnection
     redisp/IRedisRuntimeDriver
     redisp/IRedisRuntimeConnection])
  => [["connect"]
      ["disconnect" "exec"]
      ["connect"]
      ["disconnect" "exec"]])

(comment
  (s/snapto '[xt.protocol.redis-connection])
  
  (s/seedgen-langadd '[xt.protocol.redis-connection] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.redis-connection] {:lang [:lua :python] :write true}))
