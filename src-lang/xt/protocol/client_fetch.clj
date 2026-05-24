(ns xt.protocol.client-fetch
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]]})

(def.xt IFetchClient
  ["request"])

(def.xt IFetchRuntimeClient
  (proto/iface-combine [-/IFetchClient]))

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

(defn.xt request-body
  "encodes request bodies using json for structured values"
  {:added "4.1.3"}
  [body]
  (cond (xt/x:nil? body)
        (return nil)

        (xt/x:is-string? body)
        (return body)

        :else
        (return (xt/x:json-encode body))))

(defn.xt decode-body
  "decodes json response bodies when possible"
  {:added "4.1.3"}
  [body]
  (cond (not (xt/x:is-string? body))
        (return body)

        (== "" body)
        (return nil)

        :else
        (try
          (return (xt/x:json-decode body))
          (catch err
            (return body)))))

(defn.xt merge-headers
  "merges standard header maps"
  {:added "4.1.3"}
  [left right]
  (var out {})
  (xt/x:obj-assign out (or left {}))
  (xt/x:obj-assign out (or right {}))
  (return out))

(defn.xt request-prepare
  "normalises the standard fetch request envelope"
  {:added "4.1.3"}
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (when (xt/x:nil? (xt/x:get-key request "method"))
    (xt/x:set-key request "method" "GET"))
  (xt/x:set-key request
                "headers"
                (-/merge-headers {}
                                 (xt/x:get-key request "headers")))
  (when (xt/x:not-nil? (xt/x:get-key request "body"))
    (xt/x:set-key request
                  "body"
                  (-/request-body (xt/x:get-key request "body"))))
  (return request))

(defn.xt response-normalize
  "normalises the standard fetch response envelope"
  {:added "4.1.3"}
  [response]
  (cond (xt/x:nil? response)
        (return {"status" nil
                 "headers" {}
                 "body" nil
                 "error" nil})

        (and (xt/x:is-object? response)
             (xt/x:has-key? response "body"))
        (do (var out (xt/x:obj-clone response))
            (xt/x:set-key out
                          "headers"
                          (-/merge-headers {}
                                           (xt/x:get-key out "headers")))
            (xt/x:set-key out
                          "body"
                          (-/decode-body (xt/x:get-key out "body")))
            (return out))

        (xt/x:is-object? response)
        (return response)

        :else
        (return {"status" nil
                 "headers" {}
                 "body" (-/decode-body response)
                 "error" nil})))
