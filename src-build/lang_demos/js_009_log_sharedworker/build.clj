(ns lang-demos.js-009-log-sharedworker.build
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [lang.core :as l]
            [lang-demos.js-007-ext-page.build :as js007]
            [std.make :as make :refer [def.make]]))

(def +makefile+
  {:type :makefile
   :main '[[:init [yarn install]]
           [:dev [yarn dev]]
           [:build [yarn build]]]})

(def +package+
  (-> js007/+package+
      (assoc-in [:main "name"] "lang-demos-js-009-log-sharedworker")
      (assoc-in [:main "scripts" "dev"] "webpack --config webpack.worker.config.js && next dev -p 3109")
      (assoc-in [:main "scripts" "build"] "webpack --config webpack.worker.config.js && next build")
      (assoc-in [:main "devDependencies" "webpack"] "^5.97.1")
      (assoc-in [:main "devDependencies" "webpack-cli"] "^4.10.0")))

(def +worker-webpack-config+
  ["const path = require('path');"
   "const webpack = require('webpack');"
   "require('@next/env').loadEnvConfig(__dirname);"
   "const publicEnv = process.env;"
   "module.exports = {"
   "  mode: 'production',"
   "  target: 'webworker',"
   "  entry: './src/workers/main.js',"
   "  experiments: { asyncWebAssembly: true },"
   "  resolve: {"
   "    fallback: { child_process: false, fs: false, module: false, os: false, path: false, worker_threads: false },"
   "    alias: { pg: path.resolve(__dirname, 'src/shims/pg.js') },"
   "  },"
   "  output: {"
   "    filename: 'lang-demos-js-009-log-sharedworker.js',"
   "    path: path.resolve(__dirname, 'public/workers'),"
   "  },"
   "  plugins: [new webpack.DefinePlugin({"
   "    'process.env.NEXT_PUBLIC_SUPABASE_URL': JSON.stringify(publicEnv.NEXT_PUBLIC_SUPABASE_URL || ''),"
   "    'process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY': JSON.stringify(publicEnv.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''),"
   "  })],"
   "};"])

(def +readme+
  {:type :raw
   :file "README.md"
   :main ["# JS-009 Log SharedWorker"
          ""
          "A static Next.js App Router sample with the same shared activity log as JS-007. The database node and Supabase service run inside a URL-backed SharedWorker; each page connects through a proxy client."
          ""
          "## Prepare local Supabase and build"
          ""
          "From the Foundation repository root, start a REPL and run these forms:"
          "(require '[lang-demos.js-009-log-sharedworker.build :as demo])"
          "(demo/setup-scratch-v0)"
          "(demo/build-js-009-log-sharedworker)"
          ""
          "This starts the local-min stack, installs the scratch_v0 schema, and waits for PostgREST."
          ""
          "## Run locally"
          ""
          "From the Foundation repository root, run:"
          "cd .build/demo/js-009-log-sharedworker"
          "cp .env.example .env.local"
          "yarn install"
          "yarn dev"
          ""
          "`yarn dev` serves the page at `http://localhost:3109`. The page reads once on startup and listens for Supabase Realtime updates through the SharedWorker, so it does not re-read the log when you switch tabs or windows."
          ""
          "The dev command bundles the SharedWorker before starting Next.js. Set NEXT_PUBLIC_SUPABASE_URL and NEXT_PUBLIC_SUPABASE_ANON_KEY in .env.local to override the local-min defaults. yarn build creates the worker bundle and exports the static site to out/."
          ""
          "The worker owns the Supabase service, database kernel, and Realtime subscription. Log reads and log_append_public calls travel through the page's worker proxy. Anyone who can reach the app can append messages."]})

(def.make JS-009-LOG-SHAREDWORKER
  {:tag "lang-demos.js-009-log-sharedworker"
   :build ".build/demo/js-009-log-sharedworker"
   :triggers '#{"js" "lang-demos.js-009-log-sharedworker"}
   :sections
   {:setup [+makefile+
            +package+
            js007/+gitignore+
            js007/+yarn-config+
            js007/+next-config+
            js007/+js-config+
            js007/+pg-browser-shim+
            {:type :raw
             :file "webpack.worker.config.js"
             :main +worker-webpack-config+}
            {:type :css
             :target "src/app"
             :file "globals.css"
             :main js007/+styles+}
            js007/+env-example+
            +readme+]}
   :default
   [{:type :raw
     :target "src/workers"
     :file "main.js"
     :main (fn []
             (l/with:cache-none
               [(l/emit-script
                 '(do (lang-demos.js-009-log-sharedworker.app.sharedworker/runtime-init))
                 {:lang :js
                  :layout :full})]))}
    {:type :module.directory
     :lang :js
     :search ["src-build/lang_demos/js_009_log_sharedworker"]
     :main 'lang-demos.js-009-log-sharedworker
     :target "src"
     :emit {:code {:link {:path-suffix ".js"}
                   :transforms {:full [#'js007/inject-next-header
                                       #'js007/inject-next-use-client]}}}}]})

(defn setup-scratch-v0
  []
  (js007/setup-scratch-v0))

(defn module-import-relative
  [^java.nio.file.Path base-path
   ^java.nio.file.Path source-dir
   ^String import-path]
  (when (str/starts-with? import-path "./")
    (let [^String target-rel (subs import-path 2)
          ^java.nio.file.Path target (.resolve base-path target-rel)]
      (when (.exists ^java.io.File (.toFile target))
        (let [relative (str (.relativize source-dir target))]
          (if (str/starts-with? relative ".")
            relative
            (str "./" relative)))))))

(defn fix-module-directory-imports!
  "Makes module.directory's root-relative imports relative to each file."
  [directory]
  (let [^java.io.File base (io/file directory)
        ^java.nio.file.Path base-path (.toPath base)]
    (doseq [^java.io.File file (file-seq base)
            :when (and (.isFile file)
                       (str/ends-with? (.getName file) ".js"))]
      (let [^java.nio.file.Path source-path (.toPath file)
            ^java.nio.file.Path source-dir (.getParent source-path)
            original (slurp file)
            fixed (str/replace
                   original
                   #"from '(\./[^']+)'"
                   (fn [[_ import-path]]
                     (str "from '"
                          (or (module-import-relative base-path
                                                      source-dir
                                                      import-path)
                              import-path)
                          "'")))]
        (when (not= original fixed)
          (spit file fixed))))))

(defn build-js-009-log-sharedworker
  []
  (require 'lang-demos.js-009-log-sharedworker.generated.main)
  (require 'lang-demos.js-009-log-sharedworker.app.page)
  (require 'lang-demos.js-009-log-sharedworker.app.layout)
  (require 'lang-demos.js-009-log-sharedworker.app.sharedworker)
  (let [result (make/build-all JS-009-LOG-SHAREDWORKER)]
    (fix-module-directory-imports! ".build/demo/js-009-log-sharedworker/src")
    result))
