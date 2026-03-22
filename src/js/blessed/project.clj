(ns js.blessed.project
  (:require [js.webpack :as webpack]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.foundation]))

;;
;; Packaging
;;

(defn module-package-json
  "creates a package json
 
   (module-package-json \"test-project\")
   => map?"
  {:added "4.0"}
  ([name & [m]]
   {:type :package.json
    :main (std.lib.collection/merge-nested
           {"name" name
            "main" "src/main.js",
            
            "scripts"
            {"dev" "npx webpack --watch"}

            "dependencies"
            {"blessed" "0.1.81",
             "raf" "3.4.1",
             "react" "17.0.2",
             "react-blessed" "0.7.2"},

            "devDependencies"
            {"@sucrase/webpack-loader" "2.0.0",
             "cache-loader" "4.1.0",
             "run-node-webpack-plugin" "1.3.0",
             "source-map-support" "0.5.19",
             "sucrase" "3.18.1",
             "webpack" "5.37.1",
             "webpack-cli" "4.7.0",
             "webpack-node-externals" "3.0.0"}

            "private" true}
           m)}))

(def +ui+
  {"chalk" "4.1.1"
   "javascript-time-ago" "2.3.5"
   "dateformat" "4.5.1"})

(def +state-proxy+
  {"valtio" "1.2.3"})

(def +state-atom+
  {"jotai" "0.16.6"})

(def +state-optics+
  {"optics-ts" "2.1.0"})

(def +state-fsm+
  {"xstate" "4.19.1"
   "@xstate/react" "1.3.3"})

(def +swr+
  {"swr" "0.5.6"})

(def +state-query+
  {"react-query" "^3.16.0"})

(def +pg+
  {"pg" "8.5.1"})

(def +fetch+
  {"node-fetch" "2.6.1"})

(def +window+
  {"window" "4.2.7"})

(def +ws+
  {"ws" "^7.4.4"})

(def +reconnecting-ws+
  {"reconnecting-websocket" "^4.4.0"})

(def +eventsource+
  {"eventsource" "1.1.0"})

(def +uuid+
  {"uuid" "8.3.2"})

(def +local-storage+
  {"node-localstorage" "2.2.1"})

(def +async-storage+
  {"@react-native-async-storage/async-storage" "1.15.5"})

(def +react-native+
  {"react-native" "0.63.4"})

(def +contrib+
  {"blessed-contrib" "4.8.21"
   "react-blessed-contrib" "0.2.1"})

(def +drawille+
  {"drawille" "1.1.1"})

(def +bresenham+
  {"bresenham" "0.0.4"})

(def +gl-matrix+
  {"gl-matrix" "3.3.0"})

(def +misc+
  {"qrcode-terminal" "^0.12.0"
   "node-localstorage" "2.1.6",})

(defn project
  "creates a blessed project
 
   (project \"Hello World\")
   => map?"
  {:added "4.0"}
  ([name & [m config]]
   {:type :lang
    :setup  [(or config webpack/+node-basic+)
             webpack/+node-makefile+
             webpack/+node-gitignore+
             (module-package-json (or name (std.lib.foundation/error "Name is required for project"))
                                  (or m {"dependencies" (clojure.core/merge +state-proxy+
                                                                            +ui+)}))]
    :output [{:type :graph
              :lang :js
              :main  (std.lib.env/ns-sym)
              :target "src"}]}))
