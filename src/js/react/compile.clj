(ns js.react.compile
  (:require [std.lib.walk :as walk]
            [std.string :as str]
            [std.lib :as h]
            [std.lang :as l]
            [js.react.compile-components :as c]
            [js.react.compile-directives :as d]
            [js.react.compile-flow :as flow]))

(defn ui-compile-layout
  [layout components]
  (walk/postwalk
   (fn [elem]
     (cond (and (vector? elem)
                (keyword? (first elem))
                (= "*" (namespace (first elem))))
           (d/compile-ui-directives elem components)
           
           
           (and (vector? elem)
                (keyword? (first elem))
                (namespace (first elem)))
           (c/compile-element-loop elem components layout-compile)           
           
           :else
           elem))
   layout))

(defn ui-compile
  [states layout components]
  (let [components (-> components
                       (c/components-resolve)
                       (c/components-expand))]
    (layout/layout-compile layout components)))



(comment
  
  '{:states {:var/html-code   ""
             :var/dsl-code    {:%/args  [""]}
             
             :var/history     {:%  (useLocalHistory "task.translate-html")}
             :var/history-idx 0
             :var/data        {:errored false
                               :warning false}
             :var/combined    {:%  (+ :var/dsl-code
                                      :var/html-code)}}
    
    :layout [:#/container
             [:#/toolbar
              [:ui/toolbar.button
               {:%/action [{:%/set :var/html-code
                            :from ""}]}]]
             [:*/for [[i p] pages]
              [:ui/button
               {:%/action [{:%/run (do (:var/history-idx 2)
                                       (return 1))}
                           {:%/set :var/history-idx
                            :from i}
                           {:%/set.async :var/history-idx
                            :from (async-fn :var/dsl-code)
                            :pending :var/pending
                            :error   :var/error
                            :transform (fn [])}]}
               p.text]]]
    
    :components
    {:#/toolbar.action.reset
     [:ui/toolbar.button
      {:%/action [[:%/set :var/html-code ""]]}
      [:ui/icon {:name "Clear"}]]}}
  
  [:ui/text-area
   {:%/value :var/html-code}]
  
  
  {:ui/icon  {:tag Icon}
   :ui/toolbar.button [:ui/button
                       {:class ["p-1"]}
                       [:ui/icon
                        {:name :props/name}]]
   
   :ui/button
   {:tag  :button
    :view {:type :action
           :key :onClick}}
   :ui/text-area
   {:tag  :textarea
    :view {:type :input
           :get :value
           :set :onChange}}}
  
  {}
  
  [:ui/button
   {:%/action [{:%/set :var/history-idx
                :from i}
               {:%/set-async :var/history-idx
                :from (async-fn :var/dsl-code)
                :pending :var/pending
                :error   :var/error
                :transform (fn [])}]}
   p.text]

  #_#_
             :var/history     {:%/fn    useLocalHistory
                               :%/args  ["task.translate-html"]}

  )
  
