(ns code.dev.client.app.components.inspector
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]]})

(defn.js Inspector
  [{:# [object]}]
  (var renderObjectDetails
       (fn []
         (when (not object)
           (return [:div {:className "text-sm text-gray-500 italic"} "No object selected"]))

         (var details [])

         (if (and (== (typeof object) "object") (not= object nil))
           (. details
              (push [:div {:key       "self"
                           :className "mb-2"}
                     [:span {:className "text-blue-600"}
                      "self"]
                     ": "
                     (. String
                        (or object.result object))]))

           (when (and object.result
                      (== (typeof object.result) "object"))
             (. (Object.entries object.result)
                (forEach (fn [[key value]]
                           (. details (push
                                       [:div {:key key :className "mb-1"}
                                        [:span {:className "text-blue-600"} key] ": " (. String value)])))))))
         (. details
            (push [:div {:key       "value"
                         :className "mb-2"}
                   [:span {:className "text-blue-600"}
                    "value"]
                   ": "
                   (. String object.result)]))
         (. details (push
                     [:div {:key "type" :className "mb-1"}
                      [:span {:className "text-blue-600"} "type"] ": " object.type]))
         (return details)))

  (return
   [:div {:className "flex flex-col h-full bg-white"}
    [:div {:className "px-4 py-2 bg-gray-200 border-b"}
     [:h2 {:className "text-sm"} "Inspector"]]

    [:div {:className "border-b p-3"}
     [:div {:className "text-xs text-gray-600 mb-2"} "Instance Variables"]
     [:% fg/ScrollArea {:className "h-32"}
      [:div {:className "font-mono text-sm"}
       (renderObjectDetails)]]]

    [:div {:className "flex-1 p-3"}
     [:div {:className "text-xs text-gray-600 mb-2"} "Object"]
     [:% fg/ScrollArea {:className "h-full"}
      [:pre {:className "font-mono text-sm"}
       (:? object (JSON.stringify object nil 2) "nil")]]]]))
