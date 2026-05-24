(ns js.lib.client-fetch-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.lib.client-fetch :as js-fetch]
             [xt.lang.common-notify :as notify]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.client-fetch/decode-body :added "4.1.3"}
(fact "encodes structured request bodies and decodes json response bodies"

  (!.js
   [(js-fetch/decode-body (js-fetch/request-body {"id" "ord-1"}))
    (js-fetch/decode-body "plain-text")])
  => [{"id" "ord-1"}
      "plain-text"])

^{:refer js.lib.client-fetch/default-request :added "4.1.3"}
(fact "runs requests through request helpers"

  (!.js
   (js-fetch/default-request
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

^{:refer js.lib.client-fetch/client :added "4.1.3"}
(fact "wraps raw js request functions as async fetch protocol clients"

  (!.js
   (var client
        (js-fetch/client
         (fn [request _opts]
           (return {"status" 200
                    "body" (. request ["body"])}))))
   [(fetch/client? client)
    (promise/x:promise-native? (fetch/request client
                                              {"method" "POST"
                                               "url" "/rest/v1/orders"
                                               "body" {"id" "ord-1"}}
                                              nil))])
  => [true
     true]

  (notify/wait-on [:js 2000]
    (promise/x:promise-then
    (fetch/request (js-fetch/client
                    (fn [request _opts]
                      (return {"status" 200
                               "body" (. request ["body"])})))
                   {"method" "POST"
                    "url" "/rest/v1/orders"
                    "body" {"id" "ord-1"}}
                   nil)
    (fn [result]
      (repl/notify result))))
  => {"status" 200
     "headers" {}
     "body" {"id" "ord-1"}})
