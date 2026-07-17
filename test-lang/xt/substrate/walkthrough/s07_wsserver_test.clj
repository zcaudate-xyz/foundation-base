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

^{:seedgen/root {:all true
                 :js   {:extra [[js.net.ws-native :as js-ws]]}
                 :lua  {:extra [[lua.net.ws-native :as lua-ws]]}
                 :python {:extra [[python.net.ws-native :as py-ws]]}
                 :ruby {:extra [[ruby.net.ws-native :as ruby-ws]]}
                 :dart {:extra [[dart.net.ws-native :as dart-ws]]}}}
(l/script- :js
  {:runtime :basic
   :require [[xt.substrate :as event-node]
             [xt.substrate.transport-websocket :as ws-transport]
             [xt.net.ws-native :as ws-native]
             ^{:seedgen/extra true}
             [js.net.ws-native :as js-ws]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(l/script- :lua
  {:runtime :nginx.instance
   :config {:program :resty}
   :require [[xt.substrate :as event-node]
             [xt.substrate.transport-websocket :as ws-transport]
             [xt.net.ws-native :as ws-native]
             [lua.net.ws-native :as lua-ws]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.substrate :as event-node]
             [xt.substrate.transport-websocket :as ws-transport]
             [xt.net.ws-native :as ws-native]
             [python.net.ws-native :as py-ws]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(wstest-restart)
          (l/rt:restart)]
  :teardown [(wstest-stop)
             (l/rt:stop)]})

(defn.js websocket-roundtrip-run [create-fn]
  (var node (event-node/node-create {"id" "node-client"}))
  (return
   (-> (event-node/attach-transport
        node
        "server"
        (ws-transport/websocket-endpoint {"create_fn" create-fn}))
       (promise/x:promise-then
        (fn [_]
          (return
           (event-node/request
            node
            "room/a"
            "demo/echo"
            ["node"]
            {"transport_id" "server"})))))))

^{:refer xt.substrate.walkthrough.s07-wsserver-test/websocket-roundtrip
  :added "4.1"}
(fact "a node websocket runtime can attach a live websocket transport and request over it"
  ^{:seedgen/base
    {:lua {:transform '{:js :lua js-ws/create lua-ws/create js-ws/connect-ws lua-ws/connect-ws}}
     :python {:transform '{:js :python js-ws/create py-ws/create js-ws/connect-ws py-ws/connect-ws}}
     :ruby {:transform '{:js :ruby js-ws/create ruby-ws/create js-ws/connect-ws ruby-ws/connect-ws}}
     :dart {:transform '{:js :dart js-ws/create dart-ws/create js-ws/connect-ws dart-ws/connect-ws}
            :input
            '(!.dt
              (-/websocket-roundtrip-run
               (fn []
                 (var client (dart-ws/create {"close_after_message" true}))
                 (return
                  (dart-ws/connect-ws
                   client
                   {"url" "ws://127.0.0.1:29632/"})))))}}}
  (notify/wait-on [:js 5000]
    (-> (-/websocket-roundtrip-run
         (fn []
           (var client (js-ws/create {}))
           (return
            (js-ws/connect-ws
             client
             {"url" "ws://127.0.0.1:29632/"}))))
        (promise/x:promise-then
         (fn [response]
           (repl/notify response)))
        (promise/x:promise-catch
         (fn [err]
           (repl/notify {"error" err})))))
  => (contains-in
      {"transport" "websocket"
       "runtime" "node"
       "action" "demo/echo"
       "args" ["node"]}))
