(ns std.dom.mock-test
  (:require [std.dom.common :as base]
            [std.dom.item :as item]
            [std.dom.mock :refer :all])
  (:use code.test))

^{:refer std.dom.mock/mock? :added "3.0"}
(fact "checks if object is a mock item"

  (-> (item/item-create :mock/label)
      (mock?))
  => true)

^{:refer std.dom.mock/mock-format :added "3.0"}
(fact "checks if object is a mock item"

  (-> (item/item-create :mock/label {:text "hello"})
      (mock-format))
  => [:mock/label "hello"]

  (-> (item/item-create :mock/pane {:children ["a" "b" "c"]})
      (mock-format))
  => [:mock/pane "a" "b" "c"])

^{:refer std.dom.mock/item-props-delete-mock :added "3.0"}
(fact "custom props delete function for mock item"

  (-> (item-props-delete-mock :mock/pane
                              (item/item-create :mock/pane {:a 1 :b 2})
                              {:b 2})
      :props)
  => {:a 1})

^{:refer std.dom.mock/item-props-set-mock :added "3.0"}
(fact "custom props update function for mock item"

  (-> (item-props-set-mock :mock/pane
                           (item/item-create :mock/pane {:a 1})
                           {:b 2 :c 3})
      :props)
  => {:a 1, :b 2, :c 3})

^{:refer std.dom.mock/item-set-list-mock :added "3.0"}
(fact "custom props set list function for mock item"

  (-> (item-set-list-mock :mock/pane
                          (item/item-create :mock/pane)
                          :a [1 2 3 4])
      :props)
  => {:a [1 2 3 4]})
