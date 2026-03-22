(ns std.lang.base.grammar-spec-test
  (:require [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar-spec :refer :all])
  (:use code.test))

^{:refer std.lang.base.grammar-spec/get-comment :added "4.0"}
(fact "gets the comment access prefix for a language"

  (get-comment helper/+default+ {})
  => "//")

^{:refer std.lang.base.grammar-spec/format-fargs :added "3.0"}
(fact "formats function inputs"
  ^:hidden
  
  (format-fargs '[[a b]])
  => '["" {} ([a b])]

  (format-fargs '["docstring" [a b]])
  => '["docstring" {} ([a b])])

^{:refer std.lang.base.grammar-spec/format-defn :added "3.0"}
(fact "standardize defn forms"
  ^:hidden
  
  (format-defn '(defn hello "hello" {:list 1} []))
  => '[{:list 1, :doc "hello"} (defn hello [])])

^{:refer std.lang.base.grammar-spec/tf-for-index :added "4.0"}
(fact "default for-index transform"

  (tf-for-index '(for:index
                  [i [0 2 3]]))
  => '(for [(var i := 0) (< i 2) (:= i (+ i 3))]))
