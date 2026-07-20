(ns xtalk.packages
  "Source-owned JavaScript and Dart package generation for top-level xt segments."
  (:require [clojure.string :as str]
            [std.make :as make :refer [def.make]]
            [std.json :as json]
            [hara.lang.compile]
            [hara.runtime.basic.impl.process-dart :as dart-runtime]))

(def VERSION "0.1.0")
(def SEGMENTS [:lang :event :substrate :mcp :net :db :ui])

(def DEPENDENCIES
  {:lang []
   :event [:lang]
   :net [:lang]
   :substrate [:lang :event :net]
   :mcp [:lang :substrate]
   :db [:lang :event :substrate :net]
   :ui [:lang :event :substrate]})

(def PLATFORM-DIRECTORIES
  {:js {:net ['js.net]
        :ui ['js.ui 'js.react.view]}
   :dart {:net ['dart.net]
          :ui ['dart.ui]}})

(def SINGLE-MODULES
  {:js {:ui ['js.react 'js.lib.figma 'js.react.view]}
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

(defn package-directory
  "returns the language-neutral workspace directory for an xt segment"
  [segment]
  (str "libs/xt-" (name segment)))

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
  ([platform segment main]
   (module-entry platform segment main "src-lang"))
  ([platform segment main source-root]
   {:type :module.directory
    :lang platform
    :search [(str source-root "/" (str/replace (str main) "." "/"))]
    :main main
    :target (if (= platform :dart)
              (str (package-directory segment) "/lib")
              (package-directory segment))
    :emit {:code (module-code-options platform segment {:extra-namespaces false})
           :lang/format (if (= platform :js) :commonjs :full)}}))

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
     :target (str (package-directory segment)
                  (when (= platform :dart) "/lib")
                  (when (seq subdir) (str "/" (str/join "/" subdir))))
     :file file
     :emit {:code (module-code-options platform segment {})
            :lang/format (if (= platform :js) :commonjs :full)}}))

(defn module-entries
  ([platform]
   (module-entries platform "src-lang"))
  ([platform source-root]
   (mapcat
    (fn [segment]
      (let [xt-main (symbol (str "xt." (name segment)))
            directories (cons xt-main
                              (get-in PLATFORM-DIRECTORIES [platform segment]))
            singles (concat (when (= segment :substrate) [xt-main])
                            (get-in SINGLE-MODULES [platform segment]))]
        (concat
         (map #(module-entry platform segment % source-root) directories)
         (map (partial single-entry platform segment) singles))))
    SEGMENTS)))

(defn js-dependencies
  [segment]
  (into (array-map)
        (map (fn [dependency]
               [(package-name :js dependency) VERSION])
             (sort-by #(package-name :js %)
                      (get DEPENDENCIES segment)))))

(defn json-pretty
  "renders deterministic two-space JSON compatible with JavaScript workspace tooling"
  ([value]
   (str (json-pretty value 0) "\n"))
  ([value level]
   (let [padding #(apply str (repeat (* 2 %) " "))]
     (cond
       (map? value)
       (if (empty? value)
         "{}"
         (str "{\n"
              (str/join
               ",\n"
               (map (fn [[k v]]
                      (str (padding (inc level))
                           (json/write (str k)) ": "
                           (json-pretty v (inc level))))
                    value))
              "\n" (padding level) "}"))

       (sequential? value)
       (if (empty? value)
         "[]"
         (str "[\n"
              (str/join
               ",\n"
               (map #(str (padding (inc level))
                          (json-pretty % (inc level)))
                    value))
              "\n" (padding level) "]"))

       :else
       (json/write value)))))

(defn js-package
  [segment]
  (let [dependencies (js-dependencies segment)]
    (cond->
     (array-map
      "name" (package-name :js segment)
      "version" VERSION
      "description" (str "Generated XTalk " (name segment) " runtime")
      "license" "MIT"
      "files" ["*.js" "**/*.js"]
      "exports" (array-map "./*.js" "./*.js"
                           "./*" "./*.js"))
    (seq dependencies)
    (assoc "dependencies" dependencies)
    (= segment :net)
    (assoc "optionalDependencies"
           (array-map "@sqlite.org/sqlite-wasm" "^3.46.1-build1"
                      "pg" "^8.13.1"
                      "redis" "^4.7.0"))
    (= segment :ui)
    (assoc "peerDependencies"
           (array-map "@xtalk/figma-ui" "^0.1.3"
                      "react" ">=18"
                      "react-dom" ">=18")
           "dependencies"
           (assoc (js-dependencies segment)
                  "react-nil" "^2.0.0"
                  "sonner" "^2.0.0")))))

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
                       (map #(str "  - " (package-directory %)) SEGMENTS)))}])
    (mapcat
     (fn [segment]
       [(if (= platform :js)
          {:type :raw
           :target (package-directory segment)
           :file "package.json"
           :main (fn []
                   (str/split (json-pretty (js-package segment)) #"\n" -1))}
          {:type :raw
           :target (package-directory segment)
           :file "pubspec.yaml"
           :main (dart-pubspec segment)})
        {:type :readme.md
         :target (package-directory segment)
         :main (generated-readme platform segment)}])
     SEGMENTS))))

(defn js-project
  "constructs the generated JavaScript package workspace project"
  ([build]
   (js-project build "src-lang"))
  ([build source-root]
   {:tag "xtalk-packages-js"
    :build build
    :sections {:setup (manifest-sections :js)}
    :default (vec (module-entries :js source-root))}))

(defn dart-project
  "constructs a Dart package workspace with optional additional members"
  ([build]
   (dart-project build []))
  ([build extra-workspace-members]
   (dart-project build extra-workspace-members "src-lang"))
  ([build extra-workspace-members source-root]
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
      :default (vec (module-entries :dart source-root))})))

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

(defn generate-at!
  "generates both package workspaces at caller-owned output directories"
  [js-build dart-build]
  (make/build-all (make/make-config (js-project js-build)))
  (make/build-all (make/make-config (dart-project dart-build))))

(defn -main
  [& args]
  (case (count args)
    0 (generate!)
    2 (apply generate-at! args)
    (throw (ex-info "Expected no arguments or JavaScript and Dart output directories."
                    {:args args}))))
