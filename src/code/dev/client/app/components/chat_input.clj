(ns code.dev.client.app.components.chat-input
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]})

(defn.js ChatInput
  [{:# [onSendMessage]}]
  (var [input setInput] (r/useState ""))

  (var handleSubmit
       (fn [e]
         (. e (preventDefault))
         (when (. input (trim))
           (onSendMessage (. input (trim)))
           (setInput ""))))
  
  (return
   [:form {:onSubmit handleSubmit :className "flex gap-2"}
    [:% fg/Input
     {:value input
      :onChange (fn [e] (return (setInput e.target.value)))
      :placeholder "Type your message..."
      :className "flex-1"}]
    [:% fg/Button
     {:type "submit"
      :disabled (not (. input (trim)))
      :className "bg-gradient-to-br from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600"}
     [:% lc/Send {:className "w-4 h-4"}]]]))
