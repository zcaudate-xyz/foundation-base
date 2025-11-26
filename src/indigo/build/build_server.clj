(ns indigo.build.build-server
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [indigo.server.pages :as pages]
            [indigo.client.page-index :as index]))

(def.make PROJECT
  {:tag       "indigo.server"
   :build     ".build/code-dev-server"
   :hooks    {:post [{:id :inject-ui
                      :fn (fn [& _]
                            #_(binding [*ns* (the-ns 'indigo.client.page-index)]
                              (indigo.client.page-index/main)))}]}
   :default  [{:type   :raw
               :file   "main.js"
               :main   (fn []
                         [(pages/emit-main
                           'indigo.client.page-index)])}]})


(def +init+
  (make/triggers-set
   PROJECT
   #{"indigo"}))

(comment

  (make/build PROJECT)
  )
