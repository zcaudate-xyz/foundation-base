(ns hara.runtime.js-playground-test
  (:require [hara.runtime.js-playground :refer :all]
            [std.json :as json]
            [std.lib.component :as component]
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

^{:refer hara.runtime.js-playground/rt-js-playground:create :added "4.0"}
(fact "creates a js playground runtime"

  (rt-js-playground:create {:lang :js})
  => (contains {:lang :js
                :runtime :playground
                :id string?}))

^{:refer hara.runtime.js-playground/start-js-playground :added "4.0"}
(fact "starts a js playground server"

  (let [rt (start-js-playground (rt-js-playground:create {:lang :js :port 0}))]
    (try
      rt => (contains {:port integer?
                       :host string?
                       :root string?
                       :channel any})
      (play-url rt) => #"http://\d+\.\d+\.\d+\.\d+:\d+/index\.html"
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/play-url :added "4.0"}
(fact "play-url is reachable while the server is running"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))
        url (play-url rt)]
    (try
      url => #"http://\d+\.\d+\.\d+\.\d+:\d+/index\.html"
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/raw-eval-js-playground :added "4.0"}
(fact "raw-eval returns not-connected when no browser is attached"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))]
    (try
      (protocol.context/-raw-eval rt "1 + 2 + 3;")
      => {:status "not-connected"}
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/raw-eval-js-playground :added "4.0"}
(fact "raw-eval evaluates through a connected browser"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))
        {:keys [ws connected]} (connect-mock-browser (:port rt))]
    (try
      (deref connected 1000 false)
      => true

      (protocol.context/-raw-eval rt "1 + 2 + 3;")
      => 6

      (finally
        (.sendClose ^WebSocket ws WebSocket/NORMAL_CLOSURE "done")
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/rt-js-playground :added "4.0"}
(fact "creates and starts a js playground runtime"

  (let [rt (rt-js-playground {:lang :js :port 0})]
    (try
      rt => (contains {:port integer?
                       :channel any})
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/playground-client-script :added "4.0"}
(fact "playground client script is emitted as JavaScript"

  (playground-client-script)
  => #"function App")

^{:refer hara.runtime.js-playground/playground-client-script :added "4.0"}
(fact "client script is generated from the js-playground-client namespace"

  (let [script (playground-client-script)]
    script => #"import React from 'https://esm.sh/react@18'"
    script => #"import ReactDOM from 'https://esm.sh/react-dom@18/client'"
    script => #"function run_eval"
    script => #"function mountf"
    script => #"return_eval"))

^{:refer hara.runtime.js-playground/page-html :added "4.0"}
(fact "default page contains the react split-screen structure"

  (let [page (page-html {})]
    page => #"<div id=\"root\"></div>"
    page => #"react@18"
    page => #"window.PLAYGROUND"
    page => #"\"id\":\"stage\""
    page => #"\"id\":\"sidebar\""))

^{:refer hara.runtime.js-playground/play-script :added "4.0"}
(fact "play-script transpiles hiccup vectors to React.createElement by default"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))]
    (try
      (play-script rt '[(defn.js hello [] [:div "hello"])] true)
      => #"React\.createElement\(\"div\"[\s\S]*\"hello\"\)"
      (finally
        (component/stop rt)))))
