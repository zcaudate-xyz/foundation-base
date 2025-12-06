(ns code.tool.refactor.base.expression-test
  (:require [code.tool.refactor.base.expression :refer :all]
            [std.block.navigate :as nav]
            [code.test :refer :all]))

^{:refer code.tool.refactor.base.expression/rewrite-if-not :added "3.0"}
(fact "rewrites (if (not ...) ...)"
  (nav/string
   (rewrite-if-not (nav/parse-root "(if (not a) b c)")))
  => "(if-not a b c)"

  (nav/string
   (rewrite-if-not (nav/parse-root "(if (not a) b)")))
  => "(if-not a b)")

^{:refer code.tool.refactor.base.expression/rewrite-when-not :added "3.0"}
(fact "rewrites (when (not ...) ...)"
  (nav/string
   (rewrite-when-not (nav/parse-root "(when (not a) b)")))
  => "(when-not a b)"

  (nav/string
   (rewrite-when-not (nav/parse-root "(when (not a) b c)")))
  => "(when-not a b c)")

^{:refer code.tool.refactor.base.expression/rewrite-not-empty :added "3.0"}
(fact "rewrites (not (empty? ...))"
  (nav/string
   (rewrite-not-empty (nav/parse-root "(not (empty? a))")))
  => "(seq a)")

^{:refer code.tool.refactor.base.expression/rewrite-not-seq :added "3.0"}
(fact "rewrites (not (seq ...))"
  (nav/string
   (rewrite-not-seq (nav/parse-root "(not (seq a))")))
  => "(empty? a)")

^{:refer code.tool.refactor.base.expression/rewrite-if-to-when :added "3.0"}
(fact "rewrites (if ... (do ...))"
  (nav/string
   (rewrite-if-to-when (nav/parse-root "(if a (do b))")))
  => "(when a b)"

  (nav/string
   (rewrite-if-to-when (nav/parse-root "(if a (do b c))")))
  => "(when a b c)"

  (nav/string
   (rewrite-if-to-when (nav/parse-root "(if a (do b) c)")))
  => "(if a (do b) c)")
