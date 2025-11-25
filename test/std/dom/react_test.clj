(ns std.dom.react-test
  (:use code.test)
  (:require [std.dom.common :as base]
            [std.dom.mock :as mock]
            [std.dom.react :refer :all]))

^{:refer std.dom.react/reactive-pre-render :added "3.0"}
(fact "sets up the react key and react store"
  
  (-> (doto (base/dom-create :mock/label)
        (reactive-pre-render :hello))
      :cache)
  => {:react/key :hello, :react/store #{}})

^{:refer std.dom.react/reactive-wrap-template :added "3.0"}
(fact "reactive wrapper function for :template")

^{:refer std.dom.react/reactive-pre-remove :added "3.0"}
(fact "removes the react key and react store"

  (-> (doto (base/dom-create :mock/label)
        (reactive-pre-render :hello)
        (reactive-pre-remove))
      :cache)
  => {})

^{:refer std.dom.react/react :added "3.0"}
(fact "call to react, for use within component"

  (binding [*react* (volatile! #{})]
    (react (atom {:data 1}) [:data]))
  => 1)

^{:refer std.dom.react/dom-set-state :added "3.0"}
(fact "sets a state given function params"
  
  (def -state- (atom {}))

  (do (dom-set-state {:state -state-
                      :key :hello
                      :new 1
                      :transform str})
      @-state-)
  => {:hello "1"})

(comment
  (./import))




^{:refer std.dom.react/schedule-update :added "4.0"}
(fact "TODO")

^{:refer std.dom.react/flush-updates :added "4.0"}
(fact "TODO")

^{:refer std.dom.react/with-scheduler :added "4.0"}
(fact "TODO")