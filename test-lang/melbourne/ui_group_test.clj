(ns melbourne.ui-group-test
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
             [melbourne.ui-group :as ui-group]]
   :export [MODULE]})

^{:refer melbourne.ui-group/EnumMultiIndexed :added "0.1"}
(fact "creates a multi-select horizontal tab bar"
  ^:hidden

  (defn.js EnumMultiIndexedDemo
    []
    (var [indices setIndices] (r/local [false true false]))
    (return
     (n/EnclosedCode 
{:label "ui-group/EnumMultiIndexed"} 
[:% ui-group/EnumMultiIndexed
        {:design {:type "light"
                 }
         :items [" STATS " " XLM " " USD "]
         :indices indices
        :setIndices setIndices}]))))

^{:refer melbourne.ui-group/EnumMulti :added "0.1"}
(fact  "creates a multi-select horizontal tab bar"
  ^:hidden

  (defn.js EnumMultiDemo
    []
    (var [values setValues] (r/local ["USD"]))
    (return
     (n/EnclosedCode 
{:label "ui-group/EnumMulti"} 
[:% ui-group/EnumMulti
       {:design {:type "light"}
        :data ["STATS" "XLM" "USD"]
        :values values
        :setValues setValues}]))))

^{:refer melbourne.ui-group/TabsIndexed :added "0.1"}
(fact "creates a indexed horizontal tab bar"
  ^:hidden
  
  (defn.js TabsIndexedDemo
    []
    (var [index0 setIndex0] (r/local 1))
    (var [index1 setIndex1] (r/local 2))
    (var [index2 setIndex2] (r/local 0))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-group/TabsIndexed"} 
[:% n/View
       {:style {:backgroundColor "#eee"
                :flex 1
                :padding 20}}
       [:% ui-group/TabsIndexed
        {:design {:type "light"
                 }
         :items ["A" "B" "C" "D"]
         :index index0
         :setIndex   setIndex0}]
       [:% ui-group/TabsIndexed
        {:design {:type "light"
                 :mode "secondary"}
         :style {:marginHorizontal 10}
         :items ["AA" "BB" "CC" "DD"]
         :index index1
         :setIndex   setIndex1}]
       [:% ui-group/TabsIndexed
        {:design {:type "light"
                 }
         :style {:marginHorizontal 1
                 :paddingVertical 2}
         :items ["123" "456" "789" "abc"]
         :index      index2
         :setIndex   setIndex2}]] 
[:% n/Row
       {:style {:backgroundColor "#333"
                :flex 1
                :padding 10
                :paddingBottom 30}}
       [:% n/View
        {:style {:width 50
                 :marginRight 50}}
        [:% ui-group/TabsIndexed
         {:design {:type "dark"
                  }
          :items ["A" "B" "C" "D"]
          :index index0
          :setIndex   setIndex0
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}}]]
       [:% n/View
        {:style {:width 100
                 :marginRight 50}}
        [:% ui-group/TabsIndexed
         {:design {:type "dark"
                  :mode "secondary"}
          :items ["AA" "BB" "CC" "DD"]
          :index index1
          :setIndex   setIndex1
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}}]]
       [:% n/View
        {:style {:width 100
                 :marginRight 50}}
        [:% ui-group/TabsIndexed
         {:design {:type "dark"
                  }
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}
          :items ["123" "456" "789" "abc"]
          :index      index2
          :setIndex   setIndex2}]]]))))

