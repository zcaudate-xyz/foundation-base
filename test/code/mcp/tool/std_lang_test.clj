(ns code.mcp.tool.std-lang-test
  (:require [code.mcp.tool.std-lang :as std-lang]
            [code.test :refer :all]
            [std.lang :as l]
            [std.lang.base.library :as lib]))

^{:refer code.mcp.tool.std-lang/lang-emit-as-fn :added "0.1"}
(fact "emits code correctly"
  (std-lang/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.std-lang/list-languages-fn :added "0.1"}
(fact "lists languages"
  (std-lang/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.std-lang/list-modules-fn :added "0.1"}
(fact "lists modules"
  (std-lang/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))


^{:refer code.mcp.tool.std-lang/lang-emit-as-safe :added "4.0"}
(fact "returns emitted code on success and an error string on failure"
  [(re-find #"[+]" (std-lang/lang-emit-as-safe :js "[:+ 1 2]"))
   (re-find #"Error:" (std-lang/lang-emit-as-safe :js "("))]
  => ["+" "Error:"])
