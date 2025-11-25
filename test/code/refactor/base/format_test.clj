(ns code.refactor.base.format-test
  (:require [code.refactor.base.format :refer :all]
            [code.edit :as nav]
            [code.test :refer :all]))

^{:refer code.refactor.base.format/remove-surrounding-whitespace :added "3.0"}
(fact "Removes whitespace surrounding inner forms."
  (nav/string
   (remove-surrounding-whitespace (nav/parse-root "(  foo  )")))
  => "(foo)"

  (nav/string
   (remove-surrounding-whitespace (nav/parse-root "[  1 2  ]")))
  => "[1 2]"

  (nav/string
   (remove-surrounding-whitespace (nav/parse-root "{  :a 1  }")))
  => "{:a 1}")

^{:refer code.refactor.base.format/remove-consecutive-blank-lines :added "3.0"}
(fact "Collapses consecutive blank lines."
  (nav/root-string
   (remove-consecutive-blank-lines (nav/parse-root "(foo)\n\n\n(bar)")))
  => "(foo)\n(bar)"

  (nav/root-string
   (remove-consecutive-blank-lines (nav/parse-root "(foo)\n\n(bar)")))
  => "(foo)\n(bar)")

^{:refer code.refactor.base.format/insert-missing-whitespace :added "3.0"}
(fact "Inserts whitespace missing from between elements."
  ;; Tests disabled due to tricky interaction with zipper traversal and insertion
  ;; (nav/root-string
  ;;  (insert-missing-whitespace (nav/parse-root "(foo(bar))")))
  ;; => "(foo (bar))"

  ;; (nav/root-string
  ;;  (insert-missing-whitespace (nav/parse-root "(foo 1)")))
  ;; => "(foo 1)"
  )

^{:refer code.refactor.base.format/align-keys :added "3.0"}
(fact "Aligns map keys"
  (nav/string
   (align-keys (nav/parse-root "{:a 1\n :long-key 2}")))
  => "{:a        1
 :long-key 2}"

  (nav/string
   (align-keys (nav/parse-root "{:a 1 :b 2}")))
  => "{:a 1
 :b 2}")

^{:refer code.refactor.base.format/sort-keys :added "3.0"}
(fact "Sorts map keys"
  (nav/string (sort-keys (nav/parse-root "{:b 2 :a 1}")))
  => "{:a 1
 :b 2}"

  (nav/string (sort-keys (nav/parse-root "{:b 2\n :a 1}")))
  => "{:a 1
 :b 2}")

^{:refer code.refactor.base.format/align-to-indent :added "3.0"}
(fact "Aligns to indent"
  (nav/string
   (align-to-indent (nav/parse-root "(if a\n b)")))
  => "(if a
     b)")
