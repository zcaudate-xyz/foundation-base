(ns js.cell-v3.adaptor.route
  (:require [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]]})

(defn.js make-registry
  "creates a route registry"
  {:added "4.0"}
  []
  (return {"::" "cell-v3.route-registry"
           :entries {}}))

(defn.js register-route
  "registers a route"
  {:added "4.0"}
  [registry route-id handler opts]
  (var prev (. registry ["entries"] [route-id]))
  (:= (. registry ["entries"] [route-id])
      (j/assign {:id route-id
                 :handler handler}
                (or opts {})))
  (return prev))

(defn.js unregister-route
  "removes a route"
  {:added "4.0"}
  [registry route-id]
  (var prev (. registry ["entries"] [route-id]))
  (del (. registry ["entries"] [route-id]))
  (return prev))

(defn.js get-route
  "gets a route"
  {:added "4.0"}
  [registry route-id]
  (return (. registry ["entries"] [route-id])))

(defn.js list-routes
  "lists routes"
  {:added "4.0"}
  [registry]
  (return (k/obj-keys (. registry ["entries"]))))

(defn.js dispatch-route
  "dispatches a route handler"
  {:added "4.0"}
  [registry route-id args ctx]
  (var entry (-/get-route registry route-id))
  (when (k/nil? entry)
    (k/err (k/cat "ERR - Route not found - " route-id)))
  (var handler (k/get-key entry "handler"))
  (return (handler ctx (k/unpack (or args [])))))
