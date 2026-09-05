lang_lang_test.clj:1:(ns code.mcp.tool.lang-lang-test
lang_lang_test.clj:2:  (:require [code.mcp.tool.lang-lang :as lang.core]
            [code.test :refer :all]))

lang_lang_test.clj:5:^{:refer code.mcp.tool.lang-lang/lang-emit-as-safe :added "4.1"}
(fact "returns emitted code on success and an error string on failure"
  [(re-find #"[+]" (lang.core/lang-emit-as-safe :js "[:+ 1 2]"))
   (re-find #"Error:" (lang.core/lang-emit-as-safe :js "("))]
  => ["+" "Error:"])

lang_lang_test.clj:11:^{:refer code.mcp.tool.lang-lang/lang-emit-as-fn :added "4.1"}
(fact "emits code correctly"
  (lang.core/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

lang_lang_test.clj:16:^{:refer code.mcp.tool.lang-lang/list-languages-fn :added "4.1"}
(fact "lists languages"
  (lang.core/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

lang_lang_test.clj:21:^{:refer code.mcp.tool.lang-lang/list-modules-fn :added "4.1"}
(fact "lists modules"
  (lang.core/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))
