(ns xt.event.node-transport-websocket-e2e-test
  (:use code.test)
  (:require [cheshire.core :as json]
            [hara.lang :as l]
            [hara.runtime.chromedriver :as chromedriver]
            [org.httpkit.server :as server]
            [xt.lang.common-notify :as notify]))

(def ^:private +ws-port+ 29631)
(def ^:private +ws-url+ (str "ws://127.0.0.1:" +ws-port+ "/"))
(defonce ^:private +ws-server+ (atom nil))

(defn- start-test-ws-server [port runtime]
  (let [stop-fn
        (server/run-server
         (fn [request]
           (server/with-channel request channel
             (server/on-receive
              channel
              (fn [msg]
                (let [{:strs [kind id action args space]} (json/parse-string msg)]
                  (when (= kind "request")
                    (server/send!
                     channel
                     (json/generate-string
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

(l/script- :js
  {:runtime :chromedriver.instance
   :require [[xt.event.node :as event-node]
             [xt.event.node-transport-websocket :as ws-transport]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
  {:setup [(reset! +ws-server+ (start-test-ws-server +ws-port+ "chromedriver"))
           (l/rt:restart :js)
           (chromedriver/goto (str "http://127.0.0.1:" (:http-port (l/default-notify)) "/")
                              4000)]
   :teardown [(when-let [stop-fn (:stop @+ws-server+)]
                (stop-fn))
              (reset! +ws-server+ nil)
              (l/rt:stop)]})

(fact "a chromedriver runtime can attach a live websocket transport and request over it"
  (notify/wait-on [:js 5000]
    (var node (event-node/node-create {"id" "browser-client"}))
    (-> (event-node/attach-transport
         node
         "server"
         (ws-transport/websocket-endpoint
          "ws://127.0.0.1:29631/"))
        (promise/x:promise-then
         (fn [_]
           (return
            (event-node/request
             node
             "room/a"
             "demo/echo"
             ["browser"]
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
       "runtime" "chromedriver"
       "action" "demo/echo"
       "args" ["browser"]}))
