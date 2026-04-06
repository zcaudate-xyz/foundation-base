(ns code.mcp.tool.jvm-namespace-test
  (:require [code.mcp.tool.jvm-namespace :as tool]
            [code.test :refer :all]))

^{:refer code.mcp.tool.jvm-namespace/jvm-namespace-fn :added "4.1"}
(fact "dispatches to jvm.namespace operations with parsed inputs"
  (let [calls (atom nil)]
    (with-redefs [jvm.namespace/list-aliases (fn [target opts]
                                               (reset! calls [target opts])
                                               "alias summary")]
      [(tool/jvm-namespace-fn nil {:operation "list-aliases"
                                   :target "[jvm.namespace]"
                                   :options "{:return :summary}"})
       @calls]))
  => [{:content [{:type "text" :text "alias summary"}]
       :isError false}
      [[jvm.namespace]
       {:print {:function false
                :summary false
                :result false
                :item false}
        :return :summary}]])

^{:refer code.mcp.tool.jvm-namespace/jvm-namespace-fn :added "4.1"}
(fact "returns an error for unknown jvm.namespace operations"
  (tool/jvm-namespace-fn nil {:operation "missing-op"})
  => {:content [{:type "text" :text "jvm.namespace operation not found: missing-op"}]
      :isError true})
