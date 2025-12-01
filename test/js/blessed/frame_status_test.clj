(ns js.blessed.frame-status-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require  [[js.react :as r :include [:fn]]
              [js.core :as j :include [:node :util]]
              [js.lib.valtio :as v]
              [js.blessed.frame-status :as frame-status]
              [js.blessed.ui-core :as ui-core]
              [js.lib.chalk :as chk]]
   :export  [MODULE]})

^{:refer js.blessed.frame-status/Status :added "4.0" :unchecked true}
(fact "displays status"
  ^:hidden
  
  (defn.js StatusDemo
    []
    (var [busy setBusy] (r/local false))
    (var [status setStatus] (r/local {:content "Hello World"
                                       :type "info"}))
    (return
     [:% ui-core/Enclosed
      {:label "frame-status/Status"
       :height 15}
      [:box {:top 2}
       [:% frame-status/Status
        #{busy setBusy
          status setStatus}]]
      [:box {:top 4}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "red"
                 :content " Set Error "
                 :onClick (fn:> (setStatus {:content "Hello Error"
                                            :type "error"}))}]]
      [:box {:top 6}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "yellow"
                 :content " Set Warn "
                 :onClick (fn:> (setStatus {:content "Hello Warn"
                                            :type "warn"}))}]]
      [:box {:top 8}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :bg "blue"
                 :content " Set Info "
                 :onClick (fn:> (setStatus {:content "Hello Info"
                                            :type "info"}))}]]
      [:box {:top 10}
       [:button {:mouse true
                 :keys true
                 :shrink true
                 :content (+ " Busy: " busy)
                 :onClick (fn:> (setBusy (not busy)))}]]]))
  
  (def.js MODULE (!:module)))
