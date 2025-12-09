(ns indigo.client.app.components.code-viewer
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]]
   :import  [["sonner@2.0.3" :as toast]]})

(defn.js generateLayout
  [component (:= indent 0)]
  (var indentation (. (new Array (+ indent 1)) (join "  ")))
  (var code "")

  (var tagMap {:Container "div"
               :FlexRow "div"
               :FlexCol "div"
               :Card "div"
               :Heading "h2"
               :Text "p"
               :Button "button"
               :Input "input"
               :Checkbox "input"
               :Switch "input"})

  (var tag (or (. tagMap [component.type]) "div"))

    ;; Generate opening tag

  (:= code (+ code indentation ":" tag))

    ;; Add properties if they exist

  (var props component.properties)
  (var propKeys (Object.keys props))

  (when (> propKeys.length 0)
    (:= code (+ code " {"))
    (. propKeys (forEach (fn [key idx]
                           (var value (. props [key]))
                           (when (and (== key "children") (== (typeof value) "string"))
                             (return))
                           (:= code (+ code ":" key " "))
                           (if (== (typeof value) "string")
                             (:= code (+ code "\"" value "\""))
                             (:= code (+ code value)))
                           (when (< idx (- propKeys.length 1))
                             (:= code (+ code "\n" indentation "      "))))))
    (:= code (+ code "}")))

    ;; Add text content if it exists

  (when (and props.children (== (typeof props.children) "string"))
    (:= code (+ code "\n" indentation "   \"" props.children "\"")))

    ;; Add child components

  (when (> component.children.length 0)
    (. component.children
       (forEach (fn [child]
                  (:= code (+ code "\n" (-/generateLayout child (+ indent 1))))))))
  
  (:= code (+ code "]"))
  (return code))

(defn.js generateStdLangCode
  [components]
  (var code "")

  ;; Header comment

  (:= code (+ code ";; Generated std.lang UI Component\n"))
  (:= code (+ code ";; Styled with Tailwind CSS\n\n"))

  ;; Namespace declaration

  (:= code (+ code "(ns my-component\n"))
  (:= code (+ code "  (:require [std.lang :as l]\n"))
  (:= code (+ code "            [std.lib :as h]))\n\n"))

  ;; Script block

  (:= code (+ code "(l/script :js\n"))
  (:= code (+ code "  {:runtime :websocket\n"))
  (:= code (+ code "   :require [[js.react :as r]\n"))
  (:= code (+ code "             [xt.lang.base-lib :as k]]\n"))
  (:= code (+ code "   }))\n\n"))

  ;; Component definition

  (:= code (+ code "(defn.js MyComponent\n"))
  (:= code (+ code "  [props]\n"))
  (:= code (+ code "  (return\n"))
  (:= code (+ code "    (r/return-ui\n"))
  (:= code (+ code "      {;; Layout Section - Tailwind CSS Styled Components\n"))
  (:= code (+ code "       :layout\n"))

  ;; Generate layout from components

  (when (> components.length 0)
    (var layoutCode
         (. components
            (map (fn [comp]
                   (return (-/generateLayout comp 4))))
            (join "\n\n")))
    (:= code (+ code layoutCode "\n\n")))
  
  (:= code (+ code "       ;; States Section\n"))
  (:= code (+ code "       :states {}\\n\n"))

  (:= code (+ code "       ;; Triggers Section\n"))
  (:= code (+ code "       :triggers {}\\n\n"))

  (:= code (+ code "       ;; Actions Section\n"))
  (:= code (+ code "       :actions {}\\n\n"))

  (:= code (+ code "       ;; Components Section\n"))
  (:= code (+ code "       ;; Define reusable component mappings here\n"))
  (:= code (+ code "       :components {\n"))
  (:= code (+ code "         ;; Example:\n"))
  (:= code (+ code "         ;; :ui/button {:tag :button\n"))
  (:= code (+ code "         ;;             :props {:class [\"btn\"]}}
"))
  (:= code (+ code "       }}))\n\n"))

  ;; Module export
  (:= code (+ code "\n"))

  (return code))

(defn.js copyToClipboard
  [text]
  (return (. navigator.clipboard (writeText fullCode))))

(defn.js CodeViewer
  [{:# [(:= components [])]}]
  
  (var fullCode (-/generateStdLangCode components))
  (return
   [:div {:className "flex flex-col h-full bg-white"}
    [:div {:className "px-4 py-2 bg-gray-100 border-b flex items-center justify-between"}
     [:h3 {:className "text-sm"} "Generated std.lang Code"]
     [:% fg/Button
      {:size "sm"
       :variant "ghost"
       :onClick -/copyToClipboard}
      [:% lc/Copy {:className "w-3 h-3 mr-1"}]
      "Copy"]] 

    [:% fg/ScrollArea {:className "flex-1"}
     [:pre {:className "p-4 font-mono text-xs"}
      [:code fullCode]]]]))
