(ns code.dev.client.ui-common
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]]
   :export [MODULE]})

(def$.js ReactDiffViewer
  window.ReactDiffViewer)

(defn.js isReactRoot
  [container]
  (var internalKey (. (Object.keys container)
                      (find (fn [key]
                              (return (. key (startsWith "__reactContainer")))))))
  (return (or (boolean internalKey)
              (and (. container (hasOwnProperty "_reactRootContainer"))
                   (not== (. container _reactRootContainer) undefined)))))

(defn.js getReactRoot
  [domNode]
  (var internalKey
       (k/arr-find (k/obj-keys domNode)
                   (fn [key]
                     (return (. key (startsWith "__reactContainer$"))))))
  
  (if (> internalKey 0)
    (return (. domNode [internalKey])))
  (return nil))

(defn.js renderRoot
  [id Component]
  (var rootElement (document.getElementById id))
  (var root (-/getReactRoot rootElement))
  (when  (k/nil? root)
    (:= root (r/createDOMRoot rootElement)))
  (. root (render [:% Component]))
  (return true))

(defn.js TabComponent
  [#{[controls
      (:= controlKey "current")
      (:= pages [])]}]
  (var [current
        setCurrent] (:? controls
        (r/useStateFor
         controls controlKey)
        (r/useState 0)))
  (when (k/nil? current)
    (:= current 0)
    (setCurrent 0))
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
     {:ui/tab-layout    [:div
                         {:class ["flex" "flex-col" "grow"]}]
      :ui/tab-list      [:div
                         {:role      "tablist"
                          :className ["tabs tabs-border"]}]
      :ui/tab-action    [:div {:role      "tab"}]
      :ui/tab-content   [:div
                         {:class ["flex" "flex-col"  "grow" "py-4" "px-2"]}]})))

(def.js languageToMode
  {:javascript "javascript"
   :html       "htmlmixed"
   :clojure    "clojure"
   :python     "python"
   :plpgsql    "text/x-pgsql"})

