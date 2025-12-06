(ns code.ai.heal.form-edits-test
  (:use code.test)
  (:require [code.ai.heal.form-edits :refer :all]
            [std.block.navigate :as nav]))

^{:refer code.ai.heal.form-edits/fix:namespaced-symbol-no-dot :added "4.1"}
(fact "fixes namespaced symbols without dot"
  ^:hidden
  
  (-> (nav/parse-root "std.lib/hello.world")
      (fix:namespaced-symbol-no-dot)
      (nav/root-string))
  => "(. std.lib/hello world)")

^{:refer code.ai.heal.form-edits/fix:dash-indexing :added "4.1"}
(fact "fixes dash indexing"
  ^:hidden
  
  (-> (nav/parse-root "(obj -field)")
      (fix:dash-indexing)
      (nav/root-string))
  => "(obj field)")

^{:refer code.ai.heal.form-edits/fix:set-arg-destructuring :added "4.1"}
(fact "fixes set arg destructuring"
  ^:hidden
  
  (-> (nav/parse-root "#{:# [:c]}")
      (fix:set-arg-destructuring)
      (nav/root-string))
  => "{:# [:c]}")

^{:refer code.ai.heal.form-edits/fix:remove-fg-extra-references :added "4.1"}
(fact "removes fg extra references"
  ^:hidden
  
  (-> (nav/parse-root "[js.lib.sonner]")
      (fix:remove-fg-extra-references)
      (nav/root-string))
  => "")

^{:refer code.ai.heal.form-edits/fix:replace-fg-extra-namepspaces :added "4.1"}
(fact "replaces fg extra namespaces"
  ^:hidden
  
  (-> (nav/parse-root "imf/hello")
      (fix:replace-fg-extra-namepspaces)
      (nav/root-string))
  => "fg/hello")

^{:refer code.ai.heal.form-edits/fix:remove-mistranslated-syms :added "4.1"}
(fact "removes mistranslated symbols"
  ^:hidden
  
  (-> (nav/parse-root "</>")
      (fix:remove-mistranslated-syms)
      (nav/root-string))
  => "")
