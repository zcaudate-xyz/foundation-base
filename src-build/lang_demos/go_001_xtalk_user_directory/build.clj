(ns lang-demos.go-001-xtalk-user-directory.build
  (:use [code.test :exclude [-main]])
  (:require [lang.model.builtin.spec-go.typed :as go-typed]
            [lang.typed.xtalk-analysis :as xtalk-analysis]
            [std.make :as make :refer [def.make]]
            [lang-demos.go-001-xtalk-user-directory.main :as main]))

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
  "src-build/lang_demos/go_001_xtalk_user_directory/main.clj")

(defn go-source
  [_]
  (-> +main-file+
      xtalk-analysis/analyze-file
      go-typed/emit-analysis-declarations))

(def.make GO-001-XTALK-USER-DIRECTORY
  {:build    ".build/demo/go-001-xtalk-user-directory"
   :github   {:repo "zcaudate/lang-demos.go-001-xtalk-user-directory"
              :description "Go declarations generated from canonical xtalk source"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.go-001-xtalk-user-directory.main}
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
  (make/build-all GO-001-XTALK-USER-DIRECTORY))

^{:eval false}
(fact "build the example into .build/demo/go-001-xtalk-user-directory"
  (make/build-all GO-001-XTALK-USER-DIRECTORY))

^{:eval false}
(fact "show generated go source"
  (make/run GO-001-XTALK-USER-DIRECTORY :show-source))

^{:eval false}
(fact "build the generated go module"
  (make/run GO-001-XTALK-USER-DIRECTORY :build))
