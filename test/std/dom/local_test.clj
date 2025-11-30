(ns std.dom.local-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lib.mutable :as mut]
            [std.dom.local :refer :all]
            [std.dom.common :as base]
            [std.dom.component :refer [defcomp]]
            [std.dom.impl :as impl]
            [std.dom.react :as react]
            [std.dom.type :as type]
            [std.dom.common-test]))

(defcomp :test/carrot
  [:local {:handler  prn
           :props    {:color "orange" :length 9}
           :trigger  #{:top}}]
  [_ _]
  (base/dom-compile
   [:mock/pane
    [:mock/label (str "Color: "  (local :color))]
    [:mock/label (str "Length: " (local :length) "cm")]]))

^{:refer std.dom.local/local-dom :added "3.0"}
(fact "returns the local dom"^:hidden

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      local-dom
      base/dom-format)
  => [:+ :test/carrot])

^{:refer std.dom.local/local-dom-state :added "3.0"}
(fact "returns the local dom state"^:hidden

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      local-dom-state
      deref)
  => {:color "orange", :length 9})

^{:refer std.dom.local/local-parent :added "3.0"}
(fact "returns the local dom parent"^:hidden

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      :shadow
      local-parent
      base/dom-format)
  => [:+ :test/carrot])

^{:refer std.dom.local/local-parent-state :added "3.0"}
(fact "returns the local dom parent state"^:hidden

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      :shadow
      local-parent-state
      deref)
  => {:color "orange", :length 9})

^{:refer std.dom.local/dom-ops-local :added "3.0"}
(fact "creates setters for local properties"
  
  (dom-ops-local {:a [1 2] :b 1}
                 {:a [1 2 3] :c 1})
  => [[:set :a [1 2 3] [1 2]]
      [:set :c 1 nil]
      [:delete :b 1]])

^{:refer std.dom.local/local-watch-create :added "3.0"}
(fact "creates a watch for local component"

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      (local-watch-create :color {:id :event/color}))
  => fn?)

^{:refer std.dom.local/local-watch-add :added "3.0"}
(fact "adds a watch to the local component"

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      (local-watch-add :on/color {:id :event/color}))
  => base/dom?)

^{:refer std.dom.local/local-watch-remove :added "3.0"}
(fact "removes a watch from the local component"

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      (local-watch-add :on/color {:id :event/color})
      (local-watch-remove :on/color))
  => base/dom?)

^{:refer std.dom.local/local-trigger-add :added "3.0"}
(fact "adds a trigger to the local component"

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      (local-trigger-add :on/top {:id :event/top}))
  => base/dom?)

^{:refer std.dom.local/local-trigger-remove :added "3.0"}
(fact "removes a trigger from the local component"

  (-> (base/dom-create :test/carrot)
      (impl/dom-render)
      (local-trigger-add :on/top {:id :event/top})
      (local-trigger-remove :on/top)))

^{:refer std.dom.local/local-split-props :added "3.0"}
(fact "splits props between change events and trigger events"

  (local-split-props (type/metaprops :test/carrot)
                     {:on/top  :trigger
                      :on/color :event})
  => [{:on/color :event} {:on/top :trigger}])

^{:refer std.dom.local/local-set :added "3.0"}
(fact "sets the local "

  (-> (base/dom-create :test/carrot {:color "purple"})
      (impl/dom-render)
      (local-set {:length 100} {:color "orange"})
      (local-dom-state)
      deref)
  => {:color "orange", :length 100})

^{:refer std.dom.local/dom-send-local :added "3.0"}
(fact "sends events that are in :local/events"
  (let [dom (base/dom-create :test/carrot)]
    (impl/dom-render dom)
    (reset! (:local/events (:cache dom)) [{:type :event}])
    (dom-send-local dom)
    @(:local/events (:cache dom)) => []))

^{:refer std.dom.local/dom-apply-local :added "3.0"}
(fact "applies operations to the dom"

  (-> (base/dom-create :test/carrot {:color "purple"})
      (impl/dom-render)
      (dom-apply-local (type/metaprops :test/carrot) [[:set :length 100]
                                                      [:delete :color "purple"]])
      (local-dom-state)
      deref)
  => {:color "orange", :length 100})

^{:refer std.dom.local/localized-watch :added "3.0"}
(fact "sets up the initial watch and triggers"
  (let [dom (base/dom-create :test/carrot {:on/color {:id :event/color}
                                           :on/top {:id :event/top}})
        _ (impl/dom-render dom)
        _ (localized-watch dom (:props dom))]
    (type (:local/active (:cache dom))) => clojure.lang.Atom))

^{:refer std.dom.local/dom-set-local :added "3.0"}
(fact "function for setting local dom"
  (let [dom (base/dom-create :test/carrot)
        _ (impl/dom-render dom)]
    (dom-set-local dom {:id :color :new "blue"})
    @(:local/state (:cache dom)) => (contains {:color "blue"})))

^{:refer std.dom.local/localized-handler :added "3.0"}
(fact "distinct handler for a given component"

  (localized-handler (type/metaprops :test/carrot))
  => fn?)

^{:refer std.dom.local/localized-pre-render :added "3.0"}
(fact "sets up the local state pre-render"

  (-> (doto (base/dom-create :mock/label)
        (localized-pre-render))
      :cache)
  => (contains {:local/state clojure.lang.Atom}))

^{:refer std.dom.local/localized-wrap-template :added "3.0"}
(fact "localized wrapper function for :template"
  (let [f (localized-wrap-template (fn [dom props] 1))]
    (f (base/dom-create :test/carrot) {}))
  => 1)

^{:refer std.dom.local/localized-pre-remove :added "3.0"}
(fact "localized function called on removal"

  (-> (doto (base/dom-create :mock/label)
        (localized-pre-render)
        (localized-pre-remove))
      :cache)
  => {})

^{:refer std.dom.local/local :added "3.0"}
(fact "accesses current local state"

  (binding [base/*local-dom* (doto (base/dom-create :mock/label)
                               (mut/mutable:set :cache {:local/state (atom {:hello 1})}))
            react/*react* (volatile! #{})]
    (local :hello))
  => 1)
