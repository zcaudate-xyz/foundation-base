(ns std.dom.item-test
  (:require [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.impl :as impl]
            [std.dom.item :refer :all]
            [std.dom.mock :as mock]
            [std.dom.type :as type]
            [std.dom.update :as update]
            [std.lib.mutable :as mut])
  (:use code.test))

(def +init-date+
  (do (type/metaclass-add :test/value {:metatype :dom/value})
      (type/metaprops-add :test/value {:tag :test/date
                                       :construct (fn [{:keys [value]}]
                                                    (java.util.Date. (long (or value 0))))})))

(fact "testing date"

  (-> (base/dom-create :test/date)
      (impl/dom-render)
      (base/dom-item))
  => java.util.Date

  (-> (base/dom-create :test/date)
      (diff/dom-diff (base/dom-create :test/date {:value 100})))
  => (contains-in [[:replace
                    base/dom? ;; [:- :test/date {:value 100}]
                    base/dom? ;; [:- :test/date]
                    ]])
  
  (-> (base/dom-create :test/date)
      (impl/dom-render)
      (update/dom-update (base/dom-create :test/date {:value 100}))
      ^java.util.Date (base/dom-item)
      (.getTime))
  => 100)

(def +init-cat+
  (do (type/metaclass-add :test/element {:metatype :dom/element})
      (type/metaprops-add :test/element {:tag :test/cat
                                         :construct (fn [{:keys [name]}] (test.Cat. name))
                                         :getters {:name {:type String
                                                          :fn (fn [^test.Cat cat] (.getName cat))}}
                                         :setters {:name {:type String
                                                         :fn (fn [^test.Cat cat v] (.setName cat v))}}})))

(fact "testing cat"

  (-> (base/dom-create :test/cat {:name "fluffy"})
      (impl/dom-render)
      (base/dom-item))
  => test.Cat

  (-> (base/dom-create :test/cat {:name "fluffy"})
      (diff/dom-diff (base/dom-create :test/cat {:name "spike"})))
  => [[:set :name "spike" "fluffy"]]

  (-> (base/dom-create :test/cat {:name "spike"})
      (impl/dom-render)
      (update/dom-update (base/dom-create :test/cat {:name "fluffy"}))
      base/dom-format)
  => [:+ :test/cat {:name "fluffy"}])

^{:refer std.dom.item/item-constructor :added "3.0"}
(fact "returns the given constructor for the tag"

  (item-constructor :test/date)
  => fn?)

^{:refer std.dom.item/item-setters :added "3.0"}
(fact "returns the setters for the given tag"

  (item-setters :test/cat)
  => (contains-in {:name {:type java.lang.String, :fn fn?}})^:hidden
  
  (item-setters :test/date)
  => (throws))

^{:refer std.dom.item/item-getters :added "3.0"}
(fact "returns the getters for the given tag"

  (item-getters :test/cat)
  => (contains-in {:name {:type java.lang.String, :fn fn?}})^:hidden

  (item-getters :test/date)
  => (throws))

^{:refer std.dom.item/item-access :added "3.0"}
(fact "accesses the actual value of the object"

  (item-access :test/cat
               (item-create :test/cat {:name "fluffy"})
               :name)
  => "fluffy")

^{:refer std.dom.item/item-create :added "3.0"}
(fact "generic constructor for creating dom items"

  (item-create :test/date)
  => java.util.Date)

^{:refer std.dom.item/item-props-update :added "3.0"}
(fact "provides an extensible interface for update item prop calls"

  (item-props-update :test/cat
                     (item-create :test/cat)
                     [])
  => test.Cat

  (item-props-update :test/date
                     (item-create :test/date)
                     [])
  => (throws))

^{:refer std.dom.item/item-props-update-default :added "3.0"}
(fact "default implementation of item-prop-update. does nothing."

  (item-props-update-default :test/cat
                             (item-create :test/cat)
                             [])
  => test.Cat)

^{:refer std.dom.item/item-props-set :added "3.0"}
(fact "provides an extensible interface for set item prop calls"

  
  (.getName ^test.Cat
            (item-props-set :test/cat
                            (item-create :test/cat)
                            {:name "spike"}))
  => "spike"

  (doto (item-create :mock/pane)
    (#(item-props-set :mock/pane % {:a 1 :b 2}))))

^{:refer std.dom.item/item-props-set-default :added "3.0"}
(fact "default implementation of item-prop-set. throws exception"^:hidden

  (item-props-set-default :test/cat
                          (item-create :test/cat)
                          {})
  => test.Cat

  (item-props-set-default :test/date
                          (item-create :test/date)
                          {})
  => (throws))

^{:refer std.dom.item/item-props-delete :added "3.0"}
(fact "provides an extensible interface for delete item prop calls"

  (item-props-delete :test/cat
                     (item-create :test/cat {:name "hello"})
                     {:name "hello"})
  => test.Cat)

^{:refer std.dom.item/item-props-delete-default :added "3.0"}
(fact "default implementation of item-prop-set. returns item"
  ^:hidden
  
  (item-props-delete-default :test/cat (item-create :test/cat) {})
  => test.Cat)

^{:refer std.dom.item/item-update :added "3.0"}
(fact "updates item given transform operations"

  (-> (item-update :mock/pane
                   (item-create :mock/pane {:a 1})
                   [[:delete :a 1]
                    [:set :b 2]])
      :props)
  => {:b 2})

^{:refer std.dom.item/item-set-list :added "3.0"}
(fact "updates item given a list"

  (-> (item-set-list :mock/pane
                     (item-create :mock/pane)
                     :a [1 2 3 4])
      :props)
  => {:a [1 2 3 4]})

^{:refer std.dom.item/item-set-list-default :added "3.0"}
(fact "default implementation of item-set-list. throws exception"

  (item-set-list-default :mock/pane
                         (item-create :mock/pane)
                         :a [])
  => (throws))

^{:refer std.dom.item/item-cleanup :added "3.0"}
(fact "provides an extensible interface for itement cleanup"

  (item-cleanup :mock/pane (item-create :mock/pane))
  => mock/mock?)

^{:refer std.dom.item/item-cleanup-default :added "3.0"}
(fact "default implementation of item-prop-update. does nothing."^:hidden

  (item-cleanup-default :mock/pane (item-create :mock/pane))
  => mock/mock?)
