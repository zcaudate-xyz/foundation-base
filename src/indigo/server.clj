(ns indigo.server
  (:require [mcp-clj.http-server.adapter :as http-server]
            [mcp-clj.sse :as sse]
            [mcp-clj.json :as json]
            [mcp-clj.json-rpc.executor :as executor]
            [mcp-clj.json-rpc.json-protocol :as json-protocol]
            [mcp-clj.mcp-server.core :as mcp-server]
            [mcp-clj.tools.clj-eval :as clj-eval]
            [code.ai.server.tool.code-test :as code-test]
            [code.ai.server.tool.code-doc :as code-doc]
            [code.ai.server.tool.code-manage :as code-manage]
            [indigo.tool.inspect :as inspect]
            [indigo.tool.explore :as explore]
            [indigo.repl :as repl]
            [indigo.watcher :as watcher]
            [indigo.event :as event]
            [indigo.templates :as templates]
            [std.lib.component :as component]
            [clojure.core.async :refer [go-loop alts!]]))

(defonce ^:dynamic *server* (atom nil))
(defonce ^:dynamic *mcp-server* (atom nil))
(defonce ^:dynamic *event-bus* (atom nil))
(defonce ^:dynamic *watcher* (atom nil))

(defn create-events-handler [event-bus]
  (fn [request]
    (let [{:keys [reply! close! response]} (sse/handler request)
          repl-sub (event/subscribe event-bus :repl)
          watcher-sub (event/subscribe event-bus :watcher)
          templates-sub (event/subscribe event-bus :templates)]
      (go-loop [msg (alts! [repl-sub watcher-sub templates-sub])]
        (if msg
          (do
            (reply! (sse/message (json/write msg)))
            (recur (alts! [repl-sub watcher-sub templates-sub])))
          (close!)))
      response)))

(defn create-server
  []
  (let [event-bus (event/create-event-bus)
        _ (reset! *event-bus* event-bus)
        mcp-server-instance (mcp-server/create-server
                             {:transport {:type :in-memory}
                              :tools {
                                      "code-test" {:name "code-test"
                                                   :description "Runs tests"
                                                   :inputSchema {:type "object"
                                                                 :properties {"ns" {:type "string"}}
                                                                 :required ["ns"]}
                                                   :implementation code-test/run-tests-tool}
                                      "code-doc-init" {:name "code-doc-init"
                                                       :description "Initializes a code-doc template"
                                                       :inputSchema {:type "object"
                                                                     :properties {"site" {:type "string"}}
                                                                     :required ["site"]}
                                                       :implementation code-doc/init-template-tool}
                                      "code-doc-deploy" {:name "code-doc-deploy"
                                                         :description "Deploys a code-doc template"
                                                         :inputSchema {:type "object"
                                                                       :properties {"site" {:type "string"}}
                                                                       :required ["site"]}
                                                         :implementation code-doc/deploy-template-tool}
                                      "code-doc-publish" {:name "code-doc-publish"
                                                          :description "Publishes a code-doc template"
                                                          :inputSchema {:type "object"
                                                                        :properties {"site" {:type "string"}}
                                                                        :required ["site"]}
                                                          :implementation code-doc/publish-tool}
                                      "code-manage" {:name "code-manage"
                                                     :description "Runs a code-manage task"
                                                     :inputSchema {:type "object"
                                                                   :properties {"task" {:type "string"}
                                                                                "params" {:type "string" :description "EDN string of params"}}
                                                                   :required ["task"]}
                                                     :implementation code-manage/manage-tool}
                                      "inspect-var" {:name "inspect-var"
                                                     :description "Inspects a var"
                                                     :inputSchema {:type "object"
                                                                   :properties {"var-name" {:type "string"}}
                                                                   :required ["var-name"]}
                                                     :implementation inspect/inspect-var-tool}
                                      "list-vars-and-tests" {:name "list-vars-and-tests"
                                                             :description "Lists all vars and their tests in a namespace"
                                                             :inputSchema {:type "object"
                                                                           :properties {"namespace" {:type "string"}}
                                                                           :required ["namespace"]}
                                                             :implementation inspect/list-vars-and-tests-tool}
                                      "apply-template" {:name "apply-template"
                                                        :description "Applies a code template"
                                                        :inputSchema {:type "object"
                                                                      :properties {"template-name" {:type "string"}
                                                                                   "values" {:type "string" :description "EDN string of values"}}
                                                                      :required ["template-name" "values"]}
                                                        :implementation (fn [ctx args]
                                                                          (templates/apply-template-tool @*event-bus* args))}
                                      "explore-resources" {:name "explore-resources"
                                                           :description "Lists all available resources"
                                                           :inputSchema {:type "object"
                                                                         :properties {}}
                                                           :implementation explore/explore-resources-tool}
                                      "inspect-resource" {:name "inspect-resource"
                                                          :description "Gets the details of a specific resource"
                                                          :inputSchema {:type "object"
                                                                        :properties {"resource-name" {:type "string"}}
                                                                        :required ["resource-name"]}
                                                          :implementation explore/inspect-resource-tool}
                                      }})
        _ (reset! *mcp-server* mcp-server-instance)
        handler (fn [{:keys [request-method uri] :as request}]
                  (cond
                    (= uri "/events") ((create-events-handler event-bus) request)
                    (= uri "/") {:status 200
                                 :headers {"Content-Type" "text/html"}
                                 :body (slurp "resources/public/index.html")}
                    (.startsWith uri "/main.js") {:status 200
                                                  :headers {"Content-Type" "application/javascript"}
                                                  :body (slurp (str "resources/public" uri))}
                    :else (let [body (json/parse (slurp (:body request)))
                                method (get body "method")]
                            (if (= method "eval-repl")
                              (do (repl/evaluate-repl-command event-bus (get-in body ["params" "command"]))
                                  {:status 200 :body "OK"})
                              ((@(:json-rpc-server @*mcp-server*)) request)))))
        http-server (http-server/run-server handler {:port 3002})]
    (assoc http-server :mcp-server mcp-server-instance
                       :event-bus event-bus)))


(defn start-server
  []
  (swap! *server*
         (fn [val]
           (if val
             val
             (let [server (create-server)]
               (component/start (:event-bus server))
               (reset! *watcher* (watcher/start-watcher! ["src"] @*event-bus*))
               server)))))

(defn stop-server
  []
  (swap! *server*
         (fn [val]
           (when val
             ((:stop val))
             ((-> val :mcp-server :stop))
             (watcher/stop-watcher! @*watcher*)
             (component/stop (:event-bus val)))
           nil)))

(defn wait []
  (while true
    (Thread/sleep 1000)))

(defn -main
  [& args]
  (start-server)
  (wait))

(comment
  (start-server)
  (stop-server))
