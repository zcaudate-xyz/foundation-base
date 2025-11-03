(ns code.heal.core-test
  (:use code.test)
  (:require [code.heal.core :as core]
            [code.heal.parse :as parse]
            [code.heal.print :as print]
            [std.lib :as h]))

^{:refer code.heal.core/update-content :added "4.0"}
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
(fact "check for open unpaired")

^{:refer code.heal.core/check-append-edits :added "4.0"}
(fact "checks that append edits are value")

^{:refer code.heal.core/create-append-edits :added "4.0"}
(fact "creates the append edits"
  ^:hidden
  
  (core/create-append-edits
   (parse/parse-delimiters" ( "))
  => [{:action :insert, :line 1, :col 2, :new-char ")"}])

^{:refer code.heal.core/heal-append :added "4.0"}
(fact "appends at the "
  ^:hidden
  
  (core/heal-append " ( " )
  => " () "
  
  (core/heal-append " (" )
  => " ()"
  
  (core/heal-append "(" )
  => "()")

^{:refer code.heal.core/check-remove-fn :added "4.0"}
(fact "check for close unpaired")

^{:refer code.heal.core/create-remove-edits :added "4.0"}
(fact "creates removes edits")

^{:refer code.heal.core/heal-remove :added "4.0"}
(fact "removed unmatched parens"
  ^:hidden
  
  (core/heal-remove "(()))")
  => "(())")

^{:refer code.heal.core/heal-close-heavy-single-pass :added "4.0"}
(fact "creates deletion for multiple early closes on first pass"
  ^:hidden
  
  (core/heal-close-heavy-single-pass
   "())
())"
   {})
  => "()\n())"
  (core/heal-close-heavy-single-pass
   "
()
(fact )))
  (todo ))"
   {})
  => "\n()\n(fact ))\n  (todo ))"

  (core/heal-close-heavy-single-pass
   "
(fact)
  (+ 1 2))
(fact )))
  (todo ))"
   {})
  => "\n(fact\n  (+ 1 2))\n(fact )))\n  (todo ))"

  (core/heal-close-heavy-single-pass
   (core/heal-close-heavy-single-pass
    "
(fact)
  (+ 1 2))
(fact )))
  (todo ))"
    {}))
  => "\n(fact\n  (+ 1 2))\n(fact ))\n  (todo ))"
  )

^{:refer code.heal.core/heal-close-heavy :added "4.0"}
(fact "multiple close deletions"
  ^:hidden
  
  (read-string
   (str "["
        (core/heal-close-heavy "
())
(fact))))
  (+ 1 2))
(fact )))))))
  (todo ))")
        "]"))
  => '[() (fact (+ 1 2)) (fact (todo))])

^{:refer code.heal.core/heal-open-heavy-single-pass :added "4.0"}
(fact "heals content that has been wrongly "

  (read-string
   (core/heal-open-heavy-single-pass "
(defn
 (do
   (it)
 (this)
 (this))"))
  => '(defn (do (it)) (this) (this)))

^{:refer code.heal.core/heal-open-heavy :added "4.0"}
(fact "fixes indentation parens"
  ^:hidden
  
  (read-string
   (core/heal-open-heavy "
(defn
 (do
   (it
 (this)
 (this))" {}))
  = '(defn (do (it)) (this) (this))

  (read-string
   (core/heal-open-heavy "
[:h 
 [
  []
   [[[]
 []]" {}))
  => [:h [[] [[[]]]] []]

  ;; only the first string
  (read-string
   (core/heal-open-heavy "
[:h 
 [
  []
   [[[]
[]]" {}))
  => [:h [[] [[[]]]]])

^{:refer code.heal.core/heal-raw :added "4.0"}
(fact "combining all strategies for code heal"
  ^:hidden

  
  (core/heal-raw
   (h/sys:resource-content "code/heal/cases/002_complex.block")
   {:limit 20
    }))

^{:refer code.heal.core/heal :added "4.0"}
(fact "heals the content"
  ^:hidden
  
  (read-string
   (str "["
        (core/heal
         (h/sys:resource-content "code/heal/cases/002_complex.block")
         {})
        "]")))



(comment
  (parse/is-readable
   (parse/parse
    (core/heal
     (h/sys:resource-content "code/heal/cases/002_complex.block")
     {})))

  (parse/is-readable
   (parse/parse
    (core/heal-close-heavy
     (core/heal
      (h/sys:resource-content "code/heal/cases/002_complex.block")
      {}))))

  (std.text.diff/diff
   (core/heal
    (h/sys:resource-content "code/heal/cases/002_complex.block")
    {})
   (core/heal-close-heavy
    (core/heal
     (h/sys:resource-content "code/heal/cases/002_complex.block")
     {})
    {}))
  
  (std.text.diff/->string
   (std.text.diff/diff
    (core/heal-close-heavy
     (h/sys:resource-content "code/heal/cases/002_complex.block")
     {})
    (h/sys:resource-content "code/heal/cases/002_complex.block")))
  
  (std.text.diff/->string
   (std.text.diff/diff
    (h/sys:resource-content "code/heal/cases/002_complex.block")
    (core/heal
     (h/sys:resource-content "code/heal/cases/002_complex.block")
     {})))
  
  (code.heal/print-rainbox
   (core/heal
    (h/sys:resource-content "code/heal/cases/002_complex.block")
    {})))
