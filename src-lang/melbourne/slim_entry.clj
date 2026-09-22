(ns melbourne.slim-entry
  (:require [lang.core :as  l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn [:icon :entypo]]]
             [js.react-native.ui-util :as ui-util]
             [js.react.ext-form :as ext-form]
             [xt.event.base-route :as event-route]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-text-dialog :as ui-text-dialog]
             [melbourne.ui-static :as ui-static]
             [melbourne.ui-dropdown :as ui-dropdown]
             [melbourne.slim-common :as slim-common]
             [melbourne.slim-image :as slim-image]
             [melbourne.slim-number :as slim-number]
             [melbourne.slim-select :as slim-select]
             [melbourne.slim-submit :as slim-submit]
             [melbourne.slim-time :as slim-time]
             [melbourne.slim-link :as slim-link]
             [melbourne.base-font :as base-font]
             [melbourne.base-palette :as base-palette]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.js EntryImplNotFound
  "creates a not found entry"
  {:added "4.0"}
  [#{design}]
  (return [:% ui-text/H4 #{design} "IMPL TYPE NOT FOUND"]))


;;
;; BASELINE
;;

(defn.js EntryFree
  "creates a free component"
  {:added "4.0"}
  [props]
  (var #{[impl
          (:.. rprops)]} props)
  (var #{[component
          (:.. rimpl)]} impl)
  (return
   (r/% component (Object.assign rprops
                            rimpl
                            {:impl rimpl}))))

(defn.js EntryContentRaw
  "creates a raw content view"
  {:added "4.0"}
  [props]
  (var #{design
         entry
         impl} props)
  (return
   (r/% ui-static/Text
        (Object.assign
         #{design
           {:style {:margin 10}}}
         impl)
        (n/format-entry entry))))

(defn.js EntryContentRawForm
  "creates a raw content view"
  {:added "4.0"}
  [props]
  (var #{design
         form} props)
  (var data (ext-form/listenFormData form))
  (return
   (r/% ui-static/Text
        #{design
          {:style {:margin 10}}}
        (n/format-entry data))))

(defn.js EntryContentFill
  "adds space filler for layouts"
  {:added "4.0"}
  [props]
  (return
   [:% n/Fill]))

(defn.js entryLayoutDiv
  "creates a layout component"
  {:added "4.0"}
  [props defaultComponent defaultStyle]
  (var #{[impl
          display
          (:.. rprops)]} props)
  (var #{[key
          component
          body
          debug
          style
          variant
          (:.. iprops)]} impl)
  (var #{[;;style
          (:.. cprops)]} (or (data/get-in props ["custom" key])
                             {}))
  (return
   (r/% (or component
            defaultComponent)
        (Object.assign rprops
                  #{[:style [{:marginVertical 2}
                             defaultStyle
                             style]
                     :variant (or variant {:bg nil
                                           :fg nil})]}
                  iprops
                  cprops)
        body)))

(defn.js EntryLayoutHorizontal
  "creates a horizontal layout"
  {:added "4.0"}
  [props]
  (return (-/entryLayoutDiv props ui-static/Div {:flexDirection "row"})))

(defn.js EntryLayoutVertical
  "creates a vertical layout"
  {:added "4.0"}
  [props]
  (return (-/entryLayoutDiv props ui-static/Div {})))

(defn.js EntryLayoutEnclosed
  "creates an enclosed layout"
  {:added "4.0"}
  [#{[impl
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% slim-common/FormEnclosed (Object.assign rprops iprops)
               [:% n/Row
                {:style {:flexDirection "row-reverse"}}
                body])))

(defn.js EntryLayoutPortal
  "creates an portal layout"
  {:added "4.0"}
  [#{[impl
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% n/Portal
               (Object.assign rprops iprops)
               body)))

(defn.js EntryLayoutPortalSink
  "creates an portal layout"
  {:added "4.0"}
  [#{[impl
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% n/PortalSink
               (Object.assign rprops iprops)
               body)))

(defn.js EntryLayoutScroll
  "creates a scrollview"
  {:added "4.0"}
  [#{[impl
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% ui-static/ScrollView
               (Object.assign rprops iprops)
               body)))

(defn.js EntryLayoutPopup
  "creates a popup view"
  {:added "4.0"}
  [#{[(:= impl {})
      (:.. rprops)]}]
  (var #{[style
          (:.. iprops)]} impl)
  (return (r/% ui-text/ButtonTooltip
               (Object.assign rprops
                         iprops
                         {:style [{:padding 5}
                                  (:.. (data/arrayify style))]}))))

