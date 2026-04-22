(ns
 xtbench.lua.sys.conn-redis-test
 (:require
  [lib.redis.bench :as bench]
  [std.lang :as l]
  [std.lib.security :as security]
  [xt.lang.common-notify :as notify])
 (:use code.test))

(l/script-
 :lua
 {:runtime :basic,
  :config {:program :resty},
  :require
  [[xt.sys.conn-redis :as redis]
   [lua.nginx.driver-redis :as lua-driver]
   [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(bench/start-redis-array [17001]) (l/rt:restart)],
  :teardown [(l/rt:stop) (bench/stop-redis-array [17001])]})

^{:refer xt.sys.conn-redis/connect, :added "4.0"}
(fact
 "connects to a datasource"
 ^{:hidden true}
 (!.lua
  (var
   conn
   (redis/connect
    {:constructor lua-driver/connect-constructor, :port 17001}
    {}))
  (redis/exec conn "ping" [] {}))
 =>
 "PONG"
 (!.lua
  (var
   conn
   (redis/connect
    {:constructor lua-driver/connect-constructor, :port 17001}
    {}))
  (redis/exec conn "echo" ["hello"] {}))
 =>
 "hello")

^{:refer xt.sys.conn-redis/exec, :added "4.0"}
(fact
 "executes a redis command"
 ^{:hidden true}
 (!.lua
  (var
   conn
   (redis/connect
    {:constructor lua-driver/connect-constructor, :port 17001}
    {}))
  [(redis/exec conn "ping" []) (redis/exec conn "echo" ["hello"])])
 =>
 ["PONG" "hello"])

^{:refer xt.sys.conn-redis/eval-body, :added "4.0"}
(fact
 "evaluates a the body"
 ^{:hidden true}
 (!.lua
  (var
   conn
   (redis/connect
    {:constructor lua-driver/connect-constructor, :port 17001}
    {}))
  [(redis/eval-body conn {:body "return 1"} [] {})
   (redis/eval-body conn {:body "return 0"} [] {})
   (redis/eval-body conn {:body "return nil"} [] {})
   (redis/eval-body conn {:body "return false"} [] {})
   (redis/eval-body conn {:body "return true"} [] {})
   (redis/eval-body conn {:body "return 'hello'"} [] {})])
 =>
 [1 0 nil nil 1 "hello"])

^{:refer xt.sys.conn-redis/eval-script, :added "4.0"}
(fact
 "evaluates sha, then body if errored"
 ^{:hidden true}
 (!.lua
  (var
   conn
   (redis/connect
    {:constructor lua-driver/connect-constructor, :port 17001}
    {}))
  [(redis/exec conn "flushdb" [])
   (redis/eval-script
    conn
    {:sha (@! (security/sha1 "return 1")), :body "return 1"}
    [])])
 =>
 ["OK" 1])
