(ns xt.net.http-util-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.net.http-util :refer :all]))

(l/script- :js
  {:runtime :basic
   :require [[xt.net.http-util :as util]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.net.http-util/request-body :added "4.1"}
(fact "encodes structured request bodies and leaves strings and nil untouched"

  (!.js
   [(util/request-body nil)
    (util/request-body "plain-text")
    (util/request-body {"id" "ord-1"})])
  => [nil
      "plain-text"
      "{\"id\":\"ord-1\"}"])

^{:refer xt.net.http-util/decode-body :added "4.1"}
(fact "decodes json strings when possible and preserves non-strings"

  (!.js
   [(util/decode-body "{\"id\":\"ord-1\"}")
    (util/decode-body "plain-text")
    (util/decode-body "")
    (util/decode-body nil)])
  => [{"id" "ord-1"}
      "plain-text"
      nil
      nil])

^{:refer xt.net.http-util/request-prepare :added "4.1"}
(fact "normalises the standard request envelope with defaults"

  (!.js
   [(util/request-prepare nil)
    (util/request-prepare {"method" "POST"
                           "headers" {"x-client" "test"}
                           "body" "payload"})])
  => [{"method" "GET"
       "body" ""}
      {"method" "POST"
       "headers" {"x-client" "test"}
       "body" "payload"}])

^{:refer xt.net.http-util/response-normalize :added "4.1"}
(fact "normalises raw responses and decodes json bodies"

  (!.js
   [(util/response-normalize nil)
    (util/response-normalize {"status" 200
                              "headers" {"content-type" "application/json"}
                              "body" "{\"id\":\"ord-1\"}"
                              "error" nil})
    (util/response-normalize "plain-text")])
  => [{"body" nil
       "error" nil
       "headers" {}
       "status" nil}
      {"status" 200
       "headers" {"content-type" "application/json"}
       "body" {"id" "ord-1"}
       "error" nil}
      {"body" "plain-text"
       "error" nil
       "headers" {}
       "status" nil}])

^{:refer xt.net.http-util/encode-query-params :added "4.1"}
(fact "encodes flat query param maps and skips nil values"

  (!.js
   [(util/encode-query-params {"a" 1
                               "b" nil
                               "c" "two"})
    (util/encode-query-params nil)])
  => ["a=1&c=two"
      ""])


^{:refer xt.net.http-util/get-body-data :added "4.1"}
(fact "extracts the data field from a normalized response body"

  (!.js
    [(util/get-body-data {"body" {"data" {"id" "ord-1"}}})
     (util/get-body-data {"body" "plain-text"})
     (util/get-body-data {"body" nil})
     (util/get-body-data {})])
  => [{"id" "ord-1"}
      "plain-text"
      nil
      nil])