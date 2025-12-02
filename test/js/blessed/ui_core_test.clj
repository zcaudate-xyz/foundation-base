(ns js.blessed.ui-core-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.ui-core :as ui-core]
              [js.blessed :as b :include [:fn]]
              [js.lib.chalk :as chk]
              [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(defn.js boolText
  "gets the text for true or false"
  {:added "4.0" :unchecked true}
  [b]
  (return (:? b
              [(chk/green "true")
               (chk/red "false")])))

^{:refer js.blessed.ui-core/Enclosed :added "4.0" :unchecked true}
(fact "constructs a box with label")

^{:refer js.blessed.ui-core/SmallLabel :added "4.0" :unchecked true}
(fact "Constructs a Small Label"
  ^:hidden
  
  (defn.js SmallLabelDemo
    []
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/SmallLabel"}
      [:% ui-core/SmallLabel
       {:top 2
        :left 0
        :content "Account Details"}]])))

^{:refer js.blessed.ui-core/MinimalButton :added "4.0" :unchecked true}
(fact "Constructs a Minimal Button"
  ^:hidden
  
  (defn.js MinimalButtonDemo
    []
    (var [val setVal] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/MinimalButton"}
      [:% ui-core/MinimalButton
       {:top 2
        :left 0
        :color "blue"
        :content " +1 "
        :onClick (fn []
                   (setVal (+ val 1)))}]
      [:% ui-core/MinimalButton
       {:top 2
        :left 8
        :color "red"
        :content " -1 "
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:% ui-core/MinimalButton
       {:top 2
        :left 16
        :disabled true
        :color "red"
        :content " NA "
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:box {:left 25 :shrink true
             :content (+ "" val)}]])))

^{:refer js.blessed.ui-core/SmallButton :added "4.0" :unchecked true}
(fact "Constructs a Small Button"
  ^:hidden
  
  (defn.js SmallButtonDemo
    []
    (var [val setVal] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/SmallButton"}
      [:% ui-core/SmallButton
       {:top 2
        :left 0
        :color "blue"
        :content "+1"
        :onClick (fn []
                   (setVal (+ val 1)))}]
      [:% ui-core/SmallButton
       {:top 2
        :left 8
        :color "red"
        :content "-1"
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:% ui-core/SmallButton
       {:top 2
        :left 16
        :disabled true
        :color "red"
        :content "NA"
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:box {:left 25 :shrink true
             :content (+ "" val)}]])))

^{:refer js.blessed.ui-core/ToggleButton :added "4.0" :unchecked true}
(fact "Constructs a toggle button"
  ^:hidden
  
  (defn.js ToggleButtonDemo
    []
    (var [val setVal] (r/local true))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/ToggleButton"}
      [:% ui-core/ToggleButton
       {:top 2
        :left 0
        :selected val
        :setSelected setVal
        :color "blue"
        :content "C"
        :onClick (fn:> (setVal (not val)))}]
      [:% ui-core/ToggleButton
       {:top 2
        :left 8
        :selected (not val)
        :setSelected (fn:> [val] (setVal (not val)))
        :color "red"
        :content "S"}]
      [:% ui-core/ToggleButton
       {:top 2
        :left 16
        :disabled true
        :color "red"
        :content "NA"
        :selected val
        :setSelected setVal}]
      [:box {:left 22 :shrink true
             :content (+ "" val)}]])))

^{:refer js.blessed.ui-core/BigButton :added "4.0" :unchecked true}
(fact "Constructs a Big Button"
  ^:hidden
  
  (defn.js BigButtonDemo
    []
    (var [val setVal] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/BigButton"}
      [:% ui-core/BigButton
       {:top 2
        :left 0
        :color "blue"
        :content "+1"
        :onClick (fn []
                   (setVal (+ val 1)))}]
      [:% ui-core/BigButton
       {:top 2
        :left 8
        :color "red"
        :content "-1"
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:% ui-core/BigButton
       {:top 2
        :left 16
        :disabled true
        :color "red"
        :content "NA"
        :onClick (fn []
                   (setVal (- val 1)))}]
      [:box {:left 25 :shrink true
             :content (+ "" val)}]])))

^{:refer js.blessed.ui-core/BigCheckBox :added "4.0" :unchecked true}
(fact "Constructs a Big Checkbox"
  ^:hidden

  (defn.js BigCheckBoxDemo
    []
    (var [selected setSelected] (r/local true))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/BigCheckbox"}
      [:% ui-core/BigCheckBox
       {:top 2
        :left 0
        :selected selected
        :setSelected setSelected
        :color "blue"}]
      [:box {:top 3
             :left 8
             :content (-/boolText selected)}]])))

^{:refer js.blessed.ui-core/ToggleSwitch :added "4.0" :unchecked true}
(fact "Constructs a Toggle"
  ^:hidden

  (defn.js ToggleSwitchDemo
    []
    (var [selected setSelected] (r/local true))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/ToggleSwitch"}
      [:% ui-core/ToggleSwitch
       {:top 2
        :left 0
        :selected selected
        :setSelected setSelected
        :color "blue"}]
      [:box {:left 22
             :content (-/boolText selected)}]])))

