(ns component.build-native-v1
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

(def.make COMPONENT-NATIVE
  {:tag      "native-v1"
   :build    ".build/native-v1"
   :github   {:repo   "zcaudate-xyz/demo.foundation-base"
              :description "Js Web Components"}
   :triggers #{"js" "component.web-native"}
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
                        :main
                        { ;;"main" "node_modules/expo/AppEntry.js",
                         "main" "./src/App.js",
                         "name"  "native-v1"
                         "scripts" {"start" "expo start"
                                    "android" "expo start --android"
                                    "ios" "expo start --ios"
                                    "web" "expo start --web"
                                    "eject" "expo eject"}
                         "private" true
                         "dependencies" {"@expo/vector-icons" "^14.1.0"
                                         "@react-navigation/bottom-tabs" "^7.3.10"
                                         "@react-navigation/elements" "^2.3.8"
                                         "@react-navigation/native" "^7.1.6"
                                         "ethers" "^6.15.0"
                                         "expo" "~53.0.17"
                                         "expo-auth-session" "^6.2.1"
                                         "expo-blur" "~14.1.5"
                                         "expo-constants" "~17.1.7"
                                         "expo-crypto" "^14.1.5"
                                         "expo-font" "~13.3.2"
                                         "expo-haptics" "~14.1.4"
                                         "expo-image" "~2.3.2"
                                         "expo-image-picker" "^16.1.4"
                                         "expo-linking" "~7.1.7"
                                         "expo-router" "~5.1.3"
                                         "expo-splash-screen" "~0.30.10"
                                         "expo-status-bar" "~2.2.3"
                                         "expo-symbols" "~0.4.5"
                                         "expo-system-ui" "~5.0.10"
                                         "expo-web-browser" "~14.2.0"
                                         "react" "19.0.0"
                                         "react-dom" "19.0.0"
                                         "react-native" "0.79.5"
                                         "react-native-base64" "^0.2.1"
                                         "react-native-gesture-handler" "~2.24.0"
                                         "react-native-get-random-values" "^1.11.0"
                                         "react-native-reanimated" "~3.17.4"
                                         "react-native-safe-area-context" "5.4.0"
                                         "react-native-screens" "~4.11.1"
                                         "react-native-svg" "~15.11.2"
                                         "react-native-vector-icons" "^10.2.0"
                                         "react-native-web" "~0.20.0"
                                         "react-native-webview" "13.13.5"
                                         "ua-parser-js" "^2.0.4"
                                         "url" "^0.11.4"
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
              :main   'component.web-native-index
              :emit   {:code   {:label true}}}]})

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
