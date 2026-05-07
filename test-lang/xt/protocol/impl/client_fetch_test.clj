(ns xt.protocol.impl.client-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.client-fetch/client-create :added "4.1.3"}
(fact "wraps fetch clients and falls back from request to query/rpc"

  (!.js
   (var calls [])
   (var client
        (fetch/client-create
         {"request" (fn [request opts]
                      (x:arr-push calls ["request" request opts])
                      (return {"status" "ok"}))
          "headers" {"x-client" "nested"}}
         {}))
   [(fetch/client? client)
    (fetch/request client {"url" "/health"} {"auth" "token"})
    (fetch/query client {"url" "/rows"} nil)
    (fetch/rpc client {"url" "/rpc/f"} nil)
    calls
    (. (. (. client ["_raw"]) ["headers"]) ["x-client"])])
  => [true
      {"status" "ok"}
      {"status" "ok"}
      {"status" "ok"}
      [["request" {"url" "/health"} {"auth" "token"}]
       ["request" {"url" "/rows"} nil]
       ["request" {"url" "/rpc/f"} nil]]
      "nested"])
