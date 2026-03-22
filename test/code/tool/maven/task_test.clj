(ns code.tool.maven.task-test
  (:require [code.tool.maven.task :refer :all])
  (:use code.test))

^{:refer code.tool.maven.task/make-project :added "3.0"}
(fact "makes a maven compatible project"

  (make-project)
  => map?)
