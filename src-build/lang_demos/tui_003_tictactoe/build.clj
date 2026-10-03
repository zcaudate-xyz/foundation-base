(ns lang-demos.tui-003-tictactoe.build
  (:require [js.blessed.project :as blessed-project]
            [js.webpack :as webpack]
            [std.make :as make :refer [def.make]]
            [lang-demos.tui-003-tictactoe.main]))

(def +package+
  (:main (blessed-project/module-package-json
          "tui-003-tictactoe"
          {"main" "dist/main.js"
           "imports" {"#app/*" "./src/*"}})))

(def +makefile+
  (:main webpack/+node-makefile+))

(def.make TUI-003-TICTACTOE
  {:tag "tui-003-tictactoe"
   :build ".build/demo/tui-003-tictactoe"
   :triggers '#{lang-demos.tui-003-tictactoe.main}
   :sections {:setup [{:type :gitignore
                       :main ["dist" "node_modules" "out"]}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}
                      webpack/+node-basic+]}
   :default [{:type :module.directory
              :lang :js
              :main 'lang-demos.tui-003-tictactoe.main
              :search ["src-build/lang_demos/tui_003_tictactoe"]
              :target "src"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "#app"}
                            :refine {'js.react {:treeshake true
                                                :ensure '[useInterval runIntervalStart]}}}}}]})

(defn build-tui-003-tictactoe
  []
  (make/build-all TUI-003-TICTACTOE))

(defn -main
  []
  (build-tui-003-tictactoe)
  (shutdown-agents))


(comment

  (make/run:init TUI-003-TICTACTOE)
  (make/run:dev  TUI-003-TICTACTOE)
  )
