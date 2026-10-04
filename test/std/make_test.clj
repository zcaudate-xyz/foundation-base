(ns std.make-test
  (:require [std.make :as make]
            [std.make.github :as github])
  (:use code.test))

^{:refer std.make/build-triggered-single :added "4.1"}
(fact "publishes the focused triggered build function"
  [(fn? make/build-triggered-single)
   (:public (meta #'make/build-triggered-single))]
  => [true true])

^{:refer std.make/gh:dwim-init :added "4.0"}
(fact "prepares the initial project commit"

  (make/gh:dwim-init {})
  => any?)

^{:refer std.make/gh:dwim-push :added "4.0"}
(fact "prepares the project push"

  (make/gh:dwim-push {})
  => any?)
