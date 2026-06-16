(ns xt.db.system.event-nchan
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.system.event-common :as common]
             [xt.lang.common-string :as str]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.net.ws-legacy :as ws]]})

(defn.xt client?
  "checks if a value is a wrapped nchan client descriptor"
  {:added "4.1.4"}
  [obj]
  (return (common/client? obj "nchan.client")))

(defn.xt raw-client
  "unwraps the tagged nchan client descriptor"
  {:added "4.1.4"}
  [client]
  (return (common/raw-client client "nchan.client")))

(defn.xt resolve-transport
  "resolves a websocket transport as a standard websocket driver or client"
  {:added "4.1.4"}
  [client]
  (return (common/resolve-transport client
                                    "nchan.client"
                                    "Nchan")))

(defn.xt resolve-base-url
  "resolves the base nchan url"
  {:added "4.1.4"}
  [_db client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key raw-client "base_url")
              (xt/x:get-key opts "base_url")
              nil)))

(defn.xt trim-trailing-slash
  "trims a single trailing slash"
  {:added "4.1.4"}
  [s]
  (return (common/trim-trailing-slash s)))

(defn.xt derive-websocket-base-url
  "normalizes a base url into a websocket base url"
  {:added "4.1.4"}
  [base-url]
  (when (or (xt/x:nil? base-url)
            (not (xt/x:is-string? base-url)))
    (return nil))
  (:= base-url (common/trim-trailing-slash base-url))
  (cond (str/starts-with? base-url "https://")
        (return (xt/x:cat "wss://"
                          (xt/x:str-substring base-url 8)))

        (str/starts-with? base-url "http://")
        (return (xt/x:cat "ws://"
                          (xt/x:str-substring base-url 7)))

        :else
        (return base-url)))

(defn.xt channel-group
  "Resolves the nchan channel group for an event transport."
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "channel_group")
              (xt/x:get-key raw-client "channel_group")
              "user")))

(defn.xt channel-id
  "Resolves the nchan channel id for an event transport."
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "channel_id")
              (xt/x:get-key raw-client "channel_id")
              "default")))

(defn.xt resolve-first-message
  "resolves the nchan first_message option"
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "first_message")
              (xt/x:get-key raw-client "first_message")
              nil)))

(defn.xt resolve-params
  "resolves websocket query params"
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return
   (xt/x:obj-assign
    (xt/x:obj-assign {}
                     (or (xt/x:get-key raw-client "params") {}))
    (or (xt/x:get-key opts "params") {}))))

(defn.xt resolve-subscriber-path
  "resolves the websocket subscriber path"
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "subscriber_path")
              (xt/x:get-key raw-client "subscriber_path")
              (xt/x:cat "/stream/" (-/channel-group client opts)))))

(defn.xt resolve-publisher-path
  "resolves the publisher path"
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "publisher_path")
              (xt/x:get-key raw-client "publisher_path")
              (xt/x:cat (-/resolve-subscriber-path client opts)
                        "/publish"))))

(defn.xt resolve-info-path
  "resolves the info path"
  {:added "4.1.4"}
  [client opts]
  (var raw-client (-/raw-client client))
  (return (or (xt/x:get-key opts "info_path")
              (xt/x:get-key raw-client "info_path")
              (xt/x:cat (-/resolve-subscriber-path client opts)
                        "/info"))))

(defn.xt encode-query-params
  "encodes a flat query param map"
  {:added "4.1.4"}
  [params]
  (return (common/encode-query-params params)))

(defn.xt create-scaffold
  "creates the scaffold used to connect to nchan"
  {:added "4.1.4"}
  [db client opts]
  (var base-url (-/resolve-base-url db client opts))
  (var params (-/resolve-params client opts))
  (xt/x:set-key params "id" (-/channel-id client opts))
  (when (xt/x:not-nil? (-/resolve-first-message client opts))
    (xt/x:set-key params
                  "first_message"
                  (-/resolve-first-message client opts)))
  (return {"client" client
           "base_url" base-url
           "websocket_base_url" (-/derive-websocket-base-url base-url)
           "channel_group" (-/channel-group client opts)
           "channel_id" (-/channel-id client opts)
           "subscriber_path" (-/resolve-subscriber-path client opts)
           "publisher_path" (-/resolve-publisher-path client opts)
           "info_path" (-/resolve-info-path client opts)
           "params" params}))

