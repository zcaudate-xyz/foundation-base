(ns js.blessed.frame-console-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.blessed.frame-console :as frame-console]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

^{:refer js.blessed.frame-console/ConsoleMain :added "4.0" :unchecked true}
(fact "creates a primary frame-console button"
  ^:hidden
  
  (defn.js ConsoleMainDemo
    []
    (var [current setCurrent] (r/local))
    (return
     [:% ui-core/Enclosed
      {:label "frame-console/ConsoleMain"}
      [:box {:top 1}
       [:% frame-console/ConsoleMain
        #{[current setCurrent
           :screens {:A1 (fn:> [:box "A1"])
                     :B2 (fn:> [:box "B2"])
                     :C3 (fn:> [:box "C3"])}]}]]])))

^{:refer js.blessed.frame-console/Console :added "4.0" :unchecked true}
(fact "creates a primary frame-console button"
  ^:hidden
  
  (defn.js ConsoleDemo
    []
    (var [show setShow] (r/local true))
    (var [current setCurrent] (r/local))
    (return
     [:% ui-core/Enclosed
      {:label "frame-console/Console"
       :height 20}
      [:box {:top 1
             :bg "red"}
       [:% ui-core/SmallButton
        {:content "Toggle"
         :color "white"
         :onClick (fn:> (setShow (not show)))}]
       [:% frame-console/Console
        #{[current setCurrent
           show setShow
           :bottom 1
           :left 1
           :right 1
           :height 10
           :screens {:A1 (fn:> [:box "A1"])
                     :B2 (fn:> [:box "B2"])
                     :C3 (fn:> [:box "C3"])}]}]]]))

  )
