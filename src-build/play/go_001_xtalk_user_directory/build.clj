(ns play.go-001-xtalk-user-directory.build
  (:use [code.test :exclude [-main]])
  (:require [std.lang.model.spec-go.typed :as go-typed]
            [std.lang.model.spec-xtalk.mixer :as mixer]
            [std.make :as make :refer [def.make]]
            [play.go-001-xtalk-user-directory.main :as main]))

(def +gitignore+
  ["bin"])

(def +makefile+
  [[:.PHONY {:- ["build" "show-source"]}]
   [:build
    ["go build ./..."]]
   [:show-source
    ["@sed -n '1,160p' user_directory.go"]]])

(def +go-mod+
  ["module example.com/go-001-xtalk-user-directory"
   ""
   "go 1.22"])

(def +expected-files+
  [".gitignore"
   "Makefile"
   "go.mod"
   "user_directory.go"])

(def +main-file+
  "src-build/play/go_001_xtalk_user_directory/main.clj")

(defn go-source
  [_]
  (-> +main-file+
      mixer/mix-file
      go-typed/emit-analysis-declarations))

(def.make PROJECT
  {:github   {:repo "zcaudate/play.go-001-xtalk-user-directory"
              :description "Go declarations generated from canonical xtalk source"}
   :orgfile  "Main.org"
   :triggers '#{play.go-001-xtalk-user-directory.main}
   :sections {:setup [{:type :gitignore
                       :main +gitignore+}
                      {:type :makefile
                       :main +makefile+}
                      {:type :raw
                       :file "go.mod"
                       :main +go-mod+}]}
   :default [{:type :custom
              :file "user_directory.go"
              :header "package userdirectory"
              :fn #'go-source}]})

(defn -main
  []
  (make/build-all PROJECT))

^{:eval false}
(fact "build the example into .build/"
  (make/build-all PROJECT))

^{:eval false}
(fact "show generated go source"
  (make/run PROJECT :show-source))

^{:eval false}
(fact "build the generated go module"
  (make/run PROJECT :build))
