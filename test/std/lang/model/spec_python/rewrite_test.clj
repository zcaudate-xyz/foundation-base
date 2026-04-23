(ns std.lang.model.spec-python.rewrite-test
  (:require [std.lang.model.spec-python.rewrite :as rewrite])
  (:use code.test))

^{:refer std.lang.model.spec-python.rewrite/python-rewrite-stage :added "4.1"}
(fact "rewrites inline callback functions into prior bindings"
  (rewrite/python-rewrite-stage
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

  (rewrite/python-rewrite-stage
   '(var f-raw
         (fn f-raw [x]
           (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
           (return (* x 10))))
   nil)
  => '(var f-raw
           (fn [x]
             (do
               (xtd/set-pair-step state "n" (+ 1 (xt/x:get-key state "n" 0)))
               (return (* x 10)))))

  (rewrite/python-rewrite-stage
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

^{:refer std.lang.model.spec-python.rewrite/python-rewrite-stage :added "4.1"}
(fact "keeps top-level named functions intact"
  (rewrite/python-rewrite-stage
   '(fn f-raw [x]
      (return (* x 10)))
   nil)
  => '(fn f-raw [x]
        (return (* x 10))))
