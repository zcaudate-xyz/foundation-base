(ns std.lang.model-annex.spec-julia.rewrite-test
  (:use code.test)
  (:require [std.lang.model-annex.spec-julia :refer :all]
            [std.lang.model-annex.spec-julia.rewrite :as rewrite]))

^{:refer std.lang.model-annex.spec-julia.rewrite/julia-rewrite-stage :added "4.1"}
(fact "normalizes truthy tests and preserves value-default ors for Julia"
  (let [out (rewrite/julia-rewrite-stage
             '(when curr
                (return curr))
             {:grammar +grammar+})]
    [(= 'when (first out))
     (= '(and (x:not-nil? curr) (not= false curr))
        (second out))
     (= '(return curr) (nth out 2))])
  => [true true true]

  (rewrite/julia-rewrite-stage
   '(return (or ready fallback))
   {:grammar +grammar+})
  => '(return (:? (and (x:not-nil? ready)
                       (not= false ready))
                 ready
                 fallback)))

^{:refer std.lang.model-annex.spec-julia.rewrite/julia-rewrite-stage :added "4.1"}
(fact "rewrites set destructuring vars for Julia"
  (let [out (rewrite/julia-rewrite-stage
             '(var #{spaces watch} g)
             {:grammar +grammar+})
        [_ bind extract1 extract2] out
        temp (second bind)]
    [(= 'do* (first out))
     (= 'var (first bind))
     (symbol? temp)
     (= 'g (last bind))
     (= extract1 (list 'var 'spaces (list 'x:get-key temp "spaces" nil)))
     (= extract2 (list 'var 'watch (list 'x:get-key temp "watch" nil)))])
  => [true true true true true true])

^{:refer std.lang.model-annex.spec-julia.rewrite/julia-rewrite-stage :added "4.1"}
(fact "rewrites unpack invokes for Julia"
  (rewrite/julia-rewrite-stage
   '(return (f (x:unpack xs) y))
   {:grammar +grammar+})
  => '(return (f (... xs) y)))
