(ns melbourne.ui-text-test
  (:use code.test)
  (:require [clojure.string :as string]
            [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n]
             [melbourne.base-palette :as base-palette]
             [melbourne.ui-text :as ui-text]
             [melbourne.ui-section :as ui-section]
             [js.core.impl :as j]
             [js.core :as jc]
             [js.core.fetch :as fetch]
             [xt.lang.common-data :as data]
             [xt.lang.common-lib :as lib]]
   :export [MODULE]})

^{:refer melbourne.ui-text/EnumMinor :adopt true :added "0.1"}
(fact  "creates a multi-select horizontal tab bar"
  ^:hidden
  
  (defn.js EnumMinorDemo
    []
    (var [values setValues] (r/local ["USD"]))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/EnumMinor"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/EnumMinor
         {:design {:type "light"}
          :data ["STATS" "XLM" "USD"]
          :values values
          :setValues setValues}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/EnumMinor
         {:design {:type "dark"}
          :data ["STATS" "XLM" "USD"]
          :values values
          :setValues setValues}]]]))))

^{:refer melbourne.ui-text/TabsMinor :adopt true :added "0.1"}
(fact  "creates a multi-select horizontal tab bar"
  ^:hidden

  (defn.js TabsMinorDemo
    []
    (var [value setValue] (r/local "USD"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/TabsMinor"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/TabsMinor
         {:design {:type "light"}
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/TabsMinor
         {:design {:type "dark"}
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue}]]]))))

^{:refer melbourne.ui-text/EnumAccent :adopt true :added "0.1"}
(fact  "creates a multi-select horizontal tab bar"
  ^:hidden
  
  (defn.js EnumAccentDemo
    []
    (var [values setValues] (r/local ["USD"]))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/EnumAccent"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/EnumAccent
         {:design {:type "light"}
          :data ["STATS" "XLM" "USD"]
          :values values
          :setValues setValues}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/EnumAccent
         {:design {:type "dark"}
          :data ["STATS" "XLM" "USD"]
          :values values
          :setValues setValues}]]]))))

^{:refer melbourne.ui-text/TabsAccent :adopt true :added "0.1"}
(fact  "creates a multi-select horizontal tab bar"
  ^:hidden

  (defn.js TabsAccentDemo
    []
    (var [value setValue] (r/local "USD"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/TabsAccent"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/TabsAccent
         {:design {:type "light"}
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/TabsAccent
         {:design {:type "dark"}
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue}]]]))))

^{:refer melbourne.ui-text/Row :added "4.0"}
(fact "Creates a row")

^{:refer melbourne.ui-text/createHeaderFn :added "0.1"}
(fact "seed function for the headers"
  ^:hidden
  
  (defn.js HeaderBaseDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var palette (base-palette/createPalette type color))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/createHeaderFn"} 
[:% n/Row
       [:% n/Tabs
                {:data ["purple" "teal" "blue" "green"]
                 :value color
                 :setValue setColor}]
       [:% n/Text "   "]
       [:% n/Tabs
        {:data ["light" "dark"]
         :value type
         :setValue setType}]] 
[:% n/Tabs
       {:data ["background" "primary" "neutral" "error"]
        :value foil
        :setValue setFoil}] 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/H1
                ui-text/H2
                ui-text/H3
                ui-text/H4
                ui-text/H5
                ui-text/H6]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}}
                    "HELLO"]])))]
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/H1
                ui-text/H2
                ui-text/H3
                ui-text/H4
                ui-text/H5
                ui-text/H6]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}
                     :designOverride {:variant {:fg {:key "neutral"
                                                   :tone "sharpen"}}}}
                    "HELLO"]])))]
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/H1
                ui-text/H2
                ui-text/H3
                ui-text/H4
                ui-text/H5
                ui-text/H6]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}
                     :designOverride {:variant {:fg {:key "background"
                                                   :tone "flatten"}}}}
                    "HELLO"]])))]
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/H1
                ui-text/H2
                ui-text/H3
                ui-text/H4
                ui-text/H5
                ui-text/H6]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}
                     :designOverride {:variant {:fg {:key "error"}}}}
                    "HELLO"]])))]]))))

^{:refer melbourne.ui-text/createTextFn :added "0.1"}
(fact "seed function for the text"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.ui-text/createTextFn)))]
    (string/includes? form "melbourne.base-font/fontFamily") => true
    (string/includes? form "Lato") => false)
  ^:hidden
  
  (defn.js TextBaseDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var palette (base-palette/createPalette type color))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/createTextFn"} 
[:% n/Row
       [:% n/Tabs
                {:data ["purple" "teal" "blue" "green"]
                 :value color
                 :setValue setColor}]
       [:% n/Text "   "]
       [:% n/Tabs
        {:data ["light" "dark"]
         :value type
         :setValue setType}]] 
[:% n/Tabs
       {:data ["background" "primary" "neutral" "error"]
        :value foil
        :setValue setFoil}] 
