(ns lang-demos.tui-002-game-of-life.build
  (:use [code.test :exclude [-main]])
  (:require [lang.core :as  l]
            [js.blessed.project :as blessed-project]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [lang-demos.tui-002-game-of-life.main :as main]))

(def +makefile+
  (:main webpack/+node-makefile+))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-002-game-of-life"
          {"main" "dist/main.js"
           "imports" {"#app/*" "./src/*"}})))

(def.make TUI-002-GAME-OF-LIFE
  {:tag     "demo-tui-002-game-of-life"
   :build    ".build/demo/tui-002-game-of-life"
   :github   {:repo "zcaudate/lang-demos.tui-002-game-of-life"
              :description "Simple Blessed TUI Game of Life Example"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.tui-002-game-of-life.main}
   :sections {:setup  [{:type :gitignore
                        :main ["dist" "node_modules" "out"]}
                       {:type :makefile
                        :main +makefile+}
                       {:type :package.json
                        :main +package+}
                       webpack/+node-basic+]}
    :default  [{:type :module.directory
                :lang :js
                :main 'lang-demos.tui-002-game-of-life.main
                :search ["src-build/lang_demos/tui_002_game_of_life"]
                :target "src"
                :emit {:code {:link {:path-suffix ".js"
                                     :root-prefix "#app"}
                              :refine {'js.react {:treeshake true
                                                  :ensure '[useInterval runIntervalStart]}}}}}]})

(defn -main
  []
  (std.make/build-all TUI-002-GAME-OF-LIFE)
  (make/gh:dwim-init TUI-002-GAME-OF-LIFE))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/build-all TUI-002-GAME-OF-LIFE))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:init TUI-002-GAME-OF-LIFE))

^{:eval false
  ;;
  ;; RUN DEV
  ;;
  }
(fact "Code FOR PROJECT SETUP" 

  (make/run:dev TUI-002-GAME-OF-LIFE))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init TUI-002-GAME-OF-LIFE))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push TUI-002-GAME-OF-LIFE))


