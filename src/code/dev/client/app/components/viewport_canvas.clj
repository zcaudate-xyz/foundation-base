(ns code.dev.client.app.components.viewport-canvas
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.react-dnd :as dnd] ;; useDrop, useDrag
             [js.lib.figma :as fg]
             [code.dev.client.app.components.theme-editor :as te]
             [code.dev.client.app.components.component-renderer :as cr]]
   :import  []}

  ;; Helper function to count components
  (var countComponents
    (fn [comp]
      (return (+ 1 (. comp.children (reduce (fn [acc child] (return (+ acc (-/countComponents child)))) 0)))))

  ;; Convert component tree to JSON for code view
  (var componentToJSON
    (fn [component]
      (when (not component) (return nil))

      (if component.libraryRef
        (return
         {:type component.libraryRef
          :props component.properties
          :inputs (or component.inputValues nil)})
        (return
         (var result
           {:type component.type
            :props component.properties})

         (when (and component.inputs (> (. (Object.keys component.inputs) length) 0))
           (:= result.inputSchema component.inputs))

         (when (and component.inputValues (> (. (Object.keys component.inputValues) length) 0))
           (:= result.inputs component.inputValues))

         (when (and component.states (> (. (Object.keys component.states) length) 0))
           (:= result.states component.states))

         (when (and component.triggers (> (. (Object.keys component.triggers) length) 0))
           (:= result.triggers component.triggers))

         (when (and component.actions (> (. (Object.keys component.actions) length) 0))
           (:= result.actions component.actions))

         (when (> component.children.length 0)
           (:= result.children (. component.children (map -/componentToJSON))))

         (return result))))

  (defn.js ViewportCanvas
    [{:# [components
          selectedComponent
          onSelectComponent
          onAddComponent
          onMoveComponent
          viewMode
          theme]}]
    ;; State management for all components
    (var [componentStates setComponentStates] (r/useState {}))

    ;; Initialize states from component definitions
    (r/useEffect
     (fn []
       (var initStates {})
       (var collectStates
         (fn [comp]
           (when (and comp.states (> (. (Object.keys comp.states) length) 0))
             (:= (. initStates [comp.id]) {})
             (. (Object.entries comp.states) (forEach (fn [[stateName stateDef]]
                                                        (:= (. (. initStates [comp.id]) [stateName]) stateDef.default)))))
           (. comp.children (forEach collectStates))))

       (. components (forEach collectStates))
       (setComponentStates initStates))
     [components])

    ;; Function to update state for a component
    (var updateState
      (fn [componentId stateName value]
        (setComponentStates (fn [prev]
                              (return
                               {:.. prev
                                componentId {:.. (or (. prev [componentId]) {})
                                             stateName value}})))))

    ;; Function to execute an action
    (var executeAction
      (fn [component actionName]
        (when (or (not component.actions) (not (. component.actions [actionName]))) (return))

        (var action (. component.actions [actionName]))
        (var currentStates (or (. componentStates [component.id]) {}))

        (case action.type
          "toggleState"
          (when action.target
            (var currentValue (. currentStates [action.target]))
            (updateState component.id action.target (not currentValue)))

          "setState"
          (when (and action.target (not= action.value undefined))
            (var parsedValue action.value)
            (try
              (when (and (== (typeof action.value) "string") (or (== action.value "true") (== action.value "false")))
                (:= parsedValue (== action.value "true")))
              (when (and (== (typeof action.value) "string") (not (isNaN (Number action.value))))
                (:= parsedValue (Number action.value)))
              (catch e))
            (updateState component.id action.target parsedValue))

          "incrementState"
          (when action.target
            (var currentValue (. currentStates [action.target]))
            (when (== (typeof currentValue) "number")
              (updateState component.id action.target (+ currentValue 1))))

          "customScript"
          (when action.script
            (try
              (var context
                {:state currentStates
                 :setState (fn [stateName value] (return (updateState component.id stateName value)))})
              (var func (new Function "state" "setState" action.script))
              (func context.state context.setState)
              (catch error
                (console.error "Error executing custom script:" error)))))))

    ;; Generate theme CSS variables
    (var generateThemeStyles
      (fn []
        (when (not theme) (return {}))

        (return
          {:"--color-primary" theme.colors.primary
           :"--color-secondary" theme.colors.secondary
           :"--color-accent" theme.colors.accent
           :"--color-background" theme.colors.background
           :"--color-text" theme.colors.text})))
    
    (return
     [:div {:className "flex flex-col h-full bg-[#1a1a1a]"}
      ;; Top Bar
      [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-4 justify-between"}
       [:span {:className "text-xs text-gray-400"} "Viewport"]
       [:div {:className "flex items-center gap-2"}
        [:button {:className "px-2 py-1 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232] rounded"}
         "Desktop"]
        [:button {:className "px-2 py-1 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232] rounded"}
         "Tablet"]
        [:button {:className "px-2 py-1 text-xs text-gray-400 hover:text-gray-200 hover:bg-[#323232] rounded"}
         "Mobile"]]]

      ;; Canvas
      [:div
       {:className "flex-1 overflow-auto p-8"
        :style (Object.assign
                {:background "radial-gradient(circle at 20px 20px, #2a2a2a 1px, transparent 1px)"
                 :backgroundSize "40px 40px"}
                (generateThemeStyles))}
       [:div {:className "min-h-full"}
        (. components (map (fn [component]
                             (return
                              [:% cr/ComponentRenderer
                               {:key component.id
                                :component component
                                :selectedComponent selectedComponent
                                :onSelectComponent onSelectComponent
                                :onDropComponent onAddComponent
                                :theme theme
                                :componentStates componentStates
                                :executeAction executeAction}]))))]]

      ;; Code View Toggle
      [:div {:className "h-10 bg-[#2b2b2b] border-t border-[#323232] flex items-center px-3 justify-between"}
       [:div {:className "flex items-center gap-2"}
        [:button
         {:className "px-3 py-1 text-xs bg-[#323232] text-gray-300 rounded hover:bg-[#3a3a3a]"
          :onClick (fn []
                     (var codeView (document.getElementById "code-view"))
                     (when codeView
                       (:= codeView.style.display (:? (=== codeView.style.display "none") "block" "none"))))}
         "Toggle Code"]]
       [:span {:className "text-xs text-gray-600"}
        (+ (-/countComponents components.0) " components")]] ;; components.0 assumed to be root

      ;; Code View Panel
      [:div
       {:id "code-view"
        :style {:display "none"}
        :className "h-64 bg-[#1e1e1e] border-t border-[#323232] overflow-auto"}
       [:pre {:className "p-4 text-xs text-gray-300 font-mono"}
        (JSON.stringify (-/componentToJSON components.0) nil 2)]]]]))
