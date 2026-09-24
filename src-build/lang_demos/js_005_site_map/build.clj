(ns lang-demos.js-005-site-map.build
  (:require [lang.core :as l]
            [std.make :as make :refer [def.make]]
            [lang-demos.js-005-site-map.app.client]
            [lang-demos.js-005-site-map.app.server]))

(def +manifest+
  {"version" 1
   "site" "foundation-site-map-demo"
   "schema_url" "/worker/site-map/schema.json"
   "lookup_url" "/worker/site-map/lookup.json"
   "rpc_url" "/worker/site-map/rpc.json"
   "tables" ["Log"]
   "rpcs" ["ping"]
   "pages" ["lang-demos.js-005-site-map"]})

(def +schema+
  {"Log" {"id" {"type" "uuid" "primary" true}
           "message" {"type" "text" "required" true}
           "author-id" {"type" "uuid"}}})

(def +lookup+
  {"Log" {"position" 0}})

(def +rpc+
  {"ping" {"id" "ping"
            "schema" "scratch_v0"
            "input" []
            "return" "text"
            "flags" {}
            "meta" {}}})

(def +index+
  ["<!doctype html>"
   "<html lang=\"en\">"
   "<head>"
   "  <meta charset=\"utf-8\" />"
   "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
   "  <title>SharedWorker site-map demo</title>"
   "  <link rel=\"stylesheet\" href=\"./styles.css\" />"
   "</head>"
   "<body>"
   "  <main class=\"shell\">"
   "    <p class=\"eyebrow\">Foundation / SharedWorker / site-map</p>"
   "    <h1>The worker fetches the schema.</h1>"
   "    <p class=\"intro\">The page sends only its database config and a site-map URL. The SharedWorker fetches the manifest, schema, lookup, and RPC registry itself.</p>"
   "    <button id=\"connect\" type=\"button\">Connect and initialise</button>"
   "    <pre id=\"output\" aria-live=\"polite\">Press connect to initialise the database kernel.</pre>"
   "  </main>"
   "  <script type=\"module\" src=\"./client.js\"></script>"
   "</body>"
   "</html>"])

(def +styles+
  [":root { color-scheme: dark; font-family: ui-sans-serif, system-ui, sans-serif; background: #07110f; color: #eefaf5; }"
   "* { box-sizing: border-box; }"
   "body { margin: 0; min-height: 100vh; background: radial-gradient(circle at top left, #17382b, #07110f 56%); }"
   ".shell { width: min(900px, 100%); margin: 0 auto; padding: clamp(2rem, 8vw, 6rem) 1.25rem; display: grid; gap: 1.25rem; }"
   ".eyebrow { margin: 0; color: #6fe7b8; font-size: .72rem; letter-spacing: .14em; text-transform: uppercase; }"
   "h1 { max-width: 760px; margin: 0; font-size: clamp(2.8rem, 8vw, 5.6rem); line-height: .98; letter-spacing: -.05em; }"
   ".intro { max-width: 680px; color: #a7c7ba; font-size: 1.1rem; line-height: 1.65; }"
   "button { justify-self: start; border: 0; border-radius: 999px; padding: .85rem 1.2rem; background: #6fe7b8; color: #07110f; font: inherit; font-weight: 700; cursor: pointer; }"
   "button:disabled { opacity: .65; cursor: wait; }"
   "pre { min-height: 8rem; margin: 0; padding: 1rem; border: 1px solid #24463b; border-radius: 14px; background: rgba(6, 16, 13, .88); color: #c9e8db; overflow: auto; white-space: pre-wrap; word-break: break-word; line-height: 1.5; }"])

(def +makefile+
  [[:.PHONY {:- ["start"]}]
   [:start
    ["python3 -m http.server 8080 --directory public"]]])

(def +worker-overrides+
  {"@sqlite.org/sqlite-wasm" "data:text/javascript,export default {}"
   "pg" "data:text/javascript,export default {Client: function(){}}"})

(def.make JS-005-SITE-MAP
  {:tag "lang-demos.js-005-site-map"
   :build ".build/demo/js-005-site-map"
   :triggers '#{lang-demos.js-005-site-map.app.client
                lang-demos.js-005-site-map.app.server}
   :sections
   {:setup [{:type :makefile
             :main +makefile+}
            {:type :raw
             :target "public"
             :file "index.html"
             :main +index+}
            {:type :raw
             :target "public"
             :file "styles.css"
             :main +styles+}
            {:type :json
             :target "public/worker/site-map"
             :file "manifest.json"
             :main +manifest+}
            {:type :json
             :target "public/worker/site-map"
             :file "schema.json"
             :main +schema+}
            {:type :json
             :target "public/worker/site-map"
             :file "lookup.json"
             :main +lookup+}
            {:type :json
             :target "public/worker/site-map"
             :file "rpc.json"
             :main +rpc+}]}
   :default
   [{:type :raw
     :target "public/worker/site-map"
     :file "site-map-worker.js"
     :main (fn []
             (l/with:cache-none
               [(l/emit-script
                 '(do (lang-demos.js-005-site-map.app.server/runtime-init))
                 {:lang :js
                  :layout :full
                  :emit {:override +worker-overrides+}})]))}
    {:type :module.directory
     :lang :js
     :search ["src-build"]
     :main 'lang-demos.js-005-site-map.app.client
     :target "public"
     :emit {:code {:link {:path-suffix ".js"
                          :root-prefix "./"}}}}]})

(defn -main
  []
  (make/build-all JS-005-SITE-MAP)
  (shutdown-agents)
  (System/exit 0))
