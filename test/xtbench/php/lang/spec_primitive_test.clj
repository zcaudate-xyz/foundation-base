(ns xtbench.php.lang.spec-primitive-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-primitive :as primitive]))

(l/script- :php
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-primitive :as primitive]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-primitive/!:G :added "4.1"}
(fact "reads and writes global values"

  (!.php
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.php
    (% [(+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.php
    (* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.php
    (+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.php
    [(- 10 3 2)
     (- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.php
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.php
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.php
    [(< 1 2)
     (< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.php
    [(<= 2 2)
     (<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.php
    [(== 2 2)
     (== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.php
    [(> 3 2)
     (> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.php
    [(>= 3 3)
     (>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  (!.php
    (b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.php
    (b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.php
    (b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.php
    ((fn []
       (br*
         (if false
           (return "no"))
         (elseif (> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes")

^{:refer xt.lang.spec-primitive/break :added "4.1"}
(fact "breaks out of loops"

  (!.php
    (var out [])
    (var entries [0 1 2 3 4 5])
    (for:array [i entries]
      (when (== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/do:> :added "4.1"}
(fact "runs deferred blocks immediately"

  (!.php
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.php
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.php
    (mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.php
    [(not= 2 3)
     (not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  ^*(!.php
    (pow 2 5))
  => 32)

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.php
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.php
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.php
    [(xor true false)
     (xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.php
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.php
    (->> 5
                   (- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.php
    [(and true 1)
     (and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.php
    (do
      (comment (throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.php
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.php
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.php
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.php
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.php
    [(not true)
     (not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.php
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.php
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.php
    (var out [])
    (try
      (throw "boom")
      (catch err
          (xt/x:arr-push out err))
      (finally
        (xt/x:arr-push out "done")))
    (return out))
  => ["boom" "done"])

^{:refer xt.lang.spec-primitive/when :added "4.1"}
(fact "runs truthy branches"

  (!.php
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.php
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.php
    ((fn [x]
       (return (+ x 1)))
     2))
  => 3)

(comment
  
  (s/snapto)
  (s/run '[xt.lang.spec-primitive])
  (s/run '[xt.lang])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r :php :ruby :dart :julia] :write true})
  (s/seedgen-benchremove '[xt.lang.spec-primitive] {:lang [:r :php :ruby :dart :julia :scheme :elisp] :write true})
  (s/seedgen-langadd  '[xt.lang.spec-primitive] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.spec-primitive] {:lang [:lua :python] :write true}))
