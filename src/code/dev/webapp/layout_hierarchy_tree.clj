(ns code.dev.webapp.layout-hierarchy-tree
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.base-lib :as k]
             [js.lib.lucide :as lc]
             [js.lib.radix :as rx]]})

(defn.js HierarchyTree
  [#{[(:= components [])
      selectedComponent
      onSelectComponent
      onDeleteComponent]}]
  (var [expandedNodes setExpandedNodes] (r/useState (new Set ["root"])))

  (var toggleExpanded
       (fn [id]
         (setExpandedNodes (fn [prev]
                             (var next (new Set prev))
                             (if (. next (has id))
                               (. next (delete id))
                               (. next (add id)))
                             (return next)))))

  (var renderTreeNode
       (fn [component opts]
         (var #{[(:= depth 0)]}) opts
         (var isExpanded (. expandedNodes (has (. component id))))
         (var isSelected (=== (. component id) selectedComponent))
         (var hasChildren (> (. component children length) 0))

         (return
           [:div {:key (. component id)}
            [:div {:class (+ "flex items-center gap-1 py-1 px-2 hover:bg-gray-100 cursor-pointer group "
                                 (:? isSelected "bg-blue-100" ""))
                   :style {:paddingLeft (+ (* depth 16) 8 "px")}}
             (:? hasChildren
                 [:button {:onClick (fn [e]
                                      (. e (stopPropagation))
                                      (toggleExpanded (. component id)))
                           :class "p-0.5 hover:bg-gray-200 rounded"}
                  (:? isExpanded
                      [:% lc/ChevronDown {:class "w-3 h-3"}]
                      [:% lc/ChevronRight {:class "w-3 h-3"}])]
                 [:div {:class "w-4"}])

             [:div {:class "flex-1 text-sm"
                    :onClick (fn [] (onSelectComponent (. component id)))}
              [:span {:class "text-gray-600"} (. component type)]
              [:span {:class "text-gray-400 text-xs ml-2"}
               (+ "#" (. (. component id (split "-")) (at 1)))]]

             (:? (not (=== (. component id) "root"))
                 [:% rx/Button {:size "sm"
                            :variant "ghost"
                            :class "opacity-0 group-hover:opacity-100 h-6 w-6 p-0"
                            :onClick (fn [e]
                                       (. e (stopPropagation))
                                       (onDeleteComponent (. component id)))}
                  [:% lc/Trash2 {:class "w-3 h-3 text-red-500"}]]
                 nil)]

            (:? (and hasChildren isExpanded)
                [:div
                 (k/arr-map (. component children)
                            (fn [child]
                              (return (renderTreeNode child (+ depth 1)))))]
                nil)])))

  (return
    [:div {:class "flex flex-col h-full bg-white"}
     ;; Title
     [:div {:class "px-4 py-2 bg-gray-100 border-b"}
      [:h3 {:class "text-sm"} "Component Hierarchy"]]

     ;; Tree
     [:% rx/ScrollArea {:class "flex-1"}
      [:div {:class "py-2"}
       (k/arr-map components
                  (fn [component]
                    (return (renderTreeNode component))))]]]))
