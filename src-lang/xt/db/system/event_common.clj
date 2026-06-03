(ns xt.db.system.event-common
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system :as db-system]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.client-websocket :as ws]]})

(defn.xt client?
  "checks if a value is a wrapped transport client descriptor for a given tag"
  {:added "4.1.4"}
  [obj tag]
  (return (and (xt/x:is-object? obj)
               (== tag
                   (xt/x:get-key obj "::")))))

(defn.xt raw-client
  "unwraps the tagged transport client descriptor"
  {:added "4.1.4"}
  [client tag]
  (if (-/client? client tag)
    (return (or (xt/x:get-key client "_raw") {}))
    (return (or client {}))))

(defn.xt resolve-client-source
  "resolves a raw transport client source from db or opts"
  {:added "4.1.4"}
  [db opts]
  (return (or (xt/x:get-key db "client")
              (xt/x:get-key opts "client")
              (xt/x:get-key db "transport")
              (xt/x:get-key opts "transport")
              nil)))

(defn.xt wrap-client
  "wraps raw transport config into the standard tagged client descriptor"
  {:added "4.1.4"}
  [raw tag]
  (when (-/client? raw tag)
    (return raw))
  (var source nil)
  (cond (or (ws/client? raw)
            (ws/driver? raw))
        (:= source {"transport" raw})

        (xt/x:nil? raw)
        (:= source {})

        :else
        (:= source (xt/x:obj-clone raw)))
  (return {"::" tag
           "_raw" source}))

(defn.xt resolve-transport
  "resolves a websocket transport as a standard websocket driver or client"
  {:added "4.1.4"}
  [client tag label]
  (var raw-client (-/raw-client client tag))
  (var transport-source
       (xt/x:get-key raw-client "transport"))
  (cond (ws/client? transport-source)
        (return transport-source)

        (ws/driver? transport-source)
        (return transport-source)

        (xt/x:is-function? transport-source)
        (return (ws/driver-create {"connect" transport-source}))

        (or (xt/x:is-function? (xt/x:get-key transport-source "connect"))
            (xt/x:is-function? (xt/x:get-key transport-source "connect_sync")))
        (return (ws/driver-create transport-source))

        :else
        (xt/x:err (xt/x:cat (or label tag)
                            " missing websocket transport"))))

(defn.xt request-op
  "Returns the supported xt.db request primitive present at the top level."
  {:added "4.1.4"}
  [request]
  (when (or (xt/x:nil? request)
            (not (xt/x:is-object? request)))
    (return nil))
  (cond (xt/x:not-nil? (xt/x:get-key request "db/sync"))
        (return "db/sync")

        (xt/x:not-nil? (xt/x:get-key request "db/remove"))
        (return "db/remove")

        :else
        (return nil)))

(defn.xt unwrap-request
  "Unwraps a native xt.db request from a raw payload or nested payload envelope."
  {:added "4.1.4"}
  [payload]
  (when (or (xt/x:nil? payload)
            (not (xt/x:is-object? payload)))
    (return nil))
  (cond (xt/x:not-nil? (-/request-op payload))
        (return payload)

        (xt/x:not-nil? (-/request-op (xt/x:get-key payload "payload")))
        (return (xt/x:get-key payload "payload"))

        :else
        (return nil)))

(defn.xt request?
  "Checks if a payload carries a native xt.db request."
  {:added "4.1.4"}
  [payload]
  (return (xt/x:not-nil? (-/unwrap-request payload))))

(defn.xt trim-trailing-slash
  "trims a single trailing slash"
  {:added "4.1.4"}
  [s]
  (if (and (xt/x:is-string? s)
           (str/ends-with? s "/"))
    (return (xt/x:str-substring s 0 (- (xt/x:str-len s) 1)))
    (return s)))

(defn.xt encode-query-params
  "encodes a flat query param map"
  {:added "4.1.4"}
  [params]
  (var out [])
  (xt/for:object [[k v] (or params {})]
    (when (xt/x:not-nil? v)
      (xt/x:arr-push out (xt/x:cat k "=" (xt/x:to-string v)))))
  (return (xt/x:str-join "&" out)))

(defn.xt extract-message-data
  "extracts websocket message data from raw events or payload strings"
  {:added "4.1.4"}
  [message]
  (cond (xt/x:is-string? message)
        (return message)

        (xt/x:not-nil? (xt/x:get-key message "data"))
        (return (xt/x:get-key message "data"))

        (xt/x:not-nil? (xt/x:get-key message "body"))
        (return (xt/x:get-key message "body"))

        :else
        (return message)))

(defn.xt decode-message
  "decodes websocket message data with configurable json handling"
  {:added "4.1.4"}
  [message opts]
  (var data (-/extract-message-data message))
  (if (not (xt/x:is-string? data))
    (return data))
  (when (== false (xt/x:get-key opts "decode_json"))
    (return data))
  (when (xt/x:get-key opts "decode_if_json")
    (if (or (str/starts-with? data "{")
            (str/starts-with? data "["))
      (return (xt/x:json-decode data))
      (return data)))
  (return (xt/x:json-decode data)))

(defn.xt resolve-request-transform
  "resolves an optional payload->request transform"
  {:added "4.1.4"}
  [source opts tag]
  (var raw-client (-/raw-client source tag))
  (return (or (xt/x:get-key opts "request_transform")
              (xt/x:get-key raw-client "request_transform")
              nil)))

(defn.xt apply-request
  "Applies a native xt.db request to a local db."
  {:added "4.1.4"}
  [local-db request opts]
  (when (xt/x:not-nil? (xt/x:get-key request "db/sync"))
    (db-system/sync-event local-db
                          ["add" (xt/x:get-key request "db/sync")]))
  (when (xt/x:not-nil? (xt/x:get-key request "db/remove"))
    (var schema (or (xt/x:get-key opts "schema")
                    (xt/x:get-key local-db "schema")
                    nil))
    (if (xt/x:not-nil? schema)
      (xt/for:object [[table ids] (xt/x:get-key request "db/remove")]
        (db-system/db-delete-sync local-db schema table ids))
      (db-system/sync-event local-db
                            ["remove" (xt/x:get-key request "db/remove")])))
  (return request))

(defn.xt apply-payload
  "Unwraps and applies a native xt.db request from a payload."
  {:added "4.1.4"}
  [local-db payload opts]
  (var request (-/unwrap-request payload))
  (when request
    (-/apply-request local-db request opts))
  (return [true request]))

(defn.xt subscription?
  "checks if the object is a tagged transport subscription handle"
  {:added "4.1.4"}
  [obj tag]
  (return (and (xt/x:is-object? obj)
               (== tag
                   (xt/x:get-key obj "::")))))

(defn.xt subscription-active?
  "checks whether a tagged subscription is still marked active"
  {:added "4.1.4"}
  [subscription tag]
  (return (and (-/subscription? subscription tag)
               (not (== false
                        (xt/x:get-key subscription "active"))))))

(defn.xt unsubscribe
  "tears down a tagged websocket subscription handle"
  {:added "4.1.4"}
  [subscription tag reason]
  (when (not (-/subscription? subscription tag))
    (return (promise/x:promise-run nil)))
  (xt/x:set-key subscription "active" false)
  (return
   (ws/disconnect (xt/x:get-key subscription "socket")
                  1000
                  reason)))
