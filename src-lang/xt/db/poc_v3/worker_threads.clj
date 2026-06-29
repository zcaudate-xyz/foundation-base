(ns xt.db.poc-v3.worker-threads
  "Node.js `worker_threads` version of the poc-v3 substrate/db setup.

   Unlike the browser SharedWorker variant, this runs entirely in Node's
   `:basic` runtime. The xt.db.node adaptor lives inside a worker thread
   and has already been initialised with the scratch-v3 schema/lookup by
   `xt.db.poc-v3.worker-threads-script`. The main thread just connects to
   the worker over `parentPort` and attaches proxy models. No React, no
   browser APIs are required."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.common-data :as xtd]
             [xt.event.base-model :as event-model]
             [xt.substrate :as substrate]
             [xt.substrate.page-core :as base-page]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as transport-browser]
             [xt.db.node.adaptor-client :as adaptor-client]
             [js.worker.link :as worker-link]]})

(defn.js connect-worker-threads
  "Creates a client node, spawns the worker-thread server from `script-path`,
   and connects the two over the worker's MessagePort. Returns a promise of
   the client transport connection record."
  [script-path client-id]
  (var client (substrate/node-create {"id" client-id
                                      "spaces" {"room/a" {"state" {}}}}))
  (page-proxy/install client)

  (var link (worker-link/make-node-link script-path {"eval" false}))

  (return
   (transport-browser/connect-worker
    client
    {"source" link
     "transport_id" "worker"
     "wait_ready" true})))

(defn.js with-session
  "Runs callback inside a connected worker-thread session.
   `script-path` points to the worker file produced by
   `xt.db.poc-v3.worker-threads-script/write-worker-script!`.
   callback receives [client transport-id] and should return a promise."
  [script-path callback]
  (return
   (-> (-/connect-worker-threads script-path "poc-v3-client")
       (promise/x:promise-then
        (fn [conn]
          (var client (. conn ["node"]))
          (var transport-id (. conn ["transport_id"]))
          (return (callback client transport-id)))))))

(defn.js attach-profile-request
  "Asks the worker to attach the `profile` RPC model (backed by
   `scratch_v3.update_user_profile`) without opening the proxy group."
  [client transport-id]
  (return
   (adaptor-client/attach-model-request
    client
    "room/a"
    "demo"
    "profile"
    "@xt.db/attach-rpc-model"
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

(defn.js open-demo-group
  "Opens the `room/a/demo` proxy group on the client. This must be called
   after all required models have been attached on the worker."
  [client transport-id]
  (return
   (page-proxy/open-proxy-group client "room/a" "demo"
                                {"transport_id" transport-id})))

(defn.js wait-model-output
  "Returns a promise of the next non-empty output for `model-id` in
   `room/a/demo`."
  [client model-id]
  (var group (base-page/group-get client "room/a" "demo"))
  (var model (xtd/get-in group ["models" model-id]))
  (var current (event-model/get-current model nil))
  (when (not (xtd/is-empty? current))
    (return (promise/x:promise (fn [] (return current)))))
  (return
   (promise/x:promise-new
    (fn [resolve reject]
      (event-model/add-listener
       model
       "wait-output"
       (fn [id event time meta]
         (when (== (. event ["type"]) "model.output")
           (event-model/remove-listener model "wait-output")
           (resolve (. event ["data"]))))
       {}
       (fn [event]
         (return (== (. event ["type"]) "model.output"))))
      (return nil)))))

(defn.js with-profile-read
  "Connects to the worker, invokes the profile RPC with an empty update map,
   and returns a promise of the current UserProfile row."
  [script-path account-id]
  (return
   (-/with-session
    script-path
    (fn [client transport-id]
      (var p1 (-/attach-profile-request client transport-id))
      (var p2 (promise/x:promise-then
               p1
               (fn [_]
                 (return (-/open-demo-group client transport-id)))))
      (var p3 (promise/x:promise-then
               p2
               (fn [_]
                 (return (page-proxy/proxy-call client "room/a" "demo" "profile" [[account-id {} {}]] true {"transport_id" transport-id})))))
      (var p4 (promise/x:promise-then
               p3
               (fn [_]
                 (return (-/wait-model-output client "profile")))))
      (return p4)))))

(defn.js with-profile-update
  "Connects to the worker, updates the UserProfile for account-id with fields,
   and returns a promise of the updated row output."
  [script-path account-id fields]
  (return
   (-/with-session
    script-path
    (fn [client transport-id]
      (var p1 (-/attach-profile-request client transport-id))
      (var p2 (promise/x:promise-then
               p1
               (fn [_]
                 (return (-/open-demo-group client transport-id)))))
      (var p3 (promise/x:promise-then
               p2
               (fn [_]
                 (return (page-proxy/proxy-call client "room/a" "demo" "profile" [[account-id fields {}]] true {"transport_id" transport-id})))))
      (var p4 (promise/x:promise-then
               p3
               (fn [_]
                 (return (-/wait-model-output client "profile")))))
      (return p4)))))
