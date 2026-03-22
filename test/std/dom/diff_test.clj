(ns std.dom.diff-test
  (:require [std.dom.common :as base]
            [std.dom.diff :refer :all])
  (:use code.test))

^{:refer std.dom.diff/dom-ops :added "3.0"}
(fact "converts a set of props into operations"

  (dom-ops :mock/pane
            {:a 1}
            {:b 2})
  => [[:set :b 2 nil]
      [:delete :a 1]])

^{:refer std.dom.diff/diff-list-element :added "3.0"}
(fact "diffs for elements within a list"

  (diff-list-element 0 1 2)
  => [:list-set 0 2 1]^:hidden

  (diff-list-element 0
                     (base/dom-create :mock/label {} ["hello"])
                     (base/dom-create :mock/label {} ["world"]))
  => [:list-update 0 [[:set :text "world" "hello"]]])

^{:refer std.dom.diff/diff-list-elements :added "3.0"}
(fact "diff elements in array, limit to shortest array"

  (diff-list-elements [:a :b :c] [:b])
  => [[:list-set 0 :b :a]]

  (diff-list-elements [(base/dom-create :mock/label {} ["hello"])]
                      [(base/dom-create :mock/label {} ["world"])])
  => [[:list-update 0 [[:set :text "world" "hello"]]]])

^{:refer std.dom.diff/diff-list-dom :added "3.0"}
(fact "simplified list comparison using :dom/key"

  (diff-list-dom [(base/dom-compile [:mock/pane {:dom/key 3}])
                  (base/dom-compile [:mock/pane {:dom/key 1}])
                  (base/dom-compile [:mock/pane {:dom/key 2}])
                  (base/dom-compile [:mock/pane {:dom/key 4}])]
                 [(base/dom-compile [:mock/pane {:dom/key 1}])
                  (base/dom-compile [:mock/pane {:dom/key 2}])])
  => [[:list-remove 0 1] [:list-remove 2 1]]^:hidden
  
  (-> (diff-list-dom [(base/dom-compile [:mock/pane {:dom/key 1}])
                      (base/dom-compile [:mock/pane {:dom/key 2}])]
                     [(base/dom-compile [:mock/pane {:dom/key 3 :a 3}])
                      (base/dom-compile [:mock/pane {:dom/key 1 :a 1}])
                      (base/dom-compile [:mock/pane {:dom/key 2 :a 2}])])
      str read-string)
  => [[:list-insert 0 [[:- :mock/pane {:a 3}]]]
      [:list-update 1 [[:set :a 1 nil]]]
      [:list-update 2 [[:set :a 2 nil]]]])

^{:refer std.dom.diff/diff-list :added "3.0"}
(fact "constructs diffs for a lists"

  (diff-list [:a :b :c] [:b])
  => [[:list-remove 0 1]
      [:list-remove 1 1]]^:hidden
  
  (diff-list [(base/dom-create :mock/label {} ["hello"])]
             [(base/dom-create :mock/label {} ["world"])])
  => [[:list-update 0 [[:set :text "world" "hello"]]]]

  (-> (diff-list [(base/dom-create :mock/label {:dom/key 1} ["hello"])]
                 [(base/dom-create :mock/label {:dom/key 2} ["world"])
                  (base/dom-create :mock/label {:dom/key 1} ["hello"])])
      str read-string)
  => [[:list-insert 0 [[:- :mock/label "world"]]]])

^{:refer std.dom.diff/diff-props-element :added "3.0"}
(fact "diffs for elements within a props map"
  
  (diff-props-element :text "hello" "world")
  => [:set :text "world" "hello"]^:hidden

  (diff-props-element :top
                      (base/dom-create :mock/label {} ["hello"])
                      (base/dom-create :mock/label {} ["world"]))
  => [:update :top [[:set :text "world" "hello"]]])

^{:refer std.dom.diff/diff-props :added "3.0"}
(fact "constructs diff for a set of props"

  (diff-props {:top (base/dom-create :mock/label {} ["hello"])}
              {:top (base/dom-create :mock/label {} ["world"])})
  => [[:update :top [[:set :text "world" "hello"]]]]^:hidden
  
  (diff-props {:items ["a" "b" "c"]}
              {:items ["A" "B" "C"]})
  => [[:update :items [[:list-remove 0 3]
                       [:list-insert 0 ["A" "B" "C"]]]]])

^{:refer std.dom.diff/dom-ops-default :added "3.0"}
(fact "default implementation for diff-ops"
  
  (dom-ops-default :mock/pane
                   {:a 1}
                   {:b 2})
  => [[:set :b 2 nil]
      [:delete :a 1]])

^{:refer std.dom.diff/dom-diff :added "3.0"}
(fact "returns ops for dom transform"

  (dom-diff (base/dom-create :mock/pane {:hello 1} [:a :b :c])
            (base/dom-create :mock/pane {:hello 2} [:a :B :c]))
  => [[:set :hello 2 1]
      [:update :children [[:list-remove 1 1]
                          [:list-insert 1 [:B]]]]]^:hidden

  (dom-diff (base/dom-create :mock/pane)
            (base/dom-create :mock/label))
  => (contains-in [[:replace base/dom? base/dom?]])

  (dom-diff 1 2)
  => [[:replace 2 1]])

(comment
  (./import))
