(ns code.manage.xtalk-test
    (:require [clojure.set :as set]
              [code.manage.xtalk :as xtalk])
    (:use code.test))

(fact "xtalk audit exposes the extended xtalk categories"
  (set/subset? #{:xtalk-common
                 :xtalk-functional
                 :xtalk-language-specific
                 :xtalk-std-lang-link-specific
                 :xtalk-runtime-specific}
               (set (xtalk/xtalk-categories)))
  => true)

(fact "xtalk audit exposes websocket and service symbols"
  (set/subset? #{'x:ws-connect
                 'x:ws-send
                 'x:ws-close
                 'x:notify-socket
                 'x:client-basic
                 'x:client-ws
                 'x:server-basic
                 'x:server-ws}
               (set (xtalk/xtalk-symbols)))
  => true)

(fact "xtalk facade exposes inventory and scaffold functions"
  [(fn? xtalk/xtalk-model-inventory)
   (fn? xtalk/xtalk-language-status)
   (fn? xtalk/generate-xtalk-ops)
   (fn? xtalk/scaffold-xtalk-grammar-tests)
   (fn? xtalk/separate-runtime-tests)
   (fn? xtalk/scaffold-runtime-template)
   (fn? xtalk/export-runtime-suite)
   (fn? xtalk/compile-runtime-bulk)
   (fn? xtalk/support-matrix)
   (fn? xtalk/visualize-support)]
  => [true true true true true true true true true true])

