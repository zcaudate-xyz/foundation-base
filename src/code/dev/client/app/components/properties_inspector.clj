(ns smalltalkinterfacedesign.components.properties-inspector
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [smalltalkinterfacedesign.app :as app]]})

(defn.js PropertiesInspector [{:# [component onUpdateProperty onDeleteComponent]}]
  (when (not component)
    (return
      [:div {:className "flex flex-col h-full bg-white border-l"}
        [:div {:className "px-4 py-2 bg-gray-200 border-b"}
          [:h2 {:className "text-sm"} "Properties"]]
        [:div {:className "flex-1 flex items-center justify-center"}
          [:p {:className "text-sm text-gray-400"} "No component selected"]]]))

  (var renderPropertyEditor (fn [key value]
                              (var handleChange (fn [newValue]
                                                  (onUpdateProperty component.id key newValue)))

                              (if (== key "size")
                                (return
                                  [:% fg/Select {:value value :onValueChange handleChange}
                                    [:% fg/SelectTrigger {}
                                      [:% fg/SelectValue {}]]
                                    [:% fg/SelectContent {}
                                      [:% fg/SelectItem {:value "$1"} "$1 (xs)"]
                                      [:% fg/SelectItem {:value "$2"} "$2 (sm)"]
                                      [:% fg/SelectItem {:value "$3"} "$3 (md)"]
                                      [:% fg/SelectItem {:value "$4"} "$4 (lg)"]
                                      [:% fg/SelectItem {:value "$5"} "$5 (xl)"]
                                      [:% fg/SelectItem {:value "$6"} "$6 (2xl)"]]])))

                              (if (or (== key "padding") (== key "margin") (== key "gap"))
                                (return
                                  [:% fg/Select {:value value :onValueChange handleChange}
                                    [:% fg/SelectTrigger {}
                                      [:% fg/SelectValue {}]]
                                    [:% fg/SelectContent {}
                                      [:% fg/SelectItem {:value "$0"} "$0"]
                                      [:% fg/SelectItem {:value "$1"} "$1"]
                                      [:% fg/SelectItem {:value "$2"} "$2"]
                                      [:% fg/SelectItem {:value "$3"} "$3"]
                                      [:% fg/SelectItem {:value "$4"} "$4"]
                                      [:% fg/SelectItem {:value "$5"} "$5"]
                                      [:% fg/SelectItem {:value "$6"} "$6"]
                                      [:% fg/SelectItem {:value "$8"} "$8"]]])))

                              (if (or (== key "backgroundColor") (== key "color"))
                                (return
                                  [:% fg/Select {:value value :onValueChange handleChange}
                                    [:% fg/SelectTrigger {}
                                      [:% fg/SelectValue {}]]
                                    [:% fg/SelectContent {}
                                      [:% fg/SelectItem {:value "$background"} "$background"]
                                      [:% fg/SelectItem {:value "$color"} "$color"]
                                      [:% fg/SelectItem {:value "$borderColor"} "$borderColor"]
                                      [:% fg/SelectItem {:value "$blue9"} "$blue9"]
                                      [:% fg/SelectItem {:value "$red9"} "$red9"]
                                      [:% fg/SelectItem {:value "$green9"} "$green9"]
                                      [:% fg/SelectItem {:value "$gray1"} "$gray1"]]]))

                              (return
                                [:% fg/Input
                                  {:value (or value "")
                                   :onChange (fn [e] (return (handleChange e.target.value)))
                                   :className "text-sm"}])

  (return
    [:div {:className "flex flex-col h-full bg-white border-l"}
      [:div {:className "px-4 py-2 bg-gray-200 border-b"}
        [:h2 {:className "text-sm"} "Properties"]]

      [:div {:className "p-4 border-b bg-gray-50"}
        [:div {:className "flex items-center justify-between mb-2"}
          [:div
            [:p {:className "text-sm"} (+ "tm/" component.type)]
            [:p {:className "text-xs text-gray-500"} (+ "#" component.id)]]
          (:? (!= component.id "root")
              [:% fg/Button
                {:size "sm"
                 :variant "ghost"
                 :onClick (fn [] (return (onDeleteComponent component.id)))}
                [:% lc/Trash2 {:className "w-4 h-4 text-red-500"}]]
              nil)]]

      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "p-4 space-y-4"}
          [:div
            [:h3 {:className "text-xs text-gray-500 mb-3"} "Tamagui Properties"]
            [:div {:className "space-y-3"}
              (. (Object.entries component.properties) (map (fn [[key value]]
                                                              (return
                                                                [:div {:key key :className "space-y-1"}
                                                                  [:% fg/Label {:className "text-xs capitalize"}
                                                                    (. (. (. key (replace #"\B([A-Z])" " $1")) (trim)))]
                                                                  (renderPropertyEditor key value)]))))]]]

          [:% fg/Separator {}]

          [:div
            [:h3 {:className "text-xs text-gray-500 mb-2"} "Component Info"]
            [:div {:className "text-xs space-y-1 text-gray-600"}
              [:p (+ "Children: " component.children.length)]
              [:p (+ "Type: Tamagui " component.type)]]]]]))