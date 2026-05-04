(ns hara.model.annex.spec-ruby.rewrite-test
  (:require [hara.model.annex.spec-ruby.rewrite :as rewrite])
  (:use code.test))

^{:refer hara.model.annex.spec-ruby.rewrite/rewrite-callable-body :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/rewrite-callable-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/rewrite-callable-value :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/ruby-rewrite-generator-body :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/rewrite-callable-forms :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/mark-inline-defs :added "4.1"}
(fact "TODO")

^{:refer hara.model.annex.spec-ruby.rewrite/ruby-rewrite-stage :added "4.1"}
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