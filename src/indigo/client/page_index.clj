(ns indigo.client.page-index
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:port 1312}
   :require [[js.react :as r]
             [xt.lang.base-lib :as k]
             [xt.lang.base-client :as client]
             [indigo.client.browser.browser-main :as browser-main]
             [indigo.client.ui-common :as ui]]})

(defn.js AppIndex
  []
  (r/init []
    (client/client-ws "localhost" 1312 {}))
  (return
   (r/ui [:app/top
          [:app/body]]
     {:app/top     [:div
                    {:class ["flex flex-col w-full" "bg-green-200"]
                     :style {:top 0 :bottom 0}}]
      
      :app/body    [:% browser-main/BrowserMain]})))

(defn.js main
  "TODO"
  {:added "4.0"}
  []
  (r/renderDOMRoot "root" -/AppIndex))



(comment
  (!.js
    (+ 1 2 3)))
