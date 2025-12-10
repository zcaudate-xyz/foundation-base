(ns indigo.server
  (:require [clojure.string :as string]
            [org.httpkit.server :as http]
            [org.httpkit.client :as client]
            [net.http.router :as router]
            [indigo.server.api-browser :as api-browser]
            [indigo.server.test-runner :as test-runner]
            [indigo.server.watcher :as watcher]
            [indigo.server.dispatch :as dispatch]
            [indigo.server.api-translate :as api-translate]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]
            [code.project :as project])
  (:import (java.awt Desktop)
           (java.net URI)))

(defonce *instance* (atom nil))

(def ^:dynamic *port* 1311)

(def ^:dynamic *public-path* "src-js/indigo/dist")

(defn wrap-browser-call
  [f]
  (fn [req]
    (let [params (try (json/read (:body req) json/+keyword-case-mapper+)
                      (catch Throwable t
                        (println "JSON Parse Error:" (.getMessage t))
                        {}))
          ;; _ (println "Request Body:" (:body req))
          ;; _ (println "Parsed Params:" params)
          req    (assoc req :params params)
          res    (f req)]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body    (json/write res)})))

(defn create-routes
  [prefix routes]
  (h/map-keys (fn [k]
                (str prefix k))
              routes))

(defn- extract [req param]
  (if (vector? param)
    (let [[key default] param]
      (or (get-in req [:params key]) default))
    (get-in req [:params param])))

(defn- endpoint [f & params]
  (wrap-browser-call
   (fn [req]
     (apply f (map #(extract req %) params)))))

(def api-routes
  (router/router
   (merge
    (create-routes
     "POST /api/browse/"
     {"libraries"                 (endpoint #'api-browser/list-libraries)
      "scan"                      (endpoint #'api-browser/scan-namespaces)
      "file-content"              (endpoint #'api-browser/get-file-content :path)

      ;; Clojure Lang
      "clj/namespace-entries"     (endpoint #'api-browser/get-namespace-entries :ns)
      "clj/namespaces"            (endpoint #'api-browser/list-clj-namespaces)
      "clj/namespace-source"      (endpoint #'api-browser/get-namespace-source :ns)
      "clj/components"            (endpoint #'api-browser/list-clj-vars :ns)
      "clj/component"             (endpoint #'api-browser/get-clj-var-source :ns :component)
      "clj/var-tests"             (endpoint #'api-browser/list-tests-for-var :ns :var)
      "clj/save-namespace-source" (endpoint #'api-browser/save-namespace-source :ns :source)
      "clj/completions"           (endpoint #'api-browser/get-completions :ns :prefix)
      "clj/scaffold-test"         (endpoint #'api-browser/scaffold-test :ns)
      "clj/doc-path"              (endpoint #'api-browser/get-doc-path :ns)
      "clj/file-content"          (endpoint #'api-browser/get-file-content :path)
      "clj/delete-path"           (endpoint #'api-browser/delete-path :path)

      ;; Std Lang
      "lang/namespaces"           (endpoint #'api-browser/list-namespaces [:lang "js"])
      "lang/components"           (endpoint #'api-browser/list-components [:lang "js"] :ns)
      "lang/component-preview"    (endpoint #'api-browser/component-preview [:lang "js"] :ns :component)
      "lang/emit-component"       (endpoint #'api-browser/emit-component [:lang "js"] :ns :component)
      "lang/component"            (endpoint #'api-browser/get-component [:lang "js"] :ns :component)

      ;; Tests
      "test/namespaces"           (endpoint #'api-browser/list-test-namespaces)
      "test/components"           (endpoint #'api-browser/list-test-facts :ns)
      "test/component"            (endpoint #'api-browser/get-test-fact-source :ns :component)
      "test/run-var"              (endpoint #'test-runner/run-test :ns :var)
      "test/run-ns"               (endpoint #'test-runner/run-ns-tests :ns)
      
      ;; Translate
      "translate/to-heal"         (endpoint #'api-translate/to-heal :source)
      "translate/from-html"       (endpoint #'api-translate/from-html :html)
      "translate/to-html"         (endpoint #'api-translate/to-html :dsl)}))))

(def page-routes
  (router/router
   (create-routes
    "GET "
    {#_#_"/pages/demo"   (api/page-handler "Dev Demo"  page-demo/main)
     "*"             (fn [{:keys [uri] :as req}]
                       (router/serve-resource uri *public-path*))})))

(defn repl-handler [req]
  (http/with-channel req channel
    (http/on-close channel (fn [status]
                             (dispatch/unregister! channel)
                             (println "REPL Client disconnected" status)))
    (http/on-receive channel (fn [data]
                               (if (= data "ping")
                                 (dispatch/send! channel "pong")
                                 (do
                                   (println "REPL Received:" data)
                                   (let [json-data (try (json/read data json/+keyword-case-mapper+)
                                                        (catch Throwable _ nil))]
                                     (if (and json-data (:id json-data) (:code json-data))
                                       ;; Handle JSON request with ID
                                       (try
                                         (let [form (read-string (:code json-data))
                                               ns-str (:ns json-data)
                                               target-ns (if ns-str (symbol ns-str) 'user)
                                               _ (when ns-str (require target-ns)) ;; Ensure NS is loaded
                                               _ (h/prn form ns-str)
                                               result (with-out-str
                                                        (binding [*out* *out*
                                                                  *ns* (or (find-ns target-ns) (create-ns target-ns))]
                                                          (let [res (eval form)]
                                                            (print res))))] ;; Use print to avoid newline
                                           (dispatch/send! channel {:id (:id json-data)
                                                                    :result result
                                                                    :type "eval-result"}))
                                         (catch Throwable t
                                           (dispatch/send! channel {:id (:id json-data)
                                                                    :error (.getMessage t)
                                                                    :type "eval-error"})))
                                       ;; Handle legacy raw string request
                                       (try
                                         (let [form (read-string data)
                                               result (with-out-str
                                                        (binding [*out* *out*]
                                                          (let [res (eval form)]
                                                            (println res))))]
                                           (dispatch/send! channel result))
                                         (catch Throwable t
                                           (dispatch/send! channel (str "Error: " (.getMessage t)))))))))))
    (dispatch/register! channel)
    (println "REPL Client connected")))


(defn dev-handler
  [req]
  (if (= (:uri req) "/repl")
    (repl-handler req)
    (let [{:keys [uri request-method ^org.httpkit.BytesInputStream body]} req
          body (if body
                 (String. (. body (readAllBytes))))
          req  (assoc req :body body)]
      (or (#'api-routes  req)
          (#'page-routes req)
          {:status 404 :body "Not Found"}))))

(defn server-stop
  "Stops the HTTP server"
  {:added "4.0"}
  ([]
   (swap! *instance*
          (fn [stop-fn]
            (when stop-fn (stop-fn :timeout 100))
            nil))
   (watcher/stop-watcher)))

(defn server-start
  []
  (watcher/start-watcher)
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

(comment)
