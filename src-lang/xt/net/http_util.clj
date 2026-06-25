(ns xt.net.http-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]]})

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

(defn.xt request-prepare
  "normalises the standard fetch request envelope"
  {:added "4.1.3"}
  [input]
  (var #{method
         headers
         body} (or input {}))
  (return {"method"  (or method "GET")
           "headers" headers
           "body"    (or body "")}))

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
                          (xt/x:obj-assign {} (xt/x:get-key out "headers")))
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

(defn.xt encode-query-params
  "encodes a flat query param map"
  {:added "4.1.4"}
  [params]
  (var out [])
  (xt/for:object [[k v] (or params {})]
    (when (xt/x:not-nil? v)
      (xt/x:arr-push out (xt/x:cat k "=" (xt/x:to-string v)))))
  (return (xt/x:str-join "&" out)))

(defn.xt get-body-data
  [response]
  (var out (xt/x:get-key response "body"))
  (cond (and (xt/x:is-object? out)
             (xt/x:not-nil? (xt/x:get-key out "data")))
        (return (xt/x:get-key out "data"))

        :else
        (return out)))
