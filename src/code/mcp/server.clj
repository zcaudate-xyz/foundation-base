(ns code.mcp.server
  (:require [code.mcp.wrap :as wrap])
  (:import [io.modelcontextprotocol.server McpServer McpSyncServer]
           [io.modelcontextprotocol.server.transport StdioServerTransportProvider]))

(defn create-transport-provider
  [{:keys [provider type]}]
  (or provider
      (case (or type :stdio)
        :stdio (StdioServerTransportProvider.)
        (throw (ex-info "Unsupported transport type"
                        {:type type})))) )

(defn create-server
  ([]
   (create-server {}))
  ([{:keys [transport tools server-info instructions immediate-execution]
     :or {transport {:type :stdio}
          tools []
          server-info {:name "foundation-base" :version "0.1.0"}}}]
   (let [provider (create-transport-provider transport)
         spec (McpServer/sync provider)
         spec (.serverInfo spec (:name server-info) (:version server-info))
         spec (cond-> spec
                instructions (.instructions instructions)
                true (.immediateExecution (boolean immediate-execution)))
         spec (reduce (fn [server-spec tool]
                        (.tool server-spec
                               (wrap/tool->sdk-tool tool)
                               (wrap/tool-handler tool)))
                      spec
                      tools)
         server (.build spec)]
     {:provider provider
      :server server
      :tool-specs (atom (into {}
                              (map (fn [tool]
                                     [(:name tool) (wrap/clj-tool->spec tool)]))
                              tools))})))

(defn add-tool!
  [{:keys [server tool-specs]} tool]
  (let [spec (wrap/clj-tool->spec tool)]
    (.addTool ^McpSyncServer server spec)
    (swap! tool-specs assoc (:name tool) spec)
    spec))

(defn remove-tool!
  [{:keys [server tool-specs]} tool-name]
  (.removeTool ^McpSyncServer server tool-name)
  (swap! tool-specs dissoc tool-name)
  tool-name)

(defn close!
  [{:keys [server]}]
  (.closeGracefully ^McpSyncServer server)
  nil)
