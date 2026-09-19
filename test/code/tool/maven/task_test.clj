(ns code.tool.maven.task-test
  (:require [code.project :as project]
            [code.tool.maven :as maven]
            [code.tool.maven.task :refer :all]
            [std.config :as config])
  (:use code.test))

^{:refer code.tool.maven.task/make-project :added "3.0"}
(fact "makes a maven compatible project"

  (make-project)
  => map?)

^{:refer std.config/load :added "4.1.6"}
(fact "loads the all-package Clojars release selector"

  (config/load "config/deploy/clojars.edn")
  => {:type :maven
      :repository "clojars"
      :manifest :all}

  (get-in (config/load "config/deploy.edn")
          [:releases :clojars])
  => {:type :maven
      :repository "clojars"
      :manifest :all})

^{:refer code.project/project :added "4.1.6"}
(fact "exposes package-only and split-package deployment aliases"

  (get-in (project/project) [:aliases "package-clojars"])
  => ["exec" "-ep"
      "(use 'code.tool.maven)   (let [result (package :all {:tag :clojars})] (System/exit (task-exit-code result)))"]

  (get-in (project/project) [:aliases "deploy-clojars"])
  => ["exec" "-ep"
      "(use 'code.tool.maven)   (let [result (deploy :all {:tag :clojars})] (System/exit (task-exit-code result)))"])

^{:refer code.tool.maven/task-exit-code :added "4.1.6"}
(fact "fails the process when a task summary contains errors"

  (maven/task-exit-code {:errors 0}) => 0
  (maven/task-exit-code {:errors 2}) => 1
  (maven/task-exit-code {}) => (throws))
