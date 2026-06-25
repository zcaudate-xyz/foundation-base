(ns xt.net.ws-native-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.net.ws-native :refer :all]))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.net.ws-native :as ws-native]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.net.ws-native :as ws-native]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.net.ws-native :as ws-native]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.net.ws-native/prepare-url :added "4.1"}
(fact "returns an explicit url or builds a websocket url from client defaults"

  (!.js
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
      "ws://example.com:80/ws"]

  (!.lua
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
      "ws://example.com:80/ws"]

  (!.py
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
