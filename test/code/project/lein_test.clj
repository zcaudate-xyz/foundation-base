(ns code.project.lein-test
  (:require [code.project.lein :refer :all])
  (:use code.test))

^{:refer code.project.lein/project :added "3.0"}
(fact "returns the root project map"

  (project)
  => map?)

^{:refer code.project.lein/project-name :added "3.0"}
(fact "returns the project name"

  (project-name)
  => symbol?)
