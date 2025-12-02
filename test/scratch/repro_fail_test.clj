(ns scratch.repro-fail-test
  (:use code.test)
  (:require [std.lib :as h]))

(fact "this test should fail"
  (+ 1 1) => 3)

(fact "this test should throw"
  (/ 1 0) => 1)
