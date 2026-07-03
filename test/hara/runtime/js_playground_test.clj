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

^{:refer hara.runtime.js-playground/start-js-playground :added "4.0"}
(fact "restarting the playground reuses the same port"

  (let [rt (rt-js-playground {:lang :js :port 0})
        first-port (:port rt)]
    (try
      (component/stop rt)
      (let [rt2 (component/start rt)]
        (try
          (:port rt2) => first-port
          (finally
            (component/stop rt2)))))))

^{:refer hara.runtime.js-playground/playground-client-script :added "4.0"}
(fact "client script exposes a queryable PLAYGROUND API"

  (let [script (playground-client-script)]
    script => #"PLAYGROUND\[\"getMessages\"\]"
    script => #"PLAYGROUND\[\"getStatus\"\]"
    script => #"PLAYGROUND\[\"switchTab\"\]"
    script => #"PLAYGROUND\[\"createTab\"\]"
    script => #"PLAYGROUND\[\"closeTab\"\]"
    script => #"PLAYGROUND\[\"setTabContent\"\]"))

^{:refer hara.runtime.js-playground/page-html :added "4.1"}
(fact "page-html renders the playground page with config and scripts"

  (page-html {:title "Custom Playground"
              :tabs [{:id "stage" :label "Stage"}
                     {:id "repl" :label "REPL"}]})
  => #"<!doctype html>"

  (page-html {:title "Custom Playground"})
  => #"<title>Custom Playground</title>"

  (page-html {:title "Custom Playground"})
  => #"window\.PLAYGROUND_CONFIG="

  (page-html {:title "Custom Playground"})
  => #"<script type=\"module\">")

^{:refer hara.runtime.js-playground/stop-js-playground :added "4.1"}
(fact "stop-js-playground stops the server and returns the runtime"

  (let [rt (rt-js-playground {:lang :js :port 0})]
    (try
      rt => (contains {:port integer?})
      (stop-js-playground rt) => rt
      (finally
        ;; already stopped
        ))))

^{:refer hara.runtime.js-playground/invoke-ptr-js-playground :added "4.1"}
(fact "invoke-ptr evaluates through a connected browser"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))
        {:keys [ws connected]} (connect-mock-browser (:port rt))]
    (try
      (deref connected 1000 false)
      => true

      (invoke-ptr-js-playground rt identity [1])
      => 6

      (finally
        (.sendClose ^WebSocket ws WebSocket/NORMAL_CLOSURE "done")
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/rt-js-playground-string :added "4.1"}
(fact "rt-js-playground-string formats the runtime as a readable tag"

  (rt-js-playground-string {:lang :js
                            :id "hello"
                            :host "127.0.0.1"
                            :port 1234})
  => "#rt.js-playground[:js \"hello\" \"127.0.0.1\" 1234]")

^{:refer hara.runtime.js-playground/play-file :added "4.1"}
(fact "play-file joins paths under the runtime root"

  (play-file {:root "/tmp/foo"} "bar" "baz.js")
  => "/tmp/foo/bar/baz.js")

^{:refer hara.runtime.js-playground/play-script :added "4.1"}
(fact "play-script emits js and writes a hashed script into the served root"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))]
    (try
      (play-script rt ['(+ 1 2)])
      => #"\.js$"

      (play-script rt ['(+ 1 2)] true)
      => #"1 \+ 2"
      (finally
        (component/stop rt)))))

^{:refer hara.runtime.js-playground/play-page :added "4.1"}
(fact "play-page writes a hashed html page into the served root"

  (let [rt (component/start (rt-js-playground:create {:lang :js :port 0}))]
    (try
      (play-page rt {:title "Custom Page"})
      => #"page-.*\.html"

      (play-page rt {:title "Custom Page"} true)
      => #"<title>Custom Page</title>"
      (finally
        (component/stop rt)))))