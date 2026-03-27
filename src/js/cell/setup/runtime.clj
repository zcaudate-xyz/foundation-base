(ns js.cell.setup.runtime
  "Compatibility wrappers for legacy js.cell setup runtime helpers."
  (:require [js.cell.runtime.link :as link]))

(defn make-mock-link
  "creates a kernel worker-url object backed by the mock worker"
  ([]
   (make-mock-link nil))
  ([opts]
   (link/make-mock-link opts)))

(defn make-node-link
  "creates a kernel worker-url object backed by worker_threads"
  ([script]
   (make-node-link script nil))
  ([script opts]
   (link/make-node-link script opts)))

(defn make-webworker-link
  "creates a kernel worker-url object backed by a browser WebWorker"
  ([script]
   (link/make-webworker-link script)))

(defn make-sharedworker-link
  "creates a kernel worker-url object backed by a browser SharedWorker"
  ([script]
   (link/make-sharedworker-link script)))

(defn make-link
  "dispatches to a runtime-specific worker link helper"
  ([runtime script opts]
   (link/make-link runtime script opts)))
