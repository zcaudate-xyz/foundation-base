(ns std.log.console-test
  (:use code.test)
  (:require [std.log.console :as console :refer :all]
            [std.lib :as h]
            [std.log.element :as element]
            [std.print.ansi :as ansi]
            [std.log.common :as common]))

^{:refer std.log.console/style-default :added "3.0"}
(fact "gets default style"

  (style-default {})
  => (contains {:background :none :text :white :bold false :highlight false}))

^{:refer std.log.console/join-with :added "3.0"}
(fact "helper function for string concatenation"

  (join-with ["a" "b"])
  => "a b"

  (join-with "-" ["a" "b"])
  => "a-b")

^{:refer std.log.console/console-pprint :added "3.0"}
(fact "prints the item in question"

  (console-pprint {:a 1})
  => "{:a 1}")

^{:refer std.log.console/console-format-line :added "3.0"}
(fact "format the output line"

  (console-format-line "hello")
  => "| hello")

^{:refer std.log.console/console-display? :added "3.0"}
(fact "check if item is displayed"

  (console-display? {:display {:default true}} [:display] nil)
  => true)

^{:refer std.log.console/console-render :added "3.0"}
(fact "renders the entry based on :console/style"

  (console-render {:console/style {:header {:label {:display {:default true}}}}}
                  (fn [item style] "RENDERED")
                  [:header :label])
  => "RENDERED")

^{:refer std.log.console/console-header-label :added "3.0"}
(fact "constructs the header label"

  (console-header-label {:log/label "TEST"}
                        {:min-width 10 :background :white :text :white})
  => string?)

^{:refer std.log.console/console-header-position :added "3.0"}
(fact "constructs the header position"

  (console-header-position {:log/namespace "ns" :log/line 1 :log/column 1} {})
  => string?)

^{:refer std.log.console/console-header-date :added "3.0"}
(fact "constructs the header date"

  (console-header-date {:log/timestamp 1600000000000} {})
  => string?)

^{:refer std.log.console/console-header-message :added "3.0"}
(fact "constructs the header message"

  (console-header-message {:log/template "the value is {{log/value}}"
                           :log/value "HELLO"}
                          {:text :blue :background :white})
  => string?)

^{:refer std.log.console/console-header :added "3.0"}
(fact "constructs the log header"

  (console-header {:log/label "TEST" :log/timestamp 1600000000000})
  => string?)

^{:refer std.log.console/console-meter-trace :added "3.0"}
(fact "constructs the meter trace"

  (console-meter-trace {:trace/id "1234567890123"
                        :trace/root "1234567890123"
                        :trace/display true}
                       {})
  => string?)

^{:refer std.log.console/console-meter-form :added "3.0"}
(fact "constructs the meter form"

  (console-meter-form {:meter/form '(+ 1 2)} {})
  => string?)

^{:refer std.log.console/console-meter :added "3.0"}
(fact "constructs the log meter"

  (binding [common/*trace* true]
    (console-meter {:meter/display true
                    :trace/id "1234567890123"
                    :trace/root "1234567890123"
                    :meter/form '(+ 1 2)}))
  => string?)

^{:refer std.log.console/console-status-outcome :added "3.0"}
(fact "constructs the status outcome"

  (console-status-outcome {:log/label "TEST" :meter/outcome :success :meter/reflect true} {})
  => string?)

^{:refer std.log.console/console-status-duration :added "3.0"}
(fact "constructs the status duration"

  (console-status-duration {:meter/duration 1000} {})
  => string?)

^{:refer std.log.console/console-status-start :added "3.0"}
(fact "constructs the status start time"

  (console-status-start {:meter/start 1600000000000} {})
  => string?)

^{:refer std.log.console/console-status-end :added "3.0"}
(fact "constructs the status end time"

  (console-status-end {:meter/end 1600000000000} {})
  => string?)

^{:refer std.log.console/console-status-props :added "3.0"}
(fact "constructs the status props"

  (console-status-props {:meter/props {:a 1}} {})
  => string?)

^{:refer std.log.console/console-status :added "3.0"}
(fact "constructs the log status"

  (console-status {:log/label "TEST"
                   :meter/outcome :success
                   :meter/duration 1000
                   :meter/start 1600000000000
                   :meter/end 1600000000000})
  => string?)

^{:refer std.log.console/console-body-console-text :added "3.0"}
(fact "constructs the return text from the console"

  (console-body-console-text "hello\nworld")
  => string?)

^{:refer std.log.console/console-body-console :added "3.0"}
(fact "constructs the body console"

  (console-body-console {:log/console "hello"} {})
  => string?)

^{:refer std.log.console/console-body-data-context :added "3.0"}
(fact "remove system contexts from the log entry"

  (console-body-data-context {:log/a 1 :user/b 2})
  => {:user/b 2})

^{:refer std.log.console/console-body-data :added "3.0"}
(fact "constructs the body data"

  (console-body-data {:log/value 1} {})
  => string?)

^{:refer std.log.console/console-body-exception :added "3.0"}
(fact "constructn the body exception"

  (console-body-exception {:log/exception (Exception. "error")} {})
  => string?)

^{:refer std.log.console/console-body :added "3.0"}
(fact "constructs the log body"

  (console-body {:log/value 1 :log/exception (Exception. "error")})
  => string?)

^{:refer std.log.console/console-format :added "3.0"}
(fact "formats the entire log"

  (console-format {:log/type :debug :log/label "TEST" :log/timestamp 1600000000000})
  => string?)

^{:refer std.log.console/logger-process-console :added "3.0"}
(fact "processes the label for logger"

  (logger-process-console nil [{:log/value "hello" :log/display true :log/timestamp 1600000000000}])
  => nil)

^{:refer std.log.console/console-write :added "3.0"}
(fact "write function for the console logger"

  (h/with-out-str
    (console-write {:log/value "HELLO" :log/raw "RAW"}))
  => "RAW")

^{:refer std.log.console/console-logger :added "3.0"}
(fact "creates a console logger"

  (console-logger)
  => map?)

(comment
  (./import))

(comment
  (std.log/with-logger-basic (std.log/info "aoeuaoeua"
                                           "HOUOeu"))

  (std.log/with-debug-logger
    (std.log/error {:log/label "HELLO"} "HOUOeu"))

  (std.log/with-logger-basic (std.log.profile/task "e"))

  (std.log/with-logger-basic (std.log/meter {:log/label "HELLO"} "HOUOeu"))

  nil

  (std.log/meter (do (Thread/sleep 11000)
                     (+ 1 2 3)))
  (std.log/meter (do (Thread/sleep 11000)
                     (+ 1 2 3)))
  (std.log/with-logger-verbose
    (std.log/meter {:log/label "DOCKER UP"}
                   "oeuoe"))
  "oeuoe"
  "oeuoe"
  (ansi)
  (std.log/log (ansi/normal (ansi/bold (ansi/blue "HELLO")))))
