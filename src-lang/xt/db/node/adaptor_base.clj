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
  [node service-id type defaults schema lookup]
  (return
   (-> (impl-main/create-impl type defaults schema lookup)
       (impl-main/create-impl-init)
       (promise/x:promise-then
        (fn [impl]
          (substrate/set-service node service-id impl)
          (return node))))))

(defn.xt init-base-main
  [node config schema lookup]
  (var common  (xt/x:obj-assign {"id" "db/common"}
                                (xt/x:get-key config "common")))
  (var primary  (xt/x:obj-assign {"id" "db/primary"}
                                 (xt/x:get-key config "primary")))
  (var caching  (xt/x:obj-assign {"id" "db/caching"}
                                 (xt/x:get-key config "caching")))

  (var common-id (xt/x:get-key common "id"))
  (var primary-id (xt/x:get-key primary "id"))
  (var caching-id (xt/x:get-key caching "id"))
  (substrate/set-service node
                         common-id
                         {:schema schema
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
          (var metadata {:common-id common-id
                         :primary-id primary-id
                         :caching-id caching-id
                         :caching-fn (fn [] (return (substrate/get-service node caching-id)))
                         :primary-fn (fn [] (return (substrate/get-service node primary-id)))})
          (-> (substrate/get-service node primary-id)
              (xt/x:get-key "metadata")
              (xt/x:obj-assign metadata))
          (-> (substrate/get-service node caching-id)
              (xt/x:get-key "metadata")
              (xt/x:obj-assign metadata))
          (return node))))))

(defn.xt ^{:substrate/fn "@xt.db/init-base"}
  init-base-handler
  "Server-side handler that calls init-base-main with client args.

   Returns a serializable summary so the result can be sent over a transport."
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-/init-base-main node config schema lookup)))

;;
;; REALIME HELPERS
;;


(defn.xt ^{:substrate/fn "@xt.db/subscribe-db"}
  subscribe-db-handler
  "Substrate handler for @xt.db/subscribe-db.

   Args: [conn-id topics]. Subscribes the primary db realtime client to the
   given broadcast topics, wiring received db/sync and db/remove events to the
   paired caching db."
  {:added "4.1"}
  [space args request node]
  (var service-id   (xt/x:first  args))
  (var conn-id      (xt/x:second args))
  (var topics       (xt/x:get-idx args (xt/x:offset 2)))
  (var service      (substrate/get-service node service-id))
  (return (impl-common/subscribe-db service conn-id topics)))

(defn.xt ^{:substrate/fn "@xt.db/unsubscribe-db"}
  unsubscribe-db-handler
  "Substrate handler for @xt.db/unsubscribe-db.

   Args: [conn-id topics]. Unsubscribes the primary db realtime client from
   the given broadcast topics."
  {:added "4.1"}
  [space args request node]
  (var service-id   (xt/x:first  args))
  (var conn-id      (xt/x:second args))
  (var topics       (xt/x:get-idx args (xt/x:offset 2)))
  (var service      (substrate/get-service node service-id))
  (return (impl-common/unsubscribe-db service conn-id topics)))



(defn.xt ^{:substrate/fn "@xt.db/sync-event"}
  sync-event-handler
  "Substrate handler for @xt.db/sync-event.

   Args: [payload]. Applies the db/sync and db/remove payload to the paired
   caching db and notifies any registered db listeners."
  {:added "4.1"}
  [space args request node]
  (var service-id   (xt/x:first  args))
  (var payload      (xt/x:second args))
  (var service      (substrate/get-service node service-id))
  (return (promise/x:promise-run
           (impl-common/sync-process-payload service payload))))


;;
;; CACHING HELPERS
;;

(defn.xt caching-sync-output
  "If `result` contains db/sync or db/remove, applies it to the caching
   DB paired with `service` and returns `result`."
  {:added "4.1"}
  [node service result]
  (when (and (xt/x:is-object? result)
             (or (xt/x:has-key? result "db/sync")
                 (xt/x:has-key? result "db/remove")))
    (var metadata   (xt/x:get-key service "metadata"))
    (var caching-id (xt/x:get-key metadata "caching_id"))

    (when (xt/x:not-nil? caching-id)
      (var caching (substrate/get-service node caching-id))
      (when (xt/x:not-nil? caching)
        (impl-common/sync-process-payload caching result))))
  (return (promise/x:promise-run result)))


;;
;; BASE MODEL
;;

