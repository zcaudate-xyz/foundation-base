(ns std.image.base.size-test
  (:require [std.image.base.size :refer :all])
  (:use code.test))

^{:refer std.image.base.size/size->map :added "3.0"}
(fact "converts a size to a map"

  (size->map [100 200])
  => {:width 100, :height 200}

  (size->map {:width 2 :height 3})
  => {:width 2, :height 3})

^{:refer std.image.base.size/length :added "3.0"}
(fact "calculates the length of the array"

  (length {:width 2 :height 3})
  => 6

  (length [100 200])
  => 20000)

(comment
  (./import)
  (./scaffold))
