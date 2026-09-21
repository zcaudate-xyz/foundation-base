(ns melbourne.slim-common-test
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
             [js.react.ext-form :as ext-form]
             [xt.event.base-form :as event-form]
             [melbourne.slim-common :as slim-common]
             [melbourne.base-validators :as validators]]
   :export [MODULE]})

^{:refer melbourne.slim-common/FormEnclosed :added "0.1"}
(fact "creates the form"
  ^:hidden

  (defn.js FormEnclosedDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormEnclosed"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 8}}
        [:% slim-common/FormEnclosed
         {:designNeutral "#333"
          :label "HELLO"
          :styleLabel {}
          :minWidth 150}
         [:% n/View
          {:style {:paddingTop 10}}
          [:% n/Text
           "WORLD"]]]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormEnclosed
         {:designNeutral "#eee"
          :label "HELLO"
          :styleLabel {}
          :minWidth 150}
         [:% n/View
          {:style {:paddingTop 10}}
          [:% n/Text
           "WORLD"]]]]]))))

^{:refer melbourne.slim-common/FormReadOnly :added "4.0"}
(fact "creats a read only form"
  ^:hidden
  
  (defn.js FormReadOnlyDemo
    []
    (var entry {:name "abc"})
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormReadOnly"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormReadOnly
         {:design {:type "light"}
          :label "Name"
          :entry entry
          :fieldProps {:outlined true}
          :template ["name"]}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormReadOnly
         {:design {:type "dark"}
          :label "Name"
          :entry entry
          :fieldProps {:outlined true}
          :template ["name"]}]]]))))

^{:refer melbourne.slim-common/FormInput :added "0.1"}
(fact "creates an Input"
  ^:hidden
  
  (defn.js FormInputDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "abc"})
               {:name [(validators/is-not-empty)]}))
    (r/init []
      (event-form/validate-all form))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormInput"} 
[:% n/Row
       [:% n/Button
        {:title "V"
         :onPress (fn:> (event-form/validate-field form "name"))}]
       [:% n/Text " "]
       [:% n/Button
        {:title "P"
         :onPress (fn:> (console.log form))}]] 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormInput
         {:design {:type "light"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormInput
         {:design {:type "dark"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"
          :hideValidation true}]]]))))

^{:refer melbourne.slim-common/FormInputXL :added "0.1"}
(fact "creates a Form XL"
  ^:hidden
  
  (defn.js FormInputXLDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "abc"})
               {:name [(validators/is-not-empty)]}))
    (r/init []
      (event-form/validate-all form))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormInputXL"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormInputXL
         {:design {:type "light"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormInputXL
         {:design {:type "dark"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"
          :hideValidation true}]]]))))

^{:refer melbourne.slim-common/FormTextArea :added "0.1"}
(fact "creates an TextArea"
  ^:hidden
  
  (defn.js FormTextAreaDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "abc"})
               {:name [(validators/is-not-empty)]}))
    (r/init []
      (event-form/validate-all form))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormTextArea"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormTextArea
         {:design {:type "light"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormTextArea
         {:design {:type "dark"}
          :label "Name"
          :form form
          :fieldProps {:outlined true}
          :field "name"}]]]))))

^{:refer melbourne.slim-common/FormCheckBox :added "0.1"}
(fact "creates a check box"
  ^:hidden
  
  (defn.js FormCheckBoxDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:agree false})
               {:agree []}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-common/FormCheckBox"} 
      [:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormCheckBox
         {:design {:type "light"}
          :label "I AGREE TO TERMS AND CONDITIONS"
          :form form
          :field "agree"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormCheckBox
         {:design {:type "dark"}
          :label "I AGREE TO TERMS AND CONDITIONS"
          :form form
          :field "agree"}]]] 
      [:% n/Text
       (n/format-obj form.data)]))))

^{:refer melbourne.slim-common/FormToggleButton :added "0.1"}
(fact "creates a Form Toggle Button"
  ^:hidden
  
  (defn.js FormToggleButtonDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:agree false})
               {:agree []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormToggleButton"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormToggleButton
         {:design {:type "light"}
          :label "Agree"
          :form form
          :field "agree"
          :text "I AGREE TO TERMS AND CONDITIONS"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormToggleButton
         {:design {:type "dark"}
          :label "Agree"
          :form form
          :field "agree"
          :text "I AGREE TO TERMS AND CONDITIONS"}]]]))))

