(ns code.mcp.tool.tahto-lang-test
  (:require [code.mcp.tool.tahto-lang :as lang.core]
            [code.test :refer :all]))

^{:refer code.mcp.tool.tahto-lang/lang-emit-as-safe :added "4.1"}
(fact "returns emitted code on success and an error string on failure"
  [(re-find #"[+]" (lang.core/lang-emit-as-safe :js "[:+ 1 2]"))
   (re-find #"Error:" (lang.core/lang-emit-as-safe :js "("))]
  => ["+" "Error:"])

^{:refer code.mcp.tool.tahto-lang/lang-emit-as-fn :added "4.1"}
(fact "emits code correctly"
  (lang.core/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.tahto-lang/list-languages-fn :added "4.1"}
(fact "lists languages"
  (lang.core/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.tahto-lang/list-modules-fn :added "4.1"}
(fact "lists modules"
  (lang.core/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))
