(ns melbourne.tama
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core.impl :as j]
             [js.react :as r]
             [js.tamagui :as tm]
             [melbourne.slim-core :as slim-core]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]]
   :export [MODULE]})

(defn.js entryValue
  "resolves and formats an entry value without legacy UI dependencies"
  {:added "4.0"}
  [entry impl props]
  (var #{[template
          (:= format lib/identity)]} (or impl {}))
  (var value (data/template-entry entry template props))
  (var output value)
  (try
    (:= output (format value props))
    (catch e))
  (return
   (:? (or (r/isValidElement output)
           (lib/is-string? output))
       output
       (xt/x:json-encode output))))

(defn.js entryChildren
  "turns an entry body into Tamagui children"
  {:added "4.0"}
  [props body renderFn]
  (cond (lib/nil? body)
        (return nil)

        (r/isValidElement body)
        (return body)

        (and (lib/is-object? body)
             (. body type))
        (return (renderFn body nil))

        (lib/is-array? body)
        (return
         (j/map body
                (fn [child i]
                  (return (renderFn child i)))))

        (lib/is-object? body)
        (return
         (j/map (xt/x:obj-keys body)
                (fn [key]
                  (return (renderFn (. body [key]) key)))))

        :else
        (return body)))

(defn.js renderEntry
  "renders the shared Slim entry contract with Tamagui primitives"
  {:added "4.0"}
  [props impl]
  (var #{entry} props)
  (when (r/isValidElement impl)
    (return impl))
  (:= impl (or impl {}))
  (var #{[type
          key
          component
          body
          text
          style
          template
          format
          onPress
          fieldProps
          image
          (:= gap "$2")
          (:.. iprops)]} impl)
  (var customProps (or (data/get-in props ["custom" key]) {}))
  (var renderFn
       (fn [child i]
         (return
          (renderEntry (Object.assign {} props
                                      {:key (or (. child key) i)})
                       child))))
  (var childrenFn
       (fn [value]
         (return (entryChildren props value renderFn))))
  (var textValue (entryValue entry impl props))
  (var textProps
       (Object.assign {:color "$color"}
                      iprops
                      customProps
                      (:? style {:style style} {})))
  (var layoutProps
       (Object.assign {:gap gap
                       :marginVertical "$1"}
                      iprops
                      customProps
                      (:? style {:style style} {})))
  (cond (== type "h")
        (return [:% tm/XStack #{(:.. layoutProps)} (childrenFn body)])

        (== type "v")
        (return [:% tm/YStack #{(:.. layoutProps)} (childrenFn body)])

        (== type "card")
        (return
         [:% tm/Card
          #{(:.. (Object.assign {:gap "$2"
                                 :padding "$3"
                                 :borderRadius "$4"}
                                iprops
                                customProps
                                (:? style {:style style} {})))}
          [:% tm/XStack
           {:gap "$3"
            :alignItems "center"}
           (:? image
               [:% tm/Avatar
                {:size "$5"}
                [:% tm/AvatarImage
                 {:src (data/template-entry entry
                                             (data/get-in image ["template"])
                                             props)}]
                [:% tm/AvatarFallback
                 [:% tm/Text
                  {}
                  (entryValue entry image props)]]])
           [:% tm/YStack
            {:flex 1
             :gap "$2"}
            (childrenFn body)]]])

        (or (== type "title-h1")
            (== type "h1"))
        (return [:% tm/H1 #{(:.. textProps)} textValue])

        (or (== type "title-h2")
            (== type "h2"))
        (return [:% tm/H2 #{(:.. textProps)} textValue])

        (or (== type "title-h3")
            (== type "h3"))
        (return [:% tm/H3 #{(:.. textProps)} textValue])

        (or (== type "title-h4")
            (== type "h4"))
        (return [:% tm/H4 #{(:.. textProps)} textValue])

        (or (== type "title-h5")
            (== type "h5"))
        (return [:% tm/H5 #{(:.. textProps)} textValue])

        (== type "title")
        (return [:% tm/H6 #{(:.. textProps)} textValue])

        (or (== type "bold")
            (== type "p"))
        (return
         [:% tm/Paragraph
          #{(:.. (Object.assign (:? (== type "bold")
                                  {:fontWeight "700"}
                                  {})
                                textProps))}
          textValue])

        (== type "raw")
        (return [:% tm/Text #{(:.. textProps)} textValue])

        (== type "fill")
        (return [:% tm/Spacer
                 #{(:.. (Object.assign {:flex 1} iprops customProps))}])

        (== type "separator")
        (return [:% tm/Separator
                 #{(:.. (Object.assign {:marginVertical "$2"}
                                       iprops
                                       customProps
                                       (:? style {:style style} {})))}])

        (== type "pair")
        (return
         [:% tm/XStack
          #{(:.. (Object.assign {:gap "$2"
                                 :justifyContent "space-between"}
                                iprops
                                customProps
                                (:? style {:style style} {})))}
          [:% tm/Text
           {:color "$colorSecondary"
            :flex 1}
           (childrenFn (. impl title))]
          [:% tm/Text
           {:color "$color"
            :textAlign "right"}
           (childrenFn (. impl text))]])

        (== type "control")
        (return
         [:% tm/Button
          #{(:.. (Object.assign {:size "$3"
                                 :onPress (or onPress (. props onPress))}
                                iprops
                                customProps))}
          (or text textValue)])

        (== type "field")
        (return
         [:% tm/Input
          #{(:.. (Object.assign {:size "$3"
                                 :value (or textValue "")
                                 :onChangeText (or (. props onChangeText)
                                                   (. impl onChangeText))}
                                fieldProps
                                iprops
                                customProps))}])

        (== type "icon")
        (return [:% tm/Text #{(:.. textProps)} textValue])

        (== type "image")
        (return
         [:% tm/Avatar
          #{(:.. (Object.assign {:size "$5"} iprops customProps))}
          [:% tm/AvatarImage
           {:src (data/template-entry entry
                                       (data/get-in image ["template"])
                                       props)}]
          [:% tm/AvatarFallback
           [:% tm/Text
            {}
            textValue]]])

        (== type "free")
        (return
         (r/% component
              (Object.assign props iprops customProps {:impl impl})
              (childrenFn body)))

        :else
        (return
         [:% tm/Text
          {:color "$red10"}
          "IMPL TYPE NOT FOUND: " type])))

