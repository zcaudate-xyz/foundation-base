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

(def.xt CALL_ACTIONS
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

(def.xt ATTACH_ACTIONS
  ["@xt.db/attach-model"
   "@xt.db/rpc-attach-model"
   "@xt.db/pull-attach-model"
   "@xt.db/dataview-attach-model"])

(def.xt DETACH_ACTIONS
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
  (page-proxy/group-create-proxy node space-id group-id {} {"transport_id" transport-id})
  (return
   (promise/x:promise-then
    (substrate/request node
                       nil
                       (. request ["action"])
                       args
                       {"transport_id" transport-id})
    (fn [status]
      (return
       (promise/x:promise-then
        (page-proxy/group-open-proxy node space-id group-id {"transport_id" transport-id})
        (fn [_]
          (return status))))))))

(defn.xt detach-forward-handler
  "forwards a detach action to the server and closes the proxy group"
  {:added "4.1"}
  [space args request node]
  (var page-args (xt/x:second args))
  (var space-id (xtd/get-in page-args ["space_id"]))
  (var group-id (xtd/get-in page-args ["group_id"]))
  (var transport-id (proxy-util/get-transport-id node (xtd/get-in request ["meta"])))
  (return
   (-> (page-proxy/group-close-proxy node space-id group-id {"transport_id" transport-id})
       (promise/x:promise-then
        (fn [_]
          (return
           (substrate/request node
                              nil
                              (. request ["action"])
                              args
                              {"transport_id" transport-id})))))))

(defn.xt init-proxy-handlers
  "Registers client-side proxy handlers for xt.db.node.kernel-base methods so
   that the same substrate function ids can be invoked on a client node and
   forwarded to the server. Attach actions open a proxy group after the server
   call; detach actions close it."
  {:added "4.1"}
  [node]
  (xt/for:array [action -/CALL_ACTIONS]
    (substrate/register-handler node action -/request-proxy nil))
  (xt/for:array [action -/ATTACH_ACTIONS]
    (substrate/register-handler node action -/attach-forward-handler nil))
  (xt/for:array [action -/DETACH_ACTIONS]
    (substrate/register-handler node action -/detach-forward-handler nil))
  (return node))
