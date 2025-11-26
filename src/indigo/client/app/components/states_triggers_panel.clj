(ns indigo.client.app.components.states-triggers-panel
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(defn.js StatesTab [{:# [component onUpdateStates]}]
  (var [newStateName setNewStateName] (r/useState ""))
  (var [newStateType setNewStateType] (r/useState "boolean"))

  (var handleAddState
       (fn []
         (when (not (. newStateName (trim)))
           (return))

         (var defaultValue (:? (== newStateType "boolean") false
                               (:? (== newStateType "number") 0
                                   (:? (== newStateType "array") []
                                       (:? (== newStateType "object") {} "")))))

         (var updatedStates
              (Object.assign {} (or component.states {})
                             {[newStateName] {:description ""
                                              :default defaultValue
                                              :type newStateType}}))

         (onUpdateStates component.id updatedStates)
         (setNewStateName "")
         (setNewStateType "boolean")))

  (var handleRemoveState
       (fn [stateName]
         (var updatedStates (Object.assign {} (or component.states {})))
         (del (. updatedStates [stateName]))
         (onUpdateStates component.id updatedStates)))

  (var handleUpdateStateDescription
       (fn [stateName description]
         (var updatedStates
              (Object.assign {} (or component.states {})
                             {[stateName] (Object.assign {} (. component.states [stateName])
                                                         {:description description})}))
         (onUpdateStates component.id updatedStates)))

  (var handleUpdateStateDefault
       (fn [stateName defaultValue]
         (var updatedStates
              (Object.assign {} (or component.states {})
                             {[stateName] (Object.assign {} (. component.states [stateName])
                                                         {:default defaultValue})}))
         (onUpdateStates component.id updatedStates)))

  (return
    [:% fg/ScrollArea {:className "h-full"}
      [:div {:className "p-4 space-y-4"}
        [:div {:className "p-3 bg-purple-950/30 border border-purple-900/50 rounded"}
          [:p {:className "text-xs text-purple-300 mb-1"} "💡 Component States"]
          [:p {:className "text-[10px] text-purple-400/80"}
            "Define reactive state variables. Use " [:code {:className "bg-purple-900/30 px-1 rounded"}
                                                     (:- "'{state.name}'")] " or "
            [:code {:className "bg-purple-900/30 px-1 rounded"}
             (:- "'$state.name'")]
           " in properties to bind to state values."]]

        [:div
          [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider mb-3"} "Defined States"]

          (:? (and component.states
                   (> (. (Object.keys component.states)
                         length) 0))
              [:div {:className "space-y-3"}
                (. (Object.entries component.states)
                   (map (fn [[stateName stateDef]]
                          (return
                           [:div {:key stateName :className "p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
                            [:div {:className "flex items-start justify-between mb-2"}
                             [:div {:className "flex-1"}
                              [:div {:className "flex items-center gap-2 mb-2"}
                               [:span {:className "text-xs font-mono text-purple-400"} stateName]
                               [:span {:className "text-xs text-gray-500"} (+ ": " stateDef.type)]]
                              [:% fg/Input
                               {:type "text"
                                :placeholder "Description (optional)"
                                :value (or stateDef.description "")
                                :onChange (fn [e] (return (handleUpdateStateDescription stateName e.target.value)))
                                :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2"}]
                              [:div
                               [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "Default Value"]
                               (:? (== stateDef.type "boolean")
                                   [:select
                                    {:value (String stateDef.default)
                                     :onChange (fn [e] (return (handleUpdateStateDefault stateName (== e.target.value "true"))))
                                     :className "w-full h-6 bg-[#252525] border border-[#3a3a3a] text-gray-300 text-xs rounded px-2"}
                                    [:option {:value "false"} "false"]
                                    [:option {:value "true"} "true"]]
                                   (:? (== stateDef.type "number")
                                       [:% fg/Input
                                        {:type "number"
                                         :value stateDef.default
                                         :onChange (fn [e] (return (handleUpdateStateDefault stateName (parseFloat e.target.value))))
                                         :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]
                                       [:% fg/Input
                                        {:type "text"
                                         :value (:? (== (typeof stateDef.default) "string") stateDef.default (JSON.stringify stateDef.default))
                                         :onChange (fn [e]
                                                     (try
                                                       (var parsed (JSON.parse e.target.value))
                                                       (handleUpdateStateDefault stateName parsed)
                                                       (catch _
                                                           (handleUpdateStateDefault stateName e.target.value))))
                                         :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]))]]]
                            [:% fg/Button
                             {:variant "ghost"
                              :size "sm"
                              :onClick (fn [] (return (handleRemoveState stateName)))
                              :className "h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"}
                             [:% lc/Trash2 {:className "w-3 h-3"}]]]))))]
              [:p {:className "text-xs text-gray-500 italic"} "No states defined"])

          [:div {:className "mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
            [:% fg/Label {:className "text-xs text-gray-400 mb-2 block"} "Add State"]
            [:div {:className "flex gap-2"}
              [:% fg/Input
                {:type "text"
                 :placeholder "State name"
                 :value newStateName
                 :onChange (fn [e] (return (setNewStateName e.target.value)))
                 :onKeyDown (fn [e] (return (:? (== e.key "Enter") (handleAddState) nil)))
                 :className "flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]
              [:% fg/Select {:value newStateType :onValueChange (fn [v] (return (setNewStateType v)))}
                [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                  [:% fg/SelectValue {}]]
                [:% fg/SelectContent {}
                  [:% fg/SelectItem {:value "boolean"} "boolean"]
                  [:% fg/SelectItem {:value "string"} "string"]
                  [:% fg/SelectItem {:value "number"} "number"]
                  [:% fg/SelectItem {:value "object"} "object"]
                  [:% fg/SelectItem {:value "array"} "array"]]]
              [:% fg/Button
                {:size "sm"
                 :onClick handleAddState
                 :className "h-7 px-3 bg-purple-600 hover:bg-purple-700 text-white"}
                [:% lc/Plus {:className "w-3 h-3"}]]]]]]]))

(defn.js TriggersTab
  [{:# [component onUpdateTriggers]}]
  (var [newTriggerName setNewTriggerName] (r/useState ""))
  (var [newTriggerEvent setNewTriggerEvent] (r/useState "click"))

  (var handleAddTrigger
       (fn []
         (when (not (. newTriggerName (trim)))
           (return))
                          
         (var updatedTriggers
              (Object.assign {} (or component.triggers {})
                             {[newTriggerName] {:description ""
                                                :event newTriggerEvent
                                                :action ""}}))

         (onUpdateTriggers component.id updatedTriggers)
         (setNewTriggerName "")
         (setNewTriggerEvent "click")))

  (var handleRemoveTrigger
       (fn [triggerName]
         (var updatedTriggers (Object.assign {} (or component.triggers {})))
         (del (. updatedTriggers [triggerName]))
         (onUpdateTriggers component.id updatedTriggers)))

  (var handleUpdateTrigger
       (fn [triggerName field value]
         (var updatedTriggers
              (Object.assign {} (or component.triggers {})
                             {[triggerName] (Object.assign {} (. component.triggers [triggerName])
                                                           {[field] value})}))
         (onUpdateTriggers component.id updatedTriggers)))

  (var availableActions
       (:? component.actions (Object.keys component.actions) []))

  (return
    [:% fg/ScrollArea {:className "h-full"}
      [:div {:className "p-4 space-y-4"}
        [:div {:className "p-3 bg-yellow-950/30 border border-yellow-900/50 rounded"}
          [:p {:className "text-xs text-yellow-300 mb-1"} "⚡ Event Triggers"]
          [:p {:className "text-[10px] text-yellow-400/80"}
            "Define event handlers that execute actions when events occur (click, change, submit, etc.)"]]

        [:div
          [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider mb-3"} "Defined Triggers"]

          (:? (and component.triggers
                   (> (. (Object.keys component.triggers)
                         length)
                      0))
              [:div {:className "space-y-3"}
               (. (Object.entries component.triggers)
                  (map (fn [[triggerName triggerDef]]
                         (return
                          [:div {:key triggerName :className "p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
                           [:div {:className "flex items-start justify-between mb-2"}
                            [:div {:className "flex-1"}
                             [:div {:className "flex items-center gap-2 mb-2"}
                              [:span {:className "text-xs font-mono text-yellow-400"} triggerName]
                              [:span {:className "text-xs text-gray-500"} (+ "on " triggerDef.event)]]
                             [:% fg/Input
                              {:type "text"
                               :placeholder "Description (optional)"
                               :value (or triggerDef.description "")
                               :onChange (fn [e] (return (handleUpdateTrigger triggerName "description" e.target.value)))
                               :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2"}]
                             [:div {:className "space-y-2"}
                              [:div
                               [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "Event"]
                               [:% fg/Select
                                {:value triggerDef.event
                                 :onValueChange (fn [v] (return (handleUpdateTrigger triggerName "event" v)))}
                                [:% fg/SelectTrigger {:className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                                 [:% fg/SelectValue {}]]
                                [:% fg/SelectContent {}
                                 [:% fg/SelectItem {:value "click"} "click"]
                                 [:% fg/SelectItem {:value "change"} "change"]
                                 [:% fg/SelectItem {:value "submit"} "submit"]
                                 [:% fg/SelectItem {:value "mouseenter"} "mouseenter"]
                                 [:% fg/SelectItem {:value "mouseleave"} "mouseleave"]
                                 [:% fg/SelectItem {:value "focus"} "focus"]
                                 [:% fg/SelectItem {:value "blur"} "blur"]]]]
                              [:div
                               [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "Action to Execute"]
                               [:% fg/Select
                                {:value triggerDef.action
                                 :onValueChange (fn [v] (return (handleUpdateTrigger triggerName "action" v)))}
                                [:% fg/SelectTrigger {:className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                                 [:% fg/SelectValue {:placeholder "Select action..."}]]
                                [:% fg/SelectContent {}
                                 (:? (> availableActions.length 0)
                                     (. availableActions (map (fn [actionName]
                                                                (return [:% fg/SelectItem {:key actionName :value actionName} actionName]))))
                                     [:% fg/SelectItem {:value "_none" :disabled true} "No actions defined"])]]]]]]
                           [:% fg/Button
                            {:variant "ghost"
                             :size "sm"
                             :onClick (fn [] (return (handleRemoveTrigger triggerName)))
                             :className "h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"}
                            [:% lc/Trash2 {:className "w-3 h-3"}]]]))))]
              [:p {:className "text-xs text-gray-500 italic"} "No triggers defined"])

          [:div {:className "mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
            [:% fg/Label {:className "text-xs text-gray-400 mb-2 block"} "Add Trigger"]
            [:div {:className "flex gap-2"}
              [:% fg/Input
                {:type "text"
                 :placeholder "Trigger name"
                 :value newTriggerName
                 :onChange (fn [e] (return (setNewTriggerName e.target.value)))
                 :onKeyDown (fn [e] (return (:? (== e.key "Enter") (handleAddTrigger) nil)))
                 :className "flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]
              [:% fg/Select {:value newTriggerEvent :onValueChange setNewTriggerEvent}
                [:% fg/SelectTrigger {:className "w-24 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                  [:% fg/SelectValue {}]]
                [:% fg/SelectContent {}
                  [:% fg/SelectItem {:value "click"} "click"]
                  [:% fg/SelectItem {:value "change"} "change"]
                  [:% fg/SelectItem {:value "submit"} "submit"]]]
              [:% fg/Button
                {:size "sm"
                 :onClick handleAddTrigger
                 :className "h-7 px-3 bg-yellow-600 hover:bg-yellow-700 text-white"}
               [:% lc/Plus {:className "w-3 h-3"}]]]]]]]))

(defn.js ActionsTab
  [{:# [component onUpdateActions]}]
  (var [newActionName setNewActionName] (r/useState ""))
  (var [newActionType setNewActionType] (r/useState "toggleState"))

  (var handleAddAction
       (fn []
         (when (not (. newActionName (trim)))
           (return))
         
         (var updatedActions
              (Object.assign {} (or component.actions {})
                             {[newActionName] {:description ""
                                               :script ""
                                               :value ""
                                               :type newActionType
                                               :target ""}}))
         
         (onUpdateActions component.id updatedActions)
         (setNewActionName "")
         (setNewActionType "toggleState")))

  (var handleRemoveAction
       (fn [actionName]
         (var updatedActions (Object.assign {} (or component.actions {})))
         (del (. updatedActions [actionName]))
         (onUpdateActions component.id updatedActions)))

  (var handleUpdateAction
       (fn [actionName field value]
         (var updatedActions
              (Object.assign {} (or component.actions {})
                             {[actionName] (Object.assign {} (. component.actions [actionName])
                                                          {[field] value})}))
         (onUpdateActions component.id updatedActions)))

  (var availableStates (:? component.states (Object.keys component.states) []))

  (return
   [:% fg/ScrollArea {:className "h-full"}
    [:div {:className "p-4 space-y-4"}
     [:div {:className "p-3 bg-green-950/30 border border-green-900/50 rounded"}
      [:p {:className "text-xs text-green-300 mb-1"} "🎬 Actions"]
      [:p {:className "text-[10px] text-green-400/80"}
       "Define actions that modify state or execute custom logic. Link actions to triggers."]]

     [:div
      [:h3 {:className "text-xs text-gray-500 uppercase tracking-wider mb-3"} "Defined Actions"]

      (:? (and component.actions
               (> (. (Object.keys component.actions)
                     length)
                  0))
          [:div {:className "space-y-3"}
           (. (Object.entries component.actions)
              (map (fn [[actionName actionDef]]
                     (return
                      [:div {:key actionName :className "p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
                       [:div {:className "flex items-start justify-between mb-2"}
                        [:div {:className "flex-1"}
                         [:div {:className "flex items-center gap-2 mb-2"}
                          [:span {:className "text-xs font-mono text-green-400"} actionName]
                          [:span {:className "text-xs text-gray-500"} actionDef.type]]
                         [:% fg/Input
                          {:type "text"
                           :placeholder "Description (optional)"
                           :value (or actionDef.description "")
                           :onChange (fn [e] (return (handleUpdateAction actionName "description" e.target.value)))
                           :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-400 text-xs mb-2"}]

                         (:? (or (== actionDef.type "setState") (== actionDef.type "toggleState") (== actionDef.type "incrementState"))
                             [:div {:className "mb-2"}
                              [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "Target State"]
                              [:% fg/Select
                               {:value (or actionDef.target "")
                                :onValueChange (fn [v] (return (handleUpdateAction actionName "target" v)))}
                               [:% fg/SelectTrigger {:className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
                                [:% fg/SelectValue {:placeholder "Select state..."}]]
                               [:% fg/SelectContent {}
                                (:? (> availableStates.length 0)
                                    (. availableStates (map (fn [stateName]
                                                              (return [:% fg/SelectItem {:key stateName :value stateName} stateName]))))
                                    [:% fg/SelectItem {:value "_none" :disabled true} "No states defined"])]]])
                         nil]

                        (:? (== actionDef.type "setState")
                            [:div
                             [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "Value"]
                             [:% fg/Input
                              {:type "text"
                               :placeholder "Value to set"
                               :value (or actionDef.value "")
                               :onChange (fn [e] (return (handleUpdateAction actionName "value" e.target.value)))
                               :className "h-6 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]]
                            nil)

                        (:? (== actionDef.type "customScript")
                            [:div
                             [:% fg/Label {:className "text-[10px] text-gray-500 mb-1 block"} "JavaScript Code"]
                             [:% fg/Textarea
                              {:placeholder "// Custom JavaScript code"
                               :value (or actionDef.script "")
                               :onChange (fn [e] (return (handleUpdateAction actionName "script" e.target.value)))
                               :className "min-h-[60px] bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs font-mono"}]]
                            nil)]]
                      [:% fg/Button
                       {:variant "ghost"
                        :size "sm"
                        :onClick (fn [] (return (handleRemoveAction actionName)))
                        :className "h-6 w-6 p-0 ml-2 text-gray-500 hover:text-red-400 hover:bg-red-950/20"}
                       [:% lc/Trash2 {:className "w-3 h-3"}]]))))])
      [:p {:className "text-xs text-gray-500 italic"} "No actions defined"]]

     [:div {:className "mt-3 p-3 bg-[#1e1e1e] rounded border border-[#3a3a3a]"}
      [:% fg/Label {:className "text-xs text-gray-400 mb-2 block"} "Add Action"]
      [:div {:className "flex gap-2"}
       [:% fg/Input
        {:type "text"
         :placeholder "Action name"
         :value newActionName
         :onChange (fn [e] (return (setNewActionName e.target.value)))
         :onKeyDown (fn [e] (return (:? (== e.key "Enter") (handleAddAction) nil)))
         :className "flex-1 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}]
       [:% fg/Select {:value newActionType :onValueChange (fn [v] (return (setNewActionType v)))}
        [:% fg/SelectTrigger {:className "w-32 h-7 bg-[#252525] border-[#3a3a3a] text-gray-300 text-xs"}
         [:% fg/SelectValue {}]]
        [:% fg/SelectContent {}
         [:% fg/SelectItem {:value "toggleState"} "Toggle State"]
         [:% fg/SelectItem {:value "setState"} "Set State"]
         [:% fg/SelectItem {:value "incrementState"} "Increment"]
         [:% fg/SelectItem {:value "customScript"} "Custom Script"]]]
       [:% fg/Button
        {:size "sm"
         :onClick handleAddAction
         :className "h-7 px-3 bg-green-600 hover:bg-green-700 text-white"}
        [:% lc/Plus {:className "w-3 h-3"}]]]]]]))

(defn.js StatesTriggersPanel
  [{:# [component onUpdateStates onUpdateTriggers onUpdateActions]}]
  (when (not component)
    (return
     [:div {:className "flex-1 flex items-center justify-center text-gray-500 text-sm"}
      "No component selected"]))

  (return
   [:% fg/Tabs {:defaultValue "states" :className "flex flex-col h-full"}
    [:div {:className "bg-[#2b2b2b] border-b border-[#323232]"}
     [:% fg/TabsList {:className "w-full justify-start rounded-none bg-transparent border-b-0 h-9"}
      [:% fg/TabsTrigger
       {:value "states"
        :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
       [:% lc/Database {:className "w-3 h-3 mr-1"}]
       "States"]
      [:% fg/TabsTrigger
       {:value "triggers"
        :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
       [:% lc/Zap {:className "w-3 h-3 mr-1"}]
       "Triggers"]
      [:% fg/TabsTrigger
       {:value "actions"
        :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
       "Actions"]]]

    [:% fg/TabsContent {:value "states" :className "flex-1 m-0 overflow-hidden"}
     [:% -/StatesTab {:component component :onUpdateStates onUpdateStates}]]

    [:% fg/TabsContent {:value "triggers" :className "flex-1 m-0 overflow-hidden"}
     [:% -/TriggersTab {:component component :onUpdateTriggers onUpdateTriggers}]]

    [:% fg/TabsContent {:value "actions" :className "flex-1 m-0 overflow-hidden"}
     [:% -/ActionsTab {:component component :onUpdateActions onUpdateActions}]]]))
