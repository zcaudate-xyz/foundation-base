(ns indigo.client.browser.browser-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [indigo.webapp.layout-library-browser :as lib-browser]
             [indigo.webapp.layout-hierarchy-tree :as hierarchy-tree]
             [indigo.webapp.layout-top-bar :as top-bar]
             [indigo.client.ui-global :as global]]})

(defn.js BrowserMain
  []
  (var [libraryComponents setLibraryComponents] (r/useState []))
  (var [viewMode setViewMode] (r/useState "design"))
  (var [leftPanelSize setLeftPanelSize] (r/useState 20))
  (var [rightPanelSize setRightPanelSize] (r/useState 20))

  ;; Fetch library components on mount
  (r/useEffect
   (fn []
     (var fetchComponents
          (async (fn []
                   (try
                     (var response (await (fetch "/api/translate/browser/namespaces?lang=js")))
                     (var data (await (. response (json))))
                     (var namespaces (. data namespaces))

                     (var allComponents [])
                     (await (Promise.all
                             (. namespaces
                                (map (async (fn [ns]
                                              (var resp (await (fetch (+ "/api/translate/browser/components?lang=js&ns=" ns))))
                                              (var compData (await (. resp (json))))
                                              (. compData components
                                                 (forEach (fn [c]
                                                            (. c (keyword "namespace") ns)
                                                            (allComponents.push c))))))))))

                     ;; Transform to expected format for LibraryBrowser
                     (var formattedComponents
                          (. allComponents
                             (map (fn [c]
                                    (return
                                     {:id (+ c.namespace "/" c.name)
                                      :namespace c.namespace
                                      :name c.name
                                      :description (or c.doc "")
                                      :stars 0
                                      :component {:id (+ c.namespace "/" c.name)
                                                  :type c.name
                                                  :label c.name
                                                  :libraryRef (+ c.namespace "/" c.name)
                                                  :properties {}
                                                  :children []}})))))

                     (setLibraryComponents formattedComponents)
                     (catch e
                       (console.error "Failed to fetch components" e))))))
     (fetchComponents)
     (return))
   [])

  (var handleImportComponent
       (fn [component]
         (console.log "Importing" component)))

  (var handleImportAndEdit
       (fn [component]
         (console.log "Importing and Editing" component)))

  (return
   [:div {:className "flex flex-col h-screen w-full bg-[#1e1e1e] text-gray-300 overflow-hidden font-sans"}
    ;; Top Bar
    [:% top-bar/TopBar
     {:viewMode viewMode
      :onViewModeChange setViewMode
      :onUndo (fn [])
      :onRedo (fn [])
      :canUndo false
      :canRedo false}]

    ;; Main Content Area with Resizable Panels
    [:% fg/ResizablePanelGroup {:direction "horizontal" :className "flex-1 min-h-0"}
     ;; Left Panel: Library Browser
     [:% fg/ResizablePanel {:defaultSize leftPanelSize
                            :minSize 15
                            :maxSize 30
                            :className "flex flex-col border-r border-[#323232]"}
      [:% lib-browser/LibraryBrowser
       {:components libraryComponents
        :onImportComponent handleImportComponent
        :onImportAndEdit handleImportAndEdit}]]

     [:% fg/ResizableHandle {:withHandle true :className "bg-[#323232] hover:bg-[#404040]"}]

     ;; Center Panel: Canvas / Code
     [:% fg/ResizablePanel {:defaultSize (- 100 leftPanelSize rightPanelSize)
                            :className "flex flex-col bg-[#1e1e1e]"}
      [:div {:className "flex-1 flex items-center justify-center p-8 bg-[#121212] m-2 rounded-lg border border-[#323232]"}
       [:div {:className "text-gray-500 text-sm"}
        "Canvas Area - Drag components here"]]]

     [:% fg/ResizableHandle {:withHandle true :className "bg-[#323232] hover:bg-[#404040]"}]

     ;; Right Panel: Hierarchy / Properties
     [:% fg/ResizablePanel {:defaultSize rightPanelSize
                            :minSize 15
                            :maxSize 25
                            :className "flex flex-col border-l border-[#323232] bg-[#252525]"}
      [:% hierarchy-tree/HierarchyTree
       {:components []
        :selectedComponent nil
        :onSelectComponent (fn [])
        :onDeleteComponent (fn [])}]]
     ]]))
