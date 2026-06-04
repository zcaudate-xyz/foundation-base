(ns xt.protocol.impl.graphdb
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-protocol :as proto]
             [xt.protocol.graphdb :as graph-if]]})

(defn.xt driver?
  "checks if a value is a wrapped graphdb driver"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (== "graphdb.driver"
                   (xt/x:get-key obj "::")))))

(defn.xt db?
  "checks if a value is a wrapped graphdb runtime"
  {:added "4.1"}
  [obj]
  (return (and (xt/x:is-object? obj)
               (xt/x:has-key? obj "__graphdb_impl"))))

(defn.xt require-driver
  "ensures a value is a wrapped graphdb driver"
  {:added "4.1"}
  [value]
  (when (not (-/driver? value))
    (xt/x:err "Value is not a graphdb driver"))
  (return value))

(defn.xt require-db
  "ensures a value is a wrapped graphdb runtime"
  {:added "4.1"}
  [value]
  (when (not (-/db? value))
    (xt/x:err "Value is not a graphdb runtime"))
  (return value))

(defn.xt driver-impl
  "normalises graphdb driver implementations"
  {:added "4.1"}
  [value]
  (cond (-/driver? value)
         (return (xt/x:get-key value "_impl"))

         (xt/x:is-function? value)
         (return {"create" value})

         (xt/x:is-object? value)
         (return value)

         (xt/x:nil? value)
         (return {})

         :else
         (xt/x:err "Unsupported graphdb driver implementation")))

(defn.xt db-impl
  "normalises graphdb db implementations"
  {:added "4.1"}
  [value]
  (cond (-/db? value)
         (return (xt/x:get-key value "__graphdb_impl"))

         :else
         (return (-/driver-impl value))))

(defn.xt driver-op
  "gets an operation from the graphdb driver implementation"
  {:added "4.1"}
  [driver op]
  (return (xt/x:get-key (-/driver-impl driver) op)))

(defn.xt db-op
  "gets an operation from the graphdb db implementation"
  {:added "4.1"}
  [db op]
  (return (xt/x:get-key (-/db-impl db) op)))

(defn.xt require-op
  "ensures the graphdb runtime implements a specific operation"
  {:added "4.1"}
  [db op]
  (var op-fn (-/db-op db op))
  (when (not (xt/x:is-function? op-fn))
    (xt/x:err (xt/x:cat "Graph db missing " op " implementation")))
  (return op-fn))

(defn.xt driver-create
  "wraps a graphdb implementation map with a driver protocol"
  {:added "4.1"}
  [impl]
  (when (-/driver? impl)
    (return impl))
  (:= impl (-/driver-impl impl))
  (var protocol
       (xt/proto:create
        (proto/proto-spec
         [[graph-if/IGraphDbRuntimeDriver
           {"create" (fn [self input]
                       (var create-fn (-/driver-op self "create"))
                       (when (not (xt/x:is-function? create-fn))
                         (xt/x:err "Graph db driver missing create implementation"))
                       (return (create-fn input)))}]])))
  (var driver {"::" "graphdb.driver"
               "_impl" impl})
  (xt/proto:set driver protocol)
  (return driver))

(defn.xt db-create
  "attaches the graphdb runtime protocol to a graphdb client/runtime object"
  {:added "4.1"}
  [db impl]
  (when (-/db? db)
    (return db))
  (when (xt/x:nil? db)
    (xt/x:err "Graph db runtime requires a db map"))
  (when (not (xt/x:is-object? db))
    (xt/x:err "Graph db runtime requires an object db map"))
  (:= impl (-/db-impl impl))
  (var protocol
       (xt/proto:create
        {"pull"          (fn [self schema tree opts]
                            (return ((-/require-op self "pull")
                                     self
                                     schema
                                     tree
                                     opts)))
         "pull_sync"     (fn [self schema tree opts]
                            (return ((-/require-op self "pull_sync")
                                     self
                                     schema
                                     tree
                                     opts)))
         "record_add"    (fn [self schema table-name records opts]
                            (return ((-/require-op self "record_add")
                                     self
                                     schema
                                     table-name
                                     records
                                     opts)))
         "record_delete" (fn [self schema table-name ids opts]
                            (return ((-/require-op self "record_delete")
                                     self
                                     schema
                                     table-name
                                     ids
                                     opts)))}))
  (xt/x:set-key db "__graphdb_impl" impl)
  (xt/proto:set db protocol)
  (return db))

(defn.xt create
  "creates a backend instance through the wrapped graphdb driver"
  {:added "4.1"}
  [driver input]
  (:= driver (-/require-driver driver))
  (var create-fn (xt/proto:method driver "create"))
  (when (xt/x:nil? create-fn)
    (xt/x:err "Graph db driver missing create method"))
  (return (create-fn driver input)))

(defn.xt pull
  "dispatches async pull through the wrapped graphdb runtime"
  {:added "4.1"}
  [db schema tree opts]
  (:= db (-/require-db db))
  (var op-fn (xt/proto:method db "pull"))
  (when (xt/x:nil? op-fn)
    (xt/x:err "Graph db missing pull method"))
  (return (op-fn db schema tree opts)))

(defn.xt pull-sync
  "dispatches sync pull through the wrapped graphdb runtime"
  {:added "4.1"}
  [db schema tree opts]
  (:= db (-/require-db db))
  (var op-fn (xt/proto:method db "pull_sync"))
  (when (xt/x:nil? op-fn)
    (xt/x:err "Graph db missing pull_sync method"))
  (return (op-fn db schema tree opts)))

(defn.xt record-add
  "dispatches async record_add through the wrapped graphdb runtime"
  {:added "4.1"}
  [db schema table-name records opts]
  (:= db (-/require-db db))
  (var op-fn (xt/proto:method db "record_add"))
  (when (xt/x:nil? op-fn)
    (xt/x:err "Graph db missing record_add method"))
  (return (op-fn db schema table-name records opts)))

(defn.xt record-delete
  "dispatches async record_delete through the wrapped graphdb runtime"
  {:added "4.1"}
  [db schema table-name ids opts]
  (:= db (-/require-db db))
  (var op-fn (xt/proto:method db "record_delete"))
  (when (xt/x:nil? op-fn)
    (xt/x:err "Graph db missing record_delete method"))
  (return (op-fn db schema table-name ids opts)))
