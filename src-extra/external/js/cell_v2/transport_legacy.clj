(ns js.cell-v2.transport-legacy
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell.kernel.base-util :as legacy-util]
             [js.cell-v2.transport :as transport]
             [js.cell-v2.protocol :as protocol]]})

(def$.js EV_TASK "cell/::TASK")

(def$.js LEGACY_EVAL_ACTION "@/eval")

(defn.js kinds
  "gets the legacy kind registry"
  {:added "4.0"}
  [transport]
  (var out (. transport ["state"] ["legacyKinds"]))
  (when (k/nil? out)
    (:= (. transport ["state"] ["legacyKinds"]) {})
    (:= out (. transport ["state"] ["legacyKinds"])))
  (return out))

(defn.js remember-kind
  "remembers the legacy kind for an id"
  {:added "4.0"}
  [transport id kind]
  (var registry (-/kinds transport))
  (when id
    (:= (. registry [id]) kind))
  (return kind))

(defn.js get-kind
  "gets the remembered legacy kind"
  {:added "4.0"}
  [transport id]
  (var registry (-/kinds transport))
  (var out nil)
  (when id
    (:= out (. registry [id])))
  (return out))

(defn.js clear-kind
  "clears the remembered legacy kind"
  {:added "4.0"}
  [transport id]
  (var registry (-/kinds transport))
  (var prev (-/get-kind transport id))
  (when id
    (del (. registry [id])))
  (return prev))

(defn.js input-body
  "gets the effective call input body"
  {:added "4.0"}
  [frame]
  (var body (k/get-key frame "body"))
  (var out body)
  (var input (k/get-key body "input"))
  (when (k/not-nil? input)
    (:= out input))
  (return out))

(defn.js decode-eval-body
  "decodes a legacy eval response body"
  {:added "4.0"}
  [body]
  (cond (k/is-string? body)
        (do (var out (k/json-decode body))
            (var value out)
            (when (and (k/obj? out)
                       (== "data" (k/get-key out "type")))
              (:= value (k/get-key out "value")))
            (return value))

        :else
        (return body)))

(defn.js encode-eval-body
  "encodes a legacy eval response body"
  {:added "4.0"}
  [body]
  (return (k/json-encode {:type "data"
                          :value body})))

(defn.js legacy->frame
  "translates a legacy js.cell worker message into a protocol frame"
  {:added "4.0"}
  [transport message]
  (var op (k/get-key message "op"))
  (var id (k/get-key message "id"))
  (var status (k/get-key message "status"))
  (var route (k/get-key message "route"))
  (var topic (k/get-key message "topic"))
  (var body (k/get-key message "body"))
  (cond (== op "route")
        (cond (k/not-nil? status)
              (do (var out body)
                  (-/clear-kind transport id)
                  (when (== status "ok")
                    (:= out (legacy-util/arg-decode body)))
                  (return (protocol/result id
                                           status
                                           out
                                           {}
                                           nil)))

              :else
              (do (var input body)
                  (when (k/nil? input)
                    (:= input []))
                  (:= input (legacy-util/arg-decode input))
                  (-/remember-kind transport id "route")
                  (return (protocol/call id
                                         route
                                         {:input input}
                                         {:legacyOp "route"}))))

        (== op "stream")
        (do (var emit-id id)
            (var emit-status status)
            (when (k/nil? emit-id)
              (:= emit-id (transport/next-id transport "legacy")))
            (when (k/nil? emit-status)
              (:= emit-status "ok"))
            (return (protocol/emit emit-id
                                   topic
                                   emit-status
                                   body
                                   {}
                                   nil)))

        (== op "eval")
        (cond (k/not-nil? status)
              (do (var out body)
                  (-/clear-kind transport id)
                  (when (== status "ok")
                    (:= out (-/decode-eval-body body)))
                  (return (protocol/result id
                                           status
                                           out
                                           {}
                                           nil)))

              :else
              (do (var input [body])
                  (-/remember-kind transport id "eval")
                  (return (protocol/call id
                                         -/LEGACY_EVAL_ACTION
                                         {:input input}
                                         {:legacyOp "eval"
                                          :annex "debug"}))))

        :else
        (k/err (k/cat "ERR - Legacy op not found - " op))))

