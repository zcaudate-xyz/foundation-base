(ns demo-xtdb-backbone.build
  (:require [hara.lang :as l]
            [std.make :as make :refer [def.make]]
            [demo-xtdb-backbone.app.backbone]
            [demo-xtdb-backbone.app.main]
            [demo-xtdb-backbone.app.remote]
            [demo-xtdb-backbone.app.sharedworker]
            [demo-xtdb-backbone.app.worker-base]))

(def +sharedworker-webpack-config+
  (l/emit-as
   :js
   '[(var path (require "path"))
     (:= (. module exports)
         {:mode "production"
          :target "webworker"
          :entry {:index "./src/main.js"}
          :experiments {:asyncWebAssembly true}
          :resolve {:fallback {"child_process" false
                               "fs" false
                               "module" false
                               "os" false
                               "path" false
                               "worker_threads" false}}
          :output {:filename "demo-xtdb-backbone-worker.js"
                   :path (. path (resolve __dirname "dist"))}})]))

(def +webapp-index+
  ["<!doctype html>"
   "<html lang=\"en\">"
   "<head>"
   "  <meta charset=\"utf-8\" />"
   "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
   "  <title>demo-xtdb-backbone</title>"
   "  <link rel=\"stylesheet\" href=\"./styles.css\" />"
   "</head>"
   "<body>"
   "  <main id=\"app\"></main>"
   ""
   "  <script type=\"module\" src=\"./app.js\"></script>"
   "</body>"
   "</html>"])

(def +webapp-styles+
  [":root {"
   "  color-scheme: dark;"
   "  font-family: ui-sans-serif, system-ui, sans-serif;"
   "  background: #0b1020;"
   "  color: #e5eefc;"
   "}"
   ""
   "* { box-sizing: border-box; }"
   ""
   "body {"
   "  margin: 0;"
   "  min-height: 100vh;"
   "  background: linear-gradient(180deg, #0b1020, #121933);"
   "}"
   ""
   "#app {"
   "  min-height: 100vh;"
   "}"
   ""
   ".shell {"
   "  display: grid;"
   "  gap: 1rem;"
   "  max-width: 960px;"
   "  margin: 0 auto;"
   "  padding: 2rem;"
   "}"
   ""
   ".panel {"
   "  background: rgba(15, 23, 42, 0.92);"
   "  border: 1px solid rgba(148, 163, 184, 0.18);"
   "  border-radius: 16px;"
   "  padding: 1rem 1.25rem;"
   "  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.3);"
   "}"
   ""
   "form {"
   "  display: grid;"
   "  gap: 0.75rem;"
   "}"
   ""
   "label {"
   "  display: grid;"
   "  gap: 0.35rem;"
   "  font-size: 0.95rem;"
   "}"
   ""
   "input, button {"
   "  border-radius: 10px;"
   "  border: 1px solid rgba(148, 163, 184, 0.28);"
   "  padding: 0.7rem 0.85rem;"
   "  font: inherit;"
   "}"
   ""
   "input {"
   "  background: #020617;"
   "  color: inherit;"
   "}"
   ""
   "button {"
   "  width: fit-content;"
   "  background: #2563eb;"
   "  color: white;"
   "  cursor: pointer;"
   "}"
   ""
    "button:disabled {"
    "  opacity: 0.65;"
    "  cursor: not-allowed;"
    "}"
   ""
   "pre {"
   "  margin: 0;"
   "  padding: 0.9rem;"
   "  border-radius: 12px;"
   "  background: #020617;"
   "  overflow: auto;"
   "  white-space: pre-wrap;"
   "  word-break: break-word;"
   "}"])

(def +makefile+
 [[:.PHONY {:- ["init" "build" "build-worker" "sync-worker" "start"]}]
   [:init
    ["cd sharedworker && npm install"]]
   [:build-worker
    ["cd sharedworker && npx webpack"]]
   [:sync-worker
    ["mkdir -p webapp/workers"]
    ["cp sharedworker/dist/demo-xtdb-backbone-worker.js webapp/workers/demo-xtdb-backbone-worker.js"]]
   [:build
    ["make build-worker"]
    ["make sync-worker"]]
   [:start
    ["make build"]
    ["python3 -m http.server 8080 --directory webapp"]]])

(def +expected-files+
  ["Makefile"
   "sharedworker/package.json"
   "sharedworker/webpack.config.js"
   "sharedworker/src/custom.js"
   "sharedworker/src/main.js"
   "webapp/index.html"
   "webapp/styles.css"
   "webapp/app.js"])

(def.make DEMO-XTDB-BACKBONE
  {:tag "demo-xtdb-backbone"
   :build ".build/demo-xtdb-backbone"
   :triggers '#{demo-xtdb-backbone.app.backbone
                demo-xtdb-backbone.app.main
                demo-xtdb-backbone.app.remote
                demo-xtdb-backbone.app.sharedworker
                demo-xtdb-backbone.app.worker-base}
   :sections
   {:setup [{:type :makefile
             :main +makefile+}
            {:type :raw
             :target "sharedworker"
             :file "webpack.config.js"
             :main [+sharedworker-webpack-config+]}
            {:type :package.json
             :target "sharedworker"
             :main {"license" "ISC"
                    "name" "demo-xtdb-backbone-sharedworker"
                    "private" true
                    "main" "src/main.js"
                    "scripts" {"build" "webpack"}
                    "description" "scratch_v0 split sharedworker sample"
                    "dependencies" {"@sqlite.org/sqlite-wasm" "^3.51.2-build8"
                                    "@supabase/supabase-js" "^2.103.0"}
                    "devDependencies" {"webpack" "^5.97.1"
                                       "webpack-cli" "^4.10.0"}}}
            {:type :raw
             :target "sharedworker/src"
             :file "custom.js"
             :main ["export default {};"]}
            {:type :raw
             :target "webapp"
             :file "index.html"
             :main +webapp-index+}
            {:type :raw
             :target "webapp"
             :file "styles.css"
             :main +webapp-styles+}]}
   :default
   [{:type :raw
     :target "sharedworker/src"
     :file "main.js"
     :main (fn []
             (l/with:cache-none
               [(l/emit-script
                 '(do (demo-xtdb-backbone.app.sharedworker/runtime-init))
                 {:lang :js
                  :layout :full})]))}
    {:type :module.directory
     :lang :js
     :search ["src-build"]
     :main 'demo-xtdb-backbone.app.main
     :target "webapp"
     :emit {:code {:link {:path-suffix ".js"
                          :root-prefix "./"}}}}]})

(defn -main
  []
  (make/build-all DEMO-XTDB-BACKBONE)
  (shutdown-agents)
  (System/exit 0))
