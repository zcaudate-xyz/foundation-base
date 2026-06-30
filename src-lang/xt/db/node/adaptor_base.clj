(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as http-fetch]
             [xt.db.text.sql-call :as call]
             [xt.db.text.base-tree :as base-tree]
             [xt.db.text.base-flatten :as base-flatten]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.main :as impl-main]
             [xt.substrate.page-core :as page-core]
             [xt.event.base-model :as event-model]]})

;;
;; The xt.db.node.adaptor-base 
;;

(defn.xt init-base-type
  "init-base-type installs a live impl on the node"
  {:added "4.1"}
  [node service-id type defaults schema lookup]
  (return
   (-> (impl-main/create-impl type defaults schema lookup)
       (impl-main/create-impl-init)
       (promise/x:promise-then
        (fn [impl]
          (substrate/set-service node service-id impl)
          (return node))))))

(defn.xt init-base-main
  "init-base-main sets metadata on primary and caching services"
  {:added "4.1"}
  [node config schema lookup]
  (var common  (xt/x:obj-assign {"id" "db/common"}
                                (xt/x:get-key config "common")))
  (var primary  (xt/x:obj-assign {"id" "db/primary"}
                                 (xt/x:get-key config "primary")))
  (var caching  (xt/x:obj-assign {"id" "db/caching"}
                                 (xt/x:get-key config "caching")))

  (var common-id  (xt/x:get-key common "id"))
  (var primary-id (xt/x:get-key primary "id"))
  (var caching-id (xt/x:get-key caching "id"))
  (substrate/set-service node
                         common-id
                         {:config {:common common
                                   :primary primary
                                   :caching caching}
                          :schema schema
                          :lookup lookup})
  (return
   (-> (promise/x:promise-all
        [(-/init-base-type node
                           primary-id
                           (xt/x:get-key primary "type")
                           (xt/x:get-key primary "defaults")
                           schema
                           lookup)
         (-/init-base-type node
                           caching-id
                           (xt/x:get-key caching "type")
                           (xt/x:get-key caching "defaults")
                           schema
                           lookup)])
       (promise/x:promise-then
        (fn [init]
          (-> (substrate/get-service node primary-id)
              (xt/x:get-key "metadata")
              (xt/x:obj-assign {:common-id common-id
                                :caching-id caching-id
                                :caching-fn (fn [] (return (substrate/get-service node caching-id)))}))
          (-> (substrate/get-service node caching-id)
              (xt/x:get-key "metadata")
              (xt/x:obj-assign {:common-id common-id
                                :primary-id primary-id
                                :primary-fn (fn [] (return (substrate/get-service node primary-id)))}))
          (return node))))))

(defn.xt ^{:substrate/fn "@xt.db/init-base"}
  init-base-handler
  "init-base-handler initialises services and returns a summary"
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-/init-base-main node config schema lookup)))

(defn.xt get-primary-impl
  "gets the primary impl"
  {:added "4.1"}
  [node service-id]
  (var impl         (substrate/get-service node service-id))
  (var primary-id   (xtd/get-in impl ["metadata" "primary_id"]))
  (if primary-id
    (return (substrate/get-service node primary-id))
    (return impl)))

(defn.xt get-caching-impl
  "gets the caching impl"
  {:added "4.1"}
  [node service-id]
  (var impl         (substrate/get-service node service-id))
  (var caching-id   (xtd/get-in impl ["metadata" "caching_id"]))
  (if caching-id
    (return (substrate/get-service node caching-id))))

;;
;; REALIME HELPERS
;;

(defn.xt ^{:substrate/fn "@xt.db/subscribe-db"}
  subscribe-db-handler
  "subscribes to the db handler"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first  args))
  (var conn-id      (xt/x:second args))
  (var topics       (xt/x:get-idx args (xt/x:offset 2)))
  (var primary      (-/get-primary-impl node primary-id))
  (return (impl-common/subscribe-db primary conn-id topics)))

(defn.xt ^{:substrate/fn "@xt.db/unsubscribe-db"}
  unsubscribe-db-handler
  "unsubscribes from the db handler"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first  args))
  (var conn-id      (xt/x:second args))
  (var topics       (xt/x:get-idx args (xt/x:offset 2)))
  (var primary      (-/get-primary-impl node primary-id))
  (return (impl-common/unsubscribe-db primary conn-id topics)))

(defn.xt ^{:substrate/fn "@xt.db/sync-caching"}
  sync-caching-handler
  "sync-caching-handler applies db/sync payload to the paired caching db"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first  args))
  (var payload      (xt/x:second args))
  (var caching      (-/get-caching-impl node primary-id))
  (return (impl-common/sync-process-payload caching payload)))


;;
;; BASE MODEL
;;

(defn.xt attach-base-model
  "attach-base-model registers a db listener when options.refresh is set"
  {:added "4.1"}
  [node primary-id space-id group-id model-id model-spec]
  (page-core/add-group-attach node
                              space-id
                              group-id
                              {model-id model-spec})
  (var refresh-map (xtd/get-in model-spec ["options" "refresh"]))
  (var caching (-/get-caching-impl node primary-id)) 
  (when (and caching
             (xt/x:is-object? refresh-map))
    (impl-common/add-db-listener
     caching
     (xt/x:cat space-id "/" group-id "/" model-id)
     {"guard"    refresh-map
      "callback" (fn [event]
                   (return
                    (page-core/model-update node space-id group-id model-id event)))}))
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))

(defn.xt ^{:substrate/fn "@xt.db/attach-model"}
  attach-model-handler
  "TODO"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first args))
  (var page-args    (xt/x:second args))
  (var #{space-id
         group-id
         model-id}   page-args)
  (var model-spec   (xt/x:get-idx args (xt/x:offset 2)))
  (return (-/attach-base-model node primary-id space-id group-id model-id model-spec)))

;;
;; MODEL REMOVAL
;;

(defn.xt detach-base-model
  "TODO"
  {:added "4.1"}
  [node primary-id space-id group-id model-id]
  (page-core/remove-model node space-id group-id model-id)
  (var caching (-/get-caching-impl node primary-id))
  (when (xt/x:not-nil? caching)
    (impl-common/remove-db-listener
     caching
     (xt/x:cat space-id "/" group-id "/" model-id)))
  (return {"status" "removed"
           "space" space-id
           "group" group-id
           "model" model-id}))

(defn.xt ^{:substrate/fn "@xt.db/detach-model"}
  detach-model-handler
  "TODO"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first args))
  (var page-args    (xt/x:second args))
  (var #{space-id
         group-id
         model-id}   page-args)
  (return (-/detach-base-model node primary-id space-id group-id model-id)))


;;
;; RPC HANDLERS
;;

