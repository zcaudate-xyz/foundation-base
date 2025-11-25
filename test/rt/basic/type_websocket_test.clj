(ns rt.basic.type-websocket-test
  (:use code.test)
  (:require [rt.basic.type-websocket :refer :all]
            [rt.basic.type-basic :as basic]
            [rt.basic.server-websocket :as ws]
            [rt.basic.type-common :as common]
            [std.lib :as h]))

^{:refer rt.basic.type-websocket/start-websocket :added "4.0"}
(fact "starts bench and server for websocket runtime"
  (with-redefs [basic/start-basic (fn [rt f] (assoc rt :started true))]
    (start-websocket {:id "test" :lang :js}))
  => (contains {:started true}))

^{:refer rt.basic.type-websocket/rt-websocket:create :added "4.0"}
(fact "creates a websocket runtime"
  (with-redefs [common/get-options (fn [& _] {})]
    (rt-websocket:create {:lang :js}))
  => map?)

^{:refer rt.basic.type-websocket/rt-websocket :added "4.0"}
(fact "creates and start a websocket runtime"
  (with-redefs [rt-websocket:create (fn [m] m)
                h/start (fn [m] (assoc m :started true))]
    (rt-websocket {:lang :js}))
  => {:lang :js :started true})
