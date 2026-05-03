(ns xt.cell.kernel.inner-mock
  (:require [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.lang.common-trace :as trace]
             [xt.lang.spec-promise :as spec-promise]
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
    (catch e
      (var stack (xt/x:get-key e "stack"))
      (when stack
        (trace/TRACE! stack "SEND.ERROR")))))

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
                       (spec-promise/x:promise
                        (fn []
                           (return (-/mock-worker-send worker request)))))))
  (xt/x:obj-assign worker {"postMessage" postMessage
                           "postRequest" postRequest})
  (return worker))

(defn.xt create-worker
  "initialises the mock worker"
  {:added "4.0"}
  [listener actions suppress]
  (var worker (-/mock-worker listener))
  (inner-local/actions-init (or actions {}) worker)
  (when (not suppress)
    (spec-promise/x:with-delay
     0
     (fn []
        (return (inner-impl/worker-init-signal worker {:done true})))))
  (return worker))
