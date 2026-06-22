(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as http-fetch]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.main :as impl-main]
             [xt.substrate.page-core :as page-core]
             [xt.event.base-model :as event-model]]})

;;
;; The xt.db.node.adaptor-base 
;;

(defn.xt ^{:substrate/fn true}
  set-impl
  [node service-id type defaults schema lookup]
  (return
   (-> (impl-main/create-impl type defaults schema lookup)
       (impl-main/create-impl-init)
       (promise/x:promise-then
        (fn [impl]
          (substrate/set-service node service-id impl)
          (return node))))))

(defn.xt ^{:substrate/fn true}
  init-db
  [node config schema lookup]
  (var #{primary
         caching} config)
  (return
   (-> (promise/x:promise-run node)
       (promise/x:promise-then
        (fn [node]
          (substrate/set-service
           node
           "db/common"
           {:schema schema
            :lookup lookup})
          (return node)))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/set-impl node
                       "db/primary"
                       (xt/x:get-key primary "type")
                       (xt/x:get-key primary "defaults")
                       schema
                       lookup))))
       (promise/x:promise-then
        (fn [node]
          (return
           (-/set-impl node
                       "db/caching"
                       (xt/x:get-key caching "type")
                       (xt/x:get-key caching "defaults")
                       schema
                       lookup)))))))


;;
;;
;;


(defn.xt call-primary-handler
  [space args request node]
  (var rpc-spec   (xt/x:first args))
  (var fn-args    (xt/x:second args))
  (return
   (-> (promise/x:promise-run
        (substrate/get-service node "db/primary"))
       (promise/x:promise-then
        (fn [impl]
          (return (impl-common/rpc-call-async impl rpc-spec fn-args)))))))

(defn.xt call-rpc-handler
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

(defn.xt call-fetch-handler
  [space args request node]
  (var service-id   (xt/x:first  args))
  (var fetch-input  (xt/x:second args))
  (return
   (-> (promise/x:promise-run
        (substrate/get-service node service-id))
       (promise/x:promise-then
        (fn [client]
          (return (http-fetch/request-http client fetch-input)))))))

(defn.xt create-pull-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async)."
  {:added "4.1"}
  [service model]
  (var #{local-id
         remote-id} service)
  (var #{pipeline
         options
         defaults} model)
  (return
   {"handler"
    (fn [context]
      (var node (. context ["node"]))
      (var tree (. context ["args"] [0]))
      (var caching (substrate/get-service node local-id))
      (return (impl-common/pull caching tree)))
    "pipeline" (xtd/obj-assign-nested
                {"remote" {"handler"
                           (fn [context]
                             (var node (. context ["node"]))
                             (var tree (. context ["args"] [0]))
                             (var primary (substrate/get-service node remote-id))
                             (return (impl-common/pull-async primary tree)))}}
                pipeline)
    "defaults" defaults
    "options"  options}))

(defn.xt ^{:substrate/fn true}
  custom-pull-view
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
;;
;;

(defn.xt ^{:substrate/fn true}
  init-handlers
  [node db-map])


(comment
  (comment
  {:model {:pipeline ...
           :defaults ...}
   :page  {}}

  (var pipeline (xt/x:get-key opts "pipeline"))
  (var defaults (xt/x:get-key opts "defaults"))
  (var trigger  (xt/x:get-key opts "trigger"))
  (var options  (xt/x:get-key opts "options")))

  )