(defn.js EntryLayoutDebug
  "creates a debug view"
  {:added "4.0"}
  [#{[(:= impl {})
      entry
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% ui-text/TextAlt
               (Object.assign rprops iprops
                         {:value entry})
               body)))

(defn.js EntryLayoutFadeIn
  "creates a debug view"
  {:added "4.0"}
  [#{[(:= impl {})
      (:.. rprops)]}]
  (var #{[body
          (:.. iprops)]} impl)
  (return (r/% ui-util/FadeIn
               (Object.assign rprops iprops)
               body)))

(defn.js EntryContentSeparator
  "creates a separator"
  {:added "4.0"}
  [props]
  (var #{[(:= impl {})
          (:.. rprops)]} props)
  (var #{[key
          header
          (:= variant {:fg {:key  "primary"
                            #_#_#_#_
                            :mix  "background"
                            :ratio 4}})
          (:.. iprops)]} impl)
  (return
   [:% n/View
    {:style {:marginHorizontal (:? header 0 5)}}
    (r/% ui-static/Separator
         (Object.assign rprops
                   iprops
                   #{variant}
                   (data/get-in props ["custom" key])))]))

(defn.js entryContentText
  "creates either a title or context component"
  {:added "4.0"}
  [props defaultComponent]
  (var #{[impl
          entry
          design
          (:.. rprops)]} props)
  (var #{[key
          component
          variant
          style
          (:.. iprops)]} impl)
  (var #{[template
          (:= format lib/identity)]} impl)
  (var entryData (or (data/template-entry entry template props)
                     ""))
  (var children "")
  (try
    (:= children (format entryData props))
    (catch e))
  (when (and (not (r/isValidElement children))
             (not (lib/is-string? children)))
    (:= children (xt/x:json-encode children)))
  (var oprops (Object.assign rprops
                        #{entry}
                        iprops
                        (data/get-in props ["custom" key])))
  (return
   (r/% (or component
            defaultComponent)
        (Object.assign oprops #{design variant style})
        children)))

(defn.js EntryContentTitleH1
  "creates a h4 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H1)))

(defn.js EntryContentTitleH2
  "creates a h4 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H2)))

(defn.js EntryContentTitleH3
  "creates a h4 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H3)))

(defn.js EntryContentTitleH4
  "creates a h4 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H4)))

(defn.js EntryContentTitleH5
  "creates a h5 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H5)))

(defn.js EntryContentTitle
  "creates a h6 title"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/H6)))

(defn.js EntryContentBold
  "creates a bold text"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/Bold)))

(defn.js EntryContentParagraph
  "creates a paragraph"
  {:added "4.0"}
  [props]
  (return (-/entryContentText props ui-text/P)))

(defn.js EntryContentIcon
  "creates a paragraph"
  {:added "4.0"}
  [props]
  (var #{impl
         display
         entry} props)
  (var #{[key
          template
          (:= format lib/identity)
          (:.. iprops)]} impl)
  (var #{[style
          (:.. cprops)]} (or (data/get-in props ["custom" key])
                             {}))
  (var name (format (data/template-entry entry template props)))
  (return
   (r/% ui-text/Icon
        (Object.assign {}
         props
         iprops
         #{name}
         cprops))))

(defn.js EntryContentImage
  "creates entry card avatar"
  {:added "4.0"}
  [props]
  (var #{impl
         entry} props)
  (var #{[key
          text
          style
          image
          color
          (:= format lib/identity)
          (:.. iprops)]} impl)
  (var #{[
          (:.. cprops)]} (or (data/get-in props ["custom" key])
                             {}))
  (var textInput  (:? text
                      (data/template-entry entry (. text template) props)
                      ""))
  (var colorInput (:? (lib/not-nil? color)
                      (data/template-entry entry (. color template) props)
                      ""))
  (var imageInput (:? image
                      (data/template-entry entry (. image template) props)))
  (return
   (r/% ui-text/Avatar
        (Object.assign {}
         props
         {:color  (lib/not-nil? color)
          :text   (:? textInput (format textInput))
          :image  imageInput
          :styleText  (:? text  (data/get-in props ["custom" (. text key)]))
          :styleImage (:? (data/not-empty? image) (data/get-in props ["custom" (. image key)]))
          :style  [{:margin 5}
                   (:? (and (lib/not-nil? color)
                            (lib/nil? imageInput))
                       {:backgroundColor colorInput})
                   (:.. (data/arrayify style))
                   (data/arrayify cprops.style)]}
         iprops
         cprops))))

