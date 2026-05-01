(ns xt.isolate.client
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-trace :as trace]
             [xt.lang.common-string :as str]
             [xt.lang.spec-promise :as spec-promise]
             [xt.isolate.frame :as frame]]})

;;
;; client.clj – outer async interface to an isolate
;;
;; The client holds a transport capability map plus bookkeeping for:
;;   - "active" calls  – in-flight call/response pairs keyed by id
;;   - "subscriptions" – topic subscribers keyed by a user-assigned key
;;
;; Design goals:
;;   - no postMessage / postRequest assumptions
;;   - works wherever a transport map can be provided
;;   - call! returns a Promise resolved/rejected by the isolate response
;;   - notify! is fire-and-forget (no Promise)
;;   - subscribe! registers a handler for "stream" events from the isolate
;;

;;
;; Spec declarations
;;

(defspec.xt client-listener-call
  [:fn [xt.isolate.spec/ResponseFrame
        xt.isolate.spec/ActiveCallMap]
   :xt/any])

(defspec.xt client-listener-event
  [:fn [xt.isolate.spec/ResponseFrame
        xt.isolate.spec/SubscriptionMap]
   xt.isolate.spec/StringList])

(defspec.xt client-listener
  [:fn [xt.isolate.spec/ResponseFrame
        xt.isolate.spec/ActiveCallMap
        xt.isolate.spec/SubscriptionMap]
   :xt/any])

(defspec.xt client-create
  [:fn [xt.isolate.spec/TransportMap]
   xt.isolate.spec/ClientRecord])

(defspec.xt client-active
  [:fn [xt.isolate.spec/ClientRecord]
   xt.isolate.spec/ActiveCallMap])

(defspec.xt client-subscribe
  [:fn [xt.isolate.spec/ClientRecord
        :xt/str
        :xt/any
        [:fn [:xt/any :xt/any] :xt/any]]
   [:xt/array :xt/any]])

(defspec.xt client-subscriptions
  [:fn [xt.isolate.spec/ClientRecord]
   xt.isolate.spec/StringList])

(defspec.xt client-unsubscribe
  [:fn [xt.isolate.spec/ClientRecord :xt/str]
   [:xt/array :xt/any]])

(defspec.xt client-call-id
  [:fn [xt.isolate.spec/ClientRecord] :xt/str])

(defspec.xt client-call
  [:fn [xt.isolate.spec/ClientRecord
        xt.isolate.spec/RequestFrame]
   :xt/any])

(defspec.xt client-notify
  [:fn [xt.isolate.spec/ClientRecord
        xt.isolate.spec/RequestFrame]
   :xt/any])

;;
;; client-listener-call
;;
;; Called when a response arrives for an outstanding call.
;; Resolves or rejects the stored Promise.
;;

