(ns std.object.element.common-test
  (:require [std.object.element.common :refer :all])
  (:use code.test))

^{:refer std.object.element.common/context-class :added "3.0"}
(fact "If x is a class, return x otherwise return the class of x"

  (context-class String)
  => String

  (context-class "")
  => String)

^{:refer std.object.element.common/assignable? :added "3.0"}
(fact "checks whether a class is assignable to another in sequence"
  (assignable? [String] [CharSequence])
  => true

  (assignable? [String Integer Long] [CharSequence Number Number])
  => true

  (assignable? [CharSequence] [String])
  => false)

^{:refer std.object.element.common/-invoke-element :added "3.0"}
(comment "base method for extending `invoke` for all element types")

^{:refer std.object.element.common/-to-element :added "3.0"}
(comment "base method for extending creating an element from java.reflect objects")

^{:refer std.object.element.common/-element-params :added "3.0"}
(comment "base method for extending `:params` entry for all element types")

^{:refer std.object.element.common/-format-element :added "3.0"}
(comment "base method for extending `toString` entry for all element types")

^{:refer std.object.element.common/->Element :added "3.0" :adopt true}
(fact "defines an `std.object.element.Element` instance"

  (->Element {})
  ;; #elem[uninitialised]
  )

^{:refer std.object.element.common/element :added "3.0"}
(fact "creates a element from a map"

  (element {})
  => std.object.element.common.Element)

^{:refer std.object.element.common/element? :added "3.0"}
(fact "checker for the element type"

  (element? (element {}))
  => true)

(comment
  (code.manage/import)

  (std.object.query/query-class String []))
