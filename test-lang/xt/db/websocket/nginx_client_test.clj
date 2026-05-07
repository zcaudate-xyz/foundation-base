(ns xt.db.websocket.nginx-client-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.websocket.nginx-client :as nginx]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.websocket.nginx-client/endpoint-url :added "4.1.3"}
(fact "builds statslink-style websocket urls"

  (!.js
   [(nginx/host-url {"host" "demo.test"
                     "port" 9443
                     "secured" true})
    (nginx/endpoint-url "/stream/user"
                        {"id" "user-1"
                         "token" "abc 123"}
                        {"host" "demo.test"
                         "port" 9443
                         "secured" true
                         "encode-param" (fn [s]
                                          (return (:? (== s "abc 123")
                                                      "abc%20123"
                                                      s)))} )])
  => ["wss://demo.test:9443"
      "wss://demo.test:9443/stream/user?id=user-1&token=abc%20123"])
