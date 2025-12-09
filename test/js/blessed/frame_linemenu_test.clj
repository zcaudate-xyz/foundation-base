(ns js.blessed.frame-linemenu-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.frame-linemenu :as frame-linemenu]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

^{:refer js.blessed.frame-linemenu/LineButton :added "4.0" :unchecked true}
(fact "creates a line frame-linemenu button"
  ^:hidden
  
  (defn.js LineButtonDemo
    []
    (let [[route setRoute] (r/local "file")]
      (return
       [:% ui-core/Enclosed
        {:label "frame-linemenu/LineButton"}
        [:% frame-linemenu/LineButton
         {:top 2
          :index "f1"
          :label "File"
          :selected (== route "file")
          :route "file"
          :setRoute setRoute}]
        [:% frame-linemenu/LineButton
         {:top 4
          :index "f2"
          :label "Options"
          :selected (== route "options")
          :route "options"
          :setRoute setRoute}]]))))

^{:refer js.blessed.frame-linemenu/layoutMenu :added "4.0" :unchecked true}
(fact "helper function for LineMenu")

^{:refer js.blessed.frame-linemenu/LineMenu :added "4.0" :unchecked true}
(fact "creates a line menu"
  ^:hidden
  
  (defn.js LineMenuDemo
    []
    (var [route setRoute] (r/local "file"))
    (var entries (frame-linemenu/layoutMenu [{:index "f1"
                                              :label "File"
                                              :route "file"}
                                             {:index "f2"
                                              :label "Options"
                                              :route "options"}]))
    (return
     [:% ui-core/Enclosed
      {:label "frame-linemenu/LineMenu"}
      [:box {:top 2}
       [:% frame-linemenu/LineMenu
        #{route
          setRoute
          {:entries entries}}]]]))
  
  )
