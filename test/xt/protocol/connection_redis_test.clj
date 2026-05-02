(ns xt.protocol.redis-connection-test
  (:use code.test)
  (:require [std.lang :as l]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.redis-connection :as redisp]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.protocol.redis-connection :as redisp]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.protocol.redis-connection :as redisp]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.redis-connection/IRedisConnectionDriver :added "4.1"}
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
  (s/snapto '[xt.protocol.redis-connection])
  
  (s/seedgen-langadd '[xt.protocol.redis-connection] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.protocol.redis-connection] {:lang [:lua :python] :write true}))
