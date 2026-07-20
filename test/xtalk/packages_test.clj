(ns xtalk.packages-test
  (:use code.test)
  (:require [std.make :as make]
            [xtalk.packages :refer :all]))

^{:refer xtalk.packages/package-name :added "4.1"}
(fact "uses platform-native names for one package per xt segment"
  [(package-name :js :substrate)
   (package-name :dart :substrate)]
  => ["@xtalk/substrate" "xtalk_substrate"])

^{:refer xtalk.packages/package-directory :added "4.1"}
(fact "uses stable xt-prefixed directories across language workspaces"
  (mapv package-directory SEGMENTS)
  => ["libs/xt-lang" "libs/xt-event" "libs/xt-substrate"
      "libs/xt-mcp" "libs/xt-net" "libs/xt-db" "libs/xt-ui"])

^{:refer xtalk.packages/root-prefix :added "4.1"}
(fact "links portable segments and merged adapters through package prefixes"
  (let [js-ui (root-prefix :js :ui)
        dart-net (root-prefix :dart :net)]
    [(get js-ui 'xt.lang)
     (get js-ui 'js)
     (get dart-net 'xt.lang)
     (get dart-net 'dart)])
  => ["@xtalk/lang" "@xtalk/ui" "package:xtalk_lang" "xtalk_net"])

^{:refer xtalk.packages/module-entries :added "4.1"}
(fact "merges js.net/dart.net and js.ui/dart.ui without merging package ownership"
  (let [js-mains (set (map :main (module-entries :js)))
        dart-mains (set (map :main (module-entries :dart)))]
    [(every? js-mains ['xt.lang 'xt.event 'xt.substrate 'xt.mcp 'xt.net 'xt.db 'xt.ui
                       'js.net 'js.ui 'js.react 'js.lib.figma])
     (every? dart-mains ['xt.lang 'xt.event 'xt.substrate 'xt.mcp 'xt.net 'xt.db 'xt.ui
                         'dart.net 'dart.ui])
     (set (map :target (module-entries :js)))
     (set (map :target (module-entries :dart)))])
  => [true true
      #{"libs/xt-lang" "libs/xt-event" "libs/xt-substrate" "libs/xt-net"
        "libs/xt-mcp" "libs/xt-db" "libs/xt-ui" "libs/xt-ui/lib"}
      #{"libs/xt-lang/lib" "libs/xt-event/lib" "libs/xt-substrate/lib"
        "libs/xt-mcp/lib" "libs/xt-net/lib" "libs/xt-db/lib" "libs/xt-ui/lib"}])

^{:refer xtalk.packages/js-package :added "4.1"}
(fact "keeps internal JavaScript package versions in lockstep"
  (get-in (js-package :db) ["dependencies" "@xtalk/substrate"])
  => VERSION

  (get-in (js-package :mcp) ["dependencies" "@xtalk/substrate"])
  => VERSION

  (get-in (js-package :db) ["exports" "./*.js"])
  => "./*.js"

  (get (js-package :ui) "version")
  => VERSION

  (contains? (js-package :lang) "dependencies")
  => false)

(fact "renders JavaScript manifests in deterministic workspace format"
  (json-pretty (array-map "name" "@xtalk/lang"
                          "files" ["*.js" "**/*.js"]))
  => (str "{\n"
          "  \"name\": \"@xtalk/lang\",\n"
          "  \"files\": [\n"
          "    \"*.js\",\n"
          "    \"**/*.js\"\n"
          "  ]\n"
          "}\n"))

^{:refer xtalk.packages/dart-project :added "4.1"}
(fact "adds application members without changing package workspace entries"
  (let [project (dart-project ".build/example" ["wind_demo"])
        pubspec (->> project :sections :setup
                     (filter #(= "pubspec.yaml" (:file %)))
                     first
                     :main)]
    [(:build project)
     (last pubspec)
     (count (:default project))])
  => [".build/example" "  - wind_demo" (count (module-entries :dart))])

(fact "package projects accept an external source root"
  [(-> (js-project ".build/js" "../foundation/src-lang")
       :default first :search first)
   (-> (dart-project ".build/dart" [] "../foundation/src-lang")
       :default first :search first)]
  => ["../foundation/src-lang/xt/lang"
      "../foundation/src-lang/xt/lang"])

(fact "caller-owned output directories retain the aggregate workspace projects"
  (let [js (make/make-config (js-project "/tmp/statstrade/js"))
        dart (make/make-config (dart-project "/tmp/statstrade/dart"))]
    [(-> js :instance deref :build)
     (-> dart :instance deref :build)])
  => ["/tmp/statstrade/js" "/tmp/statstrade/dart"])

(fact "generated manifests address the same xt-prefixed package directories"
  (let [js (manifest-sections :js)
        dart (manifest-sections :dart)
        dart-root (->> dart
                       (filter #(and (= "" (:target %))
                                     (= "pubspec.yaml" (:file %))))
                       first
                       :main)]
    [(->> js (map :target) (filter seq) set)
     (last dart-root)])
  => [#{"libs/xt-lang" "libs/xt-event" "libs/xt-substrate"
        "libs/xt-mcp" "libs/xt-net" "libs/xt-db" "libs/xt-ui"}
      "  - libs/xt-ui"])

^{:refer xtalk.packages/normalize-dart-module :added "4.1"}
(fact "adds Dart SDK imports required by emitted raw symbols"
  (let [source "encode(value) { return jsonEncode(value); }\nrandom() { return math.Random(); }"
        output (normalize-dart-module source nil)]
    [(boolean (re-find #"import 'dart:convert';" output))
     (boolean (re-find #"import 'dart:math' as math;" output))])
  => [true true])
