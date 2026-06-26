(ns hara.runtime.basic.impl.process-js-playground-test
  (:require [hara.common.util :as ut]
            [hara.lang :as l]
            [hara.runtime.basic.type-playground :as playground]
            [std.json :as json])
  (:use code.test)
  (:import (java.net URI)
           (java.net.http HttpClient WebSocket WebSocket$Listener)))

(l/script- :js
  {:runtime :playground
   :config {:port 0}})

(defn- connect-mock-browser
  "connects a fake browser WebSocket client to the active runtime's /ws endpoint.

   The mock replies to every eval request with the fixed numeric result 6."
  []
  (let [rt (ut/lang-rt :js)
        port (:port rt)
        client (HttpClient/newHttpClient)
        connected (promise)
        listener (reify WebSocket$Listener
                   (onOpen [_ ws]
                     (.request ws 1)
                     (deliver connected true))
                   (onText [_ ws data _last]
                     (let [msg (json/read (str data) json/+keyword-case-mapper+)
                           id (:id msg)]
                       (.sendText ws
                                  (json/write {:id id :status "ok" :body 6})
                                  true))
                     (.request ws 1))
                   (onError [_ ws err]
                     (deliver connected err)))]
    {:ws (.get (.buildAsync (.newWebSocketBuilder client)
                            (URI. (str "ws://localhost:" port "/ws"))
                            listener))
     :connected connected}))

^{:refer hara.runtime.basic.impl.process-js-playground/CANARY :adopt true :added "4.0"}
(fact "EVALUATE js code through a browser websocket"

  (let [{:keys [ws connected]} (connect-mock-browser)]
    (deref connected 1000 false)
    => true

    (!.js (+ 1 2 3))
    => 6

    (.sendClose ^WebSocket ws WebSocket/NORMAL_CLOSURE "done")))
