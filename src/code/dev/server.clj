(ns code.dev.server
  (:require [org.httpkit.server :as http]
            [code.dev.server.router :as router]
            [code.dev.server.pages :as pages]
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


(defn from-html
  [body]
  (std.block/string
   (std.block/layout
    (std.lib.walk/postwalk
     (fn [x]
       (if (map? x)
         (let [v (or (:class x)
                     (:classname x))
               v (if (string? v)
                   [v]
                   (vec (keep (fn [v]
                                (not-empty (str/trim v)))
                              v)))]
           (cond-> x
             :then (dissoc :classname :class)
             (seq v) (assoc :class v)))
         x))
     (std.html/tree body)))))

(defn to-html
  [body]
  (std.html/html
   (try 
     (read-string body)
     (catch Throwable t
       ""))))

(def dev-route-handler
  (router/router
   {#_#_#_#_
    "GET /" (fn [req] (html/html (#'pages/index-page)))
    "GET /page/demo" (fn [req] (html/html (#'pages/demo-page)))
    "POST /api/translate/from-html"  (fn [req]
                                       (json/write
                                        {:data (from-html (:body req))}))
    "POST /api/translate/to-html"    (fn [req]
                                       (json/write
                                        {:data (to-html (:body req))}))
    "POST /api/translate/js"        (fn [req] (json/write
                                               {:op :translate-js}))
    "POST /api/translate/python"    (fn [req] (json/write
                                               {:op :translate-python}))
    "POST /api/translate/postgres"  (fn [req] (json/write
                                               {:op :translate-postgres}))}))

(defn dev-handler
  [req]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
        body (if body
               (String. (. body (readAllBytes)))
               {})]
    (or (#'dev-route-handler
         (assoc req :body body))

        (cond (= uri "/")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (#'pages/index-page)}

              (= uri "/page/demo")
              {:status 200
               :headers {"Content-Type" "text/html"}
               :body    
               (#'pages/demo-page)}

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

