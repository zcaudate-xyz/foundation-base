(ns xt.runtime.type-redis-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.runtime.type-redis-connection :as redisrt]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.runtime.type-redis-connection :as redisrt]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.runtime.type-redis-connection :as redisrt]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.runtime.type-redis-connection/connection-create :added "4.1"}
(fact "wraps fresh Redis connection implementations with runtime dispatch"

  (!.js
   (var conn (redisrt/connection-create
              {"tag" "raw"}
              {"exec"       (fn [raw command args]
                              (return 1))
               "disconnect" (fn [raw]
                              (return true))}))
   [(redisrt/connection? conn)
    (redisrt/connection-exec conn "PING" [])
    (redisrt/connection-disconnect conn)])
  => [true 1 true]

  (!.lua
   (var conn (redisrt/connection-create
              {"tag" "raw"}
              {"exec"       (fn [raw command args]
                              (return 1))
               "disconnect" (fn [raw]
                              (return true))}))
   [(redisrt/connection? conn)
    (redisrt/connection-exec conn "PING" [])
    (redisrt/connection-disconnect conn)])
  => [true 1 true]

  (if CANARY-DART
    (!.dt
      (var conn (redisrt/connection-create
                 {"tag" "raw"}
                 {"exec"       (fn [raw command args]
                                 (return 1))
                  "disconnect" (fn [raw]
                                 (return true))}))
      [(redisrt/connection? conn)
       (redisrt/connection-exec conn "PING" [])
       (redisrt/connection-disconnect conn)])
    :dart-unavailable)
  => (any [true 1 true]
          :dart-unavailable))
