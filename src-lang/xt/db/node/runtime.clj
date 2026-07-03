(ns xt.db.node.runtime
  (:require [hara.lang :as l]))

(l/script :xtalk
          {:require [[xt.lang.spec-base :as xt]
                     [xt.lang.spec-promise :as promise]
                     [xt.substrate :as substrate]
                     [xt.substrate.page-proxy :as page-proxy]
                     [xt.substrate.transport-browser :as browser-transport]
                     [xt.db.node.client-base :as client-base]
                     [xt.db.node.proxy-base :as proxy-base]
                     [xt.db.node.proxy-supabase :as proxy-supabase]
                     [xt.db.node.kernel-base :as kernel-base]
                     [xt.db.node.kernel-supabase :as kernel-supabase]]})

(def.xt DEFAULT_TRANSPORT
  "xt.db.default.transport")

(def.xt DEFAULT_WORKER
  "xt.db.default.worker")

(defn.xt init-server
  [node]
  (page-proxy/install node)
  (kernel-base/init-handlers node)
  (kernel-supabase/init-handlers node))

(defn.xt init-server-proxy
  [node]
  (page-proxy/install node)
  (proxy-base/init-proxy-handlers node)
  (proxy-supabase/init-proxy-handlers node))


;;
;; SHAREDWORKER
;;

(defn.xt sharedworker-init-kernel
  [node transport-id worker-id]
  (-/init-server node)
  (:= (. globalThis ["onconnect"])
      (fn [e]
        (var port (. e ["ports"] [0]))
        (. port (start))
        (return
         (browser-transport/boot-self
          node
          {"transport_id" (or transport-id
                              -/DEFAULT_TRANSPORT)
           "target" port
           "ready" {"signal" "ready"
                    "transport" (or transport-id
                                    -/DEFAULT_TRANSPORT)
                    "worker" (or transport-id
                                 -/DEFAULT_WORKER)}})))))

(defn sharedworket-init-string
  [& [override]]
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" xt.db.node.runtime/DEFAULT_WORKER}))
      (xt.db.node.runtime/sharedworker-init-kernel node))
   {:lang :js
    :layout :full
    :emit {:override (or {"@sqlite.org/sqlite-wasm" "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                          "pg" "data:text/javascript,export default {Client: function() {}}"})}}))


(def.xt DEFAULT_SHAREDWORKER_SCRIPT
  (@! (sharedworket-init-string)))

(defn.xt sharedworker-connect
  [client config schema lookup source transport-id]
  (-/init-server-proxy client)
  (return
   (-> (browser-transport/connect-sharedworker
        client
        {"transport_id" (or transport-id
                            -/DEFAULT_TRANSPORT)
         "source" (or source
                      (browser-transport/sharedworker-source -/DEFAULT_SHAREDWORKER_SCRIPT
                                                             {"type" "module"}))})
       (promise/x:promise-then
        (fn [conn]
          (return
           (client-base/kernel-init client config schema lookup {})))))))


;;
;; WEBWORKER
;;

(defn.xt webworker-init-kernel
  [node transport-id worker-id]
  (-/init-server node)
  (return
   (browser-transport/boot-self
    node
    {"transport_id" (or transport-id
                        -/DEFAULT_TRANSPORT)
     "target" globalThis
     "ready" {"signal" "ready"
              "transport" (or transport-id
                              -/DEFAULT_TRANSPORT)
              "worker" (or worker-id
                           -/DEFAULT_WORKER)}})))

(defn webworker-init-string
  [& [override]]
  (l/emit-script
   '(do
      (var node (xt.substrate/node-create {"id" xt.db.node.runtime/DEFAULT_WORKER}))
      (xt.db.node.runtime/webworker-init-kernel node))
   {:lang :js
    :layout :full
    :emit {:override (or override
                         {"@sqlite.org/sqlite-wasm" "https://esm.sh/@sqlite.org/sqlite-wasm@3.51.2-build8"
                          "pg" "data:text/javascript,export default {Client: function() {}}"})}}))

(def.xt DEFAULT_WEBWORKER_SCRIPT
  (@! (webworker-init-string)))

(defn.xt webworker-connect
  [client config schema lookup source transport-id]
  (-/init-server-proxy client)
  (return
   (-> (browser-transport/connect-worker
        client
        {"transport_id" (or transport-id
                            -/DEFAULT_TRANSPORT)
         "source" (or source
                      (browser-transport/webworker-source -/DEFAULT_WEBWORKER_SCRIPT
                                                          {"type" "module"}))})
       (promise/x:promise-then
        (fn [conn]
          (return
           (client-base/kernel-init client config schema lookup {})))))))




;;
;; NODEWORKER
;;

(defn.xt nodeworker-init-kernel
  [node transport-id worker-id]
  (-/init-server node)
  (var worker
       {"postMessage" (fn [data]
                        (. parentPort (postMessage data)))
        "addEventListener" (fn [event listener capture]
                             (when (== event "message")
                               (. parentPort (on "message"
                                                 (fn [data]
                                                   (listener {"data" data}))))))})
  (return
   (browser-transport/boot-self
    node
    {"transport_id" (or transport-id
                        -/DEFAULT_TRANSPORT)
     "target" worker
     "ready" {"signal" "ready"
              "transport" (or transport-id
                              -/DEFAULT_TRANSPORT)
              "worker" (or worker-id
                           -/DEFAULT_WORKER)}})))

(defn nodeworker-init-string
  [& [override]]
  (l/emit-script
   '(do
      (:- :import #{parentPort} :from "'worker_threads'")
      (var node (xt.substrate/node-create {"id" xt.db.node.runtime/DEFAULT_WORKER}))
      (xt.db.node.runtime/nodeworker-init-kernel node))
   {:lang :js
    :layout :full
    :emit {:override override}}))

(def.xt DEFAULT_NODEWORKER_SCRIPT
  (@! (nodeworker-init-string)))

(defn.xt nodeworker-connect
  [client config schema lookup source transport-id]
  (-/init-server-proxy client)
  (return
   (-> (browser-transport/connect-worker
        client
        {"transport_id" (or transport-id
                                 -/DEFAULT_TRANSPORT)
         "source" (or source
                      (browser-transport/node-worker-source -/DEFAULT_NODEWORKER_SCRIPT
                                                            {}))})
       (promise/x:promise-then
        (fn [conn]
          (return
           (client-base/kernel-init client config schema lookup {})))))))
