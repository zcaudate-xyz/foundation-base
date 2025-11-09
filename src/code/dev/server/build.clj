(ns code.dev.server.build
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [code.dev.server.page-index :as index]))

(def.make PROJECT
  {:tag       "code.dev.server"
   :build     ".build/code-dev-server"
   :hooks    {:post [{:id :inject-ui
                      :fn (fn [& _]
                            (binding [*ns* (the-ns 'code.dev.server.page-index)]
                              (code.dev.server.page-index/main)))}]}
   :default  [{:type   :raw
               :file   "main.js"
               :main   (fn []
                         (l/emit-script '(-/main)
                                           {:lang :js
                                            :library (l/default-library)
                                            :module  (l/get-module (l/default-library)
                                                                   :js
                                                                   'code.dev.server.page-index)
                                            :emit {:native {:suppress true}}
                                            :layout :flat}))}]})


(def +init+
  (make/triggers-set
   PROJECT
   #{"code.dev"}))

(comment

  (make/build PROJECT)
  )
