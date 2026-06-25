(ns xt.db.node.adaptor-client
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]]})

;;
;; Client-side helpers for remotely attaching db-backed page models.
;;
;; The server-side handlers live in xt.db.node.adaptor-base. They create page
;; models that read from db/caching (sync) and db/primary (async). These client
;; helpers call those handlers over a transport and then open the resulting
;; group as a proxy on the client.
;;

(defn.xt attach-model-request
  "sends an attach request to the server-side adaptor handler"
  {:added "4.1"}
  [node space-id group-id model-id action service model-args opts]
  (var transport-id (xt/x:get-key opts "transport_id"))
  (return
   (substrate/request
    node
    space-id
    action
    [{"space_id" space-id
      "group_id" group-id
      "model_id" model-id
      "service"  service}
     model-args]
    {"transport_id" transport-id})))

(defn.xt attach-model-and-open
  "wraps an attach request so it also opens the proxy group on the client"
  {:added "4.1"}
  [node space-id group-id model-id action service model-args opts]
  (var transport-id (xt/x:get-key opts "transport_id"))
  (return
   (-> (-/attach-model-request
        node space-id group-id model-id action service model-args opts)
       (promise/x:promise-then
        (fn [_]
          (return
           (page-proxy/open-proxy-group
            node
            space-id
            group-id
            {"transport_id" transport-id})))))))

(defn.xt attach-pull-model
  "remotely attaches a pull-view model and opens it as a proxy on the client"
  {:added "4.1"}
  [node space-id group-id model-id service model-args opts]
  (return
   (-/attach-model-and-open
    node space-id group-id model-id
    "@xt.db/attach-pull-model"
    service model-args opts)))

(defn.xt attach-tree-view-model
  "remotely attaches a tree-view model and opens it as a proxy on the client"
  {:added "4.1"}
  [node space-id group-id model-id service model-args opts]
  (return
   (-/attach-model-and-open
    node space-id group-id model-id
    "@xt.db/attach-tree-view-model"
    service model-args opts)))

(defn.xt attach-rpc-model
  "remotely attaches an rpc model and opens it as a proxy on the client"
  {:added "4.1"}
  [node space-id group-id model-id service-id model-args opts]
  (return
   (-/attach-model-and-open
    node space-id group-id model-id
    "@xt.db/attach-rpc-model"
    service-id model-args opts)))
