(ns xt.cell.service.db-remote
  (:require [std.lang :as l]))

(l/script :xtalk
  {:require [[xt.cell.service.db-query :as db-query]
             [xt.cell.service.db-sync :as db-sync]
             [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.xt remote-capable?
  "checks that the db descriptor can dispatch remote requests"
  {:added "4.0"}
  [db]
  (return (k/is-function? (k/get-key db "dispatch"))))

(defn.xt normalize-remote
  "normalizes a remote spec against the db and view context"
  {:added "4.0"}
  [db remote-spec view-context]
  (return {"target"    (or (k/get-key remote-spec "target")
                           (k/get-key db "target")
                           (k/get-key view-context "target"))
           "dispatch"  (or (k/get-key remote-spec "dispatch")
                           (k/get-key db "dispatch")
                           (k/get-key view-context "dispatch"))
           "decode"    (or (k/get-key remote-spec "decode")
                           (k/get-key db "decode")
                           (k/get-key view-context "decode"))
           "map_error" (or (k/get-key remote-spec "map_error")
                           (k/get-key db "map_error")
                           (k/get-key view-context "map_error"))}))

(defn.xt build-request
  "builds a remote request envelope"
  {:added "4.0"}
  [db remote-spec op-spec view-context]
  (var remote (-/normalize-remote db remote-spec view-context))
  (return {"target"    (k/get-key remote "target")
           "op"        (k/get-key remote-spec "op")
           "body"      op-spec
           "dispatch"  (k/get-key remote "dispatch")
           "decode"    (k/get-key remote "decode")
           "map_error" (k/get-key remote "map_error")
           "view_id"   (k/get-key view-context "view-id")
           "model_id"  (k/get-key view-context "model-id")}))

(defn.xt dispatch-request
  "dispatches a remote request"
  {:added "4.0"}
  [db request view-context]
  (var dispatch-fn (or (k/get-key request "dispatch")
                       (k/get-key db "dispatch")
                       (k/get-key view-context "dispatch")))
  (when (not (k/is-function? dispatch-fn))
    (return [false {:status "error"
                    :tag "db/remote-dispatch-not-provided"}]))
  (return (dispatch-fn request view-context)))

(defn.xt decode-response
  "decodes a remote response"
  {:added "4.0"}
  [db remote-spec response view-context]
  (var remote (-/normalize-remote db remote-spec view-context))
  (var decode-fn (k/get-key remote "decode"))
  (if (k/is-function? decode-fn)
    (return [true (decode-fn response view-context)])
    (return [true response])))

(defn.xt map-remote-error
  "maps a remote error into the local error contract"
  {:added "4.0"}
  [db error view-context]
  (var map-fn (or (k/get-key db "map_error")
                  (k/get-key view-context "map_error")))
  (if (k/is-function? map-fn)
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
                                (k/obj-assign remote-spec {"op" "query"})
                                query-plan
                                view-context))
  (var [d-ok response] (-/dispatch-request db request view-context))
  (when (not d-ok)
    (return [false (-/map-remote-error db response view-context)]))
  (when (== "error" (k/get-key response "status"))
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
                                (k/obj-assign remote-spec {"op" "sync"})
                                request-body
                                view-context))
  (var [d-ok response] (-/dispatch-request db request view-context))
  (when (not d-ok)
    (return [false (-/map-remote-error db response view-context)]))
  (when (== "error" (k/get-key response "status"))
    (return [false (-/map-remote-error db response view-context)]))
  (return (-/decode-response db remote-spec response view-context)))

(def.xt MODULE (!:module))
