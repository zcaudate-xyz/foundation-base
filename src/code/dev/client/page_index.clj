(ns code.dev.client.page-index
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:port 1312}
   :require [[js.react :as r :include [:dom]]
             [xt.lang.base-client :as client]
             [code.dev.client.ui-common :as ui]]})

(defglobal.js Root nil)

(defn.js ToolbarButton
  [#{[icon
      (:.. props)]}]
  (return
   [:button
    #{[:class   ["btn" "btn-sm"]
       (:.. props)]}
    [:% ui/Icon
     #{[:name  icon
        :color "black"
        :size  18]}]]))


(defn.js TaskSmaller
  )

[:*/v {:gap 3
       :h :full}]

[:*/h {:gap 3
       :h :full}]

(defn.js TaskTranslateHtml
  []
  (var [htmlCode  setHtmlCode]    (r/useState ""))
  (var [dslCode setDslCode]   (r/useState ""))
  (var [current setCurrent] (r/useState "htmlCode"))
  (var [history setHistory]
       (-/useLocalHistory "task.translate-html"))
  
  (var [historyIndex setHistoryIndex] (r/useState 0))


  (return
   (r/ui [:#/container
          [:#/action.copy-dsl
           {:%/action :var/html-code}]
          
          [:#/toolbar
           [:#/toolbar.history-next
            {:%/action [[:%/set [:var/dsl-code ""]]
                        [:%/set [:var/html-code ""]]]}]
           [:#/toolbar.history-prev]]
          
          [:div
           {:class ["h-full"
                    "flex" "flex-row"
                    "gap-3"]}
           [:#/container
            [:#/toolbar
             [:#/toolbar.history-next]]
            [:#/editor.html-code
             {:%/sync :var/html-code}]]
           [:#/container
            [:#/toolbar
             [:#/toolbar.history-next]]
            [:#/editor.dsl-code
             {:%/sync :var/html-code}]]]
          [:#/actions]
          [:#/editor.preview
           {:%/in :var/html-code}]]
     {:#/action.clear-html    [:g/toolbar-button
                               {:%/action [[:%/set [:var/dsl-code ""]]
                                           [:%/set [:var/html-code ""]]]
                                :class   "btn"}
                               "Clear HtmlCode"]
      :#/action.copy-dsl      [:g/toolbar-button
                               {:id      "copy-btn"
                                :class   ["absolute"          "bg-gray-500"
                                          "duration-200"      "font-medium"
                                          "hover:bg-gray-600" "px-3"
                                          "py-1.5"            "right-8"
                                          "rounded-md"        "text-white"
                                          "text-xs"           "top-3"
                                          "transition-all"]
                                :onClick (fn [])}
                               "Copy"]
      :#/action.from-html      [:button
                                {:class   "btn btn-accent"
                                 :%/action [[:%/fetch ["/api/translate/from-html"
                                                       :var/html-code]
                                             {:format :json
                                              :as result
                                              :busy  :var/is-busy}]
                                            [:%/set [:var/dsl-code result]] 
                                            {:%/set [:var/history [{:op "from-html"
                                                                    :html :var/html-code
                                                                    :dsl  :var/dsl-code}
                                                                   (:.. :var/history)]]}]}
                                [:g/icon
                                 {:name :lucide/arrow-left}]
                                "From Html"]
      :#/action.to-html        [:button {:class   "btn btn-accent"
                                         :onClick (fn ^{:- [:async]} submit
                                                    []
                                                    (var res
                                                         (await (fetch "/api/translate/to-html"
                                                                       {:body dslCode :method "POST"})))
                                                    (var #{data}
                                                         (await (res.json)))
                                                    (setHtmlCode data)
                                                    (setCurrent "htmlCode")
                                                    (setHistory [{:op "to-html"
                                                                  :html   htmlCode
                                                                  :dsl    dslCode} (:.. history)]))}
                                "To Html"]
      :#/actions               [:div {:class ["grid" "grid-cols-3" "gap-3"]}
                                :*/children]
      :#/container             [:div {:class ["flex"  "flex-col"
                                              "size-full" "p-8"
                                              "gap-3"]}
                                :*/children]
      :#/editor.html-code      [:fieldset {:class ["fieldset"
                                                   "size-full"]}
                                [:% ui/CodeEditor
                                 {:language "html"
                                  :value    htmlCode
                                  :onChange setHtmlCode}]
                                [:legend {:class "fieldset-legend"}
                                 "HTML"]]
      :#/editor.dsl-code       [:fieldset {:class ["fieldset"
                                                   "size-full"]}
                                [:% ui/CodeEditor
                                 {:language "clojure"
                                  :value    dslCode
                                  :onChange setDslCode}]
                                [:legend {:class "fieldset-legend"}
                                 "DSL"]]
      :#/editor.preview        [:div {:dangerouslySetInnerHTML
                                      {"__html" :*/input}}]
      :#/toolbar               [:div]
      :#/toolbar.html-clear    [:% -/ToolbarButton
                                {:onClick (fn [] ())}]
      :#/toolbar.history-next  [:button
                                {:class   ["btn"]}
                                [:% ui/Icon
                                 {:name  "SkipForward"
                                  :color "black"
                                  :size  18
                                  }]]
      :#/toolbar.history-prev  [:button
                                {:class   ["btn"]}
                                [:% ui/Icon
                                 {:name  "SkipBack"
                                  :color "black"
                                  :size  18
                                  }]]})))


