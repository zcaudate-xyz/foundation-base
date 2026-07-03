(ns hara.lang.rewrite.common-test
  (:use code.test)
  (:require [hara.lang.rewrite.common :refer :all]))

^{:refer hara.lang.rewrite.common/with-form-meta :added "4.1"}
(fact "copies metadata from source to out when out is an IObj"
  (with-form-meta (with-meta [1 2 3] {:hello true}) [4 5 6])
  => [4 5 6]

  (meta (with-form-meta (with-meta [1 2 3] {:hello true}) [4 5 6]))
  => {:hello true})

^{:refer hara.lang.rewrite.common/with-form-meta :added "4.1"}
(fact "returns out unchanged when out is not an IObj"
  (with-form-meta (with-meta [] {:hello true}) "world")
  => "world"

  (with-form-meta (with-meta [] {:hello true}) 42)
  => 42

  (with-form-meta (with-meta [] {:hello true}) :keyword)
  => :keyword)