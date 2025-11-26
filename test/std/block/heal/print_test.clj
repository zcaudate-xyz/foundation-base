(ns std.block.heal.print-test
  (:use code.test)
  (:require [std.block.heal.print :as print]
            [std.block.heal.parse :as parse]
            [std.lib :as h]))

^{:refer std.block.heal.print/print-rainbow :added "4.0"}
(fact "prints out the "
  ^:hidden
  
  (print/print-rainbow
   (h/sys:resource-content "code/heal/cases/002_complex.block")
   (parse/pair-delimiters
    (parse/parse-delimiters (h/sys:resource-content "code/heal/cases/002_complex.block")))))

(comment
  (binding [*ns* (the-ns 'std.block.heal.print-test)]
    (std.block.heal.parse/pair-delimiters
     (std.block.heal.parse/parse-delimiters (std.lib/sys:resource-content "code/heal/cases/002_complex.block"))))
  (print/print-rainbow
   "[[[]"
   (parse/pair-delimiters
    (parse/parse-delimiters "[[[]"))))
