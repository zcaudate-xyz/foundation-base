(ns std.dom.react-test
  (:use code.test)
  (:require [std.dom.common :as base]
            [std.dom.mock :as mock]
            [std.dom.react :refer :all]
            [std.dom.update :as update]))

^{:refer std.dom.react/reactive-pre-render :added "3.0"}
(fact "sets up the react key and react store"
  
  (-> (doto (base/dom-create :mock/label)
        (reactive-pre-render :hello))
      :cache)
  => {:react/key :hello, :react/store #{}}

  (-> (doto (base/dom-create :mock/label)
        (reactive-pre-render))
      :cache
      :react/key)
  => keyword?)

^{:refer std.dom.react/reactive-wrap-template :added "3.0"}
(fact "reactive wrapper function for :template"
  (let [f (reactive-wrap-template (fn [dom props] 1))]
    (f (doto (base/dom-create :mock/label) (reactive-pre-render :hello))
       {}))
  => 1)

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
(fact "schedules a component update"
  (let [dom (base/dom-create :mock/label)]
    (with-redefs [update/dom-refresh (constantly nil)]
      (with-scheduler
        (schedule-update dom)
        (first @*scheduler*))))
  => base/dom?)

^{:refer std.dom.react/flush-updates :added "4.0"}
(fact "flushes all pending updates"
  (let [dom (base/dom-create :mock/label)]
    (with-redefs [update/dom-refresh (constantly nil)]
      (with-scheduler
        (schedule-update dom)
        (flush-updates)
        @*scheduler*)))
  => #{})

^{:refer std.dom.react/with-scheduler :added "4.0"}
(fact "executes body with a scheduler bound"
  (let [dom (base/dom-create :mock/label)]
    (with-redefs [update/dom-refresh (constantly nil)]
      (with-scheduler
        (schedule-update dom)
        (count @*scheduler*))))
  => 1)