(defn.js Entry
  "renders a Slim entry directly with Tamagui primitives"
  {:added "4.0"}
  [props]
  (return (renderEntry props (. props impl))))

(defn.js Table
  "renders a compact Tamagui table from the shared entry contract"
  {:added "4.0"}
  [props]
  (var #{[entries
          impl
          (:= columns [])]} props)
  (return
   [:% tm/YStack
    {:gap "$2"}
    [:% tm/XStack
     {:gap "$2"
      :paddingVertical "$2"
      :borderBottomWidth 1
      :borderColor "$borderColor"}
     (j/map columns
            (fn [column]
              (return
               [:% tm/Text
                {:flex 1
                 :fontWeight "700"}
                (or (. column label)
                    (. column name))])))]
    (j/map (or entries [])
           (fn [entry i]
             (return
              [:% tm/XStack
               {:key (or (. entry id) i)
                :gap "$2"
                :paddingVertical "$2"}
               (j/map columns
                      (fn [column]
                        (return
                         [:% tm/Text
                          {:flex 1}
                          (entryValue entry
                                      {:template (. column data)
                                       :format (. column format)}
                                      props)])))])))]))

(defn.js TableToolbar
  "renders table controls with direct Tamagui buttons"
  {:added "4.1"}
  [props]
  (var #{[design
          control
          children]} props)
  (var toolbarOpts (. props toolbarOpts))
  (:= toolbarOpts (or toolbarOpts {}))
  (var #{[(:= showCreate true)
          (:= showOrderBy true)]} toolbarOpts)
  (var showList (or (. control showList) true))
  (return
   [:% tm/XStack
    {:gap "$2"
     :alignItems "center"
     :paddingVertical "$2"
     :paddingHorizontal "$2"}
    (:? showCreate
        [:% tm/Button
         {:size "$2"
          :onPress (fn []
                     (when (. control setShowCreate)
                       (. control (setShowCreate true))))}
         (:? showList "CREATE" "BACK")]
        nil)
    children
    (:? showOrderBy
        [:% tm/XStack
         {:gap "$1"}
         [:% tm/Button
          {:size "$2"
           :chromeless true
           :onPress (fn []
                      (when (. control setOrderBy)
                        (. control (setOrderBy "name"))))}
          "NAME"]
         [:% tm/Button
          {:size "$2"
           :chromeless true
           :onPress (fn []
                      (when (. control setOrderBy)
                        (. control (setOrderBy "time"))))}
          "TIME"]]
        nil)]))

(defn.js TableList
  "renders entry cards directly with Tamagui primitives"
  {:added "4.1"}
  [props]
  (var #{[(:= entries [])
          (:= impl {})]} props)
  (var itemImpl
       (or (. impl item)
           (. props itemImpl)
           {:type "card"
            :body {:title {:type "title"
                           :template ["title"]}}}))
  (return
   [:% tm/ScrollView
    {:flex 1}
    (j/map entries
           (fn [entry i]
             (return
              [:% tm/YStack
               {:key (or (. entry id) i)
                :paddingBottom "$2"}
               (r/% Entry
                    (Object.assign {}
                                   props
                                   {:entry entry
                                    :impl itemImpl}))])))]))

