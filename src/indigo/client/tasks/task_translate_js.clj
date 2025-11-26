(ns indigo.client.tasks.task-translate-js
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [indigo.client.ui-global :as global]
             [indigo.client.ui-common :as ui]]})

(defn.js TaskTranslateJs
  []
  (var pushHistory
       (fn [history item]
         (return
          (k/arr-pushl [(:.. history)] item))))

  (r/return-ui
   {:layout [:*/v
             {:gap 2
              :class ["w-full"]}
             [:#/editor.toolbar]
             [:*/v
              [:div {:class ["grid grid-cols-2 w-full" "gap-3"]}
               [:#/editor.input]
               [:#/editor.output]]]]
    
    :states {:var/input-code   {:%/fn box/useBox
                                :%/args [global/Global
                                         ["task.translate_js"
                                          "input_code"]]}
             :var/output-code    {:%/fn box/useBox
                                  :%/args [global/Global
                                           ["task.translate_js"
                                            "output_code"]]}
             :var/history        {:%/fn ui/useLocalHistory
                                  :%/args ["task.translate_js.history"]}
             :var/history-idx   {:%/fn box/useBox
                                 :%/args [global/Global
                                          ["task.translate_js"
                                           "history_idx"]]}}
    
    :triggers {:trigger/history
               {:%/watch  [:var/history-idx
                           :var/history]
                :%/action
                [{:%/do
                  (when (< :var/history-idx
                           (k/len :var/history))
                    (:var/input-code
                     (k/get-in :var/history
                               [:var/history-idx
                                "input"]))
                    (:var/output-code
                     (k/get-in :var/history
                               [:var/history-idx
                                "output"])))}]}}
    
    :actions {:action/translate-input
              [{:%/set-async :var/output-code
                :to (. (global/api-post "/api/translate/js"
                                   :var/input-code)
                       (then (fn [output]
                               (navigator.clipboard.writeText output)
                               (:var/history
                                (pushHistory :var/history
                                             {:input :var/input-code
                                              :output  output
                                              :t    (k/now-ms)
                                              :op   "translate-input"}))
                               (return output))))}
               {:%/set :var/history-idx
                :to (k/len :var/history)}]}
    
    :components
    [{:#/editor.input   [:*/v {:style {:max-width "100%"}}
                         [:ui/editor
                          {:language "javascript"
                           :%/value :var/input-code
                           :%/action :action/translate-input}]]
      
      :#/editor.output  [:*/v {:style {:max-width "100%"}}
                         [:ui/editor
                          {:language "clojure"
                           :%/value  :var/output-code}]]
      
      
      
      :#/editor.toolbar [:*/h {:h 10 :gap 1}
                         [:#/toolbar.translate-input]
                         [:#/toolbar.translate-input-clear]
                         [:*/pad]
                         [:*/pad]
                         [:*/pad]
                         [:#/toolbar.translate-output-clear]
                         [:#/toolbar.translate-output-copy]
                         [:*/pad]
                         [:#/toolbar.history-prev]
                         [:#/toolbar.history-curr]
                         [:#/toolbar.history-next]
                         [:#/toolbar.history-delete]]
      
      :#/toolbar.translate-input [:ui/button
                                  {:%/action :action/translate-input
                                   :disabled (== "" :var/input-code)}
                                  "TO JS DSL"]
      
      :#/toolbar.translate-input-clear [:ui/button
                                        {:disabled (== "" :var/input-code)
                                         :%/action [{:%/set :var/input-code
                                                     :to ""}]}
                                        "Clear"]
      
      :#/toolbar.translate-output-clear [:ui/button
                                         {:disabled (== "" :var/output-code)
                                          :%/action [{:%/set :var/output-code
                                                      :to ""}]}
                                         "Clear"]
      
      :#/toolbar.translate-output-copy [:ui/button
                                        {:disabled (== "" :var/output-code)
                                         :%/action [{:%/do
                                                     (navigator.clipboard.writeText :var/output-code)}]}
                                        "Copy"]
      
      :#/toolbar.history-prev [:ui/button.icon
                               {:icon "ChevronLeft"
                                :disabled (> 2 (k/len :var/history))
                                :%/action [{:%/dec :var/history-idx
                                            :mod (k/len :var/history)}]}]
      :#/toolbar.history-curr [:ui/button
                               {:style {:width "100px"}
                                :disabled (== 0 (k/len :var/history))
                                :%/action [{:%/set :var/input-code
                                            :to (k/get-in :var/history
                                                          [:var/history-idx
                                                           "input"])}
                                           {:%/set :var/output-code
                                            :to (k/get-in :var/history
                                                          [:var/history-idx
                                                           "output"])}]}
                               (:? (== (k/len :var/history) 0)
                                   "No History"
                                   (+ (+ :var/history-idx 1) " of " (k/len :var/history)))]
      
      :#/toolbar.history-next [:ui/button.icon
                               {:icon "ChevronRight"
                                :disabled (> 2 (k/len :var/history))
                                :%/action [{:%/inc :var/history-idx
                                            :mod (k/len :var/history)}]}]
      :#/toolbar.history-delete [:ui/button
                                 {:disabled (== 0 (k/len :var/history))
                                  :%/action
                                  [{:%/do
                                    (if (< (k/len :var/history) 2)
                                      (do (:var/history [])
                                          (:var/history-idx 0))
                                      (do (:var/history
                                           (k/arr-omit :var/history
                                                       :var/history-idx))
                                          (when (not= :var/history-idx 0)
                                            (:var/history-idx
                                             (- :var/history-idx 1)))))}]}
                                 "Delete"]
      :#/toolbar.history-reset [:ui/button
                                {:%/action [{:%/set :var/history
                                             :to []}
                                            {:%/set :var/history-idx
                                             :to 0}
                                            {:%/set :var/input-code
                                             :to ""}
                                            {:%/set :var/output-code
                                             :to ""}]}
                                "Reset"]}
     ui/+ui-common+]}))
