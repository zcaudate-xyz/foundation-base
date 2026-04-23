(ns std.lang.rewrite.hoist-test
  (:require [std.lang.rewrite.hoist :as hoist])
  (:use code.test))

(def ^:private +rewriter+
  (hoist/create-rewriter
   {:fn-tags #{'fn 'fn.inner}
    :symbol-prefix "test_callback__"}))

^{:refer std.lang.rewrite.hoist/create-rewriter :added "4.1"}
(fact "shares the prefix-hoisting rewrite logic"
  ((:rewrite-stage +rewriter+)
   '(var f (xtd/memoize-key
            (fn f-raw [x]
              (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
              (return (* x 10)))))
   nil)
  => '(do*
       (var f-raw
            (fn [x]
              (do
                (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                (return (* x 10)))))
       (var f (xtd/memoize-key f-raw)))

  ((:rewrite-stage +rewriter+)
   (with-meta
     '[(var state {"n" 0})
       (var f (xtd/memoize-key
               (fn f-raw [x]
                 (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                 (return (* x 10)))))
       [(f 2) (f 2) (f 3) (xt/x:get-key state "n")]]
     {:bulk true})
   {:mopts {:emit {}}})
  => '(do*
       (var state {"n" 0})
       (var f-raw
            (fn [x]
              (do
                (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
                (return (* x 10)))))
       (var f (xtd/memoize-key f-raw))
       [(f 2) (f 2) (f 3) (xt/x:get-key state "n")]))
