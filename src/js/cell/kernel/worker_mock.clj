(ns js.cell.kernel.worker-mock
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.common-lib :as k]
             [js.cell.kernel.worker-local :as worker-local]
             [js.cell.kernel.worker-impl :as worker-impl]]})


(defspec.xt mock-worker-send
  [:fn [js.cell.kernel.spec/MockWorkerRecord [:or :xt/str js.cell.kernel.spec/RequestFrame]]
   :xt/any])

(defspec.xt mock-worker
  [:fn [[:fn [:xt/any] :xt/any]] js.cell.kernel.spec/MockWorkerRecord])

(defspec.xt create-worker
  [:fn [[:xt/maybe [:fn [:xt/any] :xt/any]]
        [:xt/maybe js.cell.kernel.spec/WorkerActionMap]
        [:xt/maybe :xt/bool]]
   js.cell.kernel.spec/MockWorkerRecord])

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
