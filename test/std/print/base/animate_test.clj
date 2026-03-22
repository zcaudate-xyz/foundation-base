(ns std.print.base.animate-test
  (:require [std.print.base.animate :refer :all])
  (:use code.test))

^{:refer std.print.base.animate/print-animation :added "3.0"}
(comment "outputs an animated ascii file"

  (print-animation "test-data/std.print/plane.ascii"))
