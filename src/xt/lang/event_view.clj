(ns xt.lang.event-view
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.event-common :as event-common]]})

(defspec.xt ViewHandler
  [:fn [:xt/any] :xt/any])

(defspec.xt ViewPipeline
  [:xt/dict :xt/str :xt/any])

(defspec.xt ViewInput
  [:xt/record
   ["current" [:xt/maybe [:xt/dict :xt/str :xt/any]]]
   ["updated" [:xt/maybe :xt/num]]
   ["default" [:fn [] :xt/any]]])

(defspec.xt ViewOutput
  [:xt/record
   ["type" :xt/str]
   ["current" [:xt/maybe :xt/any]]
   ["updated" [:xt/maybe :xt/num]]
   ["elapsed" [:xt/maybe :xt/num]]
   ["process" [:fn [:xt/any] :xt/any]]
   ["default" [:fn [] :xt/any]]])

(defspec.xt ViewRunState
  [:xt/dict :xt/str :xt/any])

(defspec.xt ViewContext
  [:xt/record
   ["view" EventView]
   ["input" [:xt/maybe [:xt/dict :xt/str :xt/any]]]])

(defspec.xt ViewEvent
  [:xt/record
   ["type" :xt/str]
   ["data" [:xt/maybe :xt/any]]
   ["meta" [:xt/maybe xt.lang.event-common/EventListenerMeta]]])

(defspec.xt EventView
  [:xt/record
   ["::" :xt/str]
   ["listeners" xt.lang.event-common/EventListenerMap]
   ["pipeline" ViewPipeline]
   ["options" [:xt/dict :xt/str :xt/any]]
   ["input" ViewInput]
   ["output" ViewOutput]])

(defspec.xt wrap-args
  [:fn [ViewHandler] [:fn [ViewContext] :xt/any]])

(defspec.xt check-disabled
  [:fn [ViewContext] :xt/bool])

(defspec.xt parse-args
  [:fn [ViewContext] [:xt/maybe :xt/any]])

(defspec.xt create-view
  [:fn [ViewHandler
        ViewPipeline
        :xt/any
        [:xt/maybe :xt/any]
        [:xt/maybe [:fn [:xt/any] :xt/any]]
        [:xt/maybe [:xt/dict :xt/str :xt/any]]]
       EventView])

(defspec.xt view-context
  [:fn [EventView] ViewContext])

(defspec.xt add-listener
  [:fn [EventView
        :xt/str
        [:fn [ViewEvent] :xt/any]
        [:xt/maybe xt.lang.event-common/EventListenerMeta]
        [:xt/maybe [:fn [ViewEvent] :xt/bool]]]
       xt.lang.event-common/EventListenerEntry])

(defspec.xt trigger-listeners
  [:fn [EventView :xt/str :xt/any] [:xt/array :xt/str]])

(defspec.xt get-input
  [:fn [EventView] ViewInput])

(defspec.xt get-output
  [:fn [EventView [:xt/maybe :xt/str]] ViewOutput])

(defspec.xt get-current
  [:fn [EventView [:xt/maybe :xt/str]] [:xt/maybe :xt/any]])

(defspec.xt is-disabled
  [:fn [EventView] :xt/bool])

(defspec.xt is-errored
  [:fn [EventView [:xt/maybe :xt/str]] :xt/bool])

(defspec.xt is-pending
  [:fn [EventView [:xt/maybe :xt/str]] :xt/bool])

(defspec.xt get-time-elapsed
  [:fn [EventView [:xt/maybe :xt/str]] [:xt/maybe :xt/num]])

(defspec.xt get-time-updated
  [:fn [EventView [:xt/maybe :xt/str]] [:xt/maybe :xt/num]])

(defspec.xt get-success
  [:fn [EventView [:xt/maybe :xt/str]] :xt/any])

(defspec.xt set-input
  [:fn [EventView [:xt/dict :xt/str :xt/any]] ViewInput])

