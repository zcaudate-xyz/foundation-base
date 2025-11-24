(ns code.heal.edit-test
  (:use code.test)
  (:require [code.heal.edit :as core]
            [code.heal.parse :as parse]
            [code.heal.print :as print]
            [std.lib :as h]))

^{:refer code.heal.edit/update-content :added "4.0"}
(fact "performs the necessary edits to a string"
  ^:hidden

  (core/update-content
   "("
   '({:action :insert, :line 1, :col 1, :new-char ")"}))
  => "()"

  
  (core/update-content
   "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done})"
   '({:action :replace, :line 5, :col 19, :new-char ")"}))
  =>
  "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done))")

^{:refer code.heal.edit/create-mismatch-edits :added "4.0"}
(fact "find the actions required to edit the content"
  ^:hidden
  
  (core/create-mismatch-edits
   (parse/pair-delimiters
    (parse/parse-delimiters
     "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done})")))
  => '({:action :replace, :line 5, :col 19, :new-char ")"}))

^{:refer code.heal.edit/check-append-fn :added "4.0"}
(fact "check for open unpaired")

^{:refer code.heal.edit/check-append-edits :added "4.0"}
(fact "checks that append edits are value")

^{:refer code.heal.edit/create-append-edits :added "4.0"}
(fact "creates the append edits"
  ^:hidden
  
  (core/create-append-edits
   (parse/parse-delimiters" ( "))
  => [{:action :insert, :line 1, :col 2, :new-char ")"}])

^{:refer code.heal.edit/check-remove-fn :added "4.0"}
(fact "check for close unpaired")

^{:refer code.heal.edit/create-remove-edits :added "4.0"}
(fact "creates removes edits")