(defn.js EntryContentPair
  "creates entry content pair"
  {:added "4.0"}
  [props]
  (var #{design
         impl
         entry
         mini} props)
  (var #{[key
          style
          title
          text
          body]} impl)
  (return
   [:% n/Row
    {:style [{:maxWidth (:? (not mini) 280)}
             style]}
    [:% n/View
     {:style {:width (:? (not mini) 80)
              :marginRight 5}}
     (:? title
         (r/% -/EntryContentTitle
              (Object.assign {} props
                           {:impl (Object.assign
                                   {:variant {:fg {:key "neutral"}}}
                                   title)})))]
    (:? (data/not-empty? body)
        body
        
        text
        (r/% -/EntryContentParagraph
             (Object.assign {} props {:impl text}))

        :else nil)]))

(def.js FIELD_COMPONENTS
  {:toggle      slim-common/FormToggleButton
   :switch      slim-common/FormToggleSwitch
   :input       slim-common/FormInput
   
   :input-xl    slim-common/FormInputXL
   :text        slim-common/FormTextArea
   
   :readonly    slim-common/FormReadOnly
   :enum-single slim-common/FormEnumSingle
   :enum-multi  slim-common/FormEnumMulti
   :color       slim-common/FormColorInput
   :chip        slim-common/FormChipInput
   
   :image       slim-image/FormImage
   :dropdown    slim-select/FormDropdown
   :picker        slim-select/FormPicker
   :picker-basic  slim-select/FormPickerBasic
   :slider      slim-number/FormSlider
   :spinner     slim-number/FormSpinner
   :spinner-basic slim-number/FormSpinnerBasic

   :time        slim-time/FormTime
   :date        slim-time/FormDate
   :datetime    slim-time/FormDatetime
   
   :link-readonly slim-link/FormLinkEntryReadOnly
   :link-dropdown slim-link/FormLinkDropdown})

(defn.js EntryContentField
  "creates an entry field"
  {:added "4.0"}
  [props]
  (var #{[impl
          display
          (:.. rprops)]} props)
  (var #{[component
          key
          field
          fieldProps
          (:.. iprops)]} impl)
  (var FieldComponent (:? (lib/is-string? component)
                          (xt/x:get-key -/FIELD_COMPONENTS
                                     component)
                          (or component
                              slim-common/FormInput)))
  (when (lib/is-function? fieldProps)
    (:= fieldProps (fieldProps props)))
  (var aprops (Object.assign rprops
                        #{field fieldProps}
                        {:className (+ "field-" field)}
                        iprops
                        (data/get-in props ["custom" key])))  
  (return
   (r/% FieldComponent aprops)))

(defn.js EntryLayoutFormFade
  "creates a debug view"
  {:added "4.0"}
  [props]
  (var #{[impl
          form]} props)
  (var #{[body
          watch
          template
          (:.. iprops)]} impl)
  (var #{fieldData} (ext-form/listenFieldsData form watch))
  (return (r/% ui-util/Fade
               (Object.assign iprops
                         {:visible (data/template-entry fieldData template props)})
               body)))

(defn.js EntryLayoutFormFold
  "creates a debug view"
  {:added "4.0"}
  [props]
  (var #{[impl
          form]} props)
  (var #{[body
          watch
          template
          (:.. iprops)]} impl)
  (var #{fieldData} (ext-form/listenFieldsData form watch))
  (return (r/% ui-util/Fold
               (Object.assign iprops
                         {:visible (data/template-entry fieldData template props)})
               [:% n/View body])))

(defn.js entrySubmitType
  "picks submit type based on submit key"
  {:added "4.0"}
  [submit submitType]
  (return (or (:? (lib/is-function? submit)
                  "custom")
              
              submitType
              (:? (== submit "create")
                  "form"
                  
                  (== submit "detail")
                  "id"
                  
                  (== submit "modify")
                  "modify"
                  
                  :else "entry"))))

