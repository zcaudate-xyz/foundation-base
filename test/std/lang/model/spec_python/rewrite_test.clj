(ns std.lang.model.spec-python.rewrite-test
  (:require [clojure.walk]
            [std.lang.model.spec-python :as py]
            [std.lang.model.spec-python.rewrite :as rewrite])
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
(fact "hoists single-body block callbacks for python"
  (let [out (rewrite/python-rewrite-stage
             '(var f (xtd/arr-keep
                      [1 2 3 4]
                      (fn [x]
                        (if (== 0 (mod x 2))
                          (return (* x 10))
                          (return nil)))))
             {:grammar py/+grammar+})
        [_ binding assign] out
        callback          (second binding)]
    [(= 'do* (first out))
     (symbol? callback)
     (.startsWith (name callback) "py_callback__")
     (= '(fn [x]
           (if (== 0 (mod x 2))
             (return (* x 10))
             (return nil)))
        (nth binding 2))
     (= '(var f (xtd/arr-keep [1 2 3 4] CALLBACK))
        (clojure.walk/prewalk-replace {callback 'CALLBACK} assign))])
  => [true true true true true])

^{:refer std.lang.model.spec-python.rewrite/python-rewrite-stage :added "4.1"}
(fact "keeps top-level named functions intact"
  (rewrite/python-rewrite-stage
   '(fn f-raw [x]
      (return (* x 10)))
   nil)
  => '(fn f-raw [x]
        (return (* x 10))))
