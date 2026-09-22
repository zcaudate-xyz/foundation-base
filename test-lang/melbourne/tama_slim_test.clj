(ns melbourne.tama-slim-test
  (:use code.test)
  (:require [lang.core :as l]
            [lang.core.impl :as impl]))
(l/script :js
  {:runtime :websocket
   :config {:id :test/repl-tama-menu}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [js.tamagui :as tm]
             [melbourne.tama :as tama]]
   :export [MODULE]})

(defn.js TamaMenuSection
  [props]
  (var #{[eyebrow title children]} props)
  (return
   [:% tm/Card
    {:width "100%"
     :padding 22
     :borderRadius 18
     :borderWidth 1
     :borderColor "#dbeafe"
     :backgroundColor "#ffffff"
     :gap 14}
    [:% tm/YStack
     {:gap 4}
     [:% tm/Text
      {:fontSize 10
       :fontWeight "800"
       :letterSpacing 1
       :color "#2563eb"}
      eyebrow]
     [:% tm/H2
      {:fontSize 20
       :fontWeight "800"
       :color "#0f172a"}
      title]]
    children]))

(defn.js TamaMenuScreen
  [props]
  (var #{[label children]} props)
  (return
   [:% tm/YStack
    {:width "100%"
     :maxWidth 860
     :alignSelf "center"
     :minWidth 0}
    (n/EnclosedCode
     {:label label}
     [:% tm/YStack
      {:width "100%"
       :gap 16}
      children])]))

(defn.js TamaSlimCommonDemo
  []
  (var [name setName] (r/local "Ava"))
  (var [agreed setAgreed] (r/local false))
  (var [enabled setEnabled] (r/local true))
  (var [currency setCurrency] (r/local "XLM"))
  (var [multi setMulti] (r/local ["XLM" "STATS"]))
  (var [color setColor] (r/local "#456789"))
  (var [tagText setTagText] (r/local ""))
  (var [selectedTags setSelectedTags] (r/local ["football" "sport"]))
  (var toggleMulti
    (fn [item]
      (setMulti
       (:? (. multi (includes item))
           (. multi (filter (fn [value] (not (== value item)))))
           (. multi (concat [item]))))))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-common"}
    [:% -/TamaMenuSection
     {:eyebrow "FORM ENCLOSED"
      :title "Light and dark surfaces"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 12}
      [:% tm/Card
       {:flex 1
        :minWidth 220
        :padding "$3"
        :backgroundColor "#f1f5f9"
        :gap "$2"}
       [:% tm/Text
        {:fontSize 11
         :fontWeight "800"
         :color "#475569"}
        "HELLO"]
       [:% tm/Text
        {:color "#334155"}
        "WORLD"]]
      [:% tm/Card
       {:flex 1
        :minWidth 220
        :padding "$3"
        :backgroundColor "#1e293b"
        :gap "$2"}
       [:% tm/Text
        {:fontSize 11
         :fontWeight "800"
         :color "#cbd5e1"}
        "HELLO"]
       [:% tm/Text
        {:color "#f8fafc"}
        "WORLD"]]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM READ ONLY"
      :title "Present a value without editing it"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 12}
      [:% tm/YStack
       {:flex 1
        :minWidth 220
        :gap 6}
       [:% tm/Label "Name"]
       [:% tm/Text
        {:padding "$2"
         :borderWidth 1
         :borderColor "$borderColor"
         :borderRadius "$2"
         :color "$color11"}
        "abc"]]
      [:% tm/YStack
       {:flex 1
        :minWidth 220
        :gap 6}
       [:% tm/Label
        {:color "$color11"}
        "Name"]
       [:% tm/Text
        {:padding "$2"
         :borderWidth 1
         :borderColor "#475569"
         :borderRadius "$2"
         :backgroundColor "#1e293b"
         :color "#f8fafc"}
        "abc"]]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM INPUT"
      :title "Edit a value and surface its validation action"}
     [:% tm/YStack
      {:gap 10}
      [:% tm/XStack
       {:flexWrap "wrap"
        :gap 12}
       [:% tm/YStack
        {:flex 1
         :minWidth 220
         :gap 6}
        [:% tm/Label "Name"]
        [:% tm/Input
         {:value name
          :onChangeText setName
          :placeholder "abc"}]]
       [:% tm/YStack
        {:flex 1
         :minWidth 220
         :gap 6}
        [:% tm/Label
         {:color "#cbd5e1"}
         "Name (quiet validation)"]
        [:% tm/Input
         {:value name
          :onChangeText setName
          :backgroundColor "#1e293b"
          :color "#f8fafc"
          :borderColor "#475569"}]]]
      [:% tm/XStack
       {:gap 8}
       [:% tm/Button
        {:size 2
         :onPress (fn [] (setName "Validated"))}
        "Validate"]
       [:% tm/Text
        {:alignSelf "center"
         :color "$color11"}
        "Value: " name]]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM INPUT XL"
      :title "Give an important field more room"}
     [:% tm/YStack
      {:gap 6
       :maxWidth 520}
      [:% tm/Label
       {:fontSize 15
        :fontWeight "700"}
       "Name"]
      [:% tm/Input
       {:size 5
        :value name
        :onChangeText setName
        :placeholder "A larger entry field"}]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM TEXT AREA"
      :title "Capture longer-form context"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 12}
      [:% tm/TextArea
       {:flex 1
        :minWidth 220
        :minHeight 110
        :placeholder "Write something useful..."}]
      [:% tm/TextArea
       {:flex 1
        :minWidth 220
        :minHeight 110
        :backgroundColor "#1e293b"
        :color "#f8fafc"
        :borderColor "#475569"
        :defaultValue "A dark text area"}]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM CHECKBOX"
      :title "Make agreement explicit"}
     [:% tm/XStack
      {:alignItems "center"
       :gap 10}
      [:% tm/Checkbox
       {:checked agreed
        :onCheckedChange setAgreed}
       [:% tm/CheckboxIndicator
        [:% tm/Text
         {:fontWeight "900"
          :color "#2563eb"}
         "✓"]]]
      [:% tm/Text
       {:fontWeight "700"}
       "I agree to terms and conditions"]]
     [:% tm/Text
      {:fontSize 12
       :color "$color11"}
      (:? agreed "Agreement recorded" "Agreement required")]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM TOGGLE BUTTON"
      :title "Use a button when the state is part of the action"}
     [:% tm/XStack
      {:gap 10
       :flexWrap "wrap"}
      [:% tm/Button
       {:size 3
        :backgroundColor (:? agreed "#2563eb" "#e2e8f0")
        :color (:? agreed "#ffffff" "#0f172a")
        :onPress (fn [] (setAgreed (not agreed)))}
       (:? agreed "I AGREE TO TERMS" "AGREE")]
      [:% tm/Button
       {:size 3
        :chromeless true
        :onPress (fn [] (setAgreed false))}
       "Clear"]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM TOGGLE SWITCH"
      :title "Keep an ongoing preference visible"}
     [:% tm/XStack
      {:alignItems "center"
       :gap 12}
      [:% tm/Switch
       {:checked enabled
        :onCheckedChange setEnabled}
       [:% tm/SwitchThumb]]
      [:% tm/Text
       {:fontWeight "700"}
       (:? enabled "Enabled" "Disabled")]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "Notifications"]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM ENUM SINGLE"
      :title "Choose one currency"}
     [:% tm/Select
      {:value currency
       :onValueChange setCurrency}
      [:% tm/SelectTrigger
       {:width "100%"
        :maxWidth 360}
       [:% tm/SelectValue]]
      [:% tm/SelectContent
       {:zIndex 200000}
       [:% tm/SelectViewport
        {:minWidth 280}
        [:% tm/SelectItem
         {:index 0
          :value "XLM"}
         [:% tm/SelectItemText "XLM"]]
        [:% tm/SelectItem
         {:index 1
          :value "USD"}
         [:% tm/SelectItemText "USD"]]
        [:% tm/SelectItem
         {:index 2
          :value "STATS"}
         [:% tm/SelectItemText "STATS"]]]]]
     [:% tm/Text
      {:fontSize 12
       :color "$color11"}
      "Selected: " currency]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM ENUM MULTI"
      :title "Select several currencies"}
     [:% tm/XStack
      {:gap 8
       :flexWrap "wrap"}
      [:% tm/Button
       {:size 2
        :chromeless (not (. multi (includes "XLM")))
        :onPress (fn [] (toggleMulti "XLM"))}
       "XLM"]
      [:% tm/Button
       {:size 2
        :chromeless (not (. multi (includes "USD")))
        :onPress (fn [] (toggleMulti "USD"))}
       "USD"]
      [:% tm/Button
       {:size 2
        :chromeless (not (. multi (includes "STATS")))
        :onPress (fn [] (toggleMulti "STATS"))}
       "STATS"]]
     [:% tm/Text
      {:fontSize 12
       :color "$color11"}
      "Selected: " (. multi (join ", "))]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM COLOR INPUT"
      :title "Choose a color with an immediate preview"}
     [:% tm/XStack
      {:alignItems "center"
       :gap 12}
      [:% tm/Input
       {:type "color"
        :value color
        :onChangeText setColor
        :width 90}]
      [:% tm/Card
       {:width 42
        :height 42
        :backgroundColor color
        :borderRadius 8}]
      [:% tm/Text
       {:fontFamily "monospace"}
       color]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM CHIP INPUT"
      :title "Keep tags readable as they accumulate"}
     [:% tm/YStack
      {:gap 10}
      [:% tm/XStack
       {:gap 8
        :flexWrap "wrap"}
       [:% tm/Button
        {:size 2
         :onPress (fn [] (setSelectedTags []))}
        "football"]
       [:% tm/Button
        {:size 2
         :onPress (fn [] (setSelectedTags []))}
        "sport"]]
      [:% tm/Input
       {:value tagText
        :onChangeText setTagText
        :placeholder "Add a tag"}]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "Selected: " (. selectedTags (join ", "))]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM LAYOUT"
      :title "Compose the controls into one coherent form"}
     [:% tm/Card
      {:padding "$4"
       :gap 12
       :maxWidth 620}
      [:% tm/YStack
       {:gap 6}
       [:% tm/Label "Currency"]
       [:% tm/Text
        {:color "$color11"}
        (. multi (join ", "))]]
      [:% tm/YStack
       {:gap 6}
       [:% tm/Label "Name"]
       [:% tm/Input
        {:value name
         :onChangeText setName}]]
      [:% tm/YStack
       {:gap 6}
       [:% tm/Label "About"]
       [:% tm/TextArea
        {:minHeight 80
         :placeholder "Tell us about yourself"}]]]]]))

