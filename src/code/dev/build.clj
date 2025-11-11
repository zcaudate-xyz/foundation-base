(ns code.dev.build
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [code.dev.server.pages :as pages]
            [code.dev.client.page-index :as index]))

(def.make PROJECT
  {:tag       "code.dev.server"
   :build     ".build/code-dev-server"
   :hooks    {:post [{:id :inject-ui
                      :fn (fn [& _]
                            (binding [*ns* (the-ns 'code.dev.client.page-index)]
                              (code.dev.client.page-index/main)))}]}
   :default  [{:type   :raw
               :file   "main.js"
               :main   (fn []
                         [(pages/emit-main
                           'code.dev.client.page-index)])}]})


(def +init+
  (make/triggers-set
   PROJECT
   #{"code.dev"}))

(comment

  (make/build PROJECT)
  )
