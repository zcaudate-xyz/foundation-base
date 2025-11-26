(ns indigo.client.app.components.transcript
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]]})

;; Interface definitions are removed as per spec
;; interface TranscriptProps { ... }

(defn.js Transcript
  [{:# [messages]}]
  (var scrollRef (r/useRef nil))

  (r/useEffect
   (fn []
     (when scrollRef.current
       (:= scrollRef.current.scrollTop scrollRef.current.scrollHeight)))
   [messages])

  (return
   [:div {:className "flex flex-col h-full bg-white border-r"}
    ;; Title
    [:div {:className "px-4 py-2 bg-gray-200 border-b"}
     [:h2 {:className "text-sm"} "Transcript"]]

    ;; Messages
    [:% fg/ScrollArea {:className "flex-1 p-3" :ref scrollRef}
     [:div {:className "space-y-1"}
      (. messages (map (fn [message index]
                         (return [:div {:key index :className "font-mono text-sm text-gray-800"}
                                  message]))))]]

    ;; Clear Button
    [:div {:className "px-3 py-2 bg-gray-50 border-t"}
     [:% fg/Button {:size "sm" :variant "outline"}
      "Clear"]]]))
