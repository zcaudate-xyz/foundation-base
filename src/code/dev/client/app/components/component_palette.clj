(ns smalltalkinterfacedesign.components.component-palette
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(var componentGroups
  [(do {:name "Layout (Tamagui)"
        :components [(do {:type "View" :icon lc/Square :color "text-blue-500"})
                     (do {:type "XStack" :icon lc/AlignHorizontalSpaceAround :color "text-blue-600"})
                     (do {:type "YStack" :icon lc/AlignVerticalSpaceAround :color "text-blue-700"})
                     (do {:type "Card" :icon lc/Layers :color "text-purple-500"})]})
   (do {:name "Typography"
        :components [(do {:type "Heading" :icon lc/Type :color "text-green-600"})
                     (do {:type "Text" :icon lc/Type :color "text-gray-500"})]})
   (do {:name "Form Elements"
        :components [(do {:type "Button" :icon lc/MousePointer2 :color "text-red-500"})
                     (do {:type "Input" :icon lc/Type :color "text-orange-500"})
                     (do {:type "Checkbox" :icon lc/CheckSquare :color "text-indigo-500"})
                     (do {:type "Switch" :icon lc/ToggleLeft :color "text-teal-500"})]})])

(defn.js ComponentPalette [{:# [onAddComponent]}]
  (return
    [:div {:className "flex flex-col h-full bg-white border-r"}
      [:div {:className "px-4 py-2 bg-gray-200 border-b"}
        [:h2 {:className "text-sm"} "Tamagui Components"]]
      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "p-3 space-y-4"}
          (. componentGroups (map (fn [group]
                                    (return
                                      [:div {:key group.name}
                                        [:h3 {:className "text-xs text-gray-500 mb-2 px-1"} group.name]
                                        [:div {:className "space-y-1"}
                                          (. group.components (map (fn [component]
                                                                     (var Icon component.icon)
                                                                     (return
                                                                       [:% fg/Button
                                                                         {:key component.type
                                                                          :variant "ghost"
                                                                          :className "w-full justify-start h-auto py-2 px-2"
                                                                          :onClick (fn [] (return (onAddComponent component.type)))}
                                                                         [:% Icon {:className (+ "w-4 h-4 mr-2 " component.color)}]
                                                                         [:span {:className "text-sm"} component.type]]))))]
                                        (:? (!= group (. componentGroups [(- componentGroups.length 1)]))
                                            [:% fg/Separator {:className "mt-3"}]
                                            nil)]))))]]
      [:div {:className "p-3 border-t bg-gray-50"}
        [:p {:className "text-xs text-gray-500"}
          "Click to add Tamagui components"]]]))