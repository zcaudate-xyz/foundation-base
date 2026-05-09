(ns xt.db.websocket.nchan-client-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.db.websocket.nchan-client :as nchan]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.websocket.nchan-client/endpoint-url :added "4.1.3"}
(fact "builds statslink-style websocket urls"

  (!.js
   [(nchan/host-url {"host" "demo.test"
                     "port" 9443
                     "secured" true})
    (nchan/endpoint-url "/stream/user"
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


^{:refer xt.db.websocket.nchan-client/join-url :added "4.1"}
(fact "joins base urls and relative paths without double slashes"

  (!.js
   [(nchan/join-url "ws://demo.test/" "/stream")
    (nchan/join-url "ws://demo.test" "stream")
    (nchan/join-url nil "/stream")])
  => ["ws://demo.test/stream"
      "ws://demo.test/stream"
      "/stream"])

^{:refer xt.db.websocket.nchan-client/host-url :added "4.1"}
(fact "builds websocket hosts with sensible defaults"

  (!.js
   [(nchan/host-url {"host" "demo.test" "port" 443 "secured" true})
    (nchan/host-url {"host" "demo.test"})
    (nchan/host-url nil)])
  => ["wss://demo.test:443"
      "ws://demo.test:80"
      "ws://localhost:80"])

^{:refer xt.db.websocket.nchan-client/query-string :added "4.1"}
(fact "encodes params and skips nil values"

  (!.js
   (nchan/query-string
    {"id" "user-1"
     "token" "abc 123"
     "ignore" nil}
    (fn [s]
      (return (:? (== s "abc 123")
                  "abc%20123"
                  s)))))
  => "id=user-1&token=abc%20123")

^{:refer xt.db.websocket.nchan-client/default-connect-raw :added "4.1"}
(fact "emits lua.nginx source for the low-level websocket connect helper"
  (let [out (l/emit-as :lua.nginx
                       '[(xt.db.websocket.nchan-client/default-connect-raw url connect_opts)])]
    [(string? out)
     (< 0 (count out))])
  => [true true])

^{:refer xt.db.websocket.nchan-client/create-driver :added "4.1"}
(fact "emits lua.nginx source for websocket driver creation"
  (let [out (l/emit-as :lua.nginx
                       '[(xt.db.websocket.nchan-client/create-driver opts)])]
    [(string? out)
     (< 0 (count out))])
  => [true true])

^{:refer xt.db.websocket.nchan-client/connect :added "4.1"}
(fact "emits lua.nginx source for websocket connection setup"
  (let [out (l/emit-as :lua.nginx
                       '[(xt.db.websocket.nchan-client/connect path params opts)])]
    [(string? out)
     (< 0 (count out))])
  => [true true])
