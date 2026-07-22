(ns xt.substrate.base-util
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate.base-frame :as frame]
             [xt.substrate.base-request :as node-request]
             [xt.substrate.base-router :as router]
             [xt.substrate.base-pubsub :as node-pubsub]]})

(defn.xt transport-get
  "gets an attached transport"
  {:added "4.1"}
  [node transport-id]
  (return (xt/x:get-key (. node ["transports"])
                        transport-id)))

(defn.xt transport-list
  "lists active transport ids"
  {:added "4.1"}
  [node]
  (return (xtd/arr-sort (xt/x:obj-keys (. node ["transports"]))
                        (fn [x] (return x))
                        xt/x:str-lt)))

(defn.xt transport-send
  "sends frames through a transport"
  {:added "4.1"}
  [node transport-id frame]
  (var transport (-/transport-get node transport-id))
  (when (xt/x:nil? transport)
    (xt/x:err (xt/x:cat "transport not found - " transport-id)))
  (var send-fn (. transport ["send_fn"]))
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
  (when (. meta ["local"])
    (return nil))
  (var target (. meta ["transport_id"]))
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
  (var status (. state ["status"]))
  (cond (== status "resolved")
        (return (promise/x:promise-run
                 (. state ["value"])))

        (== status "rejected")
        (return
         (promise/x:promise-new
          (fn [_ reject]
            (reject (. state ["error"])))))

        :else
        (return
         (promise/x:promise-then
          (promise/x:with-delay 1
           (fn [] (return nil)))
          (fn [_]
            (return (-/pending-await state)))))))

(defn.xt request-context-merge
  "merges transport context into request meta"
  {:added "4.1"}
  [request ctx]
  (:= ctx (or ctx {}))
  (var meta (. request ["meta"]))
  (when (xt/x:nil? meta)
    (:= meta {})
    (xt/x:set-key request "meta" meta))
  (when (xt/x:not-nil? (. ctx ["transport_id"]))
    (xt/x:set-key meta
                 "transport_id"
                 (. ctx ["transport_id"])))
  (return request))

(defn.xt response-ok
  "constructs and optionally sends a successful response"
  {:added "4.1"}
  [node request data meta ctx]
  (var response (frame/response-ok-frame
                 (. request ["id"])
                 (. request ["space"])
                 data
                 meta))
  (var transport-id (. ctx ["transport_id"]))
  (when (xt/x:nil? transport-id)
    (var request-meta (. request ["meta"]))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (. request-meta ["transport_id"]))))
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
                 (. request ["id"])
                 (. request ["space"])
                 error
                 meta))
  (var transport-id (. ctx ["transport_id"]))
  (when (xt/x:nil? transport-id)
    (var request-meta (. request ["meta"]))
    (when (xt/x:not-nil? request-meta)
      (:= transport-id (. request-meta ["transport_id"]))))
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
                    (not (== (. config ["id"])
                             space-id)))
           (xt/x:err (xt/x:cat "space id mismatch - " space-id)))
         (return {:state (. config ["state"])
                  :meta (or (. config ["meta"]) {})}))

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
            (xt/x:is-function? (. config ["fn"])))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (. config ["id"])
                             action)))
           (xt/x:err (xt/x:cat "handler id mismatch - " action)))
         (return {:fn (. config ["fn"])
                  :meta (or (. config ["meta"]) {})}))

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
            (xt/x:is-function? (. config ["fn"])))
       (do
         (when (and (xt/x:has-key? config "id")
                    (not (== (. config ["id"])
                             signal)))
           (xt/x:err (xt/x:cat "trigger id mismatch - " signal)))
         (return {:fn (. config ["fn"])
                  :meta (or (. config ["meta"]) {})}))

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

(defn.xt register-handler
  "registers a shared request handler"
  {:added "4.1"}
  [node action handler meta]
  (var entry {:id action
              :fn handler
              :meta (or meta {})})
  (xt/x:set-key (. node ["handlers"])
                action
                entry)
  (return entry))

(defn.xt unregister-handler
  "unregisters a shared request handler"
  {:added "4.1"}
  [node action]
  (var handlers (. node ["handlers"]))
  (var prev (xt/x:get-key handlers action))
  (xt/x:del-key handlers action)
  (return prev))

(defn.xt get-handler
  "gets a shared request handler"
  {:added "4.1"}
  [node action]
  (return (xt/x:get-key (. node ["handlers"])
                        action)))

(defn.xt list-handlers
  "lists registered request handlers"
  {:added "4.1"}
  [node]
  (return (xtd/arr-sort (xt/x:obj-keys (. node ["handlers"]))
                        (fn [x] (return x))
                        xt/x:str-lt)))

(defn.xt register-trigger
  "registers a shared stream trigger"
  {:added "4.1"}
  [node signal trigger-fn meta]
  (var entry {:id signal
              :fn trigger-fn
              :meta (or meta {})})
  (xt/x:set-key (. node ["triggers"])
                signal
                entry)
  (return entry))

(defn.xt unregister-trigger
  "unregisters a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (var triggers (. node ["triggers"]))
  (var prev (xt/x:get-key triggers signal))
  (xt/x:del-key triggers signal)
  (return prev))

(defn.xt get-trigger
  "gets a shared stream trigger"
  {:added "4.1"}
  [node signal]
  (return (xt/x:get-key (. node ["triggers"])
                        signal)))

(defn.xt list-triggers
  "lists registered stream triggers"
  {:added "4.1"}
  [node]
  (return (xtd/arr-sort (xt/x:obj-keys (. node ["triggers"]))
                        (fn [x] (return x))
                        xt/x:str-lt)))

(defn.xt request
  "issues a request locally or over an attached transport"
  {:added "4.1"}
  [node space action args meta]
  (:= meta (or meta {}))
  (var request-frame (frame/request-frame space action args meta))
  (var target (-/transport-request-target node meta))
  (if (xt/x:nil? target)
    (return
     (node-request/invoke-handler node request-frame))
    (return
     (promise/x:promise-new
      (fn [resolve reject]
        (node-request/add-pending node
                                  request-frame
                                  resolve
                                  reject
                                  {:transport_id target})
        (try
          (return
           (promise/x:promise-catch
            (-/transport-send node target request-frame)
            (fn [err]
              (node-request/remove-pending node
                                           (. request-frame ["id"]))
              (return (reject err)))))
          (catch err
            (node-request/remove-pending node
                                         (. request-frame ["id"]))
            (return (reject err)))))))))

(defn.xt publish
  "publishes a stream frame through node core and subscribed transports"
  {:added "4.1"}
  [node space signal data meta]
  (:= meta (or meta {}))
  (var stream (frame/stream-frame space
                                  signal
                                  data
                                  meta
                                  (. meta ["cause"])))
  (return
   (promise/x:promise-then
    (node-pubsub/receive-publish node stream)
    (fn [_]
      (return (-/stream-route-loop node
                                  (router/target-ids node
                                                     (. stream ["space"])
                                                     (. stream ["signal"]))
                                  stream
                                  (. meta ["transport_id"])
                                  0))))))
