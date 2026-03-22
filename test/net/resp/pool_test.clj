(ns net.resp.pool-test
  (:require [net.resp.connection :as conn]
            [net.resp.node :as node]
            [net.resp.pool :refer :all]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.component]
            [std.lib.foundation])
  (:use code.test))

(defn create-node
  []
  (node/start-node nil 4457))

(defn create-pool
  []
  (pool {:tag "test.pool"
         :path [:test :pool]
         :port 4457}))

^{:refer net.resp.pool/pool :added "3.0"}
(fact "creates the connection pool" ^:hidden
  ^:hidden
  
  (pool {:tag "test.pool"
         :path [:test :pool]
         :port 4455})
  => cc/pool?)

^{:refer net.resp.pool/pool:apply :added "3.0"}
(fact "applys a function to connection arguments" ^:hidden
  ^:hidden
  
  (std.lib.component/with-lifecycle [|node| {:start  (create-node)
                             :stop   node/stop-node}]
    (std.lib.component/with [|pool| (create-pool)]
      (pool:apply |pool| cc/req-fn [["PING"]])
      => "PONG")))

^{:refer net.resp.pool/wrap-pool :added "3.0"}
(fact "wraps a function taking pool" ^:hidden

  (std.lib.component/with [|pool| (create-pool)]
    ((wrap-pool (fn [pool]
                  (std.lib.component/started? pool)))
     {:pool |pool|}))
  => true)

^{:refer net.resp.pool/wrap-connection :added "3.0"}
(fact "wraps a function taking pool resource"
   ^:hidden
  
  (std.lib.component/with-lifecycle [|node| {:start  (create-node)
                             :stop   node/stop-node}]
    (std.lib.component/with [|pool| (create-pool)]
      ((wrap-connection
        (fn [connection]
          (std.lib.foundation/string (conn/connection:request-single connection ["PING"]))))
       {:pool |pool|})))
   => "PONG")
