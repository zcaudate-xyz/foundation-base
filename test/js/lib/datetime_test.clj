(ns js.lib.datetime-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require  [[js.core :as j :include [:node :util]]
              [js.react :as r :include [:fn]]
              [js.lib.datetime :as ut]]
   :export  [MODULE]})

^{:refer js.lib.datetime/ago :added "4.0" :unchecked true}
(fact "formats the time ago")

^{:refer js.lib.datetime/agoVerbose :added "4.0" :unchecked true}
(fact "formats the time ago in verbose"
  ^:hidden
  
  (defn.js AgoDemo
    []
    (let [current (r/const (* (- (j/now) 20000) 1000))
          [delay setDelay] (r/useCountdown
                            9 (fn []
                                (setDelay 9)))]
      (return
       [:box {:width 30
              :content "ut/ago"
              :height 8}
        [:box
         {:top 2
          :left 0
          :color "yellow"
          :content (+ "simple: " (ut/ago current) "\n"
                      "verbose: " (ut/agoVerbose current) "\n")}]]))))

^{:refer js.lib.datetime/formatDate :added "4.0" :unchecked true}
(fact "formats the date")
