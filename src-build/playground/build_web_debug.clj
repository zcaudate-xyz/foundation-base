(ns playground.build-web-debug
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.make :as make :refer [def.make]]
            [playground.web-debug-index :as web-debug]))

(def +expo-makefile+
  {:type  :makefile
   :main  '[[:init
             [yarn install]]
            [:build-web
             [yarn install]
             [npx expo build:web]]
            [:dev
             [yarn install]
             [npx expo start --web --port 19007]]
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

(def.make PLAYGROUND-WEB-DEBUG
  {:tag      "web-debug"
   :build    ".build/web-debug"
   :github   {:repo   "zcaudate-xyz/demo.web-debug"
              :description "Web Debug"}
   :sections {:common [+expo-makefile+
                       +github-workflows-build+
                       {:type :raw
                        :file "metro.config.js"
                        :main
                        ["const { getDefaultConfig } = require('expo/metro-config');"
                         ""
                         "const config = getDefaultConfig(__dirname);"
                         ""
                         "// Extend asset and source extensions"
                         "config.resolver.assetExts.push('db', 'ttf'); // Add 'ttf' for TrueType Fonts"
                         "config.resolver.sourceExts.push('db'); // If you have custom '.db' files that need resolving"
                         ""
                         "module.exports = config;"]}]
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
                                {"name" "Web Debug"
                                 "slug" "web-debug"
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
                        :main
                        {"main" "./src/App.js",
                         "name"  "web-debug"
                         "scripts" {"start" "expo start"
                                    "android" "expo start --android"
                                    "ios" "expo start --ios"
                                    "web" "expo start --web"
                                    "eject" "expo eject"}
                         "private" true
                         "dependencies" {"expo" "~53.0.17"
                                         "expo-auth-session" "^6.2.1"
                                         "react" "19.0.0"
                                         "react-dom" "19.0.0"
                                         "react-native" "0.79.5"
                                         "react-native-web" "~0.20.0"
                                         "uuid" "^11.1.0"}
                         "devDependencies" {"@babel/core" "^7.25.2"
                                            "@types/react" "~19.0.10"
                                            "eslint" "^9.25.0"
                                            "eslint-config-expo" "~9.2.0"
                                            "typescript" "~5.8.3"
                                            "@expo/metro-runtime" "^5.0.4"}
                         "metro" {"watchFolders" ["assets"]}}}]}
   :default [{:type   :module.graph
              :lang   :js
              :target "src"
              :main   'playground.web-debug-index
              :emit   {:code   {:label true}}}]})

(def +init+
  (make/triggers-set
   PLAYGROUND-WEB-DEBUG
   #{"js"
     "playground.web-debug"}))

(comment

  (make/build-all PLAYGROUND-WEB-DEBUG)
  (do (make/build-all PLAYGROUND-WEB-DEBUG)
      (make/gh:dwim-init PLAYGROUND-WEB-DEBUG))
  (make/gh:dwim-init PLAYGROUND-WEB-DEBUG)
  (make/gh:dwim-push PLAYGROUND-WEB-DEBUG)
  (def *res*
    (future (make/run-internal PLAYGROUND-WEB-DEBUG :build-web)))
  (def *res*
    (make/run PLAYGROUND-WEB-DEBUG :build-web))
  (make/run PLAYGROUND-WEB-DEBUG :dev)
  )
