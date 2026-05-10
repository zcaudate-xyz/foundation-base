(ns python.lib.client-fetch-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[python.lib.client-fetch :as py-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.lib.client-fetch/decode-body :added "4.1.3"}
(fact "encodes and decodes request bodies in python"

  (!.py
   [(py-fetch/decode-body (py-fetch/request-body {"id" "ord-1"}))
    (py-fetch/decode-body "plain-text")])
  => [{"id" "ord-1"}
      "plain-text"])

^{:refer python.lib.client-fetch/default-request-sync :added "4.1.3"}
(fact "runs sync requests through request_sync helpers"

  (!.py
   (py-fetch/default-request-sync
    {"request_sync" (fn [request _opts]
                      (return {"status" 200
                               "body" (. request ["body"])}))}
    {"method" "POST"
     "url" "/rest/v1/orders"
     "body" {"id" "ord-1"}}
    nil))
  => {"status" 200
      "body" {"id" "ord-1"}})

^{:refer python.lib.client-fetch/client :added "4.1.3"}
(fact "wraps raw python request functions as fetch protocol clients"

  (!.py
   (var client
        (py-fetch/client
         {"request_sync" (fn [request _opts]
                           (return {"status" 200
                                    "body" (. request ["body"])}))}))
   [(fetch/client? client)
    (promise/x:promise-native? (fetch/request client
                                              {"method" "POST"
                                               "url" "/rest/v1/orders"
                                               "body" {"id" "ord-1"}}
                                              nil))
    (fetch/request-sync client
                        {"method" "POST"
                         "url" "/rest/v1/orders"
                         "body" {"id" "ord-1"}}
                        nil)])
  => [true
      true
      {"status" 200
       "body" {"id" "ord-1"}}])

(fact "resolves python request promises to decoded payloads"

  (notify/wait-on [:python 2000]
    (promise/x:promise-then
     (let [client (py-fetch/client
                   {"request_sync" (fn [request _opts]
                                     (return {"status" 200
                                              "body" (. request ["body"])}))})]
       (fetch/request client
                      {"method" "POST"
                       "url" "/rest/v1/orders"
                       "body" {"id" "ord-1"}}
                      nil))
     (fn [result]
       (repl/notify result))))
  => {"status" 200
      "body" {"id" "ord-1"}})
