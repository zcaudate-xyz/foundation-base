(ns xt.cell.kernel.inner-mock
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-trace :as trace]
             [xt.lang.common-async :as async]
             [xt.cell.kernel.inner-local :as inner-local]
             [xt.cell.kernel.inner-impl :as inner-impl]]})


(defspec.xt mock-worker-send
  [:fn [xt.cell.kernel.spec/MockWorkerRecord [:or :xt/str xt.cell.kernel.spec/RequestFrame]]
   :xt/any])

(defspec.xt mock-worker
  [:fn [[:fn [:xt/any] :xt/any]] xt.cell.kernel.spec/MockWorkerRecord])

(defspec.xt create-worker
  [:fn [[:xt/maybe [:fn [:xt/any] :xt/any]]
        [:xt/maybe xt.cell.kernel.spec/WorkerActionMap]
        [:xt/maybe :xt/bool]]
   xt.cell.kernel.spec/MockWorkerRecord])

(defn.xt mock-worker-send
  "sends a request to the mock worker"
  {:added "4.0"}
  [mock message]
  (try 
    (cond (xt/x:is-string? message)
          (inner-impl/worker-process mock
                                      {:op "eval"
                                       :id nil
                                       :body message})
          
          :else
          (inner-impl/worker-process mock message))
    (catch e (trace/TRACE! (. e ["stack"]) "SEND.ERROR"))))

(defn.xt mock-worker
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
                     (xt/for:array [listener listeners]
                       (listener event))))
   (var postRequest (fn [request]
                      (return
                       (async/promise-run
                        (fn []
                          (return (-/mock-worker-send worker request)))))))
  (xt/x:obj-assign worker #{postMessage
                            postRequest})
  (return worker))

(defn.xt create-worker
  "initialises the mock worker"
  {:added "4.0"}
  [listener actions suppress]
  (var worker (-/mock-worker listener))
  (inner-local/actions-init (or actions {}) worker)
  (when (not suppress)
    (async/promise-next
     (fn []
       (return (inner-impl/worker-init-signal worker {:done true})))))
  (return worker))
