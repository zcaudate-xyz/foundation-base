(ns js.react.ext-model
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as k]
             [xt.event.base-model :as event-model]
             [js.react :as r]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-tree :as xtt]
             [xt.lang.common-trace :as trace]
             [xt.lang.spec-promise :as promise]]})

(defn.js throttled-setter
  "creates a throttled setter which only updates after a delay"
  {:added "4.0"}
  [setResult delay]
  (var throttle {:val    nil
                 :thread nil
                 :mounted true})
  (var throttled-fn
       (fn [result]
         (var t (xt/x:now-ms))
         (cond (k/not-nil? (xt/x:get-key throttle "thread"))
                (xt/x:set-key throttle "val" result)

               :else
                (do (xt/x:set-key throttle "val" result)
                    (setResult result)
                    (xt/x:set-key throttle "thread"
                               (promise/x:with-delay delay
                                 (fn []
                                  (when (and (not= (xt/x:get-key throttle "val")
                                                   result)
                                             (xt/x:get-key throttle "mounted"))
                                    (setResult (xt/x:get-key throttle "val")))
                                  (xt/x:del-key throttle "thread"))))))))
  (return [throttled-fn throttle]))

(defn.js refresh-model
  "refreshes the model"
  {:added "4.0"}
  [model opts]
  (var [context disabled] (event-model/pipeline-prep model opts))
  (var #{acc} context)
  (return (. (event-model/pipeline-run
              context
              disabled
              (fn [handler-fn context #{success error}]
                (return (. (promise/x:promise
                            (fn []
                              (return (handler-fn context))))
                           (then success)
                           (catch error))))
              nil
              k/identity)
             (then (fn []
                     (return acc))))))

(defn.js refresh-args
  "refreshes the model model args"
  {:added "4.0"}
  [model args opts]
  
  (event-model/set-input model {:data args})
  (return (-/refresh-model model opts)))

(defn.js refresh-model-remote
  "refreshes model using remote function"
  {:added "4.0"}
  [model save-output opts]
  (when (xtd/get-in model ["pipeline" "remote" "handler"])
    (var [context disabled] (event-model/pipeline-prep model opts))
    (var #{acc} context)
    
    (return (. (event-model/pipeline-run-remote
                context
                save-output
                (fn [handler-fn context #{success error}]
                  (return (. (promise/x:promise
                              (fn []
                                (return (handler-fn context))))
                             (then success)
                             (catch error))))
                nil
                k/identity)
               (then (fn:> acc))))))

(defn.js refresh-args-remote
  "refreshes model using remote function with new args"
  {:added "4.0"}
  [model args save-output opts]
  (event-model/set-input model {:data args})
  (return (-/refresh-model-remote model save-output opts)))

(defn.js refresh-model-sync
  "refreshes model using sync function"
  {:added "4.0"}
  [model save-output opts]
  (when (xtd/get-in model ["pipeline" "sync" "handler"])
    (var [context disabled] (event-model/pipeline-prep model opts))
    (var #{acc} context)
    (return (. (event-model/pipeline-run-sync
                context
                save-output
                (fn [handler-fn context #{success error}]
                  (return (. (promise/x:promise
                              (fn []
                                (return (handler-fn context))))
                             (then success)
                             (catch error))))
                nil
                k/identity)
               (then (fn:> acc))))))

(defn.js refresh-args-sync
  "refreshes model using args function"
  {:added "4.0"}
  [model args save-output opts]
  (event-model/set-input model {:data args})
  (return (-/refresh-model-sync model save-output opts)))

(defn.js make-model
  "makes and initialises model"
  {:added "4.0"}
  [main-handler
   pipeline
   default-args
   default-output
   default-process
   options]
  (var model (event-model/create-model
             main-handler
             pipeline
             default-args
             default-output
             default-process
             options))
  (event-model/init-model model)
  (xt/x:set-key model "init" (-/refresh-model model))
  (return model))

(defn.js makeModelRaw
  "makes a react compatible model without r/const"
  {:added "4.0"}
  [#{handler
     pipeline
     defaultArgs
     defaultOutput
     defaultProcess
     options}]
  (return
   (-/make-model handler
                (or pipeline {})
                defaultArgs
                defaultOutput
                defaultProcess
                options)))

(defn.js makeModel
  "makes a react compatible model"
  {:added "4.0"}
  [#{handler
     pipeline
     defaultArgs
     defaultOutput
     defaultProcess
     options}]
  (return
   (r/const (-/makeModelRaw #{handler
                             pipeline
                             defaultArgs
                             defaultOutput
                             defaultProcess
                             options}))))

(def.js TYPES
  {:input    [event-model/get-input  "current"]
   :output   [event-model/get-output "current"]
   :pending  [event-model/get-output "pending"]
   :elapsed  [event-model/get-output "elapsed"]
   :disabled [event-model/get-output "disabled"]
   :success  [event-model/get-success nil "output"]})

(defn.js initModelBase
  "initialises the model listener"
  {:added "4.0"}
  [model dest-key #{setResult
                   getResult
                   resultRef
                   resultTag
                   meta
                   pred}]
  (var #{resultFn
         resultPrint} (or meta {}))
  (var listener-id (. (Math.random)
                      (toString 36)
                      (substr 2 4)))
  (var cleanup (fn:> (event-model/remove-listener model listener-id)))
  (r/init []
     (event-model/add-listener
      model
      listener-id
      (fn [id data t meta]
        (var event (xtd/obj-clone data))
        (xt/x:set-key event "meta" meta)
        (var nresult (getResult))
        (when (and (or (k/nil? resultTag)
                       (== resultTag (. event data tag)))
                  (or (not= "model.output"
                            (. event type))
                      (== (. event data type)
                          (or dest-key "output")))
                  (not (xtt/eq-nested (r/curr resultRef)
                                     nresult)))
         #_(when (== "pending" (. event type))
           (trace/LOG! event))
         (setResult nresult))
       (when resultFn
         (resultFn event))
       (when (k/is-function? resultPrint)
         (resultPrint #{resultTag nresult event})))
     meta
     pred)
    (return cleanup))
  (return cleanup))

(defn.js listenModel
  "creates the most basic models"
  {:added "4.0"}
  [model type meta dest-key tag-key]
  (var [tfn tkey tevent] (xt/x:get-key -/TYPES type))
  (:= tevent (or tevent type))
  (var getResult (fn []
                   (var out (tfn model))
                   (return (xtd/clone-shallow
                            (:? tkey (. out [tkey]) out)))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (-/initModelBase model
                  dest-key
                  #{setResult
                    getResult
                    resultRef
                    
                    meta
                    {:resultTag tag-key
                     :resultFn  (xtd/get-in meta "resultFn")
                     :pred (fn [event]
                             (return (== (. event ["type"])
                                         (+ "model." tevent))))}})
  (return result))

(defn.js listenModelOutput
  "creates listeners on the output"
  {:added "4.0"}
  [model types meta dest-key tag-key]
  (var getOutput (fn:> (xtd/obj-clone (event-model/get-output model dest-key))))
  (var [output setOutput] (r/local getOutput))
  (var wrap (r/useIsMountedWrap))
  (var outputRef (r/useFollowRef output))
  (var pred
       (fn [event]
         (return
          (xtd/arr-some
           types
           (fn [type]
             (return (== (. event ["type"])
                         (+ "model." type))))))))
  (-/initModelBase model
                  dest-key
                  #{meta pred  
                    {:setResult (wrap setOutput)
                     :getResult getOutput
                     :resultRef outputRef
                     :resultTag tag-key
                     :resultFn  (xtd/get-in meta "resultFn")}})
  (return output))

(defn.js listenModelThrottled
  "creates the throttled listener"
  {:added "4.0"}
  [model delay meta dest-key]
  (var getResult (fn:> (xtd/clone-shallow
                        (event-model/get-success model))))
  (var [result setResult] (r/local getResult))
  (var resultRef (r/useFollowRef result))
  (var listener-id (. (Math.random)
                      (toString 36)
                      (substr 2 4)))
  (r/init []
    (var [setThrottled throttle] (-/throttled-setter setResult delay))
    (event-model/add-listener
     model
      listener-id
       (fn [_ _ _ _]
         (var nresult (getResult))
         (when (not (== (r/curr resultRef)
                      nresult))
           (setThrottled nresult)))
      meta
     (fn [event]
       (return (== "model.output"
                   (. event ["type"])))))
    (return
     (fn []
       (xt/x:set-key throttle "mounted" false)
       (event-model/remove-listener model listener-id))))
  (return result))

(defn.js wrap-pending
  "wraps function, setting pending flag"
  {:added "4.0"}
  [f with-pending]
  (if with-pending
    (return f)
    (return
     (fn [model ...args]
       (event-model/set-pending model true)
       (return
         (. (promise/x:promise
             (fn []
               (return (f model ...args))))
            (then (fn [res]
                    (event-model/set-pending model false)
                    (return res)))))))))

(defn.js refreshArgsFn
  "creates the refresh args function"
  {:added "4.0"}
  [model args opts]
  (cond (xtd/arr-every args k/not-nil?)
        (return
         (. (-/refresh-args model args opts)
            (then
             (fn [acc]
               (var [ok data] (xtd/get-in acc ["main"]))
               (when (not ok) (throw data))
               (cond (== (. opts remote) "always")
                     (do (return
                          ((-/wrap-pending -/refresh-args-remote
                                           (. opts with-pending))
                           model args true opts)))
                     
                     (== (. opts remote) "none")
                     (return nil)
                     
                     :else
                     (when (or (k/nil? (. opts remote-check))
                               (. opts (remote-check args)))
                       (if (xtd/not-empty? data)
                         (return (-/refresh-args-sync model args false opts))
                         (return (-/refresh-args-remote model args true opts)))))))))

        :else
        (return (fn:> (event-model/set-output model nil)))))

(defn.js useRefreshArgs
  "refreshes args on the model"
  {:added "4.0"}
  [model args opts]
  (:= opts (or opts {}))
  (r/watch [(xt/x:json-encode args)]
    (-/refreshArgsFn model args opts)))

(defn.js listenSuccess
  "listens to the successful output"
  {:added "4.0"}
  [model args opts meta tag-key]
  (:= opts (or opts {}))
  (var output (r/useStablized (-/listenModel model "success" meta (. opts dest) tag-key)
                              (. opts stablized)))
  (-/useRefreshArgs model args opts)
  (return ((or (. opts then)
               k/identity)
           (or output (. opts default)))))

;;
;; HELPERS
;;

(defn.js handler-base
  "constructs a base handler"
  {:added "0.1"}
  [handler m]
  (return
   (xtd/obj-assign-nested
    {:handler handler
     :defaultArgs []
     :defaultInit {:disabled true}}
    m)))

(defn.js oneshot-fn
  "creates a oneshot function"
  {:added "0.1"}
  []
  (var v true)
  (return (fn [ctx]
            (when v
              (:= v false)
              (return true))
            (return false))))

(defn.js input-disabled?
  "checks if input has been disabled (context method)"
  {:added "0.1"}
  [#{input}]
  (return (:? (or (k/nil? input)
                  (. input ["disabled"]))
              true
              false)))

(defn.js input-data
  "gets the input data (context method)"
  {:added "0.1"}
  [#{input}]
  (return (and input (. input ["data"]))))

(defn.js input-data-nil?
  "ensures that disabled flag or a nil input returns true"
  {:added "0.1"}
  [#{input}]
  (return (or (k/nil? input)
              (. input ["disabled"])
              (k/nil? (. input ["data"])))))

(defn.js output-empty?
  "checks that model is empty (context method)"
  {:added "0.1"}
  [#{model}]
  (return (xtd/is-empty? (event-model/get-current model))))

(comment
  
  (defn.js listenModelInit
    [model tags meta]
    (var initStart   (r/ref false))
    (var initTag     (r/ref false))
    (var [init setInit] (r/local false))
    (var out (-/listenModelOutput model
                                 ["pending" "output" "elapsed"]
                                 meta))
    (r/watch [out]
      (when (and (xtd/not-empty? (. out current))
                 (not init))
        (setInit true))
      (when (. out pending)
        (r/curr:set initStart true))
      (when (or (k/nil? tags)
                (. tags [(. out tag)]))
        (r/curr:set initTag true))
      (when (and (. out elapsed)
                 (r/curr initStart)
                 (r/curr initTag)
                 (not init))
        (setInit true)))
    (return init)))
