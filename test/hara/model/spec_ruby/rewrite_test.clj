(ns hara.model.spec-ruby.rewrite-test
  (:require [hara.model.spec-ruby.rewrite :as rewrite])
  (:use code.test))

^{:refer hara.model.spec-ruby.rewrite/rewrite-callable-body :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/rewrite-callable-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/rewrite-callable-value :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/rewrite-captured-callables :added "4.1"}
(fact "rewrites only nested callable bodies to use capture aliases"
  (rewrite/rewrite-captured-callables
   '[(:= out
        (promise-then out
                      (fn [_]
                        (return value))))
     (return value)]
   '{value value__capture__})
  => '[(:= out
         (promise-then out
                       (. (fn [value__capture__]
                            (return
                             (fn [_]
                               (return value__capture__))))
                          (call value))))
       (return value)]

  (rewrite/rewrite-captured-callables
   '[(fn [value]
       (return value))]
   '{value value__capture__})
  => '[(fn [value]
         (return value))])

^{:refer hara.model.spec-ruby.rewrite/ruby-rewrite-generator-body :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/rewrite-callable-forms :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/mark-inline-defs :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-ruby.rewrite/ruby-rewrite-stage :added "4.1"}
(fact "marks runtime-eval helper defs as inner for Ruby without changing normal staging"
  (let [plain (rewrite/ruby-rewrite-stage
               '(do
                  (defn helper [] (return 1))
                  (helper))
               nil)
        evald (rewrite/ruby-rewrite-stage
               '(do
                  (defn helper [] (return 1))
                  (helper))
               {:mopts {:emit {:body {:transform identity}}}})]
    [(boolean (-> plain second second meta :inner))
     (boolean (-> evald second second meta :inner))])
  => [false true])
