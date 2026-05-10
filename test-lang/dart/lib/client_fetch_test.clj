(ns dart.lib.client-fetch-test
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :dart
  {:runtime :twostep
   :require [[dart.lib.client-fetch :as dart-fetch]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(def CANARY-DART
  (common/program-exists? "dart"))

^{:refer dart.lib.client-fetch/decode-body :added "4.1.3"}
(fact "encodes and decodes request bodies in dart"

  (if CANARY-DART
    (!.dt
     [(dart-fetch/decode-body (dart-fetch/request-body {"id" "ord-1"}))
      (dart-fetch/decode-body "plain-text")])
    :dart-unavailable)
  => (any [{"id" "ord-1"} "plain-text"]
          :dart-unavailable))

^{:refer dart.lib.client-fetch/default-request-sync :added "4.1.3"}
(fact "runs sync requests through request_sync helpers"

  (if CANARY-DART
    (!.dt
     (dart-fetch/default-request-sync
      {"request_sync" (fn [request _opts]
                        (return {"status" 200
                                 "body" (. request ["body"])}))}
      {"method" "POST"
       "url" "/rest/v1/orders"
       "body" {"id" "ord-1"}}
      nil))
    :dart-unavailable)
  => (any {"status" 200
           "body" {"id" "ord-1"}}
          :dart-unavailable))

^{:refer dart.lib.client-fetch/client :added "4.1.3"}
(fact "wraps raw dart request functions as fetch protocol clients"

  (if CANARY-DART
    (!.dt
     (var client
           (dart-fetch/client
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
    :dart-unavailable)
  => (any [true true {"status" 200
                      "body" {"id" "ord-1"}}]
          :dart-unavailable))

(fact "resolves dart request promises to decoded payloads"

  (if CANARY-DART
    (notify/wait-on :dart
      (promise/x:promise-then
       (let [client (dart-fetch/client
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
    :dart-unavailable)
  => (any {"status" 200
           "body" {"id" "ord-1"}}
          :dart-unavailable))
