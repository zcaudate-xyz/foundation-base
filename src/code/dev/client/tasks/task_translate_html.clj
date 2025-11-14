(ns code.dev.client.tasks.task-translate-html
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [code.dev.client.ui-global :as global]
             [code.dev.client.ui-common :as ui]]})

(defn.js TaskTranslateHtml
  []
  (var pushHistory
       (fn [history item]
         (return
          (k/arr-pushl [(:.. history)] item))))
  
  (r/return-ui
   {:layout [:*/v
             {:gap 2}
             [:#/editor.toolbar]
             [:div {:class ["grid grid-cols-2 w-full" "gap-3"]}
              [:#/editor.html]
              [:#/editor.dsl]]
             [:ui/html
              {:input :var/html-code}]]
    
    :states {:var/html-code   {:%/fn box/useBox
                               :%/args [global/Global
                                        ["task.translate_html"
                                         "html_code"]]}
             :var/dsl-code     {:%/fn box/useBox
                                :%/args [global/Global
                                         ["task.translate_html"
                                          "dsl_code"]]}
             :var/history        {:%/fn ui/useLocalHistory
                                  :%/args ["task.translate_html.history"]}
             :var/history-idx  {:%/fn box/useBox
                                :%/args [global/Global
                                         ["task.translate_html"
                                          "history_idx"]]}}
    
    :triggers {:trigger/history
               {:%/watch  [:var/history-idx
                           :var/history]
                :%/action
                [{:%/do
                  (when (< :var/history-idx
                           (k/len :var/history))
                    (:var/html-code
                     (k/get-in :var/history
                               [:var/history-idx
                                "html"]))
                    (:var/dsl-code
                     (k/get-in :var/history
                               [:var/history-idx
                                "dsl"])))}]}}
    
    :actions {:action/from-html
              [{:%/set-async :var/dsl-code
                :to (. (global/api-post "/api/translate/from-html"
                                   :var/html-code)
                       (then (fn [dsl]
                               (navigator.clipboard.writeText dsl)
                               (:var/history
                                (pushHistory :var/history
                                             {:html :var/html-code
                                              :dsl  dsl
                                              :t    (k/now-ms)
                                              :op   "from-html"}))
                               (return dsl))))}
               {:%/set :var/history-idx
                :to (k/len :var/history)}]
              
              :action/to-html
              [{:%/set-async :var/html-code
                :to (. (global/api-post "/api/translate/to-html"
                                        :var/dsl-code)
                       (then (fn [html]
                               (navigator.clipboard.writeText html)
                               (:var/history
                                (pushHistory :var/history
                                             {:html html
                                              :dsl  :var/dsl-code
                                              :t    (k/now-ms)
                                              :op   "from-html"}))
                               (return html))))}
               {:%/set :var/history-idx
                :to (k/len :var/history)}]}
    
    :components
    [{:#/editor.dsl  [:*/v {:style {:max-width "100%"}}
                      [:ui/editor
                       {:language "clojure"
                        :%/value  :var/dsl-code
                        :%/action :action/to-html}]]
      
      :#/editor.html [:*/v {:style {:max-width "100%"}}
                      [:ui/editor
                       {:language "html"
                        :%/value :var/html-code
                        :%/action :action/from-html}]]
      
      :#/editor.toolbar [:*/h {:h 10 :gap 1}
                         [:#/toolbar.from-html]
                         [:#/toolbar.from-html-clear]
                         [:#/toolbar.from-html-copy]
                         [:*/pad]
                         [:*/pad]
                         [:*/pad]
                         [:#/toolbar.to-html]
                         [:#/toolbar.to-html-clear]
                         [:#/toolbar.to-html-copy]
                         [:*/pad]
                         [:#/toolbar.history-prev]
                         [:#/toolbar.history-curr]
                         [:#/toolbar.history-next]
                         [:#/toolbar.history-delete]]
      
      :#/toolbar.from-html [:ui/button
                            {:%/action :action/from-html
                             :disabled (== "" :var/html-code)}
                            "To DSL"
                            [:ui/icon
                             {:name "ArrowRight"
                              :size  16}]]
      
      :#/toolbar.from-html-clear [:ui/button
                                  {:disabled (== "" :var/html-code)
                                   :%/action [{:%/set :var/html-code
                                               :to ""}]}
                                  "Clear"]
      
      :#/toolbar.from-html-copy [:ui/button
                                 {:%/action [{:%/do
                                              (navigator.clipboard.writeText :var/html-code)}]
                                  :disabled (== "" :var/html-code)}
                                 "Copy"]

      :#/toolbar.to-html   [:ui/button
                            {:%/action :action/to-html
                             :disabled (== "" :var/dsl-code)}
                            [:ui/icon
                             {:name "ArrowLeft"
                              :size  16}]
                            "To HTML"]

      :#/toolbar.to-html-clear [:ui/button
                                {:disabled (== "" :var/dsl-code)
                                 :%/action [{:%/set :var/dsl-code
                                             :to ""}]}
                                "Clear"]

      :#/toolbar.to-html-copy [:ui/button
                               {:disabled (== "" :var/dsl-code)
                                :%/action [{:%/do
                                            (navigator.clipboard.writeText :var/dsl-code)}]}
                               "Copy"]
      
      :#/toolbar.history-prev [:ui/button.icon
                               {:icon "ChevronLeft"
                                :disabled (> 2 (k/len :var/history))
                                :%/action [{:%/dec :var/history-idx
                                            :mod (k/len :var/history)}]}]
      :#/toolbar.history-curr [:ui/button
                               {:style {:width "100px"}
                                :disabled (== 0 (k/len :var/history))
                                :%/action [{:%/set :var/html-code
                                            :to (k/get-in :var/history
                                                          [:var/history-idx
                                                           "html"])}
                                           {:%/set :var/dsl-code
                                            :to (k/get-in :var/history
                                                          [:var/history-idx
                                                           "dsl"])}]}
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
                                            {:%/set :var/html-code
                                             :to ""}
                                            {:%/set :var/dsl-code
                                             :to ""}]}
                                "Reset"]}
     ui/+ui-common+]}))
