(ns indigo.client.app.components.chat-message
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]]})

(defn.js ChatMessage
  [{:# [message]}]
  (var isUser
       (== message.sender "user"))
  (return
   [:div {:className (+ "flex "
                        (:? isUser
                            "justify-end"
                            "justify-start"))}
    [:div {:className (+ "max-w-[80%] md:max-w-[60%] rounded-2xl px-4 py-3 "
                         (:? isUser
                             "bg-gradient-to-br from-purple-500 to-pink-500 text-white"
                             "bg-gray-100 text-gray-900"))}
     [:p {:className "break-words"}
      message.text]
     [:p {:className (+ "text-xs mt-1 "
                        (:? isUser
                            "text-purple-100"
                            "text-gray-400"))}
      message.timestamp
      #_(. message.timestamp
         (toLocaleTimeString []
                             {:hour "2-digit"
                              :minute "2-digit"}))]]]))
