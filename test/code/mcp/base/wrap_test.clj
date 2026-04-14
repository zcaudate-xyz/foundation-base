(ns code.mcp.base.wrap-test
  (:require [code.mcp.base.wrap :as wrap]
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

^{:refer code.mcp.base.wrap/tool->sdk-tool :added "4.1"}
(fact "tool metadata converts to sdk tool"
  (let [tool (wrap/tool->sdk-tool sample-tool)]
    [(.name tool) (.description tool)])
  => ["echo" "Echo text"])

^{:refer code.mcp.base.wrap/clj-tool->spec :added "4.1"}
(fact "tool specs invoke clojure handlers"
  (let [spec (wrap/clj-tool->spec sample-tool)
        result (.apply (.call spec) nil {"text" "hello"})]
    [(instance? McpSchema$CallToolResult result)
     (-> result .content first .text)
     (.isError result)])
  => [true "hello" false])

^{:refer code.mcp.base.wrap/text-content :added "4.1"}
(fact "creates text content values"
  (-> (wrap/text-content 42) .text)
  => "42")

^{:refer code.mcp.base.wrap/call-tool-result :added "4.1"}
(fact "normalizes tool result content into MCP text content"
  (let [result (wrap/call-tool-result {:content ["hello" {:type "text" :text "world"}]
                                       :structuredContent {:ok true}})]
    [(mapv #(.text %) (.content result))
     (.isError result)])
  => [["hello" "world"] false])

^{:refer code.mcp.base.wrap/error-result :added "4.1"}
(fact "creates error call tool results"
  (let [result (wrap/error-result "boom")]
    [(-> result .content first .text)
     (.isError result)])
  => ["boom" true])

^{:refer code.mcp.base.wrap/tool-handler :added "4.1"}
(fact "wraps implementation functions and converts thrown errors"
  (let [ok (.apply (wrap/tool-handler sample-tool) nil {"text" "hello"})
        bad (.apply (wrap/tool-handler {:name "explode"
                                        :implementation (fn [_ _]
                                                          (throw (ex-info "boom" {})))})
                    nil
                    {})]
    [(-> ok .content first .text)
     (-> bad .content first .text)
     (.isError bad)])
  => ["hello" "boom" true])
