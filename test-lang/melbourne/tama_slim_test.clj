(ns melbourne.tama-slim-test
  (:use code.test)
  (:require [lang.core :as l]))
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
     :maxWidth "100%"
     :alignSelf "stretch"
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
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-common"}
    [:% -/TamaMenuSection
     {:eyebrow "COMMON CONTROLS"
      :title "A calm, consistent form surface"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 14}
      [:% tm/YStack
       {:flex 1
        :minWidth 220
        :gap 8}
       [:% tm/Label
        {:htmlFor "tama-name"
         :fontWeight "700"}
        "Name"]
       [:% tm/Input
        {:id "tama-name"
         :value name
         :onChangeText setName
         :placeholder "Your name"}]
       [:% tm/TextArea
        {:minHeight 90
         :defaultValue "A Tamagui textarea with the same field rhythm."}]]
      [:% tm/YStack
       {:flex 1
        :minWidth 220
        :gap 12}
       [:% tm/XStack
        {:alignItems "center"
         :gap 10}
        [:% tm/Checkbox
         {:id "tama-agree"
          :checked agreed
          :onCheckedChange setAgreed}
         [:% tm/CheckboxIndicator
          {:borderRadius 5}
          [:% tm/Text
           {:color "#2563eb"
            :fontWeight "900"}
           "✓"]]]
        [:% tm/Label
         {:htmlFor "tama-agree"
          :fontWeight "700"}
         "I agree to the terms"]]
       [:% tm/XStack
        {:alignItems "center"
         :gap 10}
        [:% tm/Switch
         {:checked enabled
          :onCheckedChange setEnabled}
         [:% tm/SwitchThumb]]
        [:% tm/Text
         {:color "#64748b"
          :fontSize 12}
         (:? enabled "Notifications are on" "Notifications are off")]]]]]]))

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
     {:eyebrow "NUMBER"
      :title "Tune a value with slider and stepper affordances"}
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
        {:size "$3"
         :onPress (fn [] (setAmount (- amount 5)))}
        "− 5"]
       [:% tm/Button
        {:size "$3"
         :onPress (fn [] (setAmount (+ amount 5)))}
        "+ 5"]]]]]))

(defn.js TamaSlimSelectDemo
  []
  (var [value setValue] (r/local "Victoria"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-select"}
    [:% -/TamaMenuSection
     {:eyebrow "SELECT"
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
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-link"}
    [:% -/TamaMenuSection
     {:eyebrow "LINK"
      :title "Make navigation feel intentional"}
     [:% tm/YStack
      {:gap 12
       :maxWidth 520}
      [:% tm/Anchor
       {:href "#tama-components"
        :color "#2563eb"
        :fontWeight "800"
        :textDecorationLine "underline"
        :onPress (fn [] (setMessage "Components selected"))}
       "Browse the component catalog"]
      [:% tm/XStack
       {:gap 8
        :flexWrap "wrap"}
       [:% tm/Button
        {:size "$3"
         :chromeless true
         :onPress (fn [] (setMessage "Documentation selected"))}
        "Documentation"]
       [:% tm/Button
        {:size "$3"
         :chromeless true
         :onPress (fn [] (setMessage "Source selected"))}
        "Source"]]
      [:% tm/Text
       {:color "#64748b"
        :fontSize 12}
       message]]]]))

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
       {:size "$3"
        :alignSelf "flex-start"
        :onPress (fn [] (setRetried true))}
       (:? retried "Retry queued" "Try again")]]]]))

(defn.js TamaSlimSubmitDemo
  []
  (var [status setStatus] (r/local "Ready to submit"))
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-submit"}
    [:% -/TamaMenuSection
     {:eyebrow "SUBMIT"
      :title "Show progress, success, and the next action"}
     [:% tm/XStack
      {:alignItems "center"
       :gap 12
       :flexWrap "wrap"}
      [:% tm/Button
       {:size "$4"
        :onPress (fn [] (setStatus "Submitted successfully"))}
       "Submit form"]
      [:% tm/Button
       {:size "$3"
        :chromeless true
        :onPress (fn [] (setStatus "Draft saved"))}
       "Save draft"]
      [:% tm/Text
       {:color "#64748b"
        :fontSize 12}
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
        {:size "$4"}
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
           {:size "$3"
            :chromeless true}
           "Cancel"]]
         [:% tm/DialogClose
          {:asChild true}
          [:% tm/Button
           {:size "$3"}
           "Publish"]]]]]]]]))

