(ns std.lang.model-annex.spec-ruby.rewrite-test
  (:require [std.lang.model-annex.spec-ruby.rewrite :as rewrite])
  (:use code.test))

^{:refer std.lang.model-annex.spec-ruby.rewrite/ruby-rewrite-stage :added "4.1"}
(fact "rewrites callable vars during runtime-eval staging for Ruby"
  (rewrite/ruby-rewrite-stage
   '(do
      (var f (fn [x] x))
      (f 1))
   {:mopts {:emit {:body {:transform identity}}}})
  => '(do
        (var f (fn [x] x))
        (. f (call 1))))

^{:refer std.lang.model-annex.spec-ruby.rewrite/ruby-rewrite-stage :added "4.1"}
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
