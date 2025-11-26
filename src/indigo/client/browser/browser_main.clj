(ns indigo.client.browser.browser-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [js.lib.puck :as puck]
             [js.lib.radix :as rx]
             [indigo.client.ui-global :as global]
             [indigo.client.ui-common :as ui]]})

(defn.js BrowserMain
  []
  (return
   [:% fg/Tabs {:defaultValue "namespaces"}
    [:% fg/TabsList
     [:% fg/TabsTrigger {:value "overview"} "Overview"]
     [:% fg/TabsTrigger {:value "namespaces"} "Namespaces"]
     [:% fg/TabsTrigger {:value "events"} "Events"]
     [:% fg/TabsTrigger {:value "resources"} "Resources"]
     [:% fg/TabsTrigger {:value "network"} "Network"]]
    [:% fg/TabsContent {:value "overview"}
     "Overview Content"]
    [:% fg/TabsContent {:value "namespaces"}
     [:% fg/ResizablePanelGroup {:direction "horizontal"}
      [:% fg/ResizablePanel {:defaultSize 20}
       [:% fg/ScrollArea {:className "h-full w-full"}
        [:% fg/Card
         [:% fg/CardHeader
          [:% fg/CardTitle "Namespaces"]]
         [:% fg/CardContent
          [:% fg/Button {:variant "ghost" :className "w-full justify-start"} "app.core"]
          [:% fg/Button {:variant "ghost" :className "w-full justify-start"} "app.handlers"]
          [:% fg/Button {:variant "ghost" :className "w-full justify-start"} "indigo.system"]]]]]
      [:% fg/ResizableHandle]
      [:% fg/ResizablePanel {:defaultSize 20}
       [:% fg/ScrollArea {:className "h-full w-full"}
        [:% fg/Card
         [:% fg/CardHeader
          [:% fg/CardTitle "Vars"]]
         [:% fg/CardContent
          [:% fg/Button {:variant "ghost" :className "w-full justify-start"} "process-data"]
          [:% fg/Button {:variant "ghost" :className "w-full justify-start"} "handle-request"]]]]]
      [:% fg/ResizableHandle]
      [:% fg/ResizablePanel {:defaultSize 60}
       [:% fg/ScrollArea {:className "h-full w-full"}
        [:% fg/Card
         [:% fg/CardHeader
          [:% fg/CardTitle "Var Details"]]
         [:% fg/CardContent
          [:div
           [:h2 {:class "text-xl font-bold"} "process-data"]
           [:p {:class "text-sm text-gray-500 mb-4"} "in app.core"]
           [:h3 {:class "font-bold mt-4"} "Docstring"]
           [:pre {:class "bg-gray-100 p-2 rounded whitespace-pre-wrap"}
            "Processes the given data."]
           [:h3 {:class "font-bold mt-4"} "Source"]
           [:pre {:class "bg-gray-100 p-2 rounded"}
            [:code
             "(defn process-data [data]\n  (println \"Processing data:\n\" data))"]]]]]]]]]
    [:% fg/TabsContent {:value "events"}
     [:% fg/Card
      [:% fg/CardHeader
       [:% fg/CardTitle "Live Event Stream"]]
      [:% fg/CardContent
       [:div {:class "space-y-2"}
        [:pre {:class "bg-green-100 p-2 rounded font-mono text-sm"}
         "{:type \"eval\", :ns \"app.core\", :var \"process-data\", :args \"1\", :result \"nil\"}"]
        [:pre {:class "bg-red-100 p-2 rounded font-mono text-sm"}
         "{:type \"eval-error\", :ns \"app.core\", :var \"process-data\", :args \"a\", :error \"java.lang.NumberFormatException: For input string: \\\"a\\\"\"}"]]]]]
    [:% fg/TabsContent {:value "resources"}
     [:div {:class "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"}
      [:% fg/Card
       [:% fg/CardHeader
        [:% fg/CardTitle "clj-eval"]]
       [:% fg/CardContent
        [:p "Evaluates a string of Clojure code."]]]
      [:% fg/Card
       [:% fg/CardHeader
        [:% fg/CardTitle "ls"]]
       [:% fg/CardContent
        [:p "Lists files in a directory."]]]]]
    [:% fg/TabsContent {:value "network"}
     "Network Content"]]))