(defn.js entryOnSubmit
  "creates onSubmit function based on submitType"
  {:added "4.0"}
  [submitFn submitType entry form props args]
  (:= args (or args []))
  (return
   (:? (lib/nil? submitType)
       (fn:>)

       (== submitType "custom")
       (fn:> (submitFn entry props (:.. args)))
       
       (== submitType "none")
       (fn:> (submitFn (:.. args)))
       
       (== submitType "id")
       (fn:> (submitFn (. entry id) (:.. args)))

       (== submitType "entry")
       (fn:> (submitFn entry (:.. args)))

       (== submitType "form")
       (fn:> (submitFn (. form data) (:.. args)))
       
       (== submitType "modify")
       (fn:> (submitFn (. entry id) (. form data) (:.. args)))

       (lib/is-function? submitType)
       (fn:> (submitType submitFn entry form))
       
       :else
       (fn:> (submitFn (and entry
                            (. entry id))
                       (:.. args))))))

(defn.js entryControlFn
  "creates the control function"
  {:added "4.0"}
  [control type props]
  (return (:? (== type "list")
              (. control setShowList)

              (== type "modify")
              (. control setShowModify)

              (== type "detail")
              (. control setShowDetail)

              (== type "create")
              (. control setShowCreate)

              (lib/is-function? type)
              (fn []
                (type control props))
              
              :else (. control [type]))))

(defn.js entryOnControl
  "creates the control onPress/onSuccess lambda"
  {:added "4.0"}
  [control controlType entry props]
  (when (not controlType)
    (return (fn:>)))
  
  (when (== controlType "back")
    (return
     (fn []
       (cond (. control backAction)
             (do (. control (backAction true))
                 (. control (setBackAction nil)))


             (. control showModify)
             (. control (setShowModify false))
             
             :else
             (do (. control (setShowCreate false))
                 (. control (setShowDetail nil)))))))
  (var onControl   (-/entryControlFn control controlType props))
  (return (-/entryOnSubmit onControl onControl entry props)))

(defn.js EntryContentControl
  "creates an control button"
  {:added "4.0"}
  [props]
  (var #{[impl
          entry
          (:= control {})
          (:.. rprops)]} props)
  (var #{[(:= component "minor")
          key
          submitType
          submit
          popup
          (:.. iprops)]} impl)
  (var onSubmit  (-/entryOnControl control submit entry props))
  (var fprops (or (data/get-in props ["custom" key])
                  {}))
  
  (var ControlComponent (or (. {:minor  ui-text/ButtonMinor
                                :accent ui-text/ButtonAccent}
                               [component])
                            component))
  (var onPress onSubmit)
  (when popup
    (:= onPress (fn []
                  (onSubmit)
                  (. props (setVisible false)))))
  (return
   (r/% ControlComponent
        (Object.assign rprops
                  {:style [{:width nil
                            :padding 5}
                           base-font/fontP] 
                   :onPress onPress}
                  iprops
                  fprops))))

(defn.js EntryLayoutControl
  [props]
  (var #{[impl
          entry
          control
          (:.. rprops)]} props)
  (var #{[submit
          body
          (:.. iprops)]} impl)
  (var onSubmit (-/entryOnControl control submit entry props))
  (return (r/% n/TouchableOpacity
               (Object.assign iprops {:onPress onSubmit})
               body)))

(defn.js EntryContentLink
  "creates a link button"
  {:added "4.0"}
  [props]
  (var #{[impl
          form
          entry
          route
          (:.. rprops)]} props)
  (var #{[(:= component "minor")
          key
          template
          (:.. iprops)]} impl)
  (var fprops (or (data/get-in props ["custom" key])
                  {}))
  (var ControlComponent (or (. {:minor  ui-text/ButtonMinor
                                :accent ui-text/ButtonAccent}
                               [component])
                            component))
  (var url (data/template-entry entry template props))
  (var onPress
       (fn []
         (event-route/set-url route url true)))
  (return
   (r/% ControlComponent
        (Object.assign rprops
                  {:style [{:width nil
                            :padding 5}
                           base-font/fontP] 
                   :onPress onPress}
                  iprops
                  fprops))))

(defn.js EntryLayoutLink
  [props]
  (var #{[impl
          form
          entry
          route
          (:.. rprops)]} props)
  (var #{[key
          template
          body
          (:.. iprops)]} impl)
  (var url     (data/template-entry entry template props))
  (var onPress 
       (fn []
         (event-route/set-url route url true)))
  (return (r/% n/TouchableOpacity
               (Object.assign iprops {:onPress onPress})
               body)))

