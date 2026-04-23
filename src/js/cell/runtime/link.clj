(ns js.cell.runtime.link
  "Worker link helpers for js.cell.kernel runtime scripts."
  (:require [js.cell.kernel.worker-impl]
            [js.cell.kernel.worker-local]
            [js.cell.kernel.worker-mock]
            [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j] [xt.lang.spec-base :as xt] [js.cell.kernel.worker-local :as worker-local] [js.cell.kernel.worker-mock :as worker-mock]]})

(defn.js make-mock-link
  "creates a kernel worker-url object backed by the mock worker"
  {:added "4.0"}
  [opts]
  (var config (or opts {}))
  (var #{actions suppress} config)
  (var resolved-actions actions)
  (return {:create-fn (fn [listener]
                        (return (worker-mock/create-worker listener
                                                           resolved-actions
                                                           suppress)))}))

(defn.js make-node-link
  "creates a kernel worker-url object backed by worker_threads"
  {:added "4.0"}
  [script opts]
  (var config (or opts {}))
  (var eval-flag (xt/x:get-key config "eval"))
  (var eval-mode (:? (xt/x:nil? eval-flag) true eval-flag))
  (var #{Worker} (require "worker_threads"))
  (return {:create-fn (fn [listener]
                        (var worker (new Worker script
                                                (:? eval-mode
                                                    {:eval true}
                                                    {})))
                        (. worker (on "message"
                                      (fn [data]
                                        (listener data))))
                        (return worker))}))

(defn.js resolve-script
  "resolves a script function or value"
  {:added "4.0"}
  [script]
  (return (:? (xt/x:is-function? script)
              (script)
              script)))

(defn.js make-blob-url
  "creates a blob URL from a worker script"
  {:added "4.0"}
  [script]
  (var blob (new Blob [script]
                      {:type "text/javascript"}))
  (return (. (!:G URL) (createObjectURL blob))))

(defn.js make-webworker-link
  "creates a kernel worker-url object backed by a browser WebWorker"
  {:added "4.0"}
  [script]
  (return {:create-fn (fn [listener]
                        (var url (-/make-blob-url (-/resolve-script script)))
                        (try
                          (var worker (new Worker url))
                          (. worker (addEventListener
                                     "message"
                                     (fn [e]
                                       (listener e.data))
                                     false))
                          (. (!:G URL) (revokeObjectURL url))
                          (return worker)
                          (catch err
                            (. (!:G URL) (revokeObjectURL url))
                            (throw err))))}))

(defn.js make-sharedworker-link
  "creates a kernel worker-url object backed by a browser SharedWorker"
  {:added "4.0"}
  [script]
  (return {:create-fn (fn [listener]
                        (var url (-/make-blob-url (-/resolve-script script)))
                        (try
                          (var shared (new SharedWorker url))
                          (var port (. shared ["port"]))
                          (. port (start))
                          (. port (addEventListener
                                   "message"
                                   (fn [e]
                                     (listener e.data))
                                   false))
                          (. (!:G URL) (revokeObjectURL url))
                          (return port)
                          (catch err
                            (. (!:G URL) (revokeObjectURL url))
                            (throw err))))}))

(defn.js make-link
  "dispatches to a runtime-specific worker link helper"
  {:added "4.0"}
  [runtime script opts]
  (var runtime-name (xt/x:to-string runtime))
  (cond (== runtime-name "mock")
        (return (-/make-mock-link opts))

        (== runtime-name "node")
        (return (-/make-node-link (-/resolve-script script) opts))

        (== runtime-name "webworker")
        (return (-/make-webworker-link script))

        (== runtime-name "sharedworker")
        (return (-/make-sharedworker-link script))

        :else
        (throw (xt/x:cat "Unknown js.cell.runtime.link runtime: " runtime))))
