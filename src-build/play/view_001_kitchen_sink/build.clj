(ns play.view-001-kitchen-sink.build
  "def.make builds for the xt.substrate.view kitchen-sink demo.

   VIEW-KITCHEN-SINK-JS emits the browser app (module.graph -> src/, esbuild
   bundle -> public/app.js, static index.html + Tailwind CDN). VIEW-KITCHEN-SINK-DART
   emits a self-contained Dart console package that prepares the same demo maps
   through dart.ui.view and prints the bundle summary."
  (:use code.test)
  (:require [clojure.string :as str]
            [std.make :as make :refer [def.make]]
            [xtalk.packages :as packages]
            [play.view-001-kitchen-sink.app]))

;;
;; JavaScript
;;

(def +js-package+
  {"name" "view-kitchen-sink"
   "version" "0.1.0"
   "private" true
   "type" "module"
   "scripts" {"bundle" "esbuild src/play/view_001_kitchen_sink/app.js --bundle --outfile=public/app.js"
              "start" "python3 -m http.server 8080 --directory public"}
   "dependencies" {"@xtalk/figma-ui" "^0.1.3"
                   "react" "^19.2.5"
                   "react-dom" "^19.2.5"
                   "react-nil" "^2.0.0"
                   "sonner" "^2.0.7"}
   "devDependencies" {"esbuild" "^0.24.0"}})

(def +js-index+
  ["<!doctype html>"
   "<html lang=\"en\">"
   "<head>"
   "  <meta charset=\"utf-8\" />"
   "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
   "  <title>xt.substrate.view kitchen sink</title>"
   "  <script src=\"https://cdn.tailwindcss.com\"></script>"
   "</head>"
   "<body>"
   "  <main id=\"app\"></main>"
   "  <script type=\"module\" src=\"./app.js\"></script>"
   "</body>"
   "</html>"])

(def +js-makefile+
  [[:.PHONY {:- ["install" "bundle" "start"]}]
   [:install ["npm install"]]
   [:bundle ["npm run bundle"]]
   [:start ["npm run start"]]])

(def +js-gitignore+
  {:type :gitignore
   :main ["node_modules/"
          "public/app.js"
          ".DS_Store"]})

(def.make VIEW-KITCHEN-SINK-JS
  {:tag "view-kitchen-sink-js"
   :build ".build/view-kitchen-sink-js"
   :triggers '#{play.view-001-kitchen-sink.app}
   :sections {:setup [+js-gitignore+
                      {:type :package.json
                       :main +js-package+}
                      {:type :makefile
                       :main +js-makefile+}
                      {:type :raw
                       :target "public"
                       :file "index.html"
                       :main +js-index+}]}
   :default [{:type :module.directory
              :lang :js
              :search ["src-build"]
              :main 'play.view-001-kitchen-sink.app
              :target "src"
              :emit {:code {:link {:path-suffix ".js"
                                   :root-prefix "./"}}
                     :lang/format :commonjs}}]})

;;
;; Dart
;;

