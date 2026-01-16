(ns play.ngx-001-eval.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.make :as make :refer [def.make]]
            [std.string :as str]
            [play.ngx-001-eval.main :as main]
            [rt.nginx :as nginx]))

(def.make PROJECT
  {:github   {:repo "zcaudate/play.ngx-001-eval"
              :description "Simple OpenResty Eval Example"}
   :orgfile  "Main.org"
   :triggers '#{play.ngx-001-eval.main}
   :sections {:setup  [{:type :gitignore
                        :main ["bin" "out"]}
                       {:type :makefile
                        :main +makefile+}]}
   :default  [{:type :module.single
               :lang :lua
               :main 'play.ngx-001-eval.main
               :file "eval.lua"
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

  (make/run:start PROJECT))

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


(comment
  (nginx/kill-all-nginx)
  (nginx/all-nginx-ports)
  )
