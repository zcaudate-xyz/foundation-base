(ns js.cell.kernel.worker-mock
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-lib :as k]
             [js.cell.kernel.base-util :as util]
             [js.cell.kernel.worker-state :as worker-state]
             [js.cell.kernel.worker-local :as worker-local]]})

;;
;; 
;;

(defn.js mock-send
  "sends a request to the mock worker"
  {:added "4.0"}
  [mock message]
  (try 
    (cond (k/is-string? message)
          (-/worker-process mock
                            {:op "eval"
                             :id nil
                             :body message})
          
          :else
          (-/worker-process mock message))
    (catch e (k/TRACE! (. e ["stack"]) "SEND.ERROR"))))

(defn.js new-mock
  "creates a new mock worker
 
   (!.js
    (internal/new-mock k/identity))
   => {\"::\" \"worker.mock\", \"listeners\" [nil]}"
  {:added "4.0"}
  [listener]
  (var mock {"::" "worker.mock"
             :listeners [listener]})
  (var postMessage (fn [event]
                     (var #{listeners} mock)
                     (k/for:array [listener listeners]
                       (listener event))))
  (var postRequest (fn:> [request]
                     (j/future (-/mock-send mock request))))
  (j/assign mock #{postMessage
                   postRequest})
  (return mock))

(defn.js mock-init
  "initialises the mock worker"
  {:added "4.0"}
  [listener actions suppress]
  (var mock (-/new-mock listener))
  (when actions
    (worker-local/actions-init actions))
  (when (not suppress)
    (-/worker-init-post mock {:done true}))
  (return mock))
