(ns xt.lang.event-view
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.base-lib :as k]
             [xt.lang.event-common :as event-common]]})

;;
;; CREATE
;;

(defn.xt wrap-args
  "wraps handler for context args"
  {:added "4.0"}
  [handler]
  (return (fn [context]
            (return (handler (k/unpack (. context ["args"])))))))

(defn.xt check-disabled
  "checks that view is disabled"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (return (or (k/nil? input)
              (k/nil?  (k/get-key input "data"))
              (k/get-key input "disabled")
              false)))

(defn.xt parse-args
  "parses args from context"
  {:added "4.0"}
  [context]
  (var #{input} context)
  (return (k/get-key input "data")))

(defn.xt create-view
  "creates a view"
  {:added "4.0"}
  [main-handler
   pipeline
   default-args
   default-output
   default-process
   options]
  (var entry {:pipeline  (k/obj-assign-nested
                          {:main    {:handler main-handler
                                     :wrapper -/wrap-args}
                           :remote  {:wrapper -/wrap-args
                                     #_#_:guard (fn:> false)}
                           :sync    {:wrapper -/wrap-args
                                     #_#_:guard (fn:> false)}
                           :check-args  -/parse-args    
                           :check-disabled -/check-disabled}
                          pipeline)
              :options    (or options {})
              :input  {:current nil
                       :updated nil
                       :default (:? (k/fn? default-args) default-args (fn:> default-args))}
              :output {:type "output"
                       :current nil
                       :updated nil
                       :elapsed nil
                       :process (or default-process k/identity)
                       :default (:? (k/fn? default-output) default-output (fn:> default-output))}})
  (when (k/get-in pipeline ["remote"])
    (k/set-key entry "remote" {:type "remote"
                               :current nil
                               :updated nil
                               :elapsed nil
                               :process (or default-process k/identity)
                               :default (:? (k/fn? default-output) default-output (fn:> default-output))}))
  (when (k/get-in pipeline ["sync"])
    (k/set-key entry "sync" {:type "sync"
                               :current nil
                               :updated nil
                               :elapsed nil
                               :process (or default-process k/identity)
                               :default (:? (k/fn? default-output) default-output (fn:> default-output))}))
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
  (var context  (k/obj-assign
                 {:view  view
                  :input (. input ["current"])}
                 (k/get-key options "context")))
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
  (return (. view [(or dest-key "output")])))

(defn.xt get-current
  "gets the current view output"
  {:added "4.0"}
  [view dest-key]
  (return (k/get-in view [(or dest-key "output")
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
  (return (== true (k/get-in view [(or dest-key "output")
                                   "errored"]))))

(defn.xt is-pending
  "checks that output is pending"
  {:added "4.0"}
  [view dest-key]
  (return (== true (k/get-in view [(or dest-key "output")
                                   "pending"]))))

(defn.xt get-time-elapsed
  "gets time elapsed of output"
  {:added "4.0"}
  [view dest-key]
  (return (k/get-in view [(or dest-key "output")
                          "elapsed"])))

(defn.xt get-time-updated
  "gets time updated of output"
  {:added "4.0"}
  [view dest-key]
  (return (k/get-in view [(or dest-key "output")
                          "updated"])))

(defn.xt get-success
  "gets either the current or default value if errored"
  {:added "4.0"}
  [view dest-key]
  (var output (. view [(or dest-key "output")]))
  (var #{process} output)
  (if (== true (. output ["errored"]))
    (return (process ((. output ["default"]))))
    (return (or (. output ["current"])
                (process ((. output ["default"])))))))

(defn.xt set-input
  "sets the input"
  {:added "4.0"}
  [view current]
  (var #{input
         callback} view)
  (k/obj-assign input {:current current
                       :updated (k/now-ms)})
  (-/trigger-listeners view "view.input" input)
  (return input))

(defn.xt set-output
  "sets the output"
  {:added "4.0"}
  [view current errored tag dest-key meta]
  (var output (. view [(or dest-key "output")]))
  (var #{options
         callback} view)
  (var #{accumulate} options)
  (if errored
    (k/set-key output "errored" true)
    (k/del-key output "errored"))
  (k/set-key output "updated" (k/now-ms))
  (k/set-key output "tag" tag)
  
  (cond accumulate
        (do (var prev (k/arrayify (k/get-key output "current")))
            (var next (k/arr-append
                       (k/arr-clone prev)
                       (k/arrayify current)))
            (k/set-key output "current" next))

        :else
        (k/set-key output "current" current))
  (-/trigger-listeners view "view.output" output)
  (return current))

(defn.xt set-output-disabled
  "sets the output disabled flag"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(or dest-key "output")]))
  (var #{callback} view)
  (if value
    (k/set-key output "disabled" value)
    (k/del-key output "disabled"))
  (-/trigger-listeners view "view.disabled" value)
  (return output))

(defn.xt set-pending
  "sets the output pending time"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(or dest-key "output")]))
  (if value
    (k/set-key output "pending" value)
    (k/del-key output "pending"))
  (-/trigger-listeners view "view.pending" value)
  (return output))

(defn.xt set-elapsed
  "sets the output elapsed time"
  {:added "4.0"}
  [view value dest-key]
  (var output (. view [(or dest-key "output")]))
  (if (k/is-number? value)
    (k/set-key output "elapsed" value)
    (k/del-key output "elapsed"))
  (-/trigger-listeners view "view.elapsed" value)
  (return output))

(defn.xt init-view
  "initialises view"
  {:added "4.0"}
  [view]
  (var #{input options} view)
  (var #{init} options)
  (var data ((. input ["default"])))
  (return (-/set-input view (k/obj-assign {:data data}
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
  (var context  (k/obj-assign (-/view-context view)
                              opts))
  (var disabled (check-disabled context))
  (var args (or (k/get-key context "args")
                (:? (not disabled)
                    (check-args context)
                    nil)))
  (when (k/nil? args)
    (:= disabled true))
  
  (k/set-key context "args" (k/arrayify args))
  (k/set-key context "acc"  {"::" "view.run"})
  #_(when (. context name)
    #_(when (k/nil? (. context input data))
      (x:throw "NO DATA"))
    (k/LOG! context))
  
  (return [context disabled]))

(defn.xt pipeline-set
  "sets the pipeline"
  {:added "4.0"}
  [context tag acc dest-key]
  (var #{cell view} context)
  (var process (k/get-in view [(or dest-key "output")
                               "process"]))
  (var [update? current errored] (k/get-key acc tag))
  (when (k/nil? current)
    (:= current ((k/get-in view [(or dest-key
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
                  (. context meta)))
  (return acc))

(defn.xt pipeline-call
  "calls the pipeline with async function"
  {:added "4.0"}
  [context tag disabled async-fn hook-fn skip-guard]
  (:= skip-guard (or skip-guard {}))
  (:= hook-fn (or hook-fn k/identity))
  (var #{cell model view args acc} context)
  (var #{pipeline} view)
  (var stage (or (k/get-key pipeline tag)
                 {}))
  (var #{handler guard wrapper} stage)
  (:= wrapper (or wrapper k/identity))
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
                             (k/fn? handler)
                             (or (k/nil? guard)
                                 (k/get-key skip-guard tag)
                                 (guard context acc))) [(wrapper handler) result-fn] [(fn:>) skipped-fn]))
  (return
   (async-fn handler-fn context
             {:success success-fn
              :error   error-fn})))

(defn.xt pipeline-run-impl
  "runs the pipeline"
  {:added "4.0"}
  [context stages index async-fn hook-fn complete-fn skip-guard]
  (cond (< index (x:offset (k/len stages)))
        (return
         (-/pipeline-call
          context
          (. stages [index])
          false
          async-fn
          (fn [acc tag]
            (when hook-fn
              (hook-fn acc tag))
            (return
             (-/pipeline-run-impl
              context stages (k/inc index) async-fn hook-fn complete-fn skip-guard)))
          skip-guard))
        
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
  (var started (k/now-ms))
  (k/del-key output "elapsed")
  (cond disabled
        (do (-/set-output-disabled view true dest-key)
            (return (-/pipeline-call context
                                     dest-tag
                                     true
                                     async-fn
                                     (fn [acc tag]
                                       (when hook-fn
                                         (hook-fn acc tag))
                                       (when complete-fn
                                         (complete-fn acc))))))
        
        :else
        (do (when (k/get-key output "disabled")
              (-/set-output-disabled view false dest-key))
            (-/set-pending view true dest-key)
            (return
             (-/pipeline-run-impl context ["pre"
                                           dest-tag
                                           "post"]
                                  (x:offset 0)
                                  async-fn
                                  (fn [acc tag]
                                    (when hook-fn
                                      (hook-fn acc tag))
                                    (when (== tag dest-tag)
                                      (-/pipeline-set context tag acc dest-key)))
                                  (fn [acc]
                                    (when complete-fn
                                      (complete-fn acc))
                                    (-/set-elapsed view (- (k/now-ms) started) dest-key)
                                    (-/set-pending view false dest-key)))))))

(defn.xt pipeline-run-force
  "runs the pipeline via sync or remote paths"
  {:added "4.0"}
  [context save-output async-fn hook-fn complete-fn dest-key]
  (var #{acc view} context)
  (var started (k/now-ms))
  (-/set-pending view true dest-key)
  (return
   (-/pipeline-run-impl context ["pre"
                                 dest-key
                                 "post"]
                        (x:offset 0)
                        async-fn
                        (fn [acc tag]
                          (when hook-fn
                            (hook-fn acc tag))
                          (when (== tag dest-key)
                            (-/pipeline-set context tag acc dest-key)
                            (when save-output
                              (-/pipeline-set context tag acc "output"))))
                        (fn [acc]
                          (when complete-fn
                            (complete-fn acc))
                          (-/set-elapsed view (- (k/now-ms) started) dest-key)
                          (-/set-pending view false dest-key)))))
  
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
  (return {:results (:? sort-fn (sort-fn results) results)
           :lookup (k/arr-juxt (or results [])
                               (or key-fn k/id-fn)
                               (or val-fn k/identity))}))

(defn.xt sorted-lookup
  "sorted lookup for region data"
  {:added "0.1"}
  [key]
  (return
   (fn [results]
     (return
      (-/get-with-lookup
       results
       {:sort-fn (fn:> [arr]
                   (k/arr-sort arr
                               (k/key-fn (or key "name"))
                               k/lt))})))))

(defn.xt group-by-lookup
  "creates group-by lookup"
  {:added "0.1"}
  [key]
  (return
   (fn:> [results]
     {:results results
      :lookup (k/arr-group-by results
                              (k/key-fn key)
                              k/identity)})))

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
                          (x:offset 0)
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
                          (x:offset 0)
                          async-fn
                          (fn [acc tag]
                            (when hook-fn
                              (hook-fn acc tag))
                            (when (and (== tag "sync")
                                       save-output)
                              (-/pipeline-set context tag acc "output")))
                          complete-fn
                          {:sync true}))))
