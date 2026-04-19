(ns code.mcp.tool.clj-eval-test
  (:require [code.mcp.tool.clj-eval :as clj-eval])
  (:use code.test))

^{:refer code.mcp.tool.clj-eval/clj-eval-safe :added "4.1"}
(fact "evaluates a valid form"
  (clj-eval/clj-eval-safe "(+ 1 2)")
  => (contains {:ok true
                :text "3"
                :value 3}))

^{:refer code.mcp.tool.clj-eval/clj-eval-safe :added "4.1"}
(fact "returns error info for invalid form"
  (let [result (clj-eval/clj-eval-safe "(not-a-real-function 1)")]
    (:ok result) => false
    (string? (:text result)) => true))

^{:refer code.mcp.tool.clj-eval/clj-eval-fn :added "4.1"}
(fact "wraps eval result for MCP tool response"
  (clj-eval/clj-eval-fn nil {:code "(+ 2 3)"})
  => {:content [{:type "text" :text "5"}]
      :isError false})
