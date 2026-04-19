(ns xt.cell.kernel.worker-mock
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-trace :as trace]
             [xt.lang.common-task :as task]
             [xt.cell.kernel.worker-local :as worker-local]
             [xt.cell.kernel.worker-impl :as worker-impl]]})


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
          (worker-impl/worker-process mock
                                      {:op "eval"
                                       :id nil
                                       :body message})
          
          :else
          (worker-impl/worker-process mock message))
    (catch e (trace/TRACE! (. e ["stack"]) "SEND.ERROR"))))

(defn.xt mock-worker
  "creates a new mock worker
 
   (!.js
    (internal/new-mock (fn [x] (return x))))
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
                       (task/task-run
                        (fn []
                          (return (-/mock-worker-send worker request)))))))
  (xt/x:set-key worker "postMessage" postMessage)
  (xt/x:set-key worker "postRequest" postRequest)
  (return worker))

(defn.xt create-worker
  "initialises the mock worker"
  {:added "4.0"}
  [listener actions suppress]
  (var worker (-/mock-worker listener))
  (when actions
    (worker-local/actions-init actions worker))
  (when (not suppress)
    (worker-impl/worker-init-signal worker {:done true}))
  (return worker))
