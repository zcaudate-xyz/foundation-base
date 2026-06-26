(ns xt.db.node.adaptor-proxy
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]]})

;;
;; Client-side proxy API for the server-side adaptor handlers in
;; xt.db.node.adaptor-base.
;;
;; These functions mirror the server substrate handler ids (e.g.
;; @xt.db/attach-pull-model) but run on the client. They forward the work to
;; the server over the configured transport and then keep the local proxy
;; page group in sync.
;;
;; This is a function namespace, not a handler namespace. Substrate routing
;; will always prefer an attached transport when no transport_id is given, so
;; proxy helpers must supply transport_id explicitly.
;;

(defn.xt set-default-transport
  "sets the default server transport id for proxy requests"
  {:added "4.1"}
  [node transport-id]
  (xtd/set-in node ["state" "adaptor_proxy" "default_transport_id"] transport-id)
  (return transport-id))

(defn.xt get-default-transport
  "gets the default server transport id for proxy requests"
  {:added "4.1"}
  [node]
  (return (xtd/get-in node ["state" "adaptor_proxy" "default_transport_id"])))

(defn.xt get-transport-id
  "resolves the transport id from opts or the node default"
  {:added "4.1"}
  [node opts]
  (return (or (xtd/get-in opts ["transport_id"])
              (-/get-default-transport node))))

(defn.xt request-meta
  "builds request meta with an explicit transport_id"
  {:added "4.1"}
  [node opts]
  (var transport-id (-/get-transport-id node opts))
  (return {"transport_id" transport-id}))

(defn.xt install-service-stubs
  "installs lightweight db/* service stubs on the client from a server summary"
  {:added "4.1"}
  [node summary]
  (var services (xtd/get-in summary ["services"]))
  (var primary (xtd/get-in services ["db/primary"]))
  (var caching (xtd/get-in services ["db/caching"]))
  (var common  (xtd/get-in services ["db/common"]))
  (var target (substrate/get-services node))
  (when (xt/x:not-nil? primary)
    (xt/x:set-key target "db/primary" primary))
  (when (xt/x:not-nil? caching)
    (xt/x:set-key target "db/caching" caching))
  (when (xt/x:not-nil? common)
    (xt/x:set-key target "db/common" common))
  (return node))

(defn.xt init-adaptor
  "Client proxy for @xt.db/init-adaptor.

   Forwards to the server and installs lightweight service stubs on the client."
  {:added "4.1"}
  [node config schema lookup opts]
  (return
   (-> (substrate/request node
                          nil
                          "@xt.db/init-adaptor"
                          [config schema lookup]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [summary]
          (-/install-service-stubs node summary)
          (return summary))))))

(defn.xt open-proxy-after
  "helper that opens a proxy group after a server attach succeeds"
  {:added "4.1"}
  [node space-id group-id opts]
  (var transport-id (-/get-transport-id node opts))
  (return
   (page-proxy/open-proxy-group node space-id group-id {"transport_id" transport-id})))

(defn.xt attach-pull-model
  "Client proxy for @xt.db/attach-pull-model"
  {:added "4.1"}
  [node space-id group-id model-id service model-args opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/attach-pull-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service}
                           model-args]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/open-proxy-after node space-id group-id opts)))))))

(defn.xt attach-tree-view-model
  "Client proxy for @xt.db/attach-tree-view-model"
  {:added "4.1"}
  [node space-id group-id model-id service model-args opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/attach-tree-view-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service}
                           model-args]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/open-proxy-after node space-id group-id opts)))))))

(defn.xt attach-rpc-model
  "Client proxy for @xt.db/attach-rpc-model"
  {:added "4.1"}
  [node space-id group-id model-id service-id model-args opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/attach-rpc-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service-id}
                           model-args]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/open-proxy-after node space-id group-id opts)))))))

(defn.xt close-proxy-after
  "helper that closes a proxy group after a server detach succeeds"
  {:added "4.1"}
  [node space-id group-id opts]
  (var transport-id (-/get-transport-id node opts))
  (return
   (page-proxy/close-proxy-group node space-id group-id {"transport_id" transport-id})))

(defn.xt detach-db-model
  "Client proxy for @xt.db/detach-db-model"
  {:added "4.1"}
  [node space-id group-id model-id service opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/detach-db-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service}]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/close-proxy-after node space-id group-id opts)))))))

(defn.xt call-rpc
  "Client proxy for @xt.db/call-rpc"
  {:added "4.1"}
  [node space-id service-id rpc-spec args opts]
  (return
   (substrate/request node
                      space-id
                      "@xt.db/call-rpc"
                      [service-id rpc-spec args]
                      (-/request-meta node opts))))

(defn.xt call-fetch
  "Client proxy for @xt.db/call-fetch"
  {:added "4.1"}
  [node space-id service-id args opts]
  (return
   (substrate/request node
                      space-id
                      "@xt.db/call-fetch"
                      [service-id args]
                      (-/request-meta node opts))))

(defn.xt sync-event
  "Client proxy for @xt.db/sync-event"
  {:added "4.1"}
  [node space-id payload opts]
  (return
   (substrate/request node
                      space-id
                      "@xt.db/sync-event"
                      [payload]
                      (-/request-meta node opts))))

(defn.xt subscribe-db
  "Client proxy for @xt.db/subscribe-db"
  {:added "4.1"}
  [node space-id conn-id topics opts]
  (return
   (substrate/request node
                      space-id
                      "@xt.db/subscribe-db"
                      [conn-id topics]
                      (-/request-meta node opts))))

