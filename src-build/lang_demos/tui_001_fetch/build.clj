(ns lang-demos.tui-001-fetch.build
  (:use [code.test :exclude [-main]])
  (:require [lang.core :as  l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [js.blessed.project :as blessed-project]
            [lang-demos.tui-001-fetch.main :as main]))

(def +makefile+
  (:main webpack/+node-makefile+))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-001-fetch"
          {"main" "dist/main.js"
           "dependencies" {"node-fetch" "2.6.1"}})))

(def.make TUI-001-FETCH
  {:build    ".build/demo/tui-001-fetch"
   :github   {:repo "zcaudate/lang-demos.tui-001-fetch"
              :description "Simple Blessed TUI Fetch Example"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.tui-001-fetch.main}
   :sections {:setup  [{:type :gitignore
                        :main ["dist" "node_modules" "out"]}
                       {:type :makefile
                        :main +makefile+}
                       {:type :package.json
                        :main +package+}
                       webpack/+node-basic+]}
   :default  [{:type :module.single
               :lang :js
               :main 'lang-demos.tui-001-fetch.main
               :file "index.js"
               :target "src"}]})

(def +init+
  nil)

(defn -main
  []
  (make/build-all TUI-001-FETCH)
  (make/gh:dwim-init TUI-001-FETCH))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/build-all TUI-001-FETCH))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:init TUI-001-FETCH))

^{:eval false
  ;;
  ;; RUN DEV
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:dev TUI-001-FETCH))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init TUI-001-FETCH))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push TUI-001-FETCH))
