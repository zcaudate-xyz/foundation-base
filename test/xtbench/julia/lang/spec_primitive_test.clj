(ns xtbench.julia.lang.spec-primitive-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-primitive :as primitive]))

(l/script- :julia
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

  (!.julia
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.julia
    (% [(+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.julia
    (* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.julia
    (+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.julia
    [(- 10 3 2)
     (- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.julia
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.julia
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.julia
    [(< 1 2)
     (< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.julia
    [(<= 2 2)
     (<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.julia
    [(== 2 2)
     (== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.julia
    [(> 3 2)
     (> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.julia
    [(>= 3 3)
     (>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  (!.julia
    (b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.julia
    (b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.julia
    (b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.julia
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

  (!.julia
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

  (!.julia
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.julia
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.julia
    (mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.julia
    [(not= 2 3)
     (not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  ^*(!.julia
    (pow 2 5))
  => 32)

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.julia
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.julia
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.julia
    [(xor true false)
     (xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.julia
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.julia
    (->> 5
                   (- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.julia
    [(and true 1)
     (and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.julia
    (do
      (comment (throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.julia
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.julia
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.julia
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.julia
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.julia
    [(not true)
     (not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.julia
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.julia
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.julia
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

  (!.julia
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.julia
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.julia
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