(defn.js CodeEditor
  [#{[value
      onChange
      onSubmit
      (:= language "javascript")
      (:.. props)]}]
  (var textareaRef (r/useRef nil))
  (var editorRef (r/useRef nil)) 
  (var onSubmitRef   (r/useRef onSubmit))
  (r/watch [onSubmit]
    (:= onSubmitRef.current onSubmit))
  
  (r/useEffect
   (fn []
     (if (or (== (typeof window.CodeMirror) "undefined")
             (k/nil? (. textareaRef current)))
       (return nil))
     
     (var currentMode (. -/languageToMode [language]))

     (var handleSubmit (fn [cm]
                         (if onSubmitRef.current (onSubmitRef.current (. cm (getValue))))))
      
     (var editor (window.CodeMirror.fromTextArea
                  (. textareaRef current)
                  {:lineNumbers true
                   :lineWrapping false
                   :mode currentMode
                   :extraKeys
                   {"Ctrl-Enter" handleSubmit
                    "Cmd-Enter"  handleSubmit}}))

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
               (not== value (. (. editorRef current) (getValue))))
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
       (k/for:array [cls (. (or className "") (split " "))]
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


(defn.js SplitPane
  [#{left right}]
  ;; State for the left panel's width.
  ;; We use `nil` to signify "use the default CSS width (w-1/2)".
  (var [leftWidth setLeftWidth] (r/useState nil))

  ;; Ref for the main container to calculate bounds
  (var containerRef (r/useRef nil))
  
  ;; 1. --- Mouse Move Handler ---
  ;; This is wrapped in `useCallback` so it can be safely added
  ;; and removed from the document's event listeners.
  (var handleMouseMove
    (r/useCallback
     (fn [e]
       ;; Bail out if we don't have the container ref yet
       (if (k/nil? (. containerRef current))
         (return nil))

       (var containerRect (. (. containerRef current) (getBoundingClientRect)))

       ;; Calculate new width from the mouse's X position
       (var newLeftWidth (- (. e clientX) (. containerRect left)))

       ;; Optional: Add constraints for min/max width
       (var minWidth 100) ;; e.g., 100px minimum
       (var maxWidth (- (. containerRect width) 100)) ;; e.g., 100px for right panel

       (if (< newLeftWidth minWidth)
         (:= newLeftWidth minWidth))
       (if (> newLeftWidth maxWidth)
         (:= newLeftWidth maxWidth))

       ;; Set the new width in state
       (setLeftWidth newLeftWidth))
     [])) ;; `setLeftWidth` is stable, so no dependencies needed.
  
  ;; 2. --- Mouse Up Handler ---
  ;; This cleans up the global event listeners.
  (var handleMouseUp
    (r/useCallback
     (fn []
       (. document (removeEventListener "mousemove" handleMouseMove))
       (. document (removeEventListener "mouseup" handleMouseUp)))
     [handleMouseMove]))

  ;; 3. --- Mouse Down Handler ---
  ;; This is attached to the divider. It kicks off the whole process.
  (var handleMouseDown
    (fn [e]
      ;; Prevent default text selection
      (. e (preventDefault))

      ;; Add global listeners
      (. document (addEventListener "mousemove" handleMouseMove))
      (. document (addEventListener "mouseup" handleMouseUp))))

  (return
    [:div
     {:ref containerRef ;; Attach the ref to the main container
      :className "flex w-full overflow-hidden bg-gray-200"}

     ;; LEFT PANEL
     ;; - It uses the default 'w-1/2' class only if `leftWidth` is nil.
     ;; - Once dragging starts, its width is set via the `style` prop.
     [:div
      {:className (+ "bg-white overflow-y-auto w-1/2"
                     #_(:? (k/nil? leftWidth) "w-1/2" "flex-1"))
       #_#_:style (:? (k/not-nil? leftWidth)
                  {:width (+ leftWidth "px")} {})}
      left]

     ;; DIVIDER
     ;; - The `onMouseDown` handler starts the drag.
     [:div
      {:onMouseDown handleMouseDown
       :className "w-3 bg-gray-200 cursor-col-resize hover:bg-gray-200"}]

     ;; RIGHT PANEL
     ;; - 'flex-1' is the magic. It automatically fills the
     ;;   remaining space left by the other two elements.
     [:div
      {:className "flex-1 grow bg-white overflow-y-auto"}
      right]]))

(defn.js TextDiffViewer
  [{:# [(:= oldValue "")
        (:= newValue "")]}]
  (var diff (. (. window Diff) (diffLines oldValue newValue)))
  (var diff-elements [])
  (var line 1)
  (k/for:array [[index part] diff]
    (var className "") 
    (cond (. part added)
          (do (:= className "diff-added")
              (k/arr-pushl diff-elements
                           [:span {:key index :class ["bg-green-100"]}
                            (+ "L" line ": " (. part value))])
              (:= line (+ line (. part count))))

          
          (. part removed)
          (do (:= className "diff-removed")
              (k/arr-pushl diff-elements
                           [:span {:key index :class ["bg-red-100"]}
                            (+ "L" line ": " (. part value))]))

          :else 
          (:= line (+ line (. part count)))))
  
  (return
   [:pre {:className "diff-viewer-pre"}
    diff-elements]))


(def +ui-common+
  `{:ui/button.icon [:ui/button
                     {:class   ["btn" "btn-sm"]}
                     [:ui/icon
                      {:name :props/icon
                       :size  16}]]

    :ui/html       {:tag :div
                    :props {:dangerouslySetInnerHTML
                            {"__html" :props/input}}}
    
    :ui/button     {:tag :a
                    :props {:class   ["btn" "btn-sm"]}
                    :view {:type :action
                           :key :onClick}}
    
    :ui/icon       {:tag -/Icon
                    :children []}

    :ui/split-pane {:tag -/SplitPane
                    :children []}

    :ui/diffviewer   {:tag -/ReactDiffViewer
                      :children []}
    :ui/diffview   {:tag -/TextDiffViewer
                    :children []}
    :ui/editor {:tag -/CodeEditor
                :view {:type :input
                       :key :onSubmit
                       :get {:key :value}
                       :set {:key :onChange}}}})








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
