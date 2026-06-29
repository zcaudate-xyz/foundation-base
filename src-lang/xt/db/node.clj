(ns xt.db.node
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.adaptor-base :as adaptor-base]
             [xt.db.node.adaptor-proxy :as adaptor-proxy]
             [xt.db.node.adaptor-supabase :as adaptor-supabase]
             [xt.db.node.adaptor-proxy-supabase :as adaptor-proxy-supabase]]})

(def$.xt ACTION_QUERY           "@xt.db/query")
(def$.xt ACTION_SYNC            "@xt.db/sync-event")
(def$.xt ACTION_MODEL_PUT       "@xt.db/model-put")
(def$.xt ACTION_MODEL_MATERIALIZE "@xt.db/model-materialize")
(def$.xt ACTION_MODEL_SYNC      "@xt.db/model-sync")
(def$.xt ACTION_SOURCE_SHARE    "@xt.db/source-share")
(def$.xt ACTION_NODE_SUMMARY    "@xt.db/node-summary")

(defn.xt init-handlers
  "Installs all server-side xt.db.node adaptor handlers on a node."
  {:added "4.1"}
  [node]
  (adaptor-base/init-handlers node)
  (adaptor-supabase/init-handlers node)
  (return node))

(defn.xt init-proxy-handlers
  "Installs all client-side xt.db.node adaptor proxy handlers on a node."
  {:added "4.1"}
  [node]
  (adaptor-proxy/init-proxy-handlers node)
  (adaptor-proxy-supabase/init-proxy-handlers node)
  (return node))

(defn.xt server-service-config
  "normalises the server bootstrap config"
  {:added "4.1"}
  [opts]
  (var config (or opts {}))
  (var db (xt/x:get-key config "db"))
  (when (xt/x:not-nil? db)
    (return config))
  (var schema (xt/x:get-key config "schema"))
  (var lookup (xt/x:get-key config "lookup"))
  (var views (xt/x:get-key config "views"))
  (var db-opts (xt/x:get-key config "db_opts"))
  (return {"schema" schema
           "lookup" lookup
           "views" views
           "db_opts" db-opts
           "primary" (or (xt/x:get-key config "primary") {})
           "caching" (or (xt/x:get-key config "caching") {})
           "common"  (or (xt/x:get-key config "common") {})}))

(defn.xt install-server-services
  "installs the db services on an existing node"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var schema (xt/x:get-key config "schema"))
  (var lookup (xt/x:get-key config "lookup"))
  (var views  (xt/x:get-key config "views"))
  (var db     (xt/x:get-key config "db"))
  (var db-opts (xt/x:get-key config "db_opts"))
  (var primary (xt/x:get-key config "primary"))
  (var caching (xt/x:get-key config "caching"))
  (var common-id  (or (xt/x:get-key (or (xt/x:get-key config "common") {}) "id") "db/common"))
  (var primary-id (or (xt/x:get-key (or primary {}) "id") "db/primary"))
  (var caching-id (or (xt/x:get-key (or caching {}) "id") "db/caching"))
  (substrate/set-service node common-id {"schema" schema
                                         "lookup" lookup
                                         "views" views
                                         "db_opts" db-opts})
  (when (xt/x:not-nil? db)
    (substrate/set-service node primary-id db)
    (substrate/set-service node caching-id db)
    (return node))
  (return
   (-> (adaptor-base/init-base-main node config schema lookup)
       (promise/x:promise-then
        (fn [node]
          (when (xt/x:not-nil? views)
            (substrate/set-service node "db/views" views))
          (return node))))))

(defn.xt install
  "installs the full server-side db node stack on an existing node"
  {:added "4.1"}
  [node opts]
  (return
   (-> (-/install-server-services node opts)
       (promise/x:promise-then
        (fn [node]
          (-/init-handlers node)
          (page-proxy/install node)
          (return node))))))

(defn.xt create-server
  "creates and installs a full server-side db node"
  {:added "4.1"}
  [opts]
  (var node-opts (or (xt/x:get-key opts "node") {}))
  (return (promise/x:promise-run (-/install (substrate/node-create node-opts) opts))))

(defn.xt create
  "compat alias for create-server"
  {:added "4.1"}
  [opts]
  (return (-/create-server opts)))

(defn.xt install-client
  "installs the full client-side proxy stack on an existing node"
  {:added "4.1"}
  [node opts]
  (var config (or opts {}))
  (var transport-id (xt/x:get-key config "transport_id"))
  (-/init-proxy-handlers node)
  (page-proxy/install node)
  (when (xt/x:not-nil? transport-id)
    (adaptor-proxy/set-default-transport node transport-id)
    (adaptor-proxy-supabase/set-default-transport node transport-id))
  (return node))

(defn.xt create-client
  "creates and installs a full client-side proxy node"
  {:added "4.1"}
  [opts]
  (var node-opts (or (xt/x:get-key opts "node") {}))
  (return (promise/x:promise-run (-/install-client (substrate/node-create node-opts) opts))))

(defn.xt create-pair
  "creates a full server/client node pair"
  {:added "4.1"}
  [server-opts client-opts]
  (return
   (-> (-/create-server server-opts)
       (promise/x:promise-then
        (fn [server]
          (return
           (-> (-/create-client client-opts)
               (promise/x:promise-then
                (fn [client]
                  (return {"server" server
                           "client" client}))))))))))
