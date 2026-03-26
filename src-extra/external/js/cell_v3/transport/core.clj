(ns js.cell-v3.transport.core
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell-v3.kernel.protocol :as protocol]]})

(defn.js make-transport
  "creates a transport binding around a channel"
  {:added "4.0"}
  [channel opts]
  (:= opts (or opts {}))
  (return {"::" "cell-v3.transport"
           :id (or (. opts ["id"])
                   "transport")
           :channel channel
           :send (. opts ["send"])
           :pending {}
           :tasks {}
           :subscriptions {}
           :counter 0
           :system (. opts ["system"])
           :listenerId (. opts ["listenerId"])
           :forwardAll (. opts ["forwardAll"])
           :handleCall (. opts ["handleCall"])
           :handleEmit (. opts ["handleEmit"])
           :context (. opts ["context"])
           :state {}}))

(defn.js next-id
  "returns the next transport id"
  {:added "4.0"}
  [transport prefix]
  (var n (+ 1 (or (. transport ["counter"])
                  0)))
  (:= (. transport ["counter"]) n)
  (return (k/cat (or prefix
                     (. transport ["id"])
                     "transport")
                 "-"
                 n)))

(defn.js send-frame
  "sends a frame over the transport"
  {:added "4.0"}
  [transport frame]
  (var send-fn (or (. transport ["send"])
                   (k/get-key (. transport ["channel"]) "send")))
  (cond send-fn
        (return (send-fn frame transport))

        (k/get-key (. transport ["channel"]) "postMessage")
        (return (j/postMessage (. transport ["channel"]) frame))

        :else
        (k/err "ERR - Transport cannot send frame")))

(defn.js list-subscriptions
  "lists transport subscriptions"
  {:added "4.0"}
  [transport]
  (return (k/obj-keys (. transport ["subscriptions"]))))

(defn.js list-tasks
  "lists tracked task refs"
  {:added "4.0"}
  [transport]
  (return (k/obj-keys (. transport ["tasks"]))))

(defn.js get-task
  "gets a tracked task entry"
  {:added "4.0"}
  [transport task-ref]
  (return (. transport ["tasks"] [task-ref])))

(defn.js subscribe-signal
  "subscribes the transport to a signal"
  {:added "4.0"}
  [transport signal]
  (var prev (. transport ["subscriptions"] [signal]))
  (:= (. transport ["subscriptions"] [signal]) true)
  (return prev))

(defn.js unsubscribe-signal
  "unsubscribes the transport from a signal"
  {:added "4.0"}
  [transport signal]
  (var prev (. transport ["subscriptions"] [signal]))
  (del (. transport ["subscriptions"] [signal]))
  (return prev))

(defn.js subscribed?
  "checks if a signal should be forwarded"
  {:added "4.0"}
  [transport signal]
  (return (or (== true (. transport ["forwardAll"]))
              (. transport ["subscriptions"] ["*"])
              (. transport ["subscriptions"] [signal]))))

(defn.js promise?
  "checks if a value is a promise"
  {:added "4.0"}
  [out]
  (return (and (k/not-nil? out)
               (k/not-nil? (. out ["constructor"]))
               (== "Promise" (. out ["constructor"] ["name"])))))

(defn.js error-body
  "normalizes an error body"
  {:added "4.0"}
  [err]
  (cond (k/is-string? err)
        (return {:message err})

        :else
        (do (var body {:message (or (. err ["message"])
                                    "Transport Error")})
            (when (. err ["stack"])
              (k/set-key body "stack" (. err ["stack"])))
            (return body))))

(defn.js normalize-call-output
  "normalizes a call result for protocol responses"
  {:added "4.0"}
  [out]
  (if (and (k/obj? out)
           (k/not-nil? (k/get-key out "status"))
           (k/nil? (k/get-key out "op"))
           (k/nil? (k/get-key out "topic"))
           (k/nil? (k/get-key out "signal"))
           (k/nil? (k/get-key out "action")))
    (return {:status (k/get-key out "status")
             :body (k/get-key out "body")
             :meta (or (k/get-key out "meta")
                       {})
             :task (k/get-key out "task")})
    (return {:status "ok"
             :body out
             :meta {}
             :task nil})))

