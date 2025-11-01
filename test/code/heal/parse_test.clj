(ns code.heal.parse-test
  (:use code.test)
  (:require [code.heal.parse :as parse]
            [std.lib :as h]))

;; This is a test file


^{:refer code.heal.parse/catalog-delimiters-in-string :added "4.0"}
(fact "gets all the delimiters in the file"
  ^:hidden

  (parse/catalog-delimiters-in-string
   (h/sys:resource-content "code/heal/cases/001_basic.block")))


^{:refer code.heal.parse/print-delimiters :added "4.0"}
(fact "TODO"
  ^:hidden
  
  (parse/print-delimiters
   (h/sys:resource-content "code/heal/cases/001_basic.block")
   (parse/catalog-delimiters-in-string
    (h/sys:resource-content "code/heal/cases/001_basic.block"))
   {:line-numbers true}))
