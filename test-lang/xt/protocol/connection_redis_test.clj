(ns xt.protocol.connection-redis-test
  (:use code.test)
  (:require [hara.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.connection-redis :as redisp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.connection-redis :as redisp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.connection-redis :as redisp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.connection-redis/IRedisConnectionDriver :added "4.1"}
(fact "defines the Redis connection protocol surfaces"

  (!.js
    [redisp/IRedisConnectionDriver
     redisp/IRedisConnection
     redisp/IRedisRuntimeDriver
     redisp/IRedisRuntimeConnection])
  => [["connect"]
      ["disconnect" "exec"]
      ["connect"]
      ["disconnect" "exec"]]

  (!.lua
    [redisp/IRedisConnectionDriver
     redisp/IRedisConnection
     redisp/IRedisRuntimeDriver
     redisp/IRedisRuntimeConnection])
  => [["connect"]
      ["disconnect" "exec"]
      ["connect"]
      ["disconnect" "exec"]]

  (!.py
    [redisp/IRedisConnectionDriver
     redisp/IRedisConnection
     redisp/IRedisRuntimeDriver
     redisp/IRedisRuntimeConnection])
  => [["connect"]
      ["disconnect" "exec"]
      ["connect"]
      ["disconnect" "exec"]])

(comment
  (s/snapto '[xt.protocol.connection-redis])
  
  (s/seedgen-langadd '[xt.protocol.connection-redis] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.connection-redis] {:lang [:lua :python] :write true}))
