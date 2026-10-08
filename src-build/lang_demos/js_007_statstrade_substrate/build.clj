(ns lang-demos.js-007-statstrade-substrate.build
  (:require [lang.core :as l]
            [std.make :as make :refer [def.make]]
            [lang-demos.js-007-statstrade-substrate.main]))

(def +package+
  {"name" "statstrade-substrate-example"
   "private" true
   "type" "module"
   "scripts" {"dev" "vite --host 0.0.0.0 --port 28012"
              "build" "vite build"
              "test" "npm run build && node smoke.cjs"}
   "dependencies" {"react" "19.2.3"
                   "react-dom" "19.2.3"
                   "react-native-web" "~0.21.0"}
   "devDependencies" {"vite" "^7.1.7"
                      "jsdom" "26.1.0"}})

(def +index+
  ["<!doctype html>"
   "<html lang=\"en\"><head><meta charset=\"utf-8\">"
   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
   "<title>Topic / xt.substrate</title></head>"
   "<body style=\"margin:0;background:#f7f8fa\"><main id=\"app\"></main>"
   "<script type=\"module\" src=\"./app.js\"></script></body></html>"])

(def.make JS-007-STATSTRADE-SUBSTRATE
  {:tag "lang-demos.js-007-statstrade-substrate"
   :build ".build/demo/js-007-statstrade-substrate"
   :triggers '#{lang-demos.js-007-statstrade-substrate.main
                lang-demos.js-007-statstrade-substrate.hook
                lang-demos.js-007-statstrade-substrate.model
                lang-demos.js-007-statstrade-substrate.link
                lang-demos.js-007-statstrade-substrate.fixture}
   :sections
   {:setup [{:type :package.json :main +package+}
            {:type :raw :file "vite.config.js"
             :main ["export default {define: {global: 'globalThis'}};"]}
            {:type :raw :file "index.html" :main +index+}]}
   :default
   [{:type :raw
     :file "smoke.cjs"
     :main (fn [] [(slurp "src-build/lang_demos/js_007_statstrade_substrate/smoke.cjs")])}
    {:type :raw
     :file "app.js"
     :main (fn []
             [(l/emit-script
               '(do (lang-demos.js-007-statstrade-substrate.main/mount))
               {:lang :js :layout :full
                :emit {:lang/jsx false
                       :override {"react-native" "react-native-web"}}})])}]})

(defn -main
  []
  (make/build-all JS-007-STATSTRADE-SUBSTRATE)
  (shutdown-agents)
  (System/exit 0))
