(ns ruby.net.http-fetch
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as util]]})

(defn.rb request-class [method]
  (return
   (xt/x:eval
    (cond (== method "POST") "Net::HTTP::Post"
          (== method "PUT") "Net::HTTP::Put"
          (== method "PATCH") "Net::HTTP::Patch"
          (== method "DELETE") "Net::HTTP::Delete"
          :else "Net::HTTP::Get"))))

(defn.rb response-headers [res]
  (var out {})
  (:- "res.each_header { |key, value| out[key] = value }")
  (return out))

(defn.rb request-http-raw [client input]
  (var #{raw} client)
  (:= raw (or raw {}))
  (var request (fetch/prepare-input client input))
  (var request-fn (xt/x:get-key raw "request"))
  (when (xt/x:is-function? request-fn)
    (return (util/response-normalize (request-fn request {}))))
  (require "net/http")
  (require "uri")
  (var uri (. URI (parse (xt/x:get-key request "url"))))
  (var req-class (-/request-class (xt/x:get-key request "method")))
  (var req (. req-class (new uri)))
  (xt/for:object [[key value] (or (xt/x:get-key request "headers") {})]
    (xt/x:set-key req key value))
  (var body (xt/x:get-key request "body"))
  (when (xt/x:not-nil? body)
    (:= (. req body) body))
  (try
    (var http-class (xt/x:eval "Net::HTTP"))
    (var http (. http-class (new (. uri host) (. uri port))))
    (:= (. http use_ssl) (== "https" (. uri scheme)))
    (var res (:- "http.request(req)"))
    (return {"status" (. (. res code) (to_i))
             "headers" (-/response-headers res)
             "body" (util/decode-body (. res body))})
    (catch err
      (return {"status" nil
               "headers" {}
               "body" (xt/x:to-string err)
               "error" err}))))

(defn.rb request-http [client input]
  (var handler (fetch/prepare-middleware client -/request-http-raw))
  (return (promise/x:promise-run (handler client input))))

(defimpl.xt ^{:lang :ruby}
  RubyHttpFetchClient
  [defaults middleware raw]
  fetch/IHttpClient
  {fetch/request-http -/request-http})

(defn.rb create [defaults middleware := nil]
  (return
   (-/RubyHttpFetchClient (or defaults {})
                          (or middleware [fetch/wrap-prepare-input])
                          nil)))
