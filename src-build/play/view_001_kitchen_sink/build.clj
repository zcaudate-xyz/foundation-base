(ns play.view-001-kitchen-sink.build
  "def.make builds for the xt.substrate.view kitchen-sink demo.

   VIEW-KITCHEN-SINK-JS emits the browser app (module.graph -> src/, esbuild
   bundle -> public/app.js, static index.html + Tailwind CDN). VIEW-KITCHEN-SINK-DART
   emits a self-contained Dart console package that prepares the same demo maps
   through dart.ui.view and prints the bundle summary. VIEW-KITCHEN-SINK-FLUTTER
   emits a Flutter package that renders the same demo through fluttersdk_wind's
   WDynamic widget (flutter create scaffold + lib/main.dart host)."
  (:use code.test)
  (:require [clojure.string :as str]
            [std.fs :as fs]
            [std.lib.os :as os]
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
  [package-name]
  (into (array-map)
        (map (fn [[main target]]
               [main (str "package:" package-name "/"
                          (str/replace target "lib/" ""))])
             +dart-directories+)))

(defn dart-code-options
  [package-name]
  {:extra-namespaces false
   :link {:path-suffix ".dart"
          :root-prefix (dart-root-prefix package-name)}
   :transforms {:full [packages/normalize-dart-module]}})

(defn dart-entries
  [package-name]
  (vec
   (concat
    (map (fn [[main target]]
           {:type :module.directory
            :lang :dart
            :search [(str "src-lang/" (str/replace (str main) "." "/"))]
            :main main
            :target target
            :emit {:code (dart-code-options package-name)
                   :lang/format :full}})
         +dart-directories+)
    [{:type :module.single
      :lang :dart
      :main 'xt.substrate
      :target "lib/xt_substrate"
      :file "substrate.dart"
      :emit {:code (dart-code-options package-name)
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
   "import 'package:view_demos/xt_substrate/view-demos.dart' as demos;"
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
   "  final node = substrate.node_create(<dynamic, dynamic>{});"
   "  final spec = view.view_spec('demo', <dynamic, dynamic>{}, null);"
   "  final rt = runtime.runtime_create("
   "      node, spec, demos.kitchen_sink_render, <dynamic, dynamic>{'space_id': 'app'});"
   "  final bundle = runtime.prepare(rt);"
   "  final json = bundle['json'] as Map;"
   "  final actions = bundle['actions'] as Map;"
   "  print('root type      : ${json['type']}');"
   "  print('node count     : ${countNodes(json)}');"
   "  print('action count   : ${actions.keys.length}');"
   "  print('portable valid : ${view.validate_portable(demos.kitchen_sink_spec())}');"
   "  var escapeRejected = false;"
   "  try {"
   "    view.validate_portable(demos.web_escape_spec());"
   "  } catch (_) {"
   "    escapeRejected = true;"
   "  }"
   "  print('escape rejected: $escapeRejected');"
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
   :default (dart-entries "view_demos")})

;;
;; Flutter
;;

(def +flutter-build-root+ ".build/view-kitchen-sink-flutter")

(def +flutter-pubspec+
  ["name: view_kitchen_sink"
   "version: 0.1.0"
   "description: Flutter host rendering the xt.substrate.view kitchen sink through fluttersdk_wind"
   "publish_to: none"
   "environment:"
   "  sdk: '>=3.6.0 <4.0.0'"
   "dependencies:"
   "  flutter:"
   "    sdk: flutter"
   "  fluttersdk_wind: ^1.2.0"
   "dev_dependencies:"
   "  flutter_test:"
   "    sdk: flutter"
   "flutter:"
   "  uses-material-design: true"])

(def +flutter-main-dart+
  ["// Flutter host for the xt.substrate.view kitchen-sink demo."
   "// Prepares the Wind json/actions bundle through dart.ui.view and renders"
   "// it with fluttersdk_wind's WDynamic widget."
   "import 'package:flutter/material.dart';"
   "import 'package:fluttersdk_wind/fluttersdk_wind.dart';"
   ""
   "import 'package:view_kitchen_sink/xt_substrate/substrate.dart' as substrate;"
   "import 'package:view_kitchen_sink/xt_substrate/view.dart' as view;"
   "import 'package:view_kitchen_sink/xt_substrate/view-demos.dart' as demos;"
   "import 'package:view_kitchen_sink/xt_ui/view/runtime.dart' as runtime;"
   ""
   "void main() {"
   "  runApp(const KitchenSinkApp());"
   "}"
   ""
   "class KitchenSinkApp extends StatelessWidget {"
   "  const KitchenSinkApp({super.key});"
   ""
   "  @override"
   "  Widget build(BuildContext context) {"
   "    return WindTheme("
   "      data: WindThemeData(),"
   "      builder: (context, controller) => MaterialApp("
   "        debugShowCheckedModeBanner: false,"
   "        title: 'xt.substrate.view kitchen sink',"
   "        theme: controller.toThemeData(),"
   "        home: const KitchenSinkScreen(),"
   "      ),"
   "    );"
   "  }"
   "}"
   ""
   "class KitchenSinkScreen extends StatefulWidget {"
   "  const KitchenSinkScreen({super.key});"
   ""
   "  @override"
   "  State<KitchenSinkScreen> createState() => _KitchenSinkScreenState();"
   "}"
   ""
   "class _KitchenSinkScreenState extends State<KitchenSinkScreen> {"
   "  late final dynamic rt;"
   ""
   "  @override"
   "  void initState() {"
   "    super.initState();"
   "    final node = substrate.node_create(<dynamic, dynamic>{});"
   "    final spec = view.view_spec('demo', <dynamic, dynamic>{}, null);"
   "    rt = runtime.runtime_create("
   "        node, spec, demos.kitchen_sink_render, <dynamic, dynamic>{'space_id': 'app'});"
   "    rt['invalidate'] = (dynamic snapshot, dynamic revision, dynamic event) {"
   "      if (mounted) setState(() {});"
   "    };"
   "  }"
   ""
   "  @override"
   "  Widget build(BuildContext context) {"
   "    final bundle = runtime.prepare(rt);"
   "    return Scaffold("
   "      body: SafeArea("
   "        child: SingleChildScrollView("
   "          child: WDynamic("
   "            json: Map<String, dynamic>.from(bundle['json'] as Map),"
   "            actions: Map<String, Function>.from(bundle['actions'] as Map),"
   "            onError: (type, error) => Center("
   "              child: Text('Unable to render $type: $error'),"
   "            ),"
   "            onUnknownWidget: (type, props) => Center("
   "              child: Text('Unsupported Wind widget: $type'),"
   "            ),"
   "          ),"
   "        ),"
   "      ),"
   "    );"
   "  }"
   "}"])

(def +flutter-widget-test+
  ["import 'package:flutter_test/flutter_test.dart';"
   "import 'package:fluttersdk_wind/fluttersdk_wind.dart';"
   ""
   "import 'package:view_kitchen_sink/main.dart';"
   ""
   "void main() {"
   "  testWidgets('kitchen sink mounts through WDynamic', (tester) async {"
   "    await tester.pumpWidget(const KitchenSinkApp());"
   "    await tester.pump();"
   ""
   "    expect(find.byType(WDynamic), findsOneWidget);"
   "  });"
   "}"])

(def +flutter-makefile+
  [[:.PHONY {:- ["get" "run-macos" "run-web" "test"]}]
   [:get ["flutter pub get"]]
   [:run-macos ["flutter run -d macos"]]
   [:run-web ["flutter run -d chrome"]]
   [:test ["flutter test"]]])

(def +flutter-gitignore+
  {:type :gitignore
   :main [".dart_tool/"
          "pubspec.lock"
          "build/"
          ".flutter-plugins"
          ".flutter-plugins-dependencies"]})

(def.make VIEW-KITCHEN-SINK-FLUTTER
  {:tag "view-kitchen-sink-flutter"
   :build +flutter-build-root+
   :sections {:setup [+flutter-gitignore+
                      {:type :raw
                       :file "pubspec.yaml"
                       :main +flutter-pubspec+}
                      {:type :raw
                       :target "lib"
                       :file "main.dart"
                       :main +flutter-main-dart+}
                      {:type :raw
                       :target "test"
                       :file "widget_test.dart"
                       :main +flutter-widget-test+}
                      {:type :makefile
                       :main +flutter-makefile+}]}
   :default (dart-entries "view_kitchen_sink")})

(defn- run-command!
  [root args]
  (let [^Process process (os/sh {:root root
                                 :args args
                                 :inherit true
                                 :wait true})
        exit (.exitValue process)]
    (when (not= 0 exit)
      (throw (ex-info "Command failed" {:root root :args args :exit exit})))
    process))

(defn ensure-flutter-scaffold!
  []
  (fs/create-directory +flutter-build-root+)
  (when (or (not (fs/exists? (str +flutter-build-root+ "/macos")))
            (not (fs/exists? (str +flutter-build-root+ "/web"))))
    (run-command! +flutter-build-root+
                  ["flutter" "create"
                   "--platforms=macos,web"
                   "--project-name" "view_kitchen_sink"
                   "--org" "dev.xtalk"
                   "--no-pub"
                   "."])))

(defn flutter-build!
  []
  (ensure-flutter-scaffold!)
  (make/build-all VIEW-KITCHEN-SINK-FLUTTER)
  (run-command! +flutter-build-root+ ["flutter" "pub" "get"])
  +flutter-build-root+)

(defn flutter-test!
  []
  (flutter-build!)
  (run-command! +flutter-build-root+ ["flutter" "analyze" "--no-fatal-infos"])
  (run-command! +flutter-build-root+ ["flutter" "test"]))

(defn flutter-run!
  [device]
  (flutter-build!)
  (run-command! +flutter-build-root+ ["flutter" "run" "-d" device]))

(defn -main
  [& [command]]
  (case (or command "build")
    "build" (do (make/build-all VIEW-KITCHEN-SINK-JS)
                (make/build-all VIEW-KITCHEN-SINK-DART)
                (flutter-build!))
    "test" (flutter-test!)
    "run-macos" (flutter-run! "macos")
    "run-web" (flutter-run! "chrome")
    (throw (ex-info "Unknown kitchen-sink build command"
                    {:command command
                     :supported ["build" "test" "run-macos" "run-web"]}))))

^{:eval false}
(fact "build the kitchen-sink demo for js, dart and flutter"
  (-main "build")

  ;; js: cd .build/view-kitchen-sink-js && make install && make bundle && make start
  ;;     open http://localhost:8080
  ;; dart: cd .build/view-kitchen-sink-dart && make get && make run
  ;; flutter: cd .build/view-kitchen-sink-flutter && make get && make run-macos
  )
