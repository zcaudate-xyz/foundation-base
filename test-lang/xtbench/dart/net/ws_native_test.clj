(ns xtbench.dart.net.ws-native-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.net.ws-native :refer :all]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.net.ws-native :as ws-native]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.net.ws-native/prepare-url :added "4.1"}
(fact "returns an explicit url or builds a websocket url from client defaults"

  (!.dt
    [(ws-native/prepare-url {"defaults" {"host" "example.com"
                                          "port" "8080"}}
                            {"url" "ws://given"})
     (ws-native/prepare-url {"defaults" {"host" "example.com"
                                          "port" "8080"}}
                            {})
     (ws-native/prepare-url {"defaults" {"host" "example.com"
                                          "secured" true
                                          "basepath" "/api"}}
                            {"path" "/ws"})
     (ws-native/prepare-url {"defaults" {"host" "example.com"}}
                            {"path" "/ws"})])
  => ["ws://given"
      "ws://example.com:8080"
      "wss://example.com:80/api/ws"
      "ws://example.com:80/ws"])