(defn.js TamaSlimNumberDemo
  []
  (var [amount setAmount] (r/local 50))
  (var onValueChange
    (fn [values]
      (setAmount (. values (at 0)))))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-number"}
    [:% -/TamaMenuSection
     {:eyebrow "FORM SPINNER"
      :title "Nudge a value with predictable steps"}
     [:% tm/YStack
      {:gap 12
       :maxWidth 520}
      [:% tm/XStack
       {:alignItems "center"
        :justifyContent "space-between"}
       [:% tm/Text
        {:fontWeight "700"}
        "Price"]
       [:% tm/Text
        {:fontSize 24
         :fontWeight "800"
         :color "#2563eb"}
        amount]]
      [:% tm/XStack
       {:gap 8}
       [:% tm/Button
        {:size 3
         :onPress (fn [] (setAmount (- amount 2)))}
        "− 2"]
       [:% tm/Button
        {:size 3
         :onPress (fn [] (setAmount (+ amount 2)))}
        "+ 2"]]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "Step 2 · range 0–100"]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM SLIDER"
      :title "Tune a value continuously"}
     [:% tm/YStack
      {:gap 16
       :maxWidth 520}
      [:% tm/XStack
       {:alignItems "center"
        :justifyContent "space-between"}
       [:% tm/Text
        {:fontWeight "700"}
        "Price"]
       [:% tm/Text
        {:fontSize 24
         :fontWeight "800"
         :color "#2563eb"}
        amount]]
      [:% tm/Slider
       {:size 3
        :value (. Array (of amount))
        :min 0
        :max 100
        :step 1
        :onValueChange onValueChange}
       [:% tm/SliderTrack
        {:backgroundColor "#dbeafe"}
        [:% (. tm/Slider TrackActive)
         {:backgroundColor "#2563eb"}]]
       [:% tm/SliderThumb
        {:index 0
         :circular true
         :backgroundColor "#ffffff"
         :borderColor "#2563eb"}]]
      [:% tm/XStack
       {:gap 8}
       [:% tm/Button
        {:size 3
         :onPress (fn [] (setAmount (- amount 5)))}
        "− 5"]
       [:% tm/Button
        {:size 3
         :onPress (fn [] (setAmount (+ amount 5)))}
        "+ 5"]]]]]))

(defn.js TamaSlimSelectDemo
  []
  (var [value setValue] (r/local "Victoria"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-select"}
    [:% -/TamaMenuSection
     {:eyebrow "FORM PICKER"
      :title "Keep the most common choices within reach"}
     [:% tm/YStack
      {:gap 10
       :maxWidth 520}
      [:% tm/Text
       {:fontWeight "700"}
       "Price"]
      [:% tm/XStack
       {:gap 8
        :flexWrap "wrap"}
       [:% tm/Button
        {:size 3
         :chromeless (not (== value "Victoria"))
         :onPress (fn [] (setValue "Victoria"))}
        "Victoria"]
       [:% tm/Button
        {:size 3
         :chromeless (not (== value "Queensland"))
         :onPress (fn [] (setValue "Queensland"))}
        "Queensland"]
       [:% tm/Button
        {:size 3
         :chromeless (not (== value "Tasmania"))
         :onPress (fn [] (setValue "Tasmania"))}
        "Tasmania"]]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "Selected: " value]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM DROPDOWN"
      :title "Choose from a compact, keyboard-friendly list"}
     [:% tm/YStack
      {:gap 10
       :maxWidth 360}
      [:% tm/Text
       {:fontWeight "700"}
       "State"]
      [:% tm/Select
       {:value value
        :onValueChange setValue}
       [:% tm/SelectTrigger
        {:width "100%"
         :borderRadius 10}
        [:% tm/SelectValue
         {:placeholder "Choose a state"}]]
       [:% tm/SelectContent
        {:zIndex 200000}
        [:% tm/SelectViewport
         {:minWidth 280}
         [:% tm/SelectGroup
          [:% tm/SelectLabel "Australian states"]
          [:% tm/SelectItem
           {:index 0
            :value "Victoria"}
           [:% tm/SelectItemText "Victoria"]]
          [:% tm/SelectItem
           {:index 1
            :value "Queensland"}
           [:% tm/SelectItemText "Queensland"]]
          [:% tm/SelectItem
           {:index 2
            :value "Tasmania"}
           [:% tm/SelectItemText "Tasmania"]]
          [:% tm/SelectItem
           {:index 3
            :value "Western Australia"}
           [:% tm/SelectItemText "Western Australia"]]]]]]
      [:% tm/Text
       {:color "#64748b"
        :fontSize 12}
       "Selected: " value]]]]))