(defn.xt resolve-subscriber-url
  "resolves the nchan subscriber websocket url"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (var direct-url (or (xt/x:get-key opts "subscriber_url")
                      (xt/x:get-key raw-client "subscriber_url")
                      nil))
  (when (xt/x:not-nil? direct-url)
    (return direct-url))
  (var scaffold (-/create-scaffold db client opts))
  (var base-url (xt/x:get-key scaffold "websocket_base_url"))
  (when (xt/x:nil? base-url)
    (xt/x:err "Nchan missing subscriber_url/base_url"))
  (var query (-/encode-query-params (xt/x:get-key scaffold "params")))
  (if (== query "")
    (return (xt/x:cat base-url
                      (xt/x:get-key scaffold "subscriber_path")))
    (return (xt/x:cat base-url
                      (xt/x:get-key scaffold "subscriber_path")
                      "?"
                      query))))

(defn.xt resolve-publisher-url
  "resolves the nchan publisher url"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (var direct-url (or (xt/x:get-key opts "publisher_url")
                      (xt/x:get-key raw-client "publisher_url")
                      nil))
  (when (xt/x:not-nil? direct-url)
    (return direct-url))
  (var scaffold (-/create-scaffold db client opts))
  (var base-url (xt/x:get-key scaffold "base_url"))
  (when (xt/x:nil? base-url)
    (xt/x:err "Nchan missing publisher_url/base_url"))
  (var query (-/encode-query-params {"id" (xt/x:get-key scaffold "channel_id")}))
  (if (== query "")
    (return (xt/x:cat (common/trim-trailing-slash base-url)
                      (xt/x:get-key scaffold "publisher_path")))
    (return (xt/x:cat (common/trim-trailing-slash base-url)
                      (xt/x:get-key scaffold "publisher_path")
                      "?"
                      query))))

(defn.xt resolve-info-url
  "resolves the nchan info url"
  {:added "4.1.4"}
  [db client opts]
  (var raw-client (-/raw-client client))
  (var direct-url (or (xt/x:get-key opts "info_url")
                      (xt/x:get-key raw-client "info_url")
                      nil))
  (when (xt/x:not-nil? direct-url)
    (return direct-url))
  (var scaffold (-/create-scaffold db client opts))
  (var base-url (xt/x:get-key scaffold "base_url"))
  (when (xt/x:nil? base-url)
    (xt/x:err "Nchan missing info_url/base_url"))
  (return (xt/x:cat (common/trim-trailing-slash base-url)
                    (xt/x:get-key scaffold "info_path"))))

(defn.xt resolve-request-transform
  "resolves an optional payload->request transform"
  {:added "4.1.4"}
  [source opts]
  (return (common/resolve-request-transform source opts "nchan.client")))

(defn.xt client
  "wraps raw nchan config into the standard client descriptor.

   standard config keys:
   - transport
   - base_url
   - subscriber_url
   - publisher_url
   - info_url
   - channel_group
   - channel_id
   - first_message
   - params
   - request_transform"
  {:added "4.1.4"}
  [raw]
  (return (common/wrap-client raw "nchan.client")))

(defn.xt resolve-client
  "resolves the nchan client descriptor from db or opts"
  {:added "4.1.4"}
  [db opts]
  (var source (common/resolve-client-source db opts))
  (when (xt/x:nil? source)
    (xt/x:err "Nchan missing client"))
  (if (-/client? source)
    (return source)
    (return (-/client source))))

(defn.xt connect
  "connects the nchan client through the websocket protocol"
  {:added "4.1.4"}
  [db client opts]
  (var transport (-/resolve-transport client))
  (if (ws/client? transport)
    (return (promise/x:promise-run transport))
    (return (ws/connect transport
                        (-/resolve-subscriber-url db client opts)))))

(defn.xt extract-message-data
  "extracts websocket message data from raw events or payload strings"
  {:added "4.1.4"}
  [message]
  (return (common/extract-message-data message)))

