(ns code.dev.webapp.layout-top-bar
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.lucide :as lc]
             [js.lib.radix :as rx]]})

(defn.js TopBar
  [#{viewMode onViewModeChange onUndo onRedo canUndo canRedo}]
  (return
   [:div {:class "h-12 bg-[#2b2b2b] border-b border-[#323232] flex items-center px-4 gap-4"}
    ;; Left Section
    [:div {:class "flex items-center gap-2"}
     [:div {:class "flex items-center gap-1 px-2"}
      [:div {:class "w-2 h-2 rounded-full bg-red-500"}]
      [:div {:class "w-2 h-2 rounded-full bg-yellow-500"}]
      [:div {:class "w-2 h-2 rounded-full bg-green-500"}]]

     [:div {:class "w-[1px] h-6 bg-[#323232]"}]

     ;; Undo/Redo
     [:div {:class "flex items-center gap-1"}
      [:% rx/Button {:variant "ghost"
                     :size "sm"
                     :onClick onUndo
                     :disabled (not canUndo)
                     :class "h-7 w-7 p-0 text-gray-400 hover:text-gray-200 hover:bg-[#323232] disabled:opacity-30 disabled:cursor-not-allowed"
                     :title "Undo (Ctrl+Z)"}
       [:% lc/Undo {:class "w-3.5 h-3.5"}]]
      [:% rx/Button {:variant "ghost"
                     :size "sm"
                     :onClick onRedo
                     :disabled (not canRedo)
                     :class "h-7 w-7 p-0 text-gray-400 hover:text-gray-200 hover:bg-[#323232] disabled:opacity-30 disabled:cursor-not-allowed"
                     :title "Redo (Ctrl+Shift+Z)"}
       [:% lc/Redo {:class "w-3.5 h-3.5"}]]]

     [:div {:class "w-[1px] h-6 bg-[#323232]"}]

     ;; View Mode Toggle
     [:div {:class "flex gap-1"}
      [:% rx/Button {:variant (:? (=== viewMode "design") "default" "ghost")
                     :size "sm"
                     :onClick (fn [] (onViewModeChange "design"))
                     :class (+ "h-8 "
                               (:? (=== viewMode "design")
                                   "bg-[#404040] text-white hover:bg-[#4a4a4a]"
                                   "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"))}
       [:% lc/Layout {:class "w-4 h-4 mr-1"}]
       "Design"]

      [:% rx/Button {:variant (:? (=== viewMode "code") "default" "ghost")
                     :size "sm"
                     :onClick (fn [] (onViewModeChange "code"))
                     :class (+ "h-8 "
                               (:? (=== viewMode "code")
                                   "bg-[#404040] text-white hover:bg-[#4a4a4a]"
                                   "text-gray-400 hover:text-gray-200 hover:bg-[#323232]"))}
       [:% lc/Code2 {:class "w-4 h-4 mr-1"}]
       "Code"]]]

    ;; Spacer
    [:div {:class "flex-1"}]

    ;; Actions
    [:div {:class "flex gap-2"}
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Save {:class "w-4 h-4 mr-1"}]
      "Save"]
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Share2 {:class "w-4 h-4 mr-1"}]
      "Export"]
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Settings {:class "w-4 h-4"}]]
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"
                    :onClick onUndo
                    :disabled (not canUndo)}
      [:% lc/Undo {:class "w-4 h-4 mr-1"}]
      "Undo"]
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"
                    :onClick onRedo
                    :disabled (not canRedo)}
      [:% lc/Redo {:class "w-4 h-4 mr-1"}]
      "Redo"]
     [:% rx/Button {:variant "ghost"
                    :size "sm"
                    :class "h-8 text-gray-400 hover:text-gray-200 hover:bg-[#323232]"}
      [:% lc/Play {:class "w-4 h-4"}]]]]))

