(ns code.dev.client.ui-common
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r :include [:dom :fn]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]]
   :export [MODULE]})

(defn.js TabComponent
  [#{[controls
      (:= key "current")
      (:= pages [])]}]
  (var [current
        setCurrent] (:? controls
                        (r/useStateFor
                         controls key)
                        (r/useState 0)))
  (var CurrentPage (. pages [current]))
  (return
   (r/ui [:ui/tab-layout
          [:ui/tab-list
           [:*/for [[i page] pages]
            [:ui/tab-action
             {:className  (+ "tab" (:? (== i current)
                                       " tab-active" ""))
              :onClick    (fn []
                            (setCurrent i))}
             page.title]]]
          [:ui/tab-content
           (:? (k/is-function? CurrentPage.content)
               [:% CurrentPage.content]
               CurrentPage.content)]]
     {:ui/tab-layout    [:div]
      :ui/tab-list      [:div
                         {:role      "tablist"
                          :className ["tabs tabs-border"]}]
      :ui/tab-action    [:div {:role      "tab"}]
      :ui/tab-content   [:div]})))

(def.js languageToMode
  {:javascript "javascript"
   :html       "htmlmixed"
   :clojure    "clojure"
   :python     "python"
   :plpgsql    "text/x-pgsql"})

(defn.js CodeEditor
  [#{[value
      onChange
      (:= language "javascript")
      (:.. props)]}]
  (var textareaRef (r/useRef nil))
  (var editorRef (r/useRef nil)) 

  (r/useEffect
    (fn []
      (if (or (== (typeof window.CodeMirror) "undefined")
              (k/nil? (. textareaRef current)))
        (return nil))

      (var currentMode (. -/languageToMode [language]))

      (var editor (window.CodeMirror.fromTextArea (. textareaRef current)
                                                    {:lineNumbers true
                                                     :mode currentMode}))

      (:= (. editorRef current) editor)
      (. editor (setValue (or value "")))

      (. editor (on "change"
                    (fn [instance]
                      (return (onChange (. instance (getValue)))))))

      (return
        (fn []
          (if (. editorRef current)
            (return (. (. editorRef current) (toTextArea)))))))
    [])

  
  (r/useEffect
    (fn []
      (if (and (. editorRef current)
               (!== value (. (. editorRef current) (getValue))))
        (return (. (. editorRef current) (setValue (or value ""))))))
    [value])

  
  (r/useEffect
    (fn []
      (if (. editorRef current)
        (var newMode (. -/languageToMode [language]))
        (return (. (. editorRef current) (setOption "mode" newMode)))))
    [language]) ;; Re-run whenever the 'language' prop changes

  (return
   [:textarea
    #{[:ref textareaRef
       (:.. props)]}]))

(defn.js Icon
  [#{[name color size strokeWidth className (:.. rest)]}]
  (var iconRef (r/useRef nil))

  (r/useEffect
   (fn []
     (if (k/nil? (. iconRef current))
       (return (fn [])))

     ;; 1. Check if the Lucide global object and the icon exist
     (if (or (== (typeof window.lucide) "undefined")
             (k/nil? (k/get-in window.lucide ["icons" name])))
       (do
         (console.warn (+ "Icon \"" name "\" not found in Lucide library."))
         ;; Clear any previous icon
         (:= iconRef.current.innerHTML "")
         (return (fn []))))

     ;; 2. Get the abstract icon data
     (var iconNode (k/get-in window.lucide ["icons" name]))

     ;; 3. Create the SVG DOM element
     (var svgEl (window.lucide.createElement iconNode))

     ;; 4. Apply props as attributes
     (if color
       (. svgEl (setAttribute "stroke" color)))
     (if size
       (do
         (. svgEl (setAttribute "width" size))
         (. svgEl (setAttribute "height" size))))
     (if strokeWidth
       (. svgEl (setAttribute "stroke-width" strokeWidth)))
     (if className
       ;; We use classList to add classes without removing defaults
       (k/for:array [cls (. className (split " "))]
         (if (k/not-empty? cls)
           (svgEl.classList.add cls))))
   
     ;; 5. Clean the container and append the new SVG
     (:= iconRef.current.innerHTML "") ;; Remove previous icon
     (iconRef.current.appendChild svgEl)
     (return (fn [])))
   [name color size strokeWidth className]) ;; Re-run if any prop changes

  ;; This span acts as the container for the SVG
  (return
   [:span #{[:ref iconRef
             (:.. rest)]}]))

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










(def.js MODULE (!:module))


(def +snippets+
  '{:ui/live-preview  [:div {:style {:flex 1}}
                       [:h3 "Live Preview"]
                       [:iframe {:srcDoc input
                                 :title "HTML Preview"
                                 :sandbox "allow-scripts"
                                 :style {:width "100%"
                                         :height "400px"
                                         :border "1px solid #ccc"
                                         :borderRadius "4px"
                                         :backgroundColor "white"}}]]
    :ui/text-area [:fieldset {:class ["fieldset"]}
                   [:textarea
                    {:class   ["textarea" "font-mono"
                               "text-[10px]"
                               "textarea-accent"
                               "h-100" "w-full"]
                     :placeholder "Enter HTML Input"
                     :value input
                     :onChange (fn [e]
                                 (setInput e.target.value))}]
                   [:p {:class "label"}
                    "This is the input html"]]})

(comment

  

  )
