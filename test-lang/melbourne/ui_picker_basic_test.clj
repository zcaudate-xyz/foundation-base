(ns melbourne.ui-picker-basic-test
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
             [js.react-native :as n]
             [melbourne.ui-picker-basic :as ui-picker-basic]]
   :export [MODULE]})

^{:refer melbourne.ui-picker-basic/PickerBasicControls :added "0.1"}
(fact "Controls for selecting the index")

^{:refer melbourne.ui-picker-basic/PickerBasicIndexed :added "0.1"}
(fact "Creates the picker"
  ^:hidden
  
  (defn.js PickerBasicIndexedDemo
    []
    (var [index setIndex] (r/local 3))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-picker-basic/PickerBasicIndexed"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 20}}
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "light"
                   }
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :style {:fontSize 16
                   :fontWeight "800"}
           :index index
           :setIndex setIndex}]]
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "light"
                   :mode "secondary"}
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :style {:fontSize 16
                   :fontWeight "800"}
           :index index
           :setIndex setIndex}]
         ]

        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "light"
                   }
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :style {:fontSize 16
                   :fontWeight "800"}
           :index index
           :setIndex setIndex}]
         ]]
       
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 20}}
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "dark"
                   }
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :index index
           :setIndex setIndex}]
         ]
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "dark"
                   :mode "secondary"}
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :style {:fontSize 16
                   :fontWeight "800"}
           :index index
           :setIndex setIndex}]
         ]

        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasicIndexed
          {:design {:type "dark"
                   }
           :items ["Victoria"
                   "Queensland"
                   "Tasmania"
                   "New South Wales"
                   "Western Australia"
                   "South Australia"]
           :style {:fontSize 16
                   :fontWeight "800"}
           :index index
           :setIndex setIndex}]
         ]]] 
[:% n/Row
       [:% n/Button
         {:title "+1"
          :onPress (fn:> (setIndex (+ index 1)))}]
        [:% n/Button
         {:title "-1"
          :onPress (fn:> (setIndex (- index 1)))}]
       [:% n/Text
        (n/format-entry #{index})]]))))

^{:refer melbourne.ui-picker-basic/PickerBasic :added "0.1"}
(fact "creates a picker"
  ^:hidden
  
  (defn.js PickerBasicDemo
    []
    (var [value setValue] (r/local "Tasmania"))
    (var [data  setData]  (r/local ["Victoria"
                                    "Queensland"
                                    "Tasmania"
                                    "New South Wales"
                                    "Western Australia"
                                    "South Australia"]))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-picker-basic/PickerBasic"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 20}}
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasic
          {:key data
           :design {:type "light"
                   }
           :data data
           :style {:fontSize 16
                   :fontWeight "800"}
           :value value
           :setValue setValue}]
         [:% n/Text "  "]]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 20}}
        [:% n/Row
         {:style {:margin 5}}
         [:% ui-picker-basic/PickerBasic
          {:key data
           :design {:type "dark"
                   }
           :data data
           :style {:fontSize 16
                   :fontWeight "800"}
           :value value
           :setValue setValue}]
         [:% n/Text "  "]]]] 
[:% n/Row
       [:% n/Button
        {:title "Aus"
         :onPress (fn []
                    (setData ["Victoria"
                              "Queensland"
                              "Tasmania"
                              "New South Wales"
                              "Western Australia"
                              "South Australia"]))}]
       [:% n/Button
        {:title "Chn"
         :onPress (fn []
                    (setData ["Yunnan"
                              "Guangxi"
                              "Hubei"
                              "Sichuan"]))}]
       [:% n/Text
        (n/format-entry #{value})]])))
  
  (def.js MODULE (!:module))
  )
