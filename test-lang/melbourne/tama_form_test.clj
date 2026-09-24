(ns melbourne.tama-form-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [std.lib :as h]))

(def test-melbourne_tama_form_display true)

(l/script :js
  {:runtime :websocket
   :config {:id :test/tama-form
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react.ext-form :as ext-form]
             [js.react-native :as n]
             [js.tamagui :as tm]
             [melbourne.tama-form :as tama-form]]
   :export [MODULE]})

^{:refer melbourne.tama-form/FormEnclosed :added "4.1"}
(fact "uses a direct Tamagui theme and field layout"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormEnclosed)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Theme") => true
    (string/includes? form "T.YStack") => true
    (contains? deps 'js.tamagui/Theme) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %)) deps) => true))

^{:refer melbourne.tama-form/FormReadOnly :added "4.1"}
(fact "renders read-only values without a legacy text primitive"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormReadOnly)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Text") => true
    (contains? deps 'js.tamagui/Text) => true
    (string/includes? form "template-entry") => true))

^{:refer melbourne.tama-form/FormInput :added "4.1"}
(fact "binds a Slim field directly to Tamagui Input"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormInput)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Input") => true
    (string/includes? form "validate-field") => true
    (contains? deps 'js.tamagui/Input) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %)) deps) => true))

^{:refer melbourne.tama-form/FormInputXL :added "4.1"}
(fact "keeps the larger input as the same field contract"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormInputXL)
        form (pr-str (:form entry))]
    (string/includes? form "T.Input") => true
    (string/includes? form "\"$5\"") => true
    (string/includes? form "set-field") => true))

^{:refer melbourne.tama-form/FormTextArea :added "4.1"}
(fact "uses the same input seam for multiline values"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormTextArea)
        form (pr-str (:form entry))]
    (string/includes? form "FormInput") => true
    (string/includes? form "multiline") => true
    (string/includes? form "textAlignVertical") => true))

^{:refer melbourne.tama-form/FormCheckBox :added "4.1"}
(fact "binds a boolean field to Tamagui Checkbox"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormCheckBox)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Checkbox") => true
    (string/includes? form "T.Checkbox.Indicator") => true
    (contains? deps 'js.tamagui/Checkbox) => true
    (string/includes? form "onCheckedChange") => true))

^{:refer melbourne.tama-form/FormToggleButton :added "4.1"}
(fact "binds a boolean field to a Tamagui Button"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormToggleButton)
        form (pr-str (:form entry))]
    (string/includes? form "T.Button") => true
    (string/includes? form "toggle-field") => true
    (string/includes? form "active") => true))

^{:refer melbourne.tama-form/FormToggleSwitch :added "4.1"}
(fact "binds a boolean field to Tamagui Switch"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormToggleSwitch)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Switch") => true
    (string/includes? form "T.SwitchThumb") => true
    (contains? deps 'js.tamagui/Switch) => true))

^{:refer melbourne.tama-form/FormEnumSingle :added "4.1"}
(fact "renders Slim options with the Tamagui select primitives"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormEnumSingle)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Select") => true
    (string/includes? form "T.Select.Item") => true
    (contains? deps 'js.tamagui/Select) => true
    (string/includes? form "onValueChange") => true))

^{:refer melbourne.tama-form/FormEnumMulti :added "4.1"}
(fact "renders multi-choice options as direct Tamagui buttons"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormEnumMulti)
        form (pr-str (:form entry))]
    (string/includes? form "T.Button") => true
    (string/includes? form "includes") => true
    (string/includes? form "concat") => true))

^{:refer melbourne.tama-form/FormColorInput :added "4.1"}
(fact "keeps color input web-native and form-controlled"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormColorInput)
        form (pr-str (:form entry))]
    (string/includes? form "T.Input") => true
    (string/includes? form "\"color\"") => true
    (string/includes? form "set-field") => true))

