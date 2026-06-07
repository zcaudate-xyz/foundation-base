(ns xt.net.http-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-fetch :as fetch]]})

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

(defn.xt create-base
  [methods]
  (return
   (xt/x:obj-assign {"::" "net.fetch.client"}
                    (protocol/proto-spec
                     [[-/IHttpClient methods]]))))

(defn.xt request
  "dispatches request through the wrapped fetch client"
  {:added "4.1.3"}
  [client input opts]
  (var request-fn (xt/proto:method client "request_http"))
  (return (protocol/ensure-promise
           (request-fn client input opts))))



