(ns melbourne.slim-select-test
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
   :require [[js.react-native :as n]
             [js.react.ext-form :as ext-form]
             [melbourne.slim-select :as slim-select]]
   :export [MODULE]})

^{:refer melbourne.slim-select/FormPicker :added "0.1"}
(fact "creates a Picker"
  ^:hidden
  
  (defn.js FormPickerDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:price "Tasmania"})
               {:price []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-select/FormPicker"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "light"
                    }
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "light"
                    :mode "secondary"}
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "light"
                    }
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "dark"
                    }
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "dark"
                    :mode "secondary"}
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]
        [:% slim-select/FormPicker
         #{form
           {:brand {:type "dark"
                    }
            :label "Price"
            :field "price"
            :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]]]))))

^{:refer melbourne.slim-select/FormDropdown :added "0.1"}
(fact "creates a Dropdown"
  ^:hidden
  
  (defn.js FormDropdownDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:price "Victoria"})
               {:price []}))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.slim-select/FormDropdown"} 
[:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :flex 1
                  :padding 10}}
         [:% slim-select/FormDropdown
          #{form
            {:brand {:type "light"
                     :mode "secondary"}
             :label "Price"
             :field "price"
             :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]]
        [:% n/View
         {:style {:backgroundColor "#333"
                  :flex 1
                  :padding 10}}
         [:% slim-select/FormDropdown
          #{form
            {:brand {:type "dark"
                     :mode "secondary"}
             :label "Price"
             :field "price"
             :data ["Victoria"
                    "Queensland"
                    "Tasmania"
                    "New South Wales"
                    "Western Australia"
                    "South Australia"]}}]]])]))
  
  (def.js MODULE (!:module))
  )