(defn.js TamaSlimImageDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-image"}
    [:% -/TamaMenuSection
     {:eyebrow "IMAGE"
      :title "Give media a clear frame and a useful fallback"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 18
       :alignItems "center"}
      [:% tm/Image
       {:width 260
        :height 160
        :borderRadius 16
        :objectFit "cover"
        :src "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=700&q=80"}]
      [:% tm/Avatar
       {:circular true
        :size 96}
       [:% tm/AvatarImage
        {:src "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80"}]
       [:% tm/AvatarFallback
        [:% tm/Text
         {:fontWeight "800"}
         "AV"]]]]]]))

(defn.js TamaSlimLinkDemo
  []
  (var [message setMessage] (r/local "Choose a link to preview its destination."))
  (var [account setAccount] (r/local "Account 1"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-link"}
    [:% -/TamaMenuSection
     {:eyebrow "FORM LINK DROPDOWN"
      :title "Choose a related record from a lookup"}
     [:% tm/YStack
      {:gap 12
       :maxWidth 520}
      [:% tm/Label "Account"]
      [:% tm/Select
       {:value account
        :onValueChange (fn [next]
                         (setAccount next)
                         (setMessage (concat "Selected " next)))}
       [:% tm/SelectTrigger
        {:width "100%"}
        [:% tm/SelectValue]]
       [:% tm/SelectContent
        {:zIndex 200000}
        [:% tm/SelectViewport
         [:% tm/SelectItem
          {:index 0
           :value "Account 1"}
          [:% tm/SelectItemText "Account 1"]]
         [:% tm/SelectItem
          {:index 1
           :value "Account 2"}
          [:% tm/SelectItemText "Account 2"]]
         [:% tm/SelectItem
          {:index 2
           :value "Account 3"}
          [:% tm/SelectItemText "Account 3"]]]]]
      [:% tm/Text
       {:color "#64748b"
        :fontSize 12}
       message]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM LINK READ ONLY"
      :title "Show a linked record without opening an editor"}
     [:% tm/YStack
      {:gap 6
       :maxWidth 520}
      [:% tm/Label "Account"]
      [:% tm/Text
       {:padding "$2"
        :borderWidth 1
        :borderColor "$borderColor"
        :borderRadius "$2"
        :color "$color11"}
       "Account 2 · id-2"]]]
    [:% -/TamaMenuSection
     {:eyebrow "FORM LINK ENTRY READ ONLY"
      :title "Keep linked entry context visible"}
     [:% tm/Card
      {:padding "$3"
       :gap 8
       :maxWidth 520}
      [:% tm/Text
       {:fontWeight "800"}
       "Account 3"]
      [:% tm/Text
       {:color "$color11"}
       "id-3 · balance 42"]
      [:% tm/Anchor
       {:href "#account-3"
        :color "#2563eb"
        :onPress (fn [] (setMessage "Account 3 opened"))}
       "Open account"]]]]))

(defn.js TamaSlimErrorDemo
  []
  (var [retried setRetried] (r/local false))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-error"}
    [:% -/TamaMenuSection
     {:eyebrow "ERROR"
      :title "Explain a failure without losing the user"}
     [:% tm/Card
      {:padding 16
       :borderRadius 14
       :borderWidth 1
       :borderColor "#fecaca"
       :backgroundColor "#fff1f2"
       :gap 8}
      [:% tm/Text
       {:fontWeight "800"
        :color "#be123c"}
       "Something needs attention"]
      [:% tm/Text
       {:color "#9f1239"}
       "We could not load the latest component metadata."]
      [:% tm/Button
       {:size 3
        :alignSelf "flex-start"
        :onPress (fn [] (setRetried true))}
       (:? retried "Retry queued" "Try again")]]]]))

(defn.js TamaSlimSubmitDemo
  []
  (var [status setStatus] (r/local "Ready to submit"))
  (var [waiting setWaiting] (r/local false))
  (var [errored setErrored] (r/local false))
  (var [email setEmail] (r/local "a@a.com"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-submit"}
    [:% -/TamaMenuSection
     {:eyebrow "USE SUBMIT"
      :title "Expose waiting and result state"}
     [:% tm/XStack
      {:gap 8
       :flexWrap "wrap"
       :alignItems "center"}
      [:% tm/Button
       {:size 3
        :disabled waiting
        :onPress (fn []
                   (setWaiting true)
                   (setStatus "Action complete"))}
       (:? waiting "Working..." "Action")]
      [:% tm/Button
       {:size 3
        :chromeless true
        :onPress (fn []
                   (setWaiting false)
                   (setStatus "Cleared"))}
       "Clear"]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       status]]]
    [:% -/TamaMenuSection
     {:eyebrow "SUBMIT BUTTON"
      :title "Make normal, waiting, and error states obvious"}
     [:% tm/XStack
      {:gap 8
       :flexWrap "wrap"}
      [:% tm/Button
       {:size 3
        :onPress (fn [] (setErrored false))}
       "HELLO"]
      [:% tm/Button
       {:size 3
        :disabled true}
       "WAITING"]
      [:% tm/Button
       {:size 3
        :backgroundColor (:? errored "#be123c" "#2563eb")
        :onPress (fn [] (setErrored (not errored)))}
       (:? errored "CHANGE FAILED" "CHANGE PASSWORD")]]]
    [:% -/TamaMenuSection
     {:eyebrow "SUBMIT LINE"
      :title "Keep the primary action beside its reset"}
     [:% tm/XStack
      {:gap 8}
      [:% tm/Button
       {:size 3
        :onPress (fn [] (setStatus "Submitted"))}
       "Submit"]
      [:% tm/Button
       {:size 3
        :chromeless true
        :onPress (fn [] (setStatus "Reset"))}
       "Reset"]]]
    [:% -/TamaMenuSection
     {:eyebrow "SUBMIT LINE ACTIONS"
      :title "Give destructive and secondary actions a clear home"}
     [:% tm/XStack
      {:gap 8
       :flexWrap "wrap"}
      [:% tm/Button
       {:size 3
        :onPress (fn [] (setStatus "Saved"))}
       "Save"]
      [:% tm/Button
       {:size 3
        :chromeless true
        :onPress (fn [] (setStatus "Cancelled"))}
       "Cancel"]
      [:% tm/Button
       {:size 3
        :chromeless true
        :onPress (fn [] (setStatus "Cleared"))}
       "Clear"]]]
    [:% -/TamaMenuSection
     {:eyebrow "USE SUBMIT FIELD"
      :title "Validate a field before it reaches the action"}
     [:% tm/YStack
      {:gap 8
       :maxWidth 520}
      [:% tm/Label "Email"]
      [:% tm/Input
       {:value email
        :onChangeText setEmail
        :keyboardType "email-address"}]
      [:% tm/Text
       {:fontSize 12
        :color (:? (== email "") "#be123c" "$color11")}
       (:? (== email "") "Email is required" "Ready to validate")]]]
    [:% -/TamaMenuSection
     {:eyebrow "USE SUBMIT FORM"
      :title "Bring field, action, and result together"}
     [:% tm/Card
      {:padding "$4"
       :gap 10
       :maxWidth 560}
      [:% tm/Input
       {:value email
        :onChangeText setEmail
        :placeholder "Email"}]
      [:% tm/Button
       {:size 3
        :onPress (fn [] (setStatus (concat "Submitted " email)))}
       "Submit form"]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       status]]]]))

