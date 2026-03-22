(ns std.dom.mock
  (:require [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.impl :as impl]
            [std.dom.item :as item]
            [std.dom.type :as type]
            [std.dom.update :as update]
            [std.lib.collection :as collection]
            [std.lib.invoke :as invoke]
            [std.lib.mutable :as mut :refer [defmutable]]))

(declare mock-format)

(defprotocol IMock)

(defmutable MockElement [tag props]
  Object
  (toString [item] (str (mock-format item)))
  IMock)

(defmethod print-method MockElement 
  ([v ^java.io.Writer w] 
   (.write w (str v))))

(defrecord MockValue [tag props]
  Object
  (toString [item] (str (mock-format item)))
  IMock)

(defmethod print-method MockValue 
  ([v  ^java.io.Writer w] 
   (.write w (str v))))

(defn mock?
  "checks if object is a mock item
 
   (-> (item/item-create :mock/label)
       (mock?))
   => true"
  {:added "3.0"}
  ([obj] 
   (satisfies? IMock obj)))

(defn mock-format
  "checks if object is a mock item
 
   (-> (item/item-create :mock/label {:text \"hello\"})
       (mock-format))
   => [:mock/label \"hello\"]
 
   (-> (item/item-create :mock/pane {:children [\"a\" \"b\" \"c\"]})
       (mock-format))
   => [:mock/pane \"a\" \"b\" \"c\"]"
  {:added "3.0"}
  ([{:keys [tag props] :as item}] 
   (let [fmt-fn  (fn [obj] (if (mock? obj) (mock-format obj) obj))
        {:keys [key children]} (base/dom-children item)
        props  (collection/map-vals (fn [p]
                             (cond (sequential? p)
                                   (mapv fmt-fn p)
                                   
                                   :else (fmt-fn p)))
                           (dissoc props key))]
    (if (empty? props)
      (apply vector tag children)
      (apply vector tag props children)))))

(invoke/definvoke item-props-delete-mock
  "custom props delete function for mock item
 
   (-> (item-props-delete-mock :mock/pane
                               (item/item-create :mock/pane {:a 1 :b 2})
                               {:b 2})
       :props)
   => {:a 1}"
  {:added "3.0"}
  [:method {:multi item/item-props-delete
            :val :mock/element}]
  ([tag item props]
   (apply mut/mutable:update item :props dissoc (keys props))))

(invoke/definvoke item-props-set-mock
  "custom props update function for mock item
 
   (-> (item-props-set-mock :mock/pane
                            (item/item-create :mock/pane {:a 1})
                            {:b 2 :c 3})
       :props)
   => {:a 1, :b 2, :c 3}"
  {:added "3.0"}
  [:method {:multi item/item-props-set
            :val :mock/element}]
  ([tag item props]
   (mut/mutable:update item :props merge props)))

(invoke/definvoke item-set-list-mock
  "custom props set list function for mock item
 
   (-> (item-set-list-mock :mock/pane
                           (item/item-create :mock/pane)
                           :a [1 2 3 4])
       :props)
   => {:a [1 2 3 4]}"
  {:added "3.0"}
  [:method {:multi item/item-set-list
            :val :mock/element}]
  ([tag item k vlist]
   (mut/mutable:update item :props assoc k vlist)))

(defonce +init+
  (do (type/metaclass-add :mock/element {:metatype :dom/element})
      (type/metaclass-add :mock/value   {:metatype :dom/value})
      (type/metaprops-add :mock/element {:tag :mock/label
                                         :construct (fn [props] (->MockElement :mock/label props))
                                         :children {:key :text :single true}
                                         :no-check true})
      
      (type/metaprops-add :mock/element {:tag :mock/pane
                                         :construct (fn [props] (->MockElement :mock/pane props))
                                         :no-check true})

      (type/metaprops-add :mock/value   {:tag :mock/insets
                                         :construct (fn [props] (->MockValue :mock/insets props))})))
