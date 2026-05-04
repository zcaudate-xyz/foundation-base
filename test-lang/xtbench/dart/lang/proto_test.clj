(ns xtbench.dart.lang.proto-test
  (:use code.test)
  (:require [hara.runtime.basic.type-common :as common]
            [hara.lang :as l]))

(l/script- :dart
  {:runtime :twostep
   :require [[xt.lang.spec-base :as xt]]})

(def CANARY-DART
  (common/program-exists? "dart"))

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:lang-exceptions {:js {:skip true}
                    :python {:skip true}
                    :lua {:skip true}}}
(fact "supports protocol primitives in Dart"
  (if CANARY-DART
    [(!.dt
       (var obj {})
       (var proto (xt/proto:create {"label" "proto"}))
       (xt/proto:set obj proto)
       (var attached (xt/proto:get obj))
       (return (xt/x:get-key attached "label")))
      (!.dt
        (var proto (xt/proto:create
                    {"describe" (fn [curr suffix]
                                  (return (xt/x:cat (. curr ["name"]) suffix)))}))
        (var obj {"name" "alpha"})
        (xt/proto:set obj proto)
        (return [((xt/proto:method obj "describe") obj "!")
                 (xt/x:nil? (xt/proto:method obj "missing"))]))]
    :dart-unavailable)
  => (any ["proto" ["alpha!" true]]
          :dart-unavailable))
