(ns code.dev.build.build-umd
  (:require [std.make :as make :refer [def.make]]
            [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]
            [code.heal :as heal]
            [std.text.diff :as diff]))

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
    :file    "clojure-mode.umd.js"}
   :react-live
   {:package "react-live"
    :input    "react-live/dist/index.js"
    :module  "ReactLive"
    :file    "react-live.umd.js"}
   :radix-base
   {:package "radix-ui"
    :input    "radix-ui/dist/index.js"
    :module  "RadixBase"
    :file    "radix-base.umd.js"}
   :radix-main
   {:package "@radix-ui/themes"
    :input    "@radix-ui/themes/dist/esm/index.js"
    :module  "RadixMain"
    :file    "radix-main.umd.js"}
   :recharts
   {:package "recharts"
    :input    "recharts/es6/index.js"
    :module  "Recharts"
    :file    "recharts.umd.js"}
   :lexical
   {:package "lexical"
    :input    "lexical/Lexical.prod.mjs"
    :module  "Lexical"
    :file    "lexical.umd.js"}})

(def INDEXJS
  (l/emit-as
   :js
   '[(def fs (require "fs"))
     (def webpack (require "webpack"))
     (defn ^{:- [:async]} generateUmdBuildString
       [entryPath moduleName outputPath]
       (var compiler
            (webpack
             {:entry entryPath
              :mode "production"
              :output {:library {:type "umd"
                                 :name moduleName}
                       :filename outputPath}}))
       (return
        (new Promise
             (fn [resolve reject]
               (. compiler
                  (run
                    (fn [err stats]
                      (if (or err (. stats (hasErrors)))
                        (do
                          (var errorDetails (:? stats (. stats (toJson) errors) err))
                          (reject (new Error (+ "Webpack compilation failed:" (JSON.stringify errorDetails)))))
                        (try
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
                                        "webpack-cli" "^5.1.4"}}}
              {:type :raw
               :file "index.js"
               :main [INDEXJS]}]})

(defn initialise
  []
  (h/p (h/sh {:root ".build/code-dev-build-umd"
              :args ["npm" "install"]})))

(defn generate-umd-install
  [package]
  (h/p (h/sh {:root ".build/code-dev-build-umd"
              :args ["npm" "install" package]})))

(defn generate-umd
  [input-file umd-module umd-file]
  (do (h/p (h/sh {:root ".build/code-dev-build-umd"
                  :args ["node" "index.js" (str "./node_modules/" input-file) umd-module umd-file]}))
      (fs/move (str ".build/code-dev-build-umd/dist/" umd-file)
               (str "resources/assets/code.dev/public/js/" umd-file))))

(defn generate-entry
  [{:keys [package
           input
           module
           file]}]
  (do (generate-umd-install package)
      (generate-umd input module file)))

(defn list-packages
  []
  (keys +packages+))

(defn build-package
  [package-key]
  (generate-entry
   (get +packages+ package-key)))





(comment

  (build-package :puck)
  (build-package :lexical)
  (build-package :clojure-mode)
  (build-package :react-live)
  (build-package :radix-base)
  (build-package :radix-main)
  
    )


(comment


  (filter identity 
          (for [f  (keys
                    (fs/list "/Users/chris/Development/greenways/Szncampaigncenter/src-dsl"
                             {:include [".clj"]}))]
            (let [_       (h/p f)
                  input   (slurp f)
                  output  (heal/heal input)
                  layout  (try (std.block/parse-root output)
                               nil
                               (catch Throwable t
                                 f #_[t :FAILED]))
                  ]
              (h/p (diff/->string (diff/diff input output)))
              layout)))
  
  (std.block/parse-root
   (heal/heal
    (slurp 
     "/Users/chris/Development/greenways/Szncampaigncenter/src-dsl/task-manager-view.clj")))
  
  
  
  )





(comment

  (generate-entry (:recharts
                   +packages+))
  
  (generate-entry (:puck
                   +packages+))
  (generate-entry (:radix-base
                   +packages+))
  
  (generate-entry (:radix-main
                   +packages+))
  
  
  (generate-umd-install "@measured/puck")
  
  (generate-umd "@measured/puck/dist/index.js"
                "Puck"
                "puck.umd.js")

  
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
