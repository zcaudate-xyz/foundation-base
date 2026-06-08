(ns xt.protocol.impl.client-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-fetch :as fetch]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.impl.client-fetch/ensure-promise :added "4.1.3"}
(fact "normalises raw values into native promises"

  (!.js
   (promise/x:promise-native? (fetch/ensure-promise {"ok" true})))
  => true)


(comment

  (!.js

    (tree/tree-get-spec
     (fetch/client-create
      {"request" (fn [request opts]
                   (xt/x:arr-push calls ["request" request opts])
                   (return {"status" "ok"}))
       "headers" {"x-client" "nested"}}
      {})))
  )


^{:refer xt.protocol.impl.client-fetch/client-create :added "4.1.3"}
(fact "wraps fetch clients with a single request method"

  (!.js
   (var calls [])
   (var client
        (fetch/client-create
        {"request" (fn [request opts]
                     (xt/x:arr-push calls ["request" request opts])
                     (return {"status" "ok"}))
          "headers" {"x-client" "nested"}}
         {}))
    [(fetch/client? client)
      (promise/x:promise-native? (fetch/request client {"url" "/health"} {"auth" "token"}))
     calls
     (. (. (. client ["_raw"]) ["headers"]) ["x-client"])])
    => [true
       true
       [["request" {"url" "/health"
                    "method" "GET"
                    "headers" {}}
         {"auth" "token"}]]
       "nested"])

(fact "resolves async requests through the promise-returning request method"

  (notify/wait-on [:js 2000]
    (var client
         (fetch/client-create
          {"request" (fn [request _opts]
                       (return {"status" 200
                                "body" (. request ["url"])}))}
          {}))
    (promise/x:promise-then
     (fetch/request client {"url" "/health"} nil)
     (fn [result]
       (repl/notify result))))
  => {"status" 200
       "headers" {}
       "body" "/health"})

^{:refer xt.protocol.impl.client-fetch/client-source :added "4.1.3"}
(fact "normalises raw function sources into request objects"

  (!.js
   (var out (fetch/client-source
             (fn [input _opts]
                (return input))))
   (xt/x:is-function? (. out ["request"])))
  => true)
