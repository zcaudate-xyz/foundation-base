(ns std.log.core-test
  (:use code.test)
  (:require [std.log.core :refer :all]
            [std.log.common :as common]
            [std.log.console :as console]
            [std.lib :as h]
            [std.print :as print]
            [std.protocol.component :as protocol.component]))

^{:refer std.log.core/logger-submit :added "3.0"}
(fact "basic submit function"

  (let [l (basic-logger)
        _ (logger-start l)
        res (logger-submit l {:a 1})]
    (logger-stop l)
    res)
  => :logger/enqueued)

^{:refer std.log.core/logger-process :added "3.0"}
(fact "a special :fn key for processing the input"

  (logger-process {:log/value 1 :fn/process (fn [v] {:processed v})})
  => (contains {:log/value 1 :processed 1}))

^{:refer std.log.core/logger-enqueue :added "3.0"}
(fact "submits a task to the logger job queue"

  (let [l (basic-logger)
        _ (logger-start l)
        res (logger-enqueue l {:a 1 :log/level :info})]
    (logger-stop l)
    res)
  => :logger/enqueued)

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
(fact "starts the logger, initating a queue and executor"

  (let [l (basic-logger)]
    (logger-start l)
    @(:instance l))
  => (contains {:executor map?}))

^{:refer std.log.core/logger-info :added "3.0"}
(fact "returns information about the logger"

  (logger-info (common/default-logger))
  => (contains-in {:type :console}))
^{:refer std.log.core/logger-stop :added "3.0"}
(fact "stops the logger, queue and executor"

  (let [l (basic-logger)]
    (logger-start l)
    (logger-stop l)
    @(:instance l))
  => (fn [m] (nil? (:executor m))))

^{:refer std.log.core/logger-init :added "3.0"}
(fact "sets defaults for the logger"

  (logger-init {:type :basic})
  => (contains {:type :basic,
                :instance (stores {:level :debug})}))

^{:refer std.log.core/identity-logger :added "3.0"}
(fact "creates a identity logger"

  (identity-logger)
  => (contains {:type :identity}))

^{:refer std.log.core/multi-logger :added "3.0"}
(fact "creates multiple loggers"

  (multi-logger {:loggers [{:type :console
                            :interval 500
                            :max-batch 10000}
                           {:type :basic
                            :interval 500
                            :max-batch 10000}]})
  => (contains {:type :multi}))

^{:refer std.log.core/log-raw :added "3.0"}
(fact "sends raw data to the logger"

  (let [l (common/default-logger)
        _ (logger-start l)
        res (log-raw l {:a 1})]
    (logger-stop l)
    res)
  => :logger/enqueued)

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
(fact "conducts a step that is logged"

  (let [l (common/default-logger)]
    (logger-start l)
    (binding [common/*logger* l]
      (step "Doing something"
            (+ 1 2))))
  => any?)

(comment
  (./import))