(defn.js EntryContentRoute
  "creates a content route"
  {:added "4.0"}
  [props]
  (var #{[impl
          mini
          form
          entry
          control
          (:.. rprops)]} props)
  
  (var #{[(:= component "minor")
          key
          submit
          noMini
          (:.. iprops)]} impl)
  (var [route setRoute] [(. control [submit])
                         (. control [(+ "set" (string/capitalize submit))])])
  
  (var fprops (or (data/get-in props ["custom" key])
                  {}))
  (cond (or (not mini)
            noMini)
        (do 
          (var ControlComponent (or (. {:minor  ui-text/TabsMinor
                                        :accent ui-text/TabsAccent}
                                       [component])
                                    component))
          (return
           (r/% ControlComponent
                (Object.assign rprops
                          {:style [{:width nil
                                    :padding 5}] 
                           :value route
                           :setValue setRoute}
                          iprops
                          fprops))))

        :else
        (return
         (r/% ui-dropdown/Dropdown
              (Object.assign rprops
                        {:style [{:width 130
                                  :padding 7}]
                         :styleMenuItem {:width 150}
                         :value route
                         :setValue setRoute}
                        iprops
                        fprops)))))

(defn.js EntryContentRouteToggle
  "creates a content route"
  {:added "4.0"}
  [props]
  (var #{[impl
          mini
          form
          entry
          (:= control {})
          (:.. rprops)]} props)
  
  (var #{[(:= component "minor")
          key
          submit
          valueOn
          valueOff
          (:.. iprops)]} impl)
  (var [route setRoute] [(. control [submit])
                         (. control [(+ "set" (string/capitalize submit))])])
  
  
  (var fprops (or (data/get-in props ["custom" key])
                  {}))
  (var ControlComponent (or (. {:minor  ui-text/ToggleMinor
                                        :accent ui-text/ToggleAccent}
                                       [component])
                            component))
  (return
   (r/% ControlComponent
        (Object.assign rprops
                  {:style [{:width nil
                            :padding 5}] 
                   :selected (== route valueOn)
                   :onPress  (fn:> (setRoute
                                    (:? (== route valueOn)
                                        valueOff
                                        valueOn)))}
                  iprops
                  fprops))))

(defn.js EntryContentAction
  "creates an action"
  {:added "4.0"}
  [props]
  (var #{[impl
          form
          entry
          (:= actions {})
          (:.. rprops)]} props)
  (var #{[key
          custom
          submitType
          submit
          args
          confirm
          control
          style
          popup
          onSuccess
          onError
          (:.. iprops)]} impl)
  (var submitFn  (:? (lib/is-function? submit)
                     submit
                     (. actions [submit])))
  (:= submitType (-/entrySubmitType submit submitType))
  (var onSubmit  (-/entryOnSubmit submitFn submitType entry form props args))
  (:=  onSuccess (or onSuccess
                     (-/entryOnControl (. props control)
                                       (data/get-in control ["success"])
                                       entry
                                       props)))
  
  (var sprops (or (data/get-in props ["custom" submit])
                  {}))
  (var submitProps (r/useSubmitResult
                    (Object.assign #{onSubmit
                                onError
                                onSuccess}
                              sprops)))
  (var #{onActionPress} submitProps)
  (var onPress onActionPress)
  (when popup
    (:= onPress (fn []
                  (. props (setVisible false))
                  (onActionPress))))
  (var ActionComponent (:? (== confirm "tooltip")
                           ui-text/ConfirmTooltip
                           
                           (== confirm "dialog")
                           ui-text-dialog/ConfirmDialog
                           
                           :else
                           (:? (== (. impl component)
                                   "accent")
                               ui-text/ButtonAccent
                               ui-text/ButtonMinor)))
  (var output (Object.assign rprops
                        {:style [{:width nil
                                  :padding 5}
                                 base-font/fontP
                                 style]
                         :actions actions
                         :onPress onPress}
                        iprops
                        submitProps
                        custom))
  (return
   (r/% ActionComponent output)))




