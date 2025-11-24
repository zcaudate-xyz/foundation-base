(ns std.log.form-test
  (:use code.test)
  (:require [std.log.common :as common]
            [std.log.form :refer :all]
            [std.log.core :as core]
            [std.log.console :as console]
            [std.protocol.log :as protocol.log]))

^{:refer std.log.form/log-meta :added "3.0"}
(fact "gets the metadata information on macroexpand")

^{:refer std.log.form/log-function-name :added "3.0"}
(fact "gets the function name"

  (log-function-name "ns$function_name")
  => "function-name")

^{:refer std.log.form/log-runtime-raw :added "3.0"}
(fact "returns the runtime information of the log entry"

  (set (keys (log-runtime-raw nil "std.log.form-test")))
  => #{:log/function :log/method :log/filename})

^{:refer std.log.form/log-check :added "3.0"}
(fact "checks to see if form should be ignored"

  (log-check :debug)
  => true)

^{:refer std.log.form/to-context :added "3.0"}
(fact "converts a string to a log context"

  (to-context "hello")
  => #:log{:message "hello"})

^{:refer std.log.form/log-fn :added "3.0"}
(fact "actual log function for function application"

  (log-fn (common/default-logger) :info "data" nil {})
  => "data")

^{:refer std.log.form/log-form :added "3.0"}
(fact "function for not including log forms on static compilation" ^:hidden

  (binding [common/*static* true
            common/*level* :info]
    (log-form :debug "ID" () `common/*logger* {} {:data 1} nil {}))
  => nil

  (binding [common/*static* true
            common/*level* :debug]
    (log-form :debug "ID" () `common/*logger* {} {:data 1} nil {}))
  => seq?)

^{:refer std.log.form/with-context :added "3.0"}
(fact "applies a context to the current one"

  (with-context {:a 1}
    common/*context*)
  => (contains {:a 1}))

^{:refer std.log.form/log-context :added "3.0"}
(fact "creates a log-context form"

  (log-context "hello")
  => seq?)

^{:refer std.log.form/log :added "3.0"}
(fact "produces a log" ^:hidden

  (binding [common/*level* :info
            common/*logger* (core/identity-logger {:level :debug})]
    (log "Hello"))
  => (contains  #:log{:namespace "std.log.form-test",
                      :column number?,
                      :function string?,
                      ;;:method "invokeStatic", :filename "form-init14398450122208178066.clj",
                      :level :info, :value "Hello", :timestamp number?}))

^{:refer std.log.form/log-data-form :added "3.0"}
(fact "creates a standard form given a type" ^:hidden

  (log-data-form :status))

^{:refer std.log.form/deflog-data :added "3.0"}
(fact "creates a standard form given a type" ^:hidden

  (deflog-data [:status]))

^{:refer std.log.form/log-error-form :added "3.0"}
(fact "creates forms for  `warn`, `fatal`"

  (log-error-form :warn)
  => seq?)

^{:refer std.log.form/deflog-error :added "3.0"}
(fact "creates a standard form given a type" ^:hidden

  (deflog-error [:help]))

(comment
  (./import)
  (./scaffold)

  (defn hello-entry
    ([]
     (log :debug "Hello World" {:a 1 :b 2})))

  (with-logger (logger {:type :basic
                        :level :debug})
    (hello-entry))

  (with {:level :debug}
        (hello-entry))

  (with-debug
    (spy {:stats/id 1 :stats/event-id 2}
         (+ 1 2 3)))

  (warn "THis is a Warning" {:stats/id "eee"})

  (time (log-function))
  (time (hello-entry))

  (error "Hello" {:tags #{:hello}}))