(defspec.xt set-output
  [:fn [EventView :xt/any :xt/any [:xt/maybe :xt/str] [:xt/maybe :xt/str] [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt set-output-disabled
  [:fn [EventView :xt/any [:xt/maybe :xt/str]] ViewOutput])

(defspec.xt set-pending
  [:fn [EventView :xt/any [:xt/maybe :xt/str]] ViewOutput])

(defspec.xt set-elapsed
  [:fn [EventView :xt/any [:xt/maybe :xt/str]] ViewOutput])

(defspec.xt init-view
  [:fn [EventView] ViewInput])

(defspec.xt pipeline-prep
  [:fn [EventView [:xt/maybe [:xt/dict :xt/str :xt/any]]] [:tuple ViewContext :xt/bool]])

(defspec.xt pipeline-set
  [:fn [ViewContext :xt/str ViewRunState [:xt/maybe :xt/str]] ViewRunState])

(defspec.xt pipeline-call
  [:fn [ViewContext :xt/str :xt/bool :xt/any :xt/any [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt pipeline-run-impl
  [:fn [ViewContext [:xt/array :xt/str] :xt/int :xt/any :xt/any :xt/any [:xt/maybe [:xt/dict :xt/str :xt/any]]] :xt/any])

(defspec.xt pipeline-run
  [:fn [ViewContext :xt/bool :xt/any :xt/any :xt/any [:xt/maybe :xt/str]] :xt/any])

(defspec.xt pipeline-run-force
  [:fn [ViewContext :xt/bool :xt/any :xt/any :xt/any :xt/str] :xt/any])

(defspec.xt pipeline-run-remote
  [:fn [ViewContext :xt/bool :xt/any :xt/any :xt/any] :xt/any])

(defspec.xt pipeline-run-sync
  [:fn [ViewContext :xt/bool :xt/any :xt/any :xt/any] :xt/any])

(defspec.xt get-with-lookup
  [:fn [[:xt/array :xt/any] [:xt/maybe [:xt/dict :xt/str :xt/any]]] [:xt/dict :xt/str :xt/any]])

(defspec.xt sorted-lookup
  [:fn [[:xt/maybe :xt/str]] [:fn [[:xt/array :xt/any]] [:xt/dict :xt/str :xt/any]]])

(defspec.xt group-by-lookup
  [:fn [:xt/str] [:fn [[:xt/array :xt/any]] [:xt/dict :xt/str :xt/any]]])

;;
;; CREATE
;;

(defn.xt wrap-args
  "wraps handler for context args"
  {:added "4.0"}
  [handler]
  (var wrapped-fn
       (fn [context]
         (var args (:? (xt/x:nil? (. context ["args"])) [] (. context ["args"])))
         (return (xt/x:apply handler args))))
  (return wrapped-fn))

(defn.xt check-disabled
  "checks that view is disabled"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (return (:? (xt/x:nil? input)
              true
              (xt/x:nil?  (xt/x:get-key input "data"))
              true
              (== true (xt/x:get-key input "disabled"))
              true
              :else false)))

(defn.xt parse-args
  "parses args from context"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (return (xt/x:get-key input "data")))

(defn.xt create-view
  "creates a view"
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
  (var default-args-fn
       (:? (xt/x:is-function? default-args)
           default-args
           (fn []
             (return default-args))))
  (var default-output-fn
       (:? (xt/x:is-function? default-output)
           default-output
           (fn []
             (return default-output))))
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
                        :process (:? (xt/x:nil? default-process) identity-fn default-process)
                        :default default-output-fn}})
  (when (xtd/get-in pipeline ["remote"])
     (xt/x:set-key entry "remote" {:type "remote"
                                :current nil
                                :updated nil
                                :elapsed nil
                                 :process (:? (xt/x:nil? default-process) identity-fn default-process)
                                :default default-output-fn}))
  (when (xtd/get-in pipeline ["sync"])
     (xt/x:set-key entry "sync" {:type "sync"
                                :current nil
                                :updated nil
                                :elapsed nil
                                 :process (:? (xt/x:nil? default-process) identity-fn default-process)
                                :default default-output-fn}))
  (return
   (event-common/blank-container
    "event.view"
    entry)))

(defn.xt view-context
  "gets the view-context"
  {:added "4.0"}
  [view]
  (var #{pipeline options} view)
  (var #{input} view)
  (var context-opts (xt/x:get-key options "context"))
  (when (xt/x:nil? context-opts)
    (:= context-opts {}))
  (var context  (xt/x:obj-assign
                 {:view  view
                   :input (. input ["current"])}
                 context-opts))
  (return context))

(defn.xt add-listener
  "adds a listener to the view"
  {:added "4.0"}
  [view listener-id callback meta pred]
  (return
   (event-common/add-listener
    view listener-id "view"
    callback
    meta
    pred)))

(def.xt ^{:arglists '([view listener-id])}
  remove-listener
  event-common/remove-listener)

(def.xt ^{:arglists '([view])}
  list-listeners
  event-common/list-listeners)

(defn.xt trigger-listeners
  "triggers listeners to activate"
  {:added "4.0"}
  [view type-name data]
  (return
   (event-common/trigger-listeners
    view {:type type-name
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
  "gets the view input record"
  {:added "4.0"}
  [view]
  (var #{input} view)
  (return input))

(defn.xt get-output
  "gets the view output record"
  {:added "4.0"}
  [view dest-key]
  (return (. view [(:? (xt/x:nil? dest-key) "output" dest-key)])))

(defn.xt get-current
  "gets the current view output"
  {:added "4.0"}
  [view dest-key]
  (return (xtd/get-in view [(:? (xt/x:nil? dest-key) "output" dest-key)
                            "current"])))

(defn.xt is-disabled
  "checks that the view is disabled"
  {:added "4.0"}
  [view]
  (var #{pipeline} view)
  (var #{check-disabled} pipeline)
  (var context (-/view-context view))
  (return (check-disabled context)))

(defn.xt is-errored
  "checks that output is errored"
  {:added "4.0"}
  [view dest-key]
  (return (== true (xtd/get-in view [(:? (xt/x:nil? dest-key) "output" dest-key)
                                     "errored"]))))

(defn.xt is-pending
  "checks that output is pending"
  {:added "4.0"}
  [view dest-key]
  (return (== true (xtd/get-in view [(:? (xt/x:nil? dest-key) "output" dest-key)
                                     "pending"]))))

(defn.xt get-time-elapsed
  "gets time elapsed of output"
  {:added "4.0"}
  [view dest-key]
  (return (xtd/get-in view [(:? (xt/x:nil? dest-key) "output" dest-key)
                            "elapsed"])))

(defn.xt get-time-updated
  "gets time updated of output"
  {:added "4.0"}
  [view dest-key]
  (return (xtd/get-in view [(:? (xt/x:nil? dest-key) "output" dest-key)
                            "updated"])))

(defn.xt get-success
  "gets either the current or default value if errored"
  {:added "4.0"}
  [view dest-key]
  (var output (. view [(:? (xt/x:nil? dest-key) "output" dest-key)]))
  (var #{process} output)
  (if (== true (. output ["errored"]))
    (return (process ((. output ["default"]))))
    (return (:? (xt/x:nil? (. output ["current"]))
                (process ((. output ["default"])))
                (. output ["current"])))))

(defn.xt set-input
  "sets the input"
  {:added "4.0"}
  [view current]
  (var #{input
         callback} view)
  (xt/x:obj-assign input {:current current
                       :updated (xt/x:now-ms)})
  (-/trigger-listeners view "view.input" input)
  (return input))

(defn.xt set-output
  "sets the output"
  {:added "4.0"}
  [view current errored tag dest-key meta]
  (var output (. view [(:? (xt/x:nil? dest-key) "output" dest-key)]))
  (var #{options
         callback} view)
  (var #{accumulate} options)
  (if errored
    (xt/x:set-key output "errored" true)
    (when (xt/x:has-key? output "errored")
      (xt/x:del-key output "errored")))
  (xt/x:set-key output "updated" (xt/x:now-ms))
  (xt/x:set-key output "tag" tag)
  
  (cond accumulate
        (do (var prev (xtd/arrayify (xt/x:get-key output "current")))
            (var next (xt/x:arr-append
                       (xt/x:arr-clone prev)
                       (xtd/arrayify current)))
            (xt/x:set-key output "current" next))

        :else
        (xt/x:set-key output "current" current))
  (-/trigger-listeners view "view.output" output)
  (return current))

(defn.xt set-output-disabled
  "sets the output disabled flag"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(:? (xt/x:nil? dest-key) "output" dest-key)]))
  (var #{callback} view)
  (if value
    (xt/x:set-key output "disabled" value)
    (when (xt/x:has-key? output "disabled")
      (xt/x:del-key output "disabled")))
  (-/trigger-listeners view "view.disabled" value)
  (return output))

(defn.xt set-pending
  "sets the output pending time"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(:? (xt/x:nil? dest-key) "output" dest-key)]))
  (if value
    (xt/x:set-key output "pending" value)
    (when (xt/x:has-key? output "pending")
      (xt/x:del-key output "pending")))
  (-/trigger-listeners view "view.pending" value)
  (return output))

(defn.xt set-elapsed
  "sets the output elapsed time"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(:? (xt/x:nil? dest-key) "output" dest-key)]))
  (if (xt/x:is-number? value)
    (xt/x:set-key output "elapsed" value)
    (when (xt/x:has-key? output "elapsed")
      (xt/x:del-key output "elapsed")))
  (-/trigger-listeners view "view.elapsed" value)
  (return output))

(defn.xt init-view
  "initialises view"
  {:added "4.0"}
  [view]
  (var #{input options} view)
  (var #{init} options)
  (var data ((. input ["default"])))
  (return (-/set-input view (xt/x:obj-assign {:data data}
                                          init))))

;;
;;
;;

(defn.xt pipeline-prep
  "prepares the pipeline"
  {:added "4.0"}
  [view opts]
  (var #{pipeline} view)
  (var #{check-args check-disabled} pipeline)
  (var context  (xt/x:obj-assign (-/view-context view)
                              opts))
  (var disabled (check-disabled context))
  (var args (or (xt/x:get-key context "args")
                (:? (not disabled)
                    (check-args context)
                    nil)))
  (when (xt/x:nil? args)
    (:= disabled true))
  
  (xt/x:set-key context "args" (xtd/arrayify args))
  (xt/x:set-key context "acc"  {"::" "view.run"})
  #_(when (. context name)
    #_(when (xt/x:nil? (. context input data))
      (xt/x:throw "NO DATA"))
    (xt/x:LOG! context))
  
  (return [context disabled]))

(defn.xt pipeline-set
  "sets the pipeline"
  {:added "4.0"}
  [context tag acc dest-key]
  (var #{cell view} context)
  (var process (xtd/get-in view [(or dest-key "output")
                               "process"]))
  (var record (xt/x:get-key acc tag))
  (var update? (:? (< 0 (xt/x:len record))
                   (. record [0])
                   nil))
  (var current (:? (< 1 (xt/x:len record))
                   (. record [1])
                   nil))
  (var errored (:? (< 2 (xt/x:len record))
                   (. record [2])
                   nil))
  (when (xt/x:nil? current)
    (:= current ((xtd/get-in view [(or dest-key
                                      "output")
                                  "default"]))))
  
  (when update?
    (var output (:? errored
                    current
                    (process current)))
    (-/set-output view
                  output
                  errored
                  tag
                  dest-key
                  (xt/x:get-key context "meta")))
  (return acc))

(defn.xt pipeline-call
  "calls the pipeline with async function"
  {:added "4.0"}
  [context tag disabled async-fn hook-fn skip-guard]
  (var identity-fn (fn [x] (return x)))
  (:= skip-guard (or skip-guard {}))
  (:= hook-fn (or hook-fn identity-fn))
  (var #{cell model view args acc} context)
  (var #{pipeline} view)
  (var stage (or (xt/x:get-key pipeline tag)
                 {}))
  (var #{handler guard wrapper} stage)
  (:= wrapper (or wrapper identity-fn))
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
  (var [handler-fn
        success-fn] (:? (and (not disabled)
                             (xt/x:is-function? handler)
                             (or (xt/x:nil? guard)
                                 (xt/x:get-key skip-guard tag)
                                 (guard context acc))) [(wrapper handler) result-fn] [(fn [_] (return nil)) skipped-fn]))
  (return
   (async-fn handler-fn context
             {:success success-fn
              :error   error-fn})))

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
              (. stages [index])
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
  (var #{view acc} context)
  (:= dest-key (or dest-key "output"))
  (var dest-tag (:? (== dest-key "output")
                    "main"
                    dest-key))
  (var output (. view [dest-key]))
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
            (-/set-output-disabled view true dest-key)
            (return (-/pipeline-call context
                                     dest-tag
                                     true
                                     async-fn
                                     disabled-hook)))
        
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
                   (-/set-elapsed view (- (xt/x:now-ms) started) dest-key)
                   (-/set-pending view false dest-key)))
            (when (xt/x:get-key output "disabled")
              (-/set-output-disabled view false dest-key))
            (-/set-pending view true dest-key)
            (return
             (-/pipeline-run-impl context ["pre"
                                           dest-tag
                                           "post"]
                                  (xt/x:offset 0)
                                  async-fn
                                  run-hook
                                  run-complete)))))

(defn.xt pipeline-run-force
  "runs the pipeline via sync or remote paths"
  {:added "4.0"}
  [context save-output async-fn hook-fn complete-fn dest-key]
  (var #{acc view} context)
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
         (-/set-elapsed view (- (xt/x:now-ms) started) dest-key)
         (-/set-pending view false dest-key)))
  (-/set-pending view true dest-key)
  (return
   (-/pipeline-run-impl context ["pre"
                                 dest-key
                                 "post"]
                        (xt/x:offset 0)
                        async-fn
                        force-hook
                        force-complete)))
  
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
;; VIEW UTILS
;;


(defn.xt get-with-lookup
  "creates a results vector and a lookup table"
  {:added "0.1"}
  [results opts]
  (:= opts (or opts {}))
  (var #{sort-fn
         key-fn
         val-fn} opts)
  (:= results (:? sort-fn (sort-fn results) results))
  (:= key-fn (or key-fn
                 (fn [e]
                   (return (xt/x:get-key e "id")))))
  (:= val-fn (or val-fn
                 (fn [x]
                   (return x))))
  (return {:results results
           :lookup (xtd/arr-juxt (or results [])
                                 key-fn
                                 val-fn)}))

(defn.xt sorted-lookup
  "sorted lookup for region data"
  {:added "0.1"}
  [key]
  (var sort-key (or key "name"))
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
                         xt/x:lt)))})))))

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


(comment
  (defn.xt pipeline-run-remote
    "runs the pipeline"
    {:added "4.0"}
    [context save-output async-fn hook-fn complete-fn]
    (var #{acc} context)
    (return
     (-/pipeline-run-impl context ["pre"
                                   "remote"
                                   "post"]
                          (xt/x:offset 0)
                          async-fn
                          (fn [acc tag]
                            (when hook-fn
                              (hook-fn acc tag))
                            (when (and (== tag "remote")
                                       save-output)
                              (-/pipeline-set context tag acc "output")))
                          complete-fn
                          {:remote true})))

  (defn.xt pipeline-run-sync
    "runs the sync pipeline"
    {:added "4.0"}
    [context save-output async-fn hook-fn complete-fn]
    (var #{acc} context)
    (return
     (-/pipeline-run-impl context ["pre"
                                   "sync"
                                   "post"]
                          (xt/x:offset 0)
                          async-fn
                          (fn [acc tag]
                            (when hook-fn
                              (hook-fn acc tag))
                            (when (and (== tag "sync")
                                       save-output)
                              (-/pipeline-set context tag acc "output")))
                          complete-fn
                          {:sync true}))))
