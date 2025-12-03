(ns indigo.client.tasks.task-browser
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             #_[js.react.ext-monaco :as monaco]
             [js.react.ext-box :as box]
             #_[js.react.ext-live :as live]
             [indigo.client.ui-global :as global]
             [indigo.client.ui-common :as ui]]})

(defn.js ErrorBoundary
  [{:# [children selectedComp]}]
  (var [error setError] (r/useState nil))
  (r/useEffect (fn [] (setError nil)) [selectedComp])
  (if error
    (return [:div {:className "text-red-500 p-4"}
             "Error rendering component: " (. error message)])
    (return children)))

(defn.js LiveComponent
  [{:# [ns comp]}]
  (if (or (not ns) (not comp))
    (return nil))
  
  ;; Resolve component from global scope
  (var parts (. ns (split ".")))
  (var root window)
  (k/for:array [part parts]
    (if root (:= root (. root [part]))))
  
  (var Component (:? root (. root [comp]) nil))
  
  (if Component
    (return [:div {:className "p-4 bg-white"}
             [:% Component]])
    (return [:div {:className "text-gray-500 italic"}
             "Component not found in global scope."])))

(defn.js TaskBrowser
  []
  ;; State hooks
  (var [namespaces setNamespaces] (box/useBox global/Global ["task.browser" "namespaces"]))
  (var [components setComponents] (box/useBox global/Global ["task.browser" "components"]))
  (var [selectedNs setSelectedNs] (box/useBox global/Global ["task.browser" "selected-ns"]))
  (var [selectedComp setSelectedComp] (box/useBox global/Global ["task.browser" "selected-comp"]))
  (var [sourceCode setSourceCode] (box/useBox global/Global ["task.browser" "source-code"]))
  
  ;; Init - load namespaces
  (r/useEffect
   (fn []
     (. (global/api-post "/api/translate/browser/namespaces" {:lang "js"})
        (then (fn [result] (setNamespaces result))))
     (return undefined))
   [])
  
  ;; Load components when namespace selected
  (r/useEffect
   (fn []
     (when selectedNs
       (. (global/api-post "/api/translate/browser/components"
                          {:lang "js"
                           :ns selectedNs})
          (then (fn [result] (setComponents result)))))
     (return undefined))
   [selectedNs])
  
  ;; Load source code when component selected
  (r/useEffect
   (fn []
     (when (and selectedNs selectedComp)
       (. (global/api-post "/api/translate/browser/component"
                          {:lang "js"
                           :ns selectedNs
                           :component selectedComp})
          (then (fn [result] (setSourceCode result)))))
     (return undefined))
   [selectedComp selectedNs])
  
  ;; Render
  (return
   [:div {:className "w-full h-full overflow-hidden"}
    [:div {:className "w-full h-full overflow-hidden flex"}
     ;; Namespaces column
     [:div {:className "w-1/4 h-full border-r border-gray-200 overflow-y-auto"}
      [:div {:className "p-2 font-bold bg-gray-100"} "Namespaces"]
      (k/arr-map namespaces
        (fn [ns]
          (return
           [:div
            {:key ns
             :className (+ "p-2 cursor-pointer hover:bg-blue-50 "
                          (:? (== ns selectedNs) "bg-blue-100" ""))
             :onClick (fn [] (setSelectedNs ns))}
            ns])))]
     
     ;; Components column
     [:div {:className "w-1/4 h-full border-r border-gray-200 overflow-y-auto"}
      [:div {:className "p-2 font-bold bg-gray-100"} "Components"]
      (k/arr-map components
        (fn [comp]
          (return
           [:div
            {:key comp
             :className (+ "p-2 cursor-pointer hover:bg-blue-50 "
                          (:? (== comp selectedComp) "bg-blue-100" ""))
             :onClick (fn [] (setSelectedComp comp))}
            comp])))]
     
     ;; Preview column
     [:div {:className "w-1/2 h-full overflow-y-auto p-4 flex flex-col gap-4"}
      [:div {:className "font-bold text-lg"} selectedComp]
      
      ;; Live Preview
      [:div {:className "border rounded p-4 min-h-[200px]"}
       [:div {:className "font-bold mb-2 text-gray-500"} "Preview"]
       [:% -/ErrorBoundary {:selectedComp selectedComp}
        [:% -/LiveComponent {:ns selectedNs :comp selectedComp}]]]
      
      ;; Source Code
      [:div {:className "border rounded overflow-hidden"}
       [:div {:className "font-bold p-2 bg-gray-100"} "Source"]
       [:% ui/CodeEditor
        {:language "clojure"
         :value (or sourceCode "")
         :readOnly true}]]]]]))
