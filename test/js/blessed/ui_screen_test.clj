(ns js.blessed.ui-screen-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:runtime :basic
   :config   {:emit {:lang/jsx false}}
   :require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.ui-screen :as ui-screen]
              [js.blessed.ui-core :as ui-core]
              [js.blessed :as b :include [:fn]]
              [js.lib.chalk :as chk]
              [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(fact:global
 {:setup [(l/rt:restart)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.blessed.ui-screen/ScreenMouse :added "4.0" :unchecked true}
(fact "component that updates mouse position"
  ^:hidden
  
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
        :content (j/inspect mouse)}]])))

^{:refer js.blessed.ui-screen/ScreenMeasure :added "4.0" :unchecked true}
(fact "component that measures then screen"
  ^:hidden

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
        :content (j/inspect dims)}]])))

^{:refer js.blessed.ui-screen/GridLayout :added "4.0" :unchecked true}
(fact "component that implements grid layout"
  ^:hidden
  
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
        :items (j/map (k/arr-range 20)
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
        :content (j/inspect dims)}]]))
  
  (def.js MODULE (!:module)))

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
