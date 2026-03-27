(ns js.cell-v2.link
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell.kernel.base-util :as legacy-util]
             [js.cell-v2 :as cell-v2]
             [js.cell-v2.protocol :as protocol]
             [js.cell-v2.transport :as transport]
             [js.cell-v2.transport-http :as transport-http]
             [js.cell-v2.transport-legacy :as legacy-transport]
             [js.cell-v2.transport-socket :as socket-transport]
             [js.cell-v2.transport-worker :as worker-transport]]})

(defn.js callback-signals
  "derives transport subscription signals from a callback predicate"
  {:added "4.0"}
  [pred]
  (cond (or (k/nil? pred)
            (== false pred))
        (return [])

        (or (== true pred)
            (k/fn? pred))
        (return ["*"])

        (k/obj? pred)
        (return (k/obj-keys pred))

        :else
        (return [pred])))

(defn.js dispatch-callbacks
  "dispatches an inbound event to registered callbacks"
  {:added "4.0"}
  [link input signal bus]
  (var out [])
  (k/for:object [[callback-id entry] (. link ["callbacks"])]
    (var pred (. entry ["pred"]))
    (var handler (. entry ["handler"]))
    (when (legacy-util/check-event pred signal input {:link link
                                                      :bus bus})
      (try
        (handler input signal link)
        (x:arr-push out callback-id)
        (catch err
          (k/LOG! {:stack (. err ["stack"])
                   :message (. err ["message"])})))))
  (return out))

(defn.js send-subscription
  "sends a subscribe or unsubscribe frame when supported"
  {:added "4.0"}
  [link signal subscribe]
  (when (. link ["supportsSubscribe"])
    (var tx (. link ["transport"]))
    (var prefix "unsub")
    (when subscribe
      (:= prefix "sub"))
    (var frame-id (transport/next-id tx prefix))
    (if subscribe
      (transport/send-frame tx
                            (protocol/subscribe frame-id signal nil))
      (transport/send-frame tx
                            (protocol/unsubscribe frame-id signal nil))))
  (return signal))

(defn.js update-signal-count
  "updates local callback counts for a signal and mirrors subscription changes"
  {:added "4.0"}
  [link signal delta]
  (var counts (. link ["signalCounts"]))
  (var prev (or (. counts [signal])
                0))
  (var next (+ prev delta))
  (cond (> next 0)
        (do (:= (. counts [signal]) next)
            (when (and (<= prev 0)
                       (> next 0))
              (-/send-subscription link signal true))
            (return next))

        :else
        (do (when (> prev 0)
              (del (. counts [signal]))
              (-/send-subscription link signal false))
            (return 0))))

(defn.js sync-callback-signals
  "updates subscription counts for a callback entry"
  {:added "4.0"}
  [link entry delta]
  (when entry
    (k/for:array [signal (or (. entry ["signals"]) [])]
      (-/update-signal-count link signal delta)))
  (return entry))

(defn.js make-link
  "creates a link wrapper around a transport"
  {:added "4.0"}
  [tx opts]
  (:= opts (or opts {}))
  (var system (or (. opts ["system"])
                  (cell-v2/make-system {})))
  (var id (or (. opts ["id"])
              (. tx ["id"])
              (legacy-util/rand-id "link-" 3)))
  (var listener-id (k/cat id "::callbacks"))
  (var supports-subscribe (. opts ["supportsSubscribe"]))
  (when (k/nil? supports-subscribe)
    (:= supports-subscribe true))
  (var link {"::" "cell-v2.link"
             :id id
             :transport tx
             :system system
             :listenerId listener-id
             :supportsSubscribe supports-subscribe
             :callbacks {}
             :signalCounts {}})
  (transport/bind-system tx
                         system
                         {:forwardAll false
                          :listenerId (k/cat id "::transport")})
  (cell-v2/add-event-listener
   system
   listener-id
   true
   (fn [input signal bus]
     (return (-/dispatch-callbacks link input signal bus))))
  (return link))

(defn.js link-active
  "gets the currently active call entries"
  {:added "4.0"}
  [link]
  (return (. (. link ["transport"]) ["pending"])))

