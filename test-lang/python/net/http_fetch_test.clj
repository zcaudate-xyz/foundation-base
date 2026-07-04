(ns python.net.http-fetch-test
  (:require [hara.lang :as l]
            [std.lib.env :as env])
  (:use code.test))

(l/script- :python
  {:runtime :basic
   :require [[python.net.http-fetch :as fetch]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:skip     (not (env/program-exists? "python3"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer python.net.http-fetch/create :added "4.1"}
(fact "creates an http fetch client"

  (fetch/create {} [])
  => (contains {"::" "python.net.http_fetch/PythonHttpFetchClient"
                "defaults" {}
                "middleware" vector?}))

^{:refer python.net.http-fetch/request-http-raw :added "4.1"}
(fact "performs a raw http request"

  (!.py (do (var client (fetch/create {} []))
            (xt/x:set-key client "raw"
                          {"request"
                           (fn [request opts]
                             (return {"status" 200
                                      "headers" {}
                                      "body" "\"hello\""}))})
            (var out (fetch/request-http-raw client {:url "http://example.com"
                                                     :method "GET"}))
            (return out)))
  => {"status" 200
      "headers" {}
      "body" "hello"})

^{:refer python.net.http-fetch/request-http :added "4.1"}
(fact "performs an http request through middleware"

  (!.py (do (var client (fetch/create {} []))
            (xt/x:set-key client "raw"
                          {"request"
                           (fn [request opts]
                             (return {"status" 201
                                      "headers" {}
                                      "body" "\"ok\""}))})
            (var out (fetch/request-http client {:url "http://example.com"
                                                 :method "POST"
                                                 :body "{}"}))
            (return out)))
  => {"status" 201
      "headers" {}
      "body" "ok"})
