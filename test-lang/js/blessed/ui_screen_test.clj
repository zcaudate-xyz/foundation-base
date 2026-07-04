(ns js.blessed.ui-screen-test
  (:require [hara.lang :as l])
  (:use code.test))

(l/script :js
  {:runtime :basic
   :config   {:emit {:lang/jsx false}}
   :import   [["util" :as NodeUtil]]
   :require  [[js.react :as r :include [:fn]]
              [js.lib.valtio :as v]
               [js.blessed.ui-screen :as ui-screen]
               [js.blessed.ui-core :as ui-core]
               [js.blessed :as b :include [:fn]]
               [js.lib.chalk :as chk]
               [xt.lang.common-data :as xtd]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.blessed.ui-screen/ScreenMouse :added "4.0" :unchecked true}
(fact "component that updates mouse position"

  (defn.js ScreenMouseDemo
    []
    (var mouse (v/val ui-screen/Mouse))
    (return
     [:% ui-core/Enclosed
      {:label "ui-screen/ScreenMouse"
       :height 4}
      [:% ui-screen/ScreenMouse]
      [:box
       {:top 2
        :left 0
        :color "yellow"
        :content (NodeUtil.inspect mouse)}]])))

^{:refer js.blessed.ui-screen/ScreenMeasure :added "4.0" :unchecked true}
(fact "component that measures then screen"

  (defn.js ScreenMeasureDemo
    []
    (var dims (v/val ui-screen/Dimension))
    (return
     [:% ui-core/Enclosed
      {:label "ui-screen/ScreenMeasure"
       :height 4}
      [:% ui-screen/ScreenMeasure]
      [:box
       {:top 2
        :left 0
        :color "yellow"
        :content (NodeUtil.inspect dims)}]])))

^{:refer js.blessed.ui-screen/GridLayout :added "4.0" :unchecked true}
(fact "component that implements grid layout"

  (defn.js GridLayoutDemo
    []
    (var dims (v/val ui-screen/Dimension))
    (return
     [:% ui-core/Enclosed
      {:label "ui-screen/GridLayout"
       :height 10
       :width "100%"}
      [:box {:top 2}]
      [:% ui-screen/GridLayout
       {:top 1
         :items (xtd/arr-map (xtd/arr-range 20)
                       (fn:> [i]
                        [:box
                         {:top 1
                          :bottom 1
                          :left 1
                          :right 1
                          :bg (. ["green"
                                  "red"
                                  "yellow"
                                  "blue"
                                  "gray"]
                                 [(mod i 5)])}]))
        :display {:height 3
                  :width 20}}]
      #_[:box
       {:top 2
        :left 0
        :color "yellow"
        :content (NodeUtil.inspect dims)}]]))

  )

(comment

  [:% ui/GridLayout
   {:items [[:% -/SimpleBox]
            [:box {:key "a2"
                   :content "MARKET"}]
            #_[:box {:key "a3"
                     :content "MARKET"}]
            #_[:box {:key "a4"
                     :content "MARKET"}]]
    :display {:height 7
              :width 30}}])
