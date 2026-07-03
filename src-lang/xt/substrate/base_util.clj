(ns xt.substrate.base-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-request :as node-request]]})

(defn.xt transport-get
  "gets an attached transport"
  {:added "4.1"}
  [node transport-id]
  (return (xt/x:get-key (xt/x:get-key node "transports")
                        transport-id)))

(defn.xt transport-list
  "lists active transport ids"
  {:added "4.1"}
  [node]
  (return (xtd/arr-sort (xt/x:obj-keys (xt/x:get-key node "transports"))
                        (fn [x] (return x))
                        xt/x:str-lt)))

(defn.xt transport-send
  "sends frames through a transport"
  {:added "4.1"}
  [node transport-id frame]
  (var transport (-/transport-get node transport-id))
  (when (xt/x:nil? transport)
    (xt/x:err (xt/x:cat "transport not found - " transport-id)))
  (var send-fn (xt/x:get-key transport "send_fn"))
  (when (xt/x:nil? send-fn)
    (xt/x:err (xt/x:cat "transport missing send_fn - " transport-id)))
  (return (node-request/ensure-promise
           (send-fn frame))))

(defn.xt transport-broadcast-loop
  "internal helper for broadcasting across transports"
  {:added "4.1"}
  [node ids frame exclude-id index]
  (when (>= index (xt/x:len ids))
    (return (promise/x:promise-run frame)))
  (var transport-id (xt/x:get-idx ids (xt/x:offset index)))
  (if (== transport-id exclude-id)
    (return (-/transport-broadcast-loop node ids frame exclude-id (+ index 1)))
    (return
     (promise/x:promise-then
      (-/transport-send node transport-id frame)
      (fn [_]
        (return (-/transport-broadcast-loop node ids frame exclude-id (+ index 1))))))))

(defn.xt transport-request-target
  "resolves the outbound request target transport"
  {:added "4.1"}
  [node meta]
  (var target (xt/x:get-key meta "transport_id"))
  (when (xt/x:not-nil? target)
    (return target))
  (var transports (-/transport-list node))
  (when (== 0 (xt/x:len transports))
    (return nil))
  (return (xt/x:get-idx transports (xt/x:offset 0))))

(defn.xt stream-route-loop
  "internal helper for routing streams to subscribed transports"
  {:added "4.1"}
  [node ids frame exclude-id index]
  (when (>= index (xt/x:len ids))
    (return (promise/x:promise-run frame)))
  (var transport-id (xt/x:get-idx ids (xt/x:offset index)))
  (if (== transport-id exclude-id)
    (return (-/stream-route-loop node ids frame exclude-id (+ index 1)))
    (return
     (promise/x:promise-then
      (-/transport-send node transport-id frame)
      (fn [_]
        (return (-/stream-route-loop node ids frame exclude-id (+ index 1))))))))

(defn.xt pending-await
  "waits for a pending request state to settle"
  {:added "4.1"}
  [state]
  (var status (xt/x:get-key state "status"))
  (cond (== status "resolved")
        (return (promise/x:promise-run
                 (xt/x:get-key state "value")))

        (== status "rejected")
        (return
         (promise/x:promise
          (fn []
            (xt/x:throw (xt/x:get-key state "error")))))

        :else
        (return
         (promise/x:promise-then
          (xt/x:async-run
           (fn []
             (return nil)))
          (fn [_]
            (return (-/pending-await state)))))))

(defn.xt request-context-merge
  "merges transport context into request meta"
  {:added "4.1"}
  [request ctx]
  (:= ctx (or ctx {}))
  (var meta (xt/x:get-key request "meta"))
  (when (xt/x:nil? meta)
    (:= meta {})
    (xt/x:set-key request "meta" meta))
  (when (xt/x:not-nil? (xt/x:get-key ctx "transport_id"))
    (xt/x:set-key meta
                 "transport_id"
                 (xt/x:get-key ctx "transport_id")))
  (return request))

(defn.xt response-ok
  "constructs and optionally sends a successful response"
  {:added "4.1"}
  [node request data meta ctx]
  (var response (frame/response-ok-frame
                 (xt/x:get-key request "id")
                 (xt/x:get-key request "space")
                 data
                 meta))
  (var transport-id (xt/x:get-key ctx "transport_id"))
  (when (xt/x:nil? transport-id)
    (var request-meta (xt/x:get-key request "meta"))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (xt/x:get-key request-meta "transport_id"))))
  (if (xt/x:nil? transport-id)
    (return (promise/x:promise-run response))
    (return
     (promise/x:promise-then
      (-/transport-send node transport-id response)
      (fn [_]
        (return response))))))

(defn.xt response-error
  "constructs and optionally sends an errored response"
  {:added "4.1"}
  [node request error meta ctx]
  (var response (frame/response-error-frame
                 (xt/x:get-key request "id")
                 (xt/x:get-key request "space")
                 error
                 meta))
  (var transport-id (xt/x:get-key ctx "transport_id"))
  (when (xt/x:nil? transport-id)
    (var request-meta (xt/x:get-key request "meta"))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (xt/x:get-key request-meta "transport_id"))))
  (if (xt/x:nil? transport-id)
    (return (promise/x:promise-run response))
    (return
     (promise/x:promise-then
      (-/transport-send node transport-id response)
      (fn [_]
        (return response))))))

(defn.xt config-normalize-space
  "normalises declarative space config into create-space opts"
  {:added "4.1"}
  [space-id config]
  (cond (xt/x:nil? config)
       (return nil)

       (and (xt/x:is-object? config)
            (or (xt/x:has-key? config "id")
                (xt/x:has-key? config "state")
                (xt/x:has-key? config "meta")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             space-id)))
           (xt/x:err (xt/x:cat "space id mismatch - " space-id)))
         (return {:state (xt/x:get-key config "state")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid space config - " space-id))))

(defn.xt config-normalize-handler
  "normalises handler config from fn or declarative entry"
  {:added "4.1"}
  [action config]
  (cond (xt/x:is-function? config)
       (return {:fn config
                :meta {}})

       (and (xt/x:is-object? config)
            (xt/x:is-function? (xt/x:get-key config "fn")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             action)))
           (xt/x:err (xt/x:cat "handler id mismatch - " action)))
         (return {:fn (xt/x:get-key config "fn")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid handler config - " action))))

(defn.xt config-normalize-trigger
  "normalises trigger config from fn or declarative entry"
  {:added "4.1"}
  [signal config]
  (cond (xt/x:is-function? config)
       (return {:fn config
                :meta {}})

       (and (xt/x:is-object? config)
            (xt/x:is-function? (xt/x:get-key config "fn")))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (xt/x:get-key config "id")
                             signal)))
           (xt/x:err (xt/x:cat "trigger id mismatch - " signal)))
         (return {:fn (xt/x:get-key config "fn")
                  :meta (or (xt/x:get-key config "meta") {})}))

       :else
       (xt/x:err (xt/x:cat "invalid trigger config - " signal))))

(defn.xt node-base-opts
  "removes declarative config keys before constructing node state"
  {:added "4.1"}
  [opts]
  (var base (xt/x:obj-clone (or opts {})))
  (xt/x:del-key base "spaces")
  (xt/x:del-key base "handlers")
  (xt/x:del-key base "triggers")
  (return base))