(defn.js TamaSlimEntryDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-entry"}
    [:% -/TamaMenuSection
     {:eyebrow "ENTRY"
      :title "Compose a reusable content record"}
     [:% tm/YStack
      {:gap 12
       :maxWidth 640}
      [:% tama/Entry
       {:entry {:title "Direct Tamagui card"
                :status "ACTIVE"
                :description "Shared entry data, no legacy renderer"}
        :impl {:type "card"
               :body [{:type "title"
                       :template ["title"]}
                      {:type "bold"
                       :template ["status"]}
                      {:type "p"
                       :template ["description"]}]}}]
      [:% tama/Entry
       {:entry {:title "Horizontal layout"
                :description "The same contract can switch layout"}
        :impl {:type "h"
               :body [{:type "title-h3"
                       :template ["title"]}
                      {:type "p"
                       :template ["description"]}]}}]]]]))

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
        {:size "$4"}
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
         {:size "$2"
          :chromeless true}
         "Done"]]]]]]))

(defn.js TamaSlimSheetDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-sheet"}
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
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tablem"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLEM"
      :title "Turn a list into readable cards"}
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
     {:eyebrow "TABLEG"
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
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-table"}
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
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim-tablep"}
    [:% -/TamaMenuSection
     {:eyebrow "TABLEP"
      :title "Keep larger collections easy to page through"}
     [:% tama/Table
      {:columns [{:key "item"
                  :label "Item"
                  :data ["item"]}
                 {:key "state"
                  :label "State"
                  :data ["state"]}]
       :entries (:? (== page 1)
                    [{:id "one" :item "STATS" :state "ACTIVE"}
                     {:id "two" :item "TAMA" :state "READY"}]
                    [{:id "three" :item "SLIM" :state "ARCHIVED"}
                     {:id "four" :item "PUNE" :state "BETA"}])}]
     [:% tm/XStack
      {:alignItems "center"
       :justifyContent "space-between"}
      [:% tm/Button
       {:size "$3"
        :disabled (== page 1)
        :onPress (fn [] (setPage 1))}
       "Previous"]
      [:% tm/Text
       {:fontWeight "700"}
       "Page " page " of 2"]
      [:% tm/Button
       {:size "$3"
        :disabled (== page 2)
        :onPress (fn [] (setPage 2))}
       "Next"]]]]))

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
     {:eyebrow "TABLEX"
      :title "Embed a table inside a larger workflow"}
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
                  :status "READY"}]}]]]))

(defn.js TamaSlimDemo
  []
  (return
   [:% -/TamaMenuScreen
    {:label "tama/slim"}
    [:% -/TamaMenuSection
     {:eyebrow "SLIM TO TAMA"
      :title "One coherent primitive family"}
     [:% tm/XStack
      {:flexWrap "wrap"
       :gap 14}
      [:% tm/Card
       {:flex 1
        :minWidth 240
        :padding 16
        :gap 10
        :borderRadius 14
        :backgroundColor "#eff6ff"}
       [:% tm/Text
        {:fontWeight "800"
         :color "#1d4ed8"}
        "Entry"]
       [:% tm/Text
        {:color "#334155"}
        "Composes title, status, and body content from one record."]]
      [:% tm/Card
       {:flex 1
        :minWidth 240
        :padding 16
        :gap 10
        :borderRadius 14
        :backgroundColor "#f0fdf4"}
       [:% tm/Text
        {:fontWeight "800"
         :color "#15803d"}
        "Table"]
       [:% tm/Text
        {:color "#334155"}
        "Keeps columns, rows, and actions legible across sizes."]]]]]))

(def.js MODULE (!:module))

^{:refer melbourne.tama-slim-test/TamaMenuScreen :added "4.1"}
(fact "keeps Tama demos responsive inside the shared showcase"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaMenuScreen)))]
    (every? #(clojure.string/includes? form %)
            [":width \"100%\""
             ":maxWidth \"100%\"
             ":alignSelf \"stretch\""
             ":minWidth 0"]) => true))

^{:refer melbourne.tama-slim-test/TamaSlimErrorDemo :added "4.1"}
(fact "uses Tamagui size tokens for button controls"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.tama-slim-test/TamaSlimErrorDemo)))]
    (clojure.string/includes? form ":size \"$3\"") => true
    (not (clojure.string/includes? form ":size 3")) => true))
