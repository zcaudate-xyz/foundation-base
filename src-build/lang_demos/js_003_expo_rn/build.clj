(ns lang-demos.js-003-expo-rn.build
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
             [yarn exec expo export --platform web --output-dir web-build]]
            [:dev
             [yarn install]
             [yarn exec expo start --web --port 19007]]
            [:ios
             [yarn install]
             [yarn exec expo start --ios]]
            [:android
             [yarn install]
             [yarn exec expo start --android]]
            [:purge   [yarn exec expo r -c]]]})

(def +yarn-config+
  {:type :raw
   :file ".yarnrc.yml"
   ;; Expo Metro needs node_modules resolution for react-native-web deep imports.
   :main ["nodeLinker: node-modules"]})

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
               :with {:node-version "22.x"}}
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

(def.make JS-003-EXPO-RN
  {:tag      "web-debug"
   :build    ".build/demo/js-003-expo-rn"
   :github   {:repo   "zcaudate-xyz/demo.js-003-expo-rn"
              :description "Web Debug"}
   :triggers #{"js" "lang-demos.js-003-expo-rn"}
   :sections {:common [+expo-makefile+
                       +github-workflows-build+
                       +yarn-config+
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
                                {"name" "Demo JS-003-EXPO-RN"
                                 "slug" "demo.js-003-expo-rn"
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
                         "packageManager" "yarn@4.9.4"
                         "dependencies" {"expo" "~57.0.0"
                                         "expo-auth-session" "~57.0.12"
                                         "expo-crypto" "~57.0.3"
                                         "react" "19.2.3"
                                         "react-dom" "19.2.3"
                                         "react-native" "0.86.3"
                                         "react-native-web" "~0.21.0"
                                         "uuid" "^11.1.0"}
                         "devDependencies" {"@babel/core" "^7.25.2"
                                            "@types/react" "~19.2.4"
                                            "eslint" "^9.25.0"
                                            "eslint-config-expo" "~57.0.2"
                                            "typescript" "~6.0.3"
                                            "@expo/metro-runtime" "~57.0.16"}
                         "metro" {"watchFolders" ["assets"]}}}]}
   :default [{:type   :module.graph
              :lang   :js
              :target "src"
              :main   'lang-demos.js-003-expo-rn.main
              :emit   {:code {:label true
                              :link {:path-suffix ".js"
                                     :path-separator "/"
                                     :ns-label {'lang-demos.js-003-expo-rn.main "App"}}}}}]})

(defn build-js-003-expo-rn
  []
  (require '[lang-demos.js-003-expo-rn.main :as web-debug])
  (make/build-all JS-003-EXPO-RN)
  (make/run-internal JS-003-EXPO-RN :build-web))

(defn -main
  []
  (build-js-003-expo-rn)
  (shutdown-agents)
  (System/exit 0))


(comment

  (make/build-all JS-003-EXPO-RN)
  (do (make/build-all JS-003-EXPO-RN)
      (make/gh:dwim-init JS-003-EXPO-RN))
  (make/gh:dwim-init JS-003-EXPO-RN)
  (make/gh:dwim-push JS-003-EXPO-RN)
  (def *res*
    (future (make/run-internal JS-003-EXPO-RN :build-web)))
  (def *res*
    (make/run JS-003-EXPO-RN :build-web))
  (make/run JS-003-EXPO-RN :dev)
  )
