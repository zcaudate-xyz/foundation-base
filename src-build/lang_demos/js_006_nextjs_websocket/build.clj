(ns lang-demos.js-006-nextjs-websocket.build
  (:require [std.make :as make :refer [def.make]]))

(def +makefile+
  {:type :makefile
   :main '[[:init [yarn install]]
          [:dev [yarn dev]]
          [:build [yarn build]]
          [:start [yarn start]]]})

(def +package+
  {:type :package.json
   :main {"name" "demo-js-006-nextjs-websocket"
          "private" true
          "packageManager" "yarn@4.9.4"
          "engines" {"node" ">=20.9.0"}
          "scripts" {"dev" "next dev"
                     "build" "next build"
                     "start" "next start"}
          "dependencies" {"next" "latest"
                          "react" "latest"
                          "react-dom" "latest"}}})

(def +styles+
  [[":root" {:color-scheme :dark
             :font-family "Inter, ui-sans-serif, system-ui, sans-serif"
             :background "#07110f"
             :color "#eff8f4"}]
   ["*" {:box-sizing :border-box}]
   ["body" {:margin 0
            :min-height "100vh"
            :background "radial-gradient(circle at 14% 0%, #1c4738 0, #0b1b16 34rem, #07110f 70rem)"}]
   ["button, input" {:font :inherit}]
   [".shell" {:width "min(1120px, 100%)"
              :margin "0 auto"
              :padding "clamp(2rem, 7vw, 5.5rem) 1.25rem 2rem"}]
   [".hero" {:display :flex
             :align-items :flex-end
             :justify-content :space-between
             :flex-wrap :wrap
             :gap "2rem"
             :margin-bottom "2rem"}]
   [".eyebrow, .connection-label" {:margin "0 0 .8rem"
                                   :color "#72e4b8"
                                   :font-size ".68rem"
                                   :font-weight 700
                                   :letter-spacing ".16em"
                                   :text-transform :uppercase}]
   [".hero h1" {:max-width "720px"
                :margin 0
                :font-size "clamp(2.4rem, 7vw, 5.6rem)"
                :line-height ".96"
                :letter-spacing "-.055em"}]
   [".hero-copy" {:max-width "620px"
                  :margin "1.1rem 0 0"
                  :color "#a7c7b9"
                  :line-height 1.65}]
   [".connection-card" {:display :grid
                        :grid-template-columns "auto 1fr"
                        :align-items :center
                        :column-gap ".8rem"
                        :row-gap ".2rem"
                        :min-width "200px"
                        :padding ".9rem 1rem"
                        :border "1px solid #2a5546"
                        :border-radius "14px"
                        :background "rgba(7, 20, 16, .74)"}]
   [".connection-dot" {:grid-row "span 2"
                       :width ".65rem"
                       :height ".65rem"
                       :border-radius "50%"
                       :background "#82938b"}]
   [".connection-dot.connecting" {:background "#e6bb61"}]
   [".connection-dot.connected" {:background "#5ee5a6"
                                  :box-shadow "0 0 16px #5ee5a6"}]
   [".connection-dot.error" {:background "#f07171"
                              :box-shadow "0 0 16px #f07171"}]
   [".connection-dot.disconnected" {:background "#82938b"}]
   [".connection-label" {:margin 0 :font-size ".58rem"}]
   [".connection-card strong" {:font-size ".92rem" :text-transform :capitalize}]
   [".connection-card code" {:grid-column 2 :color "#9bb8aa" :font-size ".7rem"}]
   [".users-panel" {:padding "clamp(1rem, 3vw, 1.6rem)"
                    :border "1px solid #27483c"
                    :border-radius "22px"
                    :background "rgba(10, 24, 20, .86)"
                    :box-shadow "0 24px 80px rgba(0, 0, 0, .25)"}]
   [".panel-heading" {:display :flex
                      :align-items :center
                      :justify-content :space-between
                      :gap "1rem"
                      :margin-bottom "1rem"}]
   [".panel-heading .eyebrow" {:margin-bottom ".35rem"}]
   [".panel-heading h2" {:margin 0 :font-size "1.3rem"}]
   [".panel-count" {:padding ".45rem .7rem"
                    :border "1px solid #315947"
                    :border-radius "999px"
                    :color "#b6ddca"
                    :font-size ".72rem"}]
   [".user-grid" {:display :grid
                  :grid-template-columns "repeat(auto-fit, minmax(220px, 1fr))"
                  :gap ".85rem"}]
   [".user-card" {:min-width 0
                  :padding "1.1rem"
                  :border "1px solid #254337"
                  :border-radius "15px"
                  :background "linear-gradient(145deg, #11251e, #0b1915)"}]
   [".user-card-status" {:display :flex
                         :align-items :center
                         :gap ".45rem"
                         :color "#72e4b8"
                         :font-size ".7rem"
                         :text-transform :uppercase
                         :letter-spacing ".1em"}]
   [".user-card-dot" {:width ".42rem"
                      :height ".42rem"
                      :border-radius "50%"
                      :background "#72e4b8"}]
   [".user-card h2" {:margin ".8rem 0 .3rem" :font-size "1.1rem"}]
   [".user-card p" {:margin 0 :color "#9eb7ab" :font-size ".88rem"}]
   [".footer" {:padding "1.1rem .25rem 0"
               :color "#7f9d8e"
               :font-size ".78rem"
               :line-height 1.55}]])

