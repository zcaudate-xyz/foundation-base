(ns lang-demos.js-005-site-map.app.server
  (:require [lang.core :as l]))

(l/script :js
  {:require [[xt.substrate :as substrate]
             [xt.lang.common-data :as data]
             [xt.db.node.runtime :as runtime]]
   :static {:export false}})

(defn.js runtime-init
  []
  (var node (substrate/node-create {"id" "site-map-demo-worker"}))
  (substrate/register-handler
   node
   "@demo/site-map-summary"
   (fn [space args request node]
     (var site-map (data/get-in (. node ["meta"])
                                ["xt.db/site-map" "data"]))
     (return {"manifest" (. site-map ["manifest"])
              "tables" (Object.keys (. site-map ["schema"]))
              "rpc_ids" (Object.keys (. site-map ["rpc"]))}))
   nil)
  (return (runtime/sharedworker-init-kernel node)))
