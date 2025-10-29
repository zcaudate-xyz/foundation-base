(ns code.test.base.executive-test
  (:use [code.test :exclude [run]])
  (:require [code.test.base.executive :refer :all]
            [code.project :as project]))

^{:refer code.test.base.executive/accumulate :added "3.0"}
(fact "accumulates test results from various facts and files into a single data structure"))

^{:refer code.test.base.executive/interim :added "3.0"}
(fact "summary function for accumulated results")

^{:refer code.test.base.executive/retrieve-line :added "3.0"}
(fact "returns the line of the test")

^{:refer code.test.base.executive/summarise :added "3.0"}
(fact "creates a summary of given results")

^{:refer code.test.base.executive/summarise-bulk :added "3.0"}
(fact "creates a summary of all bulk results")

^{:refer code.test.base.executive/unload-namespace :added "3.0"}
(fact "unloads a given namespace for testing")

^{:refer code.test.base.executive/load-namespace :added "3.0"}
(fact "loads a given namespace for testing")

^{:refer code.test.base.executive/test-namespace :added "3.0"}
(fact "runs a loaded namespace")

^{:refer code.test.base.executive/run-namespace :added "3.0"}
(fact "loads and run the namespace")

^{:refer code.test.base.executive/run-current :added "4.0"}
(fact "runs the current namespace (which can be a non test namespace)")

^{:refer code.test.base.executive/eval-namespace :added "3.0"}
(fact "evaluates the code within a specified namespace"))

(comment

  (def res (project/in-context (run 'std.lib-test)))

  (->> (:passed res)
       (count)))

(comment
  (->> (common/all-functions 'platform.storage-test)
       (sort-by (comp :line second))
       (map (juxt first (comp :line second))))

  (project/in-context (run 'std.lib))
  (project/in-context (load-namespace 'std.lib))
  (time (do
          (dotimes [i 10]
            (project/in-context (test-namespace 'std.lib {:test {:order :random
                                                                   ;;:parallel true
                                                                 }})))
          nil))
  (project/in-context (run 'platform.storage)))
