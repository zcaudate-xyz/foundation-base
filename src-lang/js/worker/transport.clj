(ns js.worker.transport
  "Worker transport adaptors for xt.event.node."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]]})

(defn.xt event-data
  "normalizes a worker message event into its payload"
  {:added "4.1"}
  [event]
  (return (:? (and (xt/x:is-object? event)
                   (xt/x:has-key? event "data"))
              (xt/x:get-key event "data")
              event)))

(defn.xt worker-endpoint
  "adapts a host-side Worker or create-fn source to the node transport contract"
  {:added "4.1"}
  [worker-source]
  (var current-worker nil)
  (var current-callback nil)
  (return
   {:meta {"kind" "webworker"}
    :send-fn
    (fn [frame]
      (var worker (or current-worker
                      (:? (xt/x:has-key? worker-source "create_fn")
                          nil
                          worker-source)))
      (when (xt/x:nil? worker)
        (xt/x:err "worker endpoint not started"))
      (var post-request (xt/x:get-key worker "postRequest"))
      (if (xt/x:is-function? post-request)
        (return (post-request frame))
        (return (. worker (postMessage frame)))))
    :start-fn
    (fn [listener]
      (if (xt/x:has-key? worker-source "create_fn")
        (do (:= current-worker
                ((xt/x:get-key worker-source "create_fn")
                 listener))
            (return current-worker))
        (do (:= current-worker worker-source)
            (:= current-callback
                (fn [event]
                  (return (listener (-/event-data event) nil))))
            (. current-worker (addEventListener
                               "message"
                               current-callback
                               false))
            (return current-worker))))
    :stop-fn
    (fn [_]
      (when (and (xt/x:not-nil? current-worker)
                 (xt/x:not-nil? current-callback)
                 (xt/x:is-function? (xt/x:get-key current-worker "removeEventListener")))
        (. current-worker (removeEventListener
                           "message"
                           current-callback
                           false)))
      (when (and (xt/x:not-nil? current-worker)
                 (xt/x:is-function? (xt/x:get-key current-worker "terminate")))
        (. current-worker (terminate)))
      (:= current-worker nil)
      (:= current-callback nil)
      (return true))}))

(defn.xt self-endpoint
  "adapts worker self to the node transport contract"
  {:added "4.1"}
  [worker-self]
  (var current-callback nil)
  (return
   {:meta {"kind" "webworker.self"}
    :send-fn
    (fn [frame]
      (return (. worker-self (postMessage frame))))
    :start-fn
    (fn [listener]
      (:= current-callback
          (fn [event]
            (return (listener (-/event-data event) nil))))
      (. worker-self (addEventListener
                      "message"
                      current-callback
                      false))
      (return worker-self))
    :stop-fn
    (fn [_]
      (when (and (xt/x:not-nil? current-callback)
                 (xt/x:is-function? (xt/x:get-key worker-self "removeEventListener")))
        (. worker-self (removeEventListener
                        "message"
                        current-callback
                        false)))
      (:= current-callback nil)
      (return true))}))
