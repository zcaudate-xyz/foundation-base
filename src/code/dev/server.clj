(ns code.dev.server
  (:require [org.httpkit.server :as http]
            [net.http.router :as router]
            [code.dev.server.pages :as pages]
            [code.dev.server.api-common :as api]
            [code.dev.server.api-task :as api-task]
            [code.dev.server.api-prompt :as api-prompt]
            [code.dev.client.page-index :as page-index]
            [code.dev.client.page-tasks :as page-tasks]
            [code.dev.client.page-demo :as page-demo]
            [std.lib :as h]
            [std.string :as str])
  (:import (java.awt Desktop)
           (java.net URI)))

(def ^:dynamic *port* 1311)

(def ^:dynamic *public-path* "assets/code.dev/public")

(defonce ^:dynamic *instance*
  (atom nil))

(def api-routes
  (router/router
   (api/create-prompt-routes
    "POST /api/translate/"
    {"from-html"       api-task/from-html
     "to-html"         api-task/to-html
     "to-heal"         api-task/to-heal
     "to-js-dsl"       api-task/to-js-dsl
     "to-jsxc-dsl"     api-task/to-jsxc-dsl
     "to-python-dsl"   api-task/to-python-dsl
     "to-plpgsql-dsl"  api-task/to-plpgsql-dsl})))

(def page-routes
  (router/router
   (api/create-routes
    "GET "           
    {"/"             (api/page-handler "Dev Index" page-index/main)
     "/pages/tasks"  (api/page-handler "Dev Tasks" page-tasks/main)
     "/pages/demo"   (api/page-handler "Dev Demo"  page-demo/main)
     "*"             (fn [{:keys [uri]}]
                       (or (router/serve-resource uri *public-path*)
                           {:status 404 :body "Not Found"}))})))

(defn dev-handler
  [req]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
        body (if body
               (String. (. body (readAllBytes))))
        req  (assoc req :body body)]
    (or (#'api-routes  req)
        (#'page-routes req)
        {:status 404 :body "Not Found"})))

(defn server-start
  []
  (swap! *instance*
         (fn [stop-fn]
           (when (not stop-fn)
             (http/run-server #'dev-handler {:port *port*})))))

(defn server-stop
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (swap! *instance*
          (fn [stop-fn]
            (when stop-fn (stop-fn :timeout 100))
            nil))))

(defn server-toggle
  []
  (if @*instance*
    (server-stop)
    (server-start)))

(defn open-client
  []
  (. (Desktop/getDesktop)
     (browse (URI. (str "http://localhost:" *port*)))))

(comment
  (h/sh "curl" "-X" "POST" (str "http://localhost:" *port*
                                "/api/translate/to-heal")
        "-d" "(+ 1 2 3))")
  (server-toggle)
  (open-client)
  )

