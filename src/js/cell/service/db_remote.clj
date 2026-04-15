(ns js.cell.service.db-remote
  (:require [std.lang :as l]))

(l/script :xtalk
  {:export [MODULE] :require [[js.cell.service.db-query :as db-query] [js.cell.service.db-sync :as db-sync] [xt.lang.common-spec :as xt]]})

(defn.xt remote-capable?
  "checks that the db descriptor can dispatch remote requests"
  {:added "4.0"}
  [db]
  (return (xt/x:is-function? (xt/x:get-key db "dispatch"))))

(defn.xt normalize-remote
  "normalizes a remote spec against the db and view context"
  {:added "4.0"}
  [db remote-spec view-context]
  (return {"target"    (or (xt/x:get-key remote-spec "target")
                           (xt/x:get-key db "target")
                           (xt/x:get-key view-context "target"))
           "dispatch"  (or (xt/x:get-key remote-spec "dispatch")
                           (xt/x:get-key db "dispatch")
                           (xt/x:get-key view-context "dispatch"))
           "decode"    (or (xt/x:get-key remote-spec "decode")
                           (xt/x:get-key db "decode")
                           (xt/x:get-key view-context "decode"))
           "map_error" (or (xt/x:get-key remote-spec "map_error")
                           (xt/x:get-key db "map_error")
                           (xt/x:get-key view-context "map_error"))}))

(defn.xt build-request
  "builds a remote request envelope"
  {:added "4.0"}
  [db remote-spec op-spec view-context]
  (var remote (-/normalize-remote db remote-spec view-context))
  (return {"target"    (xt/x:get-key remote "target")
           "op"        (xt/x:get-key remote-spec "op")
           "body"      op-spec
           "dispatch"  (xt/x:get-key remote "dispatch")
           "decode"    (xt/x:get-key remote "decode")
           "map_error" (xt/x:get-key remote "map_error")
           "view_id"   (xt/x:get-key view-context "view-id")
           "model_id"  (xt/x:get-key view-context "model-id")}))

(defn.xt dispatch-request
  "dispatches a remote request"
  {:added "4.0"}
  [db request view-context]
  (var dispatch-fn (or (xt/x:get-key request "dispatch")
                       (xt/x:get-key db "dispatch")
                       (xt/x:get-key view-context "dispatch")))
  (when (not (xt/x:is-function? dispatch-fn))
    (return [false {:status "error"
                    :tag "db/remote-dispatch-not-provided"}]))
  (return (dispatch-fn request view-context)))

(defn.xt decode-response
  "decodes a remote response"
  {:added "4.0"}
  [db remote-spec response view-context]
  (var remote (-/normalize-remote db remote-spec view-context))
  (var decode-fn (xt/x:get-key remote "decode"))
  (if (xt/x:is-function? decode-fn)
    (return [true (decode-fn response view-context)])
    (return [true response])))

(defn.xt map-remote-error
  "maps a remote error into the local error contract"
  {:added "4.0"}
  [db error view-context]
  (var map-fn (or (xt/x:get-key db "map_error")
                  (xt/x:get-key view-context "map_error")))
  (if (xt/x:is-function? map-fn)
    (return (map-fn error view-context))
    (return {:status "error"
             :tag "db/remote-request-failed"
             :data error})))

(defn.xt run-remote-query
  "prepares and dispatches a remote query"
  {:added "4.0"}
  [db remote-spec query-spec view-context]
  (var [ok query-plan] (db-query/prepare-query db query-spec view-context))
  (when (not ok)
    (return [ok query-plan]))
  (var request (-/build-request db
                                (xt/x:obj-assign remote-spec {"op" "query"})
                                query-plan
                                view-context))
  (var [d-ok response] (-/dispatch-request db request view-context))
  (when (not d-ok)
    (return [false (-/map-remote-error db response view-context)]))
  (when (== "error" (xt/x:get-key response "status"))
    (return [false (-/map-remote-error db response view-context)]))
  (return (-/decode-response db remote-spec response view-context)))

(defn.xt run-remote-sync
  "prepares and dispatches a remote sync request"
  {:added "4.0"}
  [db remote-spec sync-spec view-context]
  (var [ok request-body] (db-sync/prepare-sync db sync-spec view-context))
  (when (not ok)
    (return [ok request-body]))
  (var request (-/build-request db
                                (xt/x:obj-assign remote-spec {"op" "sync"})
                                request-body
                                view-context))
  (var [d-ok response] (-/dispatch-request db request view-context))
  (when (not d-ok)
    (return [false (-/map-remote-error db response view-context)]))
  (when (== "error" (xt/x:get-key response "status"))
    (return [false (-/map-remote-error db response view-context)]))
  (return (-/decode-response db remote-spec response view-context)))

(def.xt MODULE (!:module))
