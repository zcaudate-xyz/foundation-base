(ns js.react-native.overflow-scenario-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.react :as r :include [:fn]]
             [js.react-native :as n :include [:fn]]
             [xt.lang.spec-base :as xt]]})

^{:refer js.react/useGetCount :adopt true :added "4.0" :unchecked true}
(fact "find minimum demo count that overflows"
  (defn.js Demo01 []
    (var [v s] (r/local 0))
    (return (n/EnclosedCode {:label "d01"}
                            [:% n/Row [:% n/Text "a"]])))
  (defn.js Demo02 []
    (var [v s] (r/local 0))
    (return (n/EnclosedCode {:label "d02"}
                            [:% n/Row [:% n/Text "a"]]))))
