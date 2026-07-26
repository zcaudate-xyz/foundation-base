tahto_lang_test.clj:1:(ns code.mcp.tool.tahto-lang-test
tahto_lang_test.clj:2:  (:require [code.mcp.tool.tahto-lang :as tahto.core]
            [code.test :refer :all]))

tahto_lang_test.clj:5:^{:refer code.mcp.tool.tahto-lang/lang-emit-as-safe :added "4.1"}
(fact "returns emitted code on success and an error string on failure"
  [(re-find #"[+]" (tahto.core/lang-emit-as-safe :js "[:+ 1 2]"))
   (re-find #"Error:" (tahto.core/lang-emit-as-safe :js "("))]
  => ["+" "Error:"])

tahto_lang_test.clj:11:^{:refer code.mcp.tool.tahto-lang/lang-emit-as-fn :added "4.1"}
(fact "emits code correctly"
  (tahto.core/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

tahto_lang_test.clj:16:^{:refer code.mcp.tool.tahto-lang/list-languages-fn :added "4.1"}
(fact "lists languages"
  (tahto.core/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

tahto_lang_test.clj:21:^{:refer code.mcp.tool.tahto-lang/list-modules-fn :added "4.1"}
(fact "lists modules"
  (tahto.core/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))
