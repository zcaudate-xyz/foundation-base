(ns code.ai.server.patch
  (:require [mcp-clj.mcp-server.core :as core]
            [mcp-clj.log :as log]
            [clojure.walk :as walk]
            [clojure.set :as set]))

(defn- text-map
  [msg]
  {:type "text" :text msg})

(defn- transform-tool-result
  "Transform tool implementation result into MCP format"
  [result]
  (cond
    ;; Already in MCP format (has :content and :isError)
    (and (contains? result :content) (contains? result :isError))
    result

    ;; Tool implementation returned {:result "..."} format
    (contains? result :result)
    {:content [(text-map (str (:result result)))]
     :isError false}

    ;; Tool implementation returned string directly
    (string? result)
    {:content [(text-map result)]
     :isError false}

    ;; Other formats - convert to string
    :else
    {:content [(text-map (str result))]
     :isError false}))

(defn patched-handle-call-tool
  "Handle tools/call request from client - patched to handle string keys in arguments"
  [server {:keys [name arguments] :as params}]
  (log/info :server/tools-call)
  (let [session-id (-> params meta :session-id)]
    (if-let [{:keys [implementation inputSchema]} (get
                                                    @(:tool-registry server)
                                                    name)]
      (try
        (let [required-keys (set (mapv keyword (:required inputSchema)))
              provided-keys (set (map keyword (keys arguments))) ;; Force keyword keys for check
              missing-args (set/difference required-keys provided-keys)]
          (if (empty? missing-args)
            (let [context {:server server :session-id session-id}
                  arguments (walk/keywordize-keys arguments)] ;; Normalize arguments to keywords
              (transform-tool-result (implementation context arguments)))
            {:content [(text-map
                         (str "Missing args: " (vec missing-args) ", found "
                              (set (keys arguments))))]
             :isError true}))
        (catch Throwable e
          {:content [(text-map (str "Error: " (.getMessage e)))]
           :isError true}))
      {:content [(text-map (str "Tool not found: " name))]
       :isError true})))

(defn apply-patch []
  (alter-var-root #'core/handle-call-tool (constantly patched-handle-call-tool))
  (println "Applied mcp-clj handle-call-tool patch for Antigravity compatibility."))
