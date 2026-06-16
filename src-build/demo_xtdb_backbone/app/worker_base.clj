(ns demo-xtdb-backbone.app.worker-base
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.net.http-fetch :as js-fetch]
             [js.net.conn-sqlite :as sqlite-wasm]
             [xt.db.node :as db-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate.transport-browser :as browser-transport]]})

(defn.js custom-config
  [custom]
  (var config (:? (and (xt/x:is-object? custom)
                       (xt/x:has-key? custom "default"))
                  (xt/x:get-key custom "default")
                  custom))
  (return (or config {})))

(defn.js merge-config
  [default-config custom-config]
  (var config (xtd/obj-assign-nested
               (xtd/obj-assign-nested {} (or default-config {}))
               (or custom-config {})))
  (var node-id (xt/x:get-key config "node_id"))
  (xt/x:set-key config
                "ready"
                (xtd/obj-assign-nested
                 {"signal" "ready"
                  "worker" node-id}
                 (or (xt/x:get-key config "ready") {})))
  (return config))

(defn.js prepare-supabase-source
  [config source-id]
  (var source-key (or source-id "primary"))
  (var primary (xtd/obj-assign-nested
                {}
                (or (xtd/get-in config ["db" "sources" source-key])
                    {})))
  (var primary-config (xtd/obj-assign-nested
                       {}
                       (or (xt/x:get-key primary "config")
                           {})))
  (var client (xtd/obj-assign-nested
               {}
               (or (xt/x:get-key primary-config "client")
                   {})))
  (when (xt/x:nil? (xt/x:get-key client "transport"))
    (xt/x:set-key client "transport" (js-fetch/create {} {})))
  (xt/x:set-key primary "kind" (or (xt/x:get-key primary "kind")
                                   "supabase"))
  (xt/x:set-key primary-config "client" client)
  (xt/x:set-key primary "config" primary-config)
  (xtd/set-in config ["db" "sources" source-key] primary)
  (return config))

(defn.js prepare-sqlite-source
  [config source-id]
  (var source-key (or source-id "caching"))
  (var caching (xtd/obj-assign-nested
                {}
                (or (xtd/get-in config ["db" "sources" source-key])
                    {})))
  (var caching-config (xtd/obj-assign-nested
                       {}
                       (or (xt/x:get-key caching "config")
                           {})))
  (var caching-setup (xtd/obj-assign-nested
                      {"schema" true}
                      (or (xt/x:get-key caching "setup")
                          {})))
  (when (xt/x:nil? (xt/x:get-key caching-config "driver"))
    (xt/x:set-key caching-config "driver" (sqlite-wasm/create {})))
  (xt/x:set-key caching "kind" (or (xt/x:get-key caching "kind")
                                   "sqlite"))
  (xt/x:set-key caching "config" caching-config)
  (xt/x:set-key caching "setup" caching-setup)
  (xtd/set-in config ["db" "sources" source-key] caching)
  (return config))

(defn.js worker-config
  [default-config custom-config]
  (var config (-/merge-config default-config custom-config))
  (-/prepare-supabase-source config "primary")
  (-/prepare-sqlite-source config "caching")
  (return config))

(defn.js ensure-shared-state
  [config]
  (var shared-key (xt/x:get-key config "shared_key"))
  (var shared (xt/x:get-key globalThis shared-key))
  (if (xt/x:nil? shared)
    (do
      (:= shared {"counter" 0
                  "config" config})
      (xt/x:set-key
       shared
       "ready"
       (promise/x:promise-then
        (db-node/create config)
        (fn [node]
          (xt/x:set-key shared "node" node)
          (return node))))
      (xt/x:set-key globalThis shared-key shared)
      (return shared))
    (do
      (when (xt/x:nil? (xt/x:get-key shared "config"))
        (xt/x:set-key shared "config" config))
      (return shared))))

(defn.js boot-port
  [shared port]
  (var config (xt/x:get-key shared "config"))
  (. port (start))
  (return
   (promise/x:promise-then
    (xt/x:get-key shared "ready")
    (fn [node]
      (var idx (+ 1 (or (xt/x:get-key shared "counter")
                        0)))
      (xt/x:set-key shared "counter" idx)
      (return
       (browser-transport/boot-self
        node
        {"transport_id" (xt/x:cat (xt/x:get-key config "transport_prefix")
                                  idx)
         "target" port
         "ready" (xt/x:get-key config "ready")}))))))

(defn.js runtime-init
  [config]
  (var shared (-/ensure-shared-state config))
  (:= (. globalThis ["onconnect"])
      (fn [event]
        (var port (. event ["ports"] [0]))
        (return (-/boot-port shared port))))
  (return shared))
