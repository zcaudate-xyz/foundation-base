(ns js.lib.fastify
  (:require [net.http :as http]
            [std.lang :as l]))

(l/script :js
   {:require [[js.core :as j]
              [xt.lang.common-data :as d]
              [xt.lang.spec-base :as x]
              [xt.lang.common-runtime :as rt]]
    :import  [["fastify" :as Fastify]]})

(defvar.js current-servers
  "gets the current servers"
  {:added "4.0"}
  []
  (return {}))

(defn.js wrap-handler
  "wraps the request into a map"
  {:added "4.0"}
  [f]
  (return
   (fn:> [req res]
     (f {:headers (d/from-flat (. req raw rawHeaders)
                               (fn [obj k v]
                                 (x:set-key obj k v)
                                 (return obj))
                               {})
          :query  (. req query)
          :path   (. req params ["*"])
         :url (. req
                 raw
                 url)
         :method (. req
                    raw
                    method)}))))

(defn.js start-server
  "starts a fastify server"
  {:added "4.0"}
  [port handler opts]
  (var app (Fastify {:logger true}))
  (. app (all "*" (-/wrap-handler handler)))
  (return (. app
             (listen (j/assign {:port port} opts))
             (then (fn []
                     (-/current-servers-reset
                      (j/assign (-/current-servers)
                                {port (j/assign app {:port port})}))
                     (return app))))))

(defn.js stop-server
  "stops a fastify server"
  {:added "4.0"}
  [port-or-app]
  (var app (:? (x:is-number? port-or-app)
               (x:get-key (-/current-servers) port-or-app)
               port-or-app))
  (if app
    (return (do (x:del-key (-/current-servers)
                           (. app port))
                (. app
                   (close)
                   (then (fn []
                           (return true))))))
    (return nil)))
