(ns js.lib.fastify
  (:require [std.lang :as l]
            [std.lib :as h]
            [net.http :as http]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-runtime :as rt :with [defvar.js]]
             [xt.lang.base-lib :as k]]
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
     (f {:headers (k/from-flat (. req raw rawHeaders)
                               k/step-set-key
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
  (var app (:? (k/is-number? port-or-app)
               (k/get-key (-/current-servers) port-or-app)
               port-or-app))
  (if app
    (return (do (k/del-key (-/current-servers)
                           (. app port))
                (. app
                   (close)
                   (then k/T))))
    (return nil)))

