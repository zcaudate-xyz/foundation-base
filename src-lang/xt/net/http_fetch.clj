(ns xt.net.http-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]]})

(def.xt IHttpClient
  ["request_http"])

(def.xt REQUEST_FIELDS
  ["method"
   "url"
   "query"
   "headers"
   "body"
   "timeout"
   "opts"])

(def.xt RESPONSE_FIELDS
  ["status"
   "headers"
   "body"
   "error"])

(defn.xt prepare-url
  [client input]
  (var #{url path} input)
  (if (not (xt/x:nil? url))
    (return url))
  
  (var defaults (or (xt/x:get-key client "defaults")
                    {}))
  
  (var #{secured
         host
         port
         basepath} defaults)
  
  (return (xt/x:cat "http" (:? secured "s" "")
                    "://" host
                    ":"
                    (or port "80")
                    (or basepath "")
                    (or path ""))))

(defn.xt prepare-input
  [client input]
  (var defaults (or (xt/x:get-key client "defaults")
                    {}))
  (var #{body
         method} input)
  (var headers (xt/x:obj-assign
                (xt/x:obj-clone
                 (xt/x:get-key defaults "headers"))
                (xt/x:get-key input "headers")))
  (return {:url     (-/prepare-url client input)
           :body    body
           :method  (or method "GET")
           :headers headers}))

(defn.xt create-base
  [type methods]
  (return
   (xt/x:obj-assign {"::" (or type "xt.net.http-fetch")}
                    (protocol/proto-spec
                     [[-/IHttpClient methods]]))))

(defn.xt request-http
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input opts]
  (var request-fn (xt/x:get-key client "request_http"))
  (return (protocol/ensure-promise
           (request-fn client input opts))))



