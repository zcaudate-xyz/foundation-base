(ns std.lang.rewrite.lift-named-lambda-test
  (:require [clojure.string :as str]
            [std.lang.rewrite.lift-named-lambda :as lift])
  (:use code.test))

(defn- passthrough-rewrite
  [forms]
  forms)

^{:refer std.lang.rewrite.lift-named-lambda/lambda-compatible? :added "4.1"}
(fact "detects lambda-compatible function forms"
  (lift/lambda-compatible? '(fn [x] (return x)))
  => true

  (lift/lambda-compatible? '(fn named [x] (return x)))
  => false

  (lift/lambda-compatible? '(fn [x] (do (step x) (return x))))
  => false)

^{:refer std.lang.rewrite.lift-named-lambda/normalize-fn :added "4.1"}
(fact "normalizes lifted functions to anonymous block bodies"
  (lift/normalize-fn '(fn named [x]
                        (step x)
                        (return x))
                     passthrough-rewrite)
  => '(fn [x]
        (do
          (step x)
          (return x))))

^{:refer std.lang.rewrite.lift-named-lambda/lift-named-lambda :added "4.1"}
(fact "lifts named lambdas into prior var bindings"
  (lift/lift-named-lambda '(fn named [x] (return x))
                          passthrough-rewrite
                          {:symbol-prefix "test_lambda__"})
  => '[[(var named (fn [x] (return x)))] named]

  (let [[prefix sym] (lift/lift-named-lambda '(fn [x]
                                                (return x))
                                             passthrough-rewrite
                                             {:symbol-prefix "test_lambda__"})]
    [(symbol? sym)
     (str/starts-with? (name sym) "test_lambda__")
     (= 'var (ffirst prefix))
     (= sym (second (first prefix)))
     (= '(fn [x] (return x)) (nth (first prefix) 2))])
  => [true true true true true])
