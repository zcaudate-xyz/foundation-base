(ns code.dev.client.app.components.inputs-panel
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [code.dev.client.app.app :as app]]})

(defn.js InputsPanel [{:# [component onUpdateInputs onUpdateInputValues]}]
  (var [newInputName setNewInputName] (r/useState ""))
  (var [newInputType setNewInputType] (r/useState "string"))

  (when (not component)
    (return
      [:div {:className "flex-1 flex items-center justify-center text-gray-500 text-sm"}
        "No component selected"]))

  (var handleAddInput (fn []
                        (when (not (. newInputName (trim)))
                          (return))

                        (var updatedInputs
                          (Object.assign {} (or component.inputs {})
                            {(newInputName) (do {:type newInputType
                                                 :description ""})}))

                        (onUpdateInputs component.id updatedInputs)
                        (setNewInputName "")
                        (setNewInputType "string")))

  (var handleRemoveInput (fn [inputName]
                           (var updatedInputs (Object.assign {} (or component.inputs {})))
                           (del (. updatedInputs [inputName]))
                           (onUpdateInputs component.id updatedInputs)

                           (when component.inputValues
                             (var updatedValues (Object.assign {} component.inputValues))
                             (del (. updatedValues [inputName]))
                             (onUpdateInputValues component.id updatedValues))))

  (var handleUpdateInputDescription (fn [inputName description]
                                      (var updatedInputs
                                        (Object.assign {} (or component.inputs {})
                                          {(inputName) (Object.assign {} (. component.inputs [inputName])
                                             {:description description})}))
                                      (onUpdateInputs component.id updatedInputs)))

  (var handleUpdateInputValue (fn [inputName value]
                                (var updatedValues
                                  (Object.assign {} (or component.inputValues {})
                                    {(inputName) value}))
                                (onUpdateInputValues component.id updatedValues)))

  (var renderInputValueEditor (fn [inputName inputDef]
                                (var currentValue (or (or (. component.inputValues [inputName]) inputDef.default) ""))

                                (case inputDef.type
                                  "boolean"
                                  (return
                                    [:% fg/Switch
                                      {:checked currentValue
                                       :onCheckedChange (fn [checked] (return (handleUpdateInputValue inputName checked)))}])
                                  "number"
                                  (return
                                    [:% fg/Input
                                      {:type "number"
                                       :value currentValue
                                       :onChange (fn [e] (return (handleUpdateInputValue inputName (parseFloat e.target.value))))
                                       :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}])
                                  (or "object" "array")
                                  (return
                                    [:% fg/Input
                                      {:type "text"
                                       :value (:? (== (typeof currentValue) "string") currentValue (JSON.stringify currentValue))
                                       :onChange (fn [e]
                                                   (try
                                                     (var parsed (JSON.parse e.target.value))
                                                     (handleUpdateInputValue inputName parsed)
                                                     (catch _
                                                       (handleUpdateInputValue inputName e.target.value))))
                                       :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs font-mono"
                                       :placeholder (:? (== inputDef.type "array") "[]" "{}")}])
                                  :else ;; string
                                  (return
                                    [:% fg/Input
                                      {:type "text"
                                       :value currentValue
                                       :onChange (fn [e] (return (handleUpdateInputValue inputName e.target.value)))
                                       :className "h-7 bg-[#1e1e1e] border-[#3a3a3a] text-gray-300 text-xs"}]))))

  (return
    [:div {:className "flex flex-col h-full"}
      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "p-4 space-y-4"}
          [:div {:className "p-3 bg-blue-950/30 border border-blue-900/50 rounded"}
            [:p {:className "text-xs text-blue-300 mb-1"} "💡 Input Binding"]
            [:p {:className "text-[10px] text-blue-400/80"}
              "Define inputs here, then use " [:code {:className "bg-blue-900/30 px-1 rounded"} "{input.name}"] " or "
              [:code {:className "bg-blue-900/30 px-1 rounded"} "$input.name"] " in properties to bind values."]]

          [:div
            [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider mb-3"} "Input Schema"]

            (:? (and component.inputs (> (Object.keys component.inputs).length 0))
                [:div {:className "space-y-3"}
                  (. (Object.entries component.inputs) (map (fn [[inputName inputDef]]
                                                              (return
                                                                [:div {:key inputName :className "p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
                                                                  [:div {:className "flex items-start justify-between mb-2"}
                                                                    [:div {:className "flex-1"}
                                                                      [:div {:className "flex items-center gap-2 mb-1"}
                                                                        [:span {:className "text-xs font-mono text-blue-400"} inputName]
                                                                        [:span {:className "text-xs text-gray-500"} (+ ": " inputDef.type)]]
                                                                      [:% fg/Input
                                                                        {:type "text"
                                                                         :placeholder "Description (optional)"
                                                                         :value (or inputDef.description "")
                                                                         :onChange (fn [e] (return (handleUpdateInputDescription inputName e.target.value)))
                                                                         :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs"}]]
                                                                    [:% fg/Button
                                                                      {:variant "ghost"
                                                                       :size "sm"
                                                                       :onClick (fn [] (return (handleRemoveInput inputName)))
                                                                       :className "h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"}
                                                                      [:% lc/Trash2 {:className "w-3 h-3"}]]]]))))]
                [:p {:className "text-xs text-gray-500 italic"} "No inputs defined"])]

            [:div {:className "mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
              [:% fg/Label {:className "text-xs text-gray-400 mb-2 block"} "Add Input"]
              [:div {:className "flex gap-2"}
                [:% fg/Input
                  {:type "text"
                   :placeholder "Input name"
                   :value newInputName
                   :onChange (fn [e] (return (setNewInputName e.target.value)))
                   :onKeyDown (fn [e] (return (:? (== e.key "Enter") (handleAddInput) nil)))
                   :className "flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]
                [:% fg/Select {:value newInputType :onValueChange (fn [v] (return (setNewInputType v)))}
                  [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                    [:% fg/SelectValue {}]]
                  [:% fg/SelectContent {}
                    [:% fg/SelectItem {:value "string"} "string"]
                    [:% fg/SelectItem {:value "number"} "number"]
                    [:% fg/SelectItem {:value "boolean"} "boolean"]
                    [:% fg/SelectItem {:value "object"} "object"]
                    [:% fg/SelectItem {:value "array"} "array"]]]
                [:% fg/Button
                  {:size "sm"
                   :onClick handleAddInput
                   :className "h-7 px-3 bg-blue-600 hover:bg-blue-700 text-white"}
                  [:% lc/Plus {:className "w-3 h-3"}]]]]]]

          (:? (and component.inputs (> (Object.keys component.inputs).length 0))
              [:<>
                [:div {:className "h-[1px] bg-[#323232]"}]
                [:div
                  [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider mb-3"} "Input Values"]
                  [:div {:className "space-y-3"}
                    (. (Object.entries component.inputs) (map (fn [[inputName inputDef]]
                                                                (return
                                                                  [:div {:key inputName}
                                                                    [:% fg/Label {:className "text-xs text-gray-400 mb-1 block"}
                                                                      inputName
                                                                      (:? inputDef.description
                                                                          [:span {:className "text-gray-600 ml-2"} (+ "- " inputDef.description)]
                                                                          nil)]
                                                                    (renderInputValueEditor inputName inputDef)]))))]]]
              nil)]))