(ns xt.db.node.remote
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.spec :as spec]
             [xt.db.node.query :as query]
             [xt.db.node.sync :as sync]
             [xt.event.node :as event-node]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(defn.xt normalize-remote
  "normalizes remote settings"
  {:added "4.1"}
  [state remote-spec view-context]
  (return
   (xt/x:obj-assign
    {}
    (or (xt/x:get-key state "remote") {})
    (or remote-spec {})
    (or (xt/x:get-key view-context "remote") {}))))

(defn.xt request-remote
  "issues a remote node request"
  {:added "4.1"}
  [node space-id remote action payload]
  (return (event-node/request
           node
           (or (xt/x:get-key remote "space")
               (xt/x:get-key remote "target")
               space-id)
           action
           [payload]
           (or (xt/x:get-key remote "meta") {}))))

(defn.xt response-value
  "extracts the value portion of a remote response"
  {:added "4.1"}
  [response]
  (return (:? (and (xt/x:is-object? response)
                   (xt/x:has-key? response "value"))
               (xt/x:get-key response "value")
               response)))

(defn.xt run-remote-query
  "runs a query against a remote xt.db.node"
  {:added "4.1"}
  [node space-id state query-spec view-context remote-spec model-id view-id]
  (var [ok prepared] (query/prepare-query state query-spec view-context))
  (when (not ok)
    (return (promise/x:promise (fn [_ reject]
                                 (reject prepared)))))
  (var remote (-/normalize-remote state remote-spec view-context))
  (return
   (promise/x:promise-then
    (-/request-remote node
                      space-id
                      remote
                      spec/ACTION_QUERY
                      {:query query-spec
                       :view view-context})
    (fn [response]
      (when (and (xt/x:is-object? response)
                 (or (xt/x:has-key? response "db/sync")
                     (xt/x:has-key? response "db/remove")))
        (sync/apply-sync-request
         state
         {"db/sync" (xt/x:get-key response "db/sync")
          "db/remove" (xt/x:get-key response "db/remove")}))
      (return
       (query/attach-query-entry
        state
        prepared
        (-/response-value response)
        (or (xt/x:get-key response "tables")
            (xt/x:get-key prepared "tables"))
        model-id
        view-id))))))

(defn.xt run-remote-sync
  "runs a sync request against a remote xt.db.node"
  {:added "4.1"}
  [node space-id state sync-spec view-context remote-spec]
  (var [ok request] (sync/prepare-sync sync-spec view-context))
  (when (not ok)
    (return (promise/x:promise (fn [_ reject]
                                 (reject request)))))
  (var remote (-/normalize-remote state remote-spec view-context))
  (return
   (promise/x:promise-then
    (-/request-remote node
                      space-id
                      remote
                      spec/ACTION_SYNC
                      {:sync request
                       :view view-context})
    (fn [response]
      (var mirrored (or (xt/x:get-key response "result")
                        request))
      (sync/apply-sync-request state mirrored)
      (return response)))))
