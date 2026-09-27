(ns lang-demos.tui-004-substrate-scratch.build
  (:require [js.blessed.project :as blessed-project]
            [js.webpack :as webpack]
            [std.make :as make :refer [def.make]]
            [lang-demos.tui-004-substrate-scratch.main]))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-004-substrate-scratch"
          {"main" "dist/main.js"
           "imports" {"#app/*" "./src/*"}})))

(def +makefile+
  (:main webpack/+node-makefile+))

(def.make TUI-004-SUBSTRATE-SCRATCH
  {:tag "tui-004-substrate-scratch"
   :build ".build/demo/tui-004-substrate-scratch"
   :triggers '#{lang-demos.tui-004-substrate-scratch.main
                lang-demos.js-002-substrate-scratch.main}
   :sections {:setup [{:type :gitignore
                       :main ["dist" "node_modules" "out"]}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}
                      webpack/+node-basic+]}
   :default [{:type :module.directory
              :lang :js
              :main 'lang-demos.tui-004-substrate-scratch.main
              :search ["src-build"]
              :target "src"
              :emit {:lang/format :commonjs
                     :code {:link {:path-suffix ".js"
                                   :root-prefix "#app"}}}}]})

(defn build-tui-004-substrate-scratch
  []
  (make/build-all TUI-004-SUBSTRATE-SCRATCH))

(defn -main
  []
  (build-tui-004-substrate-scratch)
  (shutdown-agents))
