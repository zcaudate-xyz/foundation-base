(ns melbourne.slim-submit-test
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
           :require [[js.core.impl :as j]
                     [js.core :as jc]
                     [js.core.fetch :as fetch]
                     [xt.lang.common-data :as data]
                     [xt.lang.common-lib :as lib]
                     [js.react :as r :include [:fn]]
                     [js.react-native :as n :include [:fn]]
                     [js.react.ext-form :as ext-form]
                     [melbourne.slim-submit :as slim-submit]
                     [melbourne.base-validators :as validators]
                     [xt.event.base-form :as event-form]]
           :export [MODULE]})

^{:refer melbourne.slim-submit/useSubmit :adopt true :added "0.1"}
(fact "constructs a waiting flag"
  ^:hidden
  
  (defn.js UseSubmitDemo
    []
    (var onSubmit (fn:>
                    (jc/future-delayed [200]
                                      (return
                                       {:status "error"
                                        :body {:tag "user.account/incorrect_password"}}))))
    (var [result setResult] (r/local (fn:>)))
    (var #{waiting setWaiting
           onAction} (r/useSubmit #{setResult
                                    onSubmit}))
    (return
     (n/EnclosedCode 
      {:label "melbourne.slim-submit/useSubmit"} 
      [:% n/Row
       [:% n/View
        {:style {:backgroundColor "#eee"
                 :padding 10
                 :flex 1}}
        [:% n/Row
         [:% n/Button
          {:title "Action"
           :onPress onAction}]
         [:% n/Text " "]
         [:% n/Button
          {:title "Clear"
           :onPress (fn:> (setResult nil))}]
         [:% n/Text " "]]]] 
      [:% n/TextDisplay
       {:content (n/format-entry #{waiting
                                   result})}]))))

^{:refer melbourne.slim-submit/SubmitButton :added "0.1"}
(fact "constructs a submit button"
  ^:hidden
  
  (defn.js SubmitButtonDemo
    []
    (var [errored setErrored] (r/local true))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-submit/SubmitButton"} 
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :padding 10}}
         [:% slim-submit/SubmitButton
          {:design {:type "light"}
           :text "HELLO"}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitButton
          {:design {:type "light"}
           :waiting true
           :text "HELLO"}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitButton
          {:design {:type "light"}
           :style {:width 150
                   :textAlign "center"}
           :text "CHANGE PASSWORD"
           :errored errored
           :onPress (fn:> (setErrored (not errored)))}]]
        [:% n/View
         {:style {:backgroundColor "#333"
                  :padding 10}}
         [:% slim-submit/SubmitButton
          {:design {:type "dark"}
           :text "HELLO"}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitButton
          {:design {:type "dark"}
           :style {:width 150}
           :waiting true
           :text "HELLO"}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitButton
          {:design {:type "dark"}
           :style {:width 150}
           :errored errored
           :text "CHANGE PASSWORD"
           :onPress (fn:> (setErrored (not errored)))}]]])])))

^{:refer melbourne.slim-submit/SubmitLine :added "4.0"}
(fact "creates a submit line (with button and error)"
  ^:hidden
  
  (defn.js SubmitLineDemo
    []
    (var [errored setErrored] (r/local true))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-submit/SubmitLine"} 
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :padding 10}}
         [:% slim-submit/SubmitLine
          {:design {:type "light"}
           :errored errored
           :onActionPress (fn:> (setErrored (not errored)))
           :onActionReset (fn:> (setErrored false))}]]])])))

^{:refer melbourne.slim-submit/SubmitLineHelpers :added "4.0"}
(fact "creates the helper (cancel and clear) buttons")

^{:refer melbourne.slim-submit/SubmitLineActions :added "4.0"}
(fact "creates a submit line (with clear, cancel button and errors)"
  ^:hidden
  
  (defn.js SubmitLineActionsDemo
    []
    (var [errored setErrored] (r/local false))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-submit/SubmitLineActions"} 
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :padding 10}}
         [:% slim-submit/SubmitLineActions
          {:design {:type "light"}
           :errored errored
           :clearShow  true
           :cancelShow true
           :onActionPress (fn:> (setErrored (not errored)))
           :onActionReset (fn:> (setErrored false))}]]])])))

^{:refer melbourne.slim-submit/useSubmitField :added "4.0"}
(fact "use submit form compatible with the submit controls"
  ^:hidden
  
  (defn.js UseSubmitFieldDemo
    []
    (var form (ext-form/makeForm (fn:> {:email "a@a.com"})
                                 {:email [(validators/is-required)
                                          (validators/is-valid-email)]}))
    (var email  (ext-form/listenFieldValue form "email"))
    (var validation (ext-form/listenFieldResult form "email"
                                                {:custom "VALIDATION"}))
    (var submitProps
         (slim-submit/useSubmitField
          #{form
            {:field "email"
             :onSubmit (fn:>
                         (jc/future-delayed [500]
                                           (return {:status "error"
                                                    :tag "user.account/incorrect_password"})))
             :explicit false}}))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-submit/useSubmitField"} 
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :padding 10}}
         [:% n/TextInput
          {:value (event-form/get-field form "email")
           :onChangeText (fn [val]
                           (event-form/set-field form "email" val)
                           (event-form/validate-all form))}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitLine
          #{[:design {:type "light"}
             :errorProps {:row true}
             (:.. submitProps)]}]]
        [:% n/View
         {:style {:backgroundColor "#333"
                  :padding 10}}
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitLine
          #{[:design {:type "dark"}
             :errorProps {:row true}
             (:.. submitProps)]}]]] 
       [:% n/TextDisplay
        {:content (n/format-entry
                   #{submitProps
                     validation})}])]))
  )

^{:refer melbourne.slim-submit/useSubmitForm :added "4.0"}
(fact "use submit field compatible with the submit controls"
  ^:hidden
  
  (defn.js UseSubmitFormDemo
    []
    (var form (ext-form/makeForm (fn:> {:email "a@a.com"})
                                 {:email [(validators/is-required)
                                          (validators/is-valid-email)]}))
    (var email  (ext-form/listenFieldValue form "email"))
    (var validation (ext-form/listenFormResult
                     form
                     {:custom "VALIDATION"}))
    (var #{onAction
           onActionReset
           onActionCheck
           onActionPress
           errored
           waiting
           setWaiting
           result
           setResult} (slim-submit/useSubmitForm
                       #{form
                         {:onSubmit (fn:>
                                      (jc/future-delayed [500]
                                                        (return {:status "error"
                                                                 :tag "user.account/incorrect_password"})))
                          :explicit false}}))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
       {:label "melbourne.slim-submit/useSubmitForm"} 
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "#eee"
                  :padding 10}}
         [:% n/TextInput
          {:value (event-form/get-field form "email")
           :onChangeText (fn [val]
                           (event-form/set-field form "email" val)
                           (event-form/validate-all form))}]
         [:% n/Padding {:style {:height 10}}]
         [:% slim-submit/SubmitLine
          #{[:design {:type "light"}
             :errorProps {:row true}
             onAction
             onActionReset
             onActionCheck
             onActionPress
             errored
             waiting
             setWaiting
             result
             setResult]}]]] 
       [:% n/TextDisplay
        {:content (n/format-entry
                   #{errored waiting result
                     validation})}])]))
  
  (def.js MODULE (!:module)))
  