(defn.js TableStandard
  "renders a table with a direct Tamagui empty state"
  {:added "4.1"}
  [props]
  (var #{[(:= entries [])
          control]} props)
  (var showList (or (. control showList) true))
  (return
   (:? (and (data/is-empty? entries)
            showList)
       [:% tm/YStack
        {:flex 1
         :gap "$3"
         :alignItems "center"
         :justifyContent "center"}
        [:% tm/Text
         {:color "$color11"}
         "No entries"]
        [:% tm/Button
         {:size "$3"
          :onPress (fn []
                     (when (. control setShowCreate)
                       (. control (setShowCreate true))))}
         "ADD"]]
       (r/% Table props))))

(defn.js TableEmbedded
  "renders an embedded table with a direct Tamagui create affordance"
  {:added "4.1"}
  [props]
  (var #{[(:= entries [])
          control]} props)
  (return
   [:% tm/YStack
    {:flex 1
     :gap "$2"}
    (:? (data/not-empty? entries)
        [:% tm/Button
         {:size "$2"
          :alignSelf "flex-start"
          :onPress (fn []
                     (when (. control setShowCreate)
                       (. control (setShowCreate true))))}
         "ADD"]
        nil)
    (r/% Table props)]))

(defn.js SheetHeader
  "renders sheet column headings with Tamagui primitives"
  {:added "4.1"}
  [props]
  (var #{[(:= impl {})
          style]} props)
  (var #{[columns]} impl)
  (:= columns (or columns []))
  (return
   [:% tm/XStack
    #{(:.. (Object.assign {:gap "$2"
                           :paddingHorizontal "$2"
                           :paddingVertical "$2"
                           :borderBottomWidth 1
                           :borderColor "$borderColor"}
                          (:? style {:style style} {})))}
    (j/map columns
           (fn [column i]
             (return
              [:% tm/Text
               {:key i
                :flex 1
                :fontWeight "700"
                :color "$colorSecondary"}
               (or (. column label)
                   (. column name)
                   (. column key))])))]))

(defn.js SheetRow
  "renders a sheet row with Entry cells or direct text cells"
  {:added "4.1"}
  [props]
  (var #{[(:= entry {})
          (:= impl {})
          (:= custom {})
          style]} props)
  (var #{[columns]} impl)
  (:= columns (or columns []))
  (return
   [:% tm/XStack
    #{(:.. (Object.assign {:gap "$2"
                           :paddingHorizontal "$2"
                           :paddingVertical "$2"
                           :borderBottomWidth 1
                           :borderColor "$borderColor"}
                          (:? style {:style style} {})))}
    (j/map columns
           (fn [column i]
             (var cellProps
                  (Object.assign {}
                                 props
                                 {:entry entry
                                  :key i
                                  :impl column}
                                 (or (data/get-in custom [(. column key)])
                                     {})))
             (return
              [:% tm/YStack
               {:key i
                :flex 1}
               (:? (. column type)
                   (r/% Entry cellProps)
                   [:% tm/Text
                    {:color "$color"}
                    (entryValue entry column props)])])))]))

(defn.js SheetBasic
  "renders a basic sheet with direct Tamagui primitives"
  {:added "4.1"}
  [props]
  (var #{[(:= entries [])
          impl]} props)
  (return
   [:% tm/YStack
    {:flex 1}
    (r/% SheetHeader props)
    [:% tm/ScrollView
     {:flex 1}
     (j/map entries
            (fn:> [entry i]
              (return
               (r/% SheetRow
                    (Object.assign {}
                                   props
                                   {:key (or (. entry id) i)
                                    :entry entry})))))
    ] ]))

(defn.js Sheet
  "renders a basic sheet without legacy UI wrappers"
  {:added "4.1"}
  [props]
  (return (r/% SheetBasic props)))

(defn.js createEntry
  "creates a Tamagui entry element"
  {:added "4.1"}
  [props ...args]
  (return (r/% Entry props ...args)))

(defn.js entry
  "creates a Tamagui entry element with implementation options"
  {:added "4.1"}
  [props impl opts]
  (return
   (r/% Entry
       (Object.assign {}
                      props
                      #{impl}
                      (or opts {})))))

(def.js useLocalPrimitives slim-core/useLocalPrimitives)
(def.js useRoutePrimitives slim-core/useRoutePrimitives)
(def.js useListControl slim-core/useListControl)
(def.js useRouteControl slim-core/useRouteControl)
(def.js useLocalControl slim-core/useLocalControl)
(def.js getParentProps slim-core/getParentProps)
(def.js useParentControl slim-core/useParentControl)

(def.js MODULE (!:module))
