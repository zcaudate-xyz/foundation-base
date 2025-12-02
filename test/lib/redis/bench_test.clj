(ns lib.redis.bench-test
  (:use code.test)
  (:require [lib.redis.bench :as bench]
            [std.lib :as h]
            [std.fs :as fs]))

^{:refer lib.redis.bench/all-redis-ports :added "4.0"}
(fact "gets all active redis ports"
  (with-redefs [h/sh (constantly "redis-server 1234 *:6379 (LISTEN)")]
    (bench/all-redis-ports))
  => map?)

^{:refer lib.redis.bench/config-to-args :added "4.0"}
(fact "convert config map to args"
  (bench/config-to-args {:port 21001
                         :appendonly true})
  => "port 21001\nappendonly yes")

^{:refer lib.redis.bench/start-redis-server :added "4.0"}
(fact "starts the redis server in a given directory"
  (with-redefs [h/port:check-available (constantly 17001)
                fs/create-directory (constantly nil)
                fs/exists? (constantly true)
                h/sh (constantly (delay "process"))
                h/sh-wait (constantly nil)
                h/wait-for-port (constantly true)]
    (bench/start-redis-server {:port 17001} :test "root"))
  => (contains {:port 17001 :type :test :process any}))

^{:refer lib.redis.bench/stop-redis-server :added "4.0"}
(fact "stop the redis server"
  (with-redefs [h/sh-close (constantly nil)
                h/sh-exit (constantly nil)
                h/sh-wait (constantly nil)]
    (bench/stop-redis-server 17001 :test))
  => (any nil? map?))

^{:refer lib.redis.bench/bench-start :added "4.0"}
(fact "starts the bench"
  (with-redefs [bench/start-redis-server (constantly {:port 17001})]
    (bench/bench-start {:port 17001} :test))
  => {:port 17001})

^{:refer lib.redis.bench/bench-stop :added "4.0"}
(fact "stops the bench"
  (with-redefs [bench/stop-redis-server (constantly nil)]
    (bench/bench-stop {:port 17001 :bench :test} nil))
  => {:port 17001 :bench :test})

^{:refer lib.redis.bench/start-redis-array :added "4.0"}
(fact "starts a redis array"
  (with-redefs [bench/start-redis-server (constantly {:type :array :port 17001})]
    (bench/start-redis-array [17001]))
  => [{:type :array :port 17001}])

^{:refer lib.redis.bench/stop-redis-array :added "4.0"}
(fact "stops a redis array"
  (with-redefs [bench/stop-redis-server (constantly nil)]
    (bench/stop-redis-array [17001]))
  => (any nil? seq?))
