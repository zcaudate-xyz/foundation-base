(ns xt.substrate.walkthrough.s07-wsserver-test
  (:use code.test)
  (:require [std.json :as json]
            [hara.lang :as l]
            [org.httpkit.server :as server]
            [xt.lang.common-notify :as notify]))

(do 
  (def ^:private +ws-port+ 29632)
  (def ^:private +ws-url+ (str "ws://127.0.0.1:" +ws-port+ "/"))
  (defonce ^:private +ws-server+ (atom nil))

  (defn wstest-start-fn [port runtime]
    (let [stop-fn
          (server/run-server
           (fn [request]
             (server/with-channel request channel
               (server/on-receive
                channel
                (fn [msg]
                  (let [{:strs [kind id action args space]} (json/read msg)]
                    (when (= kind "request")
                      (server/send!
                       channel
                       (json/write
                        {"kind" "response"
                         "id" (str "res-" id)
                         "status" "ok"
                         "reply_to" id
                         "space" space
                         "meta" {}
                         "data" {"transport" "websocket"
                                 "runtime" runtime
                                 "action" action
                                 "args" args}}))))))))
           {:port port})]
      {:port port
       :stop stop-fn}))

  (defn wstest-start
    []
    (reset! +ws-server+ (wstest-start-fn +ws-port+ "node")))
  
  (defn wstest-stop
    []
    (when-let [stop-fn (:stop @+ws-server+)]
      (stop-fn))
    (reset! +ws-server+ nil))
  
  (defn wstest-restart
    []
    (wstest-stop)
    (wstest-start)))


(l/script- :js
  {:runtime :websocket
   :require [[xt.substrate :as event-node]
             [xt.substrate.transport-websocket :as ws-transport]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(wstest-restart)
          (l/rt:restart)]
  :teardown [(wstest-stop)
             (l/rt:stop)]})

(fact "a node websocket runtime can attach a live websocket transport and request over it"
  (notify/wait-on [:js 5000]
    (var ws-module (require "ws"))
    (var WebSocket (or (. ws-module ["WebSocket"])
                       ws-module))
    (var node (event-node/node-create {"id" "node-client"}))
    (-> (event-node/attach-transport
         node
         "server"
         (ws-transport/websocket-endpoint
          {"url" "ws://127.0.0.1:29632/"
           "WebSocket" WebSocket}))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request
             node
             "room/a"
             "demo/echo"
             ["node"]
             {"transport_id" "server"}))))
        (promise/x:promise-then
         (fn [response]
           (return
            (promise/x:promise-then
             (event-node/detach-transport node "server")
             (fn [_]
               (repl/notify response))))))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err}))))
    true)
  => (contains-in
      {"transport" "websocket"
       "runtime" "node"
       "action" "demo/echo"
       "args" ["node"]}))
