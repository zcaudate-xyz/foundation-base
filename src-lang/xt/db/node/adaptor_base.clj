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
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.main :as impl-main]
             [xt.substrate.page-core :as page-core]
             [xt.event.base-model :as event-model]]})


;;
;; The xt.db.node.adaptor-base 
;;

(defn.xt init-adaptor-type
  [node service-id type defaults schema lookup]
  (return
   (-> (impl-main/create-impl type defaults schema lookup)
       (impl-main/create-impl-init)
       (promise/x:promise-then
        (fn [impl]
          (substrate/set-service node service-id impl)
          (return node))))))

(defn.xt init-adaptor-main
  [node config schema lookup]
  (var primary  (or (xt/x:get-key config "primary") {}))
  (var caching  (or (xt/x:get-key config "caching") {}))
  (var common   (or (xt/x:get-key config "common") {}))
  (var primary-id (or (xt/x:get-key primary "id") "db/primary"))
  (var caching-id (or (xt/x:get-key caching "id") "db/caching"))
  (var common-id  (or (xt/x:get-key common "id")  "db/common"))
  (return
   (-> (promise/x:promise-run node)
       (promise/x:promise-then
        (fn [node]
          (substrate/set-service
           node
           common-id
           {:schema schema
            :lookup lookup})
          (return node)))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/init-adaptor-type node
                                primary-id
                                (xt/x:get-key primary "type")
                                (xt/x:get-key primary "defaults")
                                schema
                                lookup))))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/init-adaptor-type node
                                caching-id
                                (xt/x:get-key caching "type")
                                (xt/x:get-key caching "defaults")
                                schema
                                lookup))))
       (promise/x:promise-then
        (fn [node]
          (var primary-impl (substrate/get-service node primary-id))
          (var caching-impl (substrate/get-service node caching-id))
          (var primary-metadata (xt/x:get-key primary-impl "metadata"))
          (var caching-metadata (xt/x:get-key caching-impl "metadata"))
          (xt/x:set-key primary-metadata "common_id"  common-id)
          (xt/x:set-key primary-metadata "primary_id" primary-id)
          (xt/x:set-key primary-metadata "caching_id" caching-id)
          (xtd/set-in primary-impl ["state" "caching_fn"]
                      (fn [] (return caching-impl)))
          (xt/x:set-key caching-metadata "common_id"  common-id)
          (xt/x:set-key caching-metadata "primary_id" primary-id)
          (xt/x:set-key caching-metadata "caching_id" caching-id)
          (return node))))))

(defn.xt ^{:substrate/fn "@xt.db/init-adaptor"}
  init-adaptor-handler
  "Server-side handler that calls init-adaptor-main with client args.

   Returns a serializable summary so the result can be sent over a transport."
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-> (-/init-adaptor-main node config schema lookup)
       (promise/x:promise-then
        (fn [node]
          (var primary (substrate/get-service node "db/primary"))
          (var caching (substrate/get-service node "db/caching"))
          (var common  (substrate/get-service node "db/common"))
          (return
           {"status" "ok"
            "services"
            {"db/primary" {"metadata" (or (xt/x:get-key primary "metadata") {})}
             "db/caching" {"metadata" (or (xt/x:get-key caching "metadata") {})}
             "db/common"  {"metadata" (or (xt/x:get-key common "metadata") {})}}}))))))

;;
;;
;;

(defn.xt apply-sync-output
  "If `result` contains db/sync or db/remove, applies it to the caching
   DB paired with `source-impl` and returns `result`."
  {:added "4.1"}
  [node source-impl result]
  (when (and (xt/x:is-object? result)
             (or (xt/x:has-key? result "db/sync")
                 (xt/x:has-key? result "db/remove")))
    (var metadata   (xt/x:get-key source-impl "metadata"))
    (var caching-id (xt/x:get-key metadata "caching_id"))
    (when (xt/x:not-nil? caching-id)
      (var caching (substrate/get-service node caching-id))
      (when (xt/x:not-nil? caching)
        (impl-common/sync-process-payload caching result))))
  (return (promise/x:promise-run result)))

;;
;;
;;

(defn.xt ^{:substrate/fn "@xt.db/call-rpc"}
  call-rpc-handler
  [space args request node]
  (var service-id   (xt/x:first args))
  (var rpc-spec     (xt/x:second args))
  (var fn-args      (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-> (promise/x:promise-run
        (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [impl]
          (return (impl-common/rpc-call-async impl rpc-spec fn-args))))
       (promise/x:promise-then
        (fn [result]
          (return (-/apply-sync-output node
                                       (substrate/get-service node service-id)
                                       result)))))))

(defn.xt ^{:substrate/fn "@xt.db/sync-event"}
  sync-event-handler
  "Substrate handler for @xt.db/sync-event.

   Args: [payload]. Applies the db/sync and db/remove payload to the paired
   caching db and notifies any registered db listeners."
  {:added "4.1"}
  [space args request node]
  (var payload (or (xt/x:first args) {}))
  (var primary (substrate/get-service node "db/primary"))
  (var caching-id (xtd/get-in primary ["metadata" "caching_id"]))
  (var caching (or (and caching-id (substrate/get-service node caching-id))
                   (substrate/get-service node "db/caching")))
  (when (xt/x:nil? caching)
    (return (promise/x:promise-run
             [false {"status" "error"
                     "tag" "db/caching-not-found"}])))
  (return (promise/x:promise-run
           (impl-common/sync-process-payload caching payload))))

