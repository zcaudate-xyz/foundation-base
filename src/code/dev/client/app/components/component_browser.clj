(ns code.dev.client.app.components.component-browser
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.react-dnd :as dnd]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(def.js primitiveComponents
  [{:type "Container" :icon lc/Box :description "Basic container element"}
   {:type "FlexRow" :icon lc/Rows3 :description "Horizontal flex container"}
   {:type "FlexCol" :icon lc/Columns3 :description "Vertical flex container"}
   {:type "Card" :icon lc/CreditCard :description "Card component"}
   {:type "Button" :icon lc/Square :description "Button element"}
   {:type "Input" :icon lc/Type :description "Text input field"}
   {:type "Text" :icon lc/Type :description "Text element"}
   {:type "Heading" :icon lc/Type :description "Heading element"}
   {:type "Switch" :icon lc/ToggleLeft :description "Toggle switch"}
   {:type "Checkbox" :icon lc/CheckSquare :description "Checkbox input"}])

(defn.js DraggableComponentItem
  [{:# [type icon description onAddComponent]}]
  (var Icon icon)
  (var [isDragging drag]
    (dnd/useDrag
     (fn []
       (return {:type "COMPONENT"
                :item {:componentType type}
                :collect (fn [monitor]
                           (return {:isDragging (. monitor (isDragging))}))}))))

  (return
   [:div {:ref drag
          :className (+ "group flex items-center gap-2 px-3 py-2 rounded cursor-grab hover:bg-[#323232] transition-colors "
                        (:? isDragging "opacity-50 cursor-grabbing" ""))
          :onDoubleClick (fn [] (return (onAddComponent type)))
          :title "Double-click to add or drag to canvas"}
    [:% Icon {:className "w-4 h-4 text-gray-500 group-hover:text-gray-300"}]
    [:div {:className "flex-1"}
     [:div {:className "text-xs text-gray-300 group-hover:text-gray-100"} type]
     [:div {:className "text-[10px] text-gray-600 group-hover:text-gray-500"} description]]]))

(defn.js ComponentBrowser
  [{:# [onAddComponent]}]
  (return
   [:div {:className "flex flex-col h-full bg-[#252525]"}
    [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3"}
     [:span {:className "text-xs text-gray-400"} "Primitives"]]
    [:% fg/ScrollArea {:className "flex-1"}
     [:div {:className "p-3 space-y-1"}
      (. primitiveComponents
         (map (fn [comp]
                (return
                 [:% -/DraggableComponentItem
                  {:key comp.type
                   :type comp.type
                   :icon comp.icon
                   :description comp.description
                   :onAddComponent onAddComponent}]))))]]]))

