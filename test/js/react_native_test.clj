(ns js.react-native-test
  (:use code.test)
  (:require [std.lang :as  l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :play/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:host "test.statstrade.io"}}
   :require [[js.core :as j]
             [js.react :as r]
             [js.react-native :as n :include [:fn]]
             [xt.lang.base-lib :as k]]
   :export [MODULE]})

(defn.js nest-tree
  [obj prefix]
  (return (k/walk obj
                  k/identity
                  (fn [x]
                    (when  (k/obj? x)
                      (var out {})
                      (k/for:object [[k v] x]
                        (k/set-key out (+ prefix k) v))
                      (return out))
                    (return x)))))

(def.js TREEDATA
  {:a {:aa {:aaa "1"
            :aab "2"
            :aac "3"
            :aad "4"}
       :ab {:aba "1"
            :abb "2"
            :abc "3"
            :abd "4"}
       :ac {:aca "1"
            :acb "2"
            :acc "3"
            :acd "4"}
       :ad {:ada "1"
            :adb "2"
            :adc "3"
            :add "4"}}
   :b {:ba {:baa "1"
            :bab "2"
            :bac "3"
            :bad "4"}
       :bb {:bba "1"
            :bbb "2"
            :bbc "3"
            :bbd "4"}
       :bc {:bca "1"
            :bcb "2"
            :bcc "3"
            :bcd "4"}
       :bd {:bda "1"
            :bdb "2"
            :bdc "3"
            :bdd "4"}}
   :c {:ca {:caa "1"
            :cab "2"
            :cac "3"
            :cad "4"}
       :cb {:cba "1"
            :cbb "2"
            :cbc "3"
            :cbd "4"}
       :cc {:cca "1"
            :ccb "2"
            :ccc "3"
            :ccd "4"}
       :cd {:cda "1"
            :cdb "2"
            :cdc "3"
            :cdd "4"}}
   :d {:da {:daa "1"
            :dab "2"
            :dac "3"
            :dad "4"}
       :db {:dba "1"
            :dbb "2"
            :dbc "3"
            :dbd "4"}
       :dc {:dca "1"
            :dcb "2"
            :dcc "3"
            :dcd "4"}
       :dd {:dda "1"
            :ddb "2"
            :ddc "3"
            :ddd "4"}}})

^{:refer js.react-native/isWeb :added "4.0"}
(fact "checks that env is web")

^{:refer js.react-native/isTablet :added "4.0"}
(fact "checks that env is tablet")

^{:refer js.react-native/format-obj :added "4.0"}
(fact "formats an object")

^{:refer js.react-native/format-entry :added "4.0"}
(fact "formats an entry")

^{:refer js.react-native/measure :added "4.0"}
(fact "measures the element")

^{:refer js.react-native/measureRef :added "4.0"}
(fact "measures the element in a ref")

^{:refer js.react-native/Enclosed :added "0.1"}
(fact "creates a enclosed section with label"
  ^:hidden
  
  (defn.js EnclosedDemo
    []
    (return
     (n/EnclosedCode 
      {:label "js.react-native/Enclosed"} 
      [:% n/Row
       [:% n/Text "HELLO"]]))))

^{:refer js.react-native/EnclosedCodeContainer :added "4.0"}
(fact "creates a enclosed section with label"
  ^:hidden
  
  (defn.js EnclosedCodeContainerDemo
    []
    (return
     [:% n/EnclosedCodeContainer
      {:label "js.react-native/EnclosedCodeContainer"
       :code "HELLO THERE"}
      [:% n/Row
       [:% n/Text "HELLO"]]])))

^{:refer js.react-native/EnclosedCode :added "4.0"}
(fact "creates an enclosed code section"
  ^:hidden
  
  (defn.js EnclosedCodeDemo
    []
    (return
     (n/EnclosedCode
      {:label "js.react-native/EnclosedCode"
       :code "HELLO THERE"}
      [:% n/Row
       [:% n/Text "HELLO"]]))))

^{:refer js.react-native/Row :added "0.1"}
(fact "constructs a row"
  ^:hidden
  
  (defn.js RowDemo
    []
    (return
     (n/EnclosedCode 
      {:label "js.react-native/Row"} 
      [:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/Text "HELLO"]
       [:% n/Text "WORLD"]] 
      [:% n/Row
       {:style {:backgroundColor "blue"}}
       [:% n/Text "HELLO"]
       [:% n/Text "WORLD"]]))))

