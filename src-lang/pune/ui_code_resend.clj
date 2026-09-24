(ns pune.ui-code-resend
  (:use code.test)
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n]
             [melbourne.slim-submit :as slim-submit]
             [melbourne.slim-error :as slim-error]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]
             [xt.event.base-form :as event-form]
             [melbourne.ui-text :as ui-text]
             [pune.common.ext-util :as ext-util]]
   :export [MODULE]})

(defn.js CodeResendButton
  [#{[form
      meta
      design
      (:= field "email")
      sinkId
      submitProps]}]
  (var submitOptions (slim-submit/useSubmitField
                      #{[form
                         meta
                         field
                         :explicit false
                         (:.. submitProps)]}))
  (var #{onAction
         result
         setResult} submitOptions)
  (var #{disabled
         updated
         seconds} (ext-util/useResendDelay result))
  (return
   [:<>
    [:% slim-submit/SubmitButton
     #{[:design design
        :variant (ui-text/minorButtonTheme
                  {:key "background"
                   :tone "sharpen"}
                  {:key "neutral"})
        
        form
        :disabled disabled
        :text (+ "Resend Code" (:? disabled
                                   (+ " (" seconds ")")
                                   ""))
        :style {:width 160}
        :onPress onAction]}]
    [:% n/Portal
     {:target sinkId}
     (:? (lib/is-number? updated)
         [:<>
          [:% n/Padding {:style {:height 20}}]
          [:% ui-text/Caption
           {:design design}
           (+ "Code sent to " (event-form/get-field form field)
              " at " (new Date updated) ".")]]
         
         (and result (== "error" (. result ["status"])))
         [:<>
          [:% n/Padding {:style {:height 20}}]
          [:% slim-error/ErrorInfo
           {:design design
            :result result
            :onClose (fn:> (setResult nil))}]]
         
         :else nil)]]))

(def.js MODULE (!:module))
