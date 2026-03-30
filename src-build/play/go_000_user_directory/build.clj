(ns play.go-000-user-directory.build
  (:use [code.test :exclude [-main]])
  (:require [std.make :as make :refer [def.make]]
            [play.go-000-user-directory.main :as main]))

(def +gitignore+
  ["bin"])

(def +makefile+
  [[:.PHONY {:- ["build" "test" "show-source"]}]
   [:build
    ["go build ./..."]]
   [:test
    ["go test ./..."]]
   [:show-source
    ["@sed -n '1,160p' user_directory.go"]]])

(def +go-mod+
  ["module example.com/go-000-user-directory"
   ""
   "go 1.22"])

(def +expected-files+
  [".gitignore"
   "Makefile"
   "go.mod"
   "user_directory.go"])

(def.make PROJECT
  {:github   {:repo "zcaudate/play.go-000-user-directory"
              :description "Simple Go project generated from Clojure"}
   :orgfile  "Main.org"
   :triggers '#{play.go-000-user-directory.main}
   :sections {:setup [{:type :gitignore
                       :main +gitignore+}
                      {:type :makefile
                       :main +makefile+}
                      {:type :raw
                       :file "go.mod"
                       :main +go-mod+}]}
   :default [{:type :module.single
              :lang :go
              :main 'play.go-000-user-directory.main
              :file "user_directory.go"
              :header "package userdirectory"}]})

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
