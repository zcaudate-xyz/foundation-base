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

(defn emit-js [form]
  (l/emit-as :js [form]))

^{:refer xt.lang.spec-primitive/!:G :added "4.1"}
(fact "reads and writes global values"

  (!.julia
    (var prev (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) "alpha")
    (var out (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) prev)
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/$ :added "4.1"}
(fact "invokes static methods"

  (!.julia
    (primitive/$ Math max 1 4 2))
  => 4)

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.julia
    (primitive/% [(primitive/+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.julia
    (primitive/* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.julia
    (primitive/+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.julia
    [(primitive/- 10 3 2)
     (primitive/- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.julia
    [(primitive/. {:a 1 :b 2} ["b"])
     (primitive/. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.julia
    [(primitive// 12 3)
     (primitive// 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.julia
    [(primitive/< 1 2)
     (primitive/< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.julia
    [(primitive/<= 2 2)
     (primitive/<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.julia
    [(primitive/== 2 2)
     (primitive/== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.julia
    [(primitive/> 3 2)
     (primitive/> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.julia
    [(primitive/>= 3 3)
     (primitive/>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/async :added "4.1"}
(fact "creates async functions"

  (notify/wait-on :julia
    (. ((primitive/async
         (fn []
           (return 5))))
       (then (repl/>notify))))
  => 5)

^{:refer xt.lang.spec-primitive/await :added "4.1"}
(fact "awaits promise results"

  (notify/wait-on :julia
    (promise/x:promise-then
     ((primitive/async
       (fn []
         (return
          (primitive/await
           (promise/x:promise
            (fn []
              (return 7))))))))
     (repl/>notify)))
  => 7)

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  (!.julia
    (primitive/b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.julia
    (primitive/b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.julia
    (primitive/b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.julia
    ((fn []
       (primitive/br*
         (if false
           (return "no"))
         (elseif (primitive/> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes")

^{:refer xt.lang.spec-primitive/break :added "4.1"}
(fact "breaks out of loops"

  (!.julia
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 5) [(:= i (primitive/+ i 1))]]
      (when (primitive/== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/do:> :added "4.1"}
(fact "runs deferred blocks immediately"

  (!.julia
    (primitive/do:>
      1
      2
      (return 3)))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.julia
    ((primitive/fn:> [x]
       (return (primitive/+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/forange :added "4.1"}
(fact "iterates numeric ranges"

  (!.julia
    (var out [])
    (primitive/forange [i 5]
      (xt/x:arr-push out i))
    out)
  => [0 1 2 3 4])

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.julia
    (primitive/mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/new :added "4.1"}
(fact "constructs new values"

  (!.julia
    (var xs (primitive/new Array 1 2 3))
    (primitive/. xs ["length"]))
  => 3)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.julia
    [(primitive/not= 2 3)
     (primitive/not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  (!.julia
    (primitive/pow 2 5))
  => 7)

^{:refer xt.lang.spec-primitive/return :added "4.1"}
(fact "returns early from functions"

  (!.julia
    ((fn []
       (primitive/return 7)
       9)))
  => 7)

^{:refer xt.lang.spec-primitive/switch :added "4.1"}
(fact "switches across explicit cases"

  (!.julia
    ((fn []
       (primitive/switch [2]
         (primitive/case [1]
           (return "one"))
         (primitive/case [2]
           (return "two"))
         (default
           (return "other"))))))
  => "two")

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.julia
    (primitive/tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/this :added "4.1"}
(fact "accesses the current receiver"

  (!.julia
    (var obj {})
    (:= (primitive/. obj ["value"]) 7)
    (:= (primitive/. obj ["get"])
        (fn []
          (return (primitive/. this ["value"]))))
    (. obj (get)))
  => 7)

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.julia
    (primitive/do:>
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/var :added "4.1"}
(fact "declares variables"

  (!.julia
    (primitive/do
      (primitive/var total 3)
      (:= total (primitive/+ total 4))
      total))
  => 7)

^{:refer xt.lang.spec-primitive/var.inner :added "4.1"}
(fact "reassigns existing locals"

  (!.julia
    ((fn []
       (var total 1)
       (primitive/var.inner total 4)
       (return total))))
  => 4)

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.julia
    [(primitive/xor true false)
     (primitive/xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.julia
    (primitive/-> 5
                  (primitive/+ 2)
                  (primitive/* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.julia
    (primitive/->> 5
                   (primitive/- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.julia
    [(primitive/and true 1)
     (primitive/and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/case :added "4.1"}
(fact "expands high-level case branches"

  (!.julia
    (primitive/do:>
      (primitive/case 2
        1 (return "one")
        2 (return "two")
        (return "fallback"))))
  => "two")

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.julia
    (primitive/do
      (primitive/comment (primitive/throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.julia
    (primitive/do:>
      (primitive/cond
        false (return "a")
        (primitive/< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.julia
    (primitive/do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/for :added "4.1"}
(fact "runs explicit for loops"

  (!.julia
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 3) [(:= i (primitive/+ i 1))]]
      (xt/x:arr-push out i))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.julia
    (primitive/do:>
      (primitive/if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.julia
    (primitive/do:>
      (primitive/let [a 2
                      b 3]
        (return (primitive/+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.julia
    [(primitive/not true)
     (primitive/not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.julia
    [(primitive/or nil "fallback")
     (primitive/or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.julia
    (primitive/quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.julia
    (primitive/do:>
      (var out [])
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (xt/x:arr-push out err))
        (finally
          (xt/x:arr-push out "done")))
      (return out)))
  => ["boom" "done"])

^{:refer xt.lang.spec-primitive/when :added "4.1"}
(fact "runs truthy branches"

  (!.julia
    (var out [])
    (primitive/when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.julia
    (var i 0)
    (var out [])
    (primitive/while (primitive/< i 3)
      (xt/x:arr-push out i)
      (:= i (primitive/+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.julia
    ((primitive/fn [x]
       (return (primitive/+ x 1)))
     2))
  => 3)

(comment
  (str/includes? (emit-js '(yield 3)) "yield"))
