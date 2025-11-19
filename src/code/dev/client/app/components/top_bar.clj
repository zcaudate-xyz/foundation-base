(ns code.dev.client.app.components.top-bar
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

;; Interface definitions are removed as per spec
;; interface TopBarProps { ... }

(defn.js TopBar
  [{:# [viewMode onViewModeChange onUndo onRedo canUndo canRedo]}]
  (return
   [:div {:className "h-12 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-4 gap-4"}
    ;; Left Section
    [:div {:className "flex items-center gap-2"}
     [:div {:className "flex items-center gap-1 px-2"}
      [:div {:className "w-2 h-2 rounded-full bg-red-500"}]
      [:div {:className "w-2 h-2 rounded-full bg-yellow-500"}]
      [:div {:className "w-2 h-2 rounded-full bg-green-500"}]]

     [:div {:className "w-[1px] h-6 bg-[#323232]"}]

     ;; Undo/Redo
     [:div {:className "flex items-center gap-1"}
      [:% fg/Button
       {:variant "ghost"
        :size "sm"
        :onClick onUndo
        :disabled (not canUndo)
        :className "h-7 w-7 p-0 text-gray-400 hover:text-gray-200 hover:bg-[#323232] disabled:opacity-30 disabled:cursor-not-allowed"
        :title "Undo (Ctrl+Z)"}
       [:% lc/Undo {:className "w-3.5 h-3.5"}]]
      [:% fg/Button
       {:variant "ghost"
        :size "sm"
        :onClick onRedo
        :disabled (not canRedo)
        :className "h-7 w-7 p-0 text-gray-400 hover:text-gray-200 hover:bg-[#323232] disabled:opacity-30 disabled:cursor-not-allowed"
        :title "Redo (Ctrl+Shift+Z)"}
       [:% lc/Redo {:className "w-3.5 h-3.5"}]]]

     [:div {:className "w-[1px] h-6 bg-[#323232]"}]

     ;; View Mode Toggle
     [:div {:className "flex gap-1"}
      [:% fg/Button
       {:variant (:? (=== viewMode "design") "default" "ghost")
        :size "sm"
        :onClick (fn [] (return (onViewModeChange "design")))
        :className (+ "h-8 "
                      (:? (=== viewMode "design")
                          "bg-[#404040] text-white hover:bg-[#4a4a4a]"
                          "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"))}
       [:% lc/Layout {:className "w-4 h-4 mr-1"}]
       "Design"]
      [:% fg/Button
       {:variant (:? (=== viewMode "code") "default" "ghost")
        :size "sm"
        :onClick (fn [] (return (onViewModeChange "code")))
        :className (+ "h-8 "
                      (:? (=== viewMode "code")
                          "bg-[#404040] text-white hover:bg-[#4a4a4a]"
                          "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"))}
       [:% lc/Code2 {:className "w-4 h-4 mr-1"}]
       "Code"]]]

    ;; Spacer
    [:div {:className "flex-1"}]

    ;; Actions
    [:div {:className "flex gap-2"}
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Save {:className "w-4 h-4 mr-1"}]
      "Save"]
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Share2 {:className "w-4 h-4 mr-1"}]
      "Export"]
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Settings {:className "w-4 h-4"}]]
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"
       :onClick onUndo
       :disabled (not canUndo)}
      [:% lc/Undo {:className "w-4 h-4 mr-1"}]
      "Undo"]
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"
       :onClick onRedo
       :disabled (not canRedo)}
      [:% lc/Redo {:className "w-4 h-4 mr-1"}]
      "Redo"]
     [:% fg/Button
      {:variant "ghost"
       :size "sm"
       :className "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Play {:className "w-4 h-4"}]]]))
