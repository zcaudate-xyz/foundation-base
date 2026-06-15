(ns xt.db.node.adaptor-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.protocol.impl.connection-sql :as dbsql]
             [xt.substrate :as substrate]
             [xt.net.http-fetch :as http-fetch]
             [xt.db.text.sql-call :as call]
             [xt.db.system.impl-common :as impl-common]
             [xt.db.system.main :as impl-main]]})

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

;;
;;
;;


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

;;
;;
;;



(defn.xt ^{:substrate/fn true}
  init-handlers
  [node db-map])




