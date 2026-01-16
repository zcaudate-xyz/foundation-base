(ns component.build-native-index
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.make :as make :refer [def.make]]
            [component.web-native-index :as web-native-index]))

(def +expo-makefile+
  {:type  :makefile
   :main  '[[:init
             [yarn install]]
            [:build-web
             [yarn install]
             [npx expo build:web]]
            [:dev
             [yarn install]
             [npx expo start --web]]
            [:ios
             [yarn install]
             [npx expo start --ios]]
            [:android
             [yarn install]
             [npx expo start --android]]
            [:purge   [npx expo r -c]]]})

(def +github-workflows-build+
  {:type :yaml
   :file ".github/workflows/build.yml"
   :main [[:name "build gh-pages"]
          [:on ["push"]]
          [:jobs
           {:build
            {:runs-on "ubuntu-latest"
             :steps
             [{:name "Checkout repo"
               :uses "actions/checkout@v3"}
              {:name "Node Setup"
               :uses "actions/setup-node@v3"
               :with {:node-version "16.x"}}
              {:name "SSH Init"
               :run (str/|
                     "install -m 600 -D /dev/null ~/.ssh/id_rsa"
                     "echo '${{ secrets.GH_SSH_PRIVATE_KEY }}' > ~/.ssh/id_rsa"
                     "ssh-keyscan -H www.github.com > ~/.ssh/known_hosts")}
              
              {:name "Deploy gh-pages"
               :run
               (str/|
                "make build-web"
                "git config --global user.name github-actions"
                "git config --global user.email github-actions@github.com"
                "cd web-build && git init && git add -A && git commit -m 'deploying to gh-pages'"
                "git remote add origin git@github.com:zcaudate-xyz/demo.foundation-base.git"
                "git push origin HEAD:gh-pages --force")}]}}]]})

(def.make COMPONENT-NATIVE
  {:tag      "native"
   :build    ".build/native-index"
   :github   {:repo   "zcaudate-xyz/demo.foundation-base"
              :description "Js Web Components"}
   :triggers #{"js" "component.web-native"}
   :sections {:common [+expo-makefile+
                       +github-workflows-build+]
              :node   [{:type :gitignore,
                        :main
                        ["node_modules/**/*"
                         ".expo/*"
                         "npm-debug.*"
                         "*.jks"
                         "*.p8"
                         "*.p12"
                         "*.key"
                         "*.mobileprovision"
                         "*.orig.*"
                         "web-build/"
                         ".DS_Store"
                         "yarn.lock"
                         "yarn-error.log"]}
                       {:type :json
                        :file "app.json"
                        :main  {"expo"
                                {"name" "Js Web Components"
                                 "slug" "js-web-components"
                                 "version" "1.0.0",
                                 "orientation" "portrait",
                                 "entryPoint" "./src/App.js",
                                 "splash"
                                 {"resizeMode" "contain",
                                  "backgroundColor" "#ffffff"}
                                 "updates" {"fallbackToCacheTimeout" 0},
                                 "assetBundlePatterns" ["**/*"]
                                 "ios" {"supportsTablet" true},}}}
                       
                       {:type :package.json,
                        :main {"main" "node_modules/expo/AppEntry.js",
                               "name" "js-web-components",
                               "private" true,
                               "homepage" "/demo.foundation-base"
                               
                               "scripts"
                               {"start" "expo start",
                                "android" "expo start --android",
                                "ios" "expo start --ios",
                                "web" "expo start --web",
                                "eject" "expo eject"},
                               
                               "dependencies"
                               {"react-native-svg" "12.3.0",
                                "javascript-time-ago" "2.3.11",
                                "react-native" "0.68.1",
                                "expo-media-library" "14.1.0",
                                "expo-web-browser" "10.2.1",
                                "uuid" "8.3.2",
                                "react-dom" "17.0.2",
                                "lightweight-charts" "3.8.0",
                                "dateformat" "^4",
                                "react-native-vector-icons" "9.1.0",
                                "react-native-web" "0.17.7",
                                "react-native-error-boundary" "1.1.10",
                                "base-64" "1.0.0",
                                "react-native-base64" "0.1.0",
                                "react" "17.0.2",
                                "react-native-get-random-values" "1.8.0",
                                "expo-random" "12.2.0"},
                               "devDependencies"
                               {"@babel/core" "7.9.0",
                                "@babel/preset-env" "7.13.15",
                                "@types/react" "16.9.35",
                                "@types/react-native" "0.63.2",
                                "expo" "45.0.3",
                                "expo-cli" "6.3.7",
                                "typescript" "4.3.5"},
                               "metro" {"watchFolders" ["assets"]}}}]}
   :default [{:type   :module.graph
              :lang   :js
              :target "src"
              :main   'component.web-native-index
              :emit   {:code   {:label true
                                :link    {:path-suffix ".js"
                                          :path-separator "/"}}}}]})

(def +init+
  nil)

(comment

  (make/build-all COMPONENT-NATIVE)
  (do (make/build-all COMPONENT-NATIVE)
      (make/gh:dwim-init COMPONENT-NATIVE))
  (make/gh:dwim-init COMPONENT-NATIVE)
  (make/gh:dwim-push COMPONENT-NATIVE)
  (def *res*
    (future (make/run-internal COMPONENT-NATIVE :build-web)))
  (def *res*
    (make/run COMPONENT-NATIVE :build-web))
  (make/run COMPONENT-NATIVE :dev)
  )