[:% n/Row
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/P
                ui-text/Caption]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}}
                    "HELLO"]])))]
       [:% n/View
        {:style {:backgroundColor (base-palette/getColorRaw
                                   palette
                                   (or foil "background"))}}
        (j/map [ui-text/P
                ui-text/Caption]
               (fn [Component i]
                 (return
                  [:% n/View
                   {:key i
                    :style {:padding 10}}
                   [:% Component
                    {:design #{type color}
                     :variant {:fg {:key "background"}
                               :bg {:key "primary"}}}
                    "HELLO"]])))]]))))

^{:refer melbourne.ui-text/ActivityIndicator :added "0.1"}
(fact "creates an activity indicator"
  ^:hidden

  (defn.js ActivityIndicatorDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/ActivityIndicator"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/ActivityIndicator
         {:design {:type "light"}}]
        [:% ui-text/ActivityIndicator
         {:design {:type "light"}
          :size 100}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/ActivityIndicator
         {:design {:type "dark"}}]
        [:% ui-text/ActivityIndicator
         {:design {:type "light"}
          :size 100}]]]))))

^{:refer melbourne.ui-text/Icon :added "0.1"}
(fact "creates the icon"
  ^:hidden

  (defn.js IconDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/Icon"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/Icon
         {:design {:type "light"}
          :name "close"}]
        [:% ui-text/Icon
         {:design {:type "light"}
          :name "close"
          :size 100}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/Icon
         {:design {:type "dark"}
          :name "close"}]
        [:% ui-text/Icon
         {:design {:type "light"}
          :name "close"
          :size 100}]]]))))

^{:refer melbourne.ui-text/Avatar :added "0.1"}
(fact "creates the avatar"
  ^:hidden

  (defn.js AvatarDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/Avatar"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% ui-text/Avatar
         {:design {:type "light"}
          :text "H"}]
        [:% ui-text/Avatar
         {:design {:type "light"}
          :text "H"
          :size 100}]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% ui-text/Avatar
         {:design {:type "dark"}
          :text "H"}]
        [:% ui-text/Avatar
         {:design {:type "light"}
          :text "H"
          :size 100}]]]))))

^{:refer melbourne.ui-text/activeButtonTheme :added "0.1"}
(fact "creates the active button theme")

^{:refer melbourne.ui-text/minorButtonTheme :added "0.1"}
(fact "creates the minor button theme")

^{:refer melbourne.ui-text/minorToggleTheme :added "0.1"}
(fact "creates the minor toggle theme")

^{:refer melbourne.ui-text/createMinorFn :added "0.1"}
(fact "seed function for the minor components"
  ^:hidden
  
  (defn.js MinorBaseDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var [selected setSelected] (r/local false))
    (var palette (base-palette/createPalette type color))
    (var components [ui-text/ButtonMinor
                     ui-text/ToggleMinor])
    (var overrides  [(ui-text/minorToggleTheme
                      {:key "background"
                       :tone "standard"}
                      {:key "primary"
                       :tone "sharpen"})
                     (ui-text/minorToggleTheme
                      {:key "primary"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})
                     (ui-text/minorToggleTheme
                      {:key "neutral"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})
                     (ui-text/minorToggleTheme
                      {:key "error"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})])
    (return
     (n/EnclosedCode 
{:key (+ type "." color)
       :label "melbourne.ui-text/createMinorFn"} 
[:% n/Row
       [:% n/Tabs
                {:data ["purple" "teal" "blue" "green"]
                 :value color
                 :setValue setColor}]
       [:% n/Text "   "]
       [:% n/Tabs
        {:data ["light" "dark"]
         :value type
         :setValue setType}]] 
[:% n/Tabs
       {:data ["background" "primary" "neutral" "error"]
        :value foil
        :setValue setFoil}] 
[:% n/Row
       (j/map
        overrides
        (fn [override i]
          (return
           [:% n/View
            {:key i
             :style {:backgroundColor (base-palette/getColorRaw
                                       palette
                                       (or foil "background"))}}
            (j/map components
                   (fn [Component i]
                     (return
                      [:% n/View
                       {:key i
                        :style {:padding 10}}
                       [:% Component
                        {:design #{type color}
                         :variant override
                         :selected selected
                         :onPress
                         (fn:> (setSelected
                                (not selected)))
                         :text "HELLO"}]])))])))]))))

^{:refer melbourne.ui-text/accentButtonTheme :added "0.1"}
(fact "creates the accent button theme")

^{:refer melbourne.ui-text/accentToggleTheme :added "0.1"}
(fact "creates the accent toggle theme")

