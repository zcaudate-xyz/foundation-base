(ns xt.db.node
  (:refer-clojure :exclude [remove sync])
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.db.node.view-model :as model]
             [xt.db.node.view-util :as util]
             [xt.db.node.schema-spec :as spec]
             [xt.db.runtime :as db-runtime]
             [xt.db.text.sql-manage :as sql-manage]
             [xt.substrate :as event-node]
             [xt.protocol.impl.connection-sql :as sql]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(def.xt META_KEY spec/META_KEY)
(def.xt STATE_TAG spec/STATE_TAG)

(def.xt ACTION_QUERY spec/ACTION_QUERY)
(def.xt ACTION_QUERY_REFRESH spec/ACTION_QUERY_REFRESH)
(def.xt ACTION_SYNC spec/ACTION_SYNC)
(def.xt ACTION_REMOVE spec/ACTION_REMOVE)
(def.xt ACTION_CLEAR spec/ACTION_CLEAR)
(def.xt ACTION_SNAPSHOT spec/ACTION_SNAPSHOT)

(def.xt SIGNAL_CACHE_CHANGED spec/SIGNAL_CACHE_CHANGED)
(def.xt SIGNAL_CACHE_INVALIDATED spec/SIGNAL_CACHE_INVALIDATED)
(def.xt SIGNAL_QUERY_CHANGED spec/SIGNAL_QUERY_CHANGED)
(def.xt SIGNAL_MODEL_CHANGED spec/SIGNAL_MODEL_CHANGED)

(defn.xt create-space
  "registers all declarative models for a single xt.db node space"
  {:added "4.1"}
  [node space-id space-spec]
  (xt/for:object [[model-id model-spec] (or (xt/x:get-key space-spec "models") {})]
    (model/model-put node space-id model-id model-spec))
  (return node))

(defn.xt source-live?
  "checks if a source spec should be materialized into a live runtime"
  {:added "4.1"}
  [source]
  (var driver (:? (xt/x:has-key? source "driver")
                  (xt/x:get-key source "driver")
                  nil))
  (var constructor (:? (xt/x:has-key? source "constructor")
                       (xt/x:get-key source "constructor")
                       nil))
  (var wrapper (:? (xt/x:has-key? source "wrapper")
                   (xt/x:get-key source "wrapper")
                   nil))
  (return (or (xt/x:not-nil? driver)
              (xt/x:not-nil? constructor)
              (xt/x:not-nil? wrapper))))

(defn.xt materialize-source
  "materializes a live source wrapper into a db runtime and stores it on the source"
  {:added "4.1"}
  [node space-id model-id source-id]
  (var source (model/source-get node space-id model-id source-id))
  (when (or (xt/x:nil? source)
            (xt/x:not-nil? (xt/x:get-key source "db"))
            (not (-/source-live? source)))
    (return source))
  (var opts (util/node-opts node))
  (var schema (or (xt/x:get-key source "schema")
                  (xt/x:get-key opts "schema")
                  {}))
  (var lookup (or (xt/x:get-key source "lookup")
                  (xt/x:get-key opts "lookup")
                  {}))
  (var dbtype (or (xt/x:get-key source "dbtype")
                  "db.sql"))
  (var db-opts (xt/x:get-key source "db_opts"))
  (var driver (:? (xt/x:has-key? source "driver")
                  (xt/x:get-key source "driver")
                  nil))
  (var constructor (:? (xt/x:has-key? source "constructor")
                       (xt/x:get-key source "constructor")
                       nil))
  (var wrapper (:? (xt/x:has-key? source "wrapper")
                   (xt/x:get-key source "wrapper")
                   nil))
  (var connect-step (promise/x:promise-run source))
  (cond (xt/x:not-nil? driver)
        (:= connect-step (sql/connect driver source))

        (and (xt/x:not-nil? constructor)
             (xt/x:not-nil? wrapper))
        (:= connect-step
            (promise/x:promise-then
             (sql/ensure-promise (constructor source))
             (fn [raw]
               (return (wrapper raw)))))

        :else
        (:= connect-step (promise/x:promise-run source)))
  (return
   (promise/x:promise-then
    (sql/ensure-promise connect-step)
    (fn [conn]
      (var db (db-runtime/db-create
               {"::" dbtype
                :instance conn}
               schema
               lookup
               db-opts))
      (xt/x:set-key source "instance" conn)
      (xt/x:set-key source "db" db)
      (xt/x:set-key source "live" true)
      (when (xt/x:get-key source "setup_schema")
        (db-runtime/db-exec-sync
         db
         (xt/x:str-join
          "\n\n"
          (sql-manage/table-create-all
           schema
           lookup
           db-opts))))
      (when (xt/x:not-nil? (xt/x:get-key source "seed"))
        (db-runtime/sync-event db ["add" (xt/x:get-key source "seed")]))
      (return source)))))

(defn.xt materialize-space
  "materializes all live sources for registered models within a space"
  {:added "4.1"}
  [node space-id]
  (var state (model/ensure-space-state node space-id))
  (var running [])
  (xt/for:object [[model-id model-entry] (or (xt/x:get-key state "models") {})]
    (xt/for:object [[source-id source] (or (xt/x:get-key model-entry "sources") {})]
      (when (-/source-live? source)
        (xt/x:arr-push running
                       (-/materialize-source node space-id model-id source-id)))))
  (return running))

(defn.xt create
  "creates a node from a declarative xt.db manifest"
  {:added "4.1"}
  [spec]
  (:= spec (or spec {}))
  (var node-id (or (xt/x:get-key spec "node_id")
                   (xt/x:get-key spec "id")))
  (when (xt/x:nil? node-id)
    (xt/x:err "node_id not found"))
  (var node (event-node/node-create {"id" node-id}))
  (model/install node (or (xt/x:get-key spec "db") {}))
  (var running [])
  (xt/for:object [[space-id space-spec] (or (xt/x:get-key spec "spaces") {})]
    (-/create-space node space-id space-spec)
    (xt/x:arr-assign running (-/materialize-space node space-id)))
  (cond (> (xt/x:len running) 0)
        (return
         (promise/x:promise-then
          (promise/x:promise-all running)
          (fn [_]
            (return node))))

        :else
        (return (promise/x:promise-run node))))

(def.xt install model/install)
(def.xt uninstall model/uninstall)
(def.xt ensure-space-state model/ensure-space-state)

(def.xt query model/query)
(def.xt query-refresh model/query-refresh)
(def.xt sync model/sync)
(def.xt remove model/remove)
(def.xt clear model/clear)
(def.xt snapshot model/snapshot)

(def.xt model-put model/model-put)
(def.xt model-get model/model-get)
(def.xt model-dependents model/model-dependents)
(def.xt model-sync model/model-sync)
(def.xt model-refresh model/model-refresh)
(def.xt source-get model/source-get)
(def.xt source-refresh model/source-refresh)
(def.xt source-put model/source-put)
(def.xt source-sync model/source-sync)
(def.xt view-put model/view-put)
(def.xt view-get model/view-get)
(def.xt view-dependents model/view-dependents)
(def.xt view-val model/view-val)
(def.xt view-input model/view-input)
(def.xt view-pending model/view-pending)
(def.xt view-error model/view-error)
(def.xt view-refresh model/view-refresh)
(def.xt view-set-input model/view-set-input)

(def.xt node-opts util/node-opts)
