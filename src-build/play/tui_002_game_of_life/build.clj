(ns play.tui-002-game-of-life.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [play.tui-002-game-of-life.main :as main]))

(def.make PROJECT_COMMONJS
  {:github   {:repo "zcaudate/play.tui-002-game-of-life"
              :description "Simple Blessed TUI Game of Life Example"}
   :orgfile  "Main.org"
   :triggers '#{play.tui-002-game-of-life.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :js
               :main 'play.tui-002-game-of-life.main
               :file "index.js"
               :target "src"}]})

(def.make PROJECT_ESM
  {:github   {:repo "zcaudate/play.tui-002-game-of-life"
              :description "Simple Blessed TUI Game of Life Example"}
   :orgfile  "Main.org"
   :triggers '#{play.tui-002-game-of-life.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :js
               :main 'play.tui-002-game-of-life.main
               :file "index.mjs"
               :target "src"
               :emit {:code {:transforms {:full [inject-esm]}}}}]})

(def +init+
  nil)

(defn -main
  []
  (std.make/build-all play.tui-002-game-of-life.build/PROJECT_ESM)
  (make/gh:dwim-init PROJECT_ESM))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT_ESM SETUP" 

  (make/build-all PROJECT_ESM))

^{:eval false
  ;;
  ;; BUILD SETUP
  ;;
  }
(fact "Code FOR PROJECT_ESM SETUP" 

  (make/run:init PROJECT_ESM))

^{:eval false
  ;;
  ;; RUN DEV
  ;;
  }
(fact "Code FOR PROJECT_ESM SETUP" 

  (make/run:dev PROJECT_ESM))

^{:eval false
  ;;
  ;; GH INITAL SETUP
  ;;
  :ui/action [:GITHUB :SETUP]}
(fact "initial setup of repo from github"

  (make/gh:dwim-init PROJECT_ESM))

^{:eval false
  ;;
  ;; GH PUSH NEWEST
  ;;
  :ui/action [:GITHUB :PUSH]}
(fact "pushes changes to github"

  (make/gh:dwim-push PROJECT_ESM))

