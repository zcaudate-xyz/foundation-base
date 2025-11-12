(ns code.dev.client.page-index
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:port 1312}
   :require [[js.react :as r :include [:dom]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [js.react.ext-box :as box]
             [code.dev.client.ui-common :as ui]]})

(defglobal.js Root nil)

(def.js Global
  (box/attachLocalStorage
   "code.dev"
   (box/createBox {:task.translate-html {:html-code ""
                                         :dsl-code  ""}})))


(defn.js api-post
  [url body]
  (return
   (. (fetch url
             {:body body
              :method "POST"})
      (then (fn [res] (return (res.json))))
      (then (fn [#{data}] (return data))))))


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
                               :%/args [-/Global
                                        ["task.translate_html"
                                         "html_code"]]}
             :var/dsl-code    {:%/fn box/useBox
                               :%/args [-/Global
                                        ["task.translate_html"
                                         "dsl_code"]]}
             :var/history        {:%/fn ui/useLocalHistory
                                  :%/args ["task.translate_html.history"]}
             :var/history-idx   0}
    
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
                :to (. (-/api-post "/api/translate/from-html"
                                   :var/html-code)
                       (then (fn [dsl]
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
                :to (. (-/api-post "/api/translate/to-html"
                                   :var/dsl-code)
                       (then (fn [html]
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
    (merge
     {:#/editor.dsl  [:*/v {:style {:max-width "100%"}}
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
                                 {:%/action []
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
                                :%/action []}
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
     ui/+ui-common+)}))





(defn.js TaskTest
  []
  (r/return-ui
   {:states {:var/html-code   "<p>hello</p>"}
    :layout [:ui/html
             {:input :var/html-code}]
    :components
    {:ui/html
     {:tag :div
      :props {:dangerouslySetInnerHTML
              {"__html" :props/input}}
      :children []}}}))

(defn.js TaskTest
  []
  (r/return-ui
   {:states {:var/html-code   "<p>hello</p>"}
    :layout [:ui/html
             {:input :var/html-code}]
    :components
    {:ui/html
     {:tag :div
      :props {:dangerouslySetInnerHTML
              {}}
      :children [:props/input]}}}))


(defn.js TaskToJsDSL
  []
  (return
   [:fieldset {:class "fieldset"}
    [:legend {:class "fieldset-legend"}
     "What is your name?"]
    [:input {:type        "text"
             :class   "input"
             :placeholder "Type here"}]
    [:p {:class "label"}
     "Optional"]]))

(defn.js Body
  [#{context}]
  (var #{api} (:? context
                  (r/useContext context)
                  {}))
  (return
   [:div
    {:class ["flex" "grow" "p-1"]}
    [:% ui/TabComponent
     {:pages [{:title "HTML"
               :content -/TaskTranslateHtml}
              {:title "Layout and Heal Code"
               :content -/TaskToJsDSL}
              {:title "To JS DSL"
               :content -/TaskToJsDSL}
              {:title "To JS DSL"
               :content -/TaskToJsDSL}
              ]}]]))


(defn.js App
  []
  (r/init []
    (client/client-ws "localhost" 1312 {}))
  (return
   (r/ui [:app/top
          [:app/body]]
     {:app/top     [:div
                    {:class ["flex flex-col w-full"]
                     :style {:top 0 :bottom 0}}]
      
      :app/body    [:% -/Body]})))

(defn.js main
  []
  (var rootElement (document.getElementById "root"))
  (when (not -/Root)
    (:= -/Root (ReactDOM.createRoot rootElement)))
  (. -/Root (render [:% -/App]))
  (return true))



(comment
  :app/header  [:div
                {:class ["navbar" "bg-base-100" "shadow-sm"]}]
  
  :app/footer  [:nav
                {:class ["bg-white" "shadow-md" "p-4" "sticky" "top-0" "z-50"]}
                [:div {:class "flex items-center"}
                 [:a {:class "text-2xl font-display text-brand-dark font-bold"}
                  :*/children]]]
  
  (:form @App)
  (std.block/layout
   (std.lib.walk/postwalk
    (fn [x]
      (if (map? x)
        (let [v (or (:class x)
                    (:classname x))
              v (if (string? v)
                  [v]
                  v)]
          (-> x
              (assoc :class v)
              (dissoc :class :classname)))
        x))
    (std.html/tree
     (slurp "src/code/dev/server/transform.txt")))))










