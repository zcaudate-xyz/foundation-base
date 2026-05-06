(ns xtbench.ruby.sample.train-004-test
  (:use code.test)
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify]))

(l/script- :ruby
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.lang.common-repl :as repl]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.spec-base/example-a :added "4.1"}
(fact "muliple checks are also allowed"

  (!.rb
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.rb
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)

^{:refer xt.lang.spec-base/example-a :added "4.1"}
(fact "muliple checks are also allowed"

  (!.rb
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.rb
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)

^{:refer xt.lang.spec-base/example-b :added "4.1"}
(fact "forms can be suppressed"

  (!.rb
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.rb
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)

^{:refer xt.lang.spec-base/example-b :added "4.1"}
(fact "forms can be suppressed"

  (!.rb
    (xt/x:apply (fn [a b c]
                  (return (+ a b c)))
                [1 2 3]))
  => 6

  (!.rb
    (xt/x:apply (fn [a b c d]
                  (return (+ a b c d)))
                [1 2 3 4]))
  => 10)

^{:refer xt.lang.spec-base/example-c :added "4.1"}
(fact "order is important"

  (notify/wait-on :ruby
    (repl/notify 1))
  => 1)

^{:refer xt.lang.spec-base/example-c :added "4.1"}
(fact "order is important"

  (notify/wait-on :ruby
    (repl/notify 1))
  => 1)

^{:refer xt.lang.spec-base/example-d :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  [(!.rb 1)
   (inc 0)
   (notify/wait-on :ruby
     (repl/notify 1))]
  => [1 1])

^{:refer xt.lang.spec-base/example-d :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  [(!.rb 1)
   (inc 0)
   (notify/wait-on :ruby
     (repl/notify 1))]
  => [1 1])

^{:refer xt.lang.spec-base/example-e :added "4.1"}
(fact "seed meta can be mixed and matched"

  (identity (!.rb 1))
  => 1

  (identity (!.rb 2))
  => 2)

^{:refer xt.lang.spec-base/example-e :added "4.1"}
(fact "seed meta can be mixed and matched"

  (identity (!.rb 1))
  => 1

  (identity (!.rb 2))
  => 2)

^{:refer xt.lang.spec-base/example-f :added "4.1"}
(fact "expect can be customised"

  (!.rb    
    (xt/x:offset 10))
  => 10

  (!.rb    
    (xt/x:offset 10))
  => python)

^{:refer xt.lang.spec-base/example-f :added "4.1"}
(fact "expect can be customised"

  (!.rb    
    (xt/x:offset 10))
  => 10

  (!.rb    
    (xt/x:offset 10))
  => python)

^{:refer xt.lang.spec-base/example-fa :added "4.1"}
(fact "expect can be customised"

  (!.rb    
    (xt/x:offset 10))
  => 10)

^{:refer xt.lang.spec-base/example-fa :added "4.1"}
(fact "expect can be customised"

  (!.rb    
    (xt/x:offset 10))
  => 10)

^{:refer xt.lang.spec-base/example-g :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  [(!.rb 1)
   (inc 0)
   (notify/wait-on :ruby
     (repl/notify 1))]
  => [1 1])

^{:refer xt.lang.spec-base/example-g :added "4.1"}
(fact "any form is allowed with :seedgen/base meta"

  [(!.rb 1)
   (inc 0)
   (notify/wait-on :ruby
     (repl/notify 1))]
  => [1 1])
