(ns xt.lang.spec-primitive-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-primitive :as primitive]))

^{:seedgen/root {:all true, :langs [:python :lua]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-primitive :as primitive]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-primitive :as primitive]]})

(l/script- :lua
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

  (!.js
    (var prev (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) "alpha")
    (var out (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) prev)
    out)
  => "alpha"

  (!.py
    (var prev (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) "alpha")
    (var out (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) prev)
    out)
  => "alpha"

  (!.lua
    (var prev (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) "alpha")
    (var out (primitive/!:G __PRIMITIVE_TEST__))
    (:= (primitive/!:G __PRIMITIVE_TEST__) prev)
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/$ :added "4.1"}
(fact "invokes static methods"

  (!.js
    (primitive/$ Math max 1 4 2))
  => 4

  (!.py
    (primitive/$ Math max 1 4 2))
  => 4

  (!.lua
    (primitive/$ Math max 1 4 2))
  => 4)

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.js
    (primitive/% [(primitive/+ 1 2) 4]))
  => [3 4]

  (!.py
    (primitive/% [(primitive/+ 1 2) 4]))
  => [3 4]

  (!.lua
    (primitive/% [(primitive/+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.js
    (primitive/* 2 3 4))
  => 24

  (!.py
    (primitive/* 2 3 4))
  => 24

  (!.lua
    (primitive/* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.js
    (primitive/+ 1 2 3 4))
  => 10

  (!.py
    (primitive/+ 1 2 3 4))
  => 10

  (!.lua
    (primitive/+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.js
    [(primitive/- 10 3 2)
     (primitive/- 5)])
  => [5 -5]

  (!.py
    [(primitive/- 10 3 2)
     (primitive/- 5)])
  => [5 -5]

  (!.lua
    [(primitive/- 10 3 2)
     (primitive/- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.js
    [(primitive/. {:a 1 :b 2} ["b"])
     (primitive/. [1 2 3] [1])])
  => [2 2]

  (!.py
    [(primitive/. {:a 1 :b 2} ["b"])
     (primitive/. [1 2 3] [1])])
  => [2 2]

  (!.lua
    [(primitive/. {:a 1 :b 2} ["b"])
     (primitive/. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.js
    [(primitive// 12 3)
     (primitive// 20 5 2)])
  => [4 2]

  (!.py
    [(primitive// 12 3)
     (primitive// 20 5 2)])
  => [4 2]

  (!.lua
    [(primitive// 12 3)
     (primitive// 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.js
    [(primitive/< 1 2)
     (primitive/< 2 1)])
  => [true false]

  (!.py
    [(primitive/< 1 2)
     (primitive/< 2 1)])
  => [true false]

  (!.lua
    [(primitive/< 1 2)
     (primitive/< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.js
    [(primitive/<= 2 2)
     (primitive/<= 3 2)])
  => [true false]

  (!.py
    [(primitive/<= 2 2)
     (primitive/<= 3 2)])
  => [true false]

  (!.lua
    [(primitive/<= 2 2)
     (primitive/<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.js
    [(primitive/== 2 2)
     (primitive/== 2 3)])
  => [true false]

  (!.py
    [(primitive/== 2 2)
     (primitive/== 2 3)])
  => [true false]

  (!.lua
    [(primitive/== 2 2)
     (primitive/== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.js
    [(primitive/> 3 2)
     (primitive/> 2 3)])
  => [true false]

  (!.py
    [(primitive/> 3 2)
     (primitive/> 2 3)])
  => [true false]

  (!.lua
    [(primitive/> 3 2)
     (primitive/> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.js
    [(primitive/>= 3 3)
     (primitive/>= 2 3)])
  => [true false]

  (!.py
    [(primitive/>= 3 3)
     (primitive/>= 2 3)])
  => [true false]

  (!.lua
    [(primitive/>= 3 3)
     (primitive/>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/async :added "4.1"}
(fact "creates async functions"

  (notify/wait-on :js
    (. ((primitive/async
         (fn []
           (return 5))))
       (then (repl/>notify))))
  => 5

  (notify/wait-on :python
    (. ((primitive/async
         (fn []
           (return 5))))
       (then (repl/>notify))))
  => 5

  (notify/wait-on :lua
    (. ((primitive/async
         (fn []
           (return 5))))
       (then (repl/>notify))))
  => 5)

^{:refer xt.lang.spec-primitive/await :added "4.1"}
(fact "awaits promise results"

  (notify/wait-on :js
    (promise/x:promise-then
     ((primitive/async
       (fn []
         (return
          (primitive/await
           (promise/x:promise
            (fn []
              (return 7))))))))
     (repl/>notify)))
  => 7

  (notify/wait-on :python
    (promise/x:promise-then
     ((primitive/async
       (fn []
         (return
          (primitive/await
           (promise/x:promise
            (fn []
              (return 7))))))))
     (repl/>notify)))
  => 7

  (notify/wait-on :lua
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

  (!.js
    (primitive/b:<< 3 2))
  => 12

  (!.py
    (primitive/b:<< 3 2))
  => 12

  (!.lua
    (primitive/b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.js
    (primitive/b:>> 12 2))
  => 3

  (!.py
    (primitive/b:>> 12 2))
  => 3

  (!.lua
    (primitive/b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.js
    (primitive/b:xor 6 3))
  => 5

  (!.py
    (primitive/b:xor 6 3))
  => 5

  (!.lua
    (primitive/b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.js
    ((fn []
       (primitive/br*
         (if false
           (return "no"))
         (elseif (primitive/> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes"

  (!.py
    ((fn []
       (primitive/br*
         (if false
           (return "no"))
         (elseif (primitive/> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes"

  (!.lua
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

  (!.js
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 5) [(:= i (primitive/+ i 1))]]
      (when (primitive/== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.py
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 5) [(:= i (primitive/+ i 1))]]
      (when (primitive/== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.lua
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 5) [(:= i (primitive/+ i 1))]]
      (when (primitive/== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/do:> :added "4.1"}
(fact "runs deferred blocks immediately"

  (!.js
    (primitive/do:>
      1
      2
      (return 3)))
  => 3

  (!.py
    (primitive/do:>
      1
      2
      (return 3)))
  => 3

  (!.lua
    (primitive/do:>
      1
      2
      (return 3)))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.js
    ((primitive/fn:> [x]
       (return (primitive/+ x 1)))
     2))
  => 3

  (!.py
    ((primitive/fn:> [x]
       (return (primitive/+ x 1)))
     2))
  => 3

  (!.lua
    ((primitive/fn:> [x]
       (return (primitive/+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/forange :added "4.1"}
(fact "iterates numeric ranges"

  (!.js
    (var out [])
    (primitive/forange [i 5]
      (xt/x:arr-push out i))
    out)
  => [0 1 2 3 4]

  (!.py
    (var out [])
    (primitive/forange [i 5]
      (xt/x:arr-push out i))
    out)
  => [0 1 2 3 4]

  (!.lua
    (var out [])
    (primitive/forange [i 5]
      (xt/x:arr-push out i))
    out)
  => [0 1 2 3 4])

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.js
    (primitive/mod 17 5))
  => 2

  (!.py
    (primitive/mod 17 5))
  => 2

  (!.lua
    (primitive/mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/new :added "4.1"}
(fact "constructs new values"

  (!.js
    (var xs (primitive/new Array 1 2 3))
    (primitive/. xs ["length"]))
  => 3

  (!.py
    (var xs (primitive/new Array 1 2 3))
    (primitive/. xs ["length"]))
  => 3

  (!.lua
    (var xs (primitive/new Array 1 2 3))
    (primitive/. xs ["length"]))
  => 3)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.js
    [(primitive/not= 2 3)
     (primitive/not= 2 2)])
  => [true false]

  (!.py
    [(primitive/not= 2 3)
     (primitive/not= 2 2)])
  => [true false]

  (!.lua
    [(primitive/not= 2 3)
     (primitive/not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  (!.js
    (primitive/pow 2 5))
  => 7

  (!.py
    (primitive/pow 2 5))
  => 7

  (!.lua
    (primitive/pow 2 5))
  => 7)

^{:refer xt.lang.spec-primitive/return :added "4.1"}
(fact "returns early from functions"

  (!.js
    ((fn []
       (primitive/return 7)
       9)))
  => 7

  (!.py
    ((fn []
       (primitive/return 7)
       9)))
  => 7

  (!.lua
    ((fn []
       (primitive/return 7)
       9)))
  => 7)

^{:refer xt.lang.spec-primitive/switch :added "4.1"}
(fact "switches across explicit cases"

  (!.js
    ((fn []
       (primitive/switch [2]
         (primitive/case [1]
           (return "one"))
         (primitive/case [2]
           (return "two"))
         (default
           (return "other"))))))
  => "two"

  (!.py
    ((fn []
       (primitive/switch [2]
         (primitive/case [1]
           (return "one"))
         (primitive/case [2]
           (return "two"))
         (default
           (return "other"))))))
  => "two"

  (!.lua
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

  (!.js
    (primitive/tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2}

  (!.py
    (primitive/tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2}

  (!.lua
    (primitive/tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/this :added "4.1"}
(fact "accesses the current receiver"

  (!.js
    (var obj {})
    (:= (primitive/. obj ["value"]) 7)
    (:= (primitive/. obj ["get"])
        (fn []
          (return (primitive/. this ["value"]))))
    (. obj (get)))
  => 7

  (!.py
    (var obj {})
    (:= (primitive/. obj ["value"]) 7)
    (:= (primitive/. obj ["get"])
        (fn []
          (return (primitive/. this ["value"]))))
    (. obj (get)))
  => 7

  (!.lua
    (var obj {})
    (:= (primitive/. obj ["value"]) 7)
    (:= (primitive/. obj ["get"])
        (fn []
          (return (primitive/. this ["value"]))))
    (. obj (get)))
  => 7)

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.js
    (primitive/do:>
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (return err)))))
  => "boom"

  (!.py
    (primitive/do:>
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (return err)))))
  => "boom"

  (!.lua
    (primitive/do:>
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/var :added "4.1"}
(fact "declares variables"

  (!.js
    (primitive/do
      (primitive/var total 3)
      (:= total (primitive/+ total 4))
      total))
  => 7

  (!.py
    (primitive/do
      (primitive/var total 3)
      (:= total (primitive/+ total 4))
      total))
  => 7

  (!.lua
    (primitive/do
      (primitive/var total 3)
      (:= total (primitive/+ total 4))
      total))
  => 7)

^{:refer xt.lang.spec-primitive/var.inner :added "4.1"}
(fact "reassigns existing locals"

  (!.js
    ((fn []
       (var total 1)
       (primitive/var.inner total 4)
       (return total))))
  => 4

  (!.py
    ((fn []
       (var total 1)
       (primitive/var.inner total 4)
       (return total))))
  => 4

  (!.lua
    ((fn []
       (var total 1)
       (primitive/var.inner total 4)
       (return total))))
  => 4)

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.js
    [(primitive/xor true false)
     (primitive/xor true true)])
  => [false true]

  (!.py
    [(primitive/xor true false)
     (primitive/xor true true)])
  => [false true]

  (!.lua
    [(primitive/xor true false)
     (primitive/xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.js
    (primitive/-> 5
                  (primitive/+ 2)
                  (primitive/* 3)))
  => 21

  (!.py
    (primitive/-> 5
                  (primitive/+ 2)
                  (primitive/* 3)))
  => 21

  (!.lua
    (primitive/-> 5
                  (primitive/+ 2)
                  (primitive/* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.js
    (primitive/->> 5
                   (primitive/- 10)))
  => 5

  (!.py
    (primitive/->> 5
                   (primitive/- 10)))
  => 5

  (!.lua
    (primitive/->> 5
                   (primitive/- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.js
    [(primitive/and true 1)
     (primitive/and true false)])
  => [1 false]

  (!.py
    [(primitive/and true 1)
     (primitive/and true false)])
  => [1 false]

  (!.lua
    [(primitive/and true 1)
     (primitive/and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/case :added "4.1"}
(fact "expands high-level case branches"

  (!.js
    (primitive/do:>
      (primitive/case 2
        1 (return "one")
        2 (return "two")
        (return "fallback"))))
  => "two"

  (!.py
    (primitive/do:>
      (primitive/case 2
        1 (return "one")
        2 (return "two")
        (return "fallback"))))
  => "two"

  (!.lua
    (primitive/do:>
      (primitive/case 2
        1 (return "one")
        2 (return "two")
        (return "fallback"))))
  => "two")

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.js
    (primitive/do
      (primitive/comment (primitive/throw "boom"))
      1))
  => 1

  (!.py
    (primitive/do
      (primitive/comment (primitive/throw "boom"))
      1))
  => 1

  (!.lua
    (primitive/do
      (primitive/comment (primitive/throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.js
    (primitive/do:>
      (primitive/cond
        false (return "a")
        (primitive/< 1 2) (return "b")
        :else (return "c"))))
  => "b"

  (!.py
    (primitive/do:>
      (primitive/cond
        false (return "a")
        (primitive/< 1 2) (return "b")
        :else (return "c"))))
  => "b"

  (!.lua
    (primitive/do:>
      (primitive/cond
        false (return "a")
        (primitive/< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.js
    (primitive/do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2]

  (!.py
    (primitive/do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2]

  (!.lua
    (primitive/do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/for :added "4.1"}
(fact "runs explicit for loops"

  (!.js
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 3) [(:= i (primitive/+ i 1))]]
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.py
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 3) [(:= i (primitive/+ i 1))]]
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.lua
    (var out [])
    (primitive/for [(var i 0) (primitive/< i 3) [(:= i (primitive/+ i 1))]]
      (xt/x:arr-push out i))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.js
    (primitive/do:>
      (primitive/if true
        (return "yes")
        (return "no"))))
  => "yes"

  (!.py
    (primitive/do:>
      (primitive/if true
        (return "yes")
        (return "no"))))
  => "yes"

  (!.lua
    (primitive/do:>
      (primitive/if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.js
    (primitive/do:>
      (primitive/let [a 2
                      b 3]
        (return (primitive/+ a b)))))
  => 5

  (!.py
    (primitive/do:>
      (primitive/let [a 2
                      b 3]
        (return (primitive/+ a b)))))
  => 5

  (!.lua
    (primitive/do:>
      (primitive/let [a 2
                      b 3]
        (return (primitive/+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.js
    [(primitive/not true)
     (primitive/not false)])
  => [false true]

  (!.py
    [(primitive/not true)
     (primitive/not false)])
  => [false true]

  (!.lua
    [(primitive/not true)
     (primitive/not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.js
    [(primitive/or nil "fallback")
     (primitive/or 1 2)])
  => ["fallback" 1]

  (!.py
    [(primitive/or nil "fallback")
     (primitive/or 1 2)])
  => ["fallback" 1]

  (!.lua
    [(primitive/or nil "fallback")
     (primitive/or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.js
    (primitive/quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]}

  (!.py
    (primitive/quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]}

  (!.lua
    (primitive/quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.js
    (primitive/do:>
      (var out [])
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (xt/x:arr-push out err))
        (finally
          (xt/x:arr-push out "done")))
      (return out)))
  => ["boom" "done"]

  (!.py
    (primitive/do:>
      (var out [])
      (primitive/try
        (primitive/throw "boom")
        (catch err
          (xt/x:arr-push out err))
        (finally
          (xt/x:arr-push out "done")))
      (return out)))
  => ["boom" "done"]

  (!.lua
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

  (!.js
    (var out [])
    (primitive/when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2]

  (!.py
    (var out [])
    (primitive/when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2]

  (!.lua
    (var out [])
    (primitive/when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.js
    (var i 0)
    (var out [])
    (primitive/while (primitive/< i 3)
      (xt/x:arr-push out i)
      (:= i (primitive/+ i 1)))
    out)
  => [0 1 2]

  (!.py
    (var i 0)
    (var out [])
    (primitive/while (primitive/< i 3)
      (xt/x:arr-push out i)
      (:= i (primitive/+ i 1)))
    out)
  => [0 1 2]

  (!.lua
    (var i 0)
    (var out [])
    (primitive/while (primitive/< i 3)
      (xt/x:arr-push out i)
      (:= i (primitive/+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.js
    ((primitive/fn [x]
       (return (primitive/+ x 1)))
     2))
  => 3

  (!.py
    ((primitive/fn [x]
       (return (primitive/+ x 1)))
     2))
  => 3

  (!.lua
    ((primitive/fn [x]
       (return (primitive/+ x 1)))
     2))
  => 3)

(comment
  (s/snapto)
  (s/run '[xt.lang.spec-primitive])
  
  (s/seedgen-langadd '[xt.lang.spec-primitive] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.spec-primitive] {:lang [:lua :python] :write true}))
