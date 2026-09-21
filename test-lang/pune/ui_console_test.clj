(ns pune.ui-console-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :dev/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [pune.ui-console :as ui-console]]
   :export [MODULE]})

^{:refer pune.ui-console/Console :added "0.1"}
(fact "creates the console"
  ^:hidden
  
  (defn.js ConsoleDemo
    []
    (var [current setCurrent] (r/local true))
    (return
     (n/EnclosedCode 
{:label "pune.ui-console/Console"} 
[:% n/Row
       {:style {:marginTop 30}}
       [:% ui-console/Console
        #{[current setCurrent
           :screens {"a_screen" (fn:> [:% n/Text "A"])
                     "b_screen" (fn:> [:% n/Text "B"])
                     "c_screen" (fn:> [:% n/Text "C"])}]}]])))
  
  (def.js MODULE (!:module)))


