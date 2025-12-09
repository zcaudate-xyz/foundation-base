(ns js.blessed.ui-date-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :basic
   :config   {:emit {:lang/jsx false}}
   :require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.ui-core :as ui-core]
              [js.blessed.ui-date :as ui-date]
              [js.blessed :as b :include [:fn]]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.blessed.ui-date/TimePicker :added "4.0" :unchecked true}
(fact "Constructs a TimePicker"
  ^:hidden
  
  (defn.js TimePickerDemo
    []
    (var [minute setMinute] (r/local 30))
    (var [hour setHour] (r/local 10))
    (return
     [:% ui-core/Enclosed
      {:label "ui-date/TimePicker"}
      [:% ui-date/TimePicker
       {:top 2
        :minuteQuarters true
        :minuteHidden false
        :minuteLabel "m"
        :minute minute
        :setMinute setMinute 
        :hourLabel "h"
        :hour hour
        :setHour setHour
        :color "green"
        :width 20}]
      [:box {:left 25 :shrink true
             :content (+ hour ":" minute)}]])))

^{:refer js.blessed.ui-date/DatePicker :added "4.0" :unchecked true}
(fact "Constructs a DatePicker"
  ^:hidden
  
  (defn.js DatePickerDemo
    []
    (var [day setDay]     (r/local 10))
    (var [month setMonth] (r/local 12))
    (var [year setYear]   (r/local 1997))
    (return
     [:% ui-core/Enclosed
      {:label "ui-date/DatePicker"}
      [:% ui-date/DatePicker
       {:top 2
        :width 25
        :day day
        :setDay setDay 
        :month month
        :setMonth setMonth 
        :year year
        :setYear setYear 
        :color "green"}]
      [:box {:left 20 :shrink true
             :content (+ day "/" month "/" year)}]]))

  )

^{:refer js.blessed.ui-date/DurationPicker :added "4.0" :unchecked true}
(fact "Constructs a DurationPicker"
  ^:hidden
  
  (defn.js DurationPickerDemo
    []
    (var [day setDay] (r/local 30))
    (var [hour setHour] (r/local 10))
    (return
     [:% ui-core/Enclosed
      {:label "ui-date/DurationPicker"}
      [:% ui-date/DurationPicker
       {:top 2
        :height 1
        :width 20
        :minuteHidden true
        :dayLabel "d"
        :day day
        :setDay setDay 
        :hourLabel "h"
        :hour hour
        :setHour setHour
        :color "green"}]
      [:box {:left 25 :shrink true
             :content (+ day "-" hour)}]])))