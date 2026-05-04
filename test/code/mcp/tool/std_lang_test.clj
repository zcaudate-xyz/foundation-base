(ns code.mcp.tool.hara.lang-test
  (:require [code.mcp.tool.hara.lang :as hara.lang]
            [code.test :refer :all]
            [hara.lang :as l]
            [hara.lang.library :as lib]))

^{:refer code.mcp.tool.hara.lang/lang-emit-as-safe :added "4.0"}
(fact "returns emitted code on success and an error string on failure"
  [(re-find #"[+]" (hara.lang/lang-emit-as-safe :js "[:+ 1 2]"))
   (re-find #"Error:" (hara.lang/lang-emit-as-safe :js "("))]
  => ["+" "Error:"])

^{:refer code.mcp.tool.hara.lang/lang-emit-as-fn :added "0.1"}
(fact "emits code correctly"
  (hara.lang/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.hara.lang/list-languages-fn :added "0.1"}
(fact "lists languages"
  (hara.lang/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.mcp.tool.hara.lang/list-modules-fn :added "0.1"}
(fact "lists modules"
  (hara.lang/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))