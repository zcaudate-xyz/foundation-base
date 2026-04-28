(ns std.lang.rewrite.fn-test
  (:require [clojure.string :as str]
            [std.lang.rewrite.fn :as fnrw])
  (:use code.test))

(defn- passthrough-rewrite
  [forms]
  forms)

^{:refer std.lang.rewrite.fn/do-form? :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/fn-form? :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/fn-parts :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/splice-do* :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/wrap-body :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/rewrite-fn-form :added "4.1"}
(fact "rewrites function bodies with optional preparation"
  (fnrw/rewrite-fn-form
   '(fn named [x]
      (step x)
      (return x))
   passthrough-rewrite
   {:prepare-body reverse})
  => '(fn named [x]
        (do
          (return x)
          (step x))))

^{:refer std.lang.rewrite.fn/rewrite-fn-body :added "4.1"}
(fact "TODO")

^{:refer std.lang.rewrite.fn/normalize-fn :added "4.1"}
(fact "normalizes lifted functions to anonymous block bodies"
  (fnrw/normalize-fn '(fn named [x]
                        (step x)
                        (return x))
                     passthrough-rewrite)
  => '(fn [x]
        (do
          (step x)
          (return x))))

^{:refer std.lang.rewrite.fn/lambda-compatible? :added "4.1"}
(fact "detects lambda-compatible function forms"
  (fnrw/lambda-compatible? '(fn [x] (return x)))
  => true

  (fnrw/lambda-compatible? '(fn named [x] (return x)))
  => false

  (fnrw/lambda-compatible? '(fn [x]
                              (if cond
                                (return x)
                                (return nil)))
                          (fn [form]
                            (and (seq? form)
                                 (= 'if (first form)))))
  => false

  (fnrw/lambda-compatible? '(fn [x] (do (step x) (return x))))
  => false)

^{:refer std.lang.rewrite.fn/lift-named-lambda :added "4.1"}
(fact "lifts named lambdas into prior var bindings"
  (fnrw/lift-named-lambda '(fn named [x] (return x))
                          passthrough-rewrite
                          {:symbol-prefix "test_lambda__"})
  => '[[(var named (fn [x] (return x)))] named]

  (let [[prefix sym] (fnrw/lift-named-lambda '(fn [x]
                                                (return x))
                                             passthrough-rewrite
                                             {:symbol-prefix "test_lambda__"})]
    [(symbol? sym)
     (str/starts-with? (name sym) "test_lambda__")
     (= 'var (ffirst prefix))
     (= sym (second (first prefix)))
     (= '(fn [x] (return x)) (nth (first prefix) 2))])
  => [true true true true true])