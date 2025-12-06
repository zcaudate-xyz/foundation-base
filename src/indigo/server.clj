(ns indigo.server
  (:require [clojure.string :as string]
            [org.httpkit.server :as http]
            [org.httpkit.client :as client]
            [net.http.router :as router]
            [indigo.server.api-common :as api]
            [indigo.server.api-browser :as api-browser]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]
            [cheshire.core :as cheshire]
            [code.project :as project])
  (:import (java.awt Desktop)
           (java.net URI)))

(defonce *instance* (atom nil))

(def ^:dynamic *port* 1311)

(def ^:dynamic *public-path* "src-js/indigo/dist")


(defn wrap-browser-call
  [f]
  (fn [req]
    (let [params (try (cheshire/parse-string (:body req) true)
                      (catch Throwable t
                        (println "JSON Parse Error:" (.getMessage t))
                        {}))
          _ (println "Request Body:" (:body req))
          _ (println "Parsed Params:" params)
          req    (assoc req :params params)
          res    (f req)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body    (json/write res)})))

(def api-routes
  (router/router
   (merge
    (api/create-routes
     "POST /api/browse/"
     {"libraries"       (wrap-browser-call
                         (fn [req]
                           (#'api-browser/list-libraries)))
      "scan"            (wrap-browser-call
                         (fn [req]
                           (#'api-browser/scan-namespaces)))
      "lang/namespaces" (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")]
                             (#'api-browser/list-namespaces lang))))
      "lang/components" (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")
                                 ns   (get-in req [:params :ns])]
                             (#'api-browser/list-components lang ns))))
      "lang/component"  (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")
                                 ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (#'indigo.server.api-browser/get-component lang ns comp))))
      
      "clj/namespaces"  (wrap-browser-call
                         (fn [req]
                           (#'indigo.server.api-browser/list-clj-namespaces)))
      "clj/namespace-source" (wrap-browser-call
                              (fn [req]
                                (let [ns (get-in req [:params :ns])]
                                  (#'indigo.server.api-browser/get-namespace-source ns))))
      "clj/components"  (wrap-browser-call
                         (fn [req]
                           (let [ns (get-in req [:params :ns])]
                             (#'indigo.server.api-browser/list-clj-vars ns))))
      
      "clj/var-tests"   (wrap-browser-call
                         (fn [req]
                           (let [ns   (get-in req [:params :ns])
                                 var  (get-in req [:params :var])]
                             (#'indigo.server.api-browser/list-tests-for-var ns var))))
      "clj/component"   (wrap-browser-call
                         (fn [req]
                           (let [ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (#'indigo.server.api-browser/get-clj-var-source ns comp))))

      "test/namespaces" (wrap-browser-call
                         (fn [req]
                           (#'indigo.server.api-browser/list-test-namespaces)))
      "test/components" (wrap-browser-call
                         (fn [req]
                           (let [ns (get-in req [:params :ns])]
                             (#'indigo.server.api-browser/list-test-facts ns))))
      "test/component"  (wrap-browser-call
                         (fn [req]
                           (let [ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (#'indigo.server.api-browser/get-test-fact-source ns comp))))}))))

(def page-routes
  (router/router
   (api/create-routes
    "GET "
    {#_#_"/pages/demo"   (api/page-handler "Dev Demo"  page-demo/main)
     "*"             (fn [{:keys [uri] :as req}]
                       (router/serve-resource uri *public-path*))})))

(defn dev-handler
  [req]
  (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
        body (if body
               (String. (. body (readAllBytes))))
        req  (assoc req :body body)]
    (or (#'api-routes  req)
        (#'page-routes req)
        {:status 404 :body "Not Found"})))

(defn server-stop
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (swap! *instance*
          (fn [stop-fn]
            (when stop-fn (stop-fn :timeout 100))
            nil))))

(defn server-start
  []
  (swap! *instance*
         (fn [stop-fn]
           (when (not stop-fn)
             (http/run-server #'dev-handler {:port *port*})))))

(defn server-toggle
  []
  (if @*instance*
    (server-stop)
    (server-start)))

(defn server-restart
  []
  (if @*instance*
    (server-stop))
  (server-start))

(defn open-client
  []
  (. (Desktop/getDesktop)
     (browse (URI. (str "http://localhost:" *port*)))))

(comment
  (h/sh "curl" "-X" "POST" (str "http://localhost:" *port*
                                "/api/translate/to-heal")
        "-d" "(+ 1 2 3))")
  (server-restart)
  (server-toggle)
  (open-client))