^{:refer melbourne.tama-form/FormChipInput :added "4.1"}
(fact "keeps chip editing inside the direct Tamagui form layer"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormChipInput)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "T.Input") => true
    (string/includes? form "T.Button") => true
    (string/includes? form "chip_input") => true
    (contains? deps 'js.tamagui/Input) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %)) deps) => true))

^{:refer melbourne.tama-form/FormLayout :added "4.1"}
(fact "creates rows without adding another component stack"
  (let [entry (l/sym-entry :js 'melbourne.tama-form/FormLayout)
        form (pr-str (:form entry))
        deps (:deps-fragment entry)]
    (string/includes? form "createElement") => true
    (string/includes? form "T.YStack") => true
    (contains? deps 'js.tamagui/YStack) => true
    (not-any? #(re-find #"^(melbourne\\.ui-|js\\.react-native/)" (str %)) deps) => true))
  
^{:id test-melbourne_tama_form_display}
(fact "displays the shared form contract in light and dark themes"
  ^:hidden
  (defn.js TamaFormDemo
    []
    (var form (ext-form/makeForm
               (fn:> {:name "Tama"
                      :about "Direct primitives"
                      :agree true
                      :enabled false
                      :currency "XLM"
                      :currencies ["XLM"]
                      :color "#2563eb"
                      :tags ["direct" "tamagui"]})
               {:name []
                :about []
                :agree []
                :enabled []
                :currency []
                :currencies []
                :color []
                :tags []}))
    (return
     (n/EnclosedCode
      {:label "melbourne.tama-form"}
      [:% tm/XStack
       {:gap "$4"
        :flexWrap "wrap"
        :alignItems "flex-start"}
       [:% tm/YStack
        {:flex 1
         :minWidth 260
         :gap "$3"}
        [:% tama-form/FormReadOnly
         {:design {:type "light"
                   :color "blue"}
          :label "Read only"
          :entry {:name "Tama"}
          :template ["name"]}]
        [:% tama-form/FormInput
         {:design {:type "light"
                   :color "blue"}
          :label "Name"
          :form form
          :field "name"}]
        [:% tama-form/FormInputXL
         {:design {:type "light"
                   :color "blue"}
          :label "Large input"
          :form form
          :field "name"}]
        [:% tama-form/FormTextArea
         {:design {:type "light"
                   :color "blue"}
          :label "About"
          :form form
          :field "about"}]
        [:% tama-form/FormCheckBox
         {:design {:type "light"
                   :color "blue"}
          :label "I agree"
          :form form
          :field "agree"}]
        [:% tama-form/FormToggleButton
         {:design {:type "light"
                   :color "blue"}
          :label "Toggle button"
          :text "Enabled"
          :form form
          :field "agree"}]]
       [:% tm/YStack
        {:flex 1
         :minWidth 260
         :gap "$3"}
        [:% tama-form/FormInput
         {:design {:type "dark"
                   :color "green"}
          :label "Name"
          :form form
          :field "name"
          :fieldProps {:placeholder "Dark theme"} }]
        [:% tama-form/FormToggleSwitch
         {:design {:type "dark"
                   :color "green"}
          :label "Notifications"
          :form form
          :field "enabled"}]
        [:% tama-form/FormEnumSingle
         {:design {:type "dark"
                   :color "green"}
          :label "Currency"
          :form form
          :field "currency"
          :options ["XLM" "USD" "STATS"]}]
        [:% tama-form/FormEnumMulti
         {:design {:type "dark"
                   :color "green"}
          :label "Currencies"
          :form form
          :field "currencies"
          :options ["XLM" "USD" "STATS"]}]
        [:% tama-form/FormColorInput
         {:design {:type "dark"
                   :color "green"}
          :label "Accent"
          :form form
          :field "color"}]
        [:% tama-form/FormChipInput
         {:design {:type "dark"
                   :color "green"}
          :label "Tags"
          :form form
          :field "tags"}]
        [:% tama-form/FormLayout
         {:design {:type "dark"
                   :color "green"}
          :form form
          :rows [{:component tama-form/FormInput
                  :field "name"
                  :label "Layout row"}]}]]]))))
 
(def.js MODULE (!:module))
