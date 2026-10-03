(ns lang-demos.tui-004-substrate-scratch.build
  (:require [js.blessed.project :as blessed-project]
            [js.webpack :as webpack]
            [std.make :as make :refer [def.make]]
            [lang-demos.tui-004-substrate-scratch.main]))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-004-substrate-scratch"
          {"main" "dist/main.js"
           "imports" {"#app/*" "./src/*"}
           "dependencies" {"@sqlite.org/sqlite-wasm" "^3.53.4-build1"
                          "pg" "^8.23.0"}})))

(def +makefile+
  (:main webpack/+node-makefile+))

(def.make TUI-004-SUBSTRATE-SCRATCH
  {:tag "tui-004-substrate-scratch"
   :build ".build/demo/tui-004-substrate-scratch"
   :triggers '#{lang-demos.tui-004-substrate-scratch.main
                lang-demos.tui-004-substrate-scratch.main-data}
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
              :search ["src-build/lang_demos/tui_004_substrate_scratch"]
              :target "src"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "#app"}
                            :refine {'js.react {:treeshake true
                                                :ensure '[useInterval runIntervalStart]}}}}}]})

(defn build-tui-004-substrate-scratch
  []
  (make/build-all TUI-004-SUBSTRATE-SCRATCH))

(defn -main
  []
  (build-tui-004-substrate-scratch)
  (shutdown-agents))
