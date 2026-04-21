(ns std.dom.event-test
  (:require [std.dom :as dom]
            [std.dom.event :refer :all]
            [std.lib.invoke :as invoke]
            [std.lib.mutable :as mut])
  (:use code.test))

(invoke/definvoke mock-pane-local
  [:dom {:tag :mock/pane-local
         :class :local
         :handle {:test (fn [_ _])}}]
  ([_ _]
   (dom/dom-compile [:mock/pane])))

^{:refer std.dom.event/event-params :added "3.0"}
(fact "converts input into event params"

  (event-params :hello)
  => {:id :hello}

  (event-params {:id :hello})
  => {:id :hello})

^{:refer std.dom.event/event-handler :added "3.0"}
(fact "finds the relevant handler by ancestry"

  (def -parent- (doto (dom/dom-create :mock/pane)
                  (dom/dom-attach :parent-handler)))

  (def -child-  (doto (dom/dom-create :mock/pane)
                  (mut/mutable:set :parent -parent-)))

  (event-handler -child-)
  => :parent-handler)

^{:refer std.dom.event/handle-local :added "3.0"}
(fact "handles a local dom event"

  (handle-local (-> (dom/dom-create :mock/pane-local)
                    (dom/dom-render)
                    :shadow)
                {:id :local/test})
  ;;[:+ :mock/pane-local]
  => dom/dom?)

^{:refer std.dom.event/handle-event :added "3.0"}
(fact "handles an event given all necessary inputs"
  (with-redefs [std.dom.react/dom-set-state identity]
    (handle-event {:tag :mock/pane}
                  {:id :dom/set}
                  :item
                  :listener
                  {:value 1}))
  => (contains {:id :dom/set
                :item :item
                :listener :listener
                :value 1}))
