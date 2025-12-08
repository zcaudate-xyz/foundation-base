(ns play.tui-002-game-of-life.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang :as  l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [js.webpack :as webpack]
            [play.tui-002-game-of-life.main :as main]))

(def.make PROJECT_COMMONJS
  {:tag      "tui-002-game-of-life-commonjs"
   :build    ".build/tui-002-game-of-life-commonjs"
   :github   {:repo "zcaudate/play.tui-002-game-of-life"
              :description "Conway's Game of Life for React Blessed"}
   :orgfile  "Main.org"
   :sections {:setup  [webpack/+node-basic+
                       webpack/+node-makefile+
                       webpack/+node-gitignore+]}
   :default  [{:type   :module.graph
               :lang   :js
               :target "src"
               :main   'play.tui-002-game-of-life.main
               :emit   {:lang/format :commonjs
                        :code   {:label true}}}]})

(def.make PROJECT_ESM
  {:tag      "tui-002-game-of-life-esm"
   :build    ".build/tui-002-game-of-life-esm"
   :github   {:repo "zcaudate/play.tui-002-game-of-life"
              :description "Conway's Game of Life for React Blessed"}
   :orgfile  "Main.org"
   :sections {:setup  [webpack/+node-basic+
                       webpack/+node-makefile+
                       webpack/+node-gitignore+]}
   :default  [{:type   :module.graph
               :lang   :js
               :target "src"
               :main   'play.tui-002-game-of-life.main
               :emit   {:code   {:label true}}}]})

(def +init+
  (do (make/triggers-set PROJECT_COMMONJS '#{play.tui-002-game-of-life.main})
      (make/triggers-set PROJECT_ESM '#{play.tui-002-game-of-life.main})))

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