(def +dart-directories+
  "directory modules and their in-package targets"
  (array-map
   'xt.lang      "lib/xt_lang"
   'xt.event     "lib/xt_event"
   'xt.net       "lib/xt_net"
   'dart.net     "lib/xt_net"
   'xt.substrate "lib/xt_substrate"
   'dart.ui      "lib/xt_ui"))

(defn dart-root-prefix
  "maps cross-namespace imports onto the single view_demos package"
  []
  (into (array-map)
        (map (fn [[main target]]
               [main (str "package:view_demos/"
                          (str/replace target "lib/" ""))])
             +dart-directories+)))

(defn dart-code-options
  []
  {:extra-namespaces false
   :link {:path-suffix ".dart"
          :root-prefix (dart-root-prefix)}
   :transforms {:full [packages/normalize-dart-module]}})

(defn dart-entries
  []
  (vec
   (concat
    (map (fn [[main target]]
           {:type :module.directory
            :lang :dart
            :search [(str "src-lang/" (str/replace (str main) "." "/"))]
            :main main
            :target target
            :emit {:code (dart-code-options)
                   :lang/format :full}})
         +dart-directories+)
    [{:type :module.single
      :lang :dart
      :main 'xt.substrate
      :target "lib/xt_substrate"
      :file "substrate.dart"
      :emit {:code (dart-code-options)
             :lang/format :full}}])))

(def +dart-pubspec+
  ["name: view_demos"
   "version: 0.1.0"
   "description: Kitchen-sink demo for xt.substrate.view prepared through dart.ui.view"
   "publish_to: none"
   "environment:"
   "  sdk: '>=3.6.0 <4.0.0'"])

(def +dart-main+
  ["// Prints the kitchen-sink Wind bundle summary for the xt.substrate.view demo."
   "// Emitted XTalk modules expose raw top-level symbols; see lib/xt_*."
   "import 'dart:convert';"
   ""
   "import 'package:view_demos/xt_substrate/substrate.dart' as substrate;"
   "import 'package:view_demos/xt_substrate/view.dart' as view;"
   "import 'package:view_demos/xt_substrate/view_demos.dart' as demos;"
   "import 'package:view_demos/xt_ui/view/runtime.dart' as runtime;"
   ""
   "int countNodes(dynamic value) {"
   "  if (value is List) {"
   "    var total = 0;"
   "    for (final item in value) {"
   "      total += countNodes(item);"
   "    }"
   "    return total;"
   "  }"
   "  if (value is Map) {"
   "    var total = 1;"
   "    total += countNodes(value['children'] ?? const []);"
   "    return total;"
   "  }"
   "  return 0;"
   "}"
   ""
   "void main() {"
   "  final node = substrate.xt_substrate____node_create(<dynamic, dynamic>{});"
   "  final spec = view.xt_substrate_view____view_spec('demo', <dynamic, dynamic>{}, null);"
   "  final rt = runtime.dart_ui_view_runtime____runtime_create("
   "      node, spec, demos.xt_substrate_view_demos____kitchen_sink_render, <dynamic, dynamic>{'space_id': 'app'});"
   "  final bundle = runtime.dart_ui_view_runtime____prepare(rt);"
   "  final json = bundle['json'] as Map;"
   "  final actions = bundle['actions'] as Map;"
   "  print('root type      : \\${json['type']}');"
   "  print('node count     : \\${countNodes(json)}');"
   "  print('action count   : \\${actions.keys.length}');"
   "  print('portable valid : \\${view.xt_substrate_view____validate_portable(demos.xt_substrate_view_demos____kitchen_sink_spec())}');"
   "  var escapeRejected = false;"
   "  try {"
   "    view.xt_substrate_view____validate_portable(demos.xt_substrate_view_demos____web_escape_spec());"
   "  } catch (_) {"
   "    escapeRejected = true;"
   "  }"
   "  print('escape rejected: \\$escapeRejected');"
   "  if (!escapeRejected) {"
   "    throw StateError('web-escape spec should be rejected by validate-portable');"
   "  }"
   "  print('OK');"
   "}"])

(def +dart-makefile+
  [[:.PHONY {:- ["get" "run"]}]
   [:get ["dart pub get"]]
   [:run ["dart run bin/main.dart"]]])

(def +dart-gitignore+
  {:type :gitignore
   :main [".dart_tool/"
          "pubspec.lock"]})

(def.make VIEW-KITCHEN-SINK-DART
  {:tag "view-kitchen-sink-dart"
   :build ".build/view-kitchen-sink-dart"
   :sections {:setup [+dart-gitignore+
                      {:type :raw
                       :file "pubspec.yaml"
                       :main +dart-pubspec+}
                      {:type :raw
                       :target "bin"
                       :file "main.dart"
                       :main +dart-main+}
                      {:type :makefile
                       :main +dart-makefile+}]}
   :default (dart-entries)})

(defn -main
  []
  (make/build-all VIEW-KITCHEN-SINK-JS)
  (make/build-all VIEW-KITCHEN-SINK-DART))

^{:eval false}
(fact "build the kitchen-sink demo for js and dart"
  (make/build-all VIEW-KITCHEN-SINK-JS)
  (make/build-all VIEW-KITCHEN-SINK-DART)

  ;; js: cd .build/view-kitchen-sink-js && make install && make bundle && make start
  ;;     open http://localhost:8080
  ;; dart: cd .build/view-kitchen-sink-dart && make get && make run
  )
