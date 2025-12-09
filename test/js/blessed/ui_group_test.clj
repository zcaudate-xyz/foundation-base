(ns js.blessed.ui-group-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :basic
   :config   {:emit {:lang/jsx false}}
   :require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.ui-group :as ui-group]
              [js.blessed.ui-core :as ui-core]
              [js.blessed :as b :include [:fn]]
              [js.lib.chalk :as chk]
              [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

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

^{:refer js.blessed.ui-group/useTree :added "4.0" :unchecked true}
(fact "wrapper for `js.react/useTree`")

^{:refer js.blessed.ui-group/layoutTabs :added "4.0" :unchecked true}
(fact "layout tabs for horizontal and vertical")

^{:refer js.blessed.ui-group/EnumMultiIndexed :added "4.0" :unchecked true}
(fact "Constructs EnumMultiIndexed"
  ^:hidden
  
  (defn.js EnumMultiIndexedDemo
    []
    (var [indices setIndices] (r/local [false true false]))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/EnumMultiIndexed"
       :width 35}
      [:% ui-group/EnumMultiIndexed
       {:top 2
        :indices indices
        :setIndices setIndices
        :color "yellow"
        :field "currency_id"
        :items [" STATS " " XLM " " USD "]}]
      [:box {:top 1 :shrink true
             :content (+ "" (k/json-encode indices))}]])))

^{:refer js.blessed.ui-group/EnumMulti :added "4.0" :unchecked true}
(fact  "Constructs EnumMultiIndexed"
  ^:hidden
  
  (defn.js EnumMultiDemo
    []
    (var [values setValues] (r/local ["USD"]))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/EnumMulti"
       :width 35}
      [:% ui-group/EnumMulti
       {:top 2
        :values values
        :setValues setValues
        :color "yellow"
        :format (fn:> [x] (+ " " x " "))
        :data ["STATS" "XLM" "USD"]}]
      [:box {:top 1 :shrink true
             :content (+ "" (k/json-encode values))}]])))

^{:refer js.blessed.ui-group/TabsView :added "4.0" :unchecked true}
(fact "Constructs Tabs"
  ^:hidden
  
  (defn.js TabsViewDemo
    []
    (var [index setIndex] (r/local 0))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/TabsView"}
      [:% ui-group/TabsView
       {:top 2
        :height 1
        :index index
        :setIndex setIndex
        :color "yellow"
        :items [" STATS " " XLM " " USD "]}]
      [:box {:left 25 :shrink true
             :content (+ "" index)}]])))

^{:refer js.blessed.ui-group/Tabs :added "4.0" :unchecked true}
(fact "Construct Tabs"
  ^:hidden
  
  (defn.js TabsDemo
    []
    (var [value setValue] (r/local "XLM"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/Tabs"}
      [:% ui-group/Tabs
       {:top 2
        :height 1
        :value value
        :setValue setValue
        :width 20
        :format (fn:> [v] (+ " " v " "))
        :color "green"
        :data ["STATS" "XLM" "USD"]}]
      [:box {:left 25 :shrink true
             :content (+ "" value)}]])))

^{:refer js.blessed.ui-group/TabsVPane :added "4.0" :unchecked true}
(fact "constructs a tabs pane"
  ^:hidden
  
  (defn.js TabsVPaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/TabsVPane"}
      [:% ui-group/TabsVPane
       {:top 2
        :width 20
        :tree  {:a "A1"
                :b "B2"
                :c "C3"
                :d "D4"}
        :color "green"
        :initial initial
        :setInitial setInitial
        :tabsFormat (fn:> [s] (+ " " (j/toUpperCase s) " "))
        :formatFn k/identity}]
      [:box {:left 25 :shrink true
             :content (+ "" initial)}]])))

