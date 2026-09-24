(ns lang-demos.tui-000-counter.build
  (:use [code.test :exclude [-main]])
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [js.blessed.project :as blessed-project]
            [lang-demos.tui-000-counter.main :as main]))

(def +makefile+
  (:main webpack/+node-makefile+))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-000-counter"
          {"main" "dist/main.js"})))

(def.make TUI-000-COUNTER
  {:build    ".build/demo/tui-000-counter"
   :github   {:repo "zcaudate/lang-demos.tui-000-counter"
              :description "Simple Blessed TUI Example"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.tui-000-counter.main}
   :sections {:setup  [{:type :gitignore
                        :main ["dist" "node_modules" "out"]}
                       {:type :makefile
                        :main +makefile+}
                       {:type :package.json
                        :main +package+}
                       webpack/+node-basic+]}
   :default  [{:type :module.single
               :lang :js
               :main 'lang-demos.tui-000-counter.main
               :file "index.js"
               :target "src"}]})

(def +init+
  nil)

(defn -main
  []
  (make/build-all TUI-000-COUNTER)
  (make/gh:dwim-init TUI-000-COUNTER))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/build-all TUI-000-COUNTER))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:init TUI-000-COUNTER))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:dev TUI-000-COUNTER))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init TUI-000-COUNTER))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push TUI-000-COUNTER))
