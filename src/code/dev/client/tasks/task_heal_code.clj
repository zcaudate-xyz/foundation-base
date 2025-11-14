(ns code.dev.client.tasks.task-heal-code
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [code.dev.client.ui-global :as global]
             [code.dev.client.ui-common :as ui]]})

(defn.js TaskHealCode
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
               [:#/editor.healed]]]
             [:*/v
              {:class ["Diffviewer"
                       
                       "overflow-y-auto"
                       "overflow-x-auto"
                       "grid grid-cols-1 w-full"]}
              [:ui/diffview
               {:oldValue :var/input-code
                :newValue :var/healed-code}]]]
    
    :states {:var/input-code   {:%/fn box/useBox
                                :%/args [global/Global
                                         ["task.heal_code"
                                          "input_code"]]}
             :var/healed-code    {:%/fn box/useBox
                                  :%/args [global/Global
                                           ["task.heal_code"
                                            "healed_code"]]}
             :var/history        {:%/fn ui/useLocalHistory
                                  :%/args ["task.heal_code.history"]}
             :var/history-idx   {:%/fn box/useBox
                                 :%/args [global/Global
                                          ["task.heal_code"
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
                    (:var/healed-code
                     (k/get-in :var/history
                               [:var/history-idx
                                "healed"])))}]}}
    
    :actions {:action/heal-input
              [{:%/set-async :var/healed-code
                :to (. (global/api-post "/api/heal"
                                        :var/input-code)
                       (then (fn [healed]
                               (navigator.clipboard.writeText healed)
                               (:var/history
                                (pushHistory :var/history
                                             {:input :var/input-code
                                              :healed  healed
                                              :t    (k/now-ms)
                                              :op   "heal-input"}))
                               (return healed))))}
               {:%/set :var/history-idx
                :to (k/len :var/history)}]}
    
    :components
    [{:#/editor.healed  [:*/v {:style {:max-width "100%"}}
                         [:ui/editor
                          {:language "clojure"
                           :%/value  :var/healed-code}]]
      
      :#/editor.input   [:*/v {:style {:max-width "100%"}}
                         [:ui/editor
                          {:language "clojure"
                           :%/value :var/input-code
                           :%/action :action/heal-input}]]
      
      :#/editor.toolbar [:*/h {:h 10 :gap 1}
                         [:#/toolbar.heal-input]
                         [:#/toolbar.heal-input-clear]
                         [:*/pad]
                         [:*/pad]
                         [:*/pad]
                         [:#/toolbar.heal-output-clear]
                         [:#/toolbar.heal-output-copy]
                         [:*/pad]
                         [:#/toolbar.history-prev]
                         [:#/toolbar.history-curr]
                         [:#/toolbar.history-next]
                         [:#/toolbar.history-delete]]
      
      :#/toolbar.heal-input [:ui/button
                             {:%/action :action/heal-input
                              :disabled (== "" :var/input-code)}
                             "HEAL"]
      
      :#/toolbar.heal-input-clear [:ui/button
                                   {:disabled (== "" :var/input-code)
                                    :%/action [{:%/set :var/input-code
                                                :to ""}]}
                                   "Clear"]
      
      :#/toolbar.heal-output-clear [:ui/button
                                    {:disabled (== "" :var/healed-code)
                                     :%/action [{:%/set :var/healed-code
                                                 :to ""}]}
                                    "Clear"]
      
      :#/toolbar.heal-output-copy [:ui/button
                                   {:disabled (== "" :var/healed-code)
                                    :%/action [{:%/do
                                                (navigator.clipboard.writeText :var/healed-code)}]}
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
                                           {:%/set :var/healed-code
                                            :to (k/get-in :var/history
                                                          [:var/history-idx
                                                           "healed"])}]}
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
                                            {:%/set :var/healed-code
                                             :to ""}]}
                                "Reset"]}
     ui/+ui-common+]}))
