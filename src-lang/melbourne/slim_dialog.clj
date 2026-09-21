(ns melbourne.slim-dialog
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react-native :as n :include [:fn]]
             [js.react-native.ui-modal :as ui-modal]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-font :as base-font]
             [melbourne.base-font :as base-font]
             [melbourne.base-palette :as base-palette]
             [melbourne.base-font :as base-font]
             [melbourne.ui-static :as ui-static]
             [melbourne.slim-submit :as slim-submit]]
   :export [MODULE]})

(defn.js Dialog
  "creates the dialog"
  {:added "0.1"}
  [#{[design
      modalProps
      submitProps
      helperProps
      title
      titleRight
      body
      visible
      setVisible
      onSubmit
      onCancel
      children
      (:.. rprops)]}]
  (var palette (base-palette/designPalette design))
  (return
   [:% ui-modal/Modal
    #{[visible
       :position "centered"
       :styleBackdrop {:backgroundColor (base-palette/getColor palette {:key "neutral"})}
       :onClose onCancel
       (:.. modalProps)]}
    [:% ui-static/Div
     {:design design
      :style [{:maxWidth 400
               :minHeight 150
               :paddingHorizontal 15
               :paddingVertical 10
               :borderRadius 3}]}
     (:? title
         [:% n/Row
          [:% n/Text
           {:style [base-font/fontH4
                    {:color (base-palette/getColor palette {:key "neutral"})
                     :marginVertical 10}]}
           title]
          [:% n/Fill]
          titleRight])
     [:% n/View
      {:style {:flex 1
               :justifyContent "center"}}
      [:% n/Text
       {:style [base-font/fontText
                {:color (base-palette/getColor palette {:key "neutral"})
                 :marginVertical 10}]}
       body]]
     
     [:% n/View
      {:style {:flexDirection "row-reverse"
               :marginVertical 10}}
      [:% slim-submit/SubmitButton
       #{[design
          :text "OK"
          :onPress onSubmit
          (:.. submitProps)]}]
      [:% slim-submit/SubmitLineHelpers
       #{[design
          :cancelShow true
          :onCancel onCancel
          (:.. helperProps)]}]]]]))

(def.js MODULE (!:module))