^{:refer melbourne.slim-common/FormToggleSwitch :added "4.0"}
(fact "creates a Form Toggle Switch"
  ^:hidden
  
  (defn.js FormToggleSwitchDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:agree false})
               {:agree []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormToggleSwitch"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormToggleSwitch
         {:design {:type "light"}
          :label "Agree"
          :form form
          :field "agree"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormToggleSwitch
         {:design {:type "dark"}
          :label "Agree"
          :form form
          :field "agree"}]]]))))

^{:refer melbourne.slim-common/FormEnumSingle :added "0.1"}
(fact "creates an Enum Single"
  ^:hidden
  
  (defn.js FormEnumSingleDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:currency "XLM"})
               {:currency []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormEnumSingle"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormEnumSingle
         {:design {:type "light"}
          :label "Currency"
          :form form
          :field "currency"
          :options ["XLM" "USD" "STATS"]}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormEnumSingle
         {:design {:type "dark"}
          :label "Currency"
          :form form
          :field "currency"
          :options ["XLM" "USD" "STATS"]}]]]))))

^{:refer melbourne.slim-common/FormEnumMulti :added "0.1"}
(fact "creates an Enum Multi"
  ^:hidden
  
  (defn.js FormEnumMultiDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:currency ["XLM" "STATS"]})
               {:currency []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormEnumMulti"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormEnumMulti
         {:design {:type "light"}
          :label "Currency"
          :form form
          :field "currency"
          :options ["XLM" "USD" "STATS"]}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormEnumMulti
         {:design {:type "dark"}
          :label "Currency"
          :form form
          :field "currency"
          :options ["XLM" "USD" "STATS"]}]]]))))

^{:refer melbourne.slim-common/FormColorInput :added "4.0"}
(fact "creates a Color Input"
  ^:hidden
  
  (defn.js FormColorInputDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:color "#456789"})
               {:color []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormColorInput"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormColorInput
         {:design {:type "light"}
          :label "Color"
          :form form
          :field "color"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormColorInput
         {:design {:type "dark"}
          :label "Color"
          :form form
          :field "color"}]]]))))

^{:refer melbourne.slim-common/FormChipInput :added "4.0"}
(fact "creates a Chip Input"
  ^:hidden
  
  (defn.js FormChipInputDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:tags ["football" "sport"]})
               {:tags []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormChipInput"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormChipInput
         {:design {:type "light"}
          :label "Tags"
          :form form
          :field "tags"}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% slim-common/FormChipInput
         {:design {:type "dark"}
          :label "Tags"
          :form form
          :field "tags"}]]]))))

^{:refer melbourne.slim-common/FormLayout :added "0.1"}
(fact "creates the basic form layout"
  ^:hidden
  
  (defn.js FormLayoutDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:currency ["XLM" "STATS"]
                      :currency1 "USD"
                      :name    ""
                      :about   ""})
               {:currency  []
                :currency1 []
                :name      []
                :about     []}))
    (return
     (n/EnclosedCode 
{:label "melbourne.slim-common/FormLayout"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :padding 20
                 :flex 1}}
        [:% slim-common/FormLayout
         {:design {:type "light"}
          :form form
          :rows [{:component slim-common/FormEnumMulti
                  :label "Currency"
                  :field "currency"
                  :options ["XLM" "USD" "STATS"]}
                 {:component slim-common/FormEnumSingle
                  :label "Currency1"
                  :field "currency1"
                  :options ["USD" "STATS"]}
                 {:component slim-common/FormInput
                  :label "Name"
                  :field "name"}
                 {:component slim-common/FormTextArea
                  :label "About"
                  :field "about"}]}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :padding 20
                 :flex 1}}
        [:% slim-common/FormLayout
         {:design {:type "dark"}
          :form form
          :rows [{:component slim-common/FormEnumMulti
                  :label "Currency"
                  :field "currency"
                  :options ["XLM" "USD" "STATS"]}
                 {:component slim-common/FormEnumSingle
                  :label "Currency1"
                  :field "currency1"
                  :options ["USD" "STATS"]}
                 {:component slim-common/FormInput
                  :label "Name"
                  :field "name"}
                 {:component slim-common/FormTextArea
                  :label "About"
                  :field "about"}]}]]])))
  
  (def.js MODULE (!:module))
  )
