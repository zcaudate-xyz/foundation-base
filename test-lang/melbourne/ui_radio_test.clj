(ns melbourne.ui-radio-test
  (:use code.test)
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[js.react :as r]
             [js.react-native :as n :include [:fn]]
             [melbourne.ui-radio :as ui-radio]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

^{:refer melbourne.ui-radio/RadioBox :added "0.1"}
(fact "creates a horizontal radio"
  ^:hidden
  
  (defn.js RadioBoxDemo
    []
    (var [s0 setS0] (r/local true))
    (var [s1 setS1] (r/local true))
    (var [s2 setS2] (r/local true))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-radio/RadioBox"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioBox
         #{[:design {:type "light"
                    }
            :style {:margin 2}
            :selected s0
            :onPress (fn:> (setS0 (not s0)))]}]
        [:% ui-radio/RadioBox
         #{[:design {:type "light"
                    :mode "secondary"}
            :style {:margin 2}
            :selected s1
            :onPress (fn:> (setS1 (not s1)))]}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioBox
         #{[:design {:type "dark"
                    }
            :style {:margin 2}
            :selected s0
            :onPress (fn:> (setS0 (not s0)))]}]
        [:% ui-radio/RadioBox
         #{[:design {:type "dark"
                    :mode "secondary"}
            :style {:margin 2}
            :selected s1
            :onPress (fn:> (setS1 (not s1)))]}]]]))))

^{:refer melbourne.ui-radio/RadioGroupIndexed :added "0.1"}
(fact "creates a group of radio boxes"
  ^:hidden

  (defn.js RadioGroupIndexedDemo
    []
    (var [index setIndex] (r/local 1))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-radio/RadioGroupIndexed"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioGroupIndexed
         {:design {:type "light"
                  }
          :items [" STATS " " XLM " " USD "]
          :index index
          :setIndex setIndex}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioGroupIndexed
         {:design {:type "dark"
                  }
          :items [" STATS " " XLM " " USD "]
          :index index
          :setIndex setIndex}]]] 
[:% n/TextDisplay
       {:content (xt/x:json-encode index)}]))))

^{:refer melbourne.ui-radio/RadioGroup :added "0.1"}
(fact "creates a group of radio boxes"
  ^:hidden

  (defn.js RadioGroupDemo
    []
    (var [value setValue] (r/local "USD"))
    (return
     (n/EnclosedCode 
{:label " melbourne.ui-radio/RadioGroup"} 
[:% n/Row
       [:% n/Row
        {:style {:backgroundColor "#eee"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioGroup
         {:design {:type "light"
                  }
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue
          :format (fn:> [s] (+ "  " s))}]]
       [:% n/Row
        {:style {:backgroundColor "#333"
                 :flex 1
                 :padding 30}}
        [:% ui-radio/RadioGroup
         {:design {:type "dark"
                  }
          :data ["STATS" "XLM" "USD"]
          :value value
          :setValue setValue
          :format (fn:> [s] (+ "  " s))}]]] 
[:% n/TextDisplay
       {:content (xt/x:json-encode value)}])))

  (def.js MODULE (!:module))
  )
