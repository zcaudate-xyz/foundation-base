(ns code.dev.client.app.components.component-palette
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(def.js componentGroups
  [{:name "Layout (Tamagui)"
    :components [{:color "text-blue-500"
                  :type "View"
                  :icon lc/Square}
                 {:color "text-blue-600"
                  :type "XStack"
                  :icon lc/AlignHorizontalSpaceAround}
                 {:color "text-blue-700"
                  :type "YStack"
                  :icon lc/AlignVerticalSpaceAround}
                 {:color "text-purple-500"
                  :type "Card"
                  :icon lc/Layers}]}
   {:name "Typography"
    :components [{:color "text-green-600"
                  :type "Heading"
                  :icon lc/Type}
                 {:color "text-gray-500"
                  :type "Text"
                  :icon lc/Type}]}
   {:name "Form Elements"
    :components [{:color "text-red-500"
                  :type "Button"
                  :icon lc/MousePointer2}
                 {:color "text-orange-500"
                  :type "Input"
                  :icon lc/Type}
                 {:color "text-indigo-500"
                  :type "Checkbox"
                  :icon lc/CheckSquare}
                 {:color "text-teal-500"
                  :type "Switch"
                  :icon lc/ToggleLeft}]}])

(defn.js ComponentPalette
  [{:# [onAddComponent]}]
  (return
    [:div {:className "flex flex-col h-full bg-white border-r"}
      [:div {:className "px-4 py-2 bg-gray-200 border-b"}
        [:h2 {:className "text-sm"} "Tamagui Components"]]
      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "p-3 space-y-4"}
         (. -/componentGroups
            (map (fn [group]
                   (return
                    [:div {:key group.name}
                     [:h3 {:className "text-xs text-gray-500 mb-2 px-1"} group.name]
                     [:div {:className "space-y-1"}
                      (. group.components
                         (map (fn [component]
                                (var Icon component.icon)
                                (return
                                 [:% fg/Button
                                  {:key component.type
                                   :variant "ghost"
                                   :className "w-full justify-start h-auto py-2 px-2"
                                   :onClick (fn [] (return (onAddComponent component.type)))}
                                  [:% Icon {:className (+ "w-4 h-4 mr-2 " component.color)}]
                                  [:span {:className "text-sm"} component.type]]))))]
                     (:? (not= group (. componentGroups [(- componentGroups.length 1)]))
                         [:% fg/Separator {:className "mt-3"}]
                         nil)]))))]]
      [:div {:className "p-3 border-t bg-gray-50"}
        [:p {:className "text-xs text-gray-500"}
          "Click to add Tamagui components"]]]))
