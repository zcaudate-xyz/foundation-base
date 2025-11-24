(ns code.heal.legacy-test
  (:use code.test)
  (:require [code.heal.legacy :as core]
            [code.heal.parse :as parse]
            [code.heal.print :as print]
            [std.lib :as h]))

^{:refer code.heal.legacy/heal-mismatch :added "4.0"}
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

^{:refer code.heal.legacy/heal-append :added "4.0"}
(fact "appends at the "
  ^:hidden
  
  (core/heal-append " ( " )
  => " () "
  
  (core/heal-append " (" )
  => " ()"
  
  (core/heal-append "(" )
  => "()")

^{:refer code.heal.legacy/heal-remove :added "4.0"}
(fact "removed unmatched parens"
  ^:hidden
  
  (core/heal-remove "(()))")
  => "(())")

^{:refer code.heal.legacy/heal-close-heavy-single-pass :added "4.0"}
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

^{:refer code.heal.legacy/heal-close-heavy :added "4.0"}
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

^{:refer code.heal.legacy/heal-open-heavy-single-pass :added "4.0"}
(fact "heals content that has been wrongly "

  (read-string
   (core/heal-open-heavy-single-pass "
(defn
 (do
   (it)
 (this)
 (this))"))
  => '(defn (do (it)) (this) (this)))

^{:refer code.heal.legacy/heal-open-heavy :added "4.0"}
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

^{:refer code.heal.legacy/heal-legacy-raw :added "4.0"}
(fact "combining all strategies for code heal"
  ^:hidden

  
  (core/heal-raw
   (h/sys:resource-content "code/heal/cases/002_complex.block")
   {:limit 20
    }))

^{:refer code.heal.legacy/heal-legacy :added "4.0"}
(fact "heals the content"
  ^:hidden
  
  (read-string
   (str "["
        (core/heal
         (h/sys:resource-content "code/heal/cases/002_complex.block")
         {})
        "]")))