(defn.js frame->legacy
  "translates a protocol frame into a legacy js.cell worker message"
  {:added "4.0"}
  [transport frame]
  (var op (protocol/frame-op frame))
  (var id (protocol/frame-id frame))
  (var body (k/get-key frame "body"))
  (cond (== op "call")
        (do (var action (protocol/frame-action frame))
            (if (== action -/LEGACY_EVAL_ACTION)
              (do (var input (-/input-body frame))
                  (var eval-body (k/first input))
                  (when (k/nil? eval-body)
                    (:= eval-body input))
                  (-/remember-kind transport id "eval")
                  (return {:op "eval"
                           :id id
                           :body eval-body}))
              (do (var input (-/input-body frame))
                  (when (k/nil? input)
                    (:= input []))
                  (:= input (legacy-util/arg-encode input))
                  (-/remember-kind transport id "route")
                  (return {:op "route"
                           :id id
                           :route action
                           :body input}))))

        (== op "result")
        (do (var kind (-/get-kind transport id))
            (var status (k/get-key frame "status"))
            (when (k/nil? kind)
              (:= kind "route"))
            (-/clear-kind transport id)
            (if (== kind "eval")
              (do (var out body)
                  (when (== "ok" status)
                    (:= out (-/encode-eval-body body)))
                  (return {:op "eval"
                           :id id
                           :status status
                           :body out}))
              (do (var out body)
                  (when (== "ok" status)
                    (:= out (legacy-util/arg-encode body)))
                  (return {:op "route"
                           :id id
                           :status status
                           :body out}))))

        (== op "emit")
        (return {:op "stream"
                 :id id
                 :topic (protocol/frame-signal frame)
                 :status (k/get-key frame "status")
                 :body body})

        (== op "task")
        (return {:op "stream"
                 :id id
                 :topic -/EV_TASK
                 :status (k/get-key frame "status")
                 :body {:ref (k/get-key frame "ref")
                        :body body
                        :meta (k/get-key frame "meta")}})

        :else
        (k/err (k/cat "ERR - Protocol op not supported for legacy transport - " op))))

(defn.js receive-legacy
  "handles an inbound legacy worker message"
  {:added "4.0"}
  [transport message]
  (return (transport/receive-frame transport
                                   (-/legacy->frame transport message)
                                   {:transport transport
                                    :legacy true})))

(defn.js receive-message
  "handles a worker message event"
  {:added "4.0"}
  [transport event]
  (return (-/receive-legacy transport
                            (. event ["data"]))))

(defn.js attach-worker
  "attaches a legacy worker-like channel to a transport"
  {:added "4.0"}
  [transport worker]
  (var listener (fn [event]
                  (return (-/receive-message transport event))))
  (:= (. transport ["channel"]) worker)
  (:= (. transport ["worker"]) worker)
  (:= (. transport ["workerListener"]) listener)
  (. worker (addEventListener "message" listener false))
  (return transport))

(defn.js detach-worker
  "detaches the current legacy worker listener"
  {:added "4.0"}
  [transport]
  (var worker (. transport ["worker"]))
  (var listener (. transport ["workerListener"]))
  (when (and worker
             listener
             (. worker ["removeEventListener"]))
    (. worker (removeEventListener "message" listener false)))
  (:= (. transport ["worker"]) nil)
  (:= (. transport ["workerListener"]) nil)
  (return transport))

(defn.js make-legacy-worker-transport
  "creates a compatibility transport for the legacy js.cell worker message format"
  {:added "4.0"}
  [worker opts]
  (:= opts (or opts {}))
  (var tx (transport/make-transport worker opts))
  (:= (. tx ["send"])
      (fn [frame]
        (var message (-/frame->legacy tx frame))
        (if (. worker ["postMessage"])
          (return (j/postMessage worker message))
          (k/err "ERR - Legacy worker has no postMessage"))))
  (-/attach-worker tx worker)
  (when (. opts ["system"])
    (transport/bind-system tx
                           (. opts ["system"])
                           {:forwardAll (. opts ["forwardAll"])
                            :listenerId (. opts ["listenerId"])}))
  (return tx))
