(ns lang-demos.ts-001-single-source-user-directory.build
  (:use [code.test :exclude [-main]])
  (:require [clojure.string :as str]
            [std.make :as make :refer [def.make]]
             [lang.model.builtin.spec-js.ts :as ts]
             [lang.typed.xtalk-analysis :as xtalk-analysis]
             [lang-demos.ts-001-single-source-user-directory.main :as main]))

(def +gitignore+
  ["node_modules" "dist"])

(def +makefile+
  [[:.PHONY {:- ["build" "typecheck" "run" "show-types" "show-js" "show-app"]}]
   [:build
    ["npm run build"]]
   [:typecheck
    ["npm run typecheck"]]
   [:run
    ["npm start"]]
   [:show-types
    ["@sed -n '1,200p' src/index.d.ts"]]
   [:show-js
    ["@sed -n '1,200p' src/index.js"]]
   [:show-app
    ["@sed -n '1,260p' src/app.tsx"]]])

(def +package+
  {"name" "ts-001-single-source-user-directory"
   "private" true
   "type" "module"
   "main" "src/index.js"
   "types" "src/index.d.ts"
   "exports" {"." {"types" "./src/index.d.ts"
                   "import" "./src/index.js"}}
   "scripts" {"build" "tsc -p tsconfig.json"
              "typecheck" "tsc --noEmit -p tsconfig.json"
              "start" "node dist/app.js"}
   "dependencies" {"blessed" "0.1.81"
                  "raf" "3.4.1"
                  "react" "17.0.1"
                  "react-blessed" "0.7.2"}
   "devDependencies" {"@types/blessed" "^0.1.25"
                      "@types/node" "^22.10.2"
                      "@types/react" "^17.0.80"
                      "typescript" "^5.8.3"}})

(def +tsconfig+
  {"compilerOptions" {"target" "ES2022"
                      "module" "NodeNext"
                      "moduleResolution" "NodeNext"
                      "jsx" "react"
                      "strict" true
                      "noEmitOnError" true
                      "esModuleInterop" true
                      "allowSyntheticDefaultImports" true
                      "allowJs" true
                      "checkJs" false
                      "rootDir" "src"
                      "outDir" "dist"
                      "skipLibCheck" true
                      "forceConsistentCasingInFileNames" true}
   "include" ["src/**/*.ts" "src/**/*.tsx" "src/**/*.js" "src/**/*.d.ts"]})

(def +expected-files+
  [".gitignore"
   "Makefile"
   "package.json"
   "tsconfig.json"
   "src/app.tsx"
   "src/react-blessed.d.ts"
   "src/index.js"
   "src/index.d.ts"])

(def +main-file+
  "src-build/lang_demos/ts_001_single_source_user_directory/main.clj")

(def +app-file+
  "src-build/lang_demos/ts_001_single_source_user_directory/app.tsx")

(def +react-types-file+
  "src-build/lang_demos/ts_001_single_source_user_directory/react-blessed.d.ts")

(defn app-source
  [_]
  (slurp +app-file+))

(defn react-types-source
  [_]
  (slurp +react-types-file+))

(defn typescript-artifact
  [{:keys [runtime-output]}]
  (let [analysis (xtalk-analysis/analyze-file +main-file+)
        declarations (ts/emit-analysis-declarations analysis)
        ;; The JS backend exports callable values; pair each function type alias with its value declaration.
        function-values (->> (:functions analysis)
                             (map (fn [{:keys [name]}]
                                    (let [ident (ts/export-ts-ident name)]
                                      (str "export declare const " ident ": " ident ";"))))
                             (str/join "\n\n"))]
    {:output (ts/declaration-output-path runtime-output)
     :body (str declarations "\n\n" function-values)}))

(def.make TS-001-SINGLE-SOURCE-USER-DIRECTORY
  {:build    ".build/demo/ts-001-single-source-user-directory"
   :github   {:repo "zcaudate/lang-demos.ts-001-single-source-user-directory"
              :description "XTalk user directory with a TypeScript React Blessed TUI"}
   :orgfile  "Main.org"
   :triggers '#{lang-demos.ts-001-single-source-user-directory.main}
   :sections {:setup [{:type :gitignore
                       :main +gitignore+}
                      {:type :makefile
                       :main +makefile+}
                      {:type :package.json
                       :main +package+}
                      {:type :json
                       :file "tsconfig.json"
                       :main +tsconfig+}
                      {:type :custom
                       :file "src/app.tsx"
                       :fn #'app-source}
                      {:type :custom
                       :file "src/react-blessed.d.ts"
                       :fn #'react-types-source}]}
   :default [{:type :module.single
              :lang :js
              :main 'lang-demos.ts-001-single-source-user-directory.main
              :file "index.js"
              :target "src"
              :emit {:artifacts [#'typescript-artifact]}}]})

(defn -main
  []
  (make/build-all TS-001-SINGLE-SOURCE-USER-DIRECTORY))

^{:eval false}
(fact "build the example into .build/demo/ts-001-single-source-user-directory"
  (make/build-all TS-001-SINGLE-SOURCE-USER-DIRECTORY))

^{:eval false}
(fact "show generated declaration output"
  (make/run TS-001-SINGLE-SOURCE-USER-DIRECTORY :show-types))

^{:eval false}
(fact "show generated javascript output"
  (make/run TS-001-SINGLE-SOURCE-USER-DIRECTORY :show-js))

^{:eval false}
(fact "show TypeScript app source"
  (make/run TS-001-SINGLE-SOURCE-USER-DIRECTORY :show-app))

^{:eval false}
(fact "type-check and run the TypeScript app"
  (make/run TS-001-SINGLE-SOURCE-USER-DIRECTORY :typecheck)
  (make/run TS-001-SINGLE-SOURCE-USER-DIRECTORY :run))
