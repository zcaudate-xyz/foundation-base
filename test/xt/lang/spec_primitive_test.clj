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
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha"

  (!.py
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha"

  (!.lua
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.js
    (% [(+ 1 2) 4]))
  => [3 4]

  (!.py
    (% [(+ 1 2) 4]))
  => [3 4]

  (!.lua
    (% [(+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.js
    (* 2 3 4))
  => 24

  (!.py
    (* 2 3 4))
  => 24

  (!.lua
    (* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.js
    (+ 1 2 3 4))
  => 10

  (!.py
    (+ 1 2 3 4))
  => 10

  (!.lua
    (+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.js
    [(- 10 3 2)
     (- 5)])
  => [5 -5]

  (!.py
    [(- 10 3 2)
     (- 5)])
  => [5 -5]

  (!.lua
    [(- 10 3 2)
     (- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  ^{:seedgen/base {:lua {:suppress true}}}
  (!.js
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2]

  (!.py
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.js
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2]

  (!.py
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2]

  (!.lua
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.js
    [(< 1 2)
     (< 2 1)])
  => [true false]

  (!.py
    [(< 1 2)
     (< 2 1)])
  => [true false]

  (!.lua
    [(< 1 2)
     (< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.js
    [(<= 2 2)
     (<= 3 2)])
  => [true false]

  (!.py
    [(<= 2 2)
     (<= 3 2)])
  => [true false]

  (!.lua
    [(<= 2 2)
     (<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.js
    [(== 2 2)
     (== 2 3)])
  => [true false]

  (!.py
    [(== 2 2)
     (== 2 3)])
  => [true false]

  (!.lua
    [(== 2 2)
     (== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.js
    [(> 3 2)
     (> 2 3)])
  => [true false]

  (!.py
    [(> 3 2)
     (> 2 3)])
  => [true false]

  (!.lua
    [(> 3 2)
     (> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.js
    [(>= 3 3)
     (>= 2 3)])
  => [true false]

  (!.py
    [(>= 3 3)
     (>= 2 3)])
  => [true false]

  (!.lua
    [(>= 3 3)
     (>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/async :added "4.1"}
(fact "creates async functions"

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (. ((async
            (fn []
              (return 5))))
       (then (repl/>notify))))
  => 5)

^{:refer xt.lang.spec-primitive/await :added "4.1"}
(fact "awaits promise results"

  ^{:seedgen/base {:all {:suppress true}}}
  (notify/wait-on :js
    (promise/x:promise-then
     ((async
          (fn []
            (return
             (await
              (promise/x:promise
               (fn []
                 (return 7))))))))
     (repl/>notify)))
  => 7)

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  ^{:seedgen/base {:lua {:suppress true}}}
  (!.js
    (b:<< 3 2))
  => 12

  (!.py
    (b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  ^{:seedgen/base {:lua {:suppress true}}}
  (!.js
    (b:>> 12 2))
  => 3

  (!.py
    (b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  ^{:seedgen/base {:lua {:suppress true}}}
  (!.js
    (b:xor 6 3))
  => 5

  (!.py
    (b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"
  
  (!.js
    ((fn []
       (br*
         (if false
           (return "no"))
         (elseif (> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes"

  (!.py
    ((fn []
       (br*
         (if false
           (return "no"))
         (elseif (> 3 2)
           (return "yes"))
         (else
           (return "fallback"))))))
  => "yes"

  (!.lua
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

  (!.js
    (var out [])
    (var entries [0 1 2 3 4 5])
    (for:array [i entries]
      (when (== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.py
    (var out [])
    (var entries [0 1 2 3 4 5])
    (for:array [i entries]
      (when (== i 3)
        (break))
      (xt/x:arr-push out i))
    out)
  => [0 1 2]

  (!.lua
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

  (!.js
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3

  ^*(!.py
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3

  (!.lua
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  (!.js
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3

  ^*(!.py
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3

  (!.lua
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.js
    (mod 17 5))
  => 2

  (!.py
    (mod 17 5))
  => 2

  (!.lua
    (mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/new :added "4.1"}
(fact "constructs new values"

  ^{:seedgen/base {:all {:suppress true}}}
  (!.js
    (var xs (new Array 1 2 3))
    (. xs ["length"]))
  => 3)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.js
    [(not= 2 3)
     (not= 2 2)])
  => [true false]

  (!.py
    [(not= 2 3)
     (not= 2 2)])
  => [true false]

  (!.lua
    [(not= 2 3)
     (not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  ^*(!.js
    (pow 2 5))
  => 32

  ^*(!.py
    (pow 2 5))
  => 32

  (!.lua
    (pow 2 5))
  => 32)

^{:refer xt.lang.spec-primitive/switch :added "4.1"}
(fact "switches across explicit cases"

  ^{:seedgen/base {:all {:suppress true}}}
  (!.js
    ((fn []
       (switch [2]
                         (case [1]
                           (return "one"))
                         (case [2]
                           (return "two"))
                         (default
                          (return "other"))))))
  => "two")

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.js
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2}

  (!.py
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2}

  (!.lua
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/this :added "4.1"}
(fact "accesses the current receiver"

  ^{:seedgen/base {:all {:suppress true}}}
  (!.js
    (var obj {})
    (:= (. obj ["value"]) 7)
    (:= (. obj ["get"])
        (fn []
          (return (. this ["value"]))))
    (. obj (get)))
  => 7)

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.js
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom"

  (!.py
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom"

  (!.lua
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom")


^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.js
    [(xor true false)
     (xor true true)])
  => [false true]

  (!.py
    [(xor true false)
     (xor true true)])
  => [false true]

  (!.lua
    [(xor true false)
     (xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.js
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21

  (!.py
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21

  (!.lua
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.js
    (->> 5
                   (- 10)))
  => 5

  (!.py
    (->> 5
                   (- 10)))
  => 5

  (!.lua
    (->> 5
                   (- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.js
    [(and true 1)
     (and true false)])
  => [1 false]

  (!.py
    [(and true 1)
     (and true false)])
  => [1 false]

  (!.lua
    [(and true 1)
     (and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.js
    (do
      (comment (throw "boom"))
      1))
  => 1

  (!.py
    (do
      (comment (throw "boom"))
      1))
  => 1

  (!.lua
    (do
      (comment (throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.js
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b"

  (!.py
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b"

  (!.lua
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.js
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2]

  (!.py
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2]

  (!.lua
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.js
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes"

  (!.py
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes"

  (!.lua
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.js
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5

  (!.py
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5

  (!.lua
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.js
    [(not true)
     (not false)])
  => [false true]

  (!.py
    [(not true)
     (not false)])
  => [false true]

  (!.lua
    [(not true)
     (not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.js
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1]

  (!.py
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1]

  (!.lua
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.js
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]}

  (!.py
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]}

  (!.lua
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.js
    (var out [])
    (try
      (throw "boom")
      (catch err
          (xt/x:arr-push out err))
      (finally
        (xt/x:arr-push out "done")))
    (return out))
  => ["boom" "done"]

  ^*(!.py
    (var out [])
    (try
      (throw "boom")
      (catch err
          (xt/x:arr-push out err))
      (finally
        (xt/x:arr-push out "done")))
    (return out))
  => ["boom" "done"]

  ^*(!.lua
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

  (!.js
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2]

  (!.py
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2]

  (!.lua
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.js
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2]

  (!.py
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2]

  (!.lua
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.js
    ((fn [x]
       (return (+ x 1)))
     2))
  => 3

  (!.py
    ((fn [x]
       (return (+ x 1)))
     2))
  => 3

  (!.lua
    ((fn [x]
       (return (+ x 1)))
     2))
  => 3)

(comment
  
  (s/snapto)
  (s/run '[xt.lang.spec-primitive])
  (s/run '[xt.lang])
  (s/seedgen-benchadd '[xt.lang.spec] {:lang [:r :php :dart :julia] :write true})
  (s/seedgen-langadd  '[xt.lang.spec-primitive] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.spec-primitive] {:lang [:lua :python] :write true}))
