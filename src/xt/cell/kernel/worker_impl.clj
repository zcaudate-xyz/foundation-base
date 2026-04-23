(ns xt.cell.kernel.worker-impl
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-lib :as lib]
              [xt.lang.spec-base :as xt]
              [xt.lang.common-trace :as trace]
              [xt.lang.common-task :as task]
              [xt.cell.kernel.base-util :as util]
              [xt.cell.kernel.worker-state :as worker-state]]})

(defspec.xt worker-handle-async
  [:fn [:xt/any [:fn [xt.cell.kernel.spec/AnyList] :xt/any]
        :xt/str
        [:xt/maybe :xt/str]
        xt.cell.kernel.spec/AnyList]
   :xt/any])

(defspec.xt worker-process-eval
  [:fn [:xt/any
        xt.cell.kernel.spec/RequestFrame
        [:fn [xt.cell.kernel.spec/ResponseFrame]
         :xt/any]]
   :xt/any])

(defspec.xt worker-process-action
  [:fn [:xt/any xt.cell.kernel.spec/RequestFrame
        [:fn [xt.cell.kernel.spec/ResponseFrame]
         :xt/any]]
   :xt/any])

(defspec.xt worker-process
  [:fn [:xt/any xt.cell.kernel.spec/RequestFrame]
   :xt/any])

(defspec.xt worker-init
  [:fn [:xt/any [:xt/maybe [:fn [xt.cell.kernel.spec/RequestFrame]
                            xt.cell.kernel.spec/RequestFrame]]]
   :xt/bool])

(defspec.xt worker-init-signal
  [:fn [:xt/any :xt/any]
    :xt/any])

(defspec.xt post-message
  [:fn [:xt/any xt.cell.kernel.spec/ResponseFrame] :xt/any])

(defn.xt post-message
  "posts a response via a worker-like transport"
  {:added "4.0"}
  [worker body]
  (var post-fn (or (xt/x:get-key worker "post_message")
                   (xt/x:get-key worker "postMessage")))
  (when (not (xt/x:is-function? post-fn))
    (xt/x:err "ERR - worker transport cannot post messages"))
  (return (post-fn body)))

(defn.xt worker-handle-async
  "worker function for handling async tasks"
  {:added "4.0"}
  [worker f op id body]
  (var current (f (:.. body)))
  (return
   (task/task-catch
    (task/task-then current
                    (fn [ret]
                      (return (-/post-message worker
                                              (util/resp-ok op id (util/arg-encode ret))))))
    (fn [ret]
      (when (. ret ["stack"])
        (trace/TRACE! (. ret ["stack"]) "ERR"))
      (return (-/post-message worker (util/resp-error op id ret)))))))

(defn.xt worker-process-eval
  [worker input post-fn]
  (var #{op id body action} input)
  (when (== false (. (worker-state/get-state worker)
                     ["eval"]))
    (-/post-message worker (util/resp-error op id "Not enabled - EVAL")))
  (var out (lib/return-eval body))
  (var f (:? (. input ["async"])
             (fn [x] (return x))
             post-fn))
  (return (f (util/resp-ok op id out))))

(defn.xt worker-process-action
  [worker input post-fn]
  (var #{op id body action} input)
  (var action-entry  (. (worker-state/get-actions worker)
                        [action]))
  (when (== nil action-entry)
    (return (-/post-message worker (util/resp-error op id (xt/x:cat "action not found - " action)))))
            
  (var action-async  (. action-entry ["is_async"]))
  (var action-fn     (. action-entry ["handler"]))
  (var f   (:? action-async
               (fn [x] (return x))
               post-fn))
  
  (try
    (:= body (util/arg-decode (or body [])))
    (var out (:? action-async
                 (-/worker-handle-async worker action-fn op id body)
                 (action-fn (:.. body))))
    (return (f (util/resp-ok op id (util/arg-encode out))))
    (catch err
        (return (f (util/resp-error op id err))))))

(defn.xt worker-process
  "processes various types of actions"
  {:added "4.0"}
  [worker input]
  (var #{op} input)
  (var post-fn (fn [x] (return (-/post-message worker x))))
  (cond (== op "eval")
        (return
         (-/worker-process-eval worker input post-fn))
        
        (== op "call")
        (return
         (-/worker-process-action worker input post-fn))
        
        :else
        (post-fn (util/resp-error op nil input))))

(defn.xt worker-init
  "initiates the worker actions"
  {:added "4.0"}
  [worker input-fn]
  (:= input-fn (or input-fn (fn [x] (return x))))
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

(defn.xt worker-init-signal
  "posts an init message"
  {:added "4.0"}
  [worker body]
  (return (-/post-message worker (util/resp-stream util/EV_INIT body))))
