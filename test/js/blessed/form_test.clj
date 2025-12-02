(ns js.blessed.form-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as  l]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.core.style :as css]
              [js.blessed.form :as f]
              [js.react.ext-form :as ext-form]
              [js.blessed.ui-style :as ui-style]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]
              [xt.lang.event-form :as base-form]]
   :export  [MODULE]})

(def.js MODULE (!:module))

^{:refer js.blessed.form/FormWrapper :added "4.0" :unchecked true}
(fact "addes `width`, `offset` and `label`")

^{:refer js.blessed.form/wrapForm :added "4.0" :unchecked true}
(fact "wraps a component as a Form")

^{:refer js.blessed.form/Spinner :added "4.0" :unchecked true}
(fact "Constructs a Spinner"
  ^:hidden
  
  (defn.js SpinnerDemo
    []
    (var form  (ext-form/makeForm (fn:> {:age 10})
                                  {:age []}))
    (var age (ext-form/listenFieldValue form "age"))
    (return
     [:% ui-core/Enclosed
      {:label "form/Spinner"}
      [:% f/Spinner
       {:top 2
        :max 100
        :min 1
        :step 1
        :height 1
        :pad 3
        :left 0
        :label "Age"
        :form form
        :field "age"
        :offset 12
        :width 15
        :color "green"}]
      [:box {:left 25 :shrink true
             :content (+ "" age)}]])))

^{:refer js.blessed.form/NumberGridBox :added "4.0" :unchecked true}
  (fact "Constructs a NumberGridBox"
    ^:hidden
    
    (defn.js NumberGridBoxDemo
      []
      (var form  (ext-form/makeForm (fn:> {:age 10})
                                  {:age []}))
      (var age (ext-form/listenFieldValue form "age"))
      (return
       [:% ui-core/Enclosed
        {:label "form/NumberGridBox"}
        [:% f/NumberGridBox
         {:top 2
          :start  1
          :end    100
          :colCount 8
          :colWidth 4
          :left 0
          :label "Age"
          :form form
          :field "age"
          :offset 10
          :width 15
          :height 1
          :color "green"
          :textColor "white"}]
        [:box {:left 25 :shrink true
               :content (+ "" age)}]])))

^{:refer js.blessed.form/Dropdown :added "4.0" :unchecked true}
(fact "Constructs a Dropdown"
    ^:hidden
    
    (defn.js DropdownDemo
      []
      (var form  (ext-form/makeForm (fn:> {:currency "STATS"})
                                    {:currency []}))
      (var currency (ext-form/listenFieldValue form "currency"))
      (return
       [:% ui-core/Enclosed
        {:label "form/Dropdown"}
        [:% f/Dropdown
         {:top 2
          :height 1
          :offset 10
          :width 15
          :form  form
          :label "Currency"
          :field "currency"
          :color "yellow"
          :data ["STATS" "XLM" "USD"]}]
        [:box {:left 25 :shrink true
               :content (+ "" currency)}]])))

^{:refer js.blessed.form/EnumBox :added "4.0" :unchecked true}
(fact "Constructs a EnumBox"
  ^:hidden
  
  (defn.js EnumBoxDemo
    []
    (var form  (ext-form/makeForm (fn:> {:currency "STATS"})
                                  {:currency []}))
    (var currency (ext-form/listenFieldValue form "currency"))
    (return
     [:% ui-core/Enclosed
      {:label "form/EnumBox"}
      [:% f/EnumBox
       {:top 2
        :height 1
        :offset 10
        :width 15
        :form  form
        :label "Currency"
        :field "currency"
        :color "yellow"
        :data ["STATS" "XLM" "USD"]}]
      [:box {:left 25 :shrink true
             :content (+ "" currency)}]])))

^{:refer js.blessed.form/EnumMulti :added "4.0" :unchecked true}
(fact "Constructs a EnumMulti"
  ^:hidden
    
  (defn.js EnumMultiDemo
    []
    (var form  (ext-form/makeForm (fn:> {:currency ["USD"]})
                                  {:currency []}))
    (var currencies (ext-form/listenFieldValue form "currency"))
    (return
     [:% ui-core/Enclosed
      {:label "form/EnumMulti"}
      [:% f/EnumMulti
       {:top 2
        :height 1
        :offset 10
        :width 15
        :form  form
        :label "Currency"
        :field "currency"
        :color "yellow"
        :format (fn:> [s] (+ " " s " "))
        :data ["STATS" "XLM" "USD"]}]
      [:box {:top 1 :shrink true
             :content (+ "" currencies)}]])))

^{:refer js.blessed.form/Tabs :added "4.0" :unchecked true}
(fact "Constructs a Tabs"
    ^:hidden
    
    (defn.js TabsDemo
      []
      (var form  (ext-form/makeForm
                  (fn:> {:currency "STATS"})
                  {:currency []}))
      (var currency (ext-form/listenFieldValue form "currency"))
      (return
       [:% ui-core/Enclosed
        {:label "form/Tabs"}
        [:% f/Tabs
         {:top 2
          :height 1
          :offset 10
          :width 19
          :form  form
          :label "Currency"
          :field "currency"
          :color "yellow"
          :data ["STATS" "XLM" "USD"]
          :format (fn:> [s] (+ " " s " "))}]
        [:box {:left 25 :shrink true
               :content (+ "" currency)}]])))