(defn.js add-callback
  "adds a callback to the link"
  {:added "4.0"}
  [link key pred handler]
  (var prev (. link ["callbacks"] [key]))
  (-/sync-callback-signals link prev -1)
  (var entry {:key key
              :pred pred
              :handler handler
              :signals (-/callback-signals pred)})
  (:= (. link ["callbacks"] [key]) entry)
  (-/sync-callback-signals link entry 1)
  (return [prev]))

(defn.js list-callbacks
  "lists the callback ids registered on the link"
  {:added "4.0"}
  [link]
  (return (k/obj-keys (. link ["callbacks"]))))

(defn.js remove-callback
  "removes a callback from the link"
  {:added "4.0"}
  [link key]
  (var prev (. link ["callbacks"] [key]))
  (-/sync-callback-signals link prev -1)
  (del (. link ["callbacks"] [key]))
  (return [prev]))

(defn.js call-id
  "gets the next call id for a link"
  {:added "4.0"}
  [link]
  (return (transport/next-id (. link ["transport"])
                             "call")))

(defn.js send-call
  "sends a call frame over the link transport"
  {:added "4.0"}
  [link action body meta handlers id]
  (var tx (. link ["transport"]))
  (var call-id (or id
                   (transport/next-id tx "call")))
  (var frame (protocol/call call-id
                            action
                            body
                            meta))
  (var started (k/now-ms))
  (var task-handler (k/get-key handlers "task"))
  (var p (new Promise
          (fn [resolve reject]
            (:= (. tx ["pending"] [call-id])
                {:resolve resolve
                 :reject (fn [result]
                           (return (reject (j/assign {:action action
                                                     :input body
                                                     :startTime started
                                                     :endTime (k/now-ms)}
                                                    result))))
                 :callId call-id
                 :task task-handler
                 :action action
                 :body body
                 :meta meta
                 :time started}))))
  (try
    (transport/send-frame tx frame)
    (catch err
      (transport/clear-pending tx call-id)
      (throw err)))
  (return p))

(defn.js call-action
  "calls an action over the link"
  {:added "4.0"}
  [link action input opts]
  (:= opts (or opts {}))
  (var body (. opts ["body"]))
  (when (k/nil? body)
    (:= body {:input input}))
  (var meta (or (. opts ["meta"])
                {}))
  (var handlers {:task (. opts ["task"])})
  (return (-/send-call link
                       action
                       body
                       meta
                       handlers
                       (. opts ["id"]))))

(defn.js call-route
  "calls a route action over the link"
  {:added "4.0"}
  [link action input opts]
  (return (-/call-action link action input opts)))

(defn.js call-remote
  "calls a remote action over the link"
  {:added "4.0"}
  [link key input opts]
  (return (-/call-action link
                         (k/cat "remote/" key)
                         input
                         opts)))

(defn.js call-store
  "calls a store action over the link"
  {:added "4.0"}
  [link op key input opts]
  (:= opts (or opts {}))
  (var body {:store key
             :input input})
  (var raw-body (. opts ["body"]))
  (when raw-body
    (:= body raw-body))
  (return (-/call-action link
                         (k/cat "store/" op)
                         input
                         {:id (. opts ["id"])
                          :meta (. opts ["meta"])
                          :task (. opts ["task"])
                          :body body})))

(defn.js call-db
  "calls a db action over the link"
  {:added "4.0"}
  [link op key input opts]
  (:= opts (or opts {}))
  (var body {:db key
             :input input})
  (var raw-body (. opts ["body"]))
  (when raw-body
    (:= body raw-body))
  (return (-/call-action link
                         (k/cat "db/" op)
                         input
                         {:id (. opts ["id"])
                          :meta (. opts ["meta"])
                          :task (. opts ["task"])
                          :body body})))

(defn.js call
  "compatibility call wrapper for legacy route/eval payloads"
  {:added "4.0"}
  [link event]
  (var op (k/get-key event "op"))
  (var id (k/get-key event "id"))
  (var task-handler (k/get-key event "task"))
  (cond (== op "route")
        (return (-/call-action link
                               (k/get-key event "route")
                               (k/get-key event "body")
                               {:id id
                                :meta {:legacyOp "route"}
                                :task task-handler}))

        (== op "eval")
        (return (-/call-action link
                               "@/eval"
                               [(k/get-key event "body")]
                               {:id id
                                :meta {:legacyOp "eval"
                                       :annex "debug"}
                                :task task-handler}))

        :else
        (k/err (k/cat "ERR - Link op not supported - " op))))

