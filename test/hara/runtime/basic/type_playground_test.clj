(ns hara.runtime.basic.type-playground-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-playground :refer :all]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.network :as network]
            [std.protocol.context :as protocol.context])
  (:use code.test)
  (:import (java.net URI)
           (java.net.http HttpClient WebSocket WebSocket$Listener)))

(defn- connect-mock-browser
  "connects a fake browser WebSocket client to the runtime's /ws endpoint.

   The mock replies to every eval request with the fixed numeric result 6 so
   that `-raw-eval` can be exercised without a real browser."
  [port]
  (let [client (HttpClient/newHttpClient)
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

^{:refer hara.runtime.basic.type-playground/rt-playground:create :added "4.0"}
(fact "creates a playground websocket runtime"

  (rt-playground:create {:lang :js})
  => (contains {:lang :js
                :runtime :playground
                :id string?}))

^{:refer hara.runtime.basic.type-playground/start-playground :added "4.0"}
(fact "starts a playground websocket server"

  (let [rt (start-playground (rt-playground:create {:lang :js :port 0}))]
    (try
      rt => (contains {:port integer?
                       :host string?
                       :root string?
                       :channel any})
      (play-url rt) => #"http://\d+\.\d+\.\d+\.\d+:\d+/index\.html"
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.basic.type-playground/play-url :added "4.0"}
(fact "play-url is reachable while the server is running"

  (let [rt (component/start (rt-playground:create {:lang :js :port 0}))
        url (play-url rt)]
    (try
      url => #"http://\d+\.\d+\.\d+\.\d+:\d+/index\.html"
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.basic.type-playground/raw-eval-playground :added "4.0"}
(fact "raw-eval returns not-connected when no browser is attached"

  (let [rt (component/start (rt-playground:create {:lang :js :port 0}))]
    (try
      (protocol.context/-raw-eval rt "1 + 2 + 3;")
      => {:status "not-connected"}
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.basic.type-playground/raw-eval-playground :added "4.0"}
(fact "raw-eval evaluates through a connected browser"

  (let [rt (component/start (rt-playground:create {:lang :js :port 0}))
        {:keys [ws connected]} (connect-mock-browser (:port rt))]
    (try
      (deref connected 1000 false)
      => true

      (protocol.context/-raw-eval rt "1 + 2 + 3;")
      => 6

      (finally
        (.sendClose ^WebSocket ws WebSocket/NORMAL_CLOSURE "done")
        (component/stop rt)))))

^{:refer hara.runtime.basic.type-playground/rt-playground :added "4.0"}
(fact "creates and starts a playground websocket runtime"

  (let [rt (rt-playground {:lang :js :port 0})]
    (try
      rt => (contains {:port integer?
                       :channel any})
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.basic.type-common/valid-context! :added "4.0"}
(fact "playground is a valid runtime context"

  (common/valid-context! :playground)
  => nil)
