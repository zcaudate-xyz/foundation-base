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
  (return
   (-> (promise/x:promise-run node)
       (promise/x:promise-then
        (fn [node]
          (substrate/set-service
           node
           (or (xt/x:get-key common "id")
               "db/common")
           {:schema schema
            :lookup lookup})
          (return node)))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/init-adaptor-type node
                                (or (xt/x:get-key primary "id")
                                    "db/primary")
                                (xt/x:get-key primary "type")
                                (xt/x:get-key primary "defaults")
                                schema
                                lookup))))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/init-adaptor-type node
                                (or (xt/x:get-key caching "id")
                                    "db/caching")
                                (xt/x:get-key caching "type")
                                (xt/x:get-key caching "defaults")
                                schema
                                lookup)))))))

(defn.xt ^{:substrate/fn "@xt.db/init-adaptor"}
  init-adaptor-handler
  "Server-side handler that calls init-adaptor-main with client args."
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-/init-adaptor-main node config schema lookup)))

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
          (return (impl-common/rpc-call-async impl rpc-spec fn-args)))))))

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

(defn.xt
  call-primary-handler
  "Routes rpc args through the live db/primary service."
  {:added "4.1"}
  [space args request node]
  (var rpc-spec (xt/x:first args))
  (var fn-args  (xt/x:second args))
  (return
   (-> (promise/x:promise-run
        (substrate/get-service node "db/primary"))
       (promise/x:promise-then
        (fn [impl]
          (return (impl-common/rpc-call-async impl rpc-spec fn-args)))))))

;;
;;
;;

(defn.xt create-pull-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var #{caching-id
         primary-id} service)
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
  (page-core/add-group-attach
   node
   space-id
   group-id
   {model-id (-/create-pull-model service model-args)})
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))


;;
;; TREE VIEW MODEL
;;

(defn.xt create-tree-view-model
  "Creates a page model spec that plans a view and pulls from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var #{caching-id
         primary-id} service)
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
  (page-core/add-group-attach
   node
   space-id
   group-id
   {model-id (-/create-tree-view-model service model-args)})
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))


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
         (return (impl-common/rpc-call-async impl rpc-spec fn-args))))
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
  (page-core/add-group-attach
   node
   space-id
   group-id
   {model-id (-/create-rpc-model service model-args)})
  (return {"status" "attached"
           "space" space-id
           "group" group-id
           "model" model-id}))


;;
;;
;;

(defn.xt init-handlers
  [node]
  (substrate/register-handler node "@xt.db/attach-pull-model" -/attach-pull-model nil)
  (substrate/register-handler node "@xt.db/attach-rpc-model" -/attach-rpc-model nil)
  (substrate/register-handler node "@xt.db/attach-tree-view-model" -/attach-tree-view-model nil)
  (substrate/register-handler node "@xt.db/call-fetch" -/call-fetch-handler nil)
  (substrate/register-handler node "@xt.db/call-rpc" -/call-rpc-handler nil)
  (substrate/register-handler node "@xt.db/init-adaptor" -/init-adaptor-handler nil)
  (return node))

(defn list-substrate-fn
  [ns]
  (->> (ns-publics (or ns *ns*))
       (filter (fn [[n v]]
                 (string? (-> v meta :substrate/fn))))
       (sort-by (comp name key))))

