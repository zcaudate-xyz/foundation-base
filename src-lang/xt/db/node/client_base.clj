(ns xt.db.node.client-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.db.node.proxy-util :as proxy-util]]})

;;
;; Client-side API for the base db actions registered by
;; xt.db.node.kernel-base (server-side) and xt.db.node.proxy-base
;; (client-side proxy).
;;
;; All functions below issue substrate/request calls, so the same code works
;; against a node that has the base adaptor handlers installed locally or the
;; base proxy handlers installed and forwarding to a remote transport.
;;

(defn.xt kernel-init
  "initialises base db services on the node"
  {:added "4.1"}
  [node config schema lookup opts]
  (return
   (proxy-util/request-client node "@xt.db/kernel-init" [config schema lookup] opts)))

(defn.xt kernel-setup
  "initialises base db services on the node"
  {:added "4.1"}
  [node config schema lookup opts]
  (return
   (proxy-util/request-client node "@xt.db/kernel-setup" [config schema lookup] opts)))

(defn.xt kernel-teardown
  "initialises base db services on the node"
  {:added "4.1"}
  [node config opts]
  (return
   (proxy-util/request-client node "@xt.db/kernel-teardown" [config] opts)))

(defn.xt subscribe-db
  "subscribes to db topics on the requested service"
  {:added "4.1"}
  [node primary-id conn-id topics opts]
  (return
   (proxy-util/request-client node "@xt.db/subscribe-db" [primary-id conn-id topics] opts)))

(defn.xt unsubscribe-db
  "unsubscribes from db topics on the requested service"
  {:added "4.1"}
  [node primary-id conn-id topics opts]
  (return
   (proxy-util/request-client node "@xt.db/unsubscribe-db" [primary-id conn-id topics] opts)))

(defn.xt sync-cached
  "applies a db/sync payload to the paired caching db"
  {:added "4.1"}
  [node primary-id payload opts]
  (return
   (proxy-util/request-client node "@xt.db/sync-cached" [primary-id payload] opts)))

(defn.xt attach-model
  "attaches a page model to the node"
  {:added "4.1"}
  [node primary-id page-args model-spec opts]
  (return
   (proxy-util/request-client node "@xt.db/attach-model" [primary-id page-args model-spec] opts)))

(defn.xt detach-model
  "detaches a page model from the node"
  {:added "4.1"}
  [node primary-id page-args opts]
  (return
   (proxy-util/request-client node "@xt.db/detach-model" [primary-id page-args] opts)))

(defn.xt rpc-call
  "calls an rpc entry on the requested service"
  {:added "4.1"}
  [node service-id rpc-spec rpc-args opts]
  (return
   (proxy-util/request-client node "@xt.db/rpc-call" [service-id rpc-spec rpc-args] opts)))

(defn.xt rpc-attach-model
  "attaches and invokes an rpc model"
  {:added "4.1"}
  [node primary-id page-args rpc-spec model opts]
  (return
   (proxy-util/request-client node "@xt.db/rpc-attach-model" [primary-id page-args rpc-spec model] opts)))

(defn.xt pull-call
  "pulls data and syncs the result to caching"
  {:added "4.1"}
  [node primary-id tree opts]
  (return
   (proxy-util/request-client node "@xt.db/pull-call" [primary-id tree] opts)))

(defn.xt pull-cached
  "pulls data and syncs the result to caching"
  {:added "4.1"}
  [node primary-id tree opts]
  (return
   (proxy-util/request-client node "@xt.db/pull-cached" [primary-id tree] opts)))

(defn.xt pull-attach-model
  "attaches and invokes a pull-view model"
  {:added "4.1"}
  [node primary-id page-args tree model opts]
  (return
   (proxy-util/request-client node "@xt.db/pull-attach-model" [primary-id page-args tree model] opts)))

(defn.xt dataview-call
  "executes a dataview query and syncs to caching"
  {:added "4.1"}
  [node primary-id dataview opts]
  (return
   (proxy-util/request-client node "@xt.db/dataview-call" [primary-id dataview] opts)))

(defn.xt dataview-cached
  "executes a dataview query and syncs to caching"
  {:added "4.1"}
  [node primary-id dataview opts]
  (return
   (proxy-util/request-client node "@xt.db/dataview-cached" [primary-id dataview] opts)))

(defn.xt dataview-attach-model
  "attaches and invokes a dataview model"
  {:added "4.1"}
  [node primary-id page-args dataview model opts]
  (return
   (proxy-util/request-client node "@xt.db/dataview-attach-model" [primary-id page-args dataview model] opts)))