(defn.xt unsubscribe-db
  "Client proxy for @xt.db/unsubscribe-db"
  {:added "4.1"}
  [node space-id conn-id topics opts]
  (return
   (substrate/request node
                      space-id
                      "@xt.db/unsubscribe-db"
                      [conn-id topics]
                      (-/request-meta node opts))))

;;
;; Client-side proxy handlers.
;;
;; These mirror the server-side handlers in xt.db.node.adaptor-base so that the
;; same substrate function ids can be invoked on a client node and be forwarded
;; to the server over the configured transport.
;;

(defn.xt ^{:substrate/fn "@xt.db/init-adaptor"}
  init-adaptor-handler
  [space args request node]
  (var config (xt/x:first args))
  (var schema (xt/x:second args))
  (var lookup (xt/x:get-idx args (xt/x:offset 2)))
  (return (-/init-adaptor node config schema lookup request)))

(defn.xt ^{:substrate/fn "@xt.db/attach-pull-model"}
  attach-pull-model-handler
  [space args request node]
  (var node-args  (xt/x:first args))
  (var model-args (xt/x:second args))
  (var space-id   (xt/x:get-key node-args "space_id"))
  (var group-id   (xt/x:get-key node-args "group_id"))
  (var model-id   (xt/x:get-key node-args "model_id"))
  (var service    (xt/x:get-key node-args "service"))
  (return (-/attach-pull-model node space-id group-id model-id service model-args request)))

(defn.xt ^{:substrate/fn "@xt.db/attach-tree-view-model"}
  attach-tree-view-model-handler
  [space args request node]
  (var node-args  (xt/x:first args))
  (var model-args (xt/x:second args))
  (var space-id   (xt/x:get-key node-args "space_id"))
  (var group-id   (xt/x:get-key node-args "group_id"))
  (var model-id   (xt/x:get-key node-args "model_id"))
  (var service    (xt/x:get-key node-args "service"))
  (return (-/attach-tree-view-model node space-id group-id model-id service model-args request)))

(defn.xt ^{:substrate/fn "@xt.db/attach-rpc-model"}
  attach-rpc-model-handler
  [space args request node]
  (var node-args  (xt/x:first args))
  (var model-args (xt/x:second args))
  (var space-id   (xt/x:get-key node-args "space_id"))
  (var group-id   (xt/x:get-key node-args "group_id"))
  (var model-id   (xt/x:get-key node-args "model_id"))
  (var service    (xt/x:get-key node-args "service"))
  (return (-/attach-rpc-model node space-id group-id model-id service model-args request)))

(defn.xt ^{:substrate/fn "@xt.db/detach-db-model"}
  detach-db-model-handler
  [space args request node]
  (var node-args (xt/x:first args))
  (var space-id  (xt/x:get-key node-args "space_id"))
  (var group-id  (xt/x:get-key node-args "group_id"))
  (var model-id  (xt/x:get-key node-args "model_id"))
  (var service   (xt/x:get-key node-args "service"))
  (return (-/detach-db-model node space-id group-id model-id service request)))

(defn.xt ^{:substrate/fn "@xt.db/call-rpc"}
  call-rpc-handler
  [space args request node]
  (var service-id (xt/x:first args))
  (var rpc-spec   (xt/x:second args))
  (var fn-args    (xt/x:get-idx args (xt/x:offset 2)))
  (return (-/call-rpc node space service-id rpc-spec fn-args request)))

(defn.xt ^{:substrate/fn "@xt.db/call-fetch"}
  call-fetch-handler
  [space args request node]
  (var service-id  (xt/x:first args))
  (var fetch-input (xt/x:second args))
  (return (-/call-fetch node space service-id fetch-input request)))

(defn.xt ^{:substrate/fn "@xt.db/sync-event"}
  sync-event-handler
  [space args request node]
  (var payload (or (xt/x:first args) {}))
  (return (-/sync-event node space payload request)))

(defn.xt ^{:substrate/fn "@xt.db/subscribe-db"}
  subscribe-db-handler
  [space args request node]
  (var conn-id (xt/x:first args))
  (var topics  (xt/x:second args))
  (return (-/subscribe-db node space conn-id topics request)))

(defn.xt ^{:substrate/fn "@xt.db/unsubscribe-db"}
  unsubscribe-db-handler
  [space args request node]
  (var conn-id (xt/x:first args))
  (var topics  (xt/x:second args))
  (return (-/unsubscribe-db node space conn-id topics request)))

(defn.xt init-proxy-handlers
  "Registers client-side proxy handlers so that the same substrate function ids
   used server-side can be invoked on a client node and forwarded to the server."
  {:added "4.1"}
  [node]
  (substrate/register-handler node "@xt.db/init-adaptor" -/init-adaptor-handler nil)
  (substrate/register-handler node "@xt.db/attach-pull-model" -/attach-pull-model-handler nil)
  (substrate/register-handler node "@xt.db/attach-rpc-model" -/attach-rpc-model-handler nil)
  (substrate/register-handler node "@xt.db/attach-tree-view-model" -/attach-tree-view-model-handler nil)
  (substrate/register-handler node "@xt.db/detach-db-model" -/detach-db-model-handler nil)
  (substrate/register-handler node "@xt.db/call-fetch" -/call-fetch-handler nil)
  (substrate/register-handler node "@xt.db/call-rpc" -/call-rpc-handler nil)
  (substrate/register-handler node "@xt.db/sync-event" -/sync-event-handler nil)
  (substrate/register-handler node "@xt.db/subscribe-db" -/subscribe-db-handler nil)
  (substrate/register-handler node "@xt.db/unsubscribe-db" -/unsubscribe-db-handler nil)
  (return node))

