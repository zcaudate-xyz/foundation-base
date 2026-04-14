(ns net.resp.node-test
  (:require [net.resp.connection :as conn]
            [net.resp.node :as node :refer :all]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.component :as component])
  (:use code.test))

(defn create-node
  []
  (node/start-node nil 4456))

(defn create-conn
  []
  (conn/connection {:port 4456}))

(defmacro test-harness
  [& body]
  `(component/with-lifecycle [~'|node| {:start (create-node)
                                :stop node/stop-node}]
     (component/with-lifecycle [~'|conn| {:start (create-conn)
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
(fact "handles a call to perform transaction"
  (let [transact (atom {:enabled true
                        :queue [["PING"] ["ECHO" "1"]]})]
    (with-redefs [action-write (fn [_ action] action)]
      [(handle-exec nil transact nil)
       @transact]))
  => [[:write ["PONG" "1"]]
      {:enabled false :queue []}])

^{:refer net.resp.node/handle-command :added "3.0"}
(fact "handles a command call"
  (let [transact (atom {:enabled false :queue []})]
    (with-redefs [action-write (fn [_ action] action)]
      [(handle-command nil transact nil ["PING"])
       @transact]))
  => [[:string "PONG"]
      {:enabled false :queue []}])

^{:refer net.resp.node/handle-single :added "3.0"}
(fact "performs a single call"
  (with-redefs [conn/connection:read (fn [_] ["PING"])
                action-write (fn [_ action] action)
                std.lib.env/close (fn [_] nil)]
    (handle-single nil (atom {:enabled false :queue []}) nil false))
  => [:string "PONG"])

^{:refer net.resp.node/handle-loop :added "3.0"}
(fact "performs a loop call"
  handle-loop
  => fn?)

^{:refer net.resp.node/start-node :added "3.0"}
(fact "starts the remote node"
  (let [node (start-node nil 4457)]
    (try
      {:listening (not (.isClosed ^java.net.ServerSocket (:server node)))
       :has-thread (some? (:thread node))
       :has-executor (some? (:executor node))}
      (finally
        (stop-node node))))
  => {:listening true
      :has-thread true
      :has-executor true})

^{:refer net.resp.node/stop-node :added "3.0"}
(fact "stops the remote node"
  (let [node (start-node nil 4458)]
    (stop-node node))
  => nil)