^{:refer js.react-native/Fill :added "0.1"}
(fact "fills space"
  ^:hidden
  
  (defn.js FillDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/Fill"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/Text "HELLO"]
       [:% n/Fill {:style {:backgroundColor "red"}}]
       [:% n/Text "WORLD"]]))))

^{:refer js.react-native/H1 :added "0.1"}
(fact "creates an H1 element"
  ^:hidden
  
  (defn.js H1Demo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/H1"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/H1 {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/H2 :added "0.1"}
(fact "creates an H2 element"
  ^:hidden
  
  (defn.js H2Demo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/H2"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/H2 {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/H3 :added "0.1"}
(fact "creates an H3 element"
  ^:hidden
  
  (defn.js H3Demo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/H3"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/H3 {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/H4 :added "0.1"}
(fact "creates an H4 element"
  ^:hidden
  
  (defn.js H4Demo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/H4"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/H4 {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/H5 :added "0.1"}
(fact "creates an H5 element"
  ^:hidden
  
  (defn.js H5Demo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/H5"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/H5 {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/Caption :added "0.1"}
(fact "creates an Caption element"
  ^:hidden
  
  (defn.js CaptionDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/Caption"} 
[:% n/Row
       {:style {:backgroundColor "orange"}}
       [:% n/Caption {:text "HELLO WORLD"}]]))))

^{:refer js.react-native/useTree :added "4.0"}
(fact "a generic tree function")

^{:refer js.react-native/TabsIndexed :added "0.1"}
(fact "creates an enum tabs view"
  ^:hidden
  
  (defn.js TabsIndexedDemo
    []
    (var [index setIndex] (r/local 3))
    (var styleNormal   {:padding 2})
    (var styleSelected {:backgroundColor "#666"
                        :borderRadius 3
                        :color "white"
                        :padding 2})
    (return
     (n/EnclosedCode 
{:label "js.react-native/TabsIndexed"} 
[:% n/TabsIndexed
       {:items ["A" "B" "C" "D"]
        :checkIndex (fn:> true)
        :setIndex   setIndex
        :styleNormal   styleNormal
        :styleSelected styleSelected
        :index index
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{index})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/Tabs :added "0.1"}
(fact "creates an enum data view"
  ^:hidden
  
  (defn.js TabsDemo
    []
    (var [value setValue] (r/local "A"))
    (return
     (n/EnclosedCode 
{:label "js.react-native/Tabs"} 
[:% n/Tabs
       {:data ["A" "B" "C" "D"]
        :value value
        :setValue setValue
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{value})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/TabsPane :added "0.1"}
(fact "creates a  Tabs Pane"
  ^:hidden
  
  (defn.js TabsPaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     (n/EnclosedCode 
{:label "js.react-native/TabsPane"} 
[:% n/TabsPane
       {:tree  {:a "1"
                :b "2"
                :c "3"
                :d "4"}
        :initial initial
        :setInitial setInitial
        :tabsFormat (fn:> [s] (+ " " (j/toUpperCase s) " "))
        :formatFn k/identity}] 
[:% n/Caption
       {:text (k/json-encode #{initial})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/ListIndexed :added "0.1"}
(fact "creates a list view"
  ^:hidden
  
  (defn.js ListIndexedDemo
    []
    (var [index setIndex] (r/local 3))
    (return
     (n/EnclosedCode 
{:label "js.react-native/ListIndexed"} 
[:% n/ListIndexed
       {:items ["A" "B" "C" "D"]
        :checkIndex (fn:> true)
        :setIndex   setIndex
        :index index
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{index})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/List :added "0.1"}
(fact "creates a list"
  ^:hidden
  
  (defn.js ListDemo
    []
    (var [value setValue] (r/local "A"))
    (return
     (n/EnclosedCode 
{:label "js.react-native/List"} 
[:% n/List
       {:data ["A" "B" "C" "D"]
        :value value
        :setValue setValue
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{value})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/ListPane :added "0.1"}
(fact "creates a  List Pane"
  ^:hidden
  
  (defn.js ListPaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     (n/EnclosedCode 
{:label "js.react-native/ListPane"} 
[:% n/ListPane
       {:tree  {:a "1"
                :b "2"
                :c "3"
                :d "4"}
        :initial initial
        :setInitial setInitial
        :listWidth 30
        :listFormat j/toUpperCase
        :formatFn k/identity}] 
[:% n/Caption
       {:text (k/json-encode #{initial})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/TabsMultiIndexed :added "4.0"}
(fact "creates a multi tab select indexed"
  ^:hidden
  
  (defn.js TabsMultiIndexedDemo
    []
    (var [indices setIndices] (r/local [true false false true]))
    (var styleNormal   {:padding 2})
    (var styleSelected {:backgroundColor "#666"
                        :borderRadius 3
                        :color "white"
                        :padding 2})
    (return
     (n/EnclosedCode 
{:label "js.react-native/TabsMultiIndexed"} 
[:% n/TabsMultiIndexed
       {:items ["A" "B" "C" "D"]
        :setIndices   setIndices
        :styleNormal   styleNormal
        :styleSelected styleSelected
        :indices indices
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{indices})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/TabsMulti :added "4.0"}
(fact "creates a multi tab select"
  
  (defn.js TabsMultiDemo
    []
    (var [values setValues] (r/local ["A" "C"]))
    (return
     (n/EnclosedCode 
{:label "js.react-native/TabsMulti"} 
[:% n/TabsMulti
       {:data ["A" "B" "C" "D"]
        :values values
        :setValues setValues
        :format (fn:> [s] (+ " " s " "))}] 
[:% n/Caption
       {:text (k/json-encode #{values})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/TreePane :added "0.1"}
(fact "creates a  Tree Pane"
  ^:hidden
  
  (defn.js TreePaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (var [l1 setL1] (r/local))
    (var [l2 setL2] (r/local))
    (var [l3 setL3] (r/local))
    (return
     (n/EnclosedCode 
{:label "js.react-native/TreePane"} 
[:% n/TreePane
       {:tree  {:x (-/nest-tree -/TREEDATA "x")
                :y (-/nest-tree -/TREEDATA "y")
                :z (-/nest-tree -/TREEDATA "z")
                :w (-/nest-tree -/TREEDATA "w")}
        :levels [{:type "list"
                  :initial initial
                  :setInitial setInitial
                  :listWidth 30
                  :listFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "tabs"
                  :initial l1
                  :setInitial setL1
                  :listWidth 30
                  :tabsFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "list"
                  :listWidth 30
                  :initial l2
                  :setInitial setL2
                  :listFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "tabs"
                  :listWidth 30
                  :initial l3
                  :setInitial setL3
                  :tabsFormat j/toUpperCase
                  :formatFn k/json-encode}]}] 
[:% n/Caption
       {:text (k/json-encode #{initial l1 l2 l3})
        :style {:marginTop 10}}]))))

^{:refer js.react-native/displayTarget :added "4.0"}
(fact "helper function for target display")

^{:refer js.react-native/BaseIndicator :added "4.0"}
(fact "displays a base indicator"
  ^:hidden
  
  (defn.js BaseIndicatorDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/BaseIndicator"} 
[:% n/BaseIndicator
       {:color "black"
        :label "TEST"
        :content "Hello World"}]))))

^{:refer js.react-native/ToggleIndicator :added "4.0"}
(fact "displays a toggle indicator"
  ^:hidden
  
  (defn.js ToggleIndicatorDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native/ToggleIndicator"} 
[:% n/ToggleIndicator
       {:active active
        :onPress (fn:> (setActive (not active)))
        :label "TEST"}]))))

^{:refer js.react-native/RecordList :added "4.0"}
(fact "displays a record list"
  ^:hidden
  
  (defn.js RecordListDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native/RecordList"} 
[:% n/RecordList
       {:entry {:first "John"
                :last  "Smith"}
        :columns [["First Name"  "first"]
                  ["Last Name"   "last"]]}]))))

^{:refer js.react-native/TextDisplay :added "4.0"}
(fact "displays a slab of text"
  ^:hidden

  (defn.js TextDisplayDemo
    []
    (var [active setActive] (r/local true))
    (return
     (n/EnclosedCode 
{:label "js.react-native/TextDisplay"} 
[:% n/TextDisplay
       {:content "ABC"}]))))

^{:refer js.react-native/defaultGlobal :added "4.0"}
(fact "constructs the default global data")

^{:refer js.react-native/GlobalProvider :added "4.0"}
(fact "constructs the context for global data")

^{:refer js.react-native/PortalProvider :added "4.0"}
(fact "constructs an isolated context for portals and gateways to appear")

^{:refer js.react-native/PortalSinkImpl :added "4.0"}
(fact "no context sharing version of `PortalSink`")

^{:refer js.react-native/PortalSink :added "4.0"}
(fact "constructs the gateway where items will appear")

^{:refer js.react-native/Isolation :added "4.0"}
(fact "provides an isolated single scoped gateway")

^{:refer js.react-native/PortalImpl :added "4.0"}
(fact "no context sharing version of `Portal`")

^{:refer js.react-native/Portal :added "4.0"}
(fact "constructs a portal"
  ^:hidden

  (defn.js PortalView
    []
    (var [code setCode] (r/local (j/randomId 5)))
    (return
     [:% n/View
      {:style {:marginTop 10}}
      [:% n/Row
       [:% n/Button
        {:title "CHANGE"
         :onPress (fn:> (setCode (j/randomId 5)))}]]
      [:% n/Row
       [:% n/Portal
        {}
        [:% n/Text
         {:style {:color "green"}}
         (+ "HELLO - " code)]]
       [:% n/Portal
        {:target "world"}
        [:% n/Text
         {:style {:color "white"}}
         (+ "HELLO - " code)]]]]))
  
  (defn.js PortalDemo
    []
    (return
     (n/EnclosedCode 
{:label "js.react-native/Portal"} 
[:% n/PortalProvider
       [:% n/Row
        [:% n/View
         {:style {:backgroundColor "red"
                  :height 50
                  :width 200}}
         [:% n/PortalSink
          {}]]
        [:% n/Padding {:style {:flex 1}}]
        [:% n/View
         {:style {:backgroundColor "blue"
                  :color "white"
                  :height 50
                  :width 200}}
         [:% n/PortalSink
          {:name "world"}]]]
       [:% -/PortalView]
       [:% n/Portal
        {:target "world"}
        [:% n/Text
         {:style {:color "yellow"}}
         (+ "WORLD")]]
       [:% n/Portal
        {}
        [:% n/Text
         {:style {:color "yellow"}}
         (+ "DEFAULT")]]]))))

^{:refer js.react-native/usePortalLayouts :added "4.0"}
(fact "gets measurements of elements in a portal"
  ^:hidden
  
  (defn.js UsePortalLayoutsView
    [#{offset setOffset
       layouts setLayouts}]
    (var hostRef  (r/ref))
    (var #{sinkRef
           setSinkRef
           contentRef
           getLayouts}  (n/usePortalLayouts hostRef
                                            setLayouts))
    (r/watch [offset]
      (getLayouts))
    
    (return
     [:% n/View
      {:style {:marginTop 10}}
      [:% n/View
       {:ref hostRef}]
      [:% n/Row
       [:% n/Portal
        {:onSink setSinkRef}
        [:% n/View
         {:ref contentRef}
         [:% n/Text
          {:style {:color "green"}}
          (n/format-entry #{offset})]]]]]))
  
  (defn.js UsePortalLayoutsDemo
    []
    (var [offset setOffset] (r/local (j/floor
                                      (* 100 (j/random 100)))))
    
    (var [layouts setLayouts] (r/local {}))
    (return
     (n/EnclosedCode 
{:label "js.react-native/usePortalLayouts"} 
[:% n/PortalProvider
       [:% n/Row
        [:% n/Row
         {:style {:backgroundColor "red"
                  :height 300
                  :width 300}}
         [:% n/View
          {:style {:position "absolute"
                   :backgroundColor "blue"
                   :height 200
                   :width 200
                   :transform [{:translateX offset}
                               {:translateY offset}]}}
          [:% n/PortalSink
           [:% n/Button
            {:title "CHANGE"
             :onPress (fn:>
                        (setOffset (j/floor (* 100 (j/random)))))}]
           [:% -/UsePortalLayoutsView
            #{offset setOffset
              layouts setLayouts}]]]]
        [:% n/TextDisplay
         {:content (n/format-entry #{layouts offset})}]]])))
  
  (def.js MODULE
    (do (:# (!:uuid))
        (!:module))))
