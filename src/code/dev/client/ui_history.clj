(ns code.dev.client.ui-history
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.base-lib :as k]
             [code.dev.client.ui-common :as ui]]
   :export [MODULE]})

(defn.js pushHistory
  [history item]
  (return
   (k/arr-pushl [(:.. history)] item)))

(defn.js getLocalStore
  {:added "0.1"}
  [storage-key]
  (var stored (. localStorage (getItem storage-key)))
  (try
    (:= stored (JSON.parse stored))
    (catch e
        (:= stored nil)))
  (return stored))

(defn.js useLocalHistory
  {:added "0.1"}
  [history-key]
  (var [history setHistory] (r/useState
                             (or (-/getLocalStore history-key)
                                 [])))
  (var historyStr (JSON.stringify history))
  (r/watch [historyStr]
    (. localStorage (setItem history-key
                             historyStr)))
  (return [history setHistory]))


(defmacro defcomponent.js [& args])

(def +history-components+
  '{:#/toolbar.history-prev
    [:ui/button.icon
     {:icon "ChevronLeft"
      :disabled (> 2 (k/len :var/history))
      :%/action [{:%/dec :var/history-idx
                  :mod (k/len :var/history)}]}]

    :#/toolbar.history-curr
    [:ui/button
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
    
    :#/toolbar.history-next
    [:ui/button.icon
     {:icon "ChevronRight"
      :disabled (> 2 (k/len :var/history))
      :%/action [{:%/inc :var/history-idx
                  :mod (k/len :var/history)}]}]

    :#/toolbar.history-delete
    [:ui/button
     {:disabled (== 0 (k/len :var/history))
      :%/action [{:%/do
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

    :#/toolbar.history-reset
    [:ui/button
     {:%/action [{:%/set :var/history
                  :to []}
                 {:%/set :var/history-idx
                  :to 0}]}
     "Reset"]})

(defcomponent.js History
  
  {:inputs [:var/controls
            :var/history-key
            :var/history-idx-key]
   :states {:var/history
            {:%/fn   r/useStateFor
             :%/args [:var/controls :var/history-key]}
            :var/history-idx
            {:%/fn   r/useStateFor
             :%/args [:var/controls :var/history-key]}}

   :layout [:*/h {:h 10 :gap 1}
            [:#/toolbar.history-prev]
            [:#/toolbar.history-curr]
            [:#/toolbar.history-next]
            [:#/toolbar.history-delete]]

   :components
   [+history-components+
    ui/+ui-common+]})


(comment
  (defn.js History
    [#{controls
       (:= historyKey "history")
       (:= historyIndexKey "historyIdx")}]
    
    (r/return-ui
     {:layout [:*/h {:h 10 :gap 1}
               [:#/toolbar.history-prev]
               [:#/toolbar.history-curr]
               [:#/toolbar.history-next]
               [:#/toolbar.history-delete]]

      :inputs [:var/controls
               :var/history-key
               :var/history-idx-key] 
      
      :states {:var/history        {:%/fn   r/useStateFor
                                    :%/args [:var/controls :var/history-key]}
               :var/history-idx    {:%/fn   r/useStateFor
                                    :%/args [:var/controls :var/history-key]}}

      :components
      (merge
       {:#/toolbar.history-prev [:ui/button.icon
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
       ui/+ui-common+)})))
