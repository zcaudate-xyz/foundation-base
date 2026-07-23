(ns xt.event.base-model
  (:require [hara.lang :as l]
            [hara.typed :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.event.base-listener :as event-common]]})

(defspec.xt ModelHandler
  [:fn [:xt/any] :xt/any])

(defspec.xt ModelPipeline
  [:xt/dict :xt/str :xt/any])

(defspec.xt ModelInput
  [:xt/record
   ["current" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["updated" [:xt/maybe :xt/num]]
   ["default" [:fn [] :xt/any]]])

(defspec.xt ModelOutput
  [:xt/record
   ["type" :xt/str]
   ["current" [:xt/maybe :xt/any]]
   ["updated" [:xt/maybe :xt/num]]
   ["elapsed" [:xt/maybe :xt/num]]
   ["process" [:fn [:xt/any] :xt/any]]
   ["default" [:fn [] :xt/any]]])

(defspec.xt ModelRunState
  [:xt/dict :xt/str :xt/any])

(defspec.xt ModelContext
  [:xt/record
   ["model" EventModel]
   ["input" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt ModelEvent
  [:xt/record
   ["type" :xt/str]
   ["data" [:xt/maybe :xt/any]]
   ["meta" [:xt/maybe xt.event.base-listener/EventListenerMeta]]])

(defspec.xt EventModel
  [:xt/record
   ["::" :xt/str]
   ["listeners" xt.event.base-listener/EventListenerMap]
   ["pipeline" ModelPipeline]
   ["options" [:xt/dict :xt/str :xt/any]]
   ["input" ModelInput]
   ["output" ModelOutput]])

(defspec.xt wrap-args
  [:fn [ModelHandler] [:fn [ModelContext] :xt/any]])

(defspec.xt check-disabled
  [:fn [ModelContext] :xt/bool])

(defspec.xt parse-args
  [:fn [ModelContext] [:xt/maybe :xt/any]])

(defspec.xt create-model
  [:fn [ModelHandler
        ModelPipeline
        :xt/any
        [:xt/maybe :xt/any]
        [:xt/maybe [:fn [:xt/any] :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       EventModel])

(defspec.xt model-context
  [:fn [EventModel] ModelContext])

(defspec.xt add-listener
  [:fn [EventModel
        :xt/str
        xt.event.base-listener/EventListenerCallback
        [:xt/maybe xt.event.base-listener/EventListenerMeta]
        [:xt/maybe [:fn [ModelEvent] :xt/bool]]]
       xt.event.base-listener/EventListenerEntry])

(defspec.xt trigger-listeners
  [:fn [EventModel :xt/str :xt/any] [:xt/array :xt/str]])

(defspec.xt get-input
  [:fn [EventModel] ModelInput])

(defspec.xt get-output
  [:fn [EventModel [:xt/maybe :xt/str]] ModelOutput])

(defspec.xt get-current
  [:fn [EventModel [:xt/maybe :xt/str]] [:xt/maybe :xt/any]])

(defspec.xt is-disabled
  [:fn [EventModel] :xt/bool])

(defspec.xt is-errored
  [:fn [EventModel [:xt/maybe :xt/str]] :xt/bool])

(defspec.xt is-pending
  [:fn [EventModel [:xt/maybe :xt/str]] :xt/bool])

(defspec.xt get-time-elapsed
  [:fn [EventModel [:xt/maybe :xt/str]] [:xt/maybe :xt/num]])

(defspec.xt get-time-updated
  [:fn [EventModel [:xt/maybe :xt/str]] [:xt/maybe :xt/num]])

(defspec.xt get-success
  [:fn [EventModel [:xt/maybe :xt/str]] :xt/any])

(defspec.xt set-input
  [:fn [EventModel [:xt/dict :xt/str :xt/any]] ModelInput])

(defspec.xt set-output
  [:fn [EventModel :xt/any :xt/any [:xt/maybe :xt/str] [:xt/maybe :xt/str] [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt set-output-disabled
  [:fn [EventModel :xt/any [:xt/maybe :xt/str]] ModelOutput])

(defspec.xt set-pending
  [:fn [EventModel :xt/any [:xt/maybe :xt/str]] ModelOutput])

(defspec.xt set-elapsed
  [:fn [EventModel :xt/any [:xt/maybe :xt/str]] ModelOutput])

(defspec.xt init-model
  [:fn [EventModel] ModelInput])

(defspec.xt pipeline-prep
  [:fn [EventModel [:xt/maybe [:xt/dict :xt/str :xt/any]]] [:tuple ModelContext :xt/bool]])

(defspec.xt pipeline-set
  [:fn [ModelContext :xt/str ModelRunState [:xt/maybe :xt/str]] ModelRunState])

(defspec.xt pipeline-call
  [:fn [ModelContext :xt/str :xt/bool :xt/any :xt/any [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt pipeline-run-impl
  [:fn [ModelContext [:xt/array :xt/str] :xt/int :xt/any :xt/any :xt/any [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt pipeline-run
  [:fn [ModelContext :xt/bool :xt/any :xt/any :xt/any [:xt/maybe :xt/str]] :xt/any])

(defspec.xt pipeline-run-force
  [:fn [ModelContext :xt/bool :xt/any :xt/any :xt/any :xt/str] :xt/any])

(defspec.xt pipeline-run-remote
  [:fn [ModelContext :xt/bool :xt/any :xt/any :xt/any] :xt/any])

(defspec.xt pipeline-run-sync
  [:fn [ModelContext :xt/bool :xt/any :xt/any :xt/any] :xt/any])

(defspec.xt get-with-lookup
  [:fn [[:xt/array :xt/any] [:xt/maybe [:xt/dict :xt/str :xt/any]]] [:xt/dict :xt/str :xt/any]])

(defspec.xt sorted-lookup
  [:fn [[:xt/maybe :xt/str]] [:fn [[:xt/array :xt/any]] [:xt/dict :xt/str :xt/any]]])

(defspec.xt group-by-lookup
  [:fn [:xt/str] [:fn [[:xt/array :xt/any]] [:xt/dict :xt/str :xt/any]]])


;;
;; ASYNC
;;

(defn.xt async-fn-basic
  [handler context callbacks]
  (var #{success error} callbacks)
  (try
    (var output (handler context))
    (return
     (success output))
    (catch err (return (error err)))))

(defn.xt async-fn-promise
  [handler context callbacks]
  (var #{success error} callbacks)
  (try
    (var output (handler context))
    (if (promise/x:promise-native? output)
      (return
       (promise/x:promise-catch
        (promise/x:promise-then output success)
        error))
      (return (promise/x:promise-run (success output))))
    (catch err
        (return (promise/x:promise-run (error err))))))


;;
;; CREATE
;;

(defn.xt wrap-args
  "wraps handler for context args"
  {:added "4.0"}
  [handler]
  (var wrapped-fn
       (fn [context]
         (var #{args} context)
         (when (xt/x:nil? args)
           (:= args []))
         (return (xt/x:apply handler args))))
  (return wrapped-fn))

(defn.xt check-disabled
  "checks that model is disabled"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (when (xt/x:nil? input)
    (return true))
  (when (xt/x:nil? (. input ["data"]))
    (return true))
  (when (== true (. input ["disabled"]))
    (return true))
  (return false))

(defn.xt parse-args
  "parses args from context"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (return (. input ["data"])))

(defn.xt create-model
  "creates a model"
  {:added "4.0"}
  [main-handler
   pipeline
   default-args
   default-output
   default-process
   options]
  (var identity-fn (fn [x] (return x)))
  (when (xt/x:nil? options)
    (:= options {}))
  (var default-args-fn default-args)

  (when (not (xt/x:is-function? default-args-fn))
    (var args-value default-args-fn)
    (:= default-args-fn
        (fn []
          (return args-value))))
  (var default-output-fn default-output)
  (when (not (xt/x:is-function? default-output-fn))
    (var output-value default-output-fn)
    (:= default-output-fn
        (fn []
          (return output-value))))
  (var process-fn default-process)
  (when (xt/x:nil? process-fn)
    (:= process-fn identity-fn))
  (var entry {:pipeline  (xtd/obj-assign-nested
                          {:main    {:handler main-handler
                                     :wrapper -/wrap-args}
                           :remote  {:wrapper -/wrap-args
                                     #_#_:guard (fn:> false)}
                           :sync    {:wrapper -/wrap-args
                                     #_#_:guard (fn:> false)}
                           :check-args  -/parse-args    
                           :check-disabled -/check-disabled}
                          pipeline)
              :options    options
              :input  {:current nil
                       :updated nil
                       :default default-args-fn}
              :output {:type "output"
                       :current nil
                       :updated nil
                       :elapsed nil
                       :process process-fn
                       :default default-output-fn}})
  (when (xt/x:not-nil? (xtd/get-in pipeline ["remote"]))
    (xt/x:set-key entry "remote" {:type "remote"
                                  :current nil
                                  :updated nil
                                  :elapsed nil
                                  :process process-fn
                                  :default default-output-fn}))
  (when (xt/x:not-nil? (xtd/get-in pipeline ["sync"]))
    (xt/x:set-key entry "sync" {:type "sync"
                                :current nil
                                :updated nil
                                :elapsed nil
                                :process process-fn
                                :default default-output-fn}))
  (return
   (event-common/blank-container
    "event.model"
    entry)))

(defn.xt model-context
  "gets the model-context"
  {:added "4.0"}
  [model]
  (var #{pipeline options} model)
  (var #{input} model)
  (var context  (xt/x:obj-assign
                 {:model  model
                  :input (. input ["current"])}
                 (. options ["context"])))
  (return context))

(defn.xt add-listener
  "adds a listener to the model"
  {:added "4.0"}
  [model listener-id callback meta pred]
  (return
   (event-common/add-listener
    model listener-id "model"
    callback
    meta
    pred)))

(defn.xt remove-listener
  "removes a listener from the model"
  {:added "4.0"}
  [model listener-id]
  (return (event-common/remove-listener model listener-id)))

(defn.xt list-listeners
  "lists all model listeners"
  {:added "4.0"}
  [model]
  (return (event-common/list-listeners model)))

(defn.xt trigger-listeners
  "triggers listeners to activate"
  {:added "4.0"}
  [model type-name data]
  (return
   (event-common/trigger-listeners
    model {:type type-name
          :data data})))

(def.xt PIPELINE
  {:pre     {:guard      nil
             :handler    nil}
   :main    {:guard      nil
             :handler    nil}
   :sync    {:guard      nil
             :handler    nil}
   :remote  {:guard      nil
             :handler    nil}
   :post    {:guard      nil
             :handler    nil}})

(defn.xt get-input
  "gets the model input record"
  {:added "4.0"}
  [model]
  (var #{input} model)
  (return input))

(defn.xt get-output
  "gets the model output record"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (. model [dest-key])))

(defn.xt get-current
  "gets the current model output"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (xtd/get-in model [dest-key
                            "current"])))

(defn.xt is-disabled
  "checks that the model is disabled"
  {:added "4.0"}
  [model]
  (var #{pipeline} model)
  (var #{check-disabled} pipeline)
  (var context (-/model-context model))
  (return (check-disabled context)))

(defn.xt is-errored
  "checks that output is errored"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (== true (xtd/get-in model [dest-key
                                     "errored"]))))

(defn.xt is-pending
  "checks that output is pending"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (== true (xtd/get-in model [dest-key
                                     "pending"]))))

(defn.xt get-time-elapsed
  "gets time elapsed of output"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (xtd/get-in model [dest-key
                            "elapsed"])))

(defn.xt get-time-updated
  "gets time updated of output"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (return (xtd/get-in model [dest-key
                            "updated"])))

(defn.xt get-success
  "gets either the current or default value if errored"
  {:added "4.0"}
  [model dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var output (xt/x:get-key model dest-key))
  (var #{process} output)
  (if (== true (. output ["errored"]))
    (return (process ((. output ["default"]))))
    (do (var #{current} output)
        (when (xt/x:nil? current)
          (:= current (process ((. output ["default"])))))
        (return current))))

(defn.xt set-input
  "sets the input"
  {:added "4.0"}
  [model current]
  (var #{input
         callback} model)
  (xt/x:obj-assign input {:current current
                          :updated (xt/x:now-ms)})
  (-/trigger-listeners model "model.input" (-/get-input model))
  (return input))

(defn.xt set-output
  "sets the output"
  {:added "4.0"}
  [model current errored tag dest-key meta]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var output (xt/x:get-key model dest-key))

  (var #{options
         callback} model)
  (var #{accumulate} options)
  (if errored
    (xt/x:set-key output "errored" true)
    (when (xt/x:has-key? output "errored")
      (xt/x:del-key output "errored")))
  (xt/x:set-key output "updated" (xt/x:now-ms))
  (xt/x:set-key output "tag" tag)
  
  (cond accumulate
        (do (var prev (xtd/arrayify (. output ["current"])))
            (var next (xt/x:arr-assign
                       (xt/x:arr-clone prev)
                       (xtd/arrayify current)))
            (xt/x:set-key output "current" next))

         :else
         (xt/x:set-key output "current" current))
  (-/trigger-listeners model "model.output" output)
  (return current))

(defn.xt set-output-disabled
  "sets the output disabled flag"
  {:added "4.0"}
  [model value dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var output (xt/x:get-key model dest-key))
  (var #{callback} model)
  (if value
    (xt/x:set-key output "disabled" value)
    (when (xt/x:has-key? output "disabled")
      (xt/x:del-key output "disabled")))
  (-/trigger-listeners model "model.disabled" output)
  (return output))

(defn.xt set-pending
  "sets the output pending time"
  {:added "4.0"}
  [model value dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var output (xt/x:get-key model dest-key))
  (if value
    (xt/x:set-key output "pending" value)
    (when (xt/x:has-key? output "pending")
      (xt/x:del-key output "pending")))
  (-/trigger-listeners model "model.pending" output)
  (return output))

(defn.xt set-elapsed
  "sets the output elapsed time"
  {:added "4.0"}
  [model value dest-key]
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var output (. model [dest-key]))
  (if (xt/x:is-number? value)
    (xt/x:set-key output "elapsed" value)
    (when (xt/x:has-key? output "elapsed")
      (xt/x:del-key output "elapsed")))
  (-/trigger-listeners model "model.elapsed" output)
  (return output))

(defn.xt init-model
  "initialises model"
  {:added "4.0"}
  [model]
  (var #{input options} model)
  (var #{init} options)
  (var data ((. input ["default"])))
  (return (-/set-input model (xt/x:obj-assign {:data data}
                                             init))))

;;
;;
;;

(defn.xt pipeline-prep
  "prepares the pipeline"
  {:added "4.0"}
  [model opts]
  (var #{pipeline} model)
  (var #{check-args check-disabled} pipeline)
  (var context  (xt/x:obj-assign (-/model-context model)
                                 opts))
  (var disabled (check-disabled context))
  (var #{args} context)
  (when (xt/x:nil? args)
    (when (not disabled)
      (:= args (check-args context))))
  (when (xt/x:nil? args)
    (:= disabled true))
  
  (xt/x:set-key context "args" (xtd/arrayify args))
  (xt/x:set-key context "acc"  {"::" "model.run"})
  (return [context disabled]))

(defn.xt pipeline-set
  "sets the pipeline"
  {:added "4.0"}
  [context tag acc dest-key]
  (var #{model} context)
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var process (xtd/get-in model [dest-key
                                 "process"]))
  (var record (xt/x:get-key acc tag))
  (var should-update nil)
  (when (< 0 (xt/x:len record))
    (:= should-update (xt/x:get-idx record (xt/x:offset 0))))
  (var current nil)
  (when (< 1 (xt/x:len record))
    (:= current (xt/x:get-idx record (xt/x:offset 1))))
  (var errored nil)
  (when (< 2 (xt/x:len record))
    (:= errored (xt/x:get-idx record (xt/x:offset 2))))
  (when (xt/x:nil? current)
    (:= current ((xtd/get-in model [dest-key
                                   "default"]))))
  
  (when should-update
    (var output current)
    (when (not errored)
      (:= output (process current)))
    (-/set-output model
                  output
                  errored
                  tag
                  dest-key
                  (. context ["meta"])))
  (return acc))

(defn.xt pipeline-call
  "calls the pipeline with async function"
  {:added "4.0"}
  [context tag disabled async-fn hook-fn skip-guard]
  (var identity-hook (fn [acc _tag] (return acc)))
  (var identity-wrapper (fn [handler] (return handler)))
  (when (xt/x:nil? skip-guard)
    (:= skip-guard {}))
  (when (xt/x:nil? hook-fn)
    (:= hook-fn identity-hook))
  (var #{model args acc} context)
  (var #{pipeline} model)
  (var stage (xt/x:get-key pipeline tag))
  (when (xt/x:nil? stage)
    (:= stage {}))
  (var #{handler guard wrapper} stage)
  (when (xt/x:nil? wrapper)
    (:= wrapper identity-wrapper))
  (var error-fn   (fn [err]
                    (:= (. acc [tag]) [true err true])
                    (:= (. acc ["error"]) true)
                    (return (hook-fn acc tag))))
  (var skipped-fn  (fn [res]
                     (:= (. acc [tag]) [false])
                     (return (hook-fn acc tag))))
  (var result-fn   (fn [res]
                     (:= (. acc [tag]) [true res])
                     (return (hook-fn acc tag))))
  (var handler-fn nil)
  (var success-fn nil)
  (if (and (not disabled)
           (xt/x:is-function? handler)
           (or (xt/x:nil? guard)
               (xt/x:get-key skip-guard tag)
               (guard context acc)))
    (do (:= handler-fn (wrapper handler))
        (:= success-fn result-fn))
    (do (:= handler-fn (fn [_] (return nil)))
        (:= success-fn skipped-fn)))
  (return
   (async-fn handler-fn context
             {"success" success-fn
              "error"   error-fn})))

(defn.xt pipeline-run-impl
  "runs the pipeline"
  {:added "4.0"}
  [context stages index async-fn hook-fn complete-fn skip-guard]
  (cond (< index (xt/x:offset (xt/x:len stages)))
        (do (var next-hook
                 (fn [acc tag]
                   (when hook-fn
                     (hook-fn acc tag))
                   (return
                    (-/pipeline-run-impl
                     context stages (xt/x:inc index) async-fn hook-fn complete-fn skip-guard))))
            (return
             (-/pipeline-call
              context
              (xt/x:get-idx stages index)
              false
              async-fn
              next-hook
              skip-guard)))
        
        :else
        (return (complete-fn context))))

(defn.xt pipeline-run
  "runs the pipeline"
  {:added "4.0"}
  [context disabled async-fn hook-fn complete-fn dest-key]
  (var #{model acc} context)
  (when (xt/x:nil? dest-key)
    (:= dest-key "output"))
  (var dest-tag dest-key)
  (when (== dest-key "output")
    (:= dest-tag "main"))
  (var output (xt/x:get-key model dest-key))
  (var started (xt/x:now-ms))
  (when (xt/x:has-key? output "elapsed")
    (xt/x:del-key output "elapsed"))
  (cond disabled
        (do (var disabled-hook
                 (fn [acc tag]
                   (when hook-fn
                     (hook-fn acc tag))
                   (when complete-fn
                     (complete-fn acc))))
             (-/set-output-disabled model true dest-key)
             (return (-/pipeline-call context
                                      dest-tag
                                      true
                                      async-fn
                                      disabled-hook
                                      nil)))
        
        :else
        (do (var run-hook
                 (fn [acc tag]
                   (when hook-fn
                     (hook-fn acc tag))
                   (when (== tag dest-tag)
                     (-/pipeline-set context tag acc dest-key))))
            (var run-complete
                 (fn [acc]
                   (when complete-fn
                     (complete-fn acc))
                   (-/set-elapsed model (- (xt/x:now-ms) started) dest-key)
                   (-/set-pending model false dest-key)))
            (when (. output ["disabled"])
              (-/set-output-disabled model false dest-key))
            (-/set-pending model true dest-key)
            (return
             (-/pipeline-run-impl context ["pre"
                                           dest-tag
                                           "post"]
                                  (xt/x:offset 0)
                                  async-fn
                                  run-hook
                                  run-complete
                                  nil)))))

(defn.xt pipeline-run-force
  "runs the pipeline via sync or remote paths"
  {:added "4.0"}
  [context save-output async-fn hook-fn complete-fn dest-key]
  (var #{acc model} context)
  (var started (xt/x:now-ms))
  (var force-hook
       (fn [acc tag]
         (when hook-fn
           (hook-fn acc tag))
         (when (== tag dest-key)
           (-/pipeline-set context tag acc dest-key)
           (when save-output
             (-/pipeline-set context tag acc "output")))))
  (var force-complete
       (fn [acc]
         (when complete-fn
           (complete-fn acc))
         (-/set-elapsed model (- (xt/x:now-ms) started) dest-key)
         (-/set-pending model false dest-key)))
  (-/set-pending model true dest-key)
  (return
   (-/pipeline-run-impl context ["pre"
                                 dest-key
                                 "post"]
                        (xt/x:offset 0)
                        async-fn
                        force-hook
                        force-complete
                        nil)))
  
(defn.xt pipeline-run-remote
  "runs the remote pipeline"
  {:added "4.0"}
  [context save-output async-fn hook-fn complete-fn]
  (return (-/pipeline-run-force context save-output async-fn hook-fn complete-fn "remote")))

(defn.xt pipeline-run-sync
  "runs the sync pipeline"
  {:added "4.0"}
  [context save-output async-fn hook-fn complete-fn]
  (return (-/pipeline-run-force context save-output async-fn hook-fn complete-fn "sync")))


;;
;; MODEL UTILS
;;


(defn.xt get-with-lookup
  "creates a results vector and a lookup table"
  {:added "0.1"}
  [results opts]
  (when (xt/x:nil? opts)
    (:= opts {}))
  (var #{sort-fn
         key-fn
         val-fn} opts)
  (when (xt/x:not-nil? sort-fn)
    (:= results (sort-fn results)))
  (when (xt/x:nil? key-fn)
    (:= key-fn
        (fn [e]
          (return (. e ["id"])))))
  (when (xt/x:nil? val-fn)
    (:= val-fn
        (fn [x]
          (return x))))
  (when (xt/x:nil? results)
    (:= results []))
  (return {:results results
           :lookup (xtd/arr-juxt results
                                 key-fn
                                 val-fn)}))

(defn.xt sorted-lookup
  "sorted lookup for region data"
  {:added "0.1"}
  [key]
  (var sort-key key)
  (when (xt/x:nil? sort-key)
    (:= sort-key "name"))
  (return
   (fn [results]
     (return
      (-/get-with-lookup
       results
       {:sort-fn
        (fn [arr]
          (return
           (xtd/arr-sort arr
                         (fn [e]
                           (return (xt/x:get-key e sort-key)))
                         xt/x:str-lt)))})))))

(defn.xt group-by-lookup
  "creates group-by lookup"
  {:added "0.1"}
  [key]
  (return
   (fn [results]
     (return
      {:results results
       :lookup (xtd/arr-group-by results
                                 (fn [e] (return (xt/x:get-key e key)))
                                 (fn [x] (return x)))}))))