(defn.js TamaSlimDialogDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-dialog"}
    [:% -/TamaMenuSection
     {:eyebrow "DIALOG"
      :title "Keep confirmation close to the action"}
     [:% tm/Dialog
      {:modal true}
      [:% tm/DialogTrigger
       {:asChild true}
       [:% tm/Button
        {:size 4}
        "Review changes"]]
      [:% tm/DialogPortal
       [:% tm/DialogOverlay
        {:backgroundColor "rgba(15,23,42,0.45)"}]
       [:% tm/DialogContent
        {:borderRadius 18
         :padding 22
         :gap 14
         :minWidth 320}
        [:% tm/DialogTitle
         {:fontSize 20
          :fontWeight "800"}
         "Ready to publish?"]
        [:% tm/DialogDescription
         {:color "#64748b"}
         "This direct Tamagui dialog keeps the decision focused."]
        [:% tm/XStack
         {:justifyContent "flex-end"
          :gap 8}
         [:% tm/DialogClose
          {:asChild true}
          [:% tm/Button
           {:size 3
            :chromeless true}
           "Cancel"]]
         [:% tm/DialogClose
          {:asChild true}
          [:% tm/Button
           {:size 3}
           "Publish"]]]]]]]]))

(defn.js TamaSlimEntryDemo
  []
  (var entry {:currency "STATS"
              :balance "1000"
              :escrow "50.5"
              :description "A reusable record"
              :name "Ava"
              :avatar "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80"
              :label "Currency"
              :formState "Ready"
              :debug "Debug output"})
  (var samples
    [     {:id "entry-free"
      :eyebrow "ENTRY FREE"
      :title "Render a free entry surface"
      :entry entry
      :impl {:type "card" :body [{:type "title" :template ["currency"]} {:type "p" :template ["description"]}]}}
     {:id "entry-content-raw"
      :eyebrow "ENTRY CONTENT RAW"
      :title "Show raw record content"
      :entry entry
      :impl {:type "raw" :template ["description"]}}
     {:id "entry-layout-horizontal"
      :eyebrow "ENTRY LAYOUT HORIZONTAL"
      :title "Lay content out side by side"
      :entry entry
      :impl {:type "h" :body [{:type "title" :template ["currency"]} {:type "p" :template ["balance"]}]}}
     {:id "entry-layout-vertical"
      :eyebrow "ENTRY LAYOUT VERTICAL"
      :title "Stack content vertically"
      :entry entry
      :impl {:type "v" :body [{:type "title" :template ["currency"]} {:type "p" :template ["balance"]}]}}
     {:id "entry-layout-enclosed"
      :eyebrow "ENTRY LAYOUT ENCLOSED"
      :title "Give an entry a labeled boundary"
      :entry (assoc entry :layout "ENCLOSED")
      :impl {:type "card" :body [{:type "title" :template ["layout"]} {:type "p" :template ["description"]}]}}
     {:id "entry-layout-portal"
      :eyebrow "ENTRY LAYOUT PORTAL"
      :title "Keep portal content visually grouped"
      :entry (assoc entry :layout "PORTAL")
      :impl {:type "card" :body [{:type "title" :template ["layout"]} {:type "p" :template ["description"]}]}}
     {:id "entry-layout-portal-sink"
      :eyebrow "ENTRY LAYOUT PORTAL SINK"
      :title "Pair a portal with its sink"
      :entry (assoc entry :layout "PORTAL SINK")
      :impl {:type "card" :body [{:type "title" :template ["layout"]} {:type "p" :template ["description"]}]}}
     {:id "entry-layout-debug"
      :eyebrow "ENTRY LAYOUT DEBUG"
      :title "Make diagnostic content visible"
      :entry entry
      :impl {:type "raw" :template ["debug"]}}
     {:id "entry-layout-form-fade"
      :eyebrow "ENTRY LAYOUT FORM FADE"
      :title "Toggle a field with a soft transition"
      :entry entry
      :impl {:type "control" :text "TOGGLE FADE" :onPress (fn [])}}
     {:id "entry-layout-form-fold"
      :eyebrow "ENTRY LAYOUT FORM FOLD"
      :title "Toggle a field with a folded layout"
      :entry entry
      :impl {:type "control" :text "TOGGLE FOLD" :onPress (fn [])}}
     {:id "entry-content-title-h1"
      :eyebrow "ENTRY CONTENT TITLE H1"
      :title "Render the largest heading"
      :entry entry
      :impl {:type "title-h1" :template ["currency"]}}
     {:id "entry-content-title-h2"
      :eyebrow "ENTRY CONTENT TITLE H2"
      :title "Render a second-level heading"
      :entry entry
      :impl {:type "title-h2" :template ["currency"]}}
     {:id "entry-content-title-h3"
      :eyebrow "ENTRY CONTENT TITLE H3"
      :title "Render a third-level heading"
      :entry entry
      :impl {:type "title-h3" :template ["currency"]}}
     {:id "entry-content-title-h4"
      :eyebrow "ENTRY CONTENT TITLE H4"
      :title "Render a fourth-level heading"
      :entry entry
      :impl {:type "title-h4" :template ["currency"]}}
     {:id "entry-content-title-h5"
      :eyebrow "ENTRY CONTENT TITLE H5"
      :title "Render a fifth-level heading"
      :entry entry
      :impl {:type "title-h5" :template ["currency"]}}
     {:id "entry-content-bold"
      :eyebrow "ENTRY CONTENT BOLD"
      :title "Emphasize a value"
      :entry entry
      :impl {:type "bold" :template ["currency"]}}
     {:id "entry-content-raw-repeat"
      :eyebrow "ENTRY CONTENT RAW REPEAT"
      :title "Keep the repeated raw sample from Slim"
      :entry entry
      :impl {:type "raw" :template ["description"]}}
     {:id "entry-content-raw-form"
      :eyebrow "ENTRY CONTENT RAW FORM"
      :title "Show a form-backed raw value"
      :entry entry
      :impl {:type "raw" :template ["formState"]}}
     {:id "entry-content-fill"
      :eyebrow "ENTRY CONTENT FILL"
      :title "Fill the remaining horizontal space"
      :entry entry
      :impl {:type "h" :body [{:type "raw" :template ["currency"]} {:type "fill"} {:type "control" :text "OPEN" :onPress (fn [])}]}}
     {:id "entry-content-title"
      :eyebrow "ENTRY CONTENT TITLE"
      :title "Render the compact title style"
      :entry entry
      :impl {:type "title" :template ["currency"]}}
     {:id "entry-content-paragraph"
      :eyebrow "ENTRY CONTENT PARAGRAPH"
      :title "Render paragraph text"
      :entry entry
      :impl {:type "p" :template ["description"]}}
     {:id "entry-content-separator"
      :eyebrow "ENTRY CONTENT SEPARATOR"
      :title "Separate adjacent content"
      :entry entry
      :impl {:type "separator"}}
     {:id "entry-content-icon"
      :eyebrow "ENTRY CONTENT ICON"
      :title "Use a compact icon label"
      :entry (assoc entry :name "home")
      :impl {:type "icon" :template ["name"]}}
     {:id "entry-content-image"
      :eyebrow "ENTRY CONTENT IMAGE"
      :title "Pair an avatar with entry text"
      :entry entry
      :impl {:type "image" :template ["name"] :image {:template ["avatar"]}}}
     {:id "entry-content-pair"
      :eyebrow "ENTRY CONTENT PAIR"
      :title "Align a label and value"
      :entry entry
      :impl {:type "pair" :title {:type "raw" :template ["label"]} :text {:type "raw" :template ["currency"]}}}
     {:id "entry-content-field"
      :eyebrow "ENTRY CONTENT FIELD"
      :title "Expose an editable entry field"
      :entry entry
      :impl {:type "field" :template ["name"]}}
     {:id "entry-content-control"
      :eyebrow "ENTRY CONTENT CONTROL"
      :title "Attach a direct control button"
      :entry entry
      :impl {:type "control" :text "DETAIL" :onPress (fn [])}}
     {:id "entry-layout-control"
      :eyebrow "ENTRY LAYOUT CONTROL"
      :title "Combine content and a control"
      :entry entry
      :impl {:type "h" :body [{:type "raw" :template ["currency"]} {:type "fill"} {:type "control" :text "PRESS" :onPress (fn [])}]}}
     {:id "entry-content-action"
      :eyebrow "ENTRY CONTENT ACTION"
      :title "Expose an entry action"
      :entry entry
      :impl {:type "control" :text "HELLO" :onPress (fn [])}}
     {:id "entry-content-link"
      :eyebrow "ENTRY CONTENT LINK"
      :title "Expose a link-like action"
      :entry entry
      :impl {:type "control" :text "PRESS" :onPress (fn [])}}
     {:id "entry-layout-link"
      :eyebrow "ENTRY LAYOUT LINK"
      :title "Use a link-like layout"
      :entry entry
      :impl {:type "h" :body [{:type "raw" :template ["currency"]} {:type "control" :text "PRESS" :onPress (fn [])}]}}
     {:id "entry-content-route"
      :eyebrow "ENTRY CONTENT ROUTE"
      :title "Expose a route action"
      :entry entry
      :impl {:type "control" :text "DETAIL" :onPress (fn [])}}
     {:id "entry-content-route-toggle"
      :eyebrow "ENTRY CONTENT ROUTE TOGGLE"
      :title "Toggle a route action"
      :entry entry
      :impl {:type "control" :text "TOGGLE DETAIL" :onPress (fn [])}}
     {:id "entry-content-submit"
      :eyebrow "ENTRY CONTENT SUBMIT"
      :title "Submit a record action"
      :entry entry
      :impl {:type "control" :text "CREATE" :onPress (fn [])}}
     {:id "entry-layout-card"
      :eyebrow "ENTRY LAYOUT CARD"
      :title "Render the card layout"
      :entry entry
      :impl {:type "card" :body [{:type "title" :template ["currency"]} {:type "p" :template ["description"]}]}}
     {:id "entry-layout-form"
      :eyebrow "ENTRY LAYOUT FORM"
      :title "Compose fields in a form layout"
      :entry entry
      :impl {:type "v" :body [{:type "field" :template ["name"]} {:type "field" :template ["currency"]}]}}
     {:id "entry"
      :eyebrow "ENTRY"
      :title "Combine the entry vocabulary"
      :entry entry
      :impl {:type "v" :body [{:type "title" :template ["currency"]} {:type "p" :template ["description"]} {:type "separator"} {:type "image" :template ["name"] :image {:template ["avatar"]}}]}}])
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-entry"}
    (. samples
       (map (fn [sample]
              (return
               [:% -/TamaMenuSection
                {:key (. sample id)
                 :eyebrow (. sample eyebrow)
                 :title (. sample title)}
                [:% tama/Entry
                 {:entry (. sample entry)
                  :impl (. sample impl)}]]))))]))


