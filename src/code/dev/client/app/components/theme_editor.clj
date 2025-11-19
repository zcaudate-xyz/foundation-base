(ns code.dev.client.app.components.theme-editor
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

;; Interface definitions are removed as per spec
;; export interface Theme { ... }
;; interface ThemeEditorProps { ... }

(def.js defaultTheme
  {:colors {:primary "#3b82f6"
            :secondary "#8b5cf6"
            :accent "#ec4899"
            :background "#ffffff"
            :surface "#f9fafb"
            :text "#111827"
            :textSecondary "#6b7280"
            :border "#e5e7eb"
            :success "#10b981"
            :warning "#f59e0b"
            :error "#ef4444"}
   :typography {:fontFamily "Inter, system-ui, sans-serif"
                :fontFamilyHeading "Inter, system-ui, sans-serif"
                :fontFamilyMono "JetBrains Mono, monospace"
                :fontSize {:xs "0.75rem"
                           :sm "0.875rem"
                           :base "1rem"
                           :lg "1.125rem"
                           :xl "1.25rem"
                           :"2xl" "1.5rem"
                           :"3xl" "1.875rem"
                           :"4xl" "2.25rem"}}
   :spacing {:xs "0.25rem"
             :sm "0.5rem"
             :md "1rem"
             :lg "1.5rem"
             :xl "2rem"
             :"2xl" "3rem"
             :"3xl" "4rem"}
   :borderRadius {:none "0"
                  :sm "0.25rem"
                  :md "0.5rem"
                  :lg "0.75rem"
                  :xl "1rem"
                  :full "9999px"}
   :shadows {:sm "0 1px 2px 0 rgb(0 0 0 / 0.05)"
             :md "0 4px 6px -1px rgb(0 0 0 / 0.1)"
             :lg "0 10px 15px -3px rgb(0 0 0 / 0.1)"
             :xl "0 20px 25px -5px rgb(0 0 0 / 0.1)"}})

(defn.js ThemeEditor
  [{:# [theme onThemeChange]}]
  (var [activeSection setActiveSection] (r/useState "colors"))

  (var updateColor
       (fn [key value]
         (onThemeChange
          {:.. theme
           :colors {:.. theme.colors
                    key value}})))

  (var updateTypography
       (fn [key value]
         (if (. (Object.keys theme.typography) (includes key))
           (onThemeChange
            {:.. theme
             :typography {:.. theme.typography
                          key value}})
           (onThemeChange
            {:.. theme
             :typography {:.. theme.typography
                          :fontSize {:.. theme.typography.fontSize
                                     key value}}}))))

  (var updateSpacing
       (fn [key value]
         (onThemeChange
          {:.. theme
           :spacing {:.. theme.spacing
                     key value}})))

  (var updateBorderRadius
       (fn [key value]
         (onThemeChange
          {:.. theme
           :borderRadius {:.. theme.borderRadius
                          key value}})))

  (var updateShadow
       (fn [key value]
         (onThemeChange
          {:.. theme
           :shadows {:.. theme.shadows
                     key value}})))

  (var resetTheme
       (fn []
         (onThemeChange -/defaultTheme)))

  (var exportTheme
       (async (fn []
                (var themeJSON (JSON.stringify theme nil 2))
                (try
                  (await (. navigator.clipboard (writeText themeJSON)))
                  (catch err
                    (var textArea (document.createElement "textarea"))
                    (:= textArea.value themeJSON)
                    (:= textArea.style.position "fixed")
                    (:= textArea.style.left "-999999px")
                    (:= textArea.style.top "-999999px")
                    (. document.body (appendChild textArea))
                    (. textArea (focus))
                    (. textArea (select))
                    (try
                      (. document (execCommand "copy"))
                      (catch err2
                        (console.error "Failed to copy to clipboard")))
                    (. document.body (removeChild textArea)))))))

  (return
   [:div {:className "flex flex-col h-full bg-[#252525]"}
    [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3 justify-between"}
     [:span {:className "text-xs text-gray-400"} "Theme Editor"]
     [:div {:className "flex gap-1"}
      [:% fg/Button
       {:variant "ghost"
        :size "sm"
        :onClick exportTheme
        :className "h-6 px-2 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
       [:% lc/Copy {:className "w-3 h-3 mr-1"}]
       "Copy"]
      [:% fg/Button
       {:variant "ghost"
        :size "sm"
        :onClick resetTheme
        :className "h-6 px-2 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
       [:% lc/RotateCcw {:className "w-3 h-3 mr-1"}]
       "Reset"]]]

    [:div {:className "flex border-b border-[#323232] bg-[#2b2b2b]"}
     [:button
      {:onClick (fn [] (return (setActiveSection "colors")))
       :className (+ "flex items-center gap-2 px-4 py-2 text-xs transition-colors "
                     (:? (=== activeSection "colors")
                         "bg-[#323232] text-gray-200 border-b-2 border-blue-500"
                         "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]"))}
      [:% lc/Palette {:className "w-3 h-3"}]
      "Colors"]
     [:button
      {:onClick (fn [] (return (setActiveSection "typography")))
       :className (+ "flex items-center gap-2 px-4 py-2 text-xs transition-colors "
                     (:? (=== activeSection "typography")
                         "bg-[#323232] text-gray-200 border-b-2 border-blue-500"
                         "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]"))}
      [:% lc/Type {:className "w-3 h-3"}]
      "Typography"]
     [:button
      {:onClick (fn [] (return (setActiveSection "spacing")))
       :className (+ "flex items-center gap-2 px-4 py-2 text-xs transition-colors "
                     (:? (=== activeSection "spacing")
                         "bg-[#323232] text-gray-200 border-b-2 border-blue-500"
                         "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]"))}
      [:% lc/Ruler {:className "w-3 h-3"}]
      "Spacing"]
     [:button
      {:onClick (fn [] (return (setActiveSection "borders")))
       :className (+ "flex items-center gap-2 px-4 py-2 text-xs transition-colors "
                     (:? (=== activeSection "borders")
                         "bg-[#323232] text-gray-200 border-b-2 border-blue-500"
                         "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]"))}
      [:% lc/Maximize2 {:className "w-3 h-3"}]
      "Borders"]
     [:button
      {:onClick (fn [] (return (setActiveSection "shadows")))
       :className (+ "flex items-center gap-2 px-4 py-2 text-xs transition-colors "
                     (:? (=== activeSection "shadows")
                         "bg-[#323232] text-gray-200 border-b-2 border-blue-500"
                         "text-gray-500 hover:text-gray-300 hover:bg-[#2d2d2d]"))}
      "Shadows"]]

    [:% fg/ScrollArea {:className "flex-1"}
     [:div {:className "p-4 space-y-4"}
      (:? (=== activeSection "colors")
          [:<>
           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Brand Colors"]
            [:% -/ColorInput {:label "Primary" :value theme.colors.primary :onChange (fn [v] (return (updateColor "primary" v)))}]
            [:% -/ColorInput {:label "Secondary" :value theme.colors.secondary :onChange (fn [v] (return (updateColor "secondary" v)))}]
            [:% -/ColorInput {:label "Accent" :value theme.colors.accent :onChange (fn [v] (return (updateColor "accent" v)))}]]

           [:div {:className "h-[1px] bg-[#323232]"}]

           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Surface Colors"]
            [:% -/ColorInput {:label "Background" :value theme.colors.background :onChange (fn [v] (return (updateColor "background" v)))}]
            [:% -/ColorInput {:label "Surface" :value theme.colors.surface :onChange (fn [v] (return (updateColor "surface" v)))}]
            [:% -/ColorInput {:label "Border" :value theme.colors.border :onChange (fn [v] (return (updateColor "border" v)))}]]

           [:div {:className "h-[1px] bg-[#323232]"}]

           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Text Colors"]
            [:% -/ColorInput {:label "Text" :value theme.colors.text :onChange (fn [v] (return (updateColor "text" v)))}]
            [:% -/ColorInput {:label "Text Secondary" :value theme.colors.textSecondary :onChange (fn [v] (return (updateColor "textSecondary" v)))}]]

           [:div {:className "h-[1px] bg-[#323232]"}]

           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Semantic Colors"]
            [:% -/ColorInput {:label "Success" :value theme.colors.success :onChange (fn [v] (return (updateColor "success" v)))}]
            [:% -/ColorInput {:label "Warning" :value theme.colors.warning :onChange (fn [v] (return (updateColor "warning" v)))}]
            [:% -/ColorInput {:label "Error" :value theme.colors.error :onChange (fn [v] (return (updateColor "error" v)))}]]]
          nil)

      (:? (=== activeSection "typography")
          [:<>
           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Font Families"]
            [:% -/TextInput {:label "Body Font" :value theme.typography.fontFamily :onChange (fn [v] (return (updateTypography "fontFamily" v)))}]
            [:% -/TextInput {:label "Heading Font" :value theme.typography.fontFamilyHeading :onChange (fn [v] (return (updateTypography "fontFamilyHeading" v)))}]
            [:% -/TextInput {:label "Mono Font" :value theme.typography.fontFamilyMono :onChange (fn [v] (return (updateTypography "fontFamilyMono" v)))}]]

           [:div {:className "h-[1px] bg-[#323232]"}]

           [:div {:className "space-y-3"}
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Font Sizes"]
            [:% -/TextInput {:label "XS" :value theme.typography.fontSize.xs :onChange (fn [v] (return (updateTypography "xs" v)))}]
            [:% -/TextInput {:label "SM" :value theme.typography.fontSize.sm :onChange (fn [v] (return (updateTypography "sm" v)))}]
            [:% -/TextInput {:label "Base" :value theme.typography.fontSize.base :onChange (fn [v] (return (updateTypography "base" v)))}]
            [:% -/TextInput {:label "LG" :value theme.typography.fontSize.lg :onChange (fn [v] (return (updateTypography "lg" v)))}]
            [:% -/TextInput {:label "XL" :value theme.typography.fontSize.xl :onChange (fn [v] (return (updateTypography "xl" v)))}]
            [:% -/TextInput {:label "2XL" :value theme.typography.fontSize."2xl" :onChange (fn [v] (return (updateTypography "2xl" v)))}]
            [:% -/TextInput {:label "3XL" :value theme.typography.fontSize."3xl" :onChange (fn [v] (return (updateTypography "3xl" v)))}]
            [:% -/TextInput {:label "4XL" :value theme.typography.fontSize."4xl" :onChange (fn [v] (return (updateTypography "4xl" v)))}]]]
          nil)

      (:? (=== activeSection "spacing")
          [:div {:className "space-y-3"}
           [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Spacing Scale"]
           [:% -/TextInput {:label "XS" :value theme.spacing.xs :onChange (fn [v] (return (updateSpacing "xs" v)))}]
           [:% -/TextInput {:label "SM" :value theme.spacing.sm :onChange (fn [v] (return (updateSpacing "sm" v)))}]
           [:% -/TextInput {:label "MD" :value theme.spacing.md :onChange (fn [v] (return (updateSpacing "md" v)))}]
           [:% -/TextInput {:label "LG" :value theme.spacing.lg :onChange (fn [v] (return (updateSpacing "lg" v)))}]
           [:% -/TextInput {:label "XL" :value theme.spacing.xl :onChange (fn [v] (return (updateSpacing "xl" v)))}]
           [:% -/TextInput {:label "2XL" :value theme.spacing."2xl" :onChange (fn [v] (return (updateSpacing "2xl" v)))}]
           [:% -/TextInput {:label "3XL" :value theme.spacing."3xl" :onChange (fn [v] (return (updateSpacing "3xl" v)))}]]
          nil)

      (:? (=== activeSection "borders")
          [:div {:className "space-y-3"}
           [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Border Radius"]
           [:% -/TextInput {:label "None" :value theme.borderRadius.none :onChange (fn [v] (return (updateBorderRadius "none" v)))}]
           [:% -/TextInput {:label "SM" :value theme.borderRadius.sm :onChange (fn [v] (return (updateBorderRadius "sm" v)))}]
           [:% -/TextInput {:label "MD" :value theme.borderRadius.md :onChange (fn [v] (return (updateBorderRadius "md" v)))}]
           [:% -/TextInput {:label "LG" :value theme.borderRadius.lg :onChange (fn [v] (return (updateBorderRadius "lg" v)))}]
           [:% -/TextInput {:label "XL" :value theme.borderRadius.xl :onChange (fn [v] (return (updateBorderRadius "xl" v)))}]
           [:% -/TextInput {:label "Full" :value theme.borderRadius.full :onChange (fn [v] (return (updateBorderRadius "full" v)))}]]
          nil)

      (:? (=== activeSection "shadows")
          [:div {:className "space-y-3"}
           [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider"} "Box Shadows"]
           [:% -/ShadowInput {:label "SM" :value theme.shadows.sm :onChange (fn [v] (return (updateShadow "sm" v)))}]
           [:% -/ShadowInput {:label "MD" :value theme.shadows.md :onChange (fn [v] (return (updateShadow "md" v)))}]
           [:% -/ShadowInput {:label "LG" :value theme.shadows.lg :onChange (fn [v] (return (updateShadow "lg" v)))}]
           [:% -/ShadowInput {:label "XL" :value theme.shadows.xl :onChange (fn [v] (return (updateShadow "xl" v)))}]]
          nil)]]))

(defn.js ColorInput
  [{:# [label value onChange]}]
  (return
   [:div {:className "flex items-center gap-3"}
    [:div {:className "flex-1"}
     [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"} label]
     [:% fg/Input
      {:type "text"
       :value value
       :onChange (fn [e] (return (onChange e.target.value)))
       :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]]
    [:div {:className "pt-5"}
     [:input
      {:type "color"
       :value value
       :onChange (fn [e] (return (onChange e.target.value)))
       :className "w-10 h-7 rounded border border-[#3a3a3a] cursor-pointer bg-[#1e1e1e]"}]]]))

(defn.js TextInput
  [{:# [label value onChange]}]
  (return
   [:div
    [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"} label]
    [:% fg/Input
     {:type "text"
      :value value
      :onChange (fn [e] (return (onChange e.target.value)))
      :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]]))

(defn.js ShadowInput
  [{:# [label value onChange]}]
  (return
   [:div
    [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"} label]
    [:div {:className "flex items-center gap-2"}
     [:% fg/Input
      {:type "text"
       :value value
       :onChange (fn [e] (return (onChange e.target.value)))
       :className "flex-1 h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]
     [:div
      {:className "w-10 h-7 bg-white rounded border border-[#3a3a3a]"
       :style {:boxShadow value}}]]]))
