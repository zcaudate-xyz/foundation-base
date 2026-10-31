(ns xt.net.addon-neon
  (:require [lang.core :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as http]]})

(defn.xt join-url
  "Joins a configured service URL to a relative path without changing its query."
  [base path]
  (when (or (xt/x:nil? base) (== base ""))
    (xt/x:err "Neon service URL is not configured"))
  (when (== (str/substring base (- (xt/x:len base) 1) (xt/x:len base)) "/")
    (:= base (str/substring base 0 (- (xt/x:len base) 1))))
  (return (xt/x:cat base path)))

(defn.xt response-data
  "Preserves PostgREST bodies and rejects transport and HTTP failures."
  [response]
  (var normalized (http/response-normalize response))
  (var #{status body error} normalized)
  (when (or error (xt/x:nil? status) (< status 200) (>= status 300))
    (throw (xt/x:ex "Neon request failed" {"status" status "code" (:? (xt/x:is-object? body) (xt/x:get-key body "code" "neon_request_failed") "neon_request_failed")})))
  (return body))

(defn.xt bridge-input
  [client path data]
  (var defaults (xt/x:get-key client "defaults" nil))
  (var credential (xt/x:get-key defaults "bridge_token" nil))
  (var headers {"Content-Type" "application/json" "Accept" "application/json"})
  (when credential
    (xt/x:set-key headers "Authorization" (xt/x:cat "Bearer " credential)))
  (return {"url" (-/join-url (xt/x:get-key defaults "bridge_url" nil) path)
           "method" "POST" "headers" headers "body" (xt/x:json-encode (or data {}))}))

(defn.xt bridge-request
  [client path data]
  (return (-> (fetch/request-http client (-/bridge-input client path data))
              (promise/x:promise-then -/response-data))))

(defn.xt backoff
  [ms thunk]
  (return (promise/x:with-delay ms thunk)))

(l/script :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-string :as str]
             [xt.lang.spec-promise :as promise]
             [xt.net.http-fetch :as fetch]
             [xt.net.http-util :as http]]})
(defn.py backoff
  [ms thunk]
  (. (__import__ "time") (sleep (/ ms 1000.0)))
  (return (thunk)))
