(ns std.log.core-test
  (:use code.test)
  (:require [std.log.core :refer :all]
            [std.log.common :as common]
            [std.lib :as h]
            [std.print :as print]))

^{:refer std.log.core/logger-submit :added "3.0"}
(fact "basic submit function")

^{:refer std.log.core/logger-process :added "3.0"}
(fact "a special :fn key for processing the input")

^{:refer std.log.core/logger-enqueue :added "3.0"}
(fact "submits a task to the logger job queue")

^{:refer std.log.core/process-exception :added "3.0"}
(fact "converts an exception into a map"

  (process-exception (ex-info "Error" {}))
  => (contains-in {:cause "Error",
                   :via [{:message "Error", :data {}}],
                   :trace [], :data {}, :message "Error"}))

^{:refer std.log.core/logger-message :added "3.0"}
(fact "constructs a logger message"

  (logger-message :debug {} nil)
  => map?)

^{:refer std.log.core/logger-start :added "3.0"}
(fact "starts the logger, initating a queue and executor")

^{:refer std.log.core/logger-info :added "3.0"}
(fact "returns information about the logger"

  (logger-info (common/default-logger))
  => {:level :debug, :type :console})

^{:refer std.log.core/logger-stop :added "3.0"}
(fact "stops the logger, queue and executor")

^{:refer std.log.core/logger-init :added "3.0"}
(fact "sets defaults for the logger"

  (logger-init {:type :basic})
  => (contains {:type :basic,
                :instance (stores {:level :debug})}))

^{:refer std.log.core/identity-logger :added "3.0"}
(fact "creates a identity logger"

  (identity-logger))

^{:refer std.log.core/multi-logger :added "3.0"}
(fact "creates multiple loggers"

  (multi-logger {:loggers [{:type :console
                            :interval 500
                            :max-batch 10000}
                           {:type :basic
                            :interval 500
                            :max-batch 10000}]}))

^{:refer std.log.core/log-raw :added "3.0"}
(fact "sends raw data to the logger")

^{:refer std.log.core/basic-write :added "3.0"}
(fact "writes to the logger"

  (h/with-out-str
    (basic-write [{:a 1 :b 2}] false))
  => "[{:a 1, :b 2}]\n")

^{:refer std.log.core/basic-logger :added "3.0"}
(fact "constructs a basic logger"

  (basic-logger)
  => std.log.core.BasicLogger)

^{:refer std.log.core/step :added "3.0"}
(fact "conducts a step that is logged")

(comment
  (./import))
