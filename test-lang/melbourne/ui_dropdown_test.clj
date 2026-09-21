(ns melbourne.ui-dropdown-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-dropdown :as ui-dropdown]]
   :export [MODULE]})

^{:refer melbourne.ui-dropdown/DropdownIndexedModal :added "0.1"}
(fact "creates the modal")

^{:refer melbourne.ui-dropdown/DropdownIndexed :added "0.1"}
(fact "creates a horizontal check"
  ^:hidden
  
  (defn.js DropdownIndexedDemo
    []
    (var [active setActive] (r/local false))
    (var [index setIndex]   (r/local 2))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-dropdown/DropdownIndexed"} 
[:% n/TextDisplay
        {:content (n/format-entry #{index})}] 
[:% n/Row
        [:% n/Row
         {:style {:backgroundColor "#eee"
                  :flex 1
                  :flexDirection "row-reverese"
                  :padding 30}}
         [:% ui-dropdown/DropdownIndexed
          #{[:design {:type "light"}
             active setActive
             index setIndex
             :items ["A" "B" "C" "D"]]}]]
        [:% n/Row
         {:style {:backgroundColor "#333"
                  :flex 1
                  :flexDirection "row-reverese"
                  :padding 30}}
         [:% ui-dropdown/DropdownIndexed
          #{[:design {:type "dark"}
             active setActive
             index setIndex
             :items ["A" "B" "C" "D"]]}]]])])))

^{:refer melbourne.ui-dropdown/Dropdown :added "0.1"}
(fact "creates a horizontal check"
  ^:hidden
  
  (defn.js DropdownDemo
    []
    (var [active setActive]   (r/local false))
    (var [value setValue]     (r/local "C"))
    (var [display setDisplay] (r/local "screen"))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-dropdown/Dropdown"} 
[:% n/Tabs
        {:data ["screen" "dropdown"]
         :value display
         :setValue setDisplay}] 
[:% n/TextDisplay
        {:content (n/format-entry #{value})}] 
[:% n/Row
        [:% n/Row
         {:style {:backgroundColor "#eee"
                  :flex 1
                  :flexDirection "row-reverese"
                  :padding 30}}
         [:% ui-dropdown/Dropdown
          #{[:design {:type "light"}
             active setActive
             value setValue
             :displayType display
             :data ["A" "B" "C" "D"]]}]]
        [:% n/Row
         {:style {:backgroundColor "#333"
                  :flex 1
                  :flexDirection "row-reverese"
                  :padding 30}}
         [:% ui-dropdown/Dropdown
          #{[:design {:type "dark"}
             active setActive
             value setValue
             :displayType display
             :data ["A" "B" "C" "D"]]}]]])]))
  
  (def.js MODULE (!:module)))