(defn.js send-call-output
  "sends the response for an inbound call frame"
  {:added "4.0"}
  [transport frame out]
  (var norm (-/normalize-call-output out))
  (-/send-frame transport
                (protocol/result (protocol/frame-id frame)
                                 (k/get-key norm "status")
                                 (k/get-key norm "body")
                                 (k/get-key norm "meta")
                                 nil))
  (var task (k/get-key norm "task"))
  (when task
    (-/send-frame transport
                  (protocol/task (protocol/frame-id frame)
                                 (or (k/get-key task "id")
                                     (k/get-key task "ref")
                                     (protocol/frame-id frame))
                                 (or (k/get-key task "status")
                                     (k/get-key norm "status")
                                     "accepted")
                                 (or (k/get-key task "body")
                                     {:action (protocol/frame-action frame)})
                                 (or (k/get-key task "meta")
                                     (k/get-key norm "meta")))))
  (return norm))

(defn.js send-call-error
  "sends an error response for an inbound call frame"
  {:added "4.0"}
  [transport frame err]
  (return (-/send-frame transport
                        (protocol/result (protocol/frame-id frame)
                                         "error"
                                         (-/error-body err)
                                         {}
                                         nil))))

(defn.js clear-pending
  "clears a pending call entry"
  {:added "4.0"}
  [transport call-id]
  (var prev (. transport ["pending"] [call-id]))
  (del (. transport ["pending"] [call-id]))
  (return prev))

(defn.js clear-task
  "clears a tracked task entry"
  {:added "4.0"}
  [transport task-ref]
  (var prev (. transport ["tasks"] [task-ref]))
  (del (. transport ["tasks"] [task-ref]))
  (return prev))

(defn.js ensure-task-entry
  "gets or creates a task tracking entry"
  {:added "4.0"}
  [transport frame]
  (var call-id (protocol/frame-id frame))
  (var task-ref (or (k/get-key frame "ref")
                    call-id))
  (var entry (. transport ["tasks"] [task-ref]))
  (when (k/nil? entry)
    (var pending (or (. transport ["pending"] [call-id])
                     (. transport ["pending"] [task-ref])))
    (when pending
      (:= entry {:ref task-ref
                 :callId (or (. pending ["callId"])
                             call-id)
                 :handler (. pending ["task"])
                 :resolve (. pending ["resolve"])
                 :reject (. pending ["reject"])
                 :frames []})
      (:= (. transport ["tasks"] [task-ref]) entry)
      (k/set-key pending "taskRef" task-ref)))
  (return entry))

(defn.js bind-system
  "binds generic context and handlers to a transport"
  {:added "4.0"}
  [transport system opts]
  (:= opts (or opts {}))
  (:= (. transport ["system"]) system)
  (when (k/not-nil? (. opts ["forwardAll"]))
    (:= (. transport ["forwardAll"]) (. opts ["forwardAll"])))
  (when (k/not-nil? (. opts ["context"]))
    (:= (. transport ["context"]) (. opts ["context"])))
  (when (k/not-nil? (. opts ["handleCall"]))
    (:= (. transport ["handleCall"]) (. opts ["handleCall"])))
  (when (k/not-nil? (. opts ["handleEmit"]))
    (:= (. transport ["handleEmit"]) (. opts ["handleEmit"])))
  (return transport))

(defn.js unbind-system
  "unbinds generic context and handlers from a transport"
  {:added "4.0"}
  [transport]
  (:= (. transport ["system"]) nil)
  (:= (. transport ["handleCall"]) nil)
  (:= (. transport ["handleEmit"]) nil)
  (:= (. transport ["context"]) nil)
  (return transport))

(defn.js handle-call
  "handles an inbound call frame"
  {:added "4.0"}
  [transport frame ctx]
  (var system (. transport ["system"]))
  (var handle-call (or (. transport ["handleCall"])
                       (and system (. system ["handleCall"]))
                       (and system (. system ["call"]))))
  (when (k/nil? handle-call)
    (k/err "ERR - Transport has no bound call handler"))
  (try
    (var out (handle-call frame transport (or ctx
                                              (. transport ["context"])
                                              {})))
    (if (-/promise? out)
      (return (. out
                 (then (fn [res]
                         (return (-/send-call-output transport frame res))))
                 (catch (fn [err]
                          (return (-/send-call-error transport frame err))))))
      (return (-/send-call-output transport frame out)))
    (catch err
      (return (-/send-call-error transport frame err)))))

