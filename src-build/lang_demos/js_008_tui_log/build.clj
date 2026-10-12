(ns lang-demos.js-008-tui-log.build
  (:require [js.blessed.project :as blessed-project]
            [js.webpack :as webpack]
            [lang.core :as l]
            [scaffold.supabase.local-min :as local-min]
            [std.make :as make :refer [def.make]]
            [lang-demos.js-008-tui-log.main]
            [lang-demos.js-008-tui-log.runtime]))

(def +package+
  (:main (blessed-project/module-package-json
          "js-008-tui-log"
          {"main" "dist/main.js"
           "packageManager" "yarn@4.6.0"
           "scripts" {"dev" "webpack --watch"
                      "start" "node dist/main.js"}
           "dependencies" {"pg" "^8.23.0"
                           "@sqlite.org/sqlite-wasm" "^3.53.4-build1"}
           "imports" {"#app/*" "./src/*"}
           "engines" {"node" ">=20.9.0"}})))

(def +makefile+
  '[[:init [yarn install]]
    [:dev [yarn dev]]
    [:run [node "dist/main.js"]]])

(def +yarn-config+
  {:type :raw
   :file ".yarnrc.yml"
   :main ["nodeLinker: node-modules"]})

(def +webpack-base+
  (assoc webpack/+node-basic+ :file "webpack.base.config.js"))

(def +webpack-config+
  {:type :raw
   :file "webpack.config.js"
   :main ["const path = require('path');"
          "const createConfig = require('./webpack.base.config');"
          ""
          "module.exports = function (env) {"
          "  const config = createConfig(env || {});"
          "  const resolve = config.resolve || {};"
          "  config.resolve = Object.assign({}, resolve, {"
          "    alias: Object.assign({}, resolve.alias || {}, {"
          "      'react-dom/client': path.resolve(__dirname, 'src/shims/react-dom-client.js'),"
          "      'react-nil': path.resolve(__dirname, 'src/shims/react-nil.js'),"
          "    }),"
          "  });"
          "  return config;"
          "};"]})

(def +react-dom-client-shim+
  {:type :raw
   :file "src/shims/react-dom-client.js"
   :main ["export function createRoot() {"
          "  throw new Error('React DOM roots are unavailable in the Blessed TUI.');"
          "}"
          ""
          "export default { createRoot };"]})

(def +react-nil-shim+
  {:type :raw
   :file "src/shims/react-nil.js"
   :main ["export function render() {"
          "  throw new Error('React NIL renderer is unavailable in the Blessed TUI.');"
          "}"
          ""
          "export default { render };"]})

(def +readme+
  {:type :raw
   :file "README.md"
   :main ["# JS-008 TUI Log"
          ""
          "A Blessed terminal app for the shared `scratch_v0.Log` activity log. It loads rows through `js.react.ext-table`, appends or deletes entries through public Supabase RPCs, and receives inserts, updates, and deletes over the private `scratch_v0:Log` Realtime Broadcast topic. Changes also appear in open JS-007 pages. The heading reports the Realtime connection state."
          ""
          "## Prepare and build"
          ""
          "From the Foundation repository root, start a REPL and run:"
          ""
          "```clojure"
          "(require '[lang-demos.js-008-tui-log.build :as demo])"
          "(demo/setup-scratch-v0)"
          "(demo/build-js-008-tui-log)"
          "```"
          ""
          "`setup-scratch-v0` uses the same local Supabase setup as JS-007, installs the row Broadcast trigger and channel policy, and waits for the `scratch_v0.Log` Data API to be ready."
          ""
          "## Run the TUI"
          ""
          "From another terminal, at the Foundation repository root:"
          ""
          "```sh"
          "cd .build/demo/js-008-tui-log"
          "yarn install"
          "yarn start"
          "```"
          ""
          "`yarn dev` only rebuilds the bundle in watch mode; use `yarn start` to display the terminal app."
          ""
          "The app uses the local-min Supabase URL and anon key by default. Set `SUPABASE_URL` and `SUPABASE_ANON_KEY` to override them. The anon key is public, and the sample RPCs allow anyone who can reach Supabase to append a message or delete a log row by its visible ID."]})

(def.make JS-008-TUI-LOG
  {:tag "js-008-tui-log"
   :build ".build/demo/js-008-tui-log"
   :triggers '#{lang-demos.js-008-tui-log.main
                lang-demos.js-008-tui-log.runtime}
   :sections {:setup [{:type :gitignore
                       :main ["dist" "node_modules" "out"]}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}
                      +yarn-config+
                      +webpack-base+
                      +webpack-config+
                      +react-dom-client-shim+
                      +react-nil-shim+
                      +readme+]}
   :default [{:type :module.directory
              :lang :js
              :main 'lang-demos.js-008-tui-log.main
              :search ["src-build/lang_demos/js_008_tui_log"]
              :target "src"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "#app"}}}}]})

(defn setup-scratch-v0
  []
  (let [rt (l/rt 'lang-demos.js-007-ext-page.build :postgres)]
    (l/rt:setup rt 'lang-demos.js-007-ext-page.build)
    (local-min/restart-postgrest)
    (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)
    true))

(defn build-js-008-tui-log
  []
  (make/build-all JS-008-TUI-LOG))

(defn -main
  []
  (build-js-008-tui-log)
  (shutdown-agents))

(comment
  (setup-scratch-v0)
  (build-js-008-tui-log)
  (make/run JS-008-TUI-LOG :dev))