(defn.js TamaSlimPopupDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-popup"}
    [:% -/TamaMenuSection
     {:eyebrow "POPUP"
      :title "Reveal supporting actions on demand"}
     [:% tm/Popover
      [:% tm/PopoverTrigger
       {:asChild true}
       [:% tm/Button
        {:size 4}
        "Open actions"]]
      [:% tm/PopoverContent
       {:borderRadius 14
        :padding 14
        :gap 10
        :elevate true}
       [:% tm/Text
        {:fontWeight "800"}
        "Quick actions"]
       [:% tm/Button
        {:chromeless true
         :justifyContent "flex-start"}
        "Duplicate"]
       [:% tm/Button
        {:chromeless true
         :justifyContent "flex-start"}
        "Archive"]
       [:% tm/PopoverClose
        {:asChild true}
        [:% tm/Button
         {:size 2
          :chromeless true}
         "Done"]]]]]]))

(defn.js TamaSlimSheetDemo
  []
  (var [page setPage] (r/local 1))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-sheet"}
    [:% -/TamaMenuSection
     {:eyebrow "SHEET PAGINATION"
      :title "Keep a longer sheet easy to scan"}
     [:% tm/YStack
      {:gap 10
       :maxWidth 760}
      [:% tama/Sheet
       {:impl {:columns [{:key "symbol"
                          :label "Symbol"
                          :template ["symbol"]}
                         {:key "balance"
                          :label "Balance"
                          :template ["balance"]}]}
        :entries (:? (== page 1)
                     [{:id "one" :symbol "STATS" :balance 1000}
                      {:id "two" :symbol "TAMA" :balance 500}]
                     [{:id "three" :symbol "SLIM" :balance 250}
                      {:id "four" :symbol "PUNE" :balance 125}])}]
      [:% tm/XStack
       {:justifyContent "space-between"
        :alignItems "center"}
       [:% tm/Button
        {:size 2
         :disabled (== page 1)
         :onPress (fn [] (setPage 1))}
        "Previous"]
       [:% tm/Text "Page " page " of 2"]
       [:% tm/Button
        {:size 2
         :disabled (== page 2)
         :onPress (fn [] (setPage 2))}
        "Next"]]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET GROUP HEADER"
      :title "Make groups visible before the rows begin"}
     [:% tm/YStack
      {:gap 8
       :maxWidth 760}
      [:% tm/Text
       {:fontWeight "800"
        :color "#2563eb"}
       "WORLD"]
      [:% tm/Text
       {:fontWeight "800"
        :color "$color11"}
       "STATS · Melbourne"]
      [:% tm/Text
       {:fontWeight "800"
        :color "#2563eb"}
       "LOCAL"]
      [:% tm/Text
       {:fontWeight "800"
        :color "$color11"}
       "TAMA · Pune"]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET HEADER"
      :title "Align headings with the data they describe"}
     [:% tm/Card
      {:padding "$3"
       :maxWidth 760}
      [:% tama/SheetHeader
       {:impl {:columns [{:key "symbol"
                          :label "SYMBOL"}
                         {:key "balance"
                          :label "BALANCE"}
                         {:key "escrow"
                          :label "ESCROW"}]}}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET ROW"
      :title "Render one row with shared cell rules"}
     [:% tm/Card
      {:padding "$3"
       :maxWidth 760}
      [:% tama/SheetRow
       {:entry {:symbol "STATS"
                :balance 1000
                :escrow 50.5}
        :impl {:columns [{:key "symbol"
                          :label "Symbol"
                          :template ["symbol"]}
                         {:key "balance"
                          :label "Balance"
                          :template ["balance"]}
                         {:key "escrow"
                          :label "Escrow"
                          :template ["escrow"]}]}}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET BASIC ROWS"
      :title "Start with a compact collection of rows"}
     [:% tm/YStack
      {:height 260
       :maxWidth 760}
      [:% tama/SheetBasic
       {:impl {:columns [{:key "symbol"
                          :label "Symbol"
                          :template ["symbol"]}
                         {:key "balance"
                          :label "Balance"
                          :template ["balance"]}
                         {:key "escrow"
                          :label "Escrow"
                          :template ["escrow"]}]}
        :entries [{:id "one" :symbol "STATS" :balance 1000 :escrow 50.5}
                  {:id "two" :symbol "DOGE" :balance 1000 :escrow 50.5}]}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET BASIC"
      :title "Give the full basic renderer a taller frame"}
     [:% tm/YStack
      {:height 320
       :maxWidth 760}
      [:% tama/SheetBasic
       {:impl {:columns [{:key "symbol"
                          :label "Symbol"
                          :template ["symbol"]}
                         {:key "status"
                          :label "Status"
                          :template ["status"]}
                         {:key "owner"
                          :label "Owner"
                          :template ["owner"]}]}
        :entries [{:id "one" :symbol "STATS" :status "ACTIVE" :owner "Melbourne"}
                  {:id "two" :symbol "TAMA" :status "READY" :owner "Pune"}
                  {:id "three" :symbol "SLIM" :status "BARE METAL" :owner "Web"}]}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET GROUP ROWS"
      :title "Keep grouped records readable"}
     [:% tm/YStack
      {:gap 8
       :maxWidth 760}
      [:% tm/Text
       {:fontWeight "800"
        :color "#2563eb"}
       "STATS"]
      [:% tama/SheetBasic
       {:height 180
        :impl {:columns [{:key "name"
                          :label "Name"
                          :template ["name"]}
                         {:key "balance"
                          :label "Balance"
                          :template ["balance"]}]}
        :entries [{:id "one" :name "ABC" :balance 506}
                  {:id "two" :name "HIJ" :balance 130400}]}]
      [:% tm/Text
       {:fontWeight "800"
        :color "$color11"}
       "DOGE"]
      [:% tama/SheetBasic
       {:height 180
        :impl {:columns [{:key "name"
                          :label "Name"
                          :template ["name"]}
                         {:key "balance"
                          :label "Balance"
                          :template ["balance"]}]}
        :entries [{:id "three" :name "KLM" :balance 100}
                  {:id "four" :name "QRS" :balance 490}]}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SHEET"
      :title "Scan structured information at a glance"}
     [:% tama/Sheet
      {:impl {:columns [{:key "symbol"
                         :label "Symbol"
                         :template ["symbol"]}
                        {:key "status"
                         :label "Status"
                         :template ["status"]}
                        {:key "owner"
                         :label "Owner"
                         :template ["owner"]}]}
       :entries [{:id "one"
                  :symbol "STATS"
                  :status "ACTIVE"
                  :owner "Melbourne"}
                 {:id "two"
                  :symbol "TAMA"
                  :status "READY"
                  :owner "Pune"}
                 {:id "three"
                  :symbol "SLIM"
                  :status "BARE METAL"
                  :owner "Web"}]}]]]))

(defn.js TamaSlimTablemDemo
  []
  (var control (tama/useLocalControl))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tablem"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLE DEFAULT NOT FOUND"
      :title "Give an empty result a useful landing state"}
     [:% tama/TableStandard
      {:control control
       :entries []}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE DEFAULT IS LOADING"
      :title "Make waiting states legible"}
     [:% tm/Card
      {:padding "$4"
       :minHeight 120
       :justifyContent "center"
       :alignItems "center"}
      [:% tm/Text
       {:color "$color11"}
       "Loading entries…"]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE BACK BUTTON"
      :title "Keep navigation controls near the list"}
     [:% tama/TableToolbar
      {:control control
       :toolbarOpts {:showOrderBy false}}]
     [:% tm/Text
      {:fontSize 12
       :color "$color11"}
      "Toolbar state: " (:? (. control showList) "list" "create")]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST CARD BRIEF"
      :title "Show the smallest useful card"}
     [:% tama/Entry
      {:entry {:title "STATS"
               :balance 1000}
       :impl {:type "card"
              :body {:title {:type "title"
                             :template ["title"]}
                     :balance {:type "pair"
                               :title {:type "raw"
                                       :template "Balance"}
                               :text {:type "raw"
                                      :template ["balance"]}}}}}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST CARD NAV"
      :title "Add a clear navigation affordance"}
     [:% tama/Entry
      {:entry {:title "TAMA"
               :status "READY"}
       :impl {:type "card"
              :body {:title {:type "title"
                             :template ["title"]}
                     :status {:type "bold"
                              :template ["status"]}
                     :action {:type "control"
                              :text "Open"
                              :onPress (fn [])}}}}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST CARD FOLD"
      :title "Fold detail into the row when space is tight"}
     [:% tama/Entry
      {:entry {:title "SLIM"
               :description "Detail stays in the same card"}
       :impl {:type "card"
              :body [{:type "title"
                      :template ["title"]}
                     {:type "p"
                      :template ["description"]}]}}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST CARD SWIPE"
      :title "Expose secondary actions without crowding the row"}
     [:% tm/XStack
      {:gap 8
       :flexWrap "wrap"}
      [:% tama/Entry
       {:entry {:title "PUNE"
                :status "BETA"}
        :impl {:type "card"
               :body [{:type "title"
                       :template ["title"]}
                      {:type "bold"
                       :template ["status"]}]}}]
      [:% tm/Button
       {:size 2
        :chromeless true}
       "Archive"]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST"
      :title "Render a collection of entry cards"}
     [:% tm/YStack
      {:height 300
       :maxWidth 520}
      [:% tama/TableList
       {:entries [{:id "one"
                   :title "STATS"
                   :status "ACTIVE"}
                  {:id "two"
                   :title "TAMA"
                   :status "READY"}
                  {:id "three"
                   :title "SLIM"
                   :status "BARE METAL"}]
        :impl {:item {:type "card"
                      :body {:title {:type "title"
                                     :template ["title"]}
                             :status {:type "bold"
                                      :template ["status"]}}}}}]]]]))

