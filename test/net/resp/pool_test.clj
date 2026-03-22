(ns net.resp.pool-test
  (:require [net.resp.connection :as conn]
            [net.resp.node :as node]
            [net.resp.pool :refer :all]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.component :as component]
            [std.lib.foundation :as f])
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
  
  (component/with-lifecycle [|node| {:start  (create-node)
                             :stop   node/stop-node}]
    (component/with [|pool| (create-pool)]
      (pool:apply |pool| cc/req-fn [["PING"]])
      => "PONG")))

^{:refer net.resp.pool/wrap-pool :added "3.0"}
(fact "wraps a function taking pool" ^:hidden

  (component/with [|pool| (create-pool)]
    ((wrap-pool (fn [pool]
                  (component/started? pool)))
     {:pool |pool|}))
  => true)

^{:refer net.resp.pool/wrap-connection :added "3.0"}
(fact "wraps a function taking pool resource"
   ^:hidden
  
  (component/with-lifecycle [|node| {:start  (create-node)
                             :stop   node/stop-node}]
    (component/with [|pool| (create-pool)]
      ((wrap-connection
        (fn [connection]
          (f/string (conn/connection:request-single connection ["PING"]))))
       {:pool |pool|})))
   => "PONG")
