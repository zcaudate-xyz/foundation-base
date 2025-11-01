(ns code.heal.print-test
  (:use code.test)
  (:require [code.heal.print :as print]
            [code.heal.parse :as parse]
            [std.lib :as h]))

^{:refer code.heal.print/print-rainbow :added "4.0"}
(fact "prints out the "
  ^:hidden
  
  (print/print-rainbow
   (h/sys:resource-content "code/heal/cases/002_complex.block")
   (parse/pair-delimiters
    (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block")))))

(comment
  (binding [*ns* (the-ns 'code.heal.print-test)]
    (code.heal.parse/pair-delimiters
     (code.heal.parse/parse-delimiters (std.lib/sys:resource-content "code/heal/cases/002_complex.block"))))
  (print/print-rainbow
   "[[[]"
   (parse/pair-delimiters
    (parse/parse-delimiters "[[[]"))))
