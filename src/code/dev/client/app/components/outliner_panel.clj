(ns code.dev.client.app.components.outliner-panel
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.react-dnd :as dnd]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(defn.js TreeNode
  [{:# [component
        depth
        selectedComponent
        expandedNodes
        hiddenNodes
        onSelectComponent
        onDeleteComponent
        onMoveComponent
        toggleExpanded
        toggleVisibility]}]
  (var isExpanded (. expandedNodes (has component.id)))
  (var isSelected (== component.id selectedComponent))
  (var isHidden (. hiddenNodes (has component.id)))
  (var hasChildren (> component.children.length 0))

  (var displayName (or component.label component.type))

  (var [isDragging drag]
    (dnd/useDrag (fn []
                   (return {:type "OUTLINER_ITEM"
                            :item {:id component.id}
                            :collect (fn [monitor]
                                       (return {:isDragging (. monitor (isDragging))}))}))))

  (var [isOver drop]
    (dnd/useDrop (fn []
                   (return {:accept "OUTLINER_ITEM"
                            :drop (fn [item monitor]
                                    (when (. monitor (didDrop)) (return))
                                    (when (not= item.id component.id)
                                      (onMoveComponent item.id component.id "inside")))
                            :collect (fn [monitor]
                                       (return {:isOver (. monitor (isOver {:shallow true}))}))}))))

  (return
   [:div
    [:div
     {:ref (fn [node] (drag (drop node)))
      :className (+ "flex items-center gap-1 py-1 px-2 hover:bg-[#323232] cursor-pointer group transition-colors "
                    (:? isSelected "bg-[#404040]" "") " "
                    (:? isOver "bg-[#2a3a2a]" "") " "
                    (:? isDragging "opacity-50" ""))
      :style {:paddingLeft (+ (* depth 16) 8 "px")}}
     (:? hasChildren
         [:button
          {:onClick (fn [e]
                      (. e (stopPropagation))
                      (toggleExpanded component.id))
           :className "p-0.5 hover:bg-[#404040] rounded"}
          (:? isExpanded
              [:% lc/ChevronDown {:className "w-3 h-3 text-gray-500"}]
              [:% lc/ChevronRight {:className "w-3 h-3 text-gray-500"}])]
         [:div {:className "w-4"}])

     [:div
      {:className "flex-1 text-xs text-gray-300 flex items-center gap-1"
       :onClick (fn [] (return (onSelectComponent component.id)))}
      [:span {:className (:? isHidden "text-gray-600" "")} displayName]
      (:? (and component.label (not= component.label component.type))
          [:span {:className "text-gray-600 text-[10px]"} (+ "(" component.type ")")]
          nil)]

     [:div {:className "flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity"}
      [:% fg/Button
       {:size "sm"
        :variant "ghost"
        :className "h-5 w-5 p-0 hover:bg-[#404040]"
        :onClick (fn [e] (return (toggleVisibility component.id e)))}
       (:? isHidden
           [:% lc/EyeOff {:className "w-3 h-3 text-gray-600"}]
           [:% lc/Eye {:className "w-3 h-3 text-gray-500"}])]
      (:? (not= component.id "root")
          [:% fg/Button
           {:size "sm"
            :variant "ghost"
            :className "h-5 w-5 p-0 hover:bg-[#404040]"
            :onClick (fn [e]
                       (. e (stopPropagation))
                       (onDeleteComponent component.id))}
           [:% lc/Trash2 {:className "w-3 h-3 text-red-500"}]]
          nil)]]
    
    (:? (and hasChildren isExpanded)
        [:div
         (. component.children (map (fn [child]
                                      (return
                                       [:% -/TreeNode
                                        {:key child.id
                                         :component child
                                         :depth (+ depth 1)
                                         :selectedComponent selectedComponent
                                         :expandedNodes expandedNodes
                                         :hiddenNodes hiddenNodes
                                         :onSelectComponent onSelectComponent
                                         :onDeleteComponent onDeleteComponent
                                         :onMoveComponent onMoveComponent
                                         :toggleExpanded toggleExpanded
                                         :toggleVisibility toggleVisibility}]))))]
        nil)]))

(defn.js OutlinerPanel
  [{:# [components
        selectedComponent
        onSelectComponent
        onDeleteComponent
        onMoveComponent]}]
  (var [expandedNodes
        setExpandedNodes] (r/useState (new Set ["root"])))
  (var [hiddenNodes
        setHiddenNodes] (r/useState (new Set)))

  (var toggleExpanded
       (fn [id]
         (setExpandedNodes (fn [prev]
                             (var next (new Set prev))
                             (if (. next (has id))
                               (. next (delete id))
                               (. next (add id)))
                             (return next)))))

  (var toggleVisibility
       (fn [id e]
         (. e (stopPropagation))
         (setHiddenNodes (fn [prev]
                           (var next (new Set prev))
                           (if (. next (has id))
                             (. next (delete id))
                             (. next (add id)))
                           (return next)))))

  (return
   [:div {:className "flex flex-col h-full bg-[#252525]"}
    [:div {:className "h-10 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-3"}
     [:h3 {:className "text-xs text-gray-400 uppercase tracking-wide"} "Outliner"]]

    [:% fg/ScrollArea {:className "flex-1"}
     [:div {:className "py-2"}
      (. components
         (map (fn [component]
                (return
                 [:% -/TreeNode
                  {:key component.id
                   :component component
                   :depth 0
                   :selectedComponent selectedComponent
                   :expandedNodes expandedNodes
                   :hiddenNodes hiddenNodes
                   :onSelectComponent onSelectComponent
                   :onDeleteComponent onDeleteComponent
                   :onMoveComponent onMoveComponent
                   :toggleExpanded toggleExpanded
                   :toggleVisibility toggleVisibility}]))))]]

    [:div {:className "h-8 bg-[#2b2b2b] border-t border-[#323232] flex items-center px-3"}
     [:span {:className "text-[10px] text-gray-600"}
      (+ (. components
            (reduce (fn [count c]
                      (return (+ count (+ c.children.length 1)))) 0))
         " objects")]]]))


