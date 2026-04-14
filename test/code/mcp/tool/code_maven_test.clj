(ns code.mcp.tool.code-maven-test
  (:require [code.mcp.tool.code-maven :as tool]
            [code.test :refer :all]))

^{:refer code.mcp.tool.code-maven/code-maven-fn :added "4.1"}
(fact "dispatches to code.tool.maven operations with parsed inputs"
  (let [calls (atom nil)]
    (with-redefs [code.tool.maven/install (fn [target opts]
                                            (reset! calls [target opts])
                                            "install summary")]
      [(tool/code-maven-fn nil {:task "install"
                                :target "[xyz.zcaudate]"
                                :options "{:tag :all}"})
       @calls]))
  => [{:content [{:type "text" :text "install summary"}]
       :isError false}
      [[xyz.zcaudate]
       {:print {:function false
                :summary false
                :result false
                :item false}
        :tag :all}]])

^{:refer code.mcp.tool.code-maven/code-maven-fn :added "4.1"}
(fact "returns an error for unknown code.tool.maven operations"
  (tool/code-maven-fn nil {:task "missing-op"})
  => {:content [{:type "text" :text "Maven task not found: missing-op"}]
      :isError true})