(defn.js EntryContentSubmit
  "creates a layout form"
  {:added "4.0"}
  [props]
  (var #{[impl
          form
          (:= actions {})
          entry
          (:.. rprops)]} props)
  (var #{[key
          control
          explicit
          keep
          args
          field
          onSuccess
          onError
          submit
          submitType
          submitModify
          submitField
          (:.. iprops)]} impl)
  (:= form    (or form (ext-form/makeForm (fn:> {})
                                          {})))
  (var sprops (or (data/get-in props ["custom" submit])
                  {}))
  (var fprops (or (data/get-in props ["custom" key])
                  {}))
  (var submitFn  (:? (lib/is-function? submit)
                     submit
                     (. actions [submit])))
  (:= submitType (-/entrySubmitType submit submitType))
  (var onSubmit  (-/entryOnSubmit submitFn submitType entry form props args))
  (:=  onSuccess (or onSuccess
                     (-/entryOnControl (. props control)
                                       (data/get-in control ["success"])
                                       entry
                                       props)))
  (var useSubmit   (:? submitField
                       slim-submit/useSubmitField
                       slim-submit/useSubmitForm))
  
  (var submitProps (useSubmit
                    (Object.assign #{field
                                form
                                onSubmit
                                onSuccess
                                onError
                                explicit
                                keep}
                              sprops)))

  ;;
  ;;
  ;;
  (var disabled false)
  (when submitModify
    (:= disabled (:? (lib/is-array? submitModify)
                     (tree/eq-nested (data/obj-pick (. form data) submitModify)
                                  (data/obj-pick (or entry {}) submitModify))
                     (tree/eq-nested (. form data)
                                  entry))))
  (var nprops (Object.assign rprops
                        #{form disabled}
                        iprops
                        fprops
                        submitProps))
  (return
   (r/% slim-submit/SubmitLineActions nprops)))

;;
;;
;;

(defn.js EntryLayoutCard
  "creates a card entry"
  {:added "4.0"}
  [props]
  (var #{[design
          impl
          (:.. rprops)]} props)
  (var #{[body
          style
          variant
          (:.. rimpl)]} impl)
  (var #{isHeader
         avatar
         right
         title
         main
         footer} body)
  (return
   [:% ui-static/Div
    #{[design
       variant
       :style [{:flex 1}
               (:.. (data/arrayify style))]
       #_(:.. rprops)]}
    [:% n/Row
     avatar
     (:? avatar
         [:% n/Padding {:style {:width 5}}])
     [:% n/View
      {:style [{:flex 1}
               (:? (not isHeader)
                   {:marginTop 10})]}
      (:? title
          [:<>
           title
           [:% n/Padding {:style {:height 5}}]])
      main
      (:? (and body footer)
          [:% n/Padding {:style {:height 5}}])
      footer]
     (:? right
         [:<>
          [:% n/Padding {:style {:width 5}}]
          right])]]))

(defn.js EntryLayoutForm
  "creates a layout form"
  {:added "4.0"}
  [props]
  (var #{[impl
          form
          (:= actions {})
          entry
          (:.. rprops)]} props)
  (var #{body
         style} impl)
  (return
   [:% n/View
    {:style [{:display "block"
              :margin 5}
             style]}
    body
    [:% n/Padding {:style {:height 10}}]]))

(def.js ENTRY_PROPS
  ["design"
   "variant"
   "mini"
   "miniTablet"
   "entry"
   "display"
   "custom"
   "form"
   "hooks"
   "route"
   "actions"
   "views"
   "control"
   "data"
   "parent"
   "visible"
   "setVisible"])

(def.js ENTRY_COMPONENTS
  {:card     -/EntryLayoutCard
   :h        -/EntryLayoutHorizontal
   :v        -/EntryLayoutVertical
   :form     -/EntryLayoutForm
   :form-fade  -/EntryLayoutFormFade
   :form-fold  -/EntryLayoutFormFold
   :enclosed -/EntryLayoutEnclosed
   :portal   -/EntryLayoutPortal
   :portal-sink   -/EntryLayoutPortalSink
   :scroll   -/EntryLayoutScroll
   :popup    -/EntryLayoutPopup
   :debug    -/EntryLayoutDebug
   :fade_in  -/EntryLayoutFadeIn

   :control-layout   -/EntryLayoutControl
   :link-layout   -/EntryLayoutLink
   
   :free      -/EntryFree
   :raw       -/EntryContentRaw
   :raw-form  -/EntryContentRawForm
   :fill      -/EntryContentFill
   :pair      -/EntryContentPair
   :submit    -/EntryContentSubmit
   :action    -/EntryContentAction
   :control   -/EntryContentControl
   
   :route     -/EntryContentRoute
   :route-toggle  -/EntryContentRouteToggle
   :link      -/EntryContentLink
   :title     -/EntryContentTitle
   :title-h5  -/EntryContentTitleH5
   :title-h4  -/EntryContentTitleH4
   :title-h3  -/EntryContentTitleH3
   :title-h2  -/EntryContentTitleH2
   :title-h1  -/EntryContentTitleH1
   :separator -/EntryContentSeparator
   :bold  -/EntryContentBold
   :p     -/EntryContentParagraph
   :icon  -/EntryContentIcon
   :image -/EntryContentImage
   :field -/EntryContentField})

