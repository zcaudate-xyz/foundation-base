(ns hara.lang.rewrite.truthy-test
  (:use code.test)
  (:require [hara.lang.rewrite.truthy :as truthy]))

^{:refer hara.lang.rewrite.truthy/dot-boolish-call? :added "4.0"}
(fact "detects dotted method calls returning booleans"
  [(truthy/dot-boolish-call? '(. obj (containsKey "a")) '#{containsKey})
   (truthy/dot-boolish-call? '(. obj (isEmpty)) '#{isEmpty})
   (truthy/dot-boolish-call? '(. obj (containsKey "a")) '#{isEmpty})
   (truthy/dot-boolish-call? '(containsKey "a") '#{containsKey})
   (truthy/dot-boolish-call? '(. obj "a") '#{containsKey})
   (truthy/dot-boolish-call? '(. obj containsKey) '#{containsKey})]
  => [true true false false false false])

^{:refer hara.lang.rewrite.truthy/boolish-form? :added "4.1"}
(fact "detects boolish forms with configurable rules"
  [(truthy/boolish-form? '(x:eq a b)
                         {:boolish-ops '#{x:eq}})
   (truthy/boolish-form? '(. obj (containsKey "a"))
                         {:dot-boolish-calls '#{containsKey}})
   (truthy/boolish-form? '(and (x:eq a b)
                               (not (x:nil? c)))
                         {:boolish-ops '#{x:eq x:nil?}
                          :recursive-not? true
                          :recursive-and-or? true})
   (truthy/boolish-form? '(or a b)
                         {:boolish-ops '#{x:eq}})]
  => [true true true false])

^{:refer hara.lang.rewrite.truthy/truthy-check-form :added "4.0"}
(fact "builds a truthiness check for a value"
  (truthy/truthy-check-form 'x)
  => '(and (x:not-nil? x) (not= false x)))

^{:refer hara.lang.rewrite.truthy/wrap-truthy-check :added "4.0"}
(fact "wraps a form in a truthiness check, preserving source metadata"
  [(truthy/wrap-truthy-check 'source 'x)
   (truthy/wrap-truthy-check 'source 'x (fn [v] (list 'truthy? v)))
   (meta (truthy/wrap-truthy-check (with-meta 'source {:foo true}) 'x))]
  => ['(and (x:not-nil? x) (not= false x))
      '(truthy? x)
      {:foo true}])

^{:refer hara.lang.rewrite.truthy/truthy-form :added "4.1"}
(fact "wraps non-bool forms with configurable truthy checks"
  [(truthy/truthy-form 'curr
                       'curr
                       #(= 'curr %)
                       (fn [_ form]
                         (list 'wrapped form)))
   (truthy/truthy-form 'curr
                       'curr
                       #(= 'ready %)
                       (fn [_ form]
                         (truthy/wrap-truthy-check form form)))]
  => ['curr
      '(and (x:not-nil? curr)
            (not= false curr))])

^{:refer hara.lang.rewrite.truthy/truthy-or-form :added "4.1"}
(fact "builds value-preserving truthy fallback chains"
  (truthy/truthy-or-form 'source 'value 'fallback)
  => '(:? (and (x:not-nil? value)
               (not= false value))
          value
          fallback))