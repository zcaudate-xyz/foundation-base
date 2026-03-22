(ns std.make.readme-test
  (:require [std.make.readme :refer :all])
  (:use code.test))

^{:refer std.make.readme/has-orgfile? :added "4.0"}
(fact "checks that an orgfile exists")

^{:refer std.make.readme/tangle-params :added "4.0"}
(fact "gets :tangle params")

^{:refer std.make.readme/tangle-parse :added "4.0"}
(fact "parses an org file")

^{:refer std.make.readme/tangle :added "4.0"}
(fact "tangles a file")

^{:refer std.make.readme/make-readme-raw :added "4.0"}
(fact "simple filter to strip out `* Build` block ")

^{:refer std.make.readme/make-readme :added "4.0"}
(fact "makes a README.md from Main.org")
