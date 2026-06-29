(ns xt.db.poc-v3.sharedworker
  "Reusable SharedWorker bootstrap and client helpers for xt.db.poc-v3.

   The worker runs a minimal xt.substrate node with the xt.db.node adaptor. A
   browser page connects to `/poc-v3-worker.js` (a same-origin module
   SharedWorker) and asks the worker to initialise a Supabase primary + SQLite
   cache pair using the scratch-v3 schema and lookup. After that the page can
   attach proxy views and RPC models as usual.

   The scratch-v3 schema follows the Supabase annotation conventions used in
   `postgres.sample.scratch-v0` and in backend RPC namespaces:

     :api/meta {:sb/grant :all}     on functions exposed through PostgREST
     :api/meta {:sb/access ...}      on tables exposed through PostgREST

   See `postgres.core.supabase/transform-entry` for how these annotations are
   turned into `GRANT` statements."
  (:require [hara.lang :as l]
            [clojure.java.io :as io]
            [scaffold.supabase.local-min :as local-min]
            [postgres.sample.scratch-v3 :as scratch-v3]
            [postgres.core :as pg]))

(def +schema+
  "Bound scratch-v3 schema for the substrate db node."
  (pg/bind-schema (:schema (pg/app "scratch_v3"))))

(def +lookup+
  "Bound scratch-v3 lookup (indexed by :time-updated) for sync."
  (pg/bind-app (pg/app "scratch_v3") :time-updated))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]
             [xt.lang.common-data :as xtd]
             [xt.lang.spec-promise :as promise]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.adaptor-base :as adaptor-base]
             [xt.db.node.adaptor-client :as adaptor-client]]})

(def.js Schema
  (@! +schema+))

(def.js Lookup
  (@! +lookup+))

(defn.js sharedworker-source
  "Returns a transport source map that creates a module SharedWorker from
   `/poc-v3-worker.js`. This avoids blob URLs, which are blocked by the
   playground's CSP."
  []
  (return
   {"create_fn"
    (fn [listener]
      (var shared (new SharedWorker "/poc-v3-worker.js" {"type" "module"}))
      (var port (. shared ["port"]))
      (. shared (addEventListener
                 "error"
                 (fn [err]
                   (listener {"signal" "shared-error"
                              "message" (. err ["message"])}))
                 false))
      (. port (start))
      (. port (addEventListener
               "message"
               (fn [e]
                 (listener (. e ["data"])))
               false))
      (return port))}))

(defn.js connect-sharedworker
  "Creates a client node and connects it to `/poc-v3-worker.js`.
   Returns a promise of the transport connection record."
  [client-id]
  (var client (substrate/node-create {"id" client-id
                                      "spaces" {"room/a" {"state" {}}}}))
  (page-proxy/install client)
  (return
   (browser-transport/connect-sharedworker
    client
    {"transport_id" "worker"
     "source" (-/sharedworker-source)
     "wait_ready" true})))

(defn.js init-adaptor
  "Asks the worker to initialise the scratch-v3 adaptor with a Supabase
   primary and a SQLite cache."
  [client transport-id]
  (return
   (substrate/request client
                      "room/a"
                      "@xt.db/init-adaptor"
                      [{"primary" {"id" "db/primary"
                                   "type" "supabase"
                                   "defaults" (@! local-min/+config-supabase-anon+)}
                        "caching" {"id" "db/caching"
                                   "type" "sqlite"
                                   "defaults" {}}}
                       xt.db.poc-v3.sharedworker/Schema
                       xt.db.poc-v3.sharedworker/Lookup]
                      {"transport_id" transport-id})))

(defn.js attach-user-profile-rpc
  "Attaches an RPC model that reads the UserProfile row for account-id
   via the `scratch_v3.user_profile_by_account` PostgREST function."
  [client transport-id account-id]
  (return
   (adaptor-client/attach-rpc-model
    client
    "room/a"
    "demo"
    "profile"
    "db/primary"
    {"rpc_spec" {"input" [{"symbol" "i_account_id" "type" "uuid"}]
                 "return" "jsonb"
                 "schema" "scratch_v3"
                 "id" "user_profile_by_account"
                 "flags" {}}
     "pipeline" {}
     "options" {}
     "defaults" {"fn_args" [account-id]}}
    {"transport_id" transport-id})))

(defn.js attach-update-profile-rpc
  "Attaches an RPC model that calls `scratch_v3.update_user_profile`."
  [client transport-id]
  (return
   (adaptor-client/attach-rpc-model
    client
    "room/a"
    "demo"
    "update-profile"
    "db/primary"
    {"rpc_spec" {"input" [{"symbol" "i_account_id" "type" "uuid"}
                          {"symbol" "m" "type" "jsonb"}
                          {"symbol" "o_op" "type" "jsonb"}]
                 "return" "jsonb"
                 "schema" "scratch_v3"
                 "id" "update_user_profile"
                 "flags" {}}
     "pipeline" {}
     "options" {}
     "defaults" {"fn_args" []}}
    {"transport_id" transport-id})))

