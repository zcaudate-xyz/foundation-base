(ns
 xtbench.js.sys.conn-redis-test
 (:require
  [lib.redis.bench :as bench]
  [std.lang :as l]
  [std.lib.security :as security]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :js
 {:runtime :basic,
  :require
  [[xt.sys.conn-redis :as redis]
   [xt.lang.common-repl :as repl]
   [js.lib.driver-redis :as js-driver]]})

(fact:global
 {:setup [(bench/start-redis-array [17001]) (l/rt:restart)],
  :teardown [(l/rt:stop) (bench/stop-redis-array [17001])]})

^{:refer xt.sys.conn-redis/connect, :added "4.0"}
(fact
 "connects to a datasource"
 ^{:hidden true}
 (notify/wait-on
  :js
  (redis/connect
   {:constructor js-driver/connect-constructor, :port 17001}
   {:success (fn [conn] (redis/exec conn "ping" [] (repl/<!)))}))
 =>
 "PONG"
 (notify/wait-on
  :js
  (redis/connect
   {:constructor js-driver/connect-constructor, :port 17001}
   {:success
    (fn [conn] (redis/exec conn "echo" ["hello"] (repl/<!)))}))
 =>
 "hello")

^{:refer xt.sys.conn-redis/exec, :added "4.0"}
(fact
 "executes a redis command"
 ^{:hidden true}
 (do
  (notify/wait-on
   :js
   (:=
    (!:G conn)
    (redis/connect
     {:constructor js-driver/connect-constructor, :port 17001}
     (repl/<!))))
  [(notify/wait-on :js (redis/exec conn "ping" [] (repl/<!)))
   (notify/wait-on
    :js
    (. (redis/exec conn "echo" ["hello"]) (then (repl/>notify))))])
 =>
 ["PONG" "hello"])

^{:refer xt.sys.conn-redis/eval-body, :added "4.0"}
(fact
 "evaluates a the body"
 ^{:hidden true}
 (do
  (notify/wait-on
   :js
   (:=
    (!:G conn)
    (redis/connect
     {:constructor js-driver/connect-constructor, :port 17001}
     (repl/<!))))
  [(notify/wait-on
    :js
    (redis/eval-body conn {:body "return 1"} [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-body conn {:body "return 0"} [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-body conn {:body "return nil"} [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-body conn {:body "return false"} [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-body conn {:body "return true"} [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-body conn {:body "return 'hello'"} [] (repl/<!)))])
 =>
 [1 0 nil nil 1 "hello"])

^{:refer xt.sys.conn-redis/eval-script, :added "4.0"}
(fact
 "evaluates sha, then body if errored"
 ^{:hidden true}
 (do
  (notify/wait-on
   :js
   (:=
    (!:G conn)
    (redis/connect
     {:constructor js-driver/connect-constructor, :port 17001}
     (repl/<!))))
  [(notify/wait-on :js (redis/exec conn "flushdb" [] (repl/<!)))
   (notify/wait-on
    :js
    (redis/eval-script
     conn
     {:sha (@! (security/sha1 "return 2")), :body "return 2"}
     []
     (repl/<!)))])
 =>
 ["OK" 2])
