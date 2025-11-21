(ns code.dev.client.app.components.component-renderer
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]]})

;; Interface definitions are removed as per spec
;; interface ComponentRendererProps { ... }

(defn.js resolveInputBindings
  [value (:= inputValues {})]
  (when (not (== (typeof value) "string"))
    (return value))

  (var resolved value)

  (var bracketPattern (:- "/\\{inputs?\\.(\\w+)\\}/g"))
  (:= resolved (. resolved
                  (replace bracketPattern
                           (fn [match inputName]
                             (return (:? (not= (. inputValues [inputName])
                                               undefined)
                                         (String (. inputValues [inputName]))
                                         match))))))

  (var dollarPattern (:- "/\\{$inputs?\\.(\\w+)\\}/g"))
  (:= resolved (. resolved
                  (replace dollarPattern
                           (fn [match inputName]
                             (return (:? (not= (. inputValues [inputName])
                                               undefined)
                                         (String (. inputValues [inputName]))
                                         match))))))
  (return resolved))

(defn.js resolveStateBindings
  [value (:= stateValues {})]
  (when (not (== (typeof value) "string"))
    (return value))

  (var resolved value)

  (var bracketPattern (:- "/\\{states?\\.(\\w+)\\}/g"))
  (:= resolved (. resolved
                  (replace bracketPattern
                           (fn [match stateName]
                             (return (:? (not= (. stateValues [stateName]) undefined)
                                         (String (. stateValues [stateName]))
                                         match))))))

  (var dollarPattern (:- "/\\{$states?\\.(\\w+)\\}/g"))
  (:= resolved (. resolved
                  (replace dollarPattern
                           (fn [match stateName]
                             (return (:? (not= (. stateValues [stateName]) undefined)
                                         (String (. stateValues [stateName]))
                                         match))))))
  (return resolved))



(defn.js ComponentRenderer
  [{:# [component
        selectedComponent
        onSelectComponent
        onDropComponent
        theme
        componentStates
        executeAction]}]

  (var resolveComponentBindings
       (fn [comp (:= parentInputs nil)]
         
         (var inputsToUse (or comp.inputValues parentInputs {}))
         (var statesToUse (or (. componentStates [comp.id]) {}))

         (var resolvedProperties {:.. comp.properties})
         (. (Object.keys resolvedProperties)
            (forEach (fn [key]
                       (:= (. resolvedProperties [key])
                           (-/resolveInputBindings (. resolvedProperties [key]) inputsToUse))
                       (:= (. resolvedProperties [key])
                           (-/resolveStateBindings (. resolvedProperties [key]) statesToUse)))))
         
         (return
          {:.. comp
           :properties resolvedProperties
           :children (. comp.children
                        (map (fn [child] (return (resolveComponentBindings child inputsToUse)))))})))

  (var resolvedComponent (resolveComponentBindings component))
  
  (var isSelected (=== component.id selectedComponent))

  (var resolvedProperties resolvedComponent.properties)
  (var textContent resolvedProperties.children)

  (var displayLabel (or component.label component.type))

  ;; Create event handlers from triggers
  (var createEventHandlers
       (fn []
         (var handlers {})
         (when (and component.triggers component.actions)
           (. (Object.entries component.triggers)
              (forEach (fn [[triggerName triggerDef]]
                         (var eventName (+ "on"
                                           (. triggerDef.event (charAt 0) (toUpperCase))
                                           (. triggerDef.event (slice 1))))
                         (:= (. handlers [eventName])
                             (fn [e]
                               (. e (stopPropagation))
                               (when triggerDef.action
                                 (executeAction component triggerDef.action))))))))
         (return handlers)))
  
  (var eventHandlers (createEventHandlers))

  (var renderChildren
       (fn []
         (. component.children
            (map (fn [child]
                   (return
                    [:% -/ComponentRenderer
                     {:key child.id
                      :component child
                      :selectedComponent selectedComponent
                      :onSelectComponent onSelectComponent
                      :onDropComponent onDropComponent
                      :theme theme
                      :componentStates componentStates
                      :executeAction executeAction}]))))))

  (var commonProps
       {:onClick (fn [e]
                   (. e (stopPropagation))
                   (onSelectComponent component.id))
        :className (:? isSelected "ring-2 ring-blue-500" "")})

  (var allProps
       {:.. [commonProps
             eventHandlers]})

  (case component.type
    "Container" nil
    "FlexRow"   nil
    "FlexCol"   
    (return
     [:div {:.. allProps
            :className (+ allProps.className " " (or resolvedProperties.className "p-4 border border-gray-700 rounded bg-[#2b2b2b]"))}
      [:div {:className "text-[10px] text-gray-500 mb-2"} displayLabel]
      (renderChildren)
      (:? (== component.children.length 0)
          [:div {:className "text-[10px] text-gray-600 italic py-2"} "Drop components here"]
          nil)])

    "Card"
    (return
     [:div {:.. allProps
            :className (+ allProps.className " " (or resolvedProperties.className "p-6 bg-white rounded-lg shadow border-gray-700"))}
      [:div {:className "text-[10px] text-gray-500 mb-2"} displayLabel]
      (renderChildren)
      (:? (== component.children.length 0)
          [:div {:className "text-[10px] text-gray-600 italic py-2"} "Drop components here"]
          nil)])

    "Button"
    (return
     [:button {:.. allProps
               :className (+ allProps.className " " (or resolvedProperties.className "px-4 py-2 bg-blue-500 text-white rounded"))}
      (or textContent "Button")])

    "Text"
    (return
     [:p {:.. allProps
          :className (+ allProps.className " " (or resolvedProperties.className "text-gray-300"))}
      (or textContent "Text content")])

    "Heading"
    (return
     [:h2 {:.. allProps
           :className (+ allProps.className " " (or resolvedProperties.className "text-2xl font-bold text-gray-200"))}
      (or textContent "Heading")])

    "Image"
    (return
     [:img
      {:.. allProps
       :src (or resolvedProperties.src "https://via.placeholder.com/400x300")
       :alt (or resolvedProperties.alt "Image")
       :className (+ allProps.className " " (or resolvedProperties.className "w-full h-auto"))}])

    "Input"
    (return
     [:input
      {:.. allProps
       :type (or resolvedProperties.type "text")
       :placeholder (or resolvedProperties.placeholder "Enter text...")
       :className (+ allProps.className " " (or resolvedProperties.className "px-3 py-2 border border-gray-600 rounded bg-[#1e1e1e] text-gray-300"))}])

    "View"
    (return
     [:div {:.. allProps
            :className (+ allProps.className " " (or resolvedProperties.className ""))}
      (renderChildren)])

    (return
     [:div {:.. allProps
            :className (+ allProps.className " text-gray-500")}
      [:span displayLabel]])))
