(ns xtalk.packages
  "Source-owned JavaScript and Dart package generation for top-level xt segments."
  (:require [clojure.string :as str]
            [std.make :as make :refer [def.make]]
            [hara.lang.compile]
            [hara.runtime.basic.impl.process-dart :as dart-runtime]))

(def VERSION "0.1.0")
(def SEGMENTS [:lang :event :substrate :net :db :ui])

(def DEPENDENCIES
  {:lang []
   :event [:lang]
   :net [:lang]
   :substrate [:lang :event :net]
   :db [:lang :event :substrate :net]
   :ui [:lang :event :substrate]})

(def PLATFORM-DIRECTORIES
  {:js {:net ['js.net]
        :ui ['js.ui]}
   :dart {:net ['dart.net]
          :ui ['dart.ui]}})

(def SINGLE-MODULES
  {:js {:ui ['js.react 'js.lib.figma]}
   :dart {}})

(defn normalize-dart-module
  "adds Dart SDK imports required by emitted raw symbols"
  [source _static]
  (dart-runtime/ensure-dart-imports source))

(declare root-prefix)

(defn module-code-options
  [platform segment options]
  (cond-> (assoc options
                 :link {:path-suffix (if (= platform :dart) ".dart" ".js")
                        :root-prefix (root-prefix platform segment)})
    (= platform :dart) (assoc :transforms {:full [normalize-dart-module]})))

(defn package-name
  [platform segment]
  (case platform
    :js (str "@xtalk/" (name segment))
    :dart (str "xtalk_" (name segment))))

(defn root-prefix
  "Maps every cross-segment import to its package and keeps platform adapters local."
  [platform segment]
  (let [external
        (into (array-map)
              (map (fn [current]
                     [(symbol (str "xt." (name current)))
                      (if (= platform :js)
                        (str "@xtalk/" (name current))
                        (str "package:xtalk_" (name current)))])
                   SEGMENTS))]
    (cond-> external
      (= platform :js) (assoc 'js (package-name :js segment))
      (= platform :dart) (assoc 'dart (package-name :dart segment)))))

(defn module-entry
  [platform segment main]
  {:type :module.directory
   :lang platform
   :search [(str "src-lang/" (str/replace (str main) "." "/"))]
   :main main
   :target (if (= platform :dart)
             (str (name segment) "/lib")
             (name segment))
   :emit {:code (module-code-options platform segment {:extra-namespaces false})
          :lang/format (if (= platform :js) :commonjs :full)}})

(defn single-entry
  [platform segment main]
  (let [parts (str/split (str main) #"\.")
        adapter? (contains? #{"xt" "js" "dart"} (first parts))
        relative (if adapter? (rest parts) parts)
        file (str (last relative) (if (= platform :dart) ".dart" ".js"))
        subdir (butlast relative)]
    {:type :module.single
     :lang platform
     :main main
     :target (str (name segment)
                  (when (= platform :dart) "/lib")
                  (when (seq subdir) (str "/" (str/join "/" subdir))))
     :file file
     :emit {:code (module-code-options platform segment {})
            :lang/format (if (= platform :js) :commonjs :full)}}))

(defn module-entries
  [platform]
  (mapcat
   (fn [segment]
     (let [xt-main (symbol (str "xt." (name segment)))
           directories (cons xt-main
                             (get-in PLATFORM-DIRECTORIES [platform segment]))
           singles (concat (when (= segment :substrate) [xt-main])
                           (get-in SINGLE-MODULES [platform segment]))]
       (concat
        (map (partial module-entry platform segment) directories)
        (map (partial single-entry platform segment) singles))))
   SEGMENTS))

(defn js-dependencies
  [segment]
  (into {}
        (map (fn [dependency]
               [(package-name :js dependency) VERSION])
             (get DEPENDENCIES segment))))

(defn js-package
  [segment]
  (cond->
   {"name" (package-name :js segment)
    "version" VERSION
    "description" (str "Generated XTalk " (name segment) " runtime")
    "license" "MIT"
    "files" ["*.js" "**/*.js"]
    "exports" {"./*" "./*.js"}
    "dependencies" (js-dependencies segment)}
    (= segment :net)
    (assoc "optionalDependencies"
           {"@sqlite.org/sqlite-wasm" "^3.46.1-build1"
            "pg" "^8.13.1"
            "redis" "^4.7.0"})
    (= segment :ui)
    (assoc "peerDependencies"
           {"@xtalk/figma-ui" "^0.1.3"
            "react" ">=18"
            "react-dom" ">=18"}
           "dependencies"
           (assoc (js-dependencies segment)
                  "react-nil" "^2.0.0"
                  "sonner" "^2.0.0"))))

(defn dart-dependency-lines
  [segment]
  (concat
   (map (fn [dependency]
          (str "  " (package-name :dart dependency) ": " VERSION))
        (get DEPENDENCIES segment))
   (when (= segment :ui)
     ["  fluttersdk_wind: ^1.2.0"])))

(defn dart-pubspec
  [segment]
  (vec
   (concat
    [(str "name: " (package-name :dart segment))
     (str "version: " VERSION)
     (str "description: Generated XTalk " (name segment) " runtime")
     "publish_to: none"
     "resolution: workspace"
     "environment:"
     "  sdk: '>=3.6.0 <4.0.0'"
     "dependencies:"]
    (if (seq (dart-dependency-lines segment))
      (dart-dependency-lines segment)
      ["  meta: ^1.15.0"]))))

(defn generated-readme
  [platform segment]
  [(str "# " (package-name platform segment))
   ""
   "Generated from src-lang; do not edit emitted runtime files directly."
   ""
   (str "Family version: " VERSION ".")])

(defn manifest-sections
  [platform]
  (vec
   (concat
    (when (= platform :js)
      [{:type :package.json
        :target ""
        :main {"name" "@xtalk/workspace"
               "private" true
               "workspaces" ["*"]}}])
    (when (= platform :dart)
      [{:type :gitignore
        :target ""
        :main [".dart_tool/" "**/.dart_tool/"]}
       {:type :raw
        :target ""
        :file "pubspec.yaml"
        :main (vec
               (concat ["name: xtalk_packages"
                        "publish_to: none"
                        "environment:"
                        "  sdk: '>=3.6.0 <4.0.0'"
                        "workspace:"]
                       (map #(str "  - " (name %)) SEGMENTS)))}])
    (mapcat
     (fn [segment]
       [(if (= platform :js)
          {:type :package.json
           :target (name segment)
           :main (js-package segment)}
          {:type :raw
           :target (name segment)
           :file "pubspec.yaml"
           :main (dart-pubspec segment)})
        {:type :readme.md
         :target (name segment)
         :main (generated-readme platform segment)}])
     SEGMENTS))))

(defn js-project
  "constructs the generated JavaScript package workspace project"
  [build]
  {:tag "xtalk-packages-js"
   :build build
   :sections {:setup (manifest-sections :js)}
   :default (vec (module-entries :js))})

(defn dart-project
  "constructs a Dart package workspace with optional additional members"
  ([build]
   (dart-project build []))
  ([build extra-workspace-members]
   (let [sections (mapv (fn [section]
                          (if (and (= "pubspec.yaml" (:file section))
                                   (= "" (:target section)))
                            (update section :main into
                                    (map #(str "  - " %) extra-workspace-members))
                            section))
                        (manifest-sections :dart))]
     {:tag "xtalk-packages-dart"
      :build build
      :sections {:setup sections}
      :default (vec (module-entries :dart))})))

(def +js-project+
  (js-project "packages-gen/js"))

(def +dart-project+
  (dart-project "packages-gen/dart"))

(def.make XTALK-JS +js-project+)
(def.make XTALK-DART +dart-project+)

(defn generate!
  []
  (make/build-all XTALK-JS)
  (make/build-all XTALK-DART))

(defn -main
  [& _args]
  (generate!))