(defn.js handle-result
  "handles an inbound result frame"
  {:added "4.0"}
  [transport frame]
  (var entry (. transport ["pending"] [(protocol/frame-id frame)]))
  (when entry
    (-/clear-pending transport (protocol/frame-id frame))
    (when (. entry ["taskRef"])
      (-/clear-task transport (. entry ["taskRef"])))
    (if (== "error" (k/get-key frame "status"))
      (return ((. entry ["reject"]) frame))
      (return ((. entry ["resolve"]) (k/get-key frame "body")
                                      frame))))
  (return entry))

(defn.js handle-task
  "handles an inbound task frame"
  {:added "4.0"}
  [transport frame]
  (var entry (-/ensure-task-entry transport frame))
  (when entry
    (x:arr-push (. entry ["frames"]) frame)
    (k/set-key entry "last" frame)
    (k/set-key entry "status" (k/get-key frame "status"))
    (when (. entry ["handler"])
      ((. entry ["handler"]) frame entry transport))
    (cond (== "ok" (k/get-key frame "status"))
          (do (-/clear-task transport (. entry ["ref"]))
              (-/clear-pending transport (. entry ["callId"]))
              (return ((. entry ["resolve"]) (k/get-key frame "body")
                                              frame)))

          (== "error" (k/get-key frame "status"))
          (do (-/clear-task transport (. entry ["ref"]))
              (-/clear-pending transport (. entry ["callId"]))
              (return ((. entry ["reject"]) frame)))

          :else
          (return entry)))
  (return entry))

(defn.js handle-emit
  "handles an inbound emit frame"
  {:added "4.0"}
  [transport frame ctx]
  (var system (. transport ["system"]))
  (var handle-emit (or (. transport ["handleEmit"])
                       (and system (. system ["handleEmit"]))
                       (and system (. system ["emit"]))))
  (if handle-emit
    (return (handle-emit frame transport (or ctx
                                             (. transport ["context"])
                                             {})))
    (return frame)))

(defn.js handle-subscribe
  "handles an inbound subscribe frame"
  {:added "4.0"}
  [transport frame]
  (var signal (protocol/frame-signal frame))
  (-/subscribe-signal transport signal)
  (return (-/send-frame transport
                        (protocol/result (protocol/frame-id frame)
                                         "ok"
                                         {:signal signal
                                          :subscribed true}
                                         {}
                                         nil))))

(defn.js handle-unsubscribe
  "handles an inbound unsubscribe frame"
  {:added "4.0"}
  [transport frame]
  (var signal (protocol/frame-signal frame))
  (-/unsubscribe-signal transport signal)
  (return (-/send-frame transport
                        (protocol/result (protocol/frame-id frame)
                                         "ok"
                                         {:signal signal
                                          :subscribed false}
                                         {}
                                         nil))))

(defn.js receive-frame
  "processes an inbound protocol frame"
  {:added "4.0"}
  [transport frame ctx]
  (var op (protocol/frame-op frame))
  (cond (== op "hello")
        (do (:= (. transport ["state"] ["peerHello"]) frame)
            (return frame))

        (== op "call")
        (return (-/handle-call transport frame ctx))

        (== op "result")
        (return (-/handle-result transport frame))

        (== op "task")
        (return (-/handle-task transport frame))

        (== op "emit")
        (return (-/handle-emit transport frame ctx))

        (== op "subscribe")
        (return (-/handle-subscribe transport frame))

        (== op "unsubscribe")
        (return (-/handle-unsubscribe transport frame))

        :else
        (k/err (k/cat "ERR - Transport op not found - " op))))

(defn.js call
  "sends a call frame and tracks the pending result"
  {:added "4.0"}
  [transport action body meta handlers]
  (var id (-/next-id transport "call"))
  (var frame (protocol/call id action body meta))
  (var p (new Promise
          (fn [resolve reject]
            (:= (. transport ["pending"] [id])
                {:resolve resolve
                 :reject reject
                 :callId id
                 :task (k/get-key handlers "task")
                 :action action
                 :body body
                 :meta meta}))))
  (-/send-frame transport frame)
  (return p))
