(ns app.nextjs.build
  "Publication of the copied devtools only. The package does not include any application layout."
  (:require [app.manage.make :as make]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def output-root "target/app-nextjs")
(defn artifacts
  [context]
  (assoc (make/render-sections
          context
          {:load ['app.nextjs.devtools]
           :sections [{:type :module.graph
                       :lang :js
                       :main 'app.nextjs.devtools
                       :target (str output-root "/src")
                       :emit {:lang/jsx false :code {:label false :link {:path-suffix ".mjs"}}}}]})
         (str output-root "/package.json")
         (str (json/write-str
               {:name "@app/nextjs"
                :version "0.1.0"
                :private true
                :type "module"
                :exports {"." "./src/devtools.mjs"
                          "./devtools" "./src/devtools.mjs"
                          "./devtools/*" "./src/devtools/*.mjs"}
                :files ["src"]
                :peerDependencies
                {"react" ">=18" "react-dom" ">=18" "tamagui" "^1" "@tamagui/lucide-icons" "^1"}})
              "\n")))
(def PROJECT
  {:id 'app.nextjs
   :root "."
   :default-targets [:devtools]
   :project-sources ["src/app/nextjs/build.clj"]
   :targets {:devtools {:sources ["src/app/nextjs/devtools.clj" "src/app/nextjs/devtools"]
                        :outputs [output-root]
                        :render artifacts}}})
