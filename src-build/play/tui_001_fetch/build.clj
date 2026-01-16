(ns play.tui-001-fetch.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [play.tui-001-fetch.main :as main]))

(def.make PROJECT
  {:github   {:repo "zcaudate/play.tui-001-fetch"
              :description "Simple Blessed TUI Fetch Example"}
   :orgfile  "Main.org"
   :triggers '#{play.tui-001-fetch.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :js
               :main 'play.tui-001-fetch.main
               :file "index.js"
               :target "src"}]})

(def +init+
  nil)

(defn -main
  []
  (make/build-all PROJECT)
  (make/gh:dwim-init PROJECT))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/build-all PROJECT))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:init PROJECT))

^{:eval false
  ;;
  ;; RUN DEV
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:dev PROJECT))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init PROJECT))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push PROJECT))

