(ns documentation.xt-net
  (:require [hara.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.net.http-util :as util]
             [xt.net.http-fetch :as fetch]
             [xt.lang.spec-base :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

[[:hero {:title "xt.net"
         :subtitle "HTTP, websocket, SQL, Redis, and Supabase helpers."
         :lead "`xt.net` contains portable networking adapters used by generated programs and substrate/database systems."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Generated xtalk programs need consistent wrappers around fetch, websocket transports, Phoenix websocket conventions, SQL connections, Redis connections, and Supabase addons."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Request and response envelopes"}]]

"`xt.net.http-util` converts between Clojure data and the wire format used by portable fetch clients."

(fact "encode and decode request bodies"
  ^{:refer xt.net.http-util/request-body :added "4.0"}
  (!.js
    [(util/request-body nil)
     (util/request-body "plain-text")
     (util/request-body {"id" "ord-1"})])
  => [nil "plain-text" "{\"id\":\"ord-1\"}"]

  ^{:refer xt.net.http-util/decode-body :added "4.0"}
  (!.js
    [(util/decode-body "{\"id\":\"ord-1\"}")
     (util/decode-body "plain-text")
     (util/decode-body nil)])
  => [{"id" "ord-1"} "plain-text" nil]

  ^{:refer xt.net.http-util/encode-query-params :added "4.0"}
  (!.js
    (util/encode-query-params {"a" 1
                               "b" nil
                               "c" "two"}))
  => "a=1&c=two")

[[:section {:title "Response normalisation"}]]

"`xt.net.http-util/response-normalize` turns raw fetch responses into a predictable map, decoding JSON bodies automatically."

(fact "normalise a raw fetch response"
  ^{:refer xt.net.http-util/response-normalize :added "4.0"}
  (!.js
    (util/response-normalize {"status" 200
                              "headers" {"content-type" "application/json"}
                              "body" "{\"id\":\"ord-1\"}"
                              "error" nil}))
  => {"status" 200
      "headers" {"content-type" "application/json"}
      "body" {"id" "ord-1"}
      "error" nil})

[[:section {:title "Fetch preparation"}]]

"`xt.net.http-fetch` builds complete request inputs from host defaults and per-request options."

(fact "prepare url and input for a fetch client"
  ^{:refer xt.net.http-fetch/prepare-url :added "4.0"}
  (!.js
    (fetch/prepare-url
     {:defaults {:secured false
                 :host "127.0.0.1"
                 :port "55121"
                 :basepath "/auth/v1"}}
     {:path "/sign-in"}))
  => "http://127.0.0.1:55121/auth/v1/sign-in"

  ^{:refer xt.net.http-fetch/prepare-input :added "4.0"}
  (!.js
    (fetch/prepare-input
     {:defaults {:secured false
                 :host "127.0.0.1"
                 :port "55121"
                 :headers {"apikey" "TOKEN"}
                 :basepath "/auth/v1"}}
     {:path "/sign-in"}))
  => {"url" "http://127.0.0.1:55121/auth/v1/sign-in"
      "method" "GET"
      "headers" {"apikey" "TOKEN"}})

[[:section {:title "End-to-end: build a request envelope"}]]

"Combining the helpers above gives a predictable request map ready for a target fetch implementation."

(fact "construct a full GET request"
  (!.js
    (var input (fetch/prepare-input
                {:defaults {:secured true
                            :host "api.example.com"
                            :port "443"
                            :headers {"apikey" "TOKEN"}
                            :basepath "/v1"}}
                {:path "/items"
                 :query {"limit" 10}}))
    [(. input ["url"])
     (. input ["method"])
     (. input ["headers" "apikey"])])
  => ["https://api.example.com:443/v1/items?limit=10" "GET" "TOKEN"])

[[:chapter {:title "Internal usage" :link "internal"}]]

"The networking layer is used by xt.db, xt.substrate transports, and Supabase-related examples. Tests under `test-lang/xt/net` provide runnable behavior examples."

[[:chapter {:title "API" :link "api"}]]
