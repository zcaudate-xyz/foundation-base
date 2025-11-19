(ns code.dev.client.app.components.workspace
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]]})

;; Interface definitions are removed as per spec
;; interface WorkspaceProps { ... }

(defn.js Workspace
  [{:# [onExecute]}]
  (var [code setCode] (r/useState "\"Try some Smalltalk expressions:\"\n3 + 4.\n'Hello, World!' size.\n#(1 2 3 4 5) collect: [:x | x * 2]."))
  (var [selection setSelection] (r/useState ""))
  (var textareaRef (r/useRef nil)) ;; Corrected to useRef

  (var getSelectedText
    (fn []
      (var textarea textareaRef.current)
      (when textarea
        (var start textarea.selectionStart)
        (var end textarea.selectionEnd)
        (return (or (. code (substring start end)) code)))
      (return code)))

  (var handleDoIt
    (fn []
      (var selected (getSelectedText))
      (onExecute selected "do")))

  (var handlePrintIt
    (fn []
      (var selected (getSelectedText))
      (onExecute selected "print")))

  (var handleInspectIt
    (fn []
      (var selected (getSelectedText))
      (onExecute selected "inspect")))

  (return
   [:div {:className "flex flex-col h-full bg-white border-r"}
    ;; Title
    [:div {:className "px-4 py-2 bg-gray-200 border-b"}
     [:h2 {:className "text-sm"} "Workspace"]] 

    ;; Code Area
    [:textarea
     {:data-workspace true
      :ref textareaRef ;; Assign ref here
      :value code
      :onChange (fn [e] (return (setCode e.target.value)))
      :className "flex-1 px-3 py-2 font-mono text-sm resize-none focus:outline-none" 
      :placeholder "Enter Smalltalk code here..."}]

    ;; Action Buttons
    [:div {:className "flex gap-2 px-3 py-2 bg-gray-50 border-t"}
     [:% fg/Button {:size "sm" :variant "outline" :onClick handleDoIt}
      "Do it (Ctrl+D)"]
     [:% fg/Button {:size "sm" :variant "outline" :onClick handlePrintIt}
      "Print it (Ctrl+P)"]
     [:% fg/Button {:size "sm" :variant "outline" :onClick handleInspectIt}
      "Inspect it (Ctrl+I)"]]]))
