(ns std.lang.rewrite.hoist-test
  (:require [clojure.walk]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.rewrite.hoist :as hoist])
  (:use code.test))

(def ^:private +grammar+
  (grammar/grammar :test
                   (grammar/to-reserved (grammar/build))
                   helper/+default+))

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
       [(f 2) (f 2) (f 3) (xt/x:get-key state "n")])

  (let [out ((:rewrite-stage +rewriter+)
             '(var f (xtd/arr-keep
                      [1 2 3 4]
                      (fn [x]
                        (if (== 0 (mod x 2))
                          (return (* x 10))
                          (return nil)))))
             {:grammar +grammar+})
        [_ binding assign] out
        callback          (second binding)]
    [(= 'do* (first out))
     (= 'var (first binding))
     (symbol? callback)
     (= '(fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil)))
        (nth binding 2))
     (= '(var f (xtd/arr-keep [1 2 3 4] CALLBACK))
        (clojure.walk/prewalk-replace {callback 'CALLBACK} assign))])
  => [true true true true true])
