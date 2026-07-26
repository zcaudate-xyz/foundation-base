tahto/runtime/basic/type_websocket_test.clj:1:(ns tahto.runtime.basic.type-websocket-test
tahto/runtime/basic/type_websocket_test.clj:2:  (:require [tahto.runtime.basic.server-websocket :as ws]
tahto/runtime/basic/type_websocket_test.clj:3:            [tahto.runtime.basic.type-basic :as basic]
tahto/runtime/basic/type_websocket_test.clj:4:            [tahto.runtime.basic.type-common :as common]
tahto/runtime/basic/type_websocket_test.clj:5:            [tahto.runtime.basic.type-websocket :refer :all]
            [std.lib.component :as component])
  (:use code.test))

tahto/runtime/basic/type_websocket_test.clj:9:^{:refer tahto.runtime.basic.type-websocket/start-websocket :added "4.0"}
(fact "starts bench and server for websocket runtime"
  (with-redefs [basic/start-basic (fn [rt f] (assoc rt :started true))]
    (start-websocket {:id "test" :lang :js}))
  => (contains {:started true}))

tahto/runtime/basic/type_websocket_test.clj:15:^{:refer tahto.runtime.basic.type-websocket/rt-websocket:create :added "4.0"}
(fact "creates a websocket runtime"
  (with-redefs [common/get-options (fn [& _] {})]
    (rt-websocket:create {:lang :js}))
  => map?)

tahto/runtime/basic/type_websocket_test.clj:21:^{:refer tahto.runtime.basic.type-websocket/rt-websocket :added "4.0"}
(fact "creates and start a websocket runtime"
  (with-redefs [rt-websocket:create (fn [m] m)
                component/start (fn [m] (assoc m :started true))]
    (rt-websocket {:lang :js}))
  => {:lang :js :started true})
