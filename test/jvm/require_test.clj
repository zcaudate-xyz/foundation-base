(ns jvm.require-test
  (:require [jvm.require :refer :all])
  (:use code.test))

^{:refer jvm.require/force-require :added "4.0"}
(fact "a more flexible require, reloading namespaces if errored"
  (force-require 'jvm.require)
  => nil)
