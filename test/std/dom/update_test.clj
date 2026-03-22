(ns std.dom.update-test
  (:require [std.dom.common :as base]
            [std.dom.impl :as impl]
            [std.dom.mock :as mock]
            [std.dom.update :refer :all])
  (:use code.test))

^{:refer std.dom.update/dom-apply :added "3.0"}
(fact "applies operations to the dom"
  (-> (base/dom-create :mock/pane {:a 1})
      (impl/dom-render)
      (dom-apply [[:set :b 2 nil]
                  [:delete :a 1]])
      (base/dom-item)
      :props)
  => {:b 2})

^{:refer std.dom.update/update-set :added "3.0"}
(fact "sets props given a transform"

  (update-set {} [:set :a 1])
  => {:a 1}

  (update-set {} [:set :a (base/dom-compile [:mock/label "hello"])])
  => (contains {:a base/dom?}))

^{:refer std.dom.update/update-list-insert :added "3.0"}
(fact "updates the list by inserting values"

  (update-list-insert []
                      [:list-insert 0 [1 2 3 4]])
  => [1 2 3 4])

^{:refer std.dom.update/update-list-remove :added "3.0"}
(fact "updates the list by deleting values"

  (update-list-remove [1 2 3 4]
                      [:list-remove 1 2])
  => [1 4])

^{:refer std.dom.update/update-list-update :added "3.0"}
(fact "updates :update changes to list"

  (update-list-update [(-> (base/dom-compile [:mock/label])
                           (impl/dom-render))]
                      [:update 0 [[:set :text "hello"]]])
  => (contains [#(= "hello" (:text (:props %)))]))

^{:refer std.dom.update/update-list-append :added "3.0"}
(fact "updates :append changes to list"
  
  (update-list-append ["A"]
                      [:list-append "B"])
  => ["A" "B"])

^{:refer std.dom.update/update-list-drop :added "3.0"}
(fact "updates :drop changes to list"

  (update-list-drop ["A" "B"]
                    [:list-drop "B"])
  => ["A"])

^{:refer std.dom.update/update-list :added "3.0"}
(fact "updates a list within props"

  (update-list (impl/dom-render (base/dom-compile [:mock/pane "a"]))
               :items
               ["a"]
               [[:list-set 0 "A" "a"]])
  => ["A"]^:hidden
  
  (update-list (impl/dom-render (base/dom-compile [:mock/pane"a"]))
               :items
               ["a"]
               [[:list-drop "a"]])
  => []
  
  (update-list (impl/dom-render (base/dom-compile [:mock/pane"a"]))
               :items
               ["a"]
               [[:list-append "b"]])
  => ["a" "b"])

^{:refer std.dom.update/update-props-delete :added "3.0"}
(fact "updates props given an operation"
  
  (update-props-delete {:text "hello"}
                       [:delete :text "hello"])
  => {})

^{:refer std.dom.update/update-props-update :added "3.0"}
(fact "updates :update changes to props"

  (update-props-update (doto (base/dom-compile [:mock/pane]) impl/dom-render)
                       {:items ["A"]}
                       [:update :items [[:list-set 0 "B"]]])
  => {:items ["B"]})

^{:refer std.dom.update/update-props :added "3.0"}
(fact "updates props of doms"

  (update-props (base/dom-compile [:mock/label]) {} [[:set :text "hello"]])
  => {:text "hello"}^:hidden

  (let [-label- (base/dom-compile [:mock/label "a"])]
    (-> (update-props (doto (base/dom-compile [:mock/pane {:top -label-}])
                        impl/dom-render)
                      {:top -label-}
                      [[:set :top (base/dom-compile [:mock/label "A"])]])
        :top
        base/dom-format))
  => [:+ :mock/label "A"]

  (update-props (doto (base/dom-compile [:mock/pane "a"])
                  (impl/dom-render))
                {:items ["a"]}
                [[:update :items [[:list-set 0 "A"]]]])
  => {:items ["A"]})

^{:refer std.dom.update/dom-apply-default :added "3.0"}
(fact "default function for dom-apply"
  
  (-> (dom-apply-default (doto (base/dom-compile [:mock/pane {:items ["a"]}])
                           (impl/dom-render))
                         [[:update :items [[:list-set 0 "A"]]]])
      (base/dom-item)
      (mock/mock-format))
  => [:mock/pane {:items ["A"]}])

^{:refer std.dom.update/dom-update :added "3.0"}
(fact "updates current dom given new dom"
  
  (-> (doto (base/dom-compile [:mock/pane {:a 1}
                               [:mock/pane {:b 2}]
                               [:mock/pane {:c 3}]])
        (impl/dom-render)
        (dom-update (base/dom-compile [:mock/pane {:a 1}
                                       [:mock/pane {:b 2}]
                                       [:mock/pane {:c 4}]])))
      (base/dom-item)
      str read-string)
  => [:mock/pane {:a 1} [:mock/pane {:b 2}] [:mock/pane {:c 4}]])

^{:refer std.dom.update/dom-refresh :added "3.0"}
(fact "refreshes current dom, used for components"

  (-> (base/dom-compile [:mock/pane {:b 2}])
      (impl/dom-render)
      (dom-refresh)
      str read-string)
  => [:+ :mock/pane {:b 2}])

(comment
  (./import)
  (./scaffold)
  )
