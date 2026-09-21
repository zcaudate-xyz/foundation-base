(ns melbourne.addon-validation-test
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
             [melbourne.addon-validation :as addon-validation]
             [melbourne.ui-input :as ui-input]
             [melbourne.base-validators :as validators]
             [xt.event.base-form :as base-form]]
   :export [MODULE]})

^{:refer melbourne.addon-validation/addonValidation :added "0.1"}
(fact "creates the form validation"
  ^:hidden
  
  (defn.js AddonValidationDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name ""})
               {:name [(validators/is-not-empty)]}))
    (var #{result data} (ext-form/listenForm form))
    (r/init []
      (base-form/validate-field))
    (return
     (n/EnclosedCode 
{:label "melbourne.addon-validation/addonValidation"} 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 10}}
        [:% ui-input/Input
         {:highlighted true
          :brand {:type "light"}
          :addons [(addon-validation/addonValidation
                    {:result (. result ["name"])
                     :brand {:type "light"}})]}]]
       [:% n/View
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 10}}
        [:% ui-input/Input
         {:highlighted true
          :brand {:type "dark"}
          :addons [(addon-validation/addonValidation
                    {:result (. result ["name"])
                     :brand {:type "dark"}})]}]]])))
  
  (def.js MODULE (!:module))
  )

