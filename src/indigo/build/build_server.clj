(ns indigo.build.build-server
  (:require [indigo.client.page-index :as index]
            [indigo.server.pages :as pages]
            [std.lang :as l]
            [std.make :as make :refer [def.make]]))

(def.make PROJECT
  {:tag       "indigo.server"
   :build     ".build/code-dev-server"
   :triggers #{"indigo"}
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
  nil)

(comment

  (make/build PROJECT)
  )
