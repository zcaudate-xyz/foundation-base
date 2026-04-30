(ns xt.protocol.redis-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.redis-connection :as redisp]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.protocol.redis-connection :as redisp]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.protocol.redis-connection :as redisp]]})

(def CANARY-DART
  (common/program-exists? "dart"))

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

  (if CANARY-DART
    (!.dt
      [redisp/IRedisConnectionDriver
       redisp/IRedisConnection
       redisp/IRedisRuntimeDriver
       redisp/IRedisRuntimeConnection])
    :dart-unavailable)
  => (any [["connect"]
           ["disconnect" "exec"]
           ["connect"]
           ["disconnect" "exec"]]
          :dart-unavailable))
