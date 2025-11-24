(ns std.make-test
  (:use code.test)
  (:require [std.make :as make]
            [std.make.github :as github]))

^{:refer std.make/gh:dwim-init :added "4.0"}
(fact "prepares the initial project commit"

  (make/gh:dwim-init {})
  => any?)

^{:refer std.make/gh:dwim-push :added "4.0"}
(fact "prepares the project push"

  (make/gh:dwim-push {})
  => any?)
