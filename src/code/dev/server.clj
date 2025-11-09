(ns code.dev.server
  (:require [org.httpkit.server :as http]
            [code.dev.server.router :as router]
            [code.dev.server.frontend :as frontend]
            [std.json :as json]
            [std.lib :as h]
            [std.lib.bin :as bin]
            [std.html :as html]
            [std.string :as str])
  (:import (java.awt Desktop)
           (java.net URI)))

(def ^:dynamic *port* 1311)

(def ^:dynamic *public-path* "assets/code.dev/public")

(defonce ^:dynamic *instance*
  (atom nil))

(def dev-route-handler
  (router/router
   {#_#_#_#_
    "GET /" (fn [req] (html/html (#'frontend/index-page)))
    "GET /page/demo" (fn [req] (html/html (#'frontend/demo-page)))
    "POST /api/translate/html"      (fn [req] (json/write
                                               {:op :translate-html}))
    "POST /api/translate/js"        (fn [req] (json/write
                                               {:op :translate-js}))
    "POST /api/translate/python"    (fn [req] (json/write
                                               {:op :translate-python}))
    "POST /api/translate/postgres"  (fn [req] (json/write
                                               {:op :translate-postgres}))}))

(defn dev-handler
  [req]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
        parsed-body (if body
                      (json/read
                       (String. (. body
                                   (readAllBytes))))
                      {})]
    (or (#'dev-route-handler
         (assoc req :body body))

        (cond (= uri "/")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (html/html (#'frontend/index-page))}

              (= uri "/page/demo")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (html/html (#'frontend/demo-page))}

              (= request-method :get)
              (router/serve-resource uri *public-path*)
              
              :else
              {:status 404 :body "Not Found"}))))

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
                                "/api/translate/js"))
  (server-toggle)
  (open-client)
  )

