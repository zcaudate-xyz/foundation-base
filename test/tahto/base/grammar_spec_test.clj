(ns tahto.base.grammar-spec-test
  (:require [tahto.base.emit-common :as common]
             [tahto.base.emit-helper :as helper]
             [tahto.base.grammar-spec :refer :all])
  (:use code.test))

(defn mixin-add-symbol
  [m]
  {:symbol *symbol*
   :base (:base m)})

^{:refer tahto.base.grammar-spec/get-comment :added "4.0"}
(fact "gets the comment access prefix for a language"

  (get-comment helper/+default+ {})
  => "//")

^{:refer tahto.base.grammar-spec/format-fargs :added "3.0"}
(fact "formats function inputs"

  (format-fargs '[[a b]])
  => '["" {} ([a b])]

  (format-fargs '["docstring" [a b]])
  => '["docstring" {} ([a b])]

  (format-fargs '(([a] a)
                  ([a b] (+ a b))))
  => '["" {} (([a] a)
              ([a b] (+ a b)))])

^{:refer tahto.base.grammar-spec/format-defn-mixins :added "4.1"}
(fact "applies mixins from symbols, forms and maps in order"
  (binding [*symbol* 'hello]
    [(format-defn-mixins {:base true}
                         ['tahto.base.grammar-spec-test/mixin-add-symbol])
     (format-defn-mixins {:base true}
                         ['(hash-map :from-form *symbol*)])
     (format-defn-mixins {:base true}
                         ['{:from-map true}])])
  => '[{:symbol hello
        :base true}
       {:from-form hello}
       {:base true
        :from-map true}])

^{:refer tahto.base.grammar-spec/format-defn :added "3.0"}
(fact "standardize defn forms"

  (format-defn '(defn hello "hello" {:list 1} []))
  => '[{:list 1, :doc "hello"} (defn hello [])])

^{:refer tahto.base.grammar-spec/tf-for-index :added "4.0"}
(fact "default for-index transform"

  (tf-for-index '(for:index
                  [i [0 2 3]]))
  => '(for [(var i := 0) (< i 2) (:= i (+ i 3))]))