(defn.xt client-listener-call
  "resolves or rejects an active call from a response frame"
  {:added "4.0"}
  [data active]
  (var #{op id status body} data)
  (var entry (xt/x:get-key active id))
  (when (xt/x:not-nil? entry)
    (var #{input time resolve reject} entry)
    (:= input (or input {}))
    (try
      (cond (== status "ok")
            (do (xt/x:del-key active id)
                (return (resolve body)))
            :else
            (do (xt/x:del-key active id)
                (return (reject (xt/x:obj-assign {:route  (. input ["route"])
                                                  :input  (. input ["body"])
                                                  :start-time time
                                                  :end-time (xt/x:now-ms)}
                                                 data)))))
      (catch err (return (reject {:op    op
                                  :id    id
                                  :route (. input ["route"])
                                  :status "error"
                                  :message "Format Invalid"
                                  :input (. input ["body"])
                                  :start-time time
                                  :end-time (xt/x:now-ms)
                                  :body body}))))))

;;
;; client-listener-event
;;
;; Called when a "stream" frame arrives.
;; Dispatches to all matching topic subscriptions.
;;

(defn.xt client-listener-event
  "dispatches stream events to matching subscriptions"
  {:added "4.0"}
  [data subscriptions]
  (var #{topic} data)
  (var out [])
  (xt/for:object [[sub-key entry] subscriptions]
    (var #{pred handler} entry)
    (when (frame/check-topic pred topic data)
      (try (handler data topic)
           (xt/x:arr-push out sub-key)
           (catch err (trace/LOG! {:stack   (. err ["stack"])
                                   :message (. err ["message"])})))))
  (return out))

;;
;; client-listener
;;
;; Dispatches incoming frames to call-listener or event-listener.
;;

(defn.xt client-listener
  "routes an incoming response frame to the correct handler"
  {:added "4.0"}
  [data active subscriptions]
  (var #{op id} data)
  (cond (or (== op "call")
            (== op "notify")
            (== op "eval"))
        (return (-/client-listener-call data active))

        (== op "stream")
        (return (-/client-listener-event data subscriptions))))

;;
;; client-create
;;
;; Builds a ClientRecord from a transport map and wires the listener.
;;

(defn.xt client-create
  "creates a client from a transport map"
  {:added "4.0"}
  [transport]
  (var active        {})
  (var subscriptions {})
  (var id            (frame/rand-id "" 3))
  (var client        {"::"           "isolate.client"
                      :id            id
                      :transport     transport
                      :active        active
                      :subscriptions subscriptions})
  (var listen (. transport ["listen"]))
  (listen (fn [data]
            (-/client-listener data active subscriptions)))
  (return client))

;;
;; Accessors
;;

(defn.xt client-active
  "returns the active-call map"
  {:added "4.0"}
  [client]
  (return (xt/x:get-key client "active")))

(defn.xt client-subscribe
  "adds a topic subscription to the client"
  {:added "4.0"}
  [client key pred handler]
  (var prev (. client ["subscriptions"] [key]))
  (:= (. client ["subscriptions"] [key])
      {:key     key
       :pred    pred
       :handler handler})
  (return [prev]))

(defn.xt client-subscriptions
  "lists subscription keys on the client"
  {:added "4.0"}
  [client]
  (return (xt/x:obj-keys (. client ["subscriptions"]))))

(defn.xt client-unsubscribe
  "removes a topic subscription from the client"
  {:added "4.0"}
  [client key]
  (var prev (. client ["subscriptions"] [key]))
  (del (. client ["subscriptions"] [key]))
  (return [prev]))

;;
;; call-id
;;
;; Returns a fresh correlation id that is not currently in flight.
;;

(defn.xt client-call-id
  "generates a fresh correlation id for the client"
  {:added "4.0"}
  [client]
  (var #{id active} client)
  (while true
    (var cid (frame/rand-id (+ id "-") 3))
    (when (xt/x:nil? (xt/x:get-key active cid))
      (return cid))))

;;
;; client-call
;;
;; Sends a RequestFrame over the transport and returns a Promise.
;; The Promise is resolved/rejected when the response arrives.
;;

(defn.xt client-call
  "sends a request frame and returns a Promise for the response"
  {:added "4.0"}
  [client event]
  (var #{transport active} client)
  (var cid (or (. event ["id"])
               (-/client-call-id client)))
  (:= (. event ["id"]) cid)
  (var p (new Promise
              (fn [resolve reject]
                (:= (. active [cid])
                    {:resolve (fn [data] (resolve data))
                     :reject  (fn [data] (reject data))
                     :input   event
                     :time    (xt/x:now-ms)}))))
  (var send (. transport ["send"]))
  (send event)
  (return p))

;;
;; client-notify
;;
;; Sends a frame without expecting a response (fire-and-forget).
;;

(defn.xt client-notify
  "sends a fire-and-forget notification frame"
  {:added "4.0"}
  [client event]
  (var #{transport} client)
  (var send (. transport ["send"]))
  (return (send event)))
