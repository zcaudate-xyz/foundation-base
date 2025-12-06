(ns code.tool.maven.task-test
  (:use code.test)
  (:require [code.tool.maven.task :refer :all]))

^{:refer code.tool.maven.task/make-project :added "3.0"}
(fact "makes a maven compatible project"

  (make-project)
  => map?)
