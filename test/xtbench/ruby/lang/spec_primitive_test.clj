(ns xtbench.ruby.lang.spec-primitive-test
  (:use code.test)
  (:require [clojure.java.shell :as shell]
            [std.lang :as l]
            [xt.lang.common-notify :as notify]
            [xt.lang.spec-primitive :as primitive]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]
             [xt.lang.spec-primitive :as primitive]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

(defn assert-ruby-syntax!
  [form]
  (let [tmp (java.io.File/createTempFile "ruby-emitter-" ".rb")]
    (try
      (let [code (l/emit-as :ruby [form])]
        (spit tmp code)
        (let [result (shell/sh "ruby" "-c" (.getAbsolutePath tmp))]
          (when-not (zero? (:exit result))
            (throw (ex-info "Invalid emitted Ruby"
                            {:form form
                             :code code
                             :result result}))))
        code)
      (finally
        (.delete tmp)))))

^{:refer xt.lang.spec-primitive/!:G :added "4.1"}
(fact "reads and writes global values"

  (!.rb
    (:= (!:G __PRIMITIVE_TEST__) "alpha")
    (var out (!:G __PRIMITIVE_TEST__))
    out)
  => "alpha")

^{:refer xt.lang.spec-primitive/% :added "4.1"}
(fact "emits internal expressions directly"

  (!.rb
    (% [(+ 1 2) 4]))
  => [3 4])

^{:refer xt.lang.spec-primitive/* :added "4.1"}
(fact "multiplies values"

  (!.rb
    (* 2 3 4))
  => 24)

^{:refer xt.lang.spec-primitive/+ :added "4.1"}
(fact "adds values"

  (!.rb
    (+ 1 2 3 4))
  => 10)

^{:refer xt.lang.spec-primitive/- :added "4.1"}
(fact "subtracts values"

  (!.rb
    [(- 10 3 2)
     (- 5)])
  => [5 -5])

^{:refer xt.lang.spec-primitive/. :added "4.1"}
(fact "indexes values"

  (!.rb
    [(. {:a 1 :b 2} ["b"])
     (. [1 2 3] [1])])
  => [2 2])

^{:refer xt.lang.spec-primitive// :added "4.1"}
(fact "divides values"

  (!.rb
    [(/ 12 3)
     (/ 20 5 2)])
  => [4 2])

^{:refer xt.lang.spec-primitive/< :added "4.1"}
(fact "compares less-than"

  (!.rb
    [(< 1 2)
     (< 2 1)])
  => [true false])

^{:refer xt.lang.spec-primitive/<= :added "4.1"}
(fact "compares less-than-or-equal"

  (!.rb
    [(<= 2 2)
     (<= 3 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/== :added "4.1"}
(fact "compares equality"

  (!.rb
    [(== 2 2)
     (== 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/> :added "4.1"}
(fact "compares greater-than"

  (!.rb
    [(> 3 2)
     (> 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/>= :added "4.1"}
(fact "compares greater-than-or-equal"

  (!.rb
    [(>= 3 3)
     (>= 2 3)])
  => [true false])

^{:refer xt.lang.spec-primitive/b:<< :added "4.1"}
(fact "bit-shifts left"

  (!.rb
    (b:<< 3 2))
  => 12)

^{:refer xt.lang.spec-primitive/b:>> :added "4.1"}
(fact "bit-shifts right"

  (!.rb
    (b:>> 12 2))
  => 3)

^{:refer xt.lang.spec-primitive/b:xor :added "4.1"}
(fact "bitwise xors values"

  (!.rb
    (b:xor 6 3))
  => 5)

^{:refer xt.lang.spec-primitive/br* :added "4.1"}
(fact "branches across control clauses"

  (!.rb
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

  (!.rb
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

  (!.rb
    (do:>
     (var a 1)
     (var b 2)
     (return (+ a b))))
  => 3)

^{:refer xt.lang.spec-primitive/fn:> :added "4.1"}
(fact "creates arrow functions"

  [(boolean (assert-ruby-syntax!
             '((fn:> [x]
                (return (+ x 1)))
              2)))
   (boolean (assert-ruby-syntax!
             '((fn []
                (br*
                  (if false
                    (return "no"))
                  (elseif (> 3 2)
                    (return "yes"))
                  (else
                    (return "fallback")))))))
   (boolean (assert-ruby-syntax!
             '(do:>
               (var a 1)
               (var b 2)
               (return (+ a b)))))
   (boolean (assert-ruby-syntax!
             '(do:>
               (cond
                 false (return "a")
                 (< 1 2) (return "b")
                 :else (return "c")))))
   (boolean (assert-ruby-syntax!
             '(do:>
               (if true
                 (return "yes")
                 (return "no")))))
   (boolean (assert-ruby-syntax!
             '(do:>
               (let [a 2
                      b 3]
                 (return (+ a b))))))
   (boolean (assert-ruby-syntax!
             '(do
                (var out [])
                (try
                  (throw "boom")
                  (catch err
                    (x:arr-push out err))
                  (finally
                    (x:arr-push out "done")))
                (return out))))]
  => [true true true true true true true]

  (!.rb
    ((fn:> [x]
       (return (+ x 1)))
     2))
  => 3)

^{:refer xt.lang.spec-primitive/mod :added "4.1"}
(fact "calculates modulo"

  (!.rb
    (mod 17 5))
  => 2)

^{:refer xt.lang.spec-primitive/not= :added "4.1"}
(fact "compares inequality"

  (!.rb
    [(not= 2 3)
     (not= 2 2)])
  => [true false])

^{:refer xt.lang.spec-primitive/pow :added "4.1"}
(fact "raises powers"

  ^*(!.rb
    (pow 2 5))
  => 32)

^{:refer xt.lang.spec-primitive/tab :added "4.1"}
(fact "creates tables from pairs"

  (!.rb
    (tab [:a 1]
                   [:b 2]))
  => {"a" 1
      "b" 2})

^{:refer xt.lang.spec-primitive/throw :added "4.1"}
(fact "throws values"

  (!.rb
    (do:>
      (try
        (throw "boom")
        (catch err
          (return err)))))
  => "boom")

^{:refer xt.lang.spec-primitive/xor :added "4.1"}
(fact "computes logical xor"

  (!.rb
    [(xor true false)
     (xor true true)])
  => [false true])

^{:refer xt.lang.spec-primitive/-> :added "4.1"}
(fact "threads the first argument"

  (!.rb
    (-> 5
                  (+ 2)
                  (* 3)))
  => 21)

^{:refer xt.lang.spec-primitive/->> :added "4.1"}
(fact "threads the last argument"

  (!.rb
    (->> 5
                   (- 10)))
  => 5)

^{:refer xt.lang.spec-primitive/and :added "4.1"}
(fact "computes logical and"

  (!.rb
    [(and true 1)
     (and true false)])
  => [1 false])

^{:refer xt.lang.spec-primitive/comment :added "4.1"}
(fact "discards commented forms"

  (!.rb
    (do
      (comment (throw "boom"))
      1))
  => 1)

^{:refer xt.lang.spec-primitive/cond :added "4.1"}
(fact "selects the first matching branch"

  (!.rb
    (do:>
      (cond
        false (return "a")
        (< 1 2) (return "b")
        :else (return "c"))))
  => "b")

^{:refer xt.lang.spec-primitive/do :added "4.1"}
(fact "runs sequential expressions"

  (!.rb
    (do
      (var out [])
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2)
      out))
  => [1 2])

^{:refer xt.lang.spec-primitive/if :added "4.1"}
(fact "selects between branches"

  (!.rb
    (do:>
      (if true
        (return "yes")
        (return "no"))))
  => "yes")

^{:refer xt.lang.spec-primitive/let :added "4.1"}
(fact "binds locals"

  (!.rb
    (do:>
      (let [a 2
                      b 3]
        (return (+ a b)))))
  => 5)

^{:refer xt.lang.spec-primitive/not :added "4.1"}
(fact "negates truthiness"

  (!.rb
    [(not true)
     (not false)])
  => [false true])

^{:refer xt.lang.spec-primitive/or :added "4.1"}
(fact "computes logical or"

  (!.rb
    [(or nil "fallback")
     (or 1 2)])
  => ["fallback" 1])

^{:refer xt.lang.spec-primitive/quote :added "4.1"}
(fact "returns quoted literals"

  (!.rb
    (quote {:a 1
                      :b [2 3]}))
  => {"a" 1
      "b" [2 3]})

^{:refer xt.lang.spec-primitive/try :added "4.1"}
(fact "runs catch and finally handlers"

  (!.rb
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

  (!.rb
    (var out [])
    (when true
      (xt/x:arr-push out 1)
      (xt/x:arr-push out 2))
    out)
  => [1 2])

^{:refer xt.lang.spec-primitive/while :added "4.1"}
(fact "loops while conditions hold"

  (!.rb
    (var i 0)
    (var out [])
    (while (< i 3)
      (xt/x:arr-push out i)
      (:= i (+ i 1)))
    out)
  => [0 1 2])

^{:refer xt.lang.spec-primitive/fn :added "4.1"}
(fact "creates functions"

  (!.rb
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
