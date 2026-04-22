(ns js.react-native.ext-form-test
  (:require [std.lang :as l])
  (:use code.test))

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
              [xt.lang.common-spec :as xt]
              [xt.lang.event-form :as event-form]]
    })

(def.js RegistraionValidation
  {:first-name [["is-not-empty" {:message "Must not be empty"
                                 :check (fn:> [v rec]
                                          (and (xt/x:not-nil? v)
                                               (< 0 (xt/x:len v))))}]]
   :last-name  [["is-not-empty" {:message "Must not be empty"
                                 :check (fn:> [v rec]
                                          (j/future-delayed [100]
                                            (return (and (xt/x:not-nil? v)
                                                         (< 0 (xt/x:len v))))))}]]
   :email      [["is-not-empty" {:message "Must not be empty"
                                 :check (fn:> [v rec]
                                          (j/future-delayed [100]
                                            (return (and (xt/x:not-nil? v)
                                                         (< 0 (xt/x:len v))))))}]]})

^{:refer js.react-native.ext-form-test/RegistrationForm
  :adopt true
  :added "0.1"}
(fact "adding a carosel stepper"

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
        [:% n/Text (:? (== "errored" (xt/x:get-path fields ["first_name" "status"]))
                       (xt/x:get-path fields ["first_name" "message"]))]]
       [:% n/Row
        [:% n/TextInput
         {:value last-name
          :onChangeText (event-form/field-fn form "last_name")
          :style {:margin 5
                  :padding 5
                  :backgroundColor "#eee"}}]
        [:% n/Text (:? (== "errored" (xt/x:get-path fields ["last_name" "status"]))
                       (xt/x:get-path fields ["last_name" "message"]))]]
       [:% n/Row
        [:% n/TextInput
         {:value email
          :onChangeText (event-form/field-fn form "email")
          :style {:margin 5
                  :padding 5
                  :backgroundColor "#eee"}}]
        [:% n/Text (:? (== "errored" (xt/x:get-path fields ["email" "status"]))
                       (xt/x:get-path fields ["email" "message"]))]]]
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
