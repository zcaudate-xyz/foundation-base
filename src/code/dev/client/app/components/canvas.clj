(ns code.dev.client.app.components.canvas
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]]})

(defn.js Canvas
  [{:# [components
        selectedComponent
        onSelectComponent
        onAddComponent]}]

  (var renderComponent
       (fn [component]
         (var isSelected (== component.id selectedComponent))
         (var baseClasses
              (:? isSelected
                  "outline outline-2 outline-blue-500 outline-offset-2"
                  "hover:outline hover:outline-1 hover:outline-gray-300 hover:outline-offset-2"))

         (var handleClick
              (fn [e]
                (. e (stopPropagation))
                (onSelectComponent component.id)))

         (var renderChildren
              (fn []
                (if (== component.children.length 0)
                  (return nil)
                  (return
                   [:div {:className "space-y-2"}
                    (. component.children (map (fn [child]
                                                 (return [:div {:key child.id} (renderComponent child)]))))]))))

         (var commonProps {:onClick handleClick
                           :className (+ baseClasses " cursor-pointer transition-all")})

         (var textContent component.properties.children)

         (case component.type
           "View"
           (return
            [:div {:.. commonProps
                   :className (+ commonProps.className " p-4 border rounded")}
             [:div {:className "text-xs text-gray-400 mb-2"} component.type]
             (renderChildren)])

           "XStack"
           (return
            [:div {:.. commonProps
                   :className (+ commonProps.className " p-4 border rounded")}
             [:div {:className "text-xs text-gray-400 mb-2"} component.type]
             (renderChildren)])

           "YStack"
           (return
            [:div {:.. commonProps
                   :className (+ commonProps.className " p-4 border rounded")}
             [:div {:className "text-xs text-gray-400 mb-2"} component.type]
             (renderChildren)])

           "Card"
           (return
            [:% fg/Card {:.. commonProps}
             [:div {:className "text-xs text-gray-400 mb-2"} component.type]
             (renderChildren)])

           "Button"
           (return
            [:div {:.. commonProps}
             [:% fg/Button {:className "pointer-events-none"}
              (or textContent "Button")]])

           "Input"
           (return
            [:div {:.. commonProps}
             [:% fg/Input {:placeholder (or component.properties.placeholder "Enter text...")
                           :className "pointer-events-none"}]])

           "Checkbox"
           (return
            [:div {:.. commonProps}
             [:div {:className "flex items-center gap-2"}
              [:% fg/Checkbox {:className "pointer-events-none"}]
              [:label {:className "text-sm"} "Checkbox"]]])

           "Switch"
           (return
            [:div {:.. commonProps}
             [:div {:className "flex items-center gap-2"}
              [:% fg/Switch {:className "pointer-events-none"}]
              [:label {:className "text-sm"} "Switch"]]])

           "Text"
           (return
            [:p {:.. commonProps}
             (or textContent "Text content")])

           "Heading"
           (return
            [:h2 {:.. commonProps}
             (or textContent "Heading")])

           :else
           (return
            [:div {:.. commonProps}
             [:span {:className "text-gray-500"} component.type]]))))

  (return
   [:div {:className "flex flex-col h-full bg-white"}
    [:div {:className "px-4 py-2 bg-gray-200 border-b flex items-center justify-between"}
     [:h2 {:className "text-sm"} "Canvas (Preview)"]
     [:div {:className "flex items-center gap-2"}
      [:span {:className "text-xs text-gray-500"} "Tamagui Preview"]]]

    [:% fg/ScrollArea {:className "flex-1"}
     [:div {:className "p-8 min-h-full bg-gray-50"}
      [:div {:className "bg-white rounded-lg shadow-sm p-8 min-h-[600px]"}
       (. components
          (map (fn [component]
                 (return [:div {:key component.id} (renderComponent component)]))))]]]]))