(defn.js TaskTranslateHtml
  []
  (var [htmlCode  setHtmlCode]    (r/useState ""))
  (var [dslCode setDslCode]   (r/useState ""))
  (var [current setCurrent] (r/useState "htmlCode"))
  (var [history setHistory]
       (-/useLocalHistory "task.translate-html"))
  
  (var [historyIndex setHistoryIndex] (r/useState 0))
  (return
   (r/ui [:ui/container
          [:ui/action.copy-dsl]
          [:ui/toolbar
           #_#_#_[:ui/toolbar.history-clear]
           [:ui/toolbar.history-next]
           [:ui/toolbar.history-prev]
           [:ui/toolbar.to-html]
           [:ui/toolbar.to-dsl]]
          [:div
           {:class ["h-full"
                    "flex" "flex-row"
                    "gap-3"]}
           [:ui/container
            [:ui/toolbar
             #_#_[:ui/toolbar.history-clear]
             [:ui/toolbar.history-next]]
            [:ui/editor.html-code]]
           [:ui/container
            [:ui/toolbar
             #_#_[:ui/toolbar.history-clear]
             [:ui/toolbar.history-next]]
            [:ui/editor.dsl-code]]]
          [:ui/actions
           [:ui/action.from-html]
           [:ui/action.to-html]
           #_[:ui/action.clear-html]]
          [:ui/editor.preview]]
     {:ui/action.clear-html    [:button {:class   "btn"
                                         
                                         :onClick (fn []
                                                    (setDslCode "")
                                                    (setHtmlCode "")
                                                    (setCurrent "htmlCode"))}
                                "Clear HtmlCode"]
      :ui/action.copy-dsl    [:button {:id      "copy-btn"
                                       :class   ["absolute"          "bg-gray-500"
                                                 "duration-200"      "font-medium"
                                                 "hover:bg-gray-600" "px-3"
                                                 "py-1.5"            "right-8"
                                                 "rounded-md"        "text-white"
                                                 "text-xs"           "top-3"
                                                 "transition-all"]
                                       :onClick (fn [])}
                              "Copy"]
      :ui/action.from-html      [:button {:class   "btn btn-accent"
                                          :onClick (fn ^{:- [:async]}
                                                     submit
                                                     []
                                                     (var res
                                                          (await (fetch "/api/translate/from-html"
                                                                        {:body htmlCode :method "POST"})))
                                                     (var #{data}
                                                          (await (res.json)))
                                                     (setDslCode data)
                                                     (setCurrent "dslCode")
                                                     (setHistory [{:op "from-html"
                                                                   :html   htmlCode
                                                                   :dsl    dslCode}
                                                                  (:.. history)]))}
                                 [:% ui/Icon
                                  {:name  "FileCode"
                                   :color "black"
                                   :size  18
                                   :style {:marginRight "8px"}}]
                                 "From Html"]
      :ui/action.to-html        [:button {:class   "btn btn-accent"
                                          :onClick (fn ^{:- [:async]} submit
                                                     []
                                                     (var res
                                                          (await (fetch "/api/translate/to-html"
                                                                        {:body dslCode :method "POST"})))
                                                     (var #{data}
                                                          (await (res.json)))
                                                     (setHtmlCode data)
                                                     (setCurrent "htmlCode")
                                                     (setHistory [{:op "to-html"
                                                                   :html   htmlCode
                                                                   :dsl    dslCode} (:.. history)]))}
                                 "To Html"]
      :ui/actions               [:div {:class ["grid" "grid-cols-3" "gap-3"]}
                                 :*/children]
      :ui/container             [:div {:class ["flex"  "flex-col"
                                               "size-full" "p-8"
                                               "gap-3"]}
                                 :*/children]
      :ui/editor.html-code      [:fieldset {:class ["fieldset"
                                                    "size-full"]}
                                 [:% ui/CodeEditor
                                  {:language "html"
                                   :value    htmlCode
                                   :onChange setHtmlCode}]
                                 [:legend {:class "fieldset-legend"}
                                  "HTML"]]
      :ui/editor.dsl-code       [:fieldset {:class ["fieldset"
                                                    "size-full"]}
                                 [:% ui/CodeEditor
                                  {:language "clojure"
                                   :value    dslCode
                                   :onChange setDslCode}]
                                 [:legend {:class "fieldset-legend"}
                                  "DSL"]]
      :ui/editor.preview        [:div {:dangerouslySetInnerHTML {"__html" htmlCode}}]
      :ui/toolbar               [:div]
      #_#_:ui/toolbar.html-clear [:button
                              {:class   ["btn" "btn-sm" "flex" "pr-1"]}
                              [:% ui/Icon
                               {:name  "Trash2"
                                :color "black"
                                :size  18
                                }]]
      :ui/toolbar.html-clear [:% -/ToolbarButton
                              {:onClick (fn [] ())}]
      :ui/toolbar.history-next  [:button
                                 {:class   ["btn"]}
                                 [:% ui/Icon
                                  {:name  "SkipForward"
                                   :color "black"
                                   :size  18
                                   }]]
      :ui/toolbar.history-prev  [:button
                                 {:class   ["btn"]}
                                 [:% ui/Icon
                                  {:name  "SkipBack"
                                   :color "black"
                                   :size  18
                                   }]]})))





