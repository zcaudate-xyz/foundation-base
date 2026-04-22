(ns std.dom.common-test
  (:require [std.dom.common :refer :all]
            [std.dom.impl :as impl]
            [std.dom.mock :as mock])
  (:use code.test))

^{:refer std.dom.common/dom? :added "3.0"}
(fact "checks if object is an dom"

  (dom? (dom-create :mock/label {} ["hello"]))
  => true)

^{:refer std.dom.common/dom-metaprops :added "3.0"}
(fact "checks dom for ui type"

  (dom-metaprops (dom-new :mock/label {}))
  => (contains {:tag :mock/label
                :construct fn?
                :children {:key :text :single true}
                :metaclass :mock/element
                :metatype :dom/element}))

^{:refer std.dom.common/dom-metatype :added "3.0"}
(fact "returns the associated metatype"

  (dom-metatype (dom-new :mock/label {}))
  => :dom/element

  (dom-metatype (dom-new :mock/insets {}))
  => :dom/value)

^{:refer std.dom.common/dom-metaclass :added "3.0"}
(fact "returns the associated metaclass"

  (dom-metaclass (dom-new :mock/label {}))
  => :mock/element

  (dom-metaclass (dom-new :mock/insets {}))
  => :mock/value)

^{:refer std.dom.common/component? :added "3.0"}
(fact "checks if metatype is of :dom/component"

  (component? (dom-new :mock/label {}))
  => false)

^{:refer std.dom.common/element? :added "3.0"}
(fact "checks if metatype is of :dom/element"

  (element? (dom-new :mock/label {}))
  => true)

^{:refer std.dom.common/value? :added "3.0"}
(fact "checks if metatype is of :dom/value"

  (value? (dom-new :mock/insets {}))
  => true)

^{:refer std.dom.common/dom-item :added "3.0"}
(fact "returns itement associated with the dom"

  (-> (dom-new :mock/label {:a 1})
      (impl/dom-render)
      (dom-item))
  => mock/mock?

  (-> (dom-new :mock/label)
      (dom-item))
  => nil)

^{:refer std.dom.common/dom-item? :added "3.0"}
(fact "returns whether dom has associated itement"

  (dom-item? (dom-new :mock/pane))
  => false

  (-> (dom-new :mock/pane)
      impl/dom-render
      dom-item?)
  => true)

^{:refer std.dom.common/dom-top :added "3.0"}
(fact "returns the top-most dom element"

  (def -a- (dom-create :mock/label))
  (def -b- (dom-create :mock/pane {} [(dom-create :mock/pane {} [-a-])]))

  (dom-top -a-) => -b-)

^{:refer std.dom.common/dom-split-props :added "3.0"}
(fact "splits :on namespace keywords"

  (dom-split-props {:on/item :event/item
                    :item "hello"})
  => [{:on/item :event/item} {:item "hello"}])

^{:refer std.dom.common/props-apply :added "3.0"}
(fact "applies an action to props map"

  (props-apply {:a (dom-create :mock/label {:text "hello"})
                :b (dom-create :mock/pane {} ["world"])}
               (comp :props dom-item impl/dom-render))
  => {:a {:text "hello"},
      :b {:children ["world"]}})

^{:refer std.dom.common/dom-new :added "3.0"}
(fact "creates a new dom type"

  (dom-new :mock/label {})
  => dom?)

^{:refer std.dom.common/dom-children :added "3.0"}
(fact "retrieves children of dom object"

  (dom-children (dom-create :mock/pane {} ["hello"]))
  => {:key :children
      :children ["hello"]}

  (dom-children (dom-create :mock/pane {}
                            [(dom-create :mock/pane)
                             (dom-create :mock/pane)]))
  => (contains-in {:key :children
                   :children [dom? dom?]}))

^{:refer std.dom.common/children->props :added "3.0"}
(fact "converts children array to props"

  (children->props ["hello"] :mock/label)
  => {:text "hello"}

  (children->props ["hello"] :mock/pane)
  => {:children ["hello"]})

