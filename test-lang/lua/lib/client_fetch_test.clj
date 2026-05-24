(ns lua.lib.client-fetch-test
  (:require [hara.runtime.nginx]
            [hara.lang :as l]
            )
  (:use code.test))

(l/script- :lua.nginx
  {:runtime :basic
   :config  {:program :resty}
   :require [[lua.lib.client-fetch :as lua-fetch]
             [xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.lib.client-fetch/decode-body :added "4.1.3"}
(fact "encodes and decodes request bodies in lua"

  (!.lua
   [(lua-fetch/decode-body (lua-fetch/request-body {"id" "ord-1"}))
    (lua-fetch/decode-body "plain-text")])
  => [{"id" "ord-1"}
      "plain-text"])

^{:refer lua.lib.client-fetch/default-request :added "4.1.3"}
(fact "runs requests through request helpers"

  (!.lua
   (lua-fetch/default-request
    {"request" (fn [request _opts]
                 (return {"status" 200
                          "body" (. request ["body"])}))}
    {"method" "POST"
     "url" "/rest/v1/orders"
     "body" {"id" "ord-1"}}
    nil))
  => {"status" 200
      "headers" {}
      "body" {"id" "ord-1"}})

^{:refer lua.lib.client-fetch/client :added "4.1.3"}
(fact "wraps raw lua request functions as fetch protocol clients"

  (!.lua
   (local client
          (lua-fetch/client
          {"request" (fn [request _opts]
                       (return {"status" 200
                                "body" (. request ["body"])}))}))
   [(fetch/client? client)
    (promise/x:promise-native? (fetch/request client
                                               {"method" "POST"
                                                "url" "/rest/v1/orders"
                                                "body" {"id" "ord-1"}}
                                             nil))])
  => [true true]

  (notify/wait-on [:lua.nginx 2000]
   (promise/x:promise-then
    (fetch/request (lua-fetch/client
                    {"request" (fn [request _opts]
                                 (return {"status" 200
                                          "body" (. request ["body"])}))})
                   {"method" "POST"
                    "url" "/rest/v1/orders"
                    "body" {"id" "ord-1"}}
                   nil)
    (fn [result]
      (repl/notify result))))
  => {"status" 200
     "headers" {}
     "body" {"id" "ord-1"}})