(defn.js TamaSlimTablegDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tableg"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST VIEW ENTRIES"
      :title "Keep a flat set of rows easy to scan"}
     [:% tama/Table
      {:columns [{:key "name"
                  :label "Name"
                  :data ["name"]}
                 {:key "balance"
                  :label "Balance"
                  :data ["balance"]}]
       :entries [{:id "one" :name "ABC" :balance 506}
                 {:id "two" :name "HIJ" :balance 130400}
                 {:id "three" :name "NOP" :balance 1000}
                 {:id "four" :name "TUV" :balance 79}]}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST VIEW GROUP"
      :title "Show group boundaries before their rows"}
     [:% tm/YStack
      {:gap 8}
      [:% tm/Text
       {:fontWeight "800"
        :color "#2563eb"}
       "STATS"]
      [:% tama/Table
       {:columns [{:key "name"
                   :label "Name"
                   :data ["name"]}
                  {:key "balance"
                   :label "Balance"
                   :data ["balance"]}]
        :entries [{:id "one" :name "ABC" :balance 506}
                  {:id "two" :name "HIJ" :balance 130400}]}]
      [:% tm/Text
       {:fontWeight "800"
        :color "$color11"}
       "DOGE"]
      [:% tama/Table
       {:columns [{:key "name"
                   :label "Name"
                   :data ["name"]}
                  {:key "balance"
                   :label "Balance"
                   :data ["balance"]}]
        :entries [{:id "three" :name "KLM" :balance 100}
                  {:id "four" :name "QRS" :balance 490}]}]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST VIEW"
      :title "Group related rows without losing hierarchy"}
     [:% tm/YStack
      {:gap 14
       :maxWidth 680}
      [:% tm/Text
       {:fontWeight "800"
        :color "#2563eb"}
       "ACTIVE"]
      [:% tama/Table
       {:columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "owner"
                   :label "Owner"
                   :data ["owner"]}]
        :entries [{:id "one"
                   :symbol "STATS"
                   :owner "Melbourne"}
                  {:id "two"
                   :symbol "TAMA"
                   :owner "Pune"}]}]
      [:% tm/Text
       {:fontWeight "800"
        :color "#64748b"}
       "ARCHIVED"]
      [:% tama/Table
       {:columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "owner"
                   :label "Owner"
                   :data ["owner"]}]
        :entries [{:id "three"
                   :symbol "SLIM"
                   :owner "Web"}]}]]]]))

