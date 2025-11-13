(ns code.dev.build.build-umd
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]))

(def INDEXJS
  (l/emit-as
   :js
   '[(def fs (require "fs")) ;
     (def webpack (require "webpack"))
     (defn ^{:- [:async]} generateUmdBuildString
       [entryPath moduleName outputPath]
       (var compiler
            (webpack
             {:entry entryPath
              :mode "production"
              :output {:library {:type "umd"
                                 :name moduleName}
                       :filename outputPath}
              :plugins
              [(new webpack.DefinePlugin
                    {"process.browser" true})]
              :target "web"
              
              :module
              {:rules
               [{:test    #"\\.js$"
                 :enforce "pre"
                 :use     [{:loader  "string-replace-loader"
                            :options {:search  "[\u0080-\uFFFF]"
                                      :replace ""
                                      :flags   "g"}}
                           "babel-loader"]}]}}))
       (return
        (new Promise
             (fn [resolve reject]
               (. compiler
                  (run
                    (fn [err stats]
                      (if (or err (. stats (hasErrors)))
                        (do
                          (var errorDetails (:? stats (. stats (toJson) errors) err))
                          (console.log "FAILED" (JSON.stringify errorDetails))
                          (reject (new Error (+ "Webpack compilation failed: " (JSON.stringify errorDetails))))
                          (return nil))
                        (try
                          (console.log "SUCCESS")
                          (resolve true)
                          (catch readError
                              (reject
                               (new Error (+ "Failed to read compiled code from memory: " (. readError message))))))))))))))
     
     (var entryPath (.  (. process argv) [2]))
     (var moduleName (. (. process argv) [3]))
     (var outputPath (. (. process argv) [4]))
     
     (defn ^{:- [async]} main []
       (if(or (not entryPath) (not moduleName))
         (do
           (console.error "Usage: node cli.js <entry_path> <module_name> [output_path]")
           (console.error "Example: node cli.js ./src/index.js MyModule ./dist/bundle.js")
           (process.exit 1)))
       (try
         (do
           (console.log (+ "Starting compilation of: " entryPath " to " outputPath))
           (console.log (+ "Module Name (Global): " moduleName))
           (var success (await (generateUmdBuildString entryPath moduleName outputPath)))
           (if success
             (do
               (console.log (+ "\n✅ Compilation successful! Output written to: " outputPath)))
             (do
               (console.log "\n--- Compiled UMD Code ---\n")
               (console.log compiledCode)
               (console.log "\n--------------------------\n"))))
         (catch error
             (do
               (console.error "\n❌ An error occurred during execution:")
               (console.error (. error message))
               (process.exit 1)))))
     (main)]))


(def.make BUILD_UMD
  {:tag       "code.dev.build-umd"
   :build     ".build/code-dev-build-umd"
   :hooks    {}
   :default  [{:type   :package.json
               :main   {"name" "code.dev.build-umd",
                        "version" "1.0.0",
                        "description" "Server-side utility to download, compile UMD bundles, and encode them as UTF8.",
                        "main" "index.js",
                        "dependencies" {"memory-fs" "^0.3.0",
                                        "webpack" "^5.90.0",
                                        "webpack-cli" "^5.1.4"
                                        "string-replace-loader" "^3.2.0"}}}
              {:type :raw
               :file "index.js"
               :main [INDEXJS]}]})

(defn initialise
  []
  (h/sh {:root ".build/code-dev-build-umd"
         :args ["npm" "install"]}))

(defn generate-umd-install
  [package]
  (h/sh {:root ".build/code-dev-build-umd"
         :args ["npm" "install" package]}))

(defn generate-umd
  [input-file umd-module umd-file]
  (do (h/p (h/sh {:root ".build/code-dev-build-umd"
                  :args ["node" "index.js" (str "./node_modules/" input-file) umd-module umd-file]}))
      (fs/move (str ".build/code-dev-build-umd/dist/" umd-file)
               (str "resources/assets/code.dev/public/js/" umd-file))))

(defn generate
  [{:keys [package
           input
           module
           file]}]
  (do (generate-umd-install package)
      (generate-umd input module file)))

(def +packages+
  {:puck
   {:package "@measured/puck"
    :input    "@measured/puck/dist/index.js"
    :module  "Puck"
    :file    "puck.umd.js"}
   :clojure-mode
   {:package "@nextjournal/clojure-mode"
    :input    "@nextjournal/clojure-mode/dist/nextjournal/clojure_mode.mjs"
    :module  "ClojureMode"
    :file    "clojure_mode.umd.js"}
   :radix-ui
   {:package "@nextjournal/clojure-mode"
    :input    "@nextjournal/clojure-mode/dist/nextjournal/clojure_mode.mjs"
    :module  "ClojureMode"
    :file    "clojure_mode.umd.js"}})

(comment
  (do(make/build-all BUILD_UMD)
     (generate (:clojure-mode
                +packages+)))
  
  (generate (:puck
             +packages+))
  
  
  (generate-umd-install "@measured/puck")
  
  (generate-umd "@measured/puck/dist/index.js"
                "Puck"
                "puck.umd.js")
  )





(comment
  
  (make/build-all BUILD_UMD)
  (h/sh {:root ".build/code-dev-build-umd"
         :args ["npm" "install"]
         :inherit true})
  (h/sh {:root ".build/code-dev-build-umd"
         :args ["npm" "install" "@measured/puck"]})
  
  (h/sh {:root ".build/code-dev-build-umd"
         :args ["node" "index.js" "./node_modules/@measured/puck/dist/index.js" "Puck" "puck.umd.js"]})
  (fs/move ".build/code-dev-build-umd/puck.umd.js"
           "resources/assets/code.dev/public/js/puck.umd.js"))
