(ns xt.lib.redis-connection-test
  (:require [rt.basic.type-common :as common]
            [std.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lib.redis-connection :as redis]]})

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


^{:refer xt.lib.redis-connection/driver? :added "4.1"}
(fact "identifies wrapped redis drivers"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (redis/connection-create
                       opts
                       {"exec"       (fn [raw command args]
                                       (return command))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (var driver
        (redis/driver-create impl))
   [(redis/driver? driver)
    (redis/driver? {"::" "redis.connection"})
    (redis/driver? nil)])
  => [true false false])

^{:refer xt.lib.redis-connection/connection? :added "4.1"}
(fact "identifies wrapped redis connections"
  (!.js
   (var conn
        (redis/connection-create
         {"tag" "raw"}
         {"exec"       (fn [raw command args]
                         (return command))
          "disconnect" (fn [raw]
                         (return true))}))
   [(redis/connection? conn)
    (redis/connection? {"::" "redis.connection.driver"})
    (redis/connection? nil)])
  => [true false false])

^{:refer xt.lib.redis-connection/ensure-promise :added "4.1"}
(fact "normalises redis values into host promises"
  (!.js
   [(promise/x:promise-native? (redis/ensure-promise 5))
    (promise/x:promise-native?
     (redis/ensure-promise
      (promise/x:promise
       (fn []
         (return 6)))))])
  => [true true]

  (notify/wait-on :js
    (promise/x:promise-then
     (redis/ensure-promise 5)
     (repl/>notify)))
  => 5)

^{:refer xt.lib.redis-connection/require-driver :added "4.1"}
(fact "requires wrapped redis drivers"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (redis/connection-create
                       opts
                       {"exec"       (fn [raw command args]
                                       (return command))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (var driver
        (redis/driver-create impl))
   [(redis/driver? (redis/require-driver driver))
    (xt/x:get-key (redis/require-driver driver) "::")])
  => [true "redis.connection.driver"])

^{:refer xt.lib.redis-connection/require-connection :added "4.1"}
(fact "requires wrapped redis connections"
  (!.js
   (var conn
        (redis/connection-create
         {"tag" "raw"}
         {"exec"       (fn [raw command args]
                         (return command))
          "disconnect" (fn [raw]
                         (return true))}))
   [(redis/connection? (redis/require-connection conn))
    (xt/x:get-key (redis/require-connection conn) "::")])
  => [true "redis.connection"])

^{:refer xt.lib.redis-connection/driver-create :added "4.1"}
(fact "wraps redis driver implementations"
  (!.js
   (var impl
        {"connect" (fn [opts]
                     (return
                      (redis/connection-create
                       opts
                       {"exec"       (fn [raw command args]
                                       (return command))
                        "disconnect" (fn [raw]
                                       (return true))})))})
   (redis/driver?
    (redis/driver-create impl)))
  => true)

^{:refer xt.lib.redis-connection/connect :added "4.1"}
(fact "connects through wrapped redis drivers"
  (notify/wait-on :js
    (var impl
         {"connect" (fn [opts]
                      (return
                       (redis/connection-create
                        opts
                        {"exec"       (fn [raw command args]
                                        (return command))
                         "disconnect" (fn [raw]
                                        (return (xt/x:get-key raw "uri")))})))})
    (promise/x:promise-then
     (redis/connect
      (redis/driver-create impl)
      {"uri" "redis://connect"})
     (fn [conn]
       (repl/notify [(redis/connection? conn)
                     (redis/disconnect conn)]))))
  => [true "redis://connect"])

^{:refer xt.lib.redis-connection/disconnect :added "4.1"}
(fact "disconnects through wrapped redis connections"
  (!.js
   (var conn
        (redis/connection-create
         {"tag" "raw"}
         {"exec"       (fn [raw command args]
                         (return command))
          "disconnect" (fn [raw]
                         (return (xt/x:get-key raw "tag")))}))
   (redis/disconnect conn))
  => "raw")

^{:refer xt.lib.redis-connection/exec :added "4.1"}
(fact "executes commands through wrapped redis connections"
  (!.js
   (var conn
        (redis/connection-create
         {"tag" "raw"}
         {"exec"       (fn [raw command args]
                         (return [command args (xt/x:get-key raw "tag")]))
          "disconnect" (fn [raw]
                         (return true))}))
   (redis/exec conn "PING" ["A"]))
  => ["PING" ["A"] "raw"])