(defn.js get-profile-output
  "Reads the current value of the attached `profile` view model."
  [client]
  (var group (base-page/group-get client "room/a" "demo"))
  (var model (xtd/get-in group ["models" "profile"]))
  (return (event-model/get-current model nil)))

(defn.js update-profile
  "Calls the attached `update-profile` RPC model to change the UserProfile
   fields for account-id. Returns the RPC result promise."
  [client account-id fields op]
  (return
   (base-page/model-set-input
    client "room/a" "demo" "update-profile"
    [[account-id fields (or op {})]]
    {})))

(defn.js refresh-profile
  "Re-runs the attached `profile` RPC model so its output reflects the latest
   primary data. Returns the model update promise."
  [client]
  (return
   (base-page/model-set-input client "room/a" "demo" "profile" [] {})))

(defn.js with-session
  "Runs callback inside a connected, initialised SharedWorker session.
   callback receives [client transport-id] and should return a promise."
  [callback]
  (return
   (-> (-/connect-sharedworker "poc-v3-session-client")
       (promise/x:promise-then
        (fn [conn]
          (var client (. conn ["node"]))
          (var transport-id (. conn ["transport_id"]))
          (return
           (-> (-/init-adaptor client transport-id)
               (promise/x:promise-then
                (fn [_]
                  (return (callback client transport-id)))))))))))

(defn sharedworker-script
  "Returns the minimal ES-module SharedWorker bootstrap as a string.

   It creates a single xt.substrate node, installs the xt.db.node adaptor
   request handlers, and exposes the node over the worker's MessagePort via
   `xt.substrate.transport-browser/boot-self`. The client supplies the schema,
   lookup and adaptor configuration through the `@xt.db/init-adaptor` handler."
  []
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" "poc-v3-sharedworker"
                                           "spaces" {"room/a" {"state" {}}}}))

      ;; install the db adaptor request handlers
      (xt.db.node.adaptor-base/init-handlers node)

      ;; wrap init-adaptor so that errors are serialised back to the page
      (xt.substrate/register-handler
       node
       "@xt.db/init-adaptor"
       (fn [space args request node]
         (return
          (. (xt.db.node.adaptor-base/init-adaptor-main node
                                                        (. args [0])
                                                        (. args [1])
                                                        (. args [2]))
             (then (fn [_]
                     (return {"status" "ok"})))
             (catch (fn [err]
                      (return {"status" "error"
                               "message" (. err ["message"])
                               "stack" (. err ["stack"])}))))))
       nil)

      ;; allow remote clients to open proxy groups on this node
      (xt.substrate.page-proxy/install node)

      (:= (. globalThis ["onconnect"])
          (fn [e]
            (var port (. e ["ports"] [0]))
            (. port (start))
            (return
             (xt.substrate.transport-browser/boot-self
              node
              {"transport_id" "host"
               "target" port
               "ready" {"signal" "ready"
                        "transport" "browser"
                        "worker" "poc-v3-sharedworker"}})))))
   {:lang :js
    :layout :full
    :emit {:override {"@sqlite.org/sqlite-wasm"
                      "/sqlite3-init.js"
                      "pg"
                      "/pg-stub.js"}}}))

(defn write-worker-files!
  "Writes `/poc-v3-worker.js` and the SQLite/pg stubs required by the worker
   into `root`. The files must be served from the same origin because module
   SharedWorkers cannot be loaded from blob URLs under the playground CSP."
  [root]
  (let [worker-path        (str root "/poc-v3-worker.js")
        pg-stub-path       (str root "/pg-stub.js")
        sqlite-init-path   (str root "/sqlite3-init.js")
        sqlite-wasm-path   (str root "/sqlite3.wasm")]
    (io/make-parents worker-path)
    (spit pg-stub-path "export default {Client: function() {}};\n")
    (io/copy (java.io.File. "node_modules/@sqlite.org/sqlite-wasm/dist/index.mjs")
             (java.io.File. sqlite-init-path))
    (io/copy (java.io.File. "node_modules/@sqlite.org/sqlite-wasm/dist/sqlite3.wasm")
             (java.io.File. sqlite-wasm-path))
    (spit worker-path (sharedworker-script))
    {:worker-path worker-path
     :pg-stub-path pg-stub-path
     :sqlite-init-path sqlite-init-path
     :sqlite-wasm-path sqlite-wasm-path}))
