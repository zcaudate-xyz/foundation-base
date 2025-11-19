(ns smalltalkinterfacedesign.components.properties-panel
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [smalltalkinterfacedesign.app :as app]
             [smalltalkinterfacedesign.components.inputs-panel :as ip]
             [smalltalkinterfacedesign.components.states-triggers-panel :as stp]]})

(def.js PropertyInput
  (r/memo (fn [{:# [componentId propertyKey value onUpdateProperty]}]
            (if (== (typeof value) "boolean")
              (return
                [:input
                  {:type "checkbox"
                   :checked value
                   :onChange (fn [e] (return (onUpdateProperty componentId propertyKey e.target.checked)))
                   :className "h-4 w-4"}]))

            (if (and (== (typeof value) "string") (> (. value length) 50))
              (return
                [:% fg/Textarea
                  {:value value
                   :onChange (fn [e] (return (onUpdateProperty componentId propertyKey e.target.value)))
                   :className "min-h-[80px] bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]))

            (return
              [:% fg/Input
                {:type "text"
                 :value (or value "")
                 :onChange (fn [e] (return (onUpdateProperty componentId propertyKey e.target.value)))
                 :className "h-8 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]))))

(defn.js PropertiesPanel [{:# [component onUpdateProperty onDeleteComponent onUpdateInputs onUpdateInputValues onUpdateStates onUpdateTriggers onUpdateActions]}]
  (when (not component)
    (return
      [:div {:className "flex flex-col h-full bg-[#252525]"}
        [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3"}
          [:h2 {:className "text-xs text-gray-400 uppercase tracking-wide"} "Properties"]]
        [:div {:className "flex-1 flex items-center justify-center"}
          [:p {:className "text-xs text-gray-600"} "No component selected"]]]))

  (var renderPropertyEditor (fn [key value]
                              (var handleChange (fn [newValue]
                                                  (onUpdateProperty component.id key newValue)))

                              (if (== key "className")
                                (return
                                  [:% fg/Textarea
                                    {:value (or value "")
                                     :onChange (fn [e] (return (handleChange e.target.value)))
                                     :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-xs text-gray-300 font-mono"
                                     :placeholder "e.g. px-4 py-2 bg-blue-500"}]))

                              (return
                                [:% fg/Input
                                  {:value (or value "")
                                   :onChange (fn [e] (return (handleChange e.target.value)))
                                   :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-xs text-gray-300"}]))))

  (return
    [:div {:className "flex flex-col h-full bg-[#252525]"}
      [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3 justify-between"}
        [:span {:className "text-xs text-gray-400"} "Properties"]
        (:? (!= component.id "root")
            [:% fg/Button
              {:variant "ghost"
               :size "sm"
               :onClick (fn [] (return (onDeleteComponent component.id)))
               :className "h-6 px-2 text-xs text-gray-400 hover:text-red-400 hover:bg-red-950/20"}
              [:% lc/Trash2 {:className "w-3 h-3 mr-1"}]
              "Delete"]
            nil)]

      [:% fg/Tabs {:defaultValue "properties" :className "flex flex-col flex-1"}
        [:div {:className "bg-[#2b2b2b] border-b border-[#323232]"}
          [:% fg/TabsList {:className "w-full justify-start rounded-none bg-transparent border-b-0 h-9"}
            [:% fg/TabsTrigger
              {:value "properties"
               :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
              "Properties"]
            [:% fg/TabsTrigger
              {:value "inputs"
               :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
              "Inputs"]
            [:% fg/TabsTrigger
              {:value "state"
               :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
              "State & Events"]]]

        [:% fg/TabsContent {:value "properties" :className "flex-1 m-0"}
          [:% fg/ScrollArea {:className "h-full"}
            [:div {:className "p-4 space-y-4"}
              [:div
                [:% fg/Label {:className "text-xs text-gray-500 uppercase tracking-wider"} "Component"]
                [:div {:className "mt-1 text-sm text-gray-300"} component.type]
                (:? component.libraryRef
                    [:div {:className "mt-1 text-xs text-blue-400 font-mono"} component.libraryRef]
                    nil)]

              [:div
                [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"} "Label"]
                [:% fg/Input
                  {:value (or component.label "")
                   :onChange (fn [e] (return (onUpdateProperty component.id "label" e.target.value)))
                   :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-xs text-gray-300"
                   :placeholder (+ component.type " label...")}]
                [:p {:className "text-[10px] text-gray-500 mt-1"} "Custom label shown in viewport and outliner"]]

              [:div {:className "h-[1px] bg-[#323232]"}]

              [:div
                [:% fg/Label {:className "text-xs text-gray-500 uppercase tracking-wider mb-3 block"}
                  "Properties"]
                [:div {:className "space-y-3"}
                  (. (Object.entries component.properties) (map (fn [[key value]]
                                                                  (return
                                                                    [:div {:key (+ component.id "-" key)}
                                                                      [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"}
                                                                        key
                                                                        (:? (and component.inputs (> (Object.keys component.inputs).length 0))
                                                                            [:span {:className "ml-2 text-[10px] text-blue-400 font-mono"} "{input.name}"]
                                                                            nil)
                                                                        (:? (and component.states (> (Object.keys component.states).length 0))
                                                                            [:span {:className "ml-2 text-[10px] text-purple-400 font-mono"} "{state.name}"]
                                                                            nil)]
                                                                      [:% -/PropertyInput
                                                                        {:componentId component.id
                                                                         :propertyKey key
                                                                         :value value
                                                                         :onUpdateProperty onUpdateProperty}]]))))]]]]

        [:% fg/TabsContent {:value "inputs" :className "flex-1 m-0"}
          [:% ip/InputsPanel
            {:component component
             :onUpdateInputs onUpdateInputs
             :onUpdateInputValues onUpdateInputValues}]]

        [:% fg/TabsContent {:value "state" :className "flex-1 m-0"}
          [:% stp/StatesTriggersPanel
            {:component component
             :onUpdateStates onUpdateStates
             :onUpdateTriggers onUpdateTriggers
             :onUpdateActions onUpdateActions}]]]]]))