(defn.xt attach-base-model
  "Attaches a page model and registers a db listener if options.refresh is set."
  {:added "4.1"}
  [node service space-id group-id model-id model-spec]
  (page-core/add-group-attach
   node
   space-id
   group-id
   {model-id model-spec})
  (var refresh-map (xtd/get-in model-spec ["options" "refresh"]))
  (when (xt/x:is-object? refresh-map)
    (var #{metadata} service)
    (var #{caching-id}  metadata)
    (var caching (substrate/get-service node caching-id))
    (var listener-id (xt/x:cat space-id "/" group-id "/" model-id))
    (impl-common/add-db-listener
     caching
     listener-id
     {"guard"    refresh-map
      "callback" (fn [event]
                   (return
                    (page-core/model-update node space-id group-id model-id event)))}))
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))


;;
;; RPC HANDLER
;;

(defn.xt call-rpc-baseline-fn
  [node service-id rpc-spec rpc-args]
  (var impl         (substrate/get-service node service-id))
  (var #{metadata} impl)
  (var #{caching-id} metadata)
  (var table-spec   (xt/x:get-key rpc-spec "table"))
  (return
   (-> (impl-common/rpc-call-async impl rpc-spec rpc-args)
       (promise/x:promise-then
        (fn [result]
          (when (and table-spec
                     caching-id)
            (var #{base type} table-spec)
            (var caching (substrate/get-service node caching-id))
            (impl-common/sync-process-payload caching {type {base result}}))
          (return result))))))

(defn.xt ^{:substrate/fn "@xt.db/call-rpc"}
  call-rpc-handler
  [space args request node]
  (var service-id   (xt/x:first args))
  (var rpc-spec     (xt/x:second args))
  (var rpc-args      (xt/x:get-idx args (xt/x:offset 2)))
  (return (-/call-rpc-baseline-fn node service-id rpc-spec rpc-args)))

;;
;; RPC MODEL
;;

(defn.xt create-rpc-model
  "Creates a page model spec that calls an RPC on a named service."
  {:added "4.1"}
  [service-id rpc-spec model]
  (var #{pipeline
         options
         defaults} model)
  (var rpc-handler
       (fn [context]
         (var #{space args node} context)
         (return (-/call-rpc-baseline-fn node service-id rpc-spec args))))
  (return
   {"handler" rpc-handler
    "pipeline" pipeline
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/attach-rpc-model"}
  attach-rpc-model
  "Server-side handler that materialises an RPC page model from client args."
  {:added "4.1"}
  [space args request node]
  (var service-id  (xt/x:first args))
  (var page-args   (xt/x:second args))
  (var rpc-spec    (xt/x:get-idx args (xt/x:offset 2)))
  (var model       (xt/x:get-idx args (xt/x:offset 3)))
  (var #{space-id
         group-id
         model-id}   page-args)
  (var model-spec (-/create-rpc-model service-id rpc-spec model))
  (var service (substrate/get-service node service-id))
  (return (-/attach-base-model node service space-id group-id model-id model-spec)))

;;
;;
;;

(defn.xt create-pull-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [metadata tree model]
  (var #{caching-id
         primary-id} metadata)
  (var #{pipeline
         options
         defaults} model)
  (var table (xt/x:first tree))
  (return
   {"handler" (fn [context]
                (var node (. context ["node"]))
                (var caching (substrate/get-service node caching-id))
                (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (var primary (substrate/get-service node primary-id))
                             (var caching (substrate/get-service node caching-id))
                             (return (-> (impl-common/pull-async primary tree)
                                         (promise/x:promise-then
                                          (fn [result]
                                            (var rows (:? (xt/x:is-array? result)
                                                          result
                                                          [result]))
                                            (var payload {"db/sync" {table rows}})
                                            (impl-common/sync-process-payload caching payload)
                                            (return result))))))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/attach-pull-model"}
  attach-pull-model
  "Server-side handler that materialises a custom pull-view page model from client args."
  {:added "4.1"}
  [space args request node]
  (var service-id  (xt/x:first args))
  (var page-args   (xt/x:second args))
  (var tree        (xt/x:get-idx args (xt/x:offset 2)))
  (var model       (xt/x:get-idx args (xt/x:offset 3)))
  (var #{space-id
         group-id
         model-id}   page-args)
  (var service (substrate/get-service node service-id))
  (var #{metadata} service)
  (var model-spec (-/create-pull-model metadata tree model))
  (return (-/attach-base-model node service space-id group-id model-id model-spec)))








;;
;; TREE VIEW MODEL
;;

(defn.xt create-tree-view-model
  "Creates a page model spec that plans a view and pulls from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var metadata (xt/x:get-key service "metadata"))
  (var caching-id (xt/x:get-key metadata "caching_id"))
  (var primary-id (xt/x:get-key metadata "primary_id"))
  (var #{table
         select-entry
         return-entry
         return-omit
         return-count
         return-id
         return-bulk
         pipeline
         options
         defaults} model)
  (var default-select-args (or (xt/x:get-key defaults "select_args") []))
  (var default-return-args (or (xt/x:get-key defaults "return_args") []))
  (var plan-fn
       (fn [impl select-args return-args]
         (var schema (xt/x:get-key impl "schema"))
         (var [ok tree] (base-tree/plan-view
                         schema
                         {:table table
                          :select-entry select-entry
                          :select-args select-args
                          :return-entry return-entry
                          :return-args return-args
                          :return-omit return-omit
                          :return-count return-count
                          :return-id return-id
                          :return-bulk return-bulk}))
         (when (not ok)
           (return [ok tree]))
         (return [true tree])))
  (return
   {"handler"
    (fn [context]
      (var node (. context ["node"]))
      (var args (. context ["args"]))
      (var select-args (or (xt/x:get-idx args 0) default-select-args))
      (var return-args (or (xt/x:get-idx args 1) default-return-args))
      (var caching (substrate/get-service node caching-id))
      (var [ok tree] (plan-fn caching select-args return-args))
      (when (not ok)
        (return [ok tree]))
      (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (var args (. context ["args"]))
                             (var select-args (or (xt/x:get-idx args 0) default-select-args))
                             (var return-args (or (xt/x:get-idx args 1) default-return-args))
                             (var primary   (substrate/get-service node primary-id))
                             (var [ok tree] (plan-fn primary select-args return-args))
                             (when (not ok)
                               (return [ok tree]))
                             (return (impl-common/pull-async primary tree)))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/attach-tree-view-model"}
  attach-tree-view-model
  "Server-side handler that materialises a tree-view page model from client args."
  {:added "4.1"}
  [space args request node]
  (var node-args   (xt/x:first args))
  (var model-args  (xt/x:second args))
  (var #{space-id
         group-id
         model-id
         service}   node-args)
  (var service-impl (substrate/get-service node service))
  (var model-spec (-/create-tree-view-model service-impl model-args))
  (return (-/attach-base-model
           node space-id group-id model-id service-impl model-spec)))



;;
;; MODEL REMOVAL
;;

(defn.xt remove-model-with-refresh
  "Removes a page model and its caching db listener."
  {:added "4.1"}
  [node space-id group-id model-id service]
  (page-core/remove-model node space-id group-id model-id)
  (var metadata (xt/x:get-key service "metadata"))
  (var caching-id (xt/x:get-key metadata "caching_id"))
  (when (xt/x:not-nil? caching-id)
    (var caching (substrate/get-service node caching-id))
    (when (xt/x:not-nil? caching)
      (impl-common/remove-db-listener
       caching
       (xt/x:cat space-id "/" group-id "/" model-id))))
  (return {"status" "removed"
           "space" space-id
           "group" group-id
           "model" model-id}))

(defn.xt ^{:substrate/fn "@xt.db/detach-db-model"}
  detach-db-model
  "Server-side handler that removes a page model and its caching db listener.

   Works for pull, tree-view and RPC models. The `service` field may be either a
   service id string or the service impl map."
  {:added "4.1"}
  [space args request node]
  (var node-args   (xt/x:first args))
  (var #{space-id
         group-id
         model-id
         service}   node-args)
  (var service-impl (substrate/get-service node service))
  (return (-/remove-model-with-refresh
           node space-id group-id model-id service-impl)))


;;
;;
;;

(defn.xt init-handlers
  [node]
  (substrate/register-handler node "@xt.db/attach-pull-model" -/attach-pull-model nil)
  (substrate/register-handler node "@xt.db/attach-rpc-model" -/attach-rpc-model nil)
  (substrate/register-handler node "@xt.db/attach-tree-view-model" -/attach-tree-view-model nil)
  (substrate/register-handler node "@xt.db/detach-db-model" -/detach-db-model nil)
  
  (substrate/register-handler node "@xt.db/call-rpc" -/call-rpc-handler nil)
  (substrate/register-handler node "@xt.db/sync-event" -/sync-event-handler nil)
  (substrate/register-handler node "@xt.db/subscribe-db" -/subscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/unsubscribe-db" -/unsubscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/init-base" -/init-base-handler nil)
  (return node))

(defn list-substrate-fn
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
