(ns smalltalkinterfacedesign.app
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.react.dnd :as dnd]
             [js.react.dnd.html5-backend :as dnd-html5]
             [smalltalkinterfacedesign.components.component-browser :as cb]
             [smalltalkinterfacedesign.components.library-browser :as lb]
             [smalltalkinterfacedesign.components.viewport-canvas :as vc]
             [smalltalkinterfacedesign.components.properties-panel :as pp]
             [smalltalkinterfacedesign.components.outliner-panel :as op]
             [smalltalkinterfacedesign.components.top-bar :as tb]
             [smalltalkinterfacedesign.components.theme-editor :as te]
             [js.lib.figma :as fg]]})

(def.js defaultTheme (.- te/defaultTheme))
(def.js Theme (.- te/Theme))

(defn.js App []
  (var [components setComponents]
    (r/useState
      [(do {:id "root"
            :type "View"
            :label "Scene"
            :properties {:padding "$4"
                         :backgroundColor "$background"}
            :children
            [(do {:id "example-card-1"
                  :type "Card"
                  :label "Example Card"
                  :properties {:className "p-6 bg-white rounded-lg shadow-md max-w-md mx-auto mt-8"}
                  :inputs {:title {:type "string"
                                   :description "Card title text"}
                           :description {:type "string"
                                         :description "Card description"}
                           :buttonText {:type "string"
                                        :description "Button label"}
                           :count {:type "number"
                                   :description "Counter value"}}
                  :inputValues {:title "Welcome to Input Binding!"
                                :description "This card demonstrates how inputs work. Edit the input values in the Inputs tab to see changes."
                                :buttonText "Click Me"
                                :count 42}
                  :states {:isVisible {:type "boolean"
                                      :default true
                                      :description "Controls visibility of description"}
                           :clickCount {:type "number"
                                        :default 0
                                        :description "Number of button clicks"}}
                  :actions {:toggleVisibility {:type "toggleState"
                                               :target "isVisible"
                                               :description "Toggle description visibility"}
                            :incrementClicks {:type "incrementState"
                                              :target "clickCount"
                                              :description "Increment click counter"}}
                  :triggers {:onButtonClick {:event "click"
                                             :action "incrementClicks"
                                             :description "Increment counter when button is clicked"}}
                  :children
                  [(do {:id "example-heading-1"
                        :type "Heading"
                        :label "Card Title"
                        :properties {:children "{input.title}"
                                     :className "text-2xl font-bold text-gray-900 mb-4"}
                        :children []})
                   (do {:id "example-text-1"
                        :type "Text"
                        :label "Card Description"
                        :properties {:children "{input.description}"
                                     :className "text-gray-600 mb-4"}
                        :children []})
                   (do {:id "example-text-2"
                        :type "Text"
                        :label "Counter Display"
                        :properties {:children "Clicks: {state.clickCount}"
                                     :className "text-sm text-gray-500 mb-4"}
                        :children []})
                   (do {:id "example-button-1"
                        :type "Button"
                        :label "Action Button"
                        :properties {:children "{input.buttonText}"
                                     :className "px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"}
                        :children []})]})]})]))

  (var [selectedComponent setSelectedComponent] (r/useState "example-card-1"))
  (var [viewMode setViewMode] (r/useState "design"))
  (var [theme setTheme] (r/useState -/defaultTheme))

  ;; History management
  (var [history setHistory]
    (r/useState
      [[(do {:id "root"
             :type "View"
             :label "Scene"
             :properties {:padding "$4"
                          :backgroundColor "$background"}
             :children
             [(do {:id "example-card-1"
                   :type "Card"
                   :label "Example Card"
                   :properties {:className "p-6 bg-white rounded-lg shadow-md max-w-md mx-auto mt-8"}
                   :inputs {:title {:type "string"
                                    :description "Card title text"}
                            :description {:type "string"
                                          :description "Card description"}
                            :buttonText {:type "string"
                                         :description "Button label"}
                            :count {:type "number"
                                    :description "Counter value"}}
                   :inputValues {:title "Welcome to Input Binding!"
                                 :description "This card demonstrates how inputs work. Edit the input values in the Inputs tab to see changes."
                                 :buttonText "Click Me"
                                 :count 42}
                   :states {:isVisible {:type "boolean"
                                       :default true
                                       :description "Controls visibility of description"}
                            :clickCount {:type "number"
                                         :default 0
                                         :description "Number of button clicks"}}
                   :actions {:toggleVisibility {:type "toggleState"
                                                :target "isVisible"
                                                :description "Toggle description visibility"}
                             :incrementClicks {:type "incrementState"
                                               :target "clickCount"
                                               :description "Increment click counter"}}
                   :triggers {:onButtonClick {:event "click"
                                              :action "incrementClicks"
                                              :description "Increment counter when button is clicked"}}
                   :children
                   [(do {:id "example-heading-1"
                         :type "Heading"
                         :label "Card Title"
                         :properties {:children "{input.title}"
                                      :className "text-2xl font-bold text-gray-900 mb-4"}
                         :children []})
                    (do {:id "example-text-1"
                         :type "Text"
                         :label "Card Description"
                         :properties {:children "{input.description}"
                                      :className "text-gray-600 mb-4"}
                         :children []})
                    (do {:id "example-text-2"
                         :type "Text"
                         :label "Counter Display"
                         :properties {:children "Clicks: {state.clickCount}"
                                      :className "text-sm text-gray-500 mb-4"}
                         :children []})
                    (do {:id "example-button-1"
                         :type "Button"
                         :label "Action Button"
                         :properties {:children "{input.buttonText}"
                                      :className "px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"}
                         :children []})]})]})]]))
  (var [historyIndex setHistoryIndex] (r/useState 0))
  (var isUndoRedoAction (r/useRef false))

  (r/useEffect
    (fn []
      (when isUndoRedoAction.current
        (:= isUndoRedoAction.current false)
        (return))

      (var newState (JSON.parse (JSON.stringify components)))

      (setHistory (fn [prev]
                    (var newHistory (. prev (slice 0 (+ historyIndex 1))))
                    (. newHistory (push newState))
                    (when (> newHistory.length 50)
                      (. newHistory (shift)))
                    (return newHistory)))

      (setHistoryIndex (fn [prev]
                         (var newIndex (+ prev 1))
                         (return (:? (>= newIndex 50) 49 newIndex)))))
    [components])

  (var undo (fn []
              (when (> historyIndex 0)
                (:= isUndoRedoAction.current true)
                (var newIndex (- historyIndex 1))
                (setHistoryIndex newIndex)
                (setComponents (JSON.parse (JSON.stringify (. history [newIndex])))))))

  (var redo (fn []
              (when (< historyIndex (- history.length 1))
                (:= isUndoRedoAction.current true)
                (var newIndex (+ historyIndex 1))
                (setHistoryIndex newIndex)
                (setComponents (JSON.parse (JSON.stringify (. history [newIndex])))))))

  (r/useEffect
    (fn []
      (var handleKeyDown (fn [e]
                           (if (and (or e.ctrlKey e.metaKey) (not e.shiftKey) (== e.key "z"))
                             (do (. e (preventDefault)) (undo))
                             (if (and (or e.ctrlKey e.metaKey) (or (and e.shiftKey (== e.key "z")) (== e.key "y")))
                               (do (. e (preventDefault)) (redo))))))

      (. window (addEventListener "keydown" handleKeyDown))
      (return (fn [] (. window (removeEventListener "keydown" handleKeyDown)))))
    [historyIndex history])

  (var addComponent (fn [type (:= parentId "root")]
                      (when (== type "__REFRESH__")
                        (setComponents (fn [prev] [(transduce (map identity) conj [] prev)]))
                        (return))

                      (var newComponent
                        (do {:id (+ (type.toLowerCase) "-" (Date.now) )
                             :type type
                             :label type
                             :properties (getDefaultProperties type)
                             :children []
                             :parent parentId}))

                      (setComponents (fn [prev]
                                       (var updated [(transduce (map identity) conj [] prev)])
                                       (var parent (findComponentById updated parentId))
                                       (when parent
                                         (. parent.children (push newComponent)))
                                       (return updated)))

                      (setSelectedComponent newComponent.id)))

  (var moveComponent (fn [draggedId targetId position]
                       (setComponents (fn [prev]
                                        (var updated (JSON.parse (JSON.stringify prev)))

                                        (var draggedComponent nil)
                                        (var removeDragged (fn [comps]
                                                             (for [(var i 0) (< i comps.length) (:++ i)]
                                                               (when (== (. comps [i] id) draggedId)
                                                                 (:= draggedComponent (. comps [i]))
                                                                 (. comps (splice i 1))
                                                                 (return true))
                                                               (when (removeDragged (. comps [i] children))
                                                                 (return true)))
                                                             (return false)))
                                        (removeDragged updated)

                                        (when (not draggedComponent)
                                          (return prev))

                                        (var insertComponent (fn [comps (:= parentComps undefined)]
                                                               (for [(var i 0) (< i comps.length) (:++ i)]
                                                                 (when (== (. comps [i] id) targetId)
                                                                   (if (== position "inside")
                                                                     (. (. comps [i] children) (push draggedComponent))
                                                                     (if (== position "before")
                                                                       (. comps (splice i 0 draggedComponent))
                                                                       (. comps (splice (+ i 1) 0 draggedComponent))))
                                                                   (return true))
                                                                 (when (insertComponent (. comps [i] children) comps)
                                                                   (return true)))
                                                               (return false)))

                                        (insertComponent updated)
                                        (return updated)))))

  (var updateComponentProperty (fn [id property value]
                                 (setComponents (fn [prev]
                                                  (var updated [(transduce (map identity) conj [] prev)])
                                                  (var component (findComponentById updated id))
                                                  (when component
                                                    (if (== property "label")
                                                      (:= component.label value)
                                                      (:= (. component.properties [property]) value)))
                                                  (return updated)))))

  (var updateComponentInputs (fn [id inputs]
                               (setComponents (fn [prev]
                                                (var updated [(transduce (map identity) conj [] prev)])
                                                (var component (findComponentById updated id))
                                                (when component
                                                  (:= component.inputs inputs))
                                                (return updated)))))

  (var updateComponentInputValues (fn [id inputValues]
                                    (setComponents (fn [prev]
                                                     (var updated [(transduce (map identity) conj [] prev)])
                                                     (var component (findComponentById updated id))
                                                     (when component
                                                       (:= component.inputValues inputValues))
                                                     (return updated)))))

  (var updateComponentStates (fn [id states]
                               (setComponents (fn [prev]
                                                (var updated [(transduce (map identity) conj [] prev)])
                                                (var component (findComponentById updated id))
                                                (when component
                                                  (:= component.states states))
                                                (return updated)))))

  (var updateComponentTriggers (fn [id triggers]
                                 (setComponents (fn [prev]
                                                  (var updated [(transduce (map identity) conj [] prev)])
                                                  (var component (findComponentById updated id))
                                                  (when component
                                                    (:= component.triggers triggers))
                                                  (return updated)))))

  (var updateComponentActions (fn [id actions]
                                (setComponents (fn [prev]
                                                 (var updated [(transduce (map identity) conj [] prev)])
                                                 (var component (findComponentById updated id))
                                                 (when component
                                                   (:= component.actions actions))
                                                 (return updated)))))

  (var deleteComponent (fn [id]
                         (when (== id "root")
                           (return))

                         (setComponents (fn [prev]
                                          (var updated [(transduce (map identity) conj [] prev)])
                                          (removeComponentById updated id)
                                          (return updated)))

                         (setSelectedComponent "root")))

  (var findComponentById (fn [components id]
                           (for [component components]
                             (when (== component.id id)
                               (return component))
                             (var found (findComponentById component.children id))
                             (when found
                               (return found)))
                           (return nil)))

  (var removeComponentById (fn [components id]
                             (for [(var i 0) (< i components.length) (:++ i)]
                               (when (== (. components [i] id) id)
                                 (. components (splice i 1))
                                 (return true))
                               (when (removeComponentById (. components [i] children) id)
                                 (return true)))
                             (return false)))

  (var getDefaultProperties (fn [type]
                              (var defaults
                                {:Button {:children "Button" :className "px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"}
                                 :Input {:placeholder "Enter text..." :className "px-4 py-2 border border-gray-300 rounded"}
                                 :Text {:children "Text content" :className "text-gray-700"}
                                 :Heading {:children "Heading" :className "text-2xl font-bold"}
                                 :Container {:className "p-4"}
                                 :FlexRow {:className "flex gap-4"}
                                 :FlexCol {:className "flex flex-col gap-4"}
                                 :Card {:className "p-6 bg-white rounded-lg shadow"}
                                 :Switch {:className ""}
                                 :Checkbox {:className ""}})
                              (return (or (. defaults [type]) {}))))

  (var importComponent (fn [component]
                         (var generateNewIds (fn [comp (:= isRoot true)]
                                               (return
                                                 (do {:.. comp
                                                      :id (+ (comp.type.toLowerCase) "-" (Date.now) "-" (. (Math.random) (toString 36) (substr 2 9)))
                                                      :libraryRef (:? isRoot comp.libraryRef undefined)
                                                      :children (. comp.children (map (fn [child] (return (generateNewIds child false)))))}))))

                         (var newComponent (generateNewIds component))

                         (setComponents (fn [prev]
                                          (var updated [(transduce (map identity) conj [] prev)])
                                          (var parent (findComponentById updated "root"))
                                          (when parent
                                            (. parent.children (push newComponent)))
                                          (return updated)))

                         (return newComponent.id)))

  (var importAndEditComponent (fn [component]
                                (var newId (importComponent component))
                                (setSelectedComponent newId)))

  (var selectedComponentData (findComponentById components selectedComponent))

  (return
    [:% dnd/DndProvider {:backend dnd-html5/HTML5Backend}
      [:div {:className "flex flex-col h-screen bg-[#1e1e1e]"}
        [:% tb/TopBar {:viewMode viewMode :onViewModeChange setViewMode}]
        [:% fg/ResizablePanelGroup {:direction "horizontal" :className "flex-1"}
          [:% fg/ResizablePanel {:defaultSize 20 :minSize 15 :maxSize 30}
            [:% fg/Tabs {:defaultValue "primitives" :className "flex flex-col h-full"}
              [:div {:className "bg-[#252525] border-b border-[#323232]"}
                [:% fg/TabsList {:className "w-full justify-start rounded-none bg-transparent border-b-0 h-10"}
                  [:% fg/TabsTrigger
                    {:value "primitives"
                     :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
                    "Primitives"]
                  [:% fg/TabsTrigger
                    {:value "library"
                     :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
                    "Library"]
                  [:% fg/TabsTrigger
                    {:value "theme"
                     :className "rounded-none data-[state=active]:bg-[#323232] text-xs text-gray-400 data-[state=active]:text-gray-200"}
                    "Theme"]]]
              [:% fg/TabsContent {:value "primitives" :className "flex-1 m-0"}
                [:% cb/ComponentBrowser {:onAddComponent addComponent}]]
              [:% fg/TabsContent {:value "library" :className "flex-1 m-0"}
                [:% lb/LibraryBrowser
                  {:onImportComponent (fn [comp] (return (importComponent comp)))
                   :onImportAndEdit importAndEditComponent}]]
              [:% fg/TabsContent {:value "theme" :className "flex-1 m-0"}
                [:% te/ThemeEditor {:theme theme :onThemeChange setTheme}]]]]]
          [:% fg/ResizableHandle {:className "w-[1px] bg-[#323232]"}]
          [:% fg/ResizablePanel {:defaultSize 50 :minSize 30}
            [:% fg/ResizablePanelGroup {:direction "vertical"}
              [:% fg/ResizablePanel {:defaultSize 70 :minSize 40}
                [:% vc/ViewportCanvas
                  {:components components
                   :selectedComponent selectedComponent
                   :onSelectComponent setSelectedComponent
                   :onAddComponent addComponent
                   :onMoveComponent moveComponent
                   :viewMode viewMode
                   :theme theme}]]
              [:% fg/ResizableHandle {:className "h-[1px] bg-[#323232]"}]
              [:% fg/ResizablePanel {:defaultSize 30 :minSize 15}
                [:% op/OutlinerPanel
                  {:components components
                   :selectedComponent selectedComponent
                   :onSelectComponent setSelectedComponent
                   :onDeleteComponent deleteComponent
                   :onMoveComponent moveComponent}]]]]
          [:% fg/ResizableHandle {:className "w-[1px] bg-[#323232]"}]
          [:% fg/ResizablePanel {:defaultSize 30 :minSize 20 :maxSize 40}
            [:% pp/PropertiesPanel
              {:component selectedComponentData
               :onUpdateProperty updateComponentProperty
               :onDeleteComponent deleteComponent
               :onUpdateInputs updateComponentInputs
               :onUpdateInputValues updateComponentInputValues
               :onUpdateStates updateComponentStates
               :onUpdateTriggers updateComponentTriggers
               :onUpdateActions updateComponentActions}]]]]))