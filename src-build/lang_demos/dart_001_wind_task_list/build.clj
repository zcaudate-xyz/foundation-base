(ns lang-demos.dart-001-wind-task-list.build
  "Builds and runs the generated Flutter/Wind task-list demonstration."
  (:use [code.test :exclude [-main]])
  (:require [lang-demos.dart-001-wind-task-list.app]
            [std.fs :as fs]
            [std.lib.env :as env]
            [std.lib.os :as os]
            [std.make :as make :refer [def.make]]
            [lang.runtime.basic.impl.process-dart :as dart-runtime]))

(def +build-root+ ".build/demo-wind-task-list")
(def +app-root+ (str +build-root+ "/wind_demo"))

(def +pubspec+
  ["name: wind_demo"
   "description: xt.ui portable task list rendered by fluttersdk_wind"
   "publish_to: none"
   "version: 0.1.0+1"
   "environment:"
   "  sdk: '>=3.6.0 <4.0.0'"
   "dependencies:"
   "  flutter:"
   "    sdk: flutter"
   "  fluttersdk_wind: ^1.2.0"
   "dev_dependencies:"
   "  flutter_test:"
   "    sdk: flutter"
   "  flutter_lints: ^6.0.0"
   "flutter:"
   "  uses-material-design: true"])

(def +main-dart+
  ["import 'package:flutter/material.dart';"
   "import 'package:fluttersdk_wind/fluttersdk_wind.dart';"
   "import 'package:wind_demo/xt_ui/core.dart' as ui;"
   "import 'package:wind_demo/xt_ui/state/core.dart' as state_ui;"
   "import 'package:wind_demo/xt_ui/wind.dart' as wind_ui;"
   ""
   "import 'app.dart' as app;"
   ""
   "void main() {"
   "  runApp(const WindTaskListApp());"
   "}"
   ""
   "class WindTaskListApp extends StatelessWidget {"
   "  const WindTaskListApp({super.key});"
   ""
   "  @override"
   "  Widget build(BuildContext context) {"
   "    return WindTheme("
   "      data: WindThemeData(),"
   "      builder: (context, controller) => MaterialApp("
   "        debugShowCheckedModeBanner: false,"
   "        title: 'xt.ui Wind Task List',"
   "        theme: controller.toThemeData(),"
   "        home: const WindTaskListScreen(),"
   "      ),"
   "    );"
   "  }"
   "}"
   ""
   "class WindTaskListScreen extends StatefulWidget {"
   "  const WindTaskListScreen({super.key});"
   ""
   "  @override"
   "  State<WindTaskListScreen> createState() => _WindTaskListScreenState();"
   "}"
   ""
   "class _WindTaskListScreenState extends State<WindTaskListScreen> {"
   "  late final dynamic controller;"
   ""
   "  @override"
   "  void initState() {"
   "    super.initState();"
   "    controller = app.make_controller();"
   "    state_ui.subscribef(controller, 'flutter', (dynamic _, dynamic __) {"
   "      if (mounted) setState(() {});"
   "    });"
   "    state_ui.openf(controller);"
   "  }"
   ""
   "  @override"
   "  void dispose() {"
   "    state_ui.unsubscribef(controller, 'flutter');"
   "    state_ui.closef(controller);"
   "    super.dispose();"
   "  }"
   ""
   "  @override"
   "  Widget build(BuildContext context) {"
   "    final actions = state_ui.actions_create("
   "      controller,"
   "      <dynamic>['set_draft', 'add_item', 'remove_item'],"
   "    );"
   "    final tree = app.view(state_ui.snapshot(controller), actions);"
   "    final runtime = ui.runtime_create("
   "      null,"
   "      wind_ui.flutter_registry(),"
   "      <dynamic, dynamic>{},"
   "      <dynamic, dynamic>{},"
   "      <dynamic, dynamic>{},"
   "    );"
   "    final dynamic bundle = wind_ui.prepare(runtime, tree);"
   ""
   "    return Scaffold("
   "      body: SafeArea("
   "        child: WDynamic("
   "          json: Map<String, dynamic>.from(bundle['json'] as Map),"
   "          actions: Map<String, Function>.from(bundle['actions'] as Map),"
   "          onError: (type, error) => Center("
   "            child: Text('Unable to render $type: $error'),"
   "          ),"
   "          onUnknownWidget: (type, props) => Center("
   "            child: Text('Unsupported Wind widget: $type'),"
   "          ),"
   "        ),"
   "      ),"
   "    );"
   "  }"
   "}"])

(def +widget-test+
  ["import 'package:flutter/material.dart';"
   "import 'package:flutter_test/flutter_test.dart';"
   ""
   "import 'package:wind_demo/main.dart';"
   ""
   "void main() {"
   "  testWidgets('adds and removes a task through WDynamic actions', (tester) async {"
   "    await tester.pumpWidget(const WindTaskListApp());"
   "    await tester.pumpAndSettle();"
   ""
   "    expect(find.text('Learn xt.ui'), findsOneWidget);"
   "    expect(find.text('Run the Wind demo'), findsOneWidget);"
   ""
   "    await tester.enterText(find.byType(EditableText), 'Ship the demo');"
   "    await tester.pumpAndSettle();"
   "    await tester.tap(find.text('Add'));"
   "    await tester.pumpAndSettle();"
   ""
   "    expect(find.text('Ship the demo'), findsOneWidget);"
   ""
   "    await tester.tap(find.text('Remove').last);"
   "    await tester.pumpAndSettle();"
   ""
   "    expect(find.text('Ship the demo'), findsNothing);"
   "  });"
   "}"])

