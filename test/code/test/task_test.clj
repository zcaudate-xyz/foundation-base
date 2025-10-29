(ns code.test.task-test
  (:use code.test)
  (:require [code.test.task :as task]))

^{:refer code.test.task/run:interrupt :added "4.0"}
(fact "interrupts the test")

^{:refer code.test.task/run :added "3.0" :class [:test/general]}
(fact "executes all tests, optionally filtering by list or specific namespace")

  (task/run :list)

  (task/run 'std.lib.foundation)
  ;; {:files 1, :thrown 0, :facts 8, :checks 18, :passed 18, :failed 0}
  => map?)

^{:refer code.test.task/run:current :added "4.0"}
(fact "runs the current namespace")

^{:refer code.test.task/run:test :added "3.0"}
(fact "executes tests that are currently loaded in the runtime"))

^{:refer code.test.task/run:unload :added "3.0"}
(fact "unloads the test namespace")

^{:refer code.test.task/run:load :added "3.0"}
(fact "load test namespace")

^{:refer code.test.task/print-options :added "3.0" :class [:test/general]}
(fact "output options for test results"

  (task/print-options)
  => #{:disable :default :all :current :help}

  (task/print-options :default)
  => #{:print-bulk :print-failure :print-thrown} ^:hidden

  (task/print-options :all)
  => #{:print-bulk
       :print-facts-success
       :print-failure
       :print-thrown
       :print-facts
       :print-success})

^{:refer code.test.task/process-args :added "3.0"}
(fact "processes input arguments"

  (task/process-args ["hello"])
  => #{:hello})

^{:refer code.test.task/-main :added "3.0" :class [:test/general]}
(comment "main entry point for leiningen"

  (task/-main))

^{:refer code.test.task/run-errored :added "3.0" :class [:test/general]}
(comment "runs only the tests that have errored"

  (task/run-errored))
