(ns xtbench.scheme.lang.spec-primitive-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-primitive :as primitive]))

(l/script- :scheme
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

  (!.scheme
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.scheme
    (% [(+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.scheme
    (* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.scheme
    (+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.scheme
    [(- 10 3 2)
     (- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.scheme
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.scheme
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.scheme
    [(< 1 2)
     (< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.scheme
    [(<= 2 2)
     (<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.scheme
    [(== 2 2)
     (== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.scheme
    [(> 3 2)
     (> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.scheme
    [(>= 3 3)
     (>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  (!.scheme
    (b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.scheme
    (b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.scheme
    (b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.scheme
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

  (!.scheme
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

  (!.scheme
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.scheme
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.scheme
    (mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.scheme
    [(not= 2 3)
     (not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  ^*(!.scheme
    (pow 2 5))
  => 32)

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.scheme
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.scheme
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.scheme
    [(xor true false)
     (xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.scheme
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.scheme
    (->> 5
                   (- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.scheme
    [(and true 1)
     (and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.scheme
    (do
      (comment (throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.scheme
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.scheme
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.scheme
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.scheme
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.scheme
    [(not true)
     (not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.scheme
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.scheme
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.scheme
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

  (!.scheme
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.scheme
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.scheme
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
