(ns code.mcp.tool.std-lang-manage-test
  (:require [code.mcp.tool.std-lang-manage :as tool]
            [code.test :refer :all]))

^{:refer code.mcp.tool.std-lang-manage/std-lang-manage-fn :added "4.1"}
(fact "dispatches std.lang.manage tasks with default autopilot params"
  (let [calls (atom nil)]
    (with-redefs [std.lang.manage/+tasks+ {:inventory (fn [params]
                                                        (reset! calls params)
                                                        "inventory summary")}]
      [(tool/std-lang-manage-fn nil {:task "inventory"})
       @calls]))
  => [{:content [{:type "text" :text "inventory summary"}]
       :isError false}
      {:print {:function false
               :item false
               :result false
               :summary false}}])

^{:refer code.mcp.tool.std-lang-manage/std-lang-manage-fn :added "4.1"}
(fact "passes EDN vector arguments directly to std.lang.manage tasks"
  (let [calls (atom nil)]
    (with-redefs [std.lang.manage/+tasks+ {:support-matrix (fn [params]
                                                             (reset! calls params)
                                                             "support summary")}]
      [(tool/std-lang-manage-fn nil {:task "support-matrix"
                                     :args "[{:langs [:js :lua]}]"})
       @calls]))
  => [{:content [{:type "text" :text "support summary"}]
       :isError false}
      {:langs [:js :lua]}])