^{:refer std.dom.common/dom-create :added "3.0"}
(fact "creates an dom"

  (dom-create :mock/pane {} ["hello"])
  ;; [:- :label "hello"]
  => dom?

  (-> (dom-create :mock/pane {:dom/init {:children ["hello"]}})
      :extra)
  => {:dom/init {:children ["hello"]}}

  (-> (dom-create :mock/pane {:dom/key 123})
      :extra)
  => {:dom/key 123})

^{:refer std.dom.common/dom-format :added "3.0"}
(fact "formats dom for printing"

  (dom-format (dom-create :mock/label {} ["hello"]))
  => [:- :mock/label "hello"])

^{:refer std.dom.common/dom-tags-equal? :added "3.0"}
(fact "checks if two dom nodes have the same tags"

  (dom-tags-equal? (dom-create :mock/label)
                   (dom-create :mock/label))
  => true)

^{:refer std.dom.common/dom-props-equal? :added "3.0"}
(fact "checks if two dome nodes have the same props"

  (dom-props-equal? {:a 1 :b 2}
                    {:a 1 :b 2})
  => true)

^{:refer std.dom.common/dom-equal? :added "3.0"}
(fact "checks if two dom elements are equal"

  (dom-equal? (dom-create :mock/label {} ["hello"])
              (dom-create :mock/label {} ["hello"]))
  => true

  (dom-equal? (dom-create :mock/label)
              (dom-create :mock/label {} ["hello"]))
  => false

  (dom-equal? (dom-create :mock/pane)
              (dom-create :mock/label))
  => false

  (dom-equal? (dom-create :mock/pane {} [(dom-create :mock/label)])
              (dom-create :mock/pane {} [(dom-create :mock/label)]))
  => true

  (dom-equal? (dom-create :mock/pane {} [(dom-create :mock/label)])
              (dom-create :mock/pane {} [(dom-create :mock/label {:text "hello"})]))
  => false)

^{:refer std.dom.common/dom-clone :added "3.0"}
(fact "creates shallow copy of a node and its data"

  (def -a- (dom-create :mock/label))
  (def -b- (dom-clone -a-))

  (= -a- -b-) => false
  (dom-equal? -a- -b-) => true)

^{:refer std.dom.common/dom-vector? :added "3.0"}
(fact "checks if a vector can be a dom representation"

  (dom-vector? [:fx/label ""])
  =>  true

  (dom-vector? ^:data [:fx/label ""])
  => false)

^{:refer std.dom.common/dom-compile :added "3.0"}
(fact "compiles a tree structure into a dom"

  (-> (dom-compile [:mock/pane "hello"])
      dom-format)
  => [:- :mock/pane "hello"]

  (-> (dom-compile [:mock/pane
                    [:mock/label "hello"]
                    [:mock/label "world"]])
      dom-format)
  => [:- :mock/pane [:- :mock/label "hello"] [:- :mock/label "world"]])

^{:refer std.dom.common/dom-attach :added "3.0"}
(fact "attaches a handler to a dom node"

  (-> (dom-attach (dom-create :mock/label)
                  (fn [dom event] event))
      :handler)
  => fn?)

^{:refer std.dom.common/dom-detach :added "3.0"}
(fact "detaches a handler from a dom node"

  (-> (dom-attach (dom-create :mock/label)
                  (fn [dom event] event))
      (dom-detach)
      :handler)
  => nil)

^{:refer std.dom.common/dom-trigger :added "3.0"}
(fact "triggers an event, propogating up the dom hierarchy"

  (def -p- (promise))

  (-> (dom-create :mock/label)
      (dom-attach (fn [dom event] (deliver -p- event) nil))
      (dom-trigger {:a 1 :b 2}))

  @-p- => {:a 1, :b 2})

^{:refer std.dom.common/dom-assert :added "3.0"}
(fact "asserts that the props are valid"
  (dom-assert {:a 1 :b 2} [:a])

  (dom-assert {:a 1 :b 2} [:c])
  => (throws))