(defn.js worker-source
  "normalizes a worker source into an object"
  {:added "4.0"}
  [source]
  (cond (or (k/is-string? source)
            (k/fn? source))
        (return (new Worker source))

        :else
        (return source)))

(defn.js source-create-fn
  "gets a create-fn from normalized source input"
  {:added "4.0"}
  [source]
  (return (or (k/get-key source "create-fn")
              (k/get-key source "create_fn"))))

(defn.js send-worker-payload
  "sends a payload to a worker-like object"
  {:added "4.0"}
  [worker payload]
  (cond (. worker ["postRequest"])
        (return ((. worker ["postRequest"]) payload))

        (. worker ["postMessage"])
        (return (. worker (postMessage payload)))

        :else
        (k/err "ERR - Worker source has no postMessage/postRequest")))

(defn.js make-factory-worker-link
  "creates a link from a create-fn worker factory"
  {:added "4.0"}
  [source opts legacy]
  (:= opts (or opts {}))
  (var tx (transport/make-transport nil
                                    {:id (or (. opts ["transportId"])
                                             (. opts ["id"]))}))
  (var create-fn (-/source-create-fn source))
  (var worker nil)
  (cond legacy
        (do (:= worker (create-fn
                        (fn [data]
                          (return (legacy-transport/receive-legacy tx data)))))
            (:= (. tx ["send"])
                (fn [frame]
                  (return (-/send-worker-payload worker
                                                 (legacy-transport/frame->legacy tx frame))))))

        :else
        (do (:= worker (create-fn
                        (fn [data]
                          (return (transport/receive-frame tx data nil)))))
            (:= (. tx ["send"])
                (fn [frame]
                  (return (-/send-worker-payload worker frame))))))
  (:= (. tx ["channel"]) worker)
  (:= (. tx ["worker"]) worker)
  (return (-/make-link tx
                       (j/assign {:supportsSubscribe (not legacy)}
                                 opts))))

(defn.js make-worker-link
  "creates a protocol worker link"
  {:added "4.0"}
  [source opts]
  (:= opts (or opts {}))
  (var create-fn (-/source-create-fn source))
  (when create-fn
    (return (-/make-factory-worker-link source opts false)))
  (var worker (-/worker-source source))
  (var tx (worker-transport/make-worker-transport worker
                                                  {:id (or (. opts ["transportId"])
                                                           (. opts ["id"]))}))
  (return (-/make-link tx
                       (j/assign {:supportsSubscribe true}
                                 opts))))

(defn.js make-legacy-worker-link
  "creates a legacy worker link"
  {:added "4.0"}
  [source opts]
  (:= opts (or opts {}))
  (var create-fn (-/source-create-fn source))
  (when create-fn
    (return (-/make-factory-worker-link source opts true)))
  (var worker (-/worker-source source))
  (var tx (legacy-transport/make-legacy-worker-transport worker
                                                         {:id (or (. opts ["transportId"])
                                                                  (. opts ["id"]))}))
  (return (-/make-link tx
                       (j/assign {:supportsSubscribe false}
                                 opts))))

(defn.js make-socket-link
  "creates a socket-backed link"
  {:added "4.0"}
  [socket opts]
  (:= opts (or opts {}))
  (var tx (socket-transport/make-socket-transport socket
                                                  {:id (or (. opts ["transportId"])
                                                           (. opts ["id"]))
                                                   :encode (. opts ["encode"])
                                                   :decode (. opts ["decode"])}))
  (return (-/make-link tx
                       (j/assign {:supportsSubscribe true}
                                 opts))))

(defn.js make-http-link
  "creates an http-backed link"
  {:added "4.0"}
  [request opts]
  (:= opts (or opts {}))
  (var tx (transport-http/make-http-transport request
                                              {:id (or (. opts ["transportId"])
                                                       (. opts ["id"]))
                                               :encode (. opts ["encode"])
                                               :decode (. opts ["decode"])}))
  (return (-/make-link tx
                       (j/assign {:supportsSubscribe false}
                                 opts))))
