(ns std.log.common-test
  (:use code.test)
  (:require [std.log.common :as common]
            [std.protocol.log :as protocol.log]
            [std.log :as log]))

^{:refer std.log.common/set-static! :added "3.0"}
(fact "sets the global static variable"

  (let [old common/*static*]
    (try
      (common/set-static! true)
      => true

      common/*static*
      => true
      (finally
        (common/set-static! old)))))

^{:refer std.log.common/set-level! :added "3.0"}
(fact "sets the global level variable"

  (let [old common/*level*]
    (try
      (common/set-level! :info)
      => :info

      common/*level*
      => :info
      (finally
        (common/set-level! old)))))

^{:refer std.log.common/set-context! :added "3.0"}
(fact "sets the global context"

  (let [old common/*context*]
    (try
      (common/set-context! {:a 1})
      => {:a 1}

      common/*context*
      => {:a 1}
      (finally
        (common/set-context! old)))))

^{:refer std.log.common/set-logger! :added "3.0"}
(fact "sets the global logger"

  (let [old common/*logger*
        l (log/create {:type :basic})]
    (try
      (common/set-logger! l)

      (satisfies? protocol.log/ILogger common/*logger*)
      => true
      (finally
        (alter-var-root #'common/*logger* (constantly old))))))

^{:refer std.log.common/put-logger! :added "3.0"}
(fact "updates the global logger"

  (let [old common/*logger*]
    (try
      ;; Ensure a logger exists
      (common/default-logger)

      (common/put-logger! {:level :warn})
      => (partial satisfies? protocol.log/ILogger)

      (:level @(:instance common/*logger*))
      => :warn
      (finally
         (alter-var-root #'common/*logger* (constantly old))))))

^{:refer std.log.common/default-logger :added "3.0"}
(fact "returns the default logger"

  (common/default-logger)
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log.common/basic-logger :added "3.0"}
(fact "returns the basic logger"

  (common/basic-logger)
  => (partial satisfies? protocol.log/ILogger))

^{:refer std.log.common/verbose-logger :added "3.0"}
(fact "returns the verbose logger"

  (common/verbose-logger)
  => (partial satisfies? protocol.log/ILogger))
