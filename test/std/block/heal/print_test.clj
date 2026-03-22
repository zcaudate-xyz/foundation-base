(ns std.block.heal.print-test
  (:require [std.block.heal.parse :as parse]
            [std.block.heal.print :as print]
            [std.lib.env])
  (:use code.test))

^{:refer std.block.heal.print/print-rainbow :added "4.0"}
(fact "prints out the "
  ^:hidden

  (std.lib.env/with-out-str
    (print/print-rainbow
     (slurp "test-data/std.block.heal/cases/002_complex.block")
     (parse/pair-delimiters
      (parse/parse-delimiters (slurp "test-data/std.block.heal/cases/002_complex.block")))))
  => string?)

(comment
  (binding [*ns* (the-ns 'std.block.heal.print-test)]
    (std.block.heal.parse/pair-delimiters
     (std.block.heal.parse/parse-delimiters (std.lib.env/sys:resource-content "code/heal/cases/002_complex.block"))))
  (print/print-rainbow
   "[[[]"
   (parse/pair-delimiters
    (parse/parse-delimiters "[[[]"))))
