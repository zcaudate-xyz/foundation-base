(ns code.dev.client.page-tasks
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r :include [:dom]]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [code.dev.client.tasks.task-main :as task-main]
             [code.dev.client.ui-common :as ui]]})

(defn.js AppTasks
  []
  (r/init []
    (client/client-ws "localhost" 1312 {}))
  (return
   (r/ui [:app/top
          [:app/body]]
     {:app/top     [:div
                    {:class ["flex flex-col w-full"]
                     :style {:top 0 :bottom 0}}]
      
      :app/body    [:% task-main/TaskMain]})))

(defn.js main
  []
  (r/renderDOMRoot "root" -/AppTasks))
