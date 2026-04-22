(ns xt.sample.train-004-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]))

^{:seedgen/root         {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-spec :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})


;; EXAMPLE A --------------------
;; (seedgen/langadd {:lang :python}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-a :added "4.1"}
(fact "muliple checks are also allowed"

  (!.js
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  ^{:seedgen/lang {:python {:suppress true}}}
  (!.js
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)


;; AFTER
^{:refer xt.lang.common-spec/example-a :added "4.1"}
(fact "muliple checks are also allowed"

  (!.js
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  ^{:seedgen/lang {:python {:suppress true}}}
  (!.js
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10


  (!.py
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6)



;; EXAMPLE B --------------------
;; (seedgen/langadd {:lang [:lua :python]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-b :added "4.1"}
(fact "forms can be suppressed"

  (!.js
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  ^{:seedgen/lang {:python {:suppress true}}}
  (!.js
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)


;; AFTER
^{:refer xt.lang.common-spec/example-b :added "4.1"}
(fact "forms can be suppressed"

  (!.js
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  ^{:seedgen/lang {:python {:suppress true}}}
  (!.js
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10

  (!.lua
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.lua
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10
  
  (!.py
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6)


;; EXAMPLE C --------------------
;; (seedgen/langadd {:lang [:python :lua]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-c :added "4.1"}
(fact "order is important"

  (notify/wait-on :js
    (repl/notify 1))
  => 1)


;; AFTER
^{:refer xt.lang.common-spec/example-c :added "4.1"}
(fact "order is important"

  (notify/wait-on :js
    (repl/notify 1))
  => 1


  (notify/wait-on :python
    (repl/notify 1))
  => 1

  (notify/wait-on :lua
    (repl/notify 1))
  => 1)



;; EXAMPLE D --------------------
;; (seedgen/langadd {:lang [:lua :python]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-d :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  ^{:seedgen/base      true}
  [(!.js 1)
   (inc 0)
   (notify/wait-on :js
     (repl/notify 1))]
  => [1 1])


;; AFTER
^{:refer xt.lang.common-spec/example-d :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  ^{:seedgen/base       true}
  [(!.js 1)
   (inc 0)
   (notify/wait-on :js
     (repl/notify 1))]
  => [1 1]

  [(!.lua 1)
   (inc 0)
   (notify/wait-on :lua
     (repl/notify 1))]
  => [1 1]


  [(!.py 1)
   (inc 0)
   (notify/wait-on :python
     (repl/notify 1))]
  => [1 1])




;; EXAMPLE E --------------------
;; (seedgen/langadd {:lang [:lua :python]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-e :added "4.1"}
(fact "seed meta can be mixed and matched"

  ^{:seedgen/base      true}
  (identity (!.js 1))
  => 1

  ^{:seedgen/base      true
    :seedgen/lang     {:python {:suppress true}}}
  (identity (!.js 2))
  => 2)


;; AFTER
^{:refer xt.lang.common-spec/example-e :added "4.1"}
(fact "seed meta can be mixed and matched"

  ^{:seedgen/base      true}
  (identity (!.js 1))
  => 1

  ^{:seedgen/base      true
    :seedgen/lang     {:python {:suppress true}}}
  (identity (!.js 2))
  => 2

  (identity (!.lua 1))
  => 1

  (identity (!.lua 2))
  => 2

  (identity (!.py 1))
  => 1)



;; EXAMPLE F --------------------
;; (seedgen/langadd {:lang [:lua :python]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-f :added "4.1"}
(fact "expect can be customised"

  ^{:seedgen/check    {:lua  {:expect 11}}}
  (!.js    
    (xt/x:offset 10))
  => 10)


;; AFTER
^{:refer xt.lang.common-spec/example-f :added "4.1"}
(fact "expect can be customised"


  ^{:seedgen/check    {:lua  {:expect 11}}}
  (!.js    
    (xt/x:offset 10))
  => 10

  (!.lua    
    (xt/x:offset 10))
  => 11

  (!.js    
    (xt/x:offset 10))
  => python)




;; EXAMPLE F --------------------
;; (seedgen/langadd {:lang [:lua :python]}) should generate

;; BEFORE
^{:refer xt.lang.common-spec/example-f :added "4.1"}
(fact "expect can be customised"

  ^{:seedgen/check    {:lua  {:input (xt/x:offest 9)}}}
  (!.js    
    (xt/x:offset 10))
  => 10)


;; AFTER
^{:refer xt.lang.common-spec/example-f :added "4.1"}
(fact "expect can be customised"


  ^{:seedgen/check    {:lua  {:input (xt/x:offest
                                      9)}}}
  (!.js    
    (xt/x:offset 10))
  => 10

  (!.lua                      ;; follows formatting
    (xt/x:offset
     9))
  => 10

  (!.py    
    (xt/x:offset 10))
  => 10)
