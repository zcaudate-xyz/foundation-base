(ns xt.lib.redis-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lib.redis-connection :as redis]]})

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lib.redis-connection :as redis]]})

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lib.redis-connection :as redis]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lib.redis-connection/connection-create :added "4.1"}
(fact "wraps Redis connection implementations with protocol-backed dispatch"

  (!.js
   (var conn (redis/connection-create
              {"tag" "raw"}
              {"exec"       (fn [raw command args]
                              (return 1))
               "disconnect" (fn [raw]
                              (return true))}))
   [(redis/connection? conn)
    (redis/exec conn "PING" [])
    (redis/disconnect conn)])
  => [true 1 true]

  (!.lua
   (var conn (redis/connection-create
              {"tag" "raw"}
              {"exec"       (fn [raw command args]
                              (return 1))
               "disconnect" (fn [raw]
                              (return true))}))
   [(redis/connection? conn)
    (redis/exec conn "PING" [])
    (redis/disconnect conn)])
  => [true 1 true]

  (if CANARY-DART
    (!.dt
      (var conn (redis/connection-create
                 {"tag" "raw"}
                 {"exec"       (fn [raw command args]
                                 (return 1))
                  "disconnect" (fn [raw]
                                 (return true))}))
      [(redis/connection? conn)
       (redis/exec conn "PING" [])
       (redis/disconnect conn)])
    :dart-unavailable)
  => (any [true 1 true]
          :dart-unavailable))
