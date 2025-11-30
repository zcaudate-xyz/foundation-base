(ns std.dom.sync-test
  (:use code.test)
  (:require [std.dom.sync :refer :all]
            [std.dom.common :as base]
            [std.dom.update :as update]
            [std.dom.mock :as mock]
            [std.dom.diff :as diff]))

^{:refer std.dom.sync/sync-step :added "4.0"}
(fact "Performs one sync cycle"
  (let [dom-a {:tag :mock/label :props {:text "A"}}
        dom-b {:tag :mock/label :props {:text "B"}}
        server-dom (atom dom-b)
        client-dom (atom dom-a)
        last-synced (atom dom-a)

        client (map->LocalClient {:shadow-atom client-dom})
        server (map->LocalServer {:dom-atom server-dom :client-ref client})]

    (with-redefs [update/dom-apply (fn [dom ops] (assoc dom :props {:text "B"}))
                  diff/dom-diff (fn [a b] [[:set :text "B"]])]
      (sync-step server client last-synced))
    => vector?

    (:props @client-dom)
    => {:text "B"}))
