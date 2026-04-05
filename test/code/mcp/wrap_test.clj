(ns code.mcp.wrap-test
  (:require [code.mcp.wrap :as wrap]
            [code.test :refer :all])
  (:import [io.modelcontextprotocol.spec McpSchema$CallToolResult]))

(def sample-tool
  {:name "echo"
   :description "Echo text"
   :inputSchema {:type "object"
                 :properties {"text" {:type "string"}}
                 :required ["text"]}
   :implementation (fn [_ {:keys [text]}]
                     {:content [{:type "text" :text text}]
                      :isError false})})

^{:refer code.mcp.wrap/tool->sdk-tool :added "4.1"}
(fact "tool metadata converts to sdk tool"
  (let [tool (wrap/tool->sdk-tool sample-tool)]
    [(.name tool) (.description tool)])
  => ["echo" "Echo text"])

^{:refer code.mcp.wrap/clj-tool->spec :added "4.1"}
(fact "tool specs invoke clojure handlers"
  (let [spec (wrap/clj-tool->spec sample-tool)
        result (.apply (.call spec) nil {"text" "hello"})]
    [(instance? McpSchema$CallToolResult result)
     (-> result .content first .text)
     (.isError result)])
  => [true "hello" false])
