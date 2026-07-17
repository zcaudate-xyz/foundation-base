(ns xt.net.http-fetch
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defprotocol.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-protocol :as protocol]
             [xt.net.http-util :as util]]})

(defprotocol.xt IHttpClient
  (request-http [client input]))

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
  
  (var #{defaults} client)
  (var #{secured
         host
         port
         basepath} defaults)
  
  (return (xt/x:cat "http" (:? secured "s" "")
                    "://" host
                    ":"
                    (xt/x:to-string (or port 80))
                    (or basepath "")
                    (or path ""))))

(defn.xt prepare-input
  [client input]
  (var #{defaults} client)
  (var #{body
         method} input)
  (var headers (->  {}
                    (xt/x:obj-assign (xt/x:get-key defaults "headers"))
                    (xt/x:obj-assign (xt/x:get-key input "headers"))))
  (var output {:url     (-/prepare-url client input)
               :method  (or method "GET")
               :headers headers})
  (when (xt/x:not-nil? body)
    (:= output (xt/x:obj-assign output {"body" body})))
  (return output))

(defn.xt wrap-prepare-input
  [handler]
  (return
   (fn [client input]
     (var prepped (-/prepare-input client input))
     (return
      (handler client prepped)))))

(defn.xt wrap-normalise
  [handler]
  (return
   (fn [client input]
     (return
      (-> (handler client input)
          (promise/x:promise-then
           (fn [response]
             (return
              (util/response-normalize response)))))))))

(defn.xt then-normalise
  "normalises a promise of a fetch response envelope"
  {:added "4.1"}
  [promise]
  (return (promise/x:promise-then promise util/response-normalize)))

(defn.xt prepare-middleware
  [client handler]
  (var #{middleware} client)
  (xt/for:array [wrapper (or middleware [])]
    (:= handler (wrapper handler)))
  (return handler))

(defn.xt prepare-handler
  "prepares a raw request handler with input preparation, middleware, and response normalisation"
  {:added "4.1"}
  [client handler]
  (return
   (-/wrap-normalise
    (-/prepare-middleware client
     (-/wrap-prepare-input handler)))))

