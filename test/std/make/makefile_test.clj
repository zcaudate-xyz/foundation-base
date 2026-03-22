(ns std.make.makefile-test
  (:require [std.make.makefile :refer :all])
  (:use code.test))

^{:refer std.make.makefile/emit-headers :added "4.0"}
(fact "emits makefile headers"

  (emit-headers {:CC :gcc})
  => "CC = gcc")

^{:refer std.make.makefile/emit-target :added "4.0"}
(fact "emits all makefile targets")

^{:refer std.make.makefile/write :added "4.0"}
(fact "link to `std.lang.compile/compile-ext-fn`")
