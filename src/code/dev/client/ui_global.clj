(ns code.dev.client.ui-global
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react.ext-box :as box]]})

(defglobal.js Root nil)

(def.js Global
  (box/attachLocalStorage
   "code.dev"
   (box/createBox {:task.translate-html {:html-code ""
                                         :dsl-code  ""
                                         :history-idx 0}
                   :task.heal-code      {:input-code ""
                                         :healed-code  ""
                                         :history-idx 0}})))

(defn.js api-post
  [url body]
  (return
   (. (fetch url
             {:body body
              :method "POST"})
      (then (fn [res] (return (res.json))))
      (then (fn [#{data}] (return data))))))