(defn.js TamaSlimTableDemo
  []
  (var control (tama/useLocalControl))
  (var [route setRoute] (r/local "list"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-table"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLE DETAIL VIEW"
      :title "Read one record with its supporting fields"}
     [:% tama/Entry
      {:entry {:title "STATS"
               :status "ACTIVE"
               :balance 1000
               :escrow 50.5}
       :impl {:type "card"
              :body [{:type "title"
                      :template ["title"]}
                     {:type "bold"
                      :template ["status"]}
                     {:type "pair"
                      :title {:type "raw"
                              :template "Balance"}
                      :text {:type "raw"
                             :template ["balance"]}}
                     {:type "pair"
                      :title {:type "raw"
                              :template "Escrow"}
                      :text {:type "raw"
                             :template ["escrow"]}}]}}]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE CREATE VIEW"
      :title "Start a new record with focused fields"}
     [:% tm/Card
      {:padding "$4"
       :gap 10
       :maxWidth 560}
      [:% tm/Text
       {:fontSize 18
        :fontWeight "800"}
       "NEW CURRENCY"]
      [:% tm/Input
       {:placeholder "Name"}]
      [:% tm/Input
       {:placeholder "Currency"}]
      [:% tm/Button
       {:size 3}
       "Create"]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE MODIFY VIEW"
      :title "Edit a record without losing its identity"}
     [:% tm/Card
      {:padding "$4"
       :gap 10
       :maxWidth 560}
      [:% tm/Text
       {:fontSize 18
        :fontWeight "800"}
       "EDIT STATS"]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "ID: id-0"]
      [:% tm/Input
       {:defaultValue "STATS"}]
      [:% tm/Input
       {:defaultValue "National Basketball Association"}]
      [:% tm/Button
       {:size 3}
       "Save changes"]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE ROUTER VIEW"
      :title "Move between list, detail, and modify states"}
     [:% tm/YStack
      {:gap 10
       :maxWidth 680}
      [:% tm/XStack
       {:gap 8
        :flexWrap "wrap"}
       [:% tm/Button
        {:size 2
         :chromeless (not (== route "list"))
         :onPress (fn [] (setRoute "list"))}
        "List"]
       [:% tm/Button
        {:size 2
         :chromeless (not (== route "detail"))
         :onPress (fn [] (setRoute "detail"))}
        "Detail"]
       [:% tm/Button
        {:size 2
         :chromeless (not (== route "modify"))
         :onPress (fn [] (setRoute "modify"))}
        "Modify"]]
      [:% tm/Text
       {:fontWeight "700"}
       "Route: " route]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE ROUTER"
      :title "Keep route controls above a collection"}
     [:% tama/TableToolbar
      {:control control}]
     [:% tm/YStack
      {:height 260
       :maxWidth 680}
      [:% tama/TableList
       {:entries [{:id "one" :title "STATS" :status "ACTIVE"}
                  {:id "two" :title "USA" :status "READY"}
                  {:id "three" :title "XLM" :status "ARCHIVED"}]
        :impl {:item {:type "card"
                      :body {:title {:type "title"
                                     :template ["title"]}
                             :status {:type "bold"
                                      :template ["status"]}}}}}]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE"
      :title "Give a dense table a clear control bar"}
     [:% tama/TableToolbar
      {:control control}]
     [:% tama/Table
      {:columns [{:key "symbol"
                  :label "Symbol"
                  :data ["symbol"]}
                 {:key "status"
                  :label "Status"
                  :data ["status"]}
                 {:key "description"
                  :label "Description"
                  :data ["description"]}]
       :entries [{:id "one"
                  :symbol "STATS"
                  :status "ACTIVE"
                  :description "First row"}
                 {:id "two"
                  :symbol "TAMA"
                  :status "READY"
                  :description "Second row"}
                 {:id "three"
                  :symbol "SLIM"
                  :status "BARE METAL"
                  :description "Third row"}]}]]]))

(defn.js TamaSlimTablepDemo
  []
  (var [page setPage] (r/local 1))
  (var columns [{:key "item" :label "Item" :data ["item"]}
                {:key "state" :label "State" :data ["state"]}])
  (var entries (:? (== page 1)
                   [{:id "one" :item "STATS" :state "ACTIVE"}
                    {:id "two" :item "TAMA" :state "READY"}]
                   [{:id "three" :item "SLIM" :state "ARCHIVED"}
                    {:id "four" :item "PUNE" :state "BETA"}]))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tablep"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST VIEW PAGED"
      :title "Page a local collection without changing its shape"}
     [:% tama/Table {:columns columns :entries entries}]
     [:% tm/Text
      {:fontSize 12
       :color "$color11"}
      "Page " page " of 2"]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST PAGED"
      :title "Keep the page controls close to the rows"}
     [:% tm/YStack
      {:gap 10}
      [:% tama/Table {:columns columns :entries entries}]
     [:% tm/XStack
      {:gap 8}
      [:% tm/Button
       {:size 3
        :disabled (== page 1)
        :onPress (fn [] (setPage 1))}
       "Previous"]
      [:% tm/Button
       {:size 3
        :disabled (== page 2)
        :onPress (fn [] (setPage 2))}
       "Next"]]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST VIEW REMOTE PAGED"
      :title "Show a remotely controlled page response"}
     [:% tm/Card
      {:padding "$4"
       :gap 8}
      [:% tm/Text
       {:fontWeight "800"}
       "Remote page response"]
      [:% tm/Text
       {:fontSize 12
        :color "$color11"}
       "Fetched page " page " · 200 total rows"]
      [:% tama/Table {:columns columns :entries entries}]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE LIST REMOTE PAGED"
      :title "Keep remote paging feedback visible"}
     [:% tm/YStack
      {:gap 10}
      [:% tm/XStack
       {:justifyContent "space-between"}
       [:% tm/Text
        {:fontWeight "700"}
        "Remote rows"]
       [:% tm/Text
        {:fontSize 12
         :color "$color11"}
        "Total 200"]]
      [:% tama/Table {:columns columns :entries entries}]
      [:% tm/XStack
       {:justifyContent "flex-end"}
       [:% tm/Button
        {:size 2
         :onPress (fn [] (setPage (:? (== page 1) 2 1)))}
        (:? (== page 1) "Load next" "Load previous")]]]]]))

(defn.js TamaSlimTablesDemo
  []
  (var [query setQuery] (r/local ""))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tables"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLES"
      :title "Search the same data without changing its shape"}
     [:% tm/YStack
      {:gap 12}
      [:% tm/Input
       {:value query
        :placeholder "Search symbols"
        :onChangeText setQuery}]
      [:% tm/Text
       {:color "#64748b"
        :fontSize 12}
       (:? (== query "") "Showing all symbols" "Filtering for: ") query]
      [:% tama/Table
       {:columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "status"
                   :label "Status"
                   :data ["status"]}]
        :entries [{:id "one" :symbol "STATS" :status "ACTIVE"}
                  {:id "two" :symbol "TAMA" :status "READY"}
                  {:id "three" :symbol "SLIM" :status "BARE METAL"}]}]]]]))

