(ns code.manage.fn-format-test
  (:use code.test)
  (:require [code.manage.fn-format :refer :all]
            [code.edit :as nav]))

^{:refer code.manage.fn-format/list-transform :added "3.0"}
(fact "transforms `(.. [] & body)` to `(.. ([] & body))`"
  (-> (nav/parse-string "(defn foo [x] 1)")
      (nav/down)
      (nav/right)
      (nav/right)
      (list-transform)
      (nav/root-string))
  => "(defn foo\n  ([x] 1))")

^{:refer code.manage.fn-format/fn:list-forms :added "3.0"}
(fact "query to find `defn` and `defmacro` forms with a vector"
  (-> (nav/parse-string "(defn foo [x] 1)")
      (fn:list-forms)
      (nav/root-string))
  => "(defn foo\n  ([x] 1))")

^{:refer code.manage.fn-format/fn:defmethod-forms :added "3.0"}
(fact "query to find `defmethod` forms with a vector"
  (-> (nav/parse-string "(defmethod foo :x [y] 1)")
      (fn:defmethod-forms)
      (nav/root-string))
  => "(defmethod foo :x\n  ([y] 1))")

^{:refer code.manage.fn-format/fn-format :added "3.0"}
(fact "function to refactor the arglist and body"
  (with-redefs [code.framework/refactor-code (fn [ns params lookup project]
                                               {:updated true})]
    (fn-format 'code.manage.fn-format
               {}
               nil
               nil))
  => {:updated true})
