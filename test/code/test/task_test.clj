(ns code.test.task-test
  (:use code.test)
  (:require [code.test.task :as task]
            [std.task :as std.task]
            [code.test.base.executive :as executive]))

(fact "run:interrupt function"
  (std.task/with-context {:interrupt (atom false)}
    (task/run:interrupt)
    @(:interrupt std.task/*context*))
  => true)

(fact "run function with single namespace"
  (try
    (task/run 'code.test.task-test)
    => map?
    (finally (std.task/purge))))

(fact "run:current function"
  (try
    (task/run:current)
    => map?
    (finally (std.task/purge))))

(fact "run:test function"
  (try
    (task/run:test)
    => map?
    (finally (std.task/purge))))

(fact "run:unload and run:load functions"
  (let [ns 'code.test.task-test]
    (try
      (task/run:unload ns)
      (find-ns ns) => nil?
      (task/run:load ns)
      (find-ns ns) => not-nil?
      (finally (std.task/purge)))))

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
