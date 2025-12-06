(ns rt.basic.impl.process-ruby-test
  (:use code.test)
  (:require [rt.basic.impl.process-ruby :refer :all]
            [std.lang :as l]))

(l/script- :ruby
  {:runtime :oneshot})

(comment
  (l/rt:restart))

^{:refer rt.basic.impl.process-ruby/CANARY :adopt true :added "4.0"}
(fact "EVALUATE ruby code"
  ^:hidden

  (!.rb
    (. (fn []
         (+ 1 2))
       (call)))
  => 3

  (default-oneshot-wrap "1")
  => string?)




(comment

  (defn.rb add
    []
    (return (+ 1 2 3)))

  (default-basic-client 1000 {:host "localhost"}))
