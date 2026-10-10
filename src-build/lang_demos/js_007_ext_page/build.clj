(ns lang-demos.js-007-ext-page.build
  (:require [lang.core :as l]
            [postgres.core.supabase :as s]
            [scaffold.supabase.local-min :as local-min]
            [std.make :as make :refer [def.make]]))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0]
             [postgres.core]
             [postgres.core.supabase :as s]]
   :config {:host (-> local-min/+config+ :db :host)
            :port (-> local-min/+config+ :db :port)
            :user (-> local-min/+config+ :db :user)
            :pass (-> local-min/+config+ :db :password)
            :dbname (-> local-min/+config+ :db :database)
            :startup local-min/start-supabase
            :shutdown local-min/stop-supabase}
   :emit {:code {:transforms {:entry [#'s/transform-entry]}}}})

(defrun.pg __init__
  (s/grant-usage #{"scratch_v0"}))

(defn setup-scratch-v0
  "Starts local-min, installs scratch_v0, and waits for PostgREST."
  []
  (l/rt:teardown :postgres)
  (l/rt:setup :postgres)
  (local-min/restart-postgrest)
  (local-min/wait-for-postgrest-ready "scratch_v0" "Log" 120000)
  true)

(def +makefile+
  {:type :makefile
   :main '[[:init [yarn install]]
           [:dev [yarn dev]]
           [:build [yarn build]]
           [:start [yarn start]]]})

(def +package+
  {:type :package.json
   :main {"name" "lang-demos-js-007-ext-page"
          "private" true
          "packageManager" "yarn@4.9.4"
          "engines" {"node" ">=20.9.0"}
          "scripts" {"dev" "next dev"
                     "build" "next build"
                     "start" "next start"}
          "dependencies" {"next" "16.3.6"
                          "react" "19.2.3"
                          "react-dom" "19.2.3"
                          "react-native" "0.86.3"
                          "react-native-web" "0.21.0"
                          "@sqlite.org/sqlite-wasm" "^3.51.2-build8"}}})

(def +styles+
  [[":root" {:color-scheme :light
             :font-family "Inter, ui-sans-serif, system-ui, sans-serif"
             :background "#f4f6f8"
             :color "#17232b"}]
   ["*" {:box-sizing :border-box}]
   ["body" {:margin 0
            :min-height "100vh"
            :background "linear-gradient(180deg, #eef3f5 0, #f7f8f9 23rem, #f7f8f9 100%)"}]
   ["button, input, textarea" {:font :inherit}]
   ["button" {:cursor :pointer}]
   [".shell" {:width "min(1180px, 100%)"
              :margin "0 auto"
              :padding "clamp(1.25rem, 4vw, 3.5rem) 1.25rem 3rem"}]
   [".topbar" {:display :flex
               :align-items :center
               :justify-content :space-between
               :gap "1rem"
               :padding-bottom "1.8rem"
               :border-bottom "1px solid #dce3e6"}]
   [".brand" {:display :flex
              :align-items :center
              :gap ".75rem"
              :color "#1a2e37"
              :font-size ".82rem"
              :font-weight 750
              :letter-spacing ".02em"}]
   [".brand-mark" {:display :grid
                   :width "2rem"
                   :height "2rem"
                   :place-items :center
                   :border-radius ".65rem"
                   :background "#1c806b"
                   :color "#fff"
                   :font-size ".72rem"
                   :font-weight 800}]
   [".topbar-tag" {:padding ".45rem .7rem"
                   :border "1px solid #d8e2e5"
                   :border-radius "999px"
                   :background "#fff"
                   :color "#62727a"
                   :font-size ".69rem"
                   :font-weight 650
                   :letter-spacing ".04em"}]
   [".intro" {:display :grid
              :grid-template-columns "minmax(0, 1fr) auto"
              :align-items :end
              :gap "2rem"
              :padding "2.5rem 0 1.7rem"}]
   [".eyebrow" {:margin "0 0 .65rem"
                :color "#277866"
                :font-size ".67rem"
                :font-weight 750
                :letter-spacing ".13em"
                :text-transform :uppercase}]
   [".intro h1" {:margin 0
                 :font-size "clamp(2.2rem, 5vw, 3.6rem)"
                 :line-height 1
                 :letter-spacing "-.055em"
                 :font-weight 720}]
   [".intro-copy" {:max-width "680px"
                   :margin ".8rem 0 0"
                   :color "#64747b"
                   :font-size ".98rem"
                   :line-height 1.65}]
   [".live-chip" {:display :inline-flex
                  :align-items :center
                  :gap ".5rem"
                  :padding ".62rem .82rem"
                  :border "1px solid #d7e6e1"
                  :border-radius "999px"
                  :background "#fff"
                  :color "#3d5a53"
                  :font-size ".72rem"
                  :font-weight 650
                  :white-space :nowrap}]
   [".live-dot" {:width ".5rem"
                 :height ".5rem"
                 :border-radius "50%"
                 :background "#31a47f"
                 :box-shadow "0 0 0 3px #e2f3ec"}]
   [".panel" {:min-width 0
              :padding "1.25rem"
              :border "1px solid #e0e6e8"
              :border-radius "16px"
              :background "rgba(255, 255, 255, .96)"
              :box-shadow "0 12px 32px rgba(28, 47, 55, .045)"}]
   [".workspace" {:display :grid
                  :grid-template-columns "repeat(auto-fit, minmax(min(100%, 320px), 1fr))"
                  :align-items :start
                  :gap "1rem"}]
   [".panel-heading, .list-header" {:display :flex
                                   :align-items :start
                                   :justify-content :space-between
                                   :gap "1rem"
                                   :margin-bottom "1rem"}]
   [".panel-heading h2, .list-header h2" {:margin 0
                                         :font-size "1rem"
                                         :letter-spacing "-.015em"}]
   [".panel-subtitle" {:margin ".35rem 0 0"
                       :color "#78878d"
                       :font-size ".78rem"
                       :line-height 1.5}]
   [".entry-form" {:display :grid :gap ".9rem"}]
   [".field-label" {:display :block
                    :margin-bottom "-.45rem"
                    :color "#53636a"
                    :font-size ".74rem"
                    :font-weight 700}]
   [".entry-input" {:width "100%"
                    :min-height "112px"
                    :padding ".8rem .85rem"
                    :border "1px solid #dbe3e5"
                    :border-radius "10px"
                    :background "#fbfcfc"
                    :color "#17232b"
                    :font-size ".88rem"
                    :line-height 1.5
                    :resize :vertical}]
   [".entry-input:focus" {:outline "3px solid rgba(39, 120, 102, .14)"
                          :border-color "#4a9a84"}]
   [".submit-button" {:width "100%"
                      :min-height "42px"
                      :border 0
                      :border-radius "10px"
                      :background "#1f806b"
                      :color "#fff"
                      :font-size ".82rem"
                      :font-weight 700}]
   [".submit-button:disabled" {:opacity ".55" :cursor :not-allowed}]
   [".notice" {:min-height "1.1rem"
               :margin 0
               :color "#26745f"
               :font-size ".75rem"
               :line-height 1.45}]
   [".notice.error" {:color "#a43b3b"}]
   [".count" {:padding ".35rem .6rem"
              :border-radius "999px"
              :background "#edf5f2"
              :color "#347866"
              :font-size ".7rem"
              :font-weight 700
              :white-space :nowrap}]
   [".log-list" {:display :grid :gap ".65rem"}]
   [".log-row" {:display :grid
                :grid-template-columns "2rem minmax(0, 1fr)"
                :gap ".75rem"
                :align-items :start
                :padding ".85rem"
                :border "1px solid #e8edef"
                :border-radius "11px"
                :background "#fcfdfd"}]
   [".row-index" {:display :grid
                  :width "1.8rem"
                  :height "1.8rem"
                  :place-items :center
                  :border-radius ".55rem"
                  :background "#eff5f3"
                  :color "#397966"
                  :font-size ".65rem"
                  :font-weight 750}]
   [".row-message" {:margin ".08rem 0 .4rem"
                    :color "#293a40"
                    :font-size ".84rem"
                    :line-height 1.5
                    :overflow-wrap :anywhere}]
   [".row-id" {:color "#87959a"
               :font-family "ui-monospace, SFMono-Regular, Menlo, monospace"
               :font-size ".62rem"}]
   [".empty-state" {:padding "2.5rem 1rem"
                    :border "1px dashed #dce5e6"
                    :border-radius "12px"
                    :color "#819096"
                    :font-size ".82rem"
                    :text-align :center}]
   [".footer" {:padding "1.25rem .15rem 0"
               :color "#829096"
               :font-size ".7rem"
               :line-height 1.55}]])

(def +gitignore+
  {:type :gitignore
   :main ["node_modules/"
          ".next/"
          "out/"
          "build/"
          ".env*"
          "!.env.example"]})

(def +yarn-config+
  {:type :raw
   :file ".yarnrc.yml"
   :main ["nodeLinker: node-modules"]})

(def +yarn-lock+
  {:type :resource
   :main [["lang-demos/js-007-ext-page/yarn.lock" "yarn.lock"]]})

(def +next-config+
  {:type :raw
   :file "next.config.js"
   :main ["const nextConfig = {"
          "  transpilePackages: ['react-native', 'react-native-web'],"
          "  turbopack: {"
          "    root: __dirname,"
          "    resolveAlias: { 'react-native': 'react-native-web' },"
          "  },"
          "};"
          ""
          "module.exports = nextConfig;"]})

(def +js-config+
  {:type :raw
   :file "jsconfig.json"
   :main ["{" 
          "  \"compilerOptions\": {"
          "    \"baseUrl\": \".\","
          "    \"paths\": { \"pg\": [\"./src/shims/pg.js\"] }"
          "  }"
          "}"]})

(def +pg-browser-shim+
  {:type :raw
   :file "src/shims/pg.js"
   :main ["export class Client {"
          "  constructor() {"
          "    throw new Error('The PostgreSQL client is server-only and unavailable in browser builds.');"
          "  }"
          "}"]})

(def +env-example+
  {:type :raw
   :file ".env.example"
   :main ["# The browser uses the public anon key for read-only Log access."
          "NEXT_PUBLIC_SUPABASE_URL=http://127.0.0.1:55121"
          "NEXT_PUBLIC_SUPABASE_ANON_KEY="
          ""
          "# The server route uses this key only when calling log_append_public."
          "SUPABASE_URL=http://127.0.0.1:55121"
          "SUPABASE_SERVICE_ROLE_KEY="]})

(def +readme+
  {:type :raw
   :file "README.md"
   :main ["# JS-007 Ext Page"
          ""
          "A Next.js App Router sample that uses `js.react.ext-table` and `js.react.ext-model` to read the `scratch_v0.Log` table. The append form uses Melbourne UI input and button components."
          ""
          "## Prepare local Supabase and build"
          ""
          "From the Foundation repository root, start a REPL and run:"
          ""
          "```clojure"
          "(require '[lang-demos.js-007-ext-page.build :as demo])"
          "(demo/setup-scratch-v0)"
          "(demo/build-js-007-ext-page)"
          "```"
          ""
          "`setup-scratch-v0` starts the `scaffold.supabase.local-min` stack, installs the `scratch_v0` sample schema, and waits for PostgREST to expose `Log`."
          ""
          "## Configure and run Next.js"
          ""
          "In another terminal, from the Foundation repository root:"
          ""
          "```sh"
          "cd .build/demo/js-007-ext-page"
          "cp .env.example .env.local"
          "# Set SUPABASE_SERVICE_ROLE_KEY from config/scaffold/supabase-local.edn."
          "yarn install"
          "yarn dev"
          "```"
          ""
          "The app defaults to the local-min URL and anon key. Set `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_ANON_KEY` to override browser reads. Set `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` for the server route; the service role key is never included in the browser bundle."
          ""
          "The browser reads `Log` through the ext-table runtime. Submitting the form posts to `/api/log`, which invokes the existing `log_append_public` PostgREST RPC on the server, then refreshes the ext-model view. This sample intentionally has no sign-in flow, so anyone who can reach the app can append messages. Keep it local unless you add access controls."]})

(def.make JS-007-EXT-PAGE
  {:tag "lang-demos.js-007-ext-page"
   :build ".build/demo/js-007-ext-page"
   :triggers '#{"js" "lang-demos.js-007-ext-page"}
   :sections {:setup [+makefile+
                      +package+
                      +gitignore+
                      +yarn-config+
                      +yarn-lock+
                      +next-config+
                      +js-config+
                      +pg-browser-shim+
                      {:type :css :target "src/app" :file "globals.css" :main +styles+}
                      +env-example+
                      +readme+]}
   :default [{:type :module.graph
              :lang :js
              :main 'lang-demos.js-007-ext-page.main
              :target "src/generated"
              :emit {:code {:link {:path-suffix ".js"}}}}
             {:type :module.graph
              :lang :js
              :main 'lang-demos.js-007-ext-page.app.page
              :target "src/app"
              :header "\"use client\";\nimport dynamic from 'next/dynamic';\nconst App = dynamic(() => import('../generated/main.js').then((mod) => mod.App), { ssr: false });"
              :emit {:code {:link {:path-suffix ".js"}}}}
             {:type :module.graph
              :lang :js
              :main 'lang-demos.js-007-ext-page.app.layout
              :target "src/app"
              :header "import './globals.css';"
              :emit {:code {:link {:path-suffix ".js"}}}}
             {:type :module.graph
              :lang :js
              :main 'lang-demos.js-007-ext-page.app.api.log.route
              :target "src/app/api/log"
              :emit {:code {:link {:path-suffix ".js"}}}}]})

(defn build-js-007-ext-page
  []
  (require '[lang-demos.js-007-ext-page.main :as main])
  (require '[lang-demos.js-007-ext-page.app.page])
  (require '[lang-demos.js-007-ext-page.app.layout])
  (require '[lang-demos.js-007-ext-page.app.api.log.route])
  (make/build-all JS-007-EXT-PAGE))

(comment
  (setup-scratch-v0)
  (build-js-007-ext-page)
  (make/run-internal JS-007-EXT-PAGE :dev))
