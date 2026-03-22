(ns code.project.shadow-test
  (:require [code.project.shadow :refer :all])
  (:use code.test))

^{:refer code.project.shadow/project :added "3.0"}
(comment "opens a shadow.edn file as the project"

  (project "../yin/shadow-cljs.edn"))
