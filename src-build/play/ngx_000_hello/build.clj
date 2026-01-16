(ns play.ngx-000-hello.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [play.ngx-000-hello.main :as main]))

(def.make PROJECT
  {:github   {:repo "zcaudate/play.ngx-000-hello"
              :description "Simple OpenResty Example"}
   :orgfile  "Main.org"
   :triggers '#{play.ngx-000-hello.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :lua
               :main 'play.ngx-000-hello.main
               :file "hello.lua"
               :target "lua"}]})

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

      (make/run:dev PROJECT)

      (make/run-internal PROJECT :run))

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

