(ns code.ai.server.tool.basic)

(defn echo-fn
  [_ {:keys [text]}]
  {:content [{:type "text" :text text}]
   :isError false})

(def echo-tool
  {:name "echo"
   :description "Echo the input text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation #'echo-fn})

(defn ping-fn
  [_ _]
  {:content [{:type "text" :text "ping"}]
   :isError false})

(def ping-tool
  {:name "ping"
   :description "Ping the input text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation #'ping-fn})
