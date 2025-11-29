(ns net.resp.node-test
  (:use code.test)
  (:require [net.resp.node :refer :all :as node]
            [net.resp.connection :as conn]
            [net.resp.wire :as wire]
            [std.lib :as h]
            [std.concurrent :as cc]))

(defn create-node
  []
  (node/start-node nil 4456))

(defn create-conn
  []
  (conn/connection {:port 4456}))

(defmacro test-harness
  [& body]
  `(h/with:lifecycle [~'|node| {:start (create-node)
                                :stop node/stop-node}]
     (h/with:lifecycle [~'|conn| {:start (create-conn)
                                  :stop conn/connection:close}]
       ~@body)))

^{:refer net.resp.node/action-eval :added "3.0"}
(fact "creates result from an action "

  (action-eval nil ["PING"])
  => [:string "PONG"]

  (action-eval (fn [k args]
                 [:write (apply str args)])
               ["WRITE" 1 2 3])
  => [:write "123"])

^{:refer net.resp.node/action-write :added "3.0"}
(fact "writes an action to the connection"

  (test-harness
    (action-write |conn| [:write ["PING"]])
    => nil

    (wire/coerce (wire/read |conn|) :string)
    => "PONG"))

^{:refer net.resp.node/handle-multi :added "3.0"}
(fact "handles a call to start transaction"
  ^:hidden
  
  (test-harness
    (cc/req |conn| ["MULTI"])
    => "OK"

    (cc/req |conn| ["PING"])
    => "QUEUED"

    (cc/req |conn| ["PING"])
    => "QUEUED"

    (cc/req |conn| ["EXEC"])
    => ["PONG" "PONG"]))

^{:refer net.resp.node/handle-exec :added "3.0"}
(fact "handles a call to perform transaction")

^{:refer net.resp.node/handle-command :added "3.0"}
(fact "handles a command call")

^{:refer net.resp.node/handle-single :added "3.0"}
(fact "performs a single call")

^{:refer net.resp.node/handle-loop :added "3.0"}
(fact "performs a loop call")

^{:refer net.resp.node/start-node :added "3.0"}
(fact "starts the remote node")

^{:refer net.resp.node/stop-node :added "3.0"}
(fact "stops the remote node")
