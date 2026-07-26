tahto/runtime/basic/impl/process_ruby_test.clj:1:(ns tahto.runtime.basic.impl.process-ruby-test
tahto/runtime/basic/impl/process_ruby_test.clj:2:  (:require [tahto.runtime.basic.impl.process-ruby :refer :all]
            [std.lib.env :as env]
tahto/runtime/basic/impl/process_ruby_test.clj:4:            [tahto.common.preprocess-staging :as staging]
            [tahto.core.runtime :as rt]
            [tahto.core :as l]
            [xt.lang.spec-primitive :as primitive])
  (:use code.test))

(l/script- :ruby
  {:runtime :oneshot
   :require [[xt.lang.spec-primitive :as primitive]]})

(fact:global
 {:skip (not (env/program-exists? "ruby"))})

tahto/runtime/basic/impl/process_ruby_test.clj:17:^{:refer tahto.runtime.basic.impl.process-ruby/CANARY :adopt true :added "4.0"}
(fact "EVALUATE ruby code"

  (!.rb
    (. (fn []
         (+ 1 2))
        (call)))
  => 3

  (!.rb
    (primitive/+ 1 2 3 4))
  => 10

  (default-oneshot-wrap "1")
  => #"is_a\?\(Proc\)")

^{:refer tahto.core.runtime/return-wrap-invoke :added "4.1"}
(fact "wraps forms for invoke"
  (rt/return-wrap-invoke '[1 2 3])
  => seq?)

tahto/runtime/basic/impl/process_ruby_test.clj:38:^{:refer tahto.common.preprocess-staging/to-staging :added "4.1"}
 (fact "resolves standalone primitive operators during ruby staging"
  (let [book (l/get-book (l/default-library) :ruby)]
    (first
     (staging/to-staging 'xt.lang.spec-primitive/+
                          (:grammar book)
                          (:modules book)
                          {:lang :ruby
                           :module {:id 'user
                                    :link {'xt.lang.spec-primitive 'xt.lang.spec-primitive}}})))
  => '(fn [x & more] (return (+ x & more))))

tahto/runtime/basic/impl/process_ruby_test.clj:50:^{:refer tahto.runtime.basic.impl.process-ruby/default-body-wrap :added "4.1"}
(fact "assigns the final expression to OUT"
  (default-body-wrap '((defn add-10 [x] (return (+ x 10)))
                       (add-10 5)))
  => '(do
        (defn add-10 [x] (return (+ x 10)))
        (:= OUT (add-10 5))))

tahto/runtime/basic/impl/process_ruby_test.clj:58:^{:refer tahto.runtime.basic.impl.process-ruby/normalize-forms :added "4.1"}
(fact "normalizes a top-level do body"
  (normalize-forms '(do (defn add-10 [x] (return (+ x 10)))
                        (add-10 5))
                   {})
  => '((defn add-10 [x] (return (+ x 10)))
       (add-10 5))

  (normalize-forms '[1 2 3] {:bulk true})
  => '[1 2 3])

tahto/runtime/basic/impl/process_ruby_test.clj:69:^{:refer tahto.runtime.basic.impl.process-ruby/default-body-transform :added "4.1"}
(fact "applies ruby return transform"
  (default-body-transform '[1 2 3] {})
  => '(do (:= OUT [1 2 3]))

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do 1 2 (:= OUT 3))

  (default-body-transform '(do 1 2 3) {})
  => '(do 1 2 (:= OUT 3)))

(comment
  (l/rt:restart))

(comment

  (defn.rb add
    []
    (return (+ 1 2 3)))

  (default-basic-client 1000 {:host "localhost"}))
