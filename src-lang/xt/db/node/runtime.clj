(ns xt.db.node.runtime
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.kernel-base :as kernel-base]
             [xt.db.node.kernel-supabase :as kernel-supabase]]})

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
          {"transport_id" transport-id
           "target" port
           "ready" {"signal" "ready"
                    "transport" "browser"
                    "worker" worker-id}})))))

(defn.xt sharedworker-connect-kernel
  [client source transport-id config schema lookup]
  (-/init-server-proxy node)
  (return
   (-> (browser-transport/connect-sharedworker
        client
        {"transport_id" transport-id
         "source" source})
       (promise/x:promise-then
        (fn [conn]
          (return
           (substrate/request client
                              nil
                              "@xt.db/kernel-init"
                              [config schema lookup]
                              {})))))))
