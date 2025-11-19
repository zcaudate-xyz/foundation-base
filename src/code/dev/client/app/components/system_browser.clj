(ns code.dev.client.app.components.system-browser
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.lib.figma :as fg]]})

(var categories ["Kernel-Objects" "Collections-Sequenceable" "Collections-Unordered" "Graphics-Primitives" "System-Support"])
(var classes
  {:Kernel-Objects ["Object" "Behavior" "Class" "Metaclass" "Boolean" "True" "False" "UndefinedObject"]
   :Collections-Sequenceable ["Array" "OrderedCollection" "SortedCollection" "String" "Symbol"]
   :Collections-Unordered ["Set" "Dictionary" "IdentityDictionary" "Bag"]
   :Graphics-Primitives ["Point" "Rectangle" "Color" "Form"]
   :System-Support ["System" "Transcript" "Compiler" "Debugger"]})

(var methods
  {:Object ["initialize" "printOn:" "isNil" "notNil" "yourself" "copy" "deepCopy" "hash" "class"]
   :Array ["at:" "at:put:" "size" "do:" "collect:" "select:" "reject:" "first" "last"]
   :String ["size" "at:" "concat:" "isEmpty" "asUppercase" "asLowercase" "findString:"]
   :Point ["x" "y" "x:y:" "dist:" "transpose" "dotProduct:"]})

(var methodSource
  {:Object-initialize "initialize\n    \"Initialize the receiver\"\n    ^ self"
   :Object-printOn: "printOn: aStream\n    \"Print the receiver on a stream\"\n    aStream nextPutAll: self class name"
   :Array-size "size\n    \"Answer the number of elements in the receiver\"\n    ^ self basicSize"
   :Array-at: "at: index\n    \"Answer the given index\"\n    ^ self basicAt: index"})

(defn.js SystemBrowser [{:# [onExecute onMessage]}]
  (var [selectedCategory setSelectedCategory] (r/useState (. categories [0])))
  (var [selectedClass setSelectedClass] (r/useState "Object"))
  (var [selectedMethod setSelectedMethod] (r/useState "initialize"))
  (var [methodCode setMethodCode] (r/useState (. methodSource ["Object-initialize"])))

  (var handleCategorySelect (fn [category]
                              (setSelectedCategory category)
                              (var firstClass (. (. classes [category]) [0]))
                              (setSelectedClass firstClass)
                              (updateMethodView firstClass)))

  (var handleClassSelect (fn [className]
                           (setSelectedClass className)
                           (updateMethodView className)))

  (var updateMethodView (fn [className]
                          (var classMethods (or (. methods [className]) ["(no methods)"])))
                          (setSelectedMethod (. classMethods [0]))
                          (setMethodCode (or (. methodSource [(+ className "-" (. classMethods [0]))])
                                             (+ (. classMethods [0]) "\n    \"Method source not available\""))))

  (var handleMethodSelect (fn [method]
                            (setSelectedMethod method)
                            (var key (+ selectedClass "-" method))
                            (setMethodCode (or (. methodSource [key])
                                               (+ method "\n    \"Method source not available\"")))))

  (return
    [:div {:className "flex flex-col h-full bg-white"}
      [:div {:className "px-4 py-2 bg-gray-200 border-b"}
        [:h2 {:className "text-sm"} "System Browser"]]

      [:div {:className "flex flex-1 border-b overflow-hidden"}
        [:div {:className "w-1/4 border-r flex flex-col"}
          [:div {:className "px-2 py-1 bg-gray-100 border-b text-xs"} "Categories"]
          [:% fg/ScrollArea {:className "flex-1"}
            (. categories (map (fn [category]
                                  (return
                                    [:div {:key category
                                           :className (+ "px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 "
                                                         (:? (== selectedCategory category) "bg-blue-100" ""))
                                           :onClick (fn [] (return (handleCategorySelect category)))}
                                      category]))))]]

        [:div {:className "w-1/4 border-r flex flex-col"}
          [:div {:className "px-2 py-1 bg-gray-100 border-b text-xs"} "Classes"]
          [:% fg/ScrollArea {:className "flex-1"}
            (. (or (. classes [selectedCategory]) []) (map (fn [className]
                                                               (return
                                                                 [:div {:key className
                                                                        :className (+ "px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 "
                                                                                      (:? (== selectedClass className) "bg-blue-100" ""))
                                                                        :onClick (fn [] (return (handleClassSelect className)))}
                                                                   className]))))]]

        [:div {:className "w-1/4 border-r flex flex-col"}
          [:div {:className "px-2 py-1 bg-gray-100 border-b text-xs"} "Protocols"]
          [:% fg/ScrollArea {:className "flex-1"}
            (. ["accessing" "testing" "printing" "copying"] (map (fn [protocol]
                                                                    (return
                                                                      [:div {:key protocol
                                                                             :className "px-2 py-1 text-sm cursor-pointer hover:bg-gray-100"}
                                                                        protocol]))))]]

        [:div {:className "w-1/4 flex flex-col"}
          [:div {:className "px-2 py-1 bg-gray-100 border-b text-xs"} "Methods"]
          [:% fg/ScrollArea {:className "flex-1"}
            (. (or (. methods [selectedClass]) []) (map (fn [method]
                                                           (return
                                                             [:div {:key method
                                                                    :className (+ "px-2 py-1 text-sm cursor-pointer hover:bg-gray-100 "
                                                                                  (:? (== selectedMethod method) "bg-blue-100" ""))
                                                                    :onClick (fn [] (return (handleMethodSelect method)))}
                                                               method]))))]]]

      [:div {:className "flex-1 flex flex-col"}
        [:div {:className "px-2 py-1 bg-gray-100 border-b text-xs"}
          (+ selectedClass " >> " selectedMethod)]
        [:textarea
          {:value methodCode
           :onChange (fn [e] (return (setMethodCode e.target.value)))
           :className "flex-1 px-3 py-2 font-mono text-sm resize-none focus:outline-none"}]
        [:div {:className "flex gap-2 px-3 py-2 bg-gray-50 border-t"}
          [:% fg/Button {:size "sm" :variant "outline" :onClick (fn [] (return (onMessage "Method accepted")))}
            "Accept"]
          [:% fg/Button {:size "sm" :variant "outline" :onClick (fn [] (return (onMessage "Method cancelled")))}
            "Cancel"]]]]))