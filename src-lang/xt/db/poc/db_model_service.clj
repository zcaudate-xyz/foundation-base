(ns xt.db.poc.db-model-service
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as page-core]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.node.adaptor-base :as adaptor-base]]})

(defn.js init-services
  "Initialises db/primary (supabase) and db/caching (sql) services on a node.

   Config shape:
   {:primary   {:type    \"supabase\"
                :defaults {...}}
    :caching   {:type    \"sqlite\" | \"postgres\"
                :defaults {...}}}"
  {:added "4.1"
   :substrate/fn true}
  [node config schema lookup]
  (return (adaptor-base/init-db node config schema lookup)))

(defn.js create-page-model
  "Creates a page model spec that reads from db/caching (sync) and db/primary (async).

   The model's main handler pulls from the local caching service, while the
   remote pipeline handler pulls asynchronously from the supabase primary service.

   Args from context are expected to be [tree], where tree is the pull tree IR."
  {:added "4.1"}
  [model-id tree opts]
  (:= opts (or opts {}))
  (return {"handler" (fn [context]
                       (var node (. context ["node"]))
                       (var args (. context ["args"]))
                       (var pull-tree (xt/x:first args))
                       (var caching (substrate/get-service node "db/caching"))
                       (return (impl-common/pull caching pull-tree)))
           "pipeline" {"remote" {"handler" (fn [context]
                                             (var node (. context ["node"]))
                                             (var args (. context ["args"]))
                                             (var pull-tree (xt/x:first args))
                                             (var primary (substrate/get-service node "db/primary"))
                                             (return (impl-common/pull-async primary pull-tree)))}}
           "defaults" {"args" [tree]}
           "options" opts}))

(defn.js install-page-models
  "Attaches a group of db-model-service page models to a node space."
  {:added "4.1"
   :substrate/fn true}
  [node space-id group-id models]
  (var model-map {})
  (xt/for:object [[model-id model-spec] models]
    (xt/x:set-key model-map model-id model-spec))
  (return (page-core/add-group-attach node space-id group-id model-map)))
