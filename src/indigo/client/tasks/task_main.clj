(ns indigo.client.tasks.task-main
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.react :as r]
             [js.react.ext-box :as box]
             [indigo.client.ui-global :as global]
             [indigo.client.ui-common :as ui]
             [indigo.client.tasks.task-heal-code :as task-heal-code]
             [indigo.client.tasks.task-translate-js :as task-translate-js]
             [indigo.client.tasks.task-translate-html :as task-translate-html]
             [indigo.client.tasks.task-translate-jsxc :as task-translate-jsxc]
             [indigo.client.tasks.task-translate-plpgsql :as task-translate-plpgsql]
             [indigo.client.tasks.task-browser :as task-browser]]})

(defn.js TaskMain
  [#{context}]
  (var #{api} (:? context
                  (r/useContext context)
                  {}))
  (var [tabIndex setTabIndex] (box/useBox global/Global ["tabIndex"]))
  (var controls #{tabIndex setTabIndex})
  (return
   [:div
    {:class ["flex" "grow" "p-1"]}
    [:% ui/TabComponent
     {:controls controls
      :controlKey "tabIndex"
      :pages [{:title "HTML"
               :content task-translate-html/TaskTranslateHtml}
              {:title "Heal"
               :content task-heal-code/TaskHealCode}
              {:title "Js"
               :content task-translate-js/TaskTranslateJs}
              {:title "Ui"
               :content task-translate-jsxc/TaskTranslateJsxComponent}
              {:title "Plpgsql"
               :content task-translate-plpgsql/TaskTranslatePlpgsql}
              {:title "Browser"
               :content task-browser/TaskBrowser}]}]]))
