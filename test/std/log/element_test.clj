(ns std.log.element-test
  (:require [std.lib.time :as time]
            [std.log.element :refer :all])
  (:use code.test))

^{:refer std.log.element/style-ansi :added "3.0"}
(fact "constructs the style used in console"

  (style-ansi {:text :blue :background :white})
  => [:normal :on-white :blue])

^{:refer std.log.element/style-heading :added "3.0"}
(fact "returns the text for a style"

  (style-heading "Hello" {:highlight true
                          :text :blue
                          :background :white})
  => ["[22;47;34m Hello [0m" 7])

^{:refer std.log.element/elem-daily-instant :added "3.0"}
(fact "returns a styled label and instant "

  (elem-daily-instant "TIME:" (time/time-ms) {})
  => string?)

^{:refer std.log.element/elem-ns-duration :added "3.0"}
(fact "returns a styled label and duration"

  (elem-ns-duration "DURATION:" 100000 {})
  => string?)

^{:refer std.log.element/elem-position :added "3.0"}
(fact "creates a style position element"

  (elem-position {:tag :hello
                  :namespace 'user
                  :function 'add
                  :column 10
                  :line 11}
                 {:text :white})
  => string?)
