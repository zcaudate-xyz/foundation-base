(ns rt.basic.impl.process-ruby-test
  (:require [rt.basic.impl.process-ruby :refer :all]
             [rt.basic.type-common :as common]
             [std.lang.base.runtime :as rt]
             [std.lang :as l])
  (:use code.test))

(l/script- :ruby
  {:runtime :oneshot})

(comment
  (l/rt:restart))

(def CANARY-RUBY
  (common/program-exists? "ruby"))

^{:refer rt.basic.impl.process-ruby/CANARY :adopt true :added "4.0"}
(fact "EVALUATE ruby code"

  (if CANARY-RUBY
    (!.rb
      (. (fn []
           (+ 1 2))
         (call)))
    :ruby-unavailable)
  => (any 3 :ruby-unavailable)

  (default-oneshot-wrap "1")
  => string?)




(comment

  (defn.rb add
    []
    (return (+ 1 2 3)))

  (default-basic-client 1000 {:host "localhost"}))


^{:refer std.lang.base.runtime/return-wrap-invoke :added "4.1"}
(fact "wraps forms for invoke"
  (rt/return-wrap-invoke '[1 2 3])
  => seq?)

^{:refer rt.basic.impl.process-ruby/default-body-transform :added "4.1"}
(fact "applies ruby return transform"
  (default-body-transform '[1 2 3] {})
  => '(do (:= OUT [1 2 3]))

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 (:= OUT 3)))


^{:refer rt.basic.impl.process-ruby/default-body-wrap :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-ruby/normalize-forms :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-ruby/mark-inline-defs :added "4.1"}
(fact "TODO")