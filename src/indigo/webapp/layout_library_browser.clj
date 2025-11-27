(ns indigo.webapp.layout-library-browser
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.lucide :as lc]
             [js.lib.figma :as fg]
             [indigo.webapp.layout-library-browser-data :as data]]
   :import  [["react-dnd" :as #{useDrag}]]})

(def.js componentLibrary
  [{:id "ui.sections/hero-gradient"
    :namespace "ui.sections"
    :name "HeroGradient"
    :description "Hero section with gradient background"
    :stars 245
    :component {:id "hero-section"
                :type "Container"
                :label "Hero Section"
                :libraryRef "ui.sections/HeroGradient"
                :properties {:className "bg-gradient-to-r from-purple-600 to-blue-600 text-white py-20 px-6"}
                :children [{:id "hero-content"
                            :type "Container"
                            :label "Hero Content"
                            :properties {:className "max-w-4xl mx-auto text-center"}
                            :children [{:id "hero-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Build Amazing UIs"
                                                     :className "text-5xl font-bold mb-4"}
                                        :children []}
                                       {:id "hero-subtitle"
                                        :type "Text"
                                        :label "Subtitle"
                                        :properties {:children "Create beautiful interfaces with our component builder"
                                                     :className "text-xl mb-8 opacity-90"}
                                        :children []}
                                       {:id "hero-cta"
                                        :type "Button"
                                        :label "CTA Button"
                                        :properties {:children "Get Started"
                                                     :className "bg-white text-purple-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100"}
                                        :children []}]}]}}
   (:.. data/componentLibraryMore)])

(defn.js buildNamespaceTree
  [components]
  (var root {:name "root"
             :fullPath ""
             :components []
             :children (new Map)})

  (if (not components)
    (return root))

  (components.forEach
   (fn [comp]
     (var parts (comp.namespace.split "."))
     (var current root)
     (parts.forEach
      (fn [part idx]
        (if (not (current.children.has part))
          (current.children.set
           part
           {:name part
            :fullPath (. (. parts (slice 0 (+ idx 1))) (join "."))
            :components []
            :children (new Map)}))
        
        (:= current (. (. current children) (get part)))))
     
     (current.components.push comp)))
  
  (return root))

(defn.js LibraryComponentItem
  [#{[comp depth onImportComponent onImportAndEdit]}]
  (var [#{isDragging} drag]
       (useDrag (fn []
                  (return
                   {:type "LIBRARY_COMPONENT"
                    :item {:libraryComponent comp.component}
                    :collect (fn [monitor]
                               (return {:isDragging (monitor.isDragging)}))}))))
  
  (var handleDoubleClick
       (fn []
         (onImportAndEdit comp.component)))
  
  (return
    [:div {:ref drag
           :onDoubleClick handleDoubleClick
           :className (+ "flex items-start gap-2 py-2 px-2 hover:bg-[#323232] group cursor-grab "
                         (:? isDragging "opacity-50 cursor-grabbing" ""))
           :style {:paddingLeft (+ (* depth 12) 8 "px")}}
     [:% lc/FileCode {:className "w-3 h-3 text-purple-400 mt-0.5 flex-shrink-0"}]
     [:div {:className "flex-1 min-w-0"}
      [:div {:className "flex items-center gap-2 mb-1"}
       [:span {:className "text-xs text-gray-300"} (. comp name)]
       [:div {:className "flex items-center gap-1 text-[10px] text-gray-500"}
        [:% lc/Star {:className "w-2.5 h-2.5 fill-current"}]
        comp.stars]]
      [:p {:className "text-[10px] text-gray-600 mb-1"} comp.description]
      [:% fg/Button {:size "icon"
                     :variant "ghost"
                     :onClick (fn [e]
                                (e.stopPropagation)
                                (onImportComponent comp.component))
                     :className "h-5 w-5 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity"}
       [:% lc/Download {:className "w-2.5 h-2.5"}]]
      [:% fg/Button {:size "icon"
                     :variant "ghost"
                     :onClick (fn [e]
                                (e.stopPropagation)
                                (onImportAndEdit comp.component))
                     :className "h-5 w-5 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity"}
       [:% lc/Edit {:className "w-2.5 h-2.5"}]]]]]))


(defn.js LibraryBrowser
  [#{[components onImportComponent onImportAndEdit]}]
  (var [search setSearch] (r/useState ""))
  (var [expandedNamespaces setExpandedNamespaces] (r/useState (new Set ["ui"])))
  
  (var toggleNamespace
       (fn [path]
         (setExpandedNamespaces (fn [prev]
                                  (var next (new Set prev))
                                  (if (. next (has path))
                                    (. next (delete path))
                                    (. next (add path)))
                                  (return next)))))

  (var filteredComponents
       (. (or components [])
          (filter
           (fn [comp]
             (if (not search)
               (return true))
             (var searchLower (search.toLowerCase))
             (return
              (or (. comp name (toLowerCase) (includes searchLower))
                  (. comp namespace (toLowerCase) (includes searchLower))
                  (. comp description (toLowerCase) (includes searchLower))))))))

  (var namespaceTree (-/buildNamespaceTree filteredComponents))
  
  (var renderNamespaceNode
       (fn [node (:= depth 0)]
         (var results [])
         (var isExpanded (expandedNamespaces.has node.fullPath))
         
         ;; Render namespace folder
         (if (not= node.name "root")
           (results.push 
            [:div {:key node.fullPath}
             [:button
              {:onClick (fn [] (toggleNamespace node.fullPath))
               :className (+ "w-full flex items-center gap-2 py-1 px-2 hover:bg-[#323232] text-left group ")
               :style {:paddingLeft (+ (* depth 12) 8 "px")}}
                (:? isExpanded
                    [:% lc/ChevronDown {:className "w-3 h-3 text-gray-500"}]
                    [:% lc/ChevronRight {:className "w-3 h-3 text-gray-500"}])
                [:% lc/Folder {:className "w-3 h-3 text-blue-400"}]
                [:span {:className "text-xs text-gray-400"} (. node name)]
                [:span {:className "text-[10px] text-gray-600 ml-auto opacity-0 group-hover:opacity-100"}
                 (+ node.components.length
                    (. node children
                       (values)
                       (reduce 
                        (fn [sum child]
                          (return (+ sum child.components.length)))
                        0)))]]

               (:? isExpanded
                   [:<>
                    ;; Render components in this namespace
                    (. node components
                       (map 
                        (fn [comp]
                          (return
                           [:% -/LibraryComponentItem {:key (. comp id)
                                                       :comp comp
                                                       :depth (+ depth 1)
                                                       :onImportComponent onImportComponent
                                                       :onImportAndEdit onImportAndEdit}]))))

                    ;; Render child namespaces
                    (. node children
                       (values)
                       (map 
                        (fn [childNode]
                          (return (renderNamespaceNode childNode (+ depth 1))))))]
                   nil)])
           
           ;; Root node - render children directly
           (. (Array.from (node.children.values))
              (forEach (fn [childNode]
                         (results.push (:.. (renderNamespaceNode childNode depth)))))))
         
         (return results)))

  (return
   [:div {:className "flex flex-col h-full bg-[#252525]"}
    ;; Header
    [:div {:className "p-3 border-b border-[#323232]"}
     [:h2 {:className "text-xs text-gray-400 uppercase tracking-wide mb-2"}
      "Component Library"]

     ;; Search
     [:div {:className "relative"}
      [:% lc/Search {:className "absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-gray-500"}]
      [:input {:value search
               :onChange (fn [e] (setSearch e.target.value))
               :placeholder "Search namespaces..."
               :className "h-8 pl-7 bg-[#1e1e1e] border-[#323232] text-gray-300 text-xs placeholder:text-gray-600"}]]]
    
    ;; Namespace Tree
    [:% fg/ScrollArea {:className "flex-1"}
     [:div {:className "py-2"}
      (renderNamespaceNode namespaceTree)]]

    ;; Footer
    [:div {:className "p-2 border-t border-[#323232]"}
     [:div {:className "text-[10px] text-gray-600 text-center"}
      (+ (. filteredComponents length) " components in library")]]]))



(comment

  )