^{:refer js.blessed.form/TextBox :added "4.0" :unchecked true}
(fact "Constructs a TextBox"
  ^:hidden
  
  (defn.js TextBoxDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:first-name "John"
                      :last-name "Smith"})
               {:first-name []
                :last-name  []}))
    (var data (ext-form/listenFormData form))
    (return
     [:% ui-core/Enclosed
      {:label "form/TextBox"}
      [:% f/TextBox
       {:top 2
        :left 0
        :label "First"
        :form form
        :field "first_name"
        :offset 10
        :width 15
        :height 1
        :color "green"
        :textColor "white"}]
      [:% f/TextBox
       {:top 3
        :left 0
        :label "Last"
        :form  form
        :field "last_name"
        :offset 10
        :width 15
        :height 1
        :color "green"
        :textColor "white"}]
      [:box
       {:left 25
        :width 20
        :height 1
        :content (+ ""  (. data ["first_name"])
                    " " (. data ["last_name"]))}]])))

^{:refer js.blessed.form/ToggleSwitch :added "4.0" :unchecked true}
(fact "Constructs a ToggleSwitch"
  ^:hidden

  (defn.js ToggleSwitch
    []
    (var form (ext-form/makeForm
               (fn:> {:agree  false})
               {:agree []}))
    (var agree (ext-form/listenFieldValue form "agree"))
    (return
     [:% ui-core/Enclosed
      {:label "form/ToggleSwitch"}
      [:% f/ToggleSwitch
       {:top 2
        :left 0
        :label "Agree"
        :form form
        :field "agree"
        :offset 10
        :width 15
        :height 1
        :color "green"
        :textColor "white"}]
      [:box
       {:left 25
        :height 1
        :content (+ "" agree)}]])))

^{:refer js.blessed.form/ToggleButton :added "4.0" :unchecked true}
(fact "Constructs a ToggleButton"
  ^:hidden

  (defn.js ToggleButton
    []
    (var form (ext-form/makeForm
               (fn:> {:agree  false})
               {:agree []}))
    (var agree (ext-form/listenFieldValue form "agree"))
    (return
     [:% ui-core/Enclosed
      {:label "form/ToggleButton"}
      [:% f/ToggleButton
       {:top 2
        :left 0
        :content "YES I AGREE"
        :label "Agree"
        :form form
        :field "agree"
        :offset 10
        :width 15
        :height 1
        :color "green"
        :textColor "white"}]
      [:box
       {:left 25
        :height 1
        :content (+ "" agree)}]])))

^{:refer js.blessed.form/TimePicker :added "4.0" :unchecked true}
(fact "Constructs a TimePicker"
  ^:hidden
  
  (defn.js TimePickerDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:time-hour   0
                      :time-minute 15})
               {:time-hour   []
                :time-minute []}))
    (var data (ext-form/listenFormData form))
    (return
     [:% ui-core/Enclosed
      {:label "form/TimePicker"}
      [:% f/TimePicker
       {:top 2
        :left 0
        :label "Time"
        :form form
        :minuteQuarters true
        :hourField "time_hour"
        :minuteField "time_minute"
        :offset 10
        :height 1
        :width 30
        :color "green"
        :textColor "white"}]
      [:box {:left 20 :shrink true
             :content (+ (. data ["time_hour"])
                         ":"
                         (. data ["time_minute"]))}]])))

^{:refer js.blessed.form/DatePicker :added "4.0" :unchecked true}
(fact "Constructs a DatePicker"
  ^:hidden
  
  (defn.js DatePickerDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:date-day   1
                      :date-month 5
                      :date-year 1997})
               {:date-day   []
                :date-month []
                :date-year  []}))
    (var data (ext-form/listenFormData form))
    (return
     [:% ui-core/Enclosed
      {:label "form/DatePicker"}
      [:% f/DatePicker
       {:top 2
        :left 0
        :label "Date"
        :form form
        :monthQuarters true
        :dayField "date_day"
        :monthField "date_month"
        :yearField "date_year"
        :offset 10
        :height 1
        :width 30
        :color "green"
        :textColor "white"}]
      [:box {:left 20 :shrink true
             :content (+ (. data ["date_day"])
                         "/"
                         (. data ["date_month"])
                         "/"
                         (. data ["date_year"]))}]])))

^{:refer js.blessed.form/DurationPicker :added "4.0" :unchecked true}
(fact "Constructs a DurationPicker"
  ^:hidden
  
  (defn.js DurationPickerDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:from-now-day    2   
                      :from-now-hour   2
                      :from-now-minute 5})
               {:from-now-day    []   
                :from-now-hour   []
                :from-now-minute []}))
    (var data (ext-form/listenFormData form))
    (return
     [:% ui-core/Enclosed
      {:label "form/DurationPicker"}
      [:% f/DurationPicker
       {:top 2
        :left 0
        :label "Duration"
        :form form
        :minuteQuarters true
        :dayField "from_now_day"
        :hourField "from_now_hour"
        :minuteField "from_now_minute"
        :offset 10
        :height 1
        :width 30
        :color "green"
        :textColor "white"}]
      [:box {:left 20 :shrink true
             :content (+ (. data ["from_now_day"])
                         " - "
                         (. data ["from_now_hour"])
                         " - "
                         (. data ["from_now_minute"]))}]])))
