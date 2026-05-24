(ns xt.protocol.client-fetch-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script- :js
  {:runtime :basic
   :require [[xt.protocol.client-fetch :as fetch-if]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.protocol.client-fetch/request-prepare :added "4.1.3"}
(fact "normalises the standard request envelope with defaults and encoded bodies"

  (!.js
   (fetch-if/request-prepare
    {"url" "/rest/v1/orders"
     "body" {"id" "ord-1"}}))
  => {"method" "GET"
      "url" "/rest/v1/orders"
      "headers" {}
      "body" "{\"id\":\"ord-1\"}"})

^{:refer xt.protocol.client-fetch/response-normalize :added "4.1.3"}
(fact "normalises the standard response envelope and decodes json bodies"

  (!.js
   (fetch-if/response-normalize
    {"status" 200
     "body" "{\"id\":\"ord-1\"}"}))
  => {"status" 200
      "headers" {}
      "body" {"id" "ord-1"}})

^{:refer xt.protocol.client-fetch/merge-headers :added "4.1.3"}
(fact "merges header maps using the standard string-keyed envelope"

  (!.js
   (fetch-if/merge-headers
    {"x-client" "left"
     "x-shared" "left"}
    {"x-opt" "right"
     "x-shared" "right"}))
  => {"x-client" "left"
      "x-opt" "right"
      "x-shared" "right"})
