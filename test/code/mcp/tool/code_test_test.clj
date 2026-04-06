(ns code.mcp.tool.code-test-test
  (:require [code.mcp.tool.code-test :as tool]
            [code.test :refer :all]))

^{:refer code.mcp.tool.code-test/code-test-fn :added "4.1"}
(fact "dispatches to code.test.task with parsed EDN inputs"
  (let [calls (atom nil)]
    (with-redefs [code.test.task/run (fn [target opts]
                                       (reset! calls [target opts])
                                       "test summary")]
      [(tool/code-test-fn nil {:task "run"
                               :target "code.mcp.server-test"
                               :options "{:timeout 1000}"})
       @calls]))
  => [{:content [{:type "text" :text "test summary"}]
       :isError false}
      [code.mcp.server-test
       {:print {:function false
                :summary false
                :result false
                :item false}
        :timeout 1000}]])

^{:refer code.mcp.tool.code-test/code-test-fn :added "4.1"}
(fact "returns an error for unknown code.test operations"
  (tool/code-test-fn nil {:task "missing-op"})
  => {:content [{:type "text" :text "Test task not found: missing-op"}]
      :isError true})