^{:refer js.blessed.ui-core/Spinner :added "4.0" :unchecked true}
(fact "Constructs a Spinner"
  ^:hidden

  (defn.js SpinnerDemo
    []
    (var [value setValue] (r/local 0.5))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/Spinner"}
      [:% ui-core/Spinner
       {:top 2
        :max 1
        :min 0
        :step 0.1
        :pad 5
        :decimal 1
        :left 0
        :width 16
        :value value
        :setValue setValue
        :color "green"}]
      [:box {:left 25 :shrink true
             :content (+ "" (j/toFixed value 1))}]])))

^{:refer js.blessed.ui-core/EnumBoxIndexed :added "4.0" :unchecked true}
(fact "Constructs a EnumBoxIndexed"
  ^:hidden
  
  (defn.js EnumBoxDemo
    []
    (var [index setIndex] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/EnumBoxIndexed"}
      [:% ui-core/EnumBoxIndexed
       {:top 2
        :items ["one" "two" "three"]
        :index index
        :setIndex setIndex
        :width 20
        :color "green"}]
      [:box {:left 25 :shrink true
             :content (+ "" index)}]])))

^{:refer js.blessed.ui-core/EnumBox :added "4.0" :unchecked true}
(fact "Constructs a EnumBox"
  ^:hidden

  (defn.js EnumBoxDemo
    []
    (var [value setValue] (r/local "two"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/EnumBox"}
      [:% ui-core/EnumBox
       {:top 2
        :data ["one" "two" "three"]
        :value value
        :setValue setValue
        :width 20
        :color "green"}]
      [:box {:left 25 :shrink true
             :content (+ "" value)}]])))

^{:refer js.blessed.ui-core/displayDropdown :added "4.0" :unchecked true}
(fact "helper function for dropdown")

^{:refer js.blessed.ui-core/DropdownIndexed :added "4.0" :unchecked true}
(fact "Constructs a Dropdown"
  ^:hidden

  (defn.js DropdownIndexedDemo
    []
    (var [index setIndex] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/DropdownIndexed"}
      [:% ui-core/DropdownIndexed
       {:top 2
        :width 20
        :index index
        :setIndex setIndex
        :color "yellow"
        :items ["STATS" "XLM" "USD"]}]
      [:box {:left 25 :shrink true
             :content (+ "" index)}]])))

^{:refer js.blessed.ui-core/Dropdown :added "4.0" :unchecked true}
(fact "Constructs a Dropdown"
  ^:hidden

  (defn.js DropdownDemo
    []
    (var [value setValue] (r/local "XLM"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/Dropdown"}
      [:% ui-core/Dropdown
       {:top 2
        :width 20
        :value value
        :setValue setValue
        :color "yellow"
        :data ["STATS" "XLM" "USD"]}]
      [:box {:left 25 :shrink true
             :content (+ "" value)}]])))

^{:refer js.blessed.ui-core/TextBox :added "4.0" :unchecked true}
(fact "Constructs a Dropdown"
  ^:hidden

  (defn.js TextBoxDemo
    []
    (var [content setContent] (r/local "Hello World"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/TextBox"
       :width 40}
      [:% ui-core/TextBox
       {:top 2
        :width 20
        :content content
        :setContent setContent
        :color "yellow"}]
      [:box {:left 22
             :shrink true
             :content (+ "" content)}]])))

^{:refer js.blessed.ui-core/TextDisplay :added "4.0" :unchecked true}
(fact "Displays text as content"
  ^:hidden
  
  (defn.js TextDisplayDemo
    []
    (var content (r/const
                  (k/join "\n"
                          (k/arr-repeat
                           (k/join " "
                                   (k/arr-repeat "ABC"
                                                 10))
                           10))))
    (return
     [:% ui-core/Enclosed
      {:label "ui-core/TextDisplay"}
      [:% ui-core/TextDisplay
       {:top 2
        :height 3
        :width 30
        :content content}]])))

^{:refer js.blessed.ui-core/displayNumberGrid :added "4.0" :unchecked true}
(fact "helper function for NumberGridBox")

^{:refer js.blessed.ui-core/NumberGridBox :added "4.0" :unchecked true}
(fact "Constructs a NumberGridBox"
  ^:hidden
  
  (defn.js NumberGridBoxDemo
    []
    (let [[value setValue] (r/local 20)]
      (return
       [:box {:width 30
              :content "ui-core/NumberGridBox"
              :height 8}
        [:% ui-core/NumberGridBox
         {:top 2
          :value value
          :setValue setValue
          :width 10
          :textColor "white"
          :color "green"
          :start  10
          :end 80
          :colCount 10
          :colWidth 2}]
        [:box {:left 25 :shrink true
               :content (+ "" value)}]])))

  (def.js MODULE (!:module)))
