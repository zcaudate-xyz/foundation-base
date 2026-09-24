(ns lang-demos.js-005-site-map.app.client
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.lang.spec-promise :as promise]
             [xt.substrate :as substrate]
             [xt.substrate.transport-browser :as browser-transport]
             [xt.substrate.page-proxy :as page-proxy]
             [xt.db.node.proxy-base :as proxy-base]
             [xt.db.node.proxy-supabase :as proxy-supabase]
             [xt.db.node.proxy-util :as proxy-util]
             [xt.db.node.client-base :as client-base]]
   :static {:export false}})

(defn.js stringify
  [value]
  (return (JSON.stringify value nil 2)))

(defn.js query-summary
  [client init]
  (return
   (-> (substrate/request client nil "@demo/site-map-summary" [] {})
       (promise/x:promise-then
        (fn [summary]
          (return {"kernel_init" init
                   "worker_site_map" summary}))))))

(defn.js initialise-worker
  [client config source]
  (page-proxy/install client)
  (proxy-base/init-proxy-handlers client)
  (proxy-supabase/init-proxy-handlers client)
  (return
   (-> (browser-transport/connect-sharedworker
        client
        {"transport_id" "xt.db.default.transport"
         "source" source})
       (promise/x:promise-then
        (fn [connection]
          (proxy-util/set-default-transport
           client
           (. connection ["transport_id"]))
          (return
           (client-base/kernel-init client config {} {} {}))))
       (promise/x:promise-then
        (fn [init]
          (return {"init" init}))))))

(defn.js connect-demo
  []
  (var button (. document (getElementById "connect")))
  (var output (. document (getElementById "output")))
  (:= (. button ["disabled"]) true)
  (:= (. output ["textContent"]) "Connecting to the SharedWorker…")
  (var client (substrate/node-create {"id" "site-map-demo-client"}))
  (var config {"site_map_url" "/worker/site-map/manifest.json"
               "primary" {"type" "memory" "defaults" {}}
               "caching" {"type" "memory" "defaults" {}}})
  (var source
       (browser-transport/sharedworker-url-source
        "/worker/site-map/site-map-worker.js"
        {"type" "module"}))
  (-> (-/initialise-worker client config source)
      (promise/x:promise-then
       (fn [state]
         (return (-/query-summary client (. state ["init"])))))
      (promise/x:promise-then
       (fn [result]
         (:= (. output ["textContent"]) (-/stringify result))
         (:= (. button ["disabled"]) false)
         (:= (. button ["textContent"]) "Connected — load again")
         (return result)))
      (promise/x:promise-catch
       (fn [error]
         (:= (. output ["textContent"])
             (-/stringify {"error" (or (. error ["message"]) error)}))
         (:= (. button ["disabled"]) false)
         (return nil)))))

(defn.js mount
  []
  (var button (. document (getElementById "connect")))
  (. button (addEventListener "click" (fn [] (-/connect-demo))))
  (return button))

(defrun.js __init__
  (-/mount))