(defn.js TamaSlimTablexDemo
  []
  (var control (tama/useLocalControl))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tablex"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLE STANDARD"
      :title "Keep an empty standard table actionable"}
     [:% tm/YStack
      {:height 260
       :maxWidth 520}
      [:% tama/TableStandard
       {:control control
        :entries []}]]]
    [:% -/TamaMenuSection
     {:eyebrow "TABLE EMBEDDED"
      :title "Embed a table inside a larger workflow"}
     [:% tm/YStack
      {:height 320
       :maxWidth 760}
      [:% tama/TableEmbedded
       {:control control
        :columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "status"
                   :label "Status"
                   :data ["status"]}]
        :entries [{:id "one"
                   :symbol "STATS"
                   :status "ACTIVE"}
                  {:id "two"
                   :symbol "TAMA"
                   :status "READY"}]}]]]]))

(defn.js TamaSlimDemo
  []
  (var [route setRoute] (r/local "list"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim"}
    [:% -/TamaMenuSection
     {:eyebrow "SLIM TABLE"
      :title "Compose a table from direct Tamagui primitives"}
     [:% tm/YStack
      {:height 300
       :maxWidth 760}
      [:% tama/Table
       {:columns [{:key "symbol"
                   :label "Symbol"
                   :data ["symbol"]}
                  {:key "status"
                   :label "Status"
                   :data ["status"]}]
        :entries [{:id "one"
                   :symbol "STATS"
                   :status "ACTIVE"}
                  {:id "two"
                   :symbol "TAMA"
                   :status "READY"}
                  {:id "three"
                   :symbol "SLIM"
                   :status "BARE METAL"}]}]]]
    [:% -/TamaMenuSection
     {:eyebrow "SLIM CREATE ENTRY"
      :title "Create a record with a small focused form"}
     [:% tm/Card
      {:padding "$4"
       :gap 10
       :maxWidth 520}
      [:% tm/Input
       {:placeholder "Name"}]
      [:% tm/Input
       {:placeholder "Currency"}]
      [:% tm/Button
       {:size 3}
       "Create"]]]
    [:% -/TamaMenuSection
     {:eyebrow "SLIM ENTRY"
      :title "Render one record as a reusable entry"}
     [:% tama/Entry
      {:entry {:title "STATS"
               :status "ACTIVE"
               :description "A direct Tama entry"}
       :impl {:type "card"
              :body [{:type "title"
                      :template ["title"]}
                     {:type "bold"
                      :template ["status"]}
                     {:type "p"
                      :template ["description"]}]}}]]
    [:% -/TamaMenuSection
     {:eyebrow "SLIM ROUTE CONTROL"
      :title "Keep list and detail controls visible"}
     [:% tm/YStack
      {:gap 10}
      [:% tm/XStack
       {:gap 8}
       [:% tm/Button
        {:size 2
         :chromeless (not (== route "list"))
         :onPress (fn [] (setRoute "list"))}
        "List"]
       [:% tm/Button
        {:size 2
         :chromeless (not (== route "detail"))
         :onPress (fn [] (setRoute "detail"))}
        "Detail"]]
      [:% tm/Text
       {:fontWeight "700"}
       "Route: " route]]]]))

(def.js MODULE (!:module))

^{:refer melbourne.tama-slim-test/TamaMenuScreen :added "4.1"}
(fact "keeps Tama demos responsive inside the shared showcase"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaMenuScreen)))]
    (every? #(clojure.string/includes? form %)
            [":width \"100%\""
             ":maxWidth 860"
             ":alignSelf \"center\""
             ":minWidth 0"]) => true))

^{:refer melbourne.tama-slim-test/TamaSlimCommonDemo :added "4.1"}
(fact "keeps the full surveyed common control sample set"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaSlimCommonDemo)))]
    (count (re-seq #":eyebrow" form)) => 13))

^{:refer melbourne.tama-slim-test/TamaSlimDemo :added "4.1"}
(fact "keeps every Tama Slim tab aligned with its surveyed Slim samples"
  (let [form (fn [sym]
               (pr-str (:form (l/sym-entry :js sym))))
        section-count (fn [sym]
                        (count (re-seq #":eyebrow" (form sym))))
        entry-count (fn [sym]
                      (count (re-seq #":id \"entry" (form sym))))]
    (mapv (fn [sym]
            (if (= sym 'melbourne.tama-slim-test/TamaSlimEntryDemo)
              (entry-count sym)
              (section-count sym)))
          '[melbourne.tama-slim-test/TamaSlimCommonDemo
            melbourne.tama-slim-test/TamaSlimNumberDemo
            melbourne.tama-slim-test/TamaSlimSelectDemo
            melbourne.tama-slim-test/TamaSlimImageDemo
            melbourne.tama-slim-test/TamaSlimLinkDemo
            melbourne.tama-slim-test/TamaSlimErrorDemo
            melbourne.tama-slim-test/TamaSlimSubmitDemo
            melbourne.tama-slim-test/TamaSlimDialogDemo
            melbourne.tama-slim-test/TamaSlimEntryDemo
            melbourne.tama-slim-test/TamaSlimPopupDemo
            melbourne.tama-slim-test/TamaSlimSheetDemo
            melbourne.tama-slim-test/TamaSlimTablemDemo
            melbourne.tama-slim-test/TamaSlimTablegDemo
            melbourne.tama-slim-test/TamaSlimTableDemo
            melbourne.tama-slim-test/TamaSlimTablepDemo
            melbourne.tama-slim-test/TamaSlimTablesDemo
            melbourne.tama-slim-test/TamaSlimTablexDemo
            melbourne.tama-slim-test/TamaSlimDemo])
    => [13 2 2 1 3 1 6 1 37 1 8 8 3 6 4 1 2 4]))

^{:refer melbourne.tama-slim-test/TamaSlimEntryDemo :added "4.1"}
(fact "keeps every Slim entry aggregator sample represented"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaSlimEntryDemo)))
        ids ["entry-free"
             "entry-content-raw"
             "entry-layout-horizontal"
             "entry-layout-vertical"
             "entry-layout-enclosed"
             "entry-layout-portal"
             "entry-layout-portal-sink"
             "entry-layout-debug"
             "entry-layout-form-fade"
             "entry-layout-form-fold"
             "entry-content-title-h1"
             "entry-content-title-h2"
             "entry-content-title-h3"
             "entry-content-title-h4"
             "entry-content-title-h5"
             "entry-content-bold"
             "entry-content-raw-repeat"
             "entry-content-raw-form"
             "entry-content-fill"
             "entry-content-title"
             "entry-content-paragraph"
             "entry-content-separator"
             "entry-content-icon"
             "entry-content-image"
             "entry-content-pair"
             "entry-content-field"
             "entry-content-control"
             "entry-layout-control"
             "entry-content-action"
             "entry-content-link"
             "entry-layout-link"
             "entry-content-route"
             "entry-content-route-toggle"
             "entry-content-submit"
             "entry-layout-card"
             "entry-layout-form"
             "entry"]]
    (count (re-seq #":id \"entry" form)) => 37
    (every? #(clojure.string/includes? form %) ids) => true))

^{:refer melbourne.tama-slim-test/TamaSlimTableDemo :added "4.1"}
(fact "emits native equality checks for Tama route controls"
  (let [emitted (impl/emit-str
                 (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaSlimTableDemo))
                 {:lang :js})]
    (clojure.string/includes? emitted "f_eq") => false
    (clojure.string/includes? emitted "!") => true))
