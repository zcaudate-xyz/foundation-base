(ns tahto.runtime.basic.server-websocket-test
  (:use code.test)
  (:require [org.httpkit.server :as server]
            [tahto.runtime.basic.server-websocket :refer :all]
            [tahto.runtime.basic.type-bench :as bench]
            [std.json :as json]
            [tahto.core :as l]))

(l/script- :js
  {:runtime :websocket
   :config {:bench true}
   :test-mode true})

(fact:global
 {:setup [(l/rt:restart :js)]
  :teardown [(l/rt:stop)]})

^{:refer tahto.runtime.basic.server-websocket/raw-eval-websocket-server :added "4.0"
  :setup [(l/rt:restart :js)]}
(fact "raw eval for websocket connection"

  (!.js (+ 1 2 3))
  => (any 6 {:status "not-connected"}))

^{:refer tahto.runtime.basic.server-websocket/create-websocket-handler-receive :added "4.0"}
(fact "gets the websocket handler"
  (let [p (promise)
        return (atom {"id-1" p})
        channel (atom nil)]
    (create-websocket-handler-receive (json/write {:id "id-1" :data "ok"}) return channel)
    (deref p 100 :timeout) => {:id "id-1" :data "ok"}))

^{:refer tahto.runtime.basic.server-websocket/create-websocket-handler :added "4.0"}
(fact "creates the websocket handler"
  (create-websocket-handler (atom nil) (atom {}) (promise))
  => fn?)

^{:refer tahto.runtime.basic.server-websocket/create-websocket-server :added "4.0"}
(fact "creates the websocket server"
  (let [server (create-websocket-server "test" :js 0 nil)]
    server => map?
    (if-let [stop-fn @(:stop server)]
      (stop-fn))))
