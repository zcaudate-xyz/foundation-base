(ns js.cell-v3.transport.worker
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.cell-v3.transport.core :as transport]
             [xt.lang.base-lib :as k]]})

(defn.js receive-message
  "handles a worker message event"
  {:added "4.0"}
  [transport event]
  (return (transport/receive-frame transport
                                   (. event ["data"])
                                   nil)))

(defn.js attach-worker
  "attaches a worker-like channel to a transport"
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
  "detaches the current worker listener"
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

(defn.js make-worker-transport
  "creates a transport bound to a Worker-like channel"
  {:added "4.0"}
  [worker opts]
  (:= opts (or opts {}))
  (var tx (transport/make-transport worker opts))
  (-/attach-worker tx worker)
  (when (or (. opts ["system"])
            (. opts ["handleCall"])
            (. opts ["handleEmit"]))
    (transport/bind-system tx
                           (. opts ["system"])
                           {:forwardAll (. opts ["forwardAll"])
                            :context (. opts ["context"])
                            :handleCall (. opts ["handleCall"])
                            :handleEmit (. opts ["handleEmit"])}))
  (return tx))
