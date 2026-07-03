(ns hara.lang.rewrite.fn-test
  (:require [clojure.string :as str]
            [hara.lang.rewrite.fn :as fnrw])
  (:use code.test))

(defn- passthrough-rewrite
  [forms]
  forms)

^{:refer hara.lang.rewrite.fn/do-form? :added "4.0"}
(fact "identifies do and do* forms"
  (fnrw/do-form? '(do 1 2 3))
  => 'do

  (fnrw/do-form? '(do* 1 2 3))
  => 'do*

  (fnrw/do-form? '(fn [x] x))
  => nil

  (fnrw/do-form? 1)
  => false)

^{:refer hara.lang.rewrite.fn/fn-form? :added "4.0"}
(fact "identifies fn forms"
  (fnrw/fn-form? '(fn [x] x))
  => true

  (fnrw/fn-form? '(fn named [x] x))
  => true

  (fnrw/fn-form? '(do 1 2))
  => false

  (fnrw/fn-form? 1)
  => false)

^{:refer hara.lang.rewrite.fn/fn-parts :added "4.0"}
(fact "splits fn forms into name, args and body"
  (fnrw/fn-parts '(fn [x] (return x)))
  => [nil '[x] '((return x))]

  (fnrw/fn-parts '(fn named [x] (return x)))
  => '[named [x] ((return x))]

  (fnrw/fn-parts '(fn [x]))
  => [nil '[x] nil])

^{:refer hara.lang.rewrite.fn/splice-do* :added "4.0"}
(fact "splices do* forms into surrounding list"
  (fnrw/splice-do* '[(do* 1 2) 3])
  => '(1 2 3)

  (fnrw/splice-do* '[1 (do* 2 3) 4])
  => '(1 2 3 4)

  (fnrw/splice-do* '[1 2 3])
  => '(1 2 3)

  (fnrw/splice-do* '[(do* (step x) (return x))])
  => '((step x) (return x)))

^{:refer hara.lang.rewrite.fn/wrap-body :added "4.0"}
(fact "wraps multi-form bodies in do"
  (fnrw/wrap-body [])
  => []

  (fnrw/wrap-body '[(return x)])
  => '((return x))

  (fnrw/wrap-body '[(step x) (return x)])
  => '((do (step x) (return x))))

^{:refer hara.lang.rewrite.fn/rewrite-fn-form :added "4.1"}
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

^{:refer hara.lang.rewrite.fn/rewrite-fn-body :added "4.0"}
(fact "rewrites function bodies, wrapping multi-form bodies in do"
  (fnrw/rewrite-fn-body '(fn [x]
                           (step x)
                           (return x))
                        passthrough-rewrite)
  => '(fn [x]
        (do
          (step x)
          (return x)))

  (fnrw/rewrite-fn-body '(fn named [x] (return x))
                        passthrough-rewrite)
  => '(fn named [x] (return x)))

^{:refer hara.lang.rewrite.fn/normalize-fn :added "4.1"}
(fact "normalizes lifted functions to anonymous block bodies"
  (fnrw/normalize-fn '(fn named [x]
                        (step x)
                        (return x))
                     passthrough-rewrite)
  => '(fn [x]
        (do
          (step x)
          (return x))))

^{:refer hara.lang.rewrite.fn/lambda-compatible? :added "4.1"}
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

^{:refer hara.lang.rewrite.fn/lift-named-lambda :added "4.1"}
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