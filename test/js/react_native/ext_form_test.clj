(ns js.react-native.ext-form-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.react.ext-form :as ext-form]
             [xt.lang.base-lib :as k]
             [xt.lang.event-form :as event-form]]
   })

^{:refer js.react-native.ext-form-test/RegistrationForm
  :adopt true
  :added "0.1"}
(fact "adding a carosel stepper"
  ^:hidden

  (def.js RegistraionValidation
    {:first-name    [["is-not-empty" {:message "Must not be empty"
                                      :check (fn:> [v rec] (and (k/not-nil? v)
                                                                (< 0 (k/len v))))}]]
     :last-name     [["is-not-empty" {:message "Must not be empty"
                                      :check (fn:> [v rec]
                                               (j/future-delayed [100]
                                                 (return (and (k/not-nil? v)
                                                              (< 0 (k/len v))))))}]]
     :email         [["is-not-empty" {:message "Must not be empty"
                                      :check (fn:> [v rec]
                                                   (j/future-delayed [100]
                                                     (return (and (k/not-nil? v)
                                                                  (< 0 (k/len v))))))}]]})
  
  (defn.js RegistrationFormDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:first-name "hello"
                      :last-name "world"
                      :email ""})
               -/RegistraionValidation))
    
    (var #{first-name
           last-name
           email} (ext-form/listenFormData form))
    (var result   (ext-form/listenFormResult form))
    (var #{fields} result)
    (var getCount (r/useGetCount))
    (return
     (n/EnclosedCode 
{:label "js.react-native.ext-form-test/RegistrationForm"} 
[:% n/View
       {}
       [:% n/Row
        [:% n/TextInput
         {:value first-name
          :onChangeText (event-form/field-fn form "first_name")
          :style {:margin 5
                  :padding 5
                  :backgroundColor "#eee"}}]
        [:% n/Text (:? (== "errored" (k/get-path fields ["first_name" "status"]))
                       (k/get-path fields ["first_name" "message"]))]]
       [:% n/Row
        [:% n/TextInput
         {:value last-name
          :onChangeText (event-form/field-fn form "last_name")
          :style {:margin 5
                  :padding 5
                  :backgroundColor "#eee"}}]
        [:% n/Text (:? (== "errored" (k/get-path fields ["last_name" "status"]))
                       (k/get-path fields ["last_name" "message"]))]]
       [:% n/Row
        [:% n/TextInput
         {:value email
          :onChangeText (event-form/field-fn form "email")
          :style {:margin 5
                  :padding 5
                  :backgroundColor "#eee"}}]
        [:% n/Text (:? (== "errored" (k/get-path fields ["email" "status"]))
                       (k/get-path fields ["email" "message"]))]]] 
[:% n/Row
       [:% n/Button
        {:title "Validate"
         :onPress (fn:> (event-form/validate-all form))}]
       [:% n/Button
        {:title "Clear"
         :onPress (fn:> (event-form/reset-all-validators form))}]
       [:% n/Button
        {:title "Reset"
         :onPress (fn:> (event-form/reset-all-data form))}]] 
[:% n/Caption
       {:text (n/format-entry #{fields
                                {:count (getCount)
                                 :data #{first-name
                                         last-name
                                         email}}})
        :style {:marginTop 10}}])))
  

  (def.js MODULE
    (do (:# (!:uuid))
        (!:module)))
  
  )
