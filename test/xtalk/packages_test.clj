(ns xtalk.packages-test
  (:use code.test)
  (:require [std.make :as make]
            [xtalk.packages :refer :all]))

^{:refer xtalk.packages/package-name :added "4.1"}
(fact "uses platform-native names for one package per xt segment"
  [(package-name :js :substrate)
   (package-name :dart :substrate)]
  => ["@xtalk/substrate" "xtalk_substrate"])

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
    [(every? js-mains ['xt.lang 'xt.event 'xt.substrate 'xt.net 'xt.db 'xt.ui
                       'js.net 'js.ui 'js.react.view 'js.react 'js.lib.figma])
     (every? dart-mains ['xt.lang 'xt.event 'xt.substrate 'xt.net 'xt.db 'xt.ui
                         'dart.net 'dart.ui])])
  => [true true])

^{:refer xtalk.packages/js-package :added "4.1"}
(fact "keeps internal JavaScript package versions in lockstep"
  (get-in (js-package :db) ["dependencies" "@xtalk/substrate"])
  => VERSION

  (get (js-package :ui) "version")
  => VERSION

  (get-in (js-package :ui) ["peerDependencies" "@xtalk/figma-ui"])
  => "^0.1.4")

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

^{:refer xtalk.packages/single-entry :added "4.1"}
(fact "emits the js.react.view entry with its public aliases"
  (let [project {:tag "packages-single-test"
                 :build ".build/packages-single-test"
                 :sections {}
                 :default [(single-entry :js :ui 'js.react.view)]}
        _ (make/build-all (make/make-config project))
        content (slurp ".build/packages-single-test/libs/xt-ui/react/view.js")]
    [(boolean (re-find #"react/view/runtime\.js" content))
     (boolean (re-find #"runtime_create" content))
     (boolean (re-find #"native_registry" content))
     (boolean (re-find #"polyfill_registry" content))])
  => [true true true true])

^{:refer xtalk.packages/normalize-dart-module :added "4.1"}
(fact "adds Dart SDK imports required by emitted raw symbols"
  (let [source "encode(value) { return jsonEncode(value); }\nrandom() { return math.Random(); }"
        output (normalize-dart-module source nil)]
    [(boolean (re-find #"import 'dart:convert';" output))
     (boolean (re-find #"import 'dart:math' as math;" output))])
  => [true true])
