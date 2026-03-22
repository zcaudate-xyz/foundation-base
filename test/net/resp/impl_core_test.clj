(ns net.resp.impl-core-test
  (:require [lib.redis.bench :as bench]
            [net.resp.connection :as conn]
            [net.resp.wire :refer :all]
            [std.concurrent :as cc]
            [std.concurrent.request :as req]
            [std.lib.component]
            [std.lib.foundation]
            [std.lib.future]
            [std.lib.time])
  (:use [code.test])
  (:refer-clojure :exclude [read]))



(fact:global
 {:setup [(bench/start-redis-array [17001])]
  :teardown [(bench/stop-redis-array [17001])]})

(defn setup-conn
  ([]
   (let [conn (conn/connection {:port 17001})]
     (doto conn (cc/req ["FLUSHDB"])))))

(defmacro test-harness
  [& body]
  `(std.lib.component/with-lifecycle [~'|conn| {:start (setup-conn)
                                :stop conn/connection:close}]
                     ~@body))


^{:refer net.resp.wire/call :added "3.0"
  :adopt true}
(fact "calls a redis command with additional processing"
  ^:hidden

  (test-harness
    (-> (call |conn| ["SET" "TEST:A" (serialize-bytes {:a 1 :b 2} :json)])
        (std.lib.foundation/string)))
  => "OK")

^{:refer net.resp.wire/read :added "3.0"
  :adopt true}
(fact "reads from a client or connection"
  ^:hidden

  (test-harness
    (-> (doto |conn|
          (write ["SET" "TEST:A" (serialize-bytes {:a 1 :b 2} :json)])
          (write ["GET" "TEST:A"]))
        ((juxt (comp std.lib.foundation/string read)
               (comp std.lib.foundation/string read)))))
  => ["OK" "{\"a\":1,\"b\":2}"])

^{:refer std.concurrent.request/request-bulk :added "3.0" :adopt true}
(fact "returns a bulked request"
  ^:hidden

  (test-harness
    (->> (std.concurrent.request/request-bulk |conn|
                                              [["SET" "TEST:A" (serialize-bytes {:a 1} :json)]
                                               ["SET" "TEST:B" (serialize-bytes {:b 2} :json)]
                                               ["SET" "TEST:C" (serialize-bytes {:c 3} :json)]])
         (mapv std.lib.foundation/string))
    => ["OK" "OK" "OK"])

  (let [|commands| [["SET" "TEST:A" (serialize-bytes {:a 1} :json)]
                    ["SET" "TEST:B" (serialize-bytes {:b 2} :json)]
                    ["SET" "TEST:C" (serialize-bytes {:c 3} :json)]
                    ["SET" "TEST:D" (serialize-bytes {:c 4} :json)]
                    ["SET" "TEST:E" (serialize-bytes {:c 5} :json)]
                    ["SET" "TEST:F" (serialize-bytes {:c 6} :json)]]]
    (test-harness
     [(-> (std.concurrent.request/request-bulk |conn| |commands|)
          (std.lib.time/bench-ns))
      (-> (doseq [cmd |commands|]
            (std.concurrent.request/request-single |conn| cmd))
          (std.lib.time/bench-ns))]))
  => vector?)

^{:refer std.concurrent.request/req:single-complete :added "3.0"
  :adopt true}
(fact "adds extra processing to output"

  (test-harness
    (std.lib.foundation/->> (std.concurrent.request/request-single |conn| ["COMMAND" "INFO" "GET"])
           (std.concurrent.request/process-single |conn| % {:string true})
           (req/req:single-complete {:chain [ffirst]}))
    => "get"

    (let [received (std.lib.future/incomplete)]
      (std.lib.foundation/->> (std.concurrent.request/request-single |conn| ["COMMAND" "INFO" "GET"])
             (std.concurrent.request/process-single |conn| % {:string true})
             (req/req:single-complete {:async true
                                       :received received
                                       :final  (std.lib.future/future:chain received [ffirst])})
             (deref)))
    => "get"))

^{:refer std.concurrent.request/req:single :added "3.0"
  :adopt true}
(fact "requests a redis command with additional processing" ^:hidden

  (test-harness
    (req/req:single |conn| ["SET" "TEST:A" (serialize-bytes {:a 1 :b 2} :json)])
    => "OK"

    (req/req:single |conn| ["GET" "TEST:A"] {:format :json
                                             :deserialize true
                                             :chain [(comp set keys)]})
    => #{:b :a}

    @(req/req:single |conn| ["GET" "TEST:A"] {:format :json
                                              :async true
                                              :deserialize true
                                              :chain [(comp set keys)]})
    => #{:b :a}))

^{:refer std.concurrent.request/req:unit :added "3.0"
  :adopt true}
(fact "constructs a bulk"

  (test-harness
    (req/bulk |conn|
      (fn []
        (req/req:unit |conn| ["SET" "TEST:A" "1"]))) ^:hidden
    => ["OK"]

    @(req/bulk |conn|
       (fn []
         (req/req:unit |conn| ["SET" "TEST:A" "1"])
         (req/req:unit |conn| ["SET" "TEST:B" "1"]))
       {:async true
        :chain [second keyword]})
    => :OK))