(defn.xt rpc-call-baseline-fn
  "TODO"
  {:added "4.1"}
  [node primary-id rpc-spec rpc-args]
  (var primary    (-/get-primary-impl node primary-id))
  (return
   (-> (impl-common/rpc-call-async primary rpc-spec rpc-args)
       (promise/x:promise-then
        (fn [result]
          (var #{table}   rpc-spec)
          (var caching    (-/get-caching-impl node primary-id))
          (when (and table caching)
            (var #{base type} table)
            (impl-common/sync-process-payload caching {type {base result}}))
          (return result))))))

(defn.xt ^{:substrate/fn "@xt.db/rpc-call"}
  rpc-call-handler
  "rpc-call-handler routes rpc args through a named service"
  {:added "4.1"}
  [space args request node]
  (var service-id   (xt/x:first args))
  (var rpc-spec     (xt/x:second args))
  (var rpc-args     (xt/x:get-idx args (xt/x:offset 2)))
  (return (-/rpc-call-baseline-fn node service-id rpc-spec rpc-args)))

(defn.xt rpc-create-model
  "rpc-create-model builds a page model spec with an rpc handler"
  {:added "4.1"}
  [primary-id rpc-spec model]
  (var #{pipeline
         options
         defaults} model)
  (var rpc-handler
       (fn [context]
         (var #{space args node} context)
         (return (-/rpc-call-baseline-fn node primary-id rpc-spec args))))
  (return
   {"handler" rpc-handler
    "pipeline" pipeline
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/rpc-attach-model"}
  rpc-attach-model
  "rpc-attach-model attaches and invokes an rpc model"
  {:added "4.1"}
  [space args request node]
  (var primary-id  (xt/x:first args))
  (var page-args   (xt/x:second args))
  (var rpc-spec    (xt/x:get-idx args (xt/x:offset 2)))
  (var model       (xt/x:get-idx args (xt/x:offset 3)))
  (var #{space-id
         group-id
         model-id}   page-args)
  (var model-spec (-/rpc-create-model primary-id rpc-spec model))
  (return (-/attach-base-model node primary-id space-id group-id model-id model-spec)))


;;
;; PULL HANDLERS
;;

(defn.xt pull-call-baseline-fn
  "TODO"
  {:added "4.1"}
  [node primary-id tree]
  (var primary (-/get-primary-impl node primary-id))
  (return (-> (impl-common/pull-async primary tree)
              (promise/x:promise-then
               (fn [result]
                 (var table (xt/x:first tree))
                 (var caching (-/get-caching-impl node primary-id))
                 
                 (when caching
                   (var payload {"db/sync" {table result}})
                   (impl-common/sync-process-payload caching payload))
                 (return result))))))

(defn.xt ^{:substrate/fn "@xt.db/pull-call"}
  pull-call-handler
  "TODO"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first args))
  (var tree         (xt/x:second args))  
  (return (-/pull-call-baseline-fn node primary-id tree)))

(defn.xt pull-create-model
  "pull-create-model builds a page model spec with local and remote handlers"
  {:added "4.1"}
  [primary-id tree model]
  (var #{pipeline
         options
         defaults} model)
  (var table (xt/x:first tree))
  (return
   {"handler" (fn [context]
                (var node (. context ["node"]))
                (var caching (-/get-caching-impl node primary-id))
                (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (return (-/pull-call-baseline-fn node primary-id tree)))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/pull-attach-model"}
  pull-attach-model
  "pull-attach-model attaches and invokes a pull-view model"
  {:added "4.1"}
  [space args request node]
  (var primary-id  (xt/x:first args))
  (var page-args   (xt/x:second args))
  (var tree        (xt/x:get-idx args (xt/x:offset 2)))
  (var model       (xt/x:get-idx args (xt/x:offset 3)))
  (var #{space-id
         group-id
         model-id}   page-args)
  (var model-spec (-/pull-create-model primary-id tree model))
  (return (-/attach-base-model node primary-id space-id group-id model-id model-spec)))

;;
;; DATAVIEW MODEL
;;

(defn.xt dataview-call-baseline-fn
  "TODO"
  {:added "4.1"}
  [node primary-id dataview]
  (var impl      (-/get-primary-impl node primary-id))
  (var #{schema} impl)  
  (var [ok tree] (base-tree/plan-view schema
                                      (xt/x:obj-assign
                                       {:select-args []
                                        :return-args []}
                                       dataview)))
  (if (not ok)
    (throw (xt/x:ex "Invalid Dataview" dataview)))
  (return (-> (impl-common/pull-async impl tree)
              (promise/x:promise-then
               (fn [result]
                 (var #{table} dataview)
                 (var caching (-/get-caching-impl node primary-id))
                 (when caching
                   (var payload {"db/sync" {table result}})
                   (impl-common/sync-process-payload caching payload))
                 (return result))))))

(defn.xt ^{:substrate/fn "@xt.db/dataview-call"}
  dataview-call-handler
  "TODO"
  {:added "4.1"}
  [space args request node]
  (var primary-id   (xt/x:first args))
  (var dataview     (xt/x:second args))  
  (return (-/dataview-call-baseline-fn node primary-id dataview)))

(defn.xt dataview-create-model
  "TODO"
  {:added "4.1"}
  [primary-id dataview model]
  (var #{pipeline
         options
         defaults} model)
  (var #{table} dataview)
  (var user-args (xtd/get-in defaults ["args" (xt/x:offset 0)]))
  (return
   {"handler"
    (fn [context]
      (var node (. context ["node"]))
      (var args (. context ["args"]))
      (var caching   (-/get-caching-impl node primary-id))
      (var #{schema} impl)  
      (var [ok tree] (base-tree/plan-view schema
                                          (-> {:select-args []
                                               :return-args []}
                                              (xt/x:obj-assign (xt/x:first args))
                                              (xt/x:obj-assign dataview))))
      (if (not ok)
        (throw (xt/x:ex "Invalid Dataview" dataview)))
      (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (var args (. context ["args"]))
                             (return
                              (-/dataview-call-baseline-fn  node primary-id
                                                            (-> {}
                                                                (xt/x:obj-assign (xt/x:first args))
                                                                (xt/x:obj-assign dataview)))))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/dataview-attach-model"}
  dataview-attach-model
  "TODO"
  {:added "4.1"}
  [space args request node]
  (var service-id  (xt/x:first args))
  (var page-args   (xt/x:second args))
  (var dataview    (xt/x:get-idx args (xt/x:offset 2)))
  (var model       (xt/x:get-idx args (xt/x:offset 3)))
  
  (var node-args   (xt/x:first args))
  (var model-args  (xt/x:second args))
  (var #{space-id
         group-id
         model-id
         service}   node-args)
  (var service-impl (substrate/get-service node service))
  (var model-spec (-/dataview-create-model service-impl model-args))
  (return (-/attach-base-model
           node space-id group-id model-id service-impl model-spec)))



;;
;;
;;

(defn.xt init-handlers
  "TODO"
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.db/init-base" -/init-base-handler nil)
  (substrate/register-handler node "@xt.db/subscribe-db" -/subscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/unsubscribe-db" -/unsubscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/sync-caching" -/sync-caching-handler nil)
  (substrate/register-handler node "@xt.db/attach-model" -/detach-model-handler nil)
  (substrate/register-handler node "@xt.db/detach-model" -/detach-model-handler nil)
  
  (substrate/register-handler node "@xt.db/rpc-call" -/rpc-call-handler nil)
  (substrate/register-handler node "@xt.db/rpc-attach-model" -/rpc-attach-model nil)
  
  (substrate/register-handler node "@xt.db/pull-call" -/pull-call-handler nil)
  (substrate/register-handler node "@xt.db/pull-attach-model" -/pull-attach-model nil)
  (substrate/register-handler node "@xt.db/dataview-call" -/dataview-call-handler nil)
  (substrate/register-handler node "@xt.db/dataview-attach-model" -/dataview-attach-model nil)

  
  
  
  
  
  
  (return node))

(defn list-substrate-fn
  "TODO"
  {:added "4.1"}
  [ns]
  (->> (ns-publics (or ns *ns*))
       (filter (fn [[n v]]
                 (string? (-> v meta :substrate/fn))))
       (sort-by (comp name key))))



(comment
  (substrate/register-handler node "@xt.db/call-fetch" -/call-fetch-handler nil)
  
  (defn.xt ^{:substrate/fn "@xt.db/call-fetch"}
    call-fetch-handler
    [space args request node]
    (var service-id   (xt/x:first  args))
    (var fetch-input  (xt/x:second args))
    (return
     (-> (promise/x:promise-run
          (substrate/get-service node service-id))
         (promise/x:promise-then
          (fn [client]
            (return (http-fetch/request-http client fetch-input))))))))