(defn.xt ^{:substrate/fn "@xt.db/subscribe-db"}
  subscribe-db-handler
  "Substrate handler for @xt.db/subscribe-db.

   Args: [conn-id topics]. Subscribes the primary db realtime client to the
   given broadcast topics, wiring received db/sync and db/remove events to the
   paired caching db."
  {:added "4.1"}
  [space args request node]
  (var conn-id (xt/x:first args))
  (var topics  (xt/x:second args))
  (var primary (substrate/get-service node "db/primary"))
  (return (impl-common/subscribe-db primary conn-id topics)))

(defn.xt ^{:substrate/fn "@xt.db/unsubscribe-db"}
  unsubscribe-db-handler
  "Substrate handler for @xt.db/unsubscribe-db.

   Args: [conn-id topics]. Unsubscribes the primary db realtime client from
   the given broadcast topics."
  {:added "4.1"}
  [space args request node]
  (var conn-id (xt/x:first args))
  (var topics  (xt/x:second args))
  (var primary (substrate/get-service node "db/primary"))
  (return (impl-common/unsubscribe-db primary conn-id topics)))

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
          (return (http-fetch/request-http client fetch-input)))))))

;;
;;
;;

(defn.xt create-pull-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var service-metadata (xt/x:get-key service "metadata"))
  (var caching-id (xt/x:get-key service-metadata "caching_id"))
  (var primary-id (xt/x:get-key service-metadata "primary_id"))
  (var #{pipeline
         options
         defaults} model)
  (return
   {"handler"
    (fn [context]
      (var node (. context ["node"]))
      (var tree (. context ["args"] [0]))
      (var caching (substrate/get-service node caching-id))
      (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (var tree (. context ["args"] [0]))
                             (var primary (substrate/get-service node primary-id))
                             (return (impl-common/pull-async primary tree)))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt add-db-model-listener
  "Attaches a page model and registers a db listener if options.refresh is set."
  {:added "4.1"}
  [node space-id group-id model-id service model-spec]
  (page-core/add-group-attach
   node
   space-id
   group-id
   {model-id model-spec})
  (var refresh-map (xt/x:get-key (xt/x:get-key model-spec "options") "refresh"))
  (when (xt/x:is-object? refresh-map)
    (var service-metadata (xt/x:get-key service "metadata"))
    (var caching-id (xt/x:get-key service-metadata "caching_id"))
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

(defn.xt ^{:substrate/fn "@xt.db/attach-pull-model"}
  attach-pull-model
  "Server-side handler that materialises a custom pull-view page model from client args."
  {:added "4.1"}
  [space args request node]
  (var node-args   (xt/x:first args))
  (var model-args  (xt/x:second args))
  (var #{space-id
         group-id
         model-id
         service}   node-args)
  (var model-spec (-/create-pull-model service model-args))
  (return (-/add-db-model-listener
           node space-id group-id model-id service model-spec)))


;;
;; TREE VIEW MODEL
;;

(defn.xt create-tree-view-model
  "Creates a page model spec that plans a view and pulls from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var service-metadata (xt/x:get-key service "metadata"))
  (var caching-id (xt/x:get-key service-metadata "caching_id"))
  (var primary-id (xt/x:get-key service-metadata "primary_id"))
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
  (var model-spec (-/create-tree-view-model service model-args))
  (return (-/add-db-model-listener
           node space-id group-id model-id service model-spec)))


;;
;; RPC MODEL
;;

(defn.xt create-rpc-model
  "Creates a page model spec that calls an RPC on a named service."
  {:added "4.1"}
  [service-id model]
  (var #{rpc-spec
         pipeline
         options
         defaults} model)
  (var default-fn-args (or (xt/x:get-key defaults "fn_args") []))
  (var rpc-handler
       (fn [context]
         (var node (. context ["node"]))
         (var args (. context ["args"]))
         (var fn-args (or (xt/x:get-idx args 0) default-fn-args))
         (var impl (substrate/get-service node service-id))
         (return
          (-> (impl-common/rpc-call-async impl rpc-spec fn-args)
              (promise/x:promise-then
               (fn [result]
                 (return (-/apply-sync-output node impl result))))))))
  (return
   {"handler" rpc-handler
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler" rpc-handler}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn "@xt.db/attach-rpc-model"}
  attach-rpc-model
  "Server-side handler that materialises an RPC page model from client args."
  {:added "4.1"}
  [space args request node]
  (var node-args   (xt/x:first args))
  (var model-args  (xt/x:second args))
  (var #{space-id
         group-id
         model-id
         service}   node-args)
  (var service-impl (substrate/get-service node service))
  (var model-spec (-/create-rpc-model service model-args))
  (return (-/add-db-model-listener
           node space-id group-id model-id service-impl model-spec)))


;;
;; MODEL REMOVAL
;;

(defn.xt remove-model-with-refresh
  "Removes a page model and its caching db listener."
  {:added "4.1"}
  [node space-id group-id model-id service]
  (page-core/remove-model node space-id group-id model-id)
  (var service-metadata (xt/x:get-key service "metadata"))
  (var caching-id (xt/x:get-key service-metadata "caching_id"))
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
  (var service-impl service)
  (when (xt/x:is-string? service)
    (:= service-impl (substrate/get-service node service)))
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
  (substrate/register-handler node "@xt.db/call-fetch" -/call-fetch-handler nil)
  (substrate/register-handler node "@xt.db/call-rpc" -/call-rpc-handler nil)
  (substrate/register-handler node "@xt.db/sync-event" -/sync-event-handler nil)
  (substrate/register-handler node "@xt.db/subscribe-db" -/subscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/unsubscribe-db" -/unsubscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/init-adaptor" -/init-adaptor-handler nil)
  (return node))

(defn list-substrate-fn
  [ns]
  (->> (ns-publics (or ns *ns*))
       (filter (fn [[n v]]
                 (string? (-> v meta :substrate/fn))))
       (sort-by (comp name key))))

