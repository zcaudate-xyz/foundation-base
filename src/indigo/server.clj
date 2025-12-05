(ns indigo.server
  (:require [org.httpkit.server :as http]
            [org.httpkit.client :as client]
            [net.http.router :as router]
            [indigo.server.api-common :as api]
            [indigo.server.api-task :as api-task]
            [indigo.server.api-prompt :as api-prompt]
            [indigo.client.page-index :as page-index]
            [indigo.client.page-tasks :as page-tasks]
            [indigo.client.page-demo :as page-demo]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json])
  (:import (java.awt Desktop)
           (java.net URI)))

(def ^:dynamic *port* 1311)

(defonce ^:dynamic *instance* (atom nil))

(def ^:dynamic *public-path* "assets/indigo/public")

(def vite-port 5173)

(defonce ^:dynamic *vite-process* (atom nil))

(defn start-vite! []
  (swap! *vite-process*
         (fn [p]
           (if p
             p
             (do
               (println "Starting Vite server...")
               (try
                 (let [proc (h/sh {:root ".build/indigo"
                                   :args ["npm" "run" "dev"]
                                   :inherit true
                                   :wait false})]
                   (println "Vite server started.")
                   proc)
                 (catch Throwable t
                   (println "Failed to start Vite:" t)
                   nil)))))))

(defn stop-vite! []
  (swap! *vite-process*
         (fn [p]
           (when p
             (println "Stopping Vite server...")
             (h/sh-kill p)
             nil))))

(defn proxy-vite [req]
  (let [url (str "http://localhost:" vite-port (:uri req) (if (:query-string req) (str "?" (:query-string req)) ""))
        options {:method (:request-method req)
                 :headers (dissoc (:headers req) "host" "content-length")
                 :body (:body req)
                 :url url
                 :as :stream
                 :follow-redirects false}
        resp @(client/request options)]
    (if (:error resp)
      {:status 404 :body "Not Found (Vite Proxy)"}
      {:status (:status resp)
       :headers (:headers resp)
       :body (:body resp)})))

(defn wrap-browser-call
  [f]
  (fn [req]
    (let [params (try (json/read (:body req))
                      (catch Throwable _ {}))
          req    (assoc req :params params)
          res    (f req)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body    (json/write res)})))

(def api-routes
  (router/router
   (merge
    (api/create-prompt-routes
     "POST /api/translate/"
     {"from-html"       api-task/from-html
      "to-html"         api-task/to-html
      "to-heal"         api-task/to-heal
      "to-js-dsl"       api-task/to-js-dsl
      "to-jsxc-dsl"     api-task/to-jsxc-dsl
      "to-python-dsl"   api-task/to-python-dsl
      "to-plpgsql-dsl"  api-task/to-plpgsql-dsl})
    (api/create-routes
     "POST /api/browse/"
     {"lang/namespaces" (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/list-namespaces) lang))))
      "lang/components" (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")
                                 ns   (get-in req [:params :ns])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/list-components) lang ns))))
      "lang/component"  (wrap-browser-call
                         (fn [req]
                           (let [lang (or (get-in req [:params :lang]) "js")
                                 ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/get-component) lang ns comp))))

      "clj/namespaces"  (wrap-browser-call
                         (fn [req]
                           (require 'indigo.server.api-browser)
                           ((resolve 'indigo.server.api-browser/list-clj-namespaces))))
      "clj/components"  (wrap-browser-call
                         (fn [req]
                           (let [ns (get-in req [:params :ns])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/list-clj-vars) ns))))
      "clj/component"   (wrap-browser-call
                         (fn [req]
                           (let [ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/get-clj-var-source) ns comp))))

      "test/namespaces" (wrap-browser-call
                         (fn [req]
                           (require 'indigo.server.api-browser)
                           ((resolve 'indigo.server.api-browser/list-test-namespaces))))
      "test/components" (wrap-browser-call
                         (fn [req]
                           (let [ns (get-in req [:params :ns])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/list-test-facts) ns))))
      "test/component"  (wrap-browser-call
                         (fn [req]
                           (let [ns   (get-in req [:params :ns])
                                 comp (get-in req [:params :component])]
                             (require 'indigo.server.api-browser)
                             ((resolve 'indigo.server.api-browser/get-test-fact-source) ns comp))))}))))

(def page-routes
  (router/router
   (api/create-routes
    "GET "           
    {"/"             (api/page-handler "Dev Index" page-index/main)
     "/pages/tasks"  (api/page-handler "Dev Tasks" page-tasks/main)
     "/pages/demo"   (api/page-handler "Dev Demo"  page-demo/main)
     "*"             (fn [{:keys [uri] :as req}]
                       (or (router/serve-resource uri *public-path*)
                           (proxy-vite req)))})))

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
  (start-vite!)
  (swap! *instance*
         (fn [stop-fn]
           (when (not stop-fn)
             (http/run-server #'dev-handler {:port *port*})))))

(defn server-stop
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (stop-vite!)
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
