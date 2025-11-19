(ns smalltalkinterfacedesign.components.hierarchy-tree
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [smalltalkinterfacedesign.app :as app]]})

(defn.js HierarchyTree [{:# [components selectedComponent onSelectComponent onDeleteComponent]}]
  (var [expandedNodes setExpandedNodes] (r/useState (new Set ["root"])))

  (var toggleExpanded (fn [id]
                        (setExpandedNodes (fn [prev]
                                            (var next (new Set prev))
                                            (if (. next (has id))
                                              (. next (delete id))
                                              (. next (add id)))
                                            (return next)))))

  (var renderTreeNode (fn [component (:= depth 0)]
                        (var isExpanded (. expandedNodes (has component.id)))
                        (var isSelected (== component.id selectedComponent))
                        (var hasChildren (> component.children.length 0))

                        (return
                          [:div {:key component.id}
                            [:div
                              {:className (+ "flex items-center gap-1 py-1 px-2 hover:bg-gray-100 cursor-pointer group "
                                             (:? isSelected "bg-blue-100" ""))
                               :style {:paddingLeft (+ (* depth 16) 8 "px")}}
                              (:? hasChildren
                                  [:button
                                    {:onClick (fn [e]
                                                (. e (stopPropagation))
                                                (toggleExpanded component.id))
                                     :className "p-0.5 hover:bg-gray-200 rounded"}
                                    (:? isExpanded
                                        [:% lc/ChevronDown {:className "w-3 h-3"}]
                                        [:% lc/ChevronRight {:className "w-3 h-3"}])]
                                  [:div {:className "w-4"}])

                              [:div
                                {:className "flex-1 text-sm"
                                 :onClick (fn [] (return (onSelectComponent component.id)))}
                                [:span {:className "text-gray-600"} component.type]
                                [:span {:className "text-gray-400 text-xs ml-2"}
                                  (+ "#" (. (. component.id (split "-")) [1]))]]

                              (:? (!= component.id "root")
                                  [:% fg/Button
                                    {:size "sm"
                                     :variant "ghost"
                                     :className "opacity-0 group-hover:opacity-100 h-6 w-6 p-0"
                                     :onClick (fn [e]
                                                (. e (stopPropagation))
                                                (onDeleteComponent component.id))}
                                    [:% lc/Trash2 {:className "w-3 h-3 text-red-500"}]]
                                  nil)]

                            (:? (and hasChildren isExpanded)
                                [:div
                                  (. component.children (map (fn [child] (return (renderTreeNode child (+ depth 1))))))]
                                nil)])))

  (return
    [:div {:className "flex flex-col h-full bg-white"}
      [:div {:className "px-4 py-2 bg-gray-100 border-b"}
        [:h3 {:className "text-sm"} "Component Hierarchy"]]
      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "py-2"}
          (. components (map (fn [component] (return (renderTreeNode component)))))]]]))