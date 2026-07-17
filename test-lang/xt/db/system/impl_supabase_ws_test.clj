^{:seedgen/skip true}
(ns xt.db.system.impl-supabase-ws-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[js.net.ws-native :as js-ws]
             [xt.db.system.impl-supabase-ws :as supabase-ws]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.system.impl-supabase-ws/create-ws-client :added "4.1"}
(fact "creates a websocket client"

  (!.js
   (var factory {"::/override"
                 {"create_ws_client"
                  (fn [_ defaults]
                    (return (js-ws/create defaults)))}})
   (var client (supabase-ws/create-ws-client factory {"host" "example.com"}))
   {"::" (xt/x:get-key client "::")
    "defaults" (xt/x:get-key client "defaults")})
  => {"::" "js.net.ws_native/WebsocketClient"
      "defaults" {"host" "example.com"}})
