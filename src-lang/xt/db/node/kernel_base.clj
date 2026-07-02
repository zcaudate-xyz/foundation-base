(ns xt.db.node.kernel-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-protocol :as protocol]
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
;; HELPERS
;;

(defn.xt kernel-create-config
  [config]
  (var common   (xt/x:obj-assign {"id" "db/common"}
                                 (xt/x:get-key config "common")))
  (var primary  (xt/x:obj-assign {"id" "db/primary"}
                                 (xt/x:get-key config "primary")))
  (var caching  (xt/x:obj-assign {"id" "db/caching"}
                                 (xt/x:get-key config "caching")))
  (return {:common common
           :primary primary
           :caching caching}))

(defn.xt kernel-check-exists
  "checks whether common, primary and caching services are all present"
  {:added "4.1"}
  [node config]
  (:= config (-/kernel-create-config config))
  (return
   (and (xt/x:not-nil? (substrate/get-service node (xtd/get-in config ["common"  "id"])))
        (xt/x:not-nil? (substrate/get-service node (xtd/get-in config ["primary"  "id"])))
        (xt/x:not-nil? (substrate/get-service node (xtd/get-in config ["caching"  "id"]))))))

(defn.xt kernel-setup-single
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

(defn.xt kernel-teardown-single
  "tears down a single base service, disconnecting its client if possible"
  {:added "4.1"}
  [node service-id]
  (var impl (substrate/get-service node service-id))
  (when (protocol/protocol-implements impl "xt.db.system.impl_common/ISourceLifecycle")
    (impl-common/stop-db impl))  
  (substrate/remove-service node service-id)
  (return node))



;;
;; SETUP
;;

(defn.xt kernel-setup-main
  "init-base-main sets metadata on primary and caching services"
  {:added "4.1"}
  [node config schema lookup]
  (:= config (-/kernel-create-config config))
  (var common-id  (xtd/get-in config ["common"  "id"]))
  (var primary-id (xtd/get-in config ["primary" "id"]))
  (var caching-id (xtd/get-in config ["caching" "id"]))
  (substrate/set-service node
                         common-id
                         {:config connfig
                          :schema schema
                          :lookup lookup
                          :metadata {:common-id common-id}})
  (return
   (-> (promise/x:promise-all
        [(-/kernel-setup-single node
                                primary-id
                                (xtd/get-in config ["primary" "type"])
                                (xtd/get-in config ["primary" "defaults"])
                                schema
                                lookup)
         (-/kernel-setup-single node
                                caching-id
                                (xtd/get-in config ["caching" "type"])
                                (xtd/get-in config ["caching" "defaults"])
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
          (return {:status "setup"
                   :data    config}))))))

(defn.xt ^{:substrate/fn "@xt.db/kernel-setup"}
  kernel-setup-handler
  "explicitly sets up base services"
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-/kernel-setup-main node config schema lookup)))



;;
;; TEARDOWN
;;

(defn.xt kernel-teardown-main
  "tears down common, primary and caching services"
  {:added "4.1"}
  [node config]
  (:= config (-/kernel-create-config config))
  
  (-/kernel-teardown-single node (xtd/get-in config ["primary" "id"]))
  (-/kernel-teardown-single node (xtd/get-in config ["caching" "id"]))
  (substrate/remove-service node (xtd/get-in config ["common"  "id"]))
  (return {:status  "teardown"
           :data    config}))

(defn.xt ^{:substrate/fn "@xt.db/kernel-teardown"}
  kernel-teardown-handler
  "tears down base services"
  {:added "4.1"}
  [space args request node]
  (var config (xt/x:first args))
  (when (xt/x:is-string? config)
    (var common-id (-> (substrate/get-service node config)
                       (xtd/get-in ["metadata" "common_id"])))
    (:= config     (-> (substrate/get-service node common-id)
                       (xtd/get-in ["config"]))))
  (return
   (-/kernel-teardown-main node config)))

;;
;; INIT
;;

(defn.xt kernel-init-main
  "init-base-main ensures base services are present"
  {:added "4.1"}
  [node config schema lookup]
  (if (-/kernel-check-exists node config)
    (return {:status "no_change"
             :data    (-/kernel-create-config config)})
    (return (-/kernel-setup-main node config schema lookup))))

(defn.xt ^{:substrate/fn "@xt.db/kernel-init"}
  kernel-init-handler
  "initialises base services if needed and returns the node"
  {:added "4.1"}
  [space args request node]
  (var config  (xt/x:first args))
  (var schema  (xt/x:second args))
  (var lookup  (xt/x:get-idx args (xt/x:offset 2)))
  (return
   (-/kernel-init-main node config schema lookup)))




