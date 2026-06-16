(ns js.react-native.enclosed-debug-test
  (:use code.test)
  (:require [hara.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react-native :as n :include [:fn]]]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react-native/Enclosed :added "0.1"}
(fact "creates a enclosed section with label"

  (defn.js EnclosedDemo
    []
    (return
     (n/EnclosedCode
      {:label "js.react-native/Enclosed"}
      [:% n/Row
       [:% n/Text "HELLO"]]))))