^{:refer js.blessed.ui-group/TabsHPane :added "4.0" :unchecked true}
(fact "Constructs a List"
  ^:hidden

  (defn.js TabsHPaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/TabsHPane"
       :height 10}
      [:% ui-group/TabsHPane
       {:top 2
        :layout "vertical"
        :tree  {:a "A1"
                :b "B2"
                :c "C3"
                :d "D4"
                :e "E5"
                #_#_#_#_#_#_#_#_#_#_
                :f "F6"
                :g "F7"
                :h "H8"
                :i "I9"
                :j "J10"}
        :width 5
        :color "green"
        :initial initial
        :setInitial setInitial
        :tabsFormat (fn:> [s] (+ " " (j/toUpperCase s) " "))
        :formatFn k/identity}]
      [:box {:left 25 :shrink true
             :content (+ "" initial)}]])))

^{:refer js.blessed.ui-group/ListView :added "4.0" :unchecked true}
(fact "Constructs a ListView"
  ^:hidden
  
  (defn.js ListViewDemo
    []
    (var [index setIndex] (r/local 0))
    (return
     [:box {:width 30
            :content "ui-group/ListView"
            :height 8}
      [:% ui-group/ListView
       {:top 2
        :height 5
        :index index
        :setIndex setIndex
        :width 20
        :color "green"
        :items ["1 - One"
                "2 - Two"
                "3 - Three"
                "4 - Four"]}]
      [:box {:left 25 :shrink true
             :content (+ "" index)}]])))

^{:refer js.blessed.ui-group/List :added "4.0" :unchecked true}
(fact "Constructs a List"
  ^:hidden
  
  (defn.js ListDemo
    []
    (var [value setValue] (r/local "XLM"))
    (return
     [:box {:width 30
            :content "ui-group/List"
            :height 8}
      [:% ui-group/List
       {:top 2
        :height 5
        :value value
        :setValue setValue
        :width 20
        :color "green"
        :data ["STATS" "XLM" "USD"]}]
      [:box {:left 25 :shrink true
             :content (+ "" value)}]])))

^{:refer js.blessed.ui-group/ListPane :added "4.0" :unchecked true}
(fact "Constructs a List"
  ^:hidden

  (defn.js ListPaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/ListPane"
       :height 10}
      [:% ui-group/ListPane
       {:top 2
        :layout "vertical"
        :tree  {:a "A1"
                :b "B2"
                :c "C3"
                :d "D4"
                :e "E5"
                :f "F6"
                :g "F7"
                :h "H8"
                :i "I9"
                :j "J10"}
        :width 10
        :height 5
        :color "green"
        :initial initial
        :setInitial setInitial
        :tabsFormat (fn:> [s] (+ " " (j/toUpperCase s) " "))
        :formatFn k/identity}]
      [:box {:left 25 :shrink true
             :content (+ "" initial)}]])))

^{:refer js.blessed.ui-group/TreePane :added "4.0" :unchecked true}
(fact "constructs a tree pane"
  ^:hidden
  
  (defn.js TreePaneDemo
    []
    (var [initial setInitial] (r/local "a"))
    (var [l1 setL1] (r/local))
    (var [l2 setL2] (r/local))
    (var [l3 setL3] (r/local))
    (return
     [:% ui-core/Enclosed
      {:label "ui-group/TreePane"}
      [:% ui-group/TreePane
       {:top 2
        :tree  {:x (-/nest-tree -/TREEDATA "x")
                :y (-/nest-tree -/TREEDATA "y")
                :z (-/nest-tree -/TREEDATA "z")
                :w (-/nest-tree -/TREEDATA "w")}
        :levels [{:type "list"
                  :initial initial
                  :setInitial setInitial
                  :width 3
                  :color "yellow"
                  :listFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "tabs"
                  :initial l1
                  :setInitial setL1
                  :width 30
                  :color "red"
                  :tabsFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "list"
                  :width 4
                  :initial l2
                  :color "green"
                  :setInitial setL2
                  :listFormat j/toUpperCase
                  :formatFn k/json-encode}
                 {:type "tabs"
                  :color "blue"
                  :width 10
                  :initial l3
                  :setInitial setL3
                  :tabsFormat j/toUpperCase
                  :formatFn k/json-encode}]}]
      [:box {:top 10 :shrink true
             :content (j/inspect #{initial l1 l2 l3})}]])))

^{:refer js.blessed.ui-group/displayTarget :added "4.0" :unchecked true}
(fact "helper function for display"
  ^:hidden
  
  )
