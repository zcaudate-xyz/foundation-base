(ns js.cell.kernel.worker-mock
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell.kernel.worker-local :as worker-local]
             [js.cell.kernel.worker-impl :as worker-impl]]})

(defn.js mock-worker-send
  "sends a request to the mock worker"
  {:added "4.0"}
  [mock message]
  (try 
    (cond (k/is-string? message)
          (worker-impl/worker-process mock
                                      {:op "eval"
                                       :id nil
                                       :body message})
          
          :else
          (worker-impl/worker-process mock message))
    (catch e (k/TRACE! (. e ["stack"]) "SEND.ERROR"))))

(defn.js mock-worker
  "creates a new mock worker
 
   (!.js
    (internal/new-mock k/identity))
   => {\"::\" \"worker.mock\", \"listeners\" [nil]}"
  {:added "4.0"}
  [listener]
  (var worker {"::" "worker.mock"
               :listeners [listener]})
  (var postMessage (fn [event]
                     (var #{listeners} worker)
                     (k/for:array [listener listeners]
                       (listener event))))
  (var postRequest (fn [request]
                     (return
                      (j/future (-/mock-worker-send worker request)))))
  (j/assign worker #{postMessage
                     postRequest})
  (return worker))

(defn.js create-worker
  "initialises the mock worker"
  {:added "4.0"}
  [listener actions suppress]
  (var worker (-/mock-worker listener))
  (when actions
    (worker-local/actions-init actions worker))
  (when (not suppress)
    (worker-impl/worker-init-signal worker {:done true}))
  (return worker))
