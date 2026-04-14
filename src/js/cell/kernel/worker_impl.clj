(ns js.cell.kernel.worker-impl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.common-spec :as xt]
             [xt.lang.common-trace :as trace]
             [xt.lang.common-repl :as repl]
             [js.cell.kernel.base-util :as util]
             [js.cell.kernel.worker-state :as worker-state]]})

(defspec.xt worker-handle-async
  [:fn [:xt/any [:fn [js.cell.kernel.spec/AnyList] :xt/any]
        :xt/str
        [:xt/maybe :xt/str]
        js.cell.kernel.spec/AnyList]
   :xt/any])

(defspec.xt worker-process-eval
  [:fn [:xt/any
        js.cell.kernel.spec/RequestFrame
        [:fn [js.cell.kernel.spec/ResponseFrame]
         :xt/any]]
   :xt/any])

(defspec.xt worker-process-action
  [:fn [:xt/any js.cell.kernel.spec/RequestFrame
        [:fn [js.cell.kernel.spec/ResponseFrame]
         :xt/any]]
   :xt/any])

(defspec.xt worker-process
  [:fn [:xt/any js.cell.kernel.spec/RequestFrame]
   :xt/any])

(defspec.xt worker-init
  [:fn [:xt/any [:xt/maybe [:fn [js.cell.kernel.spec/RequestFrame]
                            js.cell.kernel.spec/RequestFrame]]]
   :xt/bool])

(defspec.xt worker-init-signal
  [:fn [:xt/any :xt/any]
   :xt/any])

(defn.js worker-handle-async
  "worker function for handling async tasks"
  {:added "4.0"}
  [worker f op id body]
  (return (. (f (:.. body))
             (then  (fn [ret]
                      (j/postMessage worker (util/resp-ok op id (util/arg-encode ret)))))
             (catch (fn [ret]
                      (when (. ret ["stack"])
                        (trace/TRACE! (. ret ["stack"]) "ERR"))
                       
                      (j/postMessage worker (util/resp-error op id ret)))))))

(defn.js worker-process-eval
  [worker input post-fn]
  (var #{op id body action} input)
  (when (== false (. (worker-state/get-state worker)
                     ["eval"]))
    (j/postMessage worker (util/resp-error op id "Not enabled - EVAL")))
  (var out (repl/return-eval body))
  (var f (:? (. input ["async"])
             j/identity
             post-fn))
  (return (f (util/resp-ok op id out))))

(defn.js worker-process-action
  [worker input post-fn]
  (var #{op id body action} input)
  (var action-entry  (. (worker-state/get-actions worker)
                        [action]))
  (when (== nil action-entry)
    (return (j/postMessage worker (util/resp-error op id (xt/x:cat "action not found - " action)))))
            
  (var action-async  (. action-entry ["is_async"]))
  (var action-fn     (. action-entry ["handler"]))
  (var f   (:? action-async
               j/identity
               post-fn))
  
  (try
    (:= body (util/arg-decode (or body [])))
    (var out (:? action-async
                 (-/worker-handle-async worker action-fn op id body)
                 (action-fn (:.. body))))
    (return (f (util/resp-ok op id (util/arg-encode out))))
    (catch err
        (return (f (util/resp-error op id err))))))

(defn.js worker-process
  "processes various types of actions"
  {:added "4.0"}
  [worker input]
  (var #{op} input)
  (var post-fn (fn [x] (return (j/postMessage worker x))))
  (cond (== op "eval")
        (return
         (-/worker-process-eval worker input post-fn))
        
        (== op "call")
        (return
         (-/worker-process-action worker input post-fn))
        
        :else
        (post-fn (util/resp-error op nil input))))

(defn.js worker-init
  "initiates the worker actions"
  {:added "4.0"}
  [worker input-fn]
  (:= input-fn (or input-fn j/identity))
  (. worker (addEventListener
             "message"
             (fn [e]
               (cond (xt/x:is-string? e.data)
                     (-/worker-process worker
                                       (input-fn
                                        {:op "eval"
                                         :id nil
                                         :body e.data}))
                     
                     :else
                     (-/worker-process worker (input-fn e.data))))
             false))
  (return true))

(defn.js worker-init-signal
  "posts an init message"
  {:added "4.0"}
  [worker body]
  (return (j/postMessage worker (util/resp-stream util/EV_INIT body))))
