(ns code.heal.core-test
  (:use code.test)
  (:require [code.heal.core :as core]
            [code.heal.parse :as parse]))

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

^{:refer code.heal.core/create-unclosed-edits :added "4.0"}
(fact "gets the edits for healing unclosed forms"
  ^:hidden
  
  (core/create-unclosed-edits
   (parse/pair-delimiters
    (parse/parse-delimiters
     "(")))
  => '({:action :insert, :line 1, :col 2, :new-char ")"})

  (core/create-unclosed-edits
   (parse/pair-delimiters
    (parse/parse-delimiters
     "(defn []
      (do)"))))

^{:refer code.heal.core/heal-unclosed :added "4.0"}
(fact "heals unclosed forms")
