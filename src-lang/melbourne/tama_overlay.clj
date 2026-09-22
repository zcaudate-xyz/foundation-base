(ns melbourne.tama-overlay
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/tama-overlay
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.tamagui :as tm]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

(defn.js Dialog
  "creates a controlled Tamagui dialog from the Slim dialog contract"
  [#{[visible
      setVisible
      title
      titleRight
      body
      children
      trigger
      onSubmit
      onCancel
      modalProps
      submitProps
      (:.. rprops)]}]
  (var closeFn
       (or onCancel
           (fn []
             (when setVisible
               (setVisible false)))))
  (var dialogProps
       (Object.assign
        {:modal true
         :open visible
         :onOpenChange (or setVisible (fn []))}
        (or modalProps {})
        rprops))
  (return
   [:% tm/Dialog
    #{(:.. dialogProps)}
    (:? trigger
        [:% tm/DialogTrigger
         {:asChild true}
         trigger]
        nil)
    [:% tm/DialogPortal
     [:% tm/DialogOverlay
      {:backgroundColor "rgba(15,23,42,0.45)" }]
     [:% tm/DialogContent
      {:borderRadius "$4"
       :padding "$4"
       :gap "$3"
       :minWidth 320
       :maxWidth 520}
      (:? title
          [:% tm/XStack
           {:alignItems "center"
            :justifyContent "space-between"
            :gap "$2"}
           [:% tm/DialogTitle
            {:fontSize 20
             :fontWeight "800"}
            title]
           titleRight]
          nil)
      (:? body
          [:% tm/DialogDescription
           {:color "$color11"}
           body]
          nil)
      children
      [:% tm/XStack
       {:justifyContent "flex-end"
        :gap "$2"}
       [:% tm/DialogClose
        {:asChild true}
        [:% tm/Button
         #{(:.. (Object.assign
                 {:size "$3"
                  :chromeless true
                  :onPress closeFn}
                 (or submitProps {})))}
         "CANCEL"]]
       [:% tm/DialogClose
        {:asChild true}
        [:% tm/Button
         #{(:.. (Object.assign
                 {:size "$3"
                  :onPress onSubmit}
                 (or submitProps {})))}
         "OK"]]]]]]))

(defn.js Popover
  "creates a direct Tamagui popover from the Slim popup contract"
  [#{[open
      setOpen
      trigger
      children
      content
      popoverProps
      contentProps]}]
  (var props
       (Object.assign
        (or popoverProps {})
        (:? (not (lib/nil? open))
            {:open open
             :onOpenChange setOpen}
            {})))
  (return
   [:% tm/Popover
    #{(:.. props)}
    (:? trigger
        [:% tm/PopoverTrigger
         {:asChild true}
         trigger]
        nil)
    [:% tm/PopoverContent
     #{(:.. (Object.assign
             {:borderRadius "$3"
              :padding "$3"
              :gap "$2"
              :elevate true}
             (or contentProps {})))}
     (or content children)
     [:% tm/PopoverClose
      {:asChild true}
      [:% tm/Button
       {:size "$2"
        :chromeless true}
       "DONE"]]]]))

(def.js MODULE (!:module))
