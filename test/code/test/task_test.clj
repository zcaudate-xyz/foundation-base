(ns code.test.task-test
  (:require [code.project :as project]
            [code.test.task :as task])
  (:use code.test))

^{:refer code.test.task/run:interrupt :added "4.0"}
(fact "interrupts the test")

^{:refer code.test.task/resolve-files :added "4.1"}
(fact "resolves source and test file paths to test namespaces"

  (task/resolve-files ["src/code/project.clj"
                       "test/std/task_test.clj"]
                      (project/project))
  => ['code.project-test
      'std.task-test]

  (task/resolve-files "test/code/project_test.clj test/code/test/task_test.clj"
                      (project/project))
  => ['code.project-test
      'code.test.task-test])

^{:refer code.test.task/run :added "3.0" :class [:test/general]}
(fact "executes all tests, optionally filtering by list or specific namespace"

  (task/run :list)

  (task/run 'std.lib.foundation)
  ;; {:files 1, :thrown 0, :facts 8, :checks 18, :passed 18, :failed 0}
  => map?)

^{:refer code.test.task/run:current :added "4.0"}
(fact "runs the current namespace")

^{:refer code.test.task/run:test :added "3.0"}
(fact "executes tests that are currently loaded in the runtime")

^{:refer code.test.task/run:unload :added "3.0"}
(fact "unloads the test namespace")

^{:refer code.test.task/run:load :added "3.0"}
(fact "load test namespace")

^{:refer code.test.task/run-errored :added "3.0" :class [:test/general]}
(comment "runs only the tests that have errored"

  (task/run-errored))

^{:refer code.test.task/print-options :added "3.0" :class [:test/general]}
(fact "output options for test results"

  (task/print-options)
  => #{:disable :default :all :current :help}

  (task/print-options :default)
  => #{:print-failed :print-bulk :print-timeout :print-throw}

  (task/print-options :all)
  #{:print-failed
    :print-bulk
    :print-facts-success
    :print-timeout
    :print-facts
    :print-throw
    :print-success})

^{:refer code.test.task/process-test-args :added "4.1"}
(fact "parses code.test CLI args, including multiple :only namespaces"

  (task/process-test-args [":only" "foo"])
  => {:ns '[foo]}

  (task/process-test-args [":only" "foo" "bar" "baz"])
  => {:ns '[foo bar baz]}

  (task/process-test-args [":only" "foo" "bar" ":timeout" "100"])
  => {:ns '[foo bar] :timeout 100})

^{:refer code.test.task/-main :added "3.0" :class [:test/general]}
(comment "main entry point for leiningen"

  (task/-main))