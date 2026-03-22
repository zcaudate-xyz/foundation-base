(ns xt.sys.conn-redis-test
  (:require [lib.redis.bench :as bench]
            [std.lang :as l]
            [std.lib.security :as security]
            [xt.lang.base-notify :as notify])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config  {:program :resty}
   :require [[xt.sys.conn-redis :as redis]
             [xt.lang.base-lib :as k]
             [lua.nginx.driver-redis :as lua-driver]
             [xt.lang.base-repl :as repl]]})

(l/script- :js
  {:runtime :basic
   :require [[xt.sys.conn-redis :as redis]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.driver-redis :as js-driver]]})

(l/script- :xtalk
  {:require [[xt.sys.conn-redis :as redis]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(bench/start-redis-array [17001])
             (l/rt:restart)]
  :teardown [(l/rt:stop)
             (bench/stop-redis-array [17001])]})

^{:refer xt.sys.conn-redis/connect :added "4.0"}
(fact "connects to a datasource"
  ^:hidden
  
  ;;
  ;; LUA
  ;;
  
  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   (redis/exec conn "ping" [] {}))
  => "PONG"

  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   (redis/exec conn "echo" ["hello"] {}))
  => "hello"

  ;;
  ;; JS
  ;;
  
  (notify/wait-on :js
    (redis/connect {:constructor js-driver/connect-constructor
                    :port 17001}
                   {:success (fn [conn]
                               (redis/exec conn "ping" []
                                           (repl/<!)))}))
  => "PONG"
  
  (notify/wait-on :js
    (redis/connect {:constructor js-driver/connect-constructor
                    :port 17001}
                   {:success (fn [conn]
                               (redis/exec conn "echo" ["hello"]
                                           (repl/<!)))}))
  => "hello")

^{:refer xt.sys.conn-redis/disconnect :added "4.0"}
(fact "disconnect redis")

^{:refer xt.sys.conn-redis/exec :added "4.0"}
(fact "executes a redis command"
  ^:hidden
  
  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   [(redis/exec conn "ping" [])
    (redis/exec conn "echo" ["hello"])])
  => ["PONG" "hello"]  

  (do
    (notify/wait-on :js
      (:= (!:G conn) (redis/connect {:constructor js-driver/connect-constructor
                                     :port 17001}
                                    (repl/<!))))
    
    [(notify/wait-on :js
       (redis/exec conn "ping" [] (repl/<!)))
     (notify/wait-on :js
       (. (redis/exec conn "echo" ["hello"])
          (then (repl/>notify))))])
  => ["PONG" "hello"])

^{:refer xt.sys.conn-redis/create-subscription :added "4.0"
  :setup [(bench/stop-redis-array [17001])
          (bench/start-redis-array [17001])
          (l/rt:restart)]}
(fact "creates a subscription given channel"
  ^:hidden
  
  (do (def ^:dynamic *res*
        (future
          (notify/wait-on [:lua 5000]
           (var conn (redis/connect {:constructor lua-driver/connect-constructor
                                     :port 17001}
                                    {}))
           (redis/create-subscription
            conn
            ["__TEST__"])
           (var '[res err] (lua-driver/read-reply conn))
           (repl/notify res))))
      (notify/wait-on :js
        (redis/connect {:constructor js-driver/connect-constructor
                        :port 17001}
                       {:success (fn [conn]
                                   (redis/exec conn "publish" ["__TEST__" "123"]
                                               (repl/<!)))}))
      @*res*
      (deref *res* 1000 :timeout))
  => ["message" "__TEST__" "123"])

^{:refer xt.sys.conn-redis/create-psubscription :added "4.0"
  :setup [(bench/stop-redis-array [17001])
          (bench/start-redis-array [17001])
          (l/rt:restart)]}
(fact "creates a pattern subscription given channel"
  ^:hidden

  (do (def ^:dynamic *res*
        (future
          (notify/wait-on [:lua 5000]
           (var conn (redis/connect {:constructor lua-driver/connect-constructor
                                     :port 17001}
                                    {}))
           (redis/create-psubscription
            conn
            ["*"])
           (var '[res err] (lua-driver/read-reply conn))
           (repl/notify res))))
      (notify/wait-on :js
        (redis/connect {:constructor js-driver/connect-constructor
                        :port 17001}
                       {:success (fn [conn]
                                   (redis/exec conn "publish" ["__TEST__" "123"]
                                               (repl/<!)))}))
      @*res*
      (deref *res* 1000 :timeout))
  => ["pmessage" "*" "__TEST__" "123"])

^{:refer xt.sys.conn-redis/eval-body :added "4.0"}
(fact "evaluates a the body"
  ^:hidden
  
  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   [(redis/eval-body conn {:body "return 1" }
                     []
                     {})
    (redis/eval-body conn {:body "return 0" }
                     []
                     {})
    (redis/eval-body conn {:body "return nil" }
                     []
                     {})
    (redis/eval-body conn {:body "return false" }
                     []
                     {})
    (redis/eval-body conn {:body "return true" }
                     []
                     {})
    (redis/eval-body conn {:body "return 'hello'"}
                     []
                     {})])
  => [1 0 nil nil 1 "hello"]

  
  (do
    (notify/wait-on :js
      (:= (!:G  conn) (redis/connect {:constructor js-driver/connect-constructor
                                      :port 17001}
                                       (repl/<!))))
    [(notify/wait-on :js
       (redis/eval-body conn {:body "return 1"}
                        []
                        (repl/<!)))
     (notify/wait-on :js
       (redis/eval-body conn {:body "return 0"}
                        []
                        (repl/<!)))
     (notify/wait-on :js
       (redis/eval-body conn {:body "return nil"}
                        []
                        (repl/<!)))
     (notify/wait-on :js
       (redis/eval-body conn {:body "return false"}
                        []
                        (repl/<!)))
     (notify/wait-on :js
       (redis/eval-body conn {:body "return true"}
                        []
                        (repl/<!)))
     (notify/wait-on :js
       (redis/eval-body conn {:body "return 'hello'"}
                        []
                        (repl/<!)))])
  => [1 0 nil nil 1 "hello"])

^{:refer xt.sys.conn-redis/eval-script :added "4.0"}
(fact "evaluates sha, then body if errored"
  ^:hidden
  
  (!.lua
   (var conn (redis/connect {:constructor lua-driver/connect-constructor
                             :port 17001}
                            {}))
   [(redis/exec conn "flushdb" [])
    (redis/eval-script conn {:sha (@! (security/sha1 "return 1"))
                             :body "return 1"}
                       [])])
  => ["OK" 1]

  (do
    (notify/wait-on :js
      (:= (!:G  conn) (redis/connect {:constructor js-driver/connect-constructor
                                      :port 17001}
                                     (repl/<!))))
    [(notify/wait-on :js
       (redis/exec conn "flushdb" [] (repl/<!)))
     (notify/wait-on :js
       (redis/eval-script conn {:sha (@! (security/sha1 "return 2"))
                                :body "return 2"}
                          []
                          (repl/<!)))])
  => ["OK" 2])
