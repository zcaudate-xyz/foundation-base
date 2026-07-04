(ns indigo.build.build-umd-test
  (:require [indigo.build.build-umd :refer :all])
  (:use code.test))

^{:refer indigo.build.build-umd/initialise :added "4.0"}
(fact "initialise is available"
  (fn? initialise)
  => true)

^{:refer indigo.build.build-umd/generate-umd-install :added "4.0"}
(fact "generate-umd-install is available"
  (fn? generate-umd-install)
  => true)

^{:refer indigo.build.build-umd/generate-umd :added "4.0"}
(fact "generate-umd is available"
  (fn? generate-umd)
  => true)

^{:refer indigo.build.build-umd/generate-entry :added "4.0"}
(fact "generate-entry is available"
  (fn? generate-entry)
  => true)

^{:refer indigo.build.build-umd/list-packages :added "4.0"}
(fact "lists configured packages"
  (list-packages)
  => (keys +packages+))

^{:refer indigo.build.build-umd/build-package :added "4.0"}
(fact "build-package is available"
  (fn? build-package)
  => true)