(def +gitignore+
  {:type :gitignore
   :main ["node_modules/"
          ".next/"
          "out/"
          "build/"
          ".env*"
          "!.env.example"]})

(def +env-example+
  {:type :raw
   :file ".env.example"
   :main ["# Optional websocket endpoint overrides. Defaults: page hostname and port 29002."
          "# NEXT_PUBLIC_LANG_WS_HOST=localhost"
          "# NEXT_PUBLIC_LANG_WS_PORT=29002"]})

(def +readme+
  {:type :raw
   :file "README.md"
   :main ["# JS-006 Next.js websocket demo"
          ""
          "This is the App Router version of the Expo user-card page. It connects from a client component to the Foundation websocket runtime and keeps the reactive user cards mounted while incoming code updates the page data."
          ""
          "## Start the runtime and build the app"
          ""
          "From the Foundation repository root, start a REPL and run:"
          ""
          "```clojure"
          "(require '[lang-demos.js-006-nextjs-websocket.build :as demo])"
          "(demo/build-js-006-nextjs-websocket)"
          "```"
          ""
          "Loading the source starts the websocket runtime on port 29002. Keep that REPL open."
          ""
          "In another terminal, start Next.js:"
          ""
          "```sh"
          "cd .build/demo/js-006-nextjs-websocket"
          "yarn install"
          "yarn dev"
          "```"
          ""
          "The page uses the browser hostname and port 29002 by default. Copy `.env.example` to `.env.local` and set `NEXT_PUBLIC_LANG_WS_HOST` or `NEXT_PUBLIC_LANG_WS_PORT` when the websocket runtime is on another host or port. HTTPS pages use `wss`."]})

(def.make JS-006-NEXTJS-WEBSOCKET
  {:tag "play-js-006-nextjs-websocket"
   :build ".build/demo/js-006-nextjs-websocket"
   :triggers '#{"js" "lang-demos.js-006-nextjs-websocket"}
   :sections {:setup [+makefile+
                      +package+
                      +gitignore+
                      {:type :css :target "src/app" :file "globals.css" :main +styles+}
                      +env-example+
                      +readme+]}
   :default [{:type :module.directory
              :lang :js
              :search ["src-build"]
              :main 'lang-demos.js-006-nextjs-websocket.main
              :target "src/generated"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "./"}}}}
             {:type :module.directory
              :lang :js
              :search ["src-build"]
              :main 'lang-demos.js-006-nextjs-websocket.app.page
              :target "src/app"
              :header "\"use client\";\nimport { App } from '../generated/main.js';"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "./"}}}}
             {:type :module.directory
              :lang :js
              :search ["src-build"]
              :main 'lang-demos.js-006-nextjs-websocket.app.layout
              :target "src/app"
              :header "import './globals.css';"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "./"}}}}]})

(defn build-js-04M06-nextjs-websocket
  []
  (require '[lang-demos.js-006-nextjs-websocket.main :as main])
  (make/build-all JS-006-NEXTJS-WEBSOCKET))