(defn.xt decode-message
  "decodes websocket message data when it is JSON text"
  {:added "4.1.4"}
  [message]
  (return (common/decode-message message {"decode_if_json" true})))

(defn.xt request-payload
  "Encodes a native xt.db request for nchan transport."
  {:added "4.1.4"}
  [request source opts]
  (var raw-client (-/raw-client source))
  (var payload (or (common/unwrap-request request)
                   request))
  (when (xt/x:is-string? payload)
    (return payload))
  (when (or (xt/x:get-key opts "payload_envelope")
            (xt/x:get-key raw-client "payload_envelope"))
    (return (xt/x:json-encode {"event" (common/request-op payload)
                               "payload" payload})))
  (return (xt/x:json-encode payload)))

(defn.xt payload->request
  "Normalizes an nchan payload into a native xt.db request."
  {:added "4.1.4"}
  [payload source opts]
  (cond (common/request? payload)
        (return (common/unwrap-request payload))

        (xt/x:is-function? (-/resolve-request-transform source opts))
        (return ((-/resolve-request-transform source opts) payload source opts))

        (xt/x:is-string? payload)
        (return (-/payload->request (-/decode-message payload) source opts))

        :else
        (return nil)))

(defn.xt apply-request
  "applies a normalized xt.db request to the local cache db"
  {:added "4.1.4"}
  [local-db payload source opts]
  (var request (-/payload->request payload source opts))
  (when request
    (common/apply-request local-db request opts))
  (return [true request]))

(defn.xt handle-message
  "handles inbound nchan websocket frames carrying native xt.db requests"
  {:added "4.1.4"}
  [subscription message]
  (var payload (-/decode-message message))
  (var source (xt/x:get-key subscription "client"))
  (var opts (or (xt/x:get-key subscription "opts") {}))
  (var on-request (xt/x:get-key subscription "on_request"))
  (var local-db (xt/x:get-key subscription "local_db"))
  (var [ok request] (-/apply-request local-db payload source opts))
  (when (and request
             (xt/x:is-function? on-request))
    (on-request request payload message))
  (return (:? request
              request
              payload)))

(defn.xt subscribe
  "subscribes a local xt.db cache to an nchan websocket topic"
  {:added "4.1.4"}
  [source local-db opts]
  (:= source (or source {}))
  (:= opts (or opts {}))
  (var client (-/resolve-client source opts))
  (var connect-url (-/resolve-subscriber-url nil client opts))
  (var on-status (or (xt/x:get-key opts "on_status")
                     nil))
  (var on-request (or (xt/x:get-key opts "on_request")
                      nil))
  (return
   (promise/x:promise-then
    (-/connect nil client opts)
    (fn [socket]
      (var subscription {"::" "db.nchan.subscription"
                         "client" client
                         "socket" socket
                         "connect_url" connect-url
                         "local_db" local-db
                         "opts" opts
                         "active" true
                         "on_status" on-status
                         "on_request" on-request})
      (return
       (promise/x:promise-then
        (ws/add-listener
         socket
         "message"
         (fn [message]
           (return (-/handle-message subscription message))))
        (fn [_]
          (return
           (promise/x:promise-then
            (ws/add-listener
             socket
             "close"
             (fn [message]
               (xt/x:set-key subscription "active" false)
               (when (xt/x:is-function? on-status)
                 (on-status "CLOSED" message))
               (return message)))
            (fn [_]
              (do (when (xt/x:is-function? on-status)
                    (on-status "SUBSCRIBED" subscription))
                  (return subscription))))))))))))

(defn.xt subscription?
  "checks if the object is an nchan subscription handle"
  {:added "4.1.4"}
  [obj]
  (return (common/subscription? obj "db.nchan.subscription")))

(defn.xt subscription-active?
  "checks whether a subscription is still marked active"
  {:added "4.1.4"}
  [subscription]
  (return (common/subscription-active? subscription "db.nchan.subscription")))

(defn.xt unsubscribe
  "tears down an nchan subscription handle"
  {:added "4.1.4"}
  [subscription]
  (return (common/unsubscribe subscription
                              "db.nchan.subscription"
                              "event-nchan/unsubscribe")))