^{:refer melbourne.ui-text/createAccentFn :added "0.1"}
(fact "seed function for the accent components"
  ^:hidden
  
  (defn.js AccentBaseDemo
    []
    (var [color setColor] (r/local "purple"))
    (var [type setType]   (r/local "dark"))
    (var [foil setFoil]   (r/local "background"))
    (var [selected setSelected] (r/local false))
    (var palette (base-palette/createPalette type color))
    (var components [ui-text/ButtonAccent
                     ui-text/ToggleAccent])
    (var overrides  [(ui-text/accentToggleTheme
                      {:key  "neutral"
                       :tone "standard"}
                      {:key  "primary"
                       :tone "sharpen"})
                     (ui-text/accentToggleTheme
                      {:key "primary"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})
                     (ui-text/accentToggleTheme
                      {:key "neutral"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})
                     (ui-text/accentToggleTheme
                      {:key "error"
                       :tone "standard"}
                      {:key "background"
                       :tone "sharpen"})])
    (return
     (n/EnclosedCode 
{:key (+ type "." color)
       :label "melbourne.ui-text/createAccentFn"} 
[:% n/Row
       [:% n/Tabs
                {:data ["purple" "teal" "blue" "green"]
                 :value color
                 :setValue setColor}]
       [:% n/Text "   "]
       [:% n/Tabs
        {:data ["light" "dark"]
         :value type
         :setValue setType}]] 
[:% n/Tabs
       {:data ["background" "primary" "neutral" "error"]
        :value foil
        :setValue setFoil}] 
[:% n/Row
       (j/map
        overrides
        (fn [override i]
          (return
           [:% n/View
            {:key i
             :style {:backgroundColor (base-palette/getColorRaw
                                       palette
                                       (or foil "background"))}}
            (j/map components
                   (fn [Component i]
                     (return
                      [:% n/View
                       {:key i
                        :style {:padding 10}}
                       [:% Component
                        {:design #{type color}
                         :variant override
                         :selected selected
                         :onPress
                         (fn:> (setSelected
                                (not selected)))
                         :text "HELLO"}]])))])))]))))

^{:refer melbourne.ui-text/ButtonTooltipOverlay :added "4.0"}
(fact "creates a button overlay")

^{:refer melbourne.ui-text/ButtonTooltip :added "4.0"}
(fact "creates a button tooltip"
  ^:hidden
  
  (defn.js ButtonTooltipDemo
    []
    (var mainComponent (r/const
                        (fn:> [:% n/View
                               {:style {:height 100
                                        :width 100
                                        :backgroundColor "red"}}])))
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-text/ButtonTooltip"} 
[:% n/Row
        [:% ui-section/SectionBase
         {:design {:type "light"}}
         [:% n/Row
          [:% ui-text/ButtonTooltip
           {:design {:type "light"}
            :mainComponent mainComponent
            :text  "Press"
            :tooltip {:position "top"}
            :onPress (fn:> (alert "HELLO"))}]
          [:% ui-text/ButtonTooltip
           {:design {:type "light"}
            :mainComponent mainComponent
            :component "accent"
            :text  "Press"
            :tooltip {:position "left"}
            :onPress (fn:> (alert "HELLO"))}]]]
        [:% ui-section/SectionBase
         {:design {:type "dark"}}
         [:% n/Row
          [:% ui-text/ButtonTooltip
           {:design {:type "dark"}
            :mainComponent mainComponent
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]
          [:% ui-text/ButtonTooltip
           {:design {:type "dark"}
            :mainComponent mainComponent
            :component "accent"
            :tooltip {:position "right"}
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]]]])])))

^{:refer melbourne.ui-text/ConfirmTooltip :added "4.0"}
(fact  "creates a button with tooltip confirmation"
  ^:hidden

  (defn.js ConfirmTooltipDemo
    []
    (return
     [:% n/Isolation
      (n/EnclosedCode 
{:label "melbourne.ui-text/ConfirmTooltip"} 
[:% n/Row
        [:% ui-section/SectionBase
         {:design {:type "light"}}
         [:% n/Row
          [:% ui-text/ConfirmTooltip
           {:design {:type "light"}
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]
          [:% ui-text/ConfirmTooltip
           {:design {:type "light"}
            :component "accent"
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]]]
        [:% ui-section/SectionBase
         {:design {:type "dark"}}
         [:% n/Row
          [:% ui-text/ConfirmTooltip
           {:design {:type "dark"}
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]
          [:% ui-text/ConfirmTooltip
           {:design {:type "dark"}
            :component "accent"
            :text  "Press"
            :onPress (fn:> (alert "HELLO"))}]]]])])))

^{:refer melbourne.ui-text/TextAltImpl :added "0.1"}
(fact "creates a visual viewer")

^{:refer melbourne.ui-text/TextAlt :added "0.1"}
(fact "creates the text alt demo"
  ^:hidden
  
  (defn.js TextAltDemo
    []
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-text/TextAlt"} 
[:% n/Row
       [:% ui-section/SectionBase
        {:design {:type "light"}}
        [:% n/Row
         [:% ui-text/TextAlt
          {:design {:type "light"}
           :value {:a 1 :b 2}}]]]
       [:% ui-section/SectionBase
        {:design {:type "dark"}}
        [:% n/Row
         [:% ui-text/TextAlt
          {:design {:type "dark"}
           :value {:a 1 :b 2}}]]]])))

  (def.js MODULE (!:module)))
