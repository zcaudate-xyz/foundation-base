(ns xtalk.packages-test
  (:use code.test)
  (:require [xtalk.packages :refer :all]))

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
                       'js.net 'js.ui 'js.react 'js.lib.figma])
     (every? dart-mains ['xt.lang 'xt.event 'xt.substrate 'xt.net 'xt.db 'xt.ui
                         'dart.net 'dart.ui])])
  => [true true])

^{:refer xtalk.packages/js-package :added "4.1"}
(fact "keeps internal JavaScript package versions in lockstep"
  (get-in (js-package :db) ["dependencies" "@xtalk/substrate"])
  => VERSION

  (get (js-package :ui) "version")
  => VERSION)
