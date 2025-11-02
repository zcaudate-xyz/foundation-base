(ns code.heal.core-test
  (:use code.test)
  (:require [code.heal.core :as core]
            [code.heal.parse :as parse]
            [std.lib :as h]))

^{:refer code.heal.core/update-content :added "4.0"}
(fact "performs the necessary edits to a string"
  ^:hidden

  (core/update-content
   "("
   '({:action :insert, :line 1, :col 2, :new-char ")"}))
  => "()"

  (core/update-content
   "(defn []
      (do)")
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

^{:refer code.heal.core/create-mismatch-edits :added "4.0"}
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

^{:refer code.heal.core/heal-mismatch :added "4.0"}
(fact "heals a style mismatch for paired delimiters"
  ^:hidden
  
  (core/heal-mismatch "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done})")
  =>
  "
(defn my-func [x]
  (if (> x 0)
    (println :positive)
    (println :done))")

^{:refer code.heal.core/check-append-fn :added "4.0"}
(fact "append delimiters to the end of the ")

^{:refer code.heal.core/check-append-edits :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/create-append-edits :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/heal-append :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/check-remove-fn :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/create-remove-edits :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/heal-remove :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/heal-indented-single-pass :added "4.0"}
(fact "heals content that has been wrongly "

  (read-string
   (core/heal-indented-single-pass "
(defn
 (do
   (it)
 (this)
 (this))"))
  => '(defn (do (it)) (this) (this))

  
  (h/p
   (core/heal-indented-single-pass"
[:h 
 [
  []
   [[[]
 []")))

^{:refer code.heal.core/heal-indented-multi-pass :added "4.0"}
(fact "TODO")

^{:refer code.heal.core/heal-indented :added "4.0"}
(fact "TODO"
  ^:hidden
  
  (read-string
   (core/heal-indented "
(defn
 (do
   (it
 (this)
 (this))"))
  => '(defn (do (it (this) (this))))

  (read-string
   (core/heal-indented "
[:h 
 [
  [}
   [[[]
 []"))
  => [:h [[] [[[]]]] []]

  
  (read-string
   (str "["
        (core/heal-indented
         (h/sys:resource-content "code/heal/cases/002_complex.block")
         {:print true})
        "]")))