(defn- normalize-dart-module
  "adds Dart SDK imports required by emitted raw symbols"
  [source _static]
  (dart-runtime/ensure-dart-imports source))

(def +dart-root-prefix+
  (array-map
   'xt.lang      "package:wind_demo/xt_lang"
   'xt.event     "package:wind_demo/xt_event"
   'xt.net       "package:wind_demo/xt_net"
   'xt.substrate "package:wind_demo/xt_substrate"
   'xt.ui        "package:wind_demo/xt_ui"
   ;; Dart platform adapters retain their nested package layout:
   ;; dart.ui.wind -> package:wind_demo/xt_ui/ui/wind.dart.
   'dart         "package:wind_demo/xt_ui"
   :default      "package:wind_demo"))

(def +dart-code+
  {:extra-namespaces false
   :link {:path-separator "/"
          :path-suffix ".dart"
          :root-prefix +dart-root-prefix+}
   :transforms {:full [normalize-dart-module]}})

(def.make APPLICATION
  {:tag "demo-wind-task-list"
   :build +build-root+
   :sections {:setup [{:type :raw
                       :target "wind_demo"
                       :file "pubspec.yaml"
                       :main +pubspec+}
                      {:type :raw
                       :target "wind_demo/lib"
                       :file "main.dart"
                       :main +main-dart+}
                      {:type :raw
                       :target "wind_demo/test"
                       :file "widget_test.dart"
                       :main +widget-test+}]}
   :default [{:type :module.directory
              :lang :dart
              :search ["src-lang/xt/lang"]
              :main 'xt.lang
              :target "wind_demo/lib/xt_lang"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.directory
              :lang :dart
              :search ["src-lang/xt/event"]
              :main 'xt.event
              :target "wind_demo/lib/xt_event"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.directory
              :lang :dart
              :search ["src-lang/xt/net"]
              :main 'xt.net
              :target "wind_demo/lib/xt_net"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.directory
              :lang :dart
              :search ["src-lang/xt/substrate"]
              :main 'xt.substrate
              :target "wind_demo/lib/xt_substrate"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.single
              :lang :dart
              :main 'xt.substrate
              :target "wind_demo/lib/xt_substrate"
              :file "substrate.dart"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.directory
              :lang :dart
              :search ["src-lang/xt/ui"]
              :main 'xt.ui
              :target "wind_demo/lib/xt_ui"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.directory
              :lang :dart
              :search ["src-lang/dart/ui"]
              :main 'dart.ui
              :target "wind_demo/lib/xt_ui"
              :emit {:code +dart-code+
                     :lang/format :full}}

             {:type :module.single
              :lang :dart
              :main 'lang-demos.dart-001-wind-task-list.app
              :target "wind_demo/lib"
              :file "app.dart"
              :emit {:code +dart-code+
                     :lang/format :full}}]})

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

(defn ensure-scaffold!
  []
  (fs/create-directory +build-root+)
  (when (or (not (fs/exists? (str +app-root+ "/linux")))
            (not (fs/exists? (str +app-root+ "/macos")))
            (not (fs/exists? (str +app-root+ "/web"))))
    (run-command! +build-root+
                  ["flutter" "create"
                   "--platforms=linux,macos,web"
                   "--project-name" "wind_demo"
                   "--org" "dev.xtalk"
                   "--no-pub"
                   "wind_demo"])))

(defn build!
  []
  (ensure-scaffold!)
  (make/build-all APPLICATION)
  (run-command! +app-root+ ["flutter" "pub" "get"])
  +app-root+)

(defn test!
  []
  (build!)
  (run-command! +app-root+ ["flutter" "analyze" "--no-fatal-infos"])
  (run-command! +app-root+ ["flutter" "test"])
  (run-command! +app-root+ ["flutter" "build" "web" "--no-wasm-dry-run"])
  (if (env/program-exists? "clang++")
    (run-command! +app-root+ ["flutter" "build" "linux"])
    (println "Skipping Linux build: clang++ is not installed.")))

(defn run-demo!
  [device]
  (when (and (= device "linux")
             (not (env/program-exists? "clang++")))
    (throw (ex-info "The Flutter Linux demo requires clang++"
                    {:device device :program "clang++"})))
  (build!)
  (run-command! +app-root+ ["flutter" "run" "-d" device]))

(defn build-macos!
  []
  (build!)
  (run-command! +app-root+ ["flutter" "build" "macos"]))

(defn -main
  [& [command]]
  (try
    (case (or command "build")
      "build" (build!)
      "build-macos" (build-macos!)
      "test" (test!)
      "run-linux" (run-demo! "linux")
      "run-macos" (run-demo! "macos")
      "run-web" (run-demo! "chrome")
      (throw (ex-info "Unknown Wind demo command"
                      {:command command
                       :supported ["build" "build-macos" "test"
                                   "run-linux" "run-macos" "run-web"]})))
    (finally
      (shutdown-agents))))
