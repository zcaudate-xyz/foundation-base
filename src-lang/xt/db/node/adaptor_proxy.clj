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

(defn.xt detach-pull-model
  "Client proxy for @xt.db/detach-pull-model"
  {:added "4.1"}
  [node space-id group-id model-id service opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/detach-pull-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service}]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/close-proxy-after node space-id group-id opts)))))))

(defn.xt detach-tree-view-model
  "Client proxy for @xt.db/detach-tree-view-model"
  {:added "4.1"}
  [node space-id group-id model-id service opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/detach-tree-view-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service}]
                          (-/request-meta node opts))
       (promise/x:promise-then
        (fn [_]
          (return (-/close-proxy-after node space-id group-id opts)))))))

(defn.xt detach-rpc-model
  "Client proxy for @xt.db/detach-rpc-model"
  {:added "4.1"}
  [node space-id group-id model-id service-id opts]
  (return
   (-> (substrate/request node
                          space-id
                          "@xt.db/detach-rpc-model"
                          [{"space_id" space-id
                            "group_id" group-id
                            "model_id" model-id
                            "service"  service-id}]
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

