(ns rt.basic.impl.process-ruby-test
  (:require [rt.basic.impl.process-ruby :refer :all]
              [rt.basic.type-common :as common]
              [std.lang.base.preprocess-staging :as staging]
              [std.lang.base.runtime :as rt]
              [std.lang :as l]
              [xt.lang.spec-primitive :as primitive])
  (:use code.test))

(l/script- :ruby
  {:runtime :oneshot
   :require [[xt.lang.spec-primitive :as primitive]]})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

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

  (if CANARY-RUBY
    (!.rb
      (primitive/+ 1 2 3 4))
    :ruby-unavailable)
  => (any 10 :ruby-unavailable)

  (default-oneshot-wrap "1")
  => #"is_a\?\(Proc\)")

^{:refer std.lang.base.runtime/return-wrap-invoke :added "4.1"}
(fact "wraps forms for invoke"
  (rt/return-wrap-invoke '[1 2 3])
  => seq?)

^{:refer std.lang.base.preprocess-staging/to-staging :added "4.1"}
(fact "keeps standalone primitive operators in reserved form during ruby staging"
  (let [book (l/get-book (l/default-library) :ruby)]
    (first
     (staging/to-staging '(xt.lang.spec-primitive/+)
                         (:grammar book)
                         (:modules book)
                         {:lang :ruby
                          :module {:id 'user
                                   :link {'xt.lang.spec-primitive 'xt.lang.spec-primitive}}})))
  => '+)

^{:refer rt.basic.impl.process-ruby/default-body-wrap :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-ruby/normalize-forms :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-ruby/mark-inline-defs :added "4.1"}
(fact "TODO")

^{:refer rt.basic.impl.process-ruby/default-body-transform :added "4.1"}
(fact "applies ruby return transform"
  (default-body-transform '[1 2 3] {})
  => '(do (:= OUT [1 2 3]))

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 (:= OUT 3)))

(comment
  (l/rt:restart))

(comment

  (defn.rb add
    []
    (return (+ 1 2 3)))

  (default-basic-client 1000 {:host "localhost"}))
