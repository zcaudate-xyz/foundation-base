(ns code.ai.server.tool.std-lang-test
  (:require [code.test :refer :all]
            [code.ai.server.tool.std-lang :as std-lang]
            [std.lang :as l]
            [std.lang.base.library :as lib]))

^{:refer code.ai.server.tool.std-lang/lang-emit-as-fn :added "0.1"}
(fact "emits code correctly"
  (std-lang/lang-emit-as-fn nil {:type "js" :code "[:+ 1 2]"})
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.ai.server.tool.std-lang/list-languages-fn :added "0.1"}
(fact "lists languages"
  (std-lang/list-languages-fn nil nil)
  => (contains {:content (contains [(contains {:text string?})])}))

^{:refer code.ai.server.tool.std-lang/list-modules-fn :added "0.1"}
(fact "lists modules"
  (std-lang/list-modules-fn nil {:lang "js"})
  => (contains {:content (contains [(contains {:text string?})])}))