^{:refer melbourne.ui-group/Tabs :added "0.1"}
(fact "creates a horizontal tab bar"
  ^:hidden
  
  (defn.js TabsDemo
    []
    (var [value0 setValue0] (r/local "A"))
    (var [value1 setValue1] (r/local "CC"))
    (var [value2 setValue2] (r/local "789"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-group/Tabs"} 
[:% n/View
       {:style {:backgroundColor "#333"
                :flex 1
                :padding 20}}
       [:% ui-group/Tabs
        {:design {:type "dark"
                 }
         :data ["A" "B" "C" "D"]
         :value value0
         :setValue   setValue0}]
       [:% ui-group/Tabs
        {:design {:type "dark"
                 :mode "secondary"}
         :style {:marginHorizontal 5
                 :paddingVertical 2}
         :data ["AA" "BB" "CC" "DD"]
         :value value1
         :setValue   setValue1}]
       [:% ui-group/Tabs
        {:design {:type "dark"
                 }
         :style {:marginHorizontal 1
                 :paddingVertical 2}
         :data ["123" "456" "789" "abc"]
         :value      value2
         :setValue   setValue2}]] 
[:% n/Row
       {:style {:backgroundColor "#eee"
                :flex 1
                :padding 10
                :paddingBottom 30}}
       [:% n/View
        {:style {:width 50
                 :marginRight 50}}
        [:% ui-group/Tabs
         {:design {:type "light"
                  }
          :data ["A" "B" "C" "D"]
          :value value0
          :setValue   setValue0
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}}]]
       [:% n/View
        {:style {:width 100
                 :marginRight 50}}
        [:% ui-group/Tabs
         {:design {:type "light"
                  :mode "secondary"}
          :data ["AA" "BB" "CC" "DD"]
          :value value1
          :setValue   setValue1
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}}]]
       [:% n/View
        {:style {:width 100
                 :marginRight 50}}
        [:% ui-group/Tabs
         {:design {:type "light"
                  }
          :style {:marginHorizontal 1
                  :paddingVertical 5
                  :marginVertical 5}
          :styleContainer {:flexDirection "column"}
          :data ["123" "456" "789" "abc"]
          :value      value2
          :setValue   setValue2}]]]))))

^{:refer melbourne.ui-group/ListIndexed :added "0.1"}
(fact "creates a indexed list"
  ^:hidden
  
  (defn.js ListIndexedDemo
    []
    (var [index0 setIndex0] (r/local 1))
    (var [index1 setIndex1] (r/local 2))
    (var [index2 setIndex2] (r/local 0))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-group/ListIndexed"} 
[:% n/Row
       {:style {:backgroundColor "#eee"
                :flex 1
                :padding 20}}
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/ListIndexed
         {:design {:type "light"
                  }
          :items ["A" "B" "C" "D"]
          :index index0
          :setIndex   setIndex0}]]
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/ListIndexed
         {:design {:type "light"
                  :mode "secondary"}
          :style {:marginHorizontal 10}
          :items ["AA" "BB" "CC" "DD"]
          :index index1
          :setIndex   setIndex1}]]
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/ListIndexed
         {:design {:type "light"
                  }
          :style {:marginHorizontal 1
                  :paddingVertical 2}
          :items ["123" "456" "789" "abc"]
          :index      index2
          :setIndex   setIndex2}]]]))))

^{:refer melbourne.ui-group/List :added "0.1"}
(fact "creates a list"
  ^:hidden
  
  (defn.js ListDemo
    []
    (var [value0 setValue0] (r/local "A"))
    (var [value1 setValue1] (r/local "CC"))
    (var [value2 setValue2] (r/local "789"))
    (return
     (n/EnclosedCode 
{:label "melbourne.ui-group/List"} 
[:% n/Row
       {:style {:backgroundColor "#333"
                :flex 1
                :padding 20}}
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/List
         {:design {:type "dark"
                  }
          :data ["A" "B" "C" "D"]
          :value value0
          :setValue   setValue0}]]
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/List
         {:design {:type "dark"
                  :mode "secondary"}
          :style {:marginHorizontal 5
                  :paddingVertical 2}
          :data ["AA" "BB" "CC" "DD"]
          :value value1
          :setValue   setValue1}]]
       [:% n/View
        {:style {:width 100}}
        [:% ui-group/List
         {:design {:type "dark"
                  }
          :style {:marginHorizontal 1
                  :paddingVertical 2}
          :data ["123" "456" "789" "abc"]
          :value      value2
          :setValue   setValue2}]]])))
  
  (def.js MODULE (!:module))
  )