#_
(defn.js TaskTranslateHtml
  []
  (var [input  setInput]    (r/useState ""))
  (var [output setOutput]   (r/useState ""))
  (var [current setCurrent] (r/useState "input"))
  
  (return
   [:div
    {:class ["flex" "flex-col"
             "size-full"
             "p-8" "gap-3"]}
    (:? (== current "input")
        [:fieldset {:class ["fieldset"]}
         [:legend {:class "fieldset-legend"}
          "Enter HTML Input"]
         [:textarea
          {:class   ["textarea" "font-mono"
                     "textarea-accent"
                     "h-100" "w-full"]
           :placeholder "Type here"
           :value input
           :onChange (fn [e]
                       (setInput e.target.value))}]
         [:p {:class "label"}
          "This is the input html"]]

        [:fieldset {:class ["fieldset"]}
         [:legend {:class "fieldset-legend"}
          "Hiccup output"]
         [:textarea
          {:class   ["textarea" "font-mono"
                     "textarea-accent"
                     "h-100" "w-full"]
           :disabled true
           :value output}]
         [:p {:class "label"}
          "This is the translated hiccup"]])
    [:div {:class ["grid" "grid-cols-2" "gap-3"]}
     [:button
      {:class "btn btn-accent"
       :onClick (fn ^{:- [:async]}
                  submit []
                  (var res     (await (fetch "/api/translate/html"
                                             {:method "POST"
                                              :body input})))
                  (var #{data} (await (res.json)))
                  
                  (setOutput data)
                  (setCurrent "output"))}
      "To Hiccup"]
     [:button
      {:class "btn"
       :onClick (fn []
                  (console.log "hello"))}
      "Clear Input"]]]) )

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
   [:div {:class "flex-auto"}
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

(def app-defaults1
  {:app/top     [:div
                 {:class "flex flex-col bg-gray-200"}]
   :app/header  [:div
                 {:class ["navbar" "bg-base-100" "shadow-sm"]}]
   :app/body    [-/Body]
   :app/footer  [:nav
                 {:class ["bg-white" "shadow-md" "p-4" "sticky" "top-0" "z-50"]}
                 [:div {:class "flex items-center"}
                  [:a {:class "text-2xl font-display text-brand-dark font-bold"}
                   :*/children]]]})

(defn.js App
  []
  (r/init []
    (client/client-ws "localhost" 1312 {}))
  (return
   (r/ui [:app/top
          #_[:app/header
             ["HELLO WORLD"]]
          [:app/body]
          #_[:app/footer]]
     -/app-defaults1
     )))

(defn.js main
  []
  (var rootElement (document.getElementById "root"))
  (when (not -/Root)
    (:= -/Root (ReactDOM.createRoot rootElement)))
  (. -/Root (render [:% -/App]))
  (return true))

(comment
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










