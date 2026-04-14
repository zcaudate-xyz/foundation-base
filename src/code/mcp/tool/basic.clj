(ns code.mcp.tool.basic)

(defn echo-fn
  [_ {:keys [text]}]
  {:content [{:type "text" :text text}]
   :isError false})

(def echo-tool
  {:name "echo"
   :description "Return the provided text unchanged. Use this as a minimal connectivity and payload-shape check when wiring up a new MCP client."
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"
                                      :description "The exact text payload to echo back."}}
                 :required ["text"]}
   :implementation #'echo-fn})

(defn ping-fn
  [_ _]
  {:content [{:type "text" :text "ping"}]
   :isError false})

(def ping-tool
  {:name "ping"
   :description "Return a simple `ping` response so an MCP client can verify that the server is alive before invoking project tools."
   :inputSchema {:type "object"
                 :properties {}}
   :implementation #'ping-fn})
