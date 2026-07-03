(ns xt.db.node.proxy-base
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.proxy-util :as proxy-util]]})

;;
;; Client-side proxy handlers for the server-side base adaptor handlers in
;; xt.db.node.kernel-base.
;;
;; These mirror the server substrate handler ids (e.g. @xt.db/init-base)
;; so that the same function ids can be invoked on a client node and forwarded
;; to the server over the configured transport.
;;
;; Substrate routing will always prefer an attached transport when no
;; transport_id is given, so proxy handlers must supply transport_id
;; explicitly from the request meta.
;;

(def.xt call-actions
  ["@xt.db/kernel-init"
   "@xt.db/kernel-setup"
   "@xt.db/kernel-teardown"
   "@xt.db/subscribe-db"
   "@xt.db/unsubscribe-db"
   "@xt.db/sync-cached"
   "@xt.db/rpc-call"
   "@xt.db/pull-call"
   "@xt.db/pull-cached"
   "@xt.db/dataview-call"
   "@xt.db/dataview-cached"])

(def.xt attach-actions
  ["@xt.db/attach-model"
   "@xt.db/rpc-attach-model"
   "@xt.db/pull-attach-model"
   "@xt.db/dataview-attach-model"])

(def.xt detach-actions
  ["@xt.db/detach-model"])

(defn.xt request-proxy
  "generic client proxy for @xt.db/* actions"
  {:added "4.1"}
  [space args request node]
  (return (proxy-util/request-proxy space args request node)))

(defn.xt attach-forward-handler
  "forwards an attach action to the server and opens the proxy group"
  {:added "4.1"}
  [space args request node]
  (var page-args (xt/x:second args))
  (var space-id (xtd/get-in page-args ["space_id"]))
  (var group-id (xtd/get-in page-args ["group_id"]))
  (var transport-id (proxy-util/get-transport-id node (xtd/get-in request ["meta"])))
  (page-proxy/create-proxy-group node space-id group-id {} {"transport_id" transport-id})
  (return
   (-> (page-proxy/open-proxy-group node space-id group-id {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [_out]
          (return
           (substrate/request node
                              nil
                              (xt/x:get-key request "action")
                              args
                              {"transport_id" transport-id})))))))

(defn.xt detach-forward-handler
  "forwards a detach action to the server and closes the proxy group"
  {:added "4.1"}
  [space args request node]
  (var page-args (xt/x:second args))
  (var space-id (xtd/get-in page-args ["space_id"]))
  (var group-id (xtd/get-in page-args ["group_id"]))
  (var transport-id (proxy-util/get-transport-id node (xtd/get-in request ["meta"])))
  (return
   (-> (page-proxy/close-proxy-group node space-id group-id {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [_out]
          (return
           (substrate/request node
                           nil
                           (xt/x:get-key request "action")
                           args
                           {"transport_id" transport-id})))))))

(defn.xt init-proxy-handlers
  "Registers client-side proxy handlers for xt.db.node.kernel-base methods so
   that the same substrate function ids can be invoked on a client node and
   forwarded to the server. Attach actions open a proxy group after the server
   call; detach actions close it."
  {:added "4.1"}
  [node]
  (xt/for:array [action -/call-actions]
    (substrate/register-handler node action -/request-proxy nil))
  (xt/for:array [action -/attach-actions]
    (substrate/register-handler node action -/attach-forward-handler nil))
  (xt/for:array [action -/detach-actions]
    (substrate/register-handler node action -/detach-forward-handler nil))
  (return node))
