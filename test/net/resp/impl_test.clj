(ns net.resp.impl-test
  (:require [net.resp.connection :as conn]
            [net.resp.node :as node]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.concurrent.request :as req]
            [std.lib.component]
            [std.lib.foundation]
            [std.lib.future])
  (:use code.test)
  (:refer-clojure :exclude [read]))

(defn create-node
  []
  (node/start-node nil 4456))

(defn create-conn
  []
  (conn/connection {:port 4456}))

(defmacro test-harness
  [& body]
  `(std.lib.component/with-lifecycle [~'|node| {:start (create-node)
                                :stop node/stop-node}]
     (std.lib.component/with-lifecycle [~'|conn| {:start (create-conn)
                                  :stop conn/connection:close}]
       ~@body)))

^{:refer net.resp.wire/call :added "3.0"
   :adopt true}
(fact "writes a command and waits for reply"

  (test-harness
    (std.lib.foundation/string (wire/call |conn| ["PING"])))
  => "PONG")

^{:refer net.resp.wire/read :added "3.0"
   :adopt true}
(fact "reads from the socket"

  (test-harness
    (wire/write |conn| ["PING"])
    (std.lib.foundation/string (wire/read |conn|)))
  => "PONG")

^{:refer net.resp.wire/write :added "3.0"
  :adopt true}
(fact "writes without reading"

  (test-harness
    (wire/write |conn| ["PING"])
    (wire/write |conn| ["PING"])

    (mapv std.lib.foundation/string [(wire/read |conn|)
                    (wire/read |conn|)]))
  => ["PONG" "PONG"])

^{:refer net.resp.wire/close :added "3.0"
   :adopt true}
(fact "closes the connection"

  (test-harness
    (wire/close |conn|)
    (wire/write |conn|))
  => (throws))

^{:refer std.concurrent.request/request-single :added "3.0"
   :adopt true}
(fact "requests multiple commands"

  (test-harness
    (-> (std.concurrent.request/request-single |conn| ["PING"])
        std.lib.foundation/string))
  => "PONG")

^{:refer std.concurrent.request/request-bulk :added "3.0"
   :adopt true}
(fact "requests multiple commands"

  (test-harness
    (->> (std.concurrent.request/request-bulk |conn| [["PING"]
                                                      ["PING"]])
         (map std.lib.foundation/string)))
  => ["PONG" "PONG"])

^{:refer std.concurrent.request/req:single :added "3.0"
    :adopt true}
(fact "Single execution"

  (test-harness
    (req/req:single |conn| ["PING"] {})
    => "PONG"

    (req/req:single |conn| ["PING"] {:format :edn
                                     :deserialize true})
    => 'PONG

    (req/req:single |conn| ["PING"] {:chain [vector]})
    => ["PONG"]))

^{:refer std.concurrent.request/req:single.meta :added "3.0"
   :adopt true}
(fact "Single execution with meta on the command"

  (test-harness
    (req/req:single |conn| ["PING"] nil)
    => "PONG"

    (req/req:single |conn| ["PING"] {:format :edn
                                     :deserialize true})
    => 'PONG

    (req/req:single |conn| ["PING"] {:chain [vector]})
    => ["PONG"]))

^{:refer std.concurrent.request/req:single.async :added "3.0"
   :adopt true}
(fact "Single Async Execution"

  (test-harness
    @(req/req:single |conn| ["PING"] {:async true})
    => "PONG"

    @(req/req:single |conn| ["PING"] {:async true
                                      :format :edn
                                      :deserialize true})
    => 'PONG

    @(req/req:single |conn| ["PING"] {:async true
                                      :chain [vector]})
    => ["PONG"]))

^{:refer std.concurrent.request/req:single.chain :added "3.0"
  :adopt true}
(fact "Chained execution"

  (test-harness
    (req/req:single |conn| ["PING"] {:chain   [read-string
                                               vector]})
    => '[PONG]

    @(req/req:single |conn| ["PING"] {:async true
                                      :chain   [read-string
                                                vector]})
    => '[PONG]))

^{:refer std.concurrent.request/request-bulk :added "3.0"
   :adopt true}
(fact "constructs a bulk request"

  (test-harness
    (->> (std.concurrent.request/request-bulk |conn| [["ECHO" "1"]
                                                      ["ECHO" "2"]
                                                      ["ECHO" "3"]])
         (map std.lib.foundation/string)))
  => ["1" "2" "3"])

^{:refer std.concurrent.request/req :added "3.0"
   :adopt true}
(fact "execution in bulked or normal mode" ^:hidden

  (test-harness
    (cc/req |conn| ["ECHO" "OK"])
    => "OK"

    (cc/req |conn| ["ECHO" "OK"] {:string false})
    => "OK"

    (cc/req |conn| ["SET" "OK"] {:string false})
    => Throwable

    (cc/req |conn| ["ECHO" "OK"] {:async true})
    => std.lib.future/future?))

^{:refer std.concurrent.request/bulk :added "3.0"
  :adopt true}
(fact "runs multiple commands at once"

  (test-harness
    (cc/bulk |conn|
      (fn []
        (cc/req |conn| ["ECHO" "OK"])
        (cc/req |conn| ["ECHO" "OK"])))
    => ["OK" "OK"]

    (cc/bulk |conn|
      (fn []
        (cc/req |conn| ["ECHO" "1"])
        (cc/req |conn| ["ECHO" "2"]))
      {:chain [(partial map std.lib.foundation/parse-long)]})
    => [1 2]))

^{:refer std.concurrent.request/transact :added "3.0"
  :adopt true}
(fact "transact function for atomic operations"

  (test-harness
    ;; Without Bulk
    (->> (cc/transact |conn|
           (fn []
             (cc/req |conn| ["PING"])
             (cc/req |conn| ["PING"])))
         (mapv deref))
    => ["PONG" "PONG"]

    ;; With Bulk

    (-> (cc/bulk |conn|
          (fn []
            (cc/req |conn| ["PING"] {:async true})
            (cc/transact |conn|
              (fn []
                (cc/req |conn| ["PING"] {:async true})
                (cc/req |conn| ["PING"] {:async true}))))))
    => ["PONG" "OK" "PONG" "PONG" ["PONG" "PONG"]])

  (test-harness
  ;; Multiple in Bulk

  (cc/bulk |conn|
    (fn []
      (cc/transact |conn|
        (fn []
          (cc/req |conn| ["PING"] {:async true})
          (cc/req |conn| ["PING"] {:async true})))
      (cc/transact |conn|
        (fn []
          (cc/req |conn| ["PING"] {:async true})
          (cc/req |conn| ["PING"] {:async true})))))
  => ["OK" "PONG" "PONG" ["PONG" "PONG"] "OK" "PONG" "PONG" ["PONG" "PONG"]])


  (test-harness
    ;; Bulk in Transaction    
    (->> (cc/transact |conn|
           (fn []
             (cc/bulk |conn|
               (fn []
                 (cc/req |conn| ["PING"] {:process vector})
                 (cc/req |conn| ["PING"])))
             (cc/bulk |conn|
               (fn []
                 (cc/req |conn| ["PING"] {:process vector})
                 (cc/req |conn| ["PING"])))))
         (map deref))
    =>  ["PONG" "PONG" "PONG" "PONG"]))

^{:refer std.concurrent.request/transact.2 :added "3.0"
  :adopt true}
(fact "More Transact"

  (test-harness
    ;; Nested Transactions in Bulk
    (let [-p- (promise)]
      [(->> (cc/transact |conn|
              (fn []
                (cc/bulk |conn|
                  (fn []
                    (deliver -p-
                             (cc/transact |conn|
                               (fn []
                                 (cc/req |conn| ["PING"])
                                 (cc/req |conn| ["PING"]))))))
                (cc/bulk |conn|
                  (fn []
                    (cc/req |conn| ["PING"])
                    (cc/req |conn| ["PING"])))))
            (map deref))
       (mapv deref @-p-)])
    => [["PONG" "PONG" "PONG" "PONG"]
        ["PONG" "PONG"]])

  (test-harness
    ;; Nested Bulk in Transactions
    (cc/bulk |conn|
      (fn []
        (cc/transact |conn|
          (fn []
            (cc/bulk |conn|
              (fn []
                (cc/req |conn| ["PING"])
                (cc/req |conn| ["PING"])))))
        (cc/transact |conn|
          (fn []
            (cc/req |conn| ["PING"])
            (cc/req |conn| ["PING"])))))
    =>  '["OK" ["PONG" "PONG"] ("PONG" "PONG")
          "OK" "PONG" "PONG" ("PONG" "PONG")]))

^{:refer std.concurrent.request/bulk:transact :added "3.0"
  :adopt true}
(fact "creates a single bulk transaction"

  (test-harness
    (cc/bulk:transact |conn|
      (fn []
        (cc/req |conn| ["PING"])
        (cc/req |conn| ["PING"])))
    =>  ["PONG" "PONG"]))
