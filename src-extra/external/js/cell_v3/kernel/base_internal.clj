(ns js.cell-v3.kernel.base-internal
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-lib :as k]
             [js.cell-v3.kernel.base-util :as util]
             [js.cell-v3.kernel.base-fn :as base-fn]]})

(defn.js worker-handle-async
  "worker function for handling async tasks"
  {:added "4.0"}
  [worker f op id body]
  (return (. (f (:.. body))
             (then (fn [ret]
                     (j/postMessage worker {:op op
                                            :id id
                                            :status "ok"
                                            :body (util/arg-encode ret)})))
             (catch (fn [ret]
                      (when (. ret ["stack"])
                        (k/TRACE! (. ret ["stack"]) "ERR"))
                      (j/postMessage worker {:op op
                                             :id id
                                             :status "error"
                                             :body ret}))))))

(defn.js worker-process
  "processes various types of actions"
  {:added "4.0"}
  [worker input]
  (var op (. input ["op"]))
  (var id (. input ["id"]))
  (var body (. input ["body"]))
  (var action (. input ["action"]))
  (var post-fn (fn:> [x] (j/postMessage worker x)))
  (cond (== op "eval")
        (do (when (== false (. (base-fn/get-state worker) ["eval"]))
              (j/postMessage worker {:op op
                                     :id id
                                     :status "error"
                                     :body (k/cat "Not enabled - EVAL")}))
            (var out (repl/return-eval body))
            (var f (:? (. input ["async"])
                       j/identity
                       post-fn))
        (return (f {:op op
                    :id id
                    :status "ok"
                    :body out})))

        (== op "action")
        (do (var action-entry (. (base-fn/get-actions worker) [action]))
            (when (== nil action-entry)
              (return (j/postMessage worker {:op op
                                             :id id
                                             :status "error"
                                             :body (k/cat "Action not found - " action)})))
            (var action-async (. action-entry ["async"]))
            (var action-fn (. action-entry ["handler"]))
            (var f (:? action-async
                       j/identity
                       post-fn))
            (try
              (:= body (util/arg-decode (or body [])))
              (var out (:? action-async
                           (-/worker-handle-async worker action-fn op id body)
                           (action-fn (:.. body))))
              (return (f {:op op
                          :id id
                          :status "ok"
                          :body (util/arg-encode out)}))
              (catch err
                (return (f {:op op
                            :id id
                            :status "error"
                            :body err})))))

        :else
        (post-fn {:op op
                  :status "error"
                  :body input})))

(defn.js worker-init
  "initiates the worker routes"
  {:added "4.0"}
  [worker input-fn]
  (:= input-fn (or input-fn k/identity))
  (. worker (addEventListener
             "message"
             (fn [e]
               (cond (k/is-string? e.data)
                     (-/worker-process worker
                                       (input-fn
                                        {:op "eval"
                                         :id nil
                                         :body e.data}))

                     :else
                     (-/worker-process worker (input-fn e.data))))
             false))
  (return true))

(defn.js worker-init-post
  "posts an init message"
  {:added "4.0"}
  [worker body]
  (return (j/postMessage worker {:op "stream"
                                 :status "ok"
                                 :topic util/EV_INIT
                                 :body body})))

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
  "creates a new mock worker"
  {:added "4.0"}
  [listener]
  (var mock {"::" "worker.mock"
             :listeners [listener]})
  (var postMessage (fn [event]
                     (var listeners (. mock ["listeners"]))
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
    (base-fn/actions-init actions))
  (when (not suppress)
    (-/worker-init-post mock {:done true}))
  (return mock))
