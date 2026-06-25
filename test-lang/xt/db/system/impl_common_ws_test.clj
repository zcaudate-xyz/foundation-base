(ns xt.db.system.impl-common-ws-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.system.impl-common-ws-test :s common-ws]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.main-ws/create-ws-client :added "4.1"}
(fact "returns nil for unsupported websocket client types"

  (!.js
   (common-ws/create-ws-client {}))
  => nil)

^{:refer xt.db.system.main-ws/create-ws-client.ws :added "4.1"}
(fact "creates a websocket client"

  (!.js
   (var client (main-ws/create-ws-client {"host" "example.com"}))
   {"::" (xt/x:get-key client "::")
    "defaults" (xt/x:get-key client "defaults")})
  => {"::" "js.net.ws_native/WebsocketClient"
      "defaults" {"host" "example.com"}})
