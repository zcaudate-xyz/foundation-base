(ns std.dom.item
  (:require [std.dom.common :as base]
            [std.dom.type :as type]
            [std.lib.collection]
            [std.lib.invoke :refer [definvoke]]))

(defmulti item-constructor
  "returns the given constructor for the tag
 
   (item-constructor :test/date)
   => fn?"
  {:added "3.0"}
  (fn [tag] (:metaclass (type/metaprops tag))))

(defmethod item-constructor :default 
  ([tag] 
   (or (:construct (type/metaprops tag))
      (throw (ex-info "No constructor" {:tag tag
                                        :metaprops (type/metaprops tag)})))))

(defmulti item-setters
  "returns the setters for the given tag
 
   (item-setters :test/cat)
   => (contains-in {:name {:type java.lang.String, :fn fn?}})"
  {:added "3.0"}
  (fn [tag] (:metaclass (type/metaprops tag))))

(defmethod item-setters :default 
  ([tag] 
   (let [metaprops (type/metaprops tag)]
    (if (= :dom/element (:metatype metaprops))
      (:setters metaprops)
      (throw (ex-info "No setters" {:tag tag
                                    :metaprops (type/metaprops tag)}))))))

(defmulti item-getters
  "returns the getters for the given tag
 
   (item-getters :test/cat)
   => (contains-in {:name {:type java.lang.String, :fn fn?}})"
  {:added "3.0"}
  (fn [tag] (:metaclass (type/metaprops tag))))

(defmethod item-getters :default 
  ([tag] 
   (let [metaprops (type/metaprops tag)]
    (if (= :dom/element (:metatype metaprops))
      (:getters metaprops)
      (throw (ex-info "No getters" {:tag tag
                                    :metaprops (type/metaprops tag)}))))))

(defn item-access
  "accesses the actual value of the object
 
   (item-access :test/cat
                (item-create :test/cat {:name \"fluffy\"})
                :name)
   => \"fluffy\""
  {:added "3.0"}
  ([tag item k] 
   (let [getters (item-getters tag)
        get-fn  (get-in getters [k :fn])]
    (if get-fn (get-fn item)))))

(defn item-create
  "generic constructor for creating dom items
 
   (item-create :test/date)
   => java.util.Date"
  {:added "3.0"}
  ([tag]
   (item-create tag {}))
  ([tag props]
   (let [construct (item-constructor tag)]
     (construct props))))

(defmulti item-props-update
  "provides an extensible interface for update item prop calls
 
   (item-props-update :test/cat
                      (item-create :test/cat)
                      [])
   => test.Cat
 
   (item-props-update :test/date
                      (item-create :test/date)
                      [])
   => (throws)"
  {:added "3.0"}
  (fn [tag item ops] (:metaclass (type/metaprops tag))))

(definvoke item-props-update-default
  "default implementation of item-prop-update. does nothing.
 
   (item-props-update-default :test/cat
                              (item-create :test/cat)
                              [])
   => test.Cat"
  {:added "3.0"}
  [:method {:multi item-props-update
            :val :default}]
  ([tag item ops]
   (let [metatype (:metatype (type/metaprops tag))]
     (if (= :dom/element metatype)
       item
       (throw (ex-info "Cannot update props" {:metatype metatype
                                              :metaprops (type/metaprops tag)
                                              :tag  tag
                                              :item item
                                              :ops  ops}))))))

(defmulti item-props-set
  "provides an extensible interface for set item prop calls
 
   
   (.getName ^test.Cat
             (item-props-set :test/cat
                             (item-create :test/cat)
                             {:name \"spike\"}))
   => \"spike\"
 
   (doto (item-create :mock/pane)
     (#(item-props-set :mock/pane % {:a 1 :b 2})))"
  {:added "3.0"}
  (fn [tag item props] (:metaclass (type/metaprops tag))))

(definvoke item-props-set-default
  "default implementation of item-prop-set. throws exception"
  {:added "3.0"}
  [:method {:multi item-props-set
            :val :default}]
  ([tag item props]
   (let [setters (item-setters tag)]
     (reduce-kv (fn [item k v]
                  (if-let [setter (get setters k)]
                    (doto item ((:fn setter) v))
                    (throw (ex-info "Cannot find set function" {:metaprops (type/metaprops tag)
                                                                :item item
                                                                :key k
                                                                :value v}))))
                item
                props))))

(defmulti item-props-delete
  "provides an extensible interface for delete item prop calls
 
   (item-props-delete :test/cat
                      (item-create :test/cat {:name \"hello\"})
                      {:name \"hello\"})
   => test.Cat"
  {:added "3.0"}
  (fn [tag item props] (:metaclass (type/metaprops tag))))

(definvoke item-props-delete-default
  "default implementation of item-prop-set. returns item"
  {:added "3.0"}
  [:method {:multi item-props-delete
            :val :default}]
  ([tag item props]
   item))

(defn item-update
  "updates item given transform operations
 
   (-> (item-update :mock/pane
                    (item-create :mock/pane {:a 1})
                    [[:delete :a 1]
                     [:set :b 2]])
       :props)
   => {:b 2}"
  {:added "3.0"}
  ([tag item ops] 
   (let [filter-fn   (fn [ops prefix]
                      (->> ops
                           (filter (comp #{prefix} first))
                           (map (comp vec rest))
                           (std.lib.collection/map-vals (fn [v] (cond-> v (base/dom? v) base/dom-item)))))
        set-props    (filter-fn ops :set)
        update-props (filter-fn ops :update)
        delete-props (filter-fn ops :delete)]
    (doto item 
      (#(item-props-set tag % set-props))
      (#(item-props-delete tag % delete-props))
      (#(item-props-update tag % update-props))))))

;; Updates Item Given Key and Updated List

(defmulti item-set-list
  "updates item given a list
 
   (-> (item-set-list :mock/pane
                      (item-create :mock/pane)
                      :a [1 2 3 4])
       :props)
   => {:a [1 2 3 4]}"
  {:added "3.0"}
  (fn [tag item k vlist] (:metaclass (type/metaprops tag))))

(definvoke item-set-list-default
  "default implementation of item-set-list. throws exception
 
   (item-set-list-default :mock/pane
                          (item-create :mock/pane)
                          :a [])
   => (throws)"
  {:added "3.0"}
  [:method {:multi item-set-list
            :val :default}]
  ([tag item k vlist]
   (let [setters (item-setters tag)
         setter  (get setters k)]
     (if-let [f (:fn setter)]
       (doto item (f vlist))
       (throw (ex-info "Cannot find set function" {:metaprops (type/metaprops tag)
                                                   :item item
                                                   :key k
                                                   :value vlist}))))))

;; Performs up additional Item destructors

(defmulti item-cleanup
  "provides an extensible interface for itement cleanup
 
   (item-cleanup :mock/pane (item-create :mock/pane))
   => mock/mock?"
  {:added "3.0"}
  (fn [tag item] (:metaclass (type/metaprops tag))))

(definvoke item-cleanup-default
  "default implementation of item-prop-update. does nothing."
  {:added "3.0"}
  [:method {:multi item-cleanup
            :val :default}]
  ([tag item]
   item))
