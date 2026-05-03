(ns xt.cell.kernel.inner-impl
  (:require [hara.lang :as l]
            [hara.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-trace :as trace]
             [xt.lang.common-lib :as lib]
             [xt.lang.spec-promise :as spec-promise]
             [xt.cell.kernel.base-util :as util]
             [xt.cell.kernel.inner-state :as inner-state]]})

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

(defn.xt worker-handle-async
  "worker function for handling async tasks"
  {:added "4.0"}
  [worker f op id args]
  (var promise (spec-promise/x:promise-run (xt/x:apply f args)))
  (return
   (spec-promise/x:promise-catch
    (spec-promise/x:promise-then
     promise
     (fn [ret]
       (var postMessage (xt/x:get-key worker "postMessage"))
       (return (xt/x:apply postMessage [(util/resp-ok op id (util/arg-encode ret))]))))
    (fn [ret]
      (var stack (xt/x:get-key ret "stack"))
      (when stack
        (trace/TRACE! stack "ERR"))
      (var postMessage (xt/x:get-key worker "postMessage"))
      (return (xt/x:apply postMessage [(util/resp-error op id ret)]))))))

(defn.xt worker-process-eval
  [worker input post-fn]
  (var #{op id body action} input)
  (when (== false (. (inner-state/get-state worker)
                     ["eval"]))
    (var postMessage (xt/x:get-key worker "postMessage"))
    (xt/x:apply postMessage [(util/resp-error op id "Not enabled - EVAL")]))
  (var out (lib/return-eval body))
  (var f (:? (xt/x:get-key input "async")
             lib/identity
               post-fn))
  (return (f (util/resp-ok op id out))))

(defn.xt worker-process-action
  [worker input post-fn]
  (var #{op id body action} input)
  (var action-entry  (. (inner-state/get-actions worker)
                        [action]))
  (when (== nil action-entry)
    (var postMessage (xt/x:get-key worker "postMessage"))
    (return (xt/x:apply postMessage [(util/resp-error op id (xt/x:cat "action not found - " action))])))
            
  (var action-static (xt/x:get-key action-entry "static"))
  (var action-async  (. action-entry ["is_async"]))
  (var action-fn     (. action-entry ["handler"]))
  (var f   (:? action-async
               lib/identity
                post-fn))
  
  (try
    (:= body (util/arg-decode (or body [])))
    (var invoke-args (xt/x:arr-clone body))
    (when (== false action-static)
      (xt/x:arr-push-first invoke-args worker))
    (var out (:? action-async
                 (-/worker-handle-async worker action-fn op id invoke-args)
                 (xt/x:apply action-fn invoke-args)))
    (return (f (util/resp-ok op id (util/arg-encode out))))
    (catch err
        (return (f (util/resp-error op id err))))))

(defn.xt worker-process
  "processes various types of actions"
  {:added "4.0"}
  [worker input]
  (var #{op} input)
  (var post-fn (fn [x]
                 (var postMessage (xt/x:get-key worker "postMessage"))
                 (return (xt/x:apply postMessage [x]))))
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
  (:= input-fn (or input-fn lib/identity))
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
  (var postMessage (xt/x:get-key worker "postMessage"))
  (return (xt/x:apply postMessage [(util/resp-stream util/EV_INIT body)])))