(def.js ENTRY_LAYOUT
  {:card           true
   :pair           true
   :form           true
   :fade-in        true
   :form-fade      true
   :form-fold      true
   :enclosed       true
   :portal         true
   :portal-sink    true
   :h              true
   :v              true
   :scroll         true
   :popup          true
   :debug          true
   :control-layout true
   :link-layout    true})

(defn.js compileEntryPopup
  "compiles the Entry Popup"
  {:added "4.0"}
  [props compileFn]
  (var #{impl} props)
  (var eprops (data/obj-pick props -/ENTRY_PROPS))
  (var body (tree/tree-walk (. impl body)
                    lib/identity
                    (fn [e]
                      (cond (and (lib/is-object? e)
                                 (or (== (. e type) "action")
                                     (== (. e type) "control")))
                            (return (Object.assign {:popup true}
                                              e))
                            
                            :else (return e)))))
  (var mainComponent
       (r/const
        (fn [mprops]
          (var entryFn (fn [impl i]
                         (return (compileFn (Object.assign {} mprops
                                                         {:key (or (. impl key)
                                                                   i)}
                                                         #{impl})))))
          (return [:% n/View
                   (j/map (or body []) entryFn)]))))
  (return
   (r/% -/EntryLayoutPopup (Object.assign #{mainComponent} props))))

(defn.js compileEntry
  "compiles the entry"
  {:added "4.0"}
  [props EntryComponent]
  (var #{[entry
          display
          components
          key
          views]} props)
  (var eprops (data/obj-pick props -/ENTRY_PROPS))
  (var impl  (or (data/get-in props ["impl"])
                 {}))
  (when (r/isValidElement impl)
    (return impl))
  
  (while (lib/is-function? (. impl props))
    (:= impl (or ((. impl props) entry props)
                 {})))
  
  (var #{[(:= type "p")]} impl)
  (var component (or (xt/x:get-key -/ENTRY_COMPONENTS type)
                     -/EntryImplNotFound))
  (var isLayout   (xt/x:get-key -/ENTRY_LAYOUT type))
  (var isAlias     (== type "alias"))
  (var isPopup     (== type "popup"))
  (var showFn  (fn [impl i]
                 (cond (not impl)
                       (return false)
                       
                       (. impl show)
                       (return ((. impl show) entry props))
                       
                       (. impl hide)
                       (return (not ((. impl hide) entry props)))
                       
                       :else
                       (return true))))
  
  (var entryFn (fn:> [impl i]
                 (-/compileEntry (Object.assign {} eprops
                                              {:key (or (. impl key)
                                                        i)}
                                              #{impl}))))
  (cond isAlias
        (do (var aliasComponent (or (. components [(. impl alias)])
                                    EntryComponent))
            (return
             (r/% aliasComponent (Object.assign {:impl (. display [(. impl alias)])}
                                           eprops))))

        isPopup
        (return (-/compileEntryPopup props -/compileEntry))
        
        isLayout
        (do  (var body (:? (lib/is-array? (. impl body))
                           (-> (. impl body)
                               (j/filter showFn)
                               (j/map entryFn))
                           
                           (lib/is-object? (. impl body))
                           (-> (. impl body)
                               (data/obj-filter showFn)
                               (data/obj-map entryFn))
                           
                           :else []))
             (return (r/% component (Object.assign {:key key
                                               :impl (Object.assign {} impl #{body})}
                                           eprops))))
        
        :else
        (do 
          (var oprops (Object.assign #{impl
                                  key
                                  {:views (. props views)}}
                                eprops))
          (return
           (r/% component oprops)))))

(defn.js Entry
  "creates the entry"
  {:added "4.0"}
  [props]
  (return (-/compileEntry props -/Entry)))

(def.js MODULE (!:module))

(comment
  {:layout    {:card  true
               :row   true
               :form  true
               :h     true
               :v     true}
   :content   {:free   true
               :title  true
               :p      true
               :pair   true
               :image  true
               :field  true
               :action true
               :separator true}})
