(ns lang-main.ui-components.web-index-build
  (:require [lang.core :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.make :as make :refer [def.make]]))

(def +expo-makefile+
  {:type  :makefile
   :main  '[[:init
             [yarn install]]
            [:build-web
             [yarn install]
             [npx expo export --platform web]]
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
          [:permissions {:contents "write"}]
          [:jobs
           {:build
            {:runs-on "ubuntu-latest"
             :steps
             [{:name "Checkout repo"
               :uses "actions/checkout@v4"}
              {:name "Node Setup"
               :uses "actions/setup-node@v4"
               :with {:node-version "20.x"}}
              {:name "Deploy gh-pages"
               :run
               (str/|
                "make build-web"
                "touch dist/.nojekyll"
                "git config --global user.name github-actions"
                "git config --global user.email github-actions@github.com"
                "cd dist && git init && git add -A && git commit -m 'deploying to gh-pages'"
                "git remote add origin https://x-access-token:${{ github.token }}@github.com/zcaudate-xyz/demo.foundation-base.git"
                "git push origin HEAD:gh-pages --force")}]}}]]})

(def.make WEB-INDEX
  {:tag      "web-index"
   :build    ".build/web-index"
   :github   {:repo   "zcaudate-xyz/demo.foundation-base"
              :description "Foundation Web Components"}
   :triggers #{"js" "lang-main.ui-components.web-index-main" "melbourne" "pune" "tama"}
   :sections {:common [+expo-makefile+
                       +github-workflows-build+
                       {:type :raw
                        :file "metro.config.js"
                        :main
                        ["const { getDefaultConfig } = require('expo/metro-config');"
                         ""
                         "const config = getDefaultConfig(__dirname);"
                         ""
                         "config.resolver.assetExts.push('db', 'ttf');"
                         "config.resolver.sourceExts.push('db');"
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
                         "dist/"
                         ".DS_Store"
                         "yarn.lock"
                         "yarn-error.log"]}
                       {:type :json
                        :file "app.json"
                        :main  {"expo"
                                {"name" "Foundation Web"
                                 "slug" "foundation-web"
                                 "version" "1.0.0"
                                 "orientation" "portrait"
                                 "entryPoint" "./src/App.js"
                                 "splash"
                                 {"resizeMode" "contain"
                                  "backgroundColor" "#ffffff"}
                                 "updates" {"fallbackToCacheTimeout" 0}
                                 "assetBundlePatterns" ["**/*"]
                                 "experiments" {"baseUrl" "/demo.foundation-base"}
                                 "ios" {"supportsTablet" true}}}}
                       {:type :package.json
                        :main
                        {"main" "./src/App.js"
                         "name" "foundation-web"
                         "scripts" {"start" "expo start"
                                    "android" "expo start --android"
                                    "ios" "expo start --ios"
                                    "web" "expo start --web"
                                    "eject" "expo eject"}
                         "private" true
                         "dependencies" {"@expo/vector-icons" "^15.0.2"
                                         "@tamagui/config" "2.0.0-rc.40"
                                         "@tamagui/get-token" "2.0.0-rc.40"
                                         "@tamagui/toast" "2.0.0-rc.40"
                                         "@react-navigation/bottom-tabs" "^7.3.10"
                                         "@react-navigation/elements" "^2.3.8"
                                         "@react-navigation/native" "^7.1.6"
                                         "ethers" "^6.15.0"
                                         "expo" "~57.0.24"
                                         "expo-auth-session" "~57.0.12"
                                         "expo-asset" "~57.0.18"
                                         "expo-blur" "~57.0.3"
                                         "expo-constants" "~57.0.19"
                                         "expo-crypto" "~57.0.3"
                                         "expo-font" "~57.0.4"
                                         "expo-haptics" "~57.0.3"
                                         "expo-image" "~57.0.5"
                                         "expo-image-picker" "~57.0.19"
                                         "expo-linking" "~57.0.10"
                                         "expo-router" "~57.0.22"
                                         "expo-splash-screen" "~57.0.9"
                                         "expo-status-bar" "~57.0.1"
                                         "expo-symbols" "~57.0.3"
                                         "expo-system-ui" "~57.0.4"
                                         "expo-web-browser" "~57.0.3"
                                         "react" "19.2.3"
                                         "react-dom" "19.2.3"
                                         "react-native" "0.86.3"
                                         "react-native-base64" "^0.2.1"
                                         "react-native-gesture-handler" "~2.32.0"
                                         "react-native-get-random-values" "^1.11.0"
                                         "react-native-reanimated" "4.5.1"
                                         "react-native-safe-area-context" "~5.7.0"
                                         "react-native-screens" "~4.26.0"
                                         "react-native-svg" "15.15.4"
                                         "react-native-vector-icons" "^10.2.0"
                                         "react-native-web" "~0.21.0"
                                         "lightweight-charts" "3.8.0"
                                         "react-native-webview" "13.16.1"
                                         "tamagui" "2.0.0-rc.40"
                                         "ua-parser-js" "^2.0.4"
                                         "url" "^0.11.4"
                                         "uuid" "^11.1.0"}
                         "devDependencies" {"@babel/core" "^7.25.2"
                                            "@types/react" "~19.2.4"
                                            "eslint" "^9.25.0"
                                            "eslint-config-expo" "~57.0.2"
                                            "typescript" "~6.0.3"
                                            "@expo/metro-runtime" "~57.0.16"}}}]}
   :default [{:type   :module.graph
              :lang   :js
              :target "src"
              :main   'lang-main.ui-components.web-index-main
              :emit   {:code   {:label true
                                :link    {:path-suffix ".js"
                                          :path-separator "/"
                                          :ns-label {'lang-main.ui-components.web-index-main "App"}}}}}]})

(def +init+
  nil)

(defn task-build
  []
  (require '[lang-main.ui-components.web-index-main :as web-index])
  (make/build-all WEB-INDEX))

(defn task-build-web
  []
  (make/run WEB-INDEX :build-web))

(defn -main
  [& [task]]
  (case task
    "build-web" (task-build-web)
    (task-build)))

(comment
  (make/build-all WEB-INDEX)
  (make/run WEB-INDEX :build-web))
