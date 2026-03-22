(ns std.dom.common
  (:require [std.dom.type :as type]
            [std.lib.collection]
            [std.lib.invoke :refer [definvoke]]
            [std.lib.mutable :as mut :refer [defmutable]]))

(def ^:dynamic *current-dom* nil)

(def ^:dynamic *local-dom*  nil)

(declare dom-format)

(defmutable Dom [tag props item parent handler shadow cache extra]
  Object
  (toString
   [this]
   (str (dom-format this))))

(defmethod print-method Dom 
  ([v ^java.io.Writer w] 
   (.write w (str v))))

(defn dom?
  "checks if object is an dom
 
   (dom? (dom-create :mock/label {} [\"hello\"]))
   => true"
  {:added "3.0"}
  ([obj] 
   (instance? Dom obj)))

(defn dom-metaprops
  "checks dom for ui type
 
   (dom-metaprops (dom-new :mock/label {}))
   => (contains {:tag :mock/label
                 :construct fn?
                 :children {:key :text :single true}
                 :metaclass :mock/element
                 :metatype :dom/element})"
  {:added "3.0"}
  ([dom] 
   (-> dom :tag type/metaprops)))

(defn dom-metatype
  "returns the associated metatype
 
   (dom-metatype (dom-new :mock/label {}))
   => :dom/element"
  {:added "3.0"}
  ([dom] 
   (-> dom dom-metaprops :metatype)))

(defn dom-metaclass
  "returns the associated metaclass
 
   (dom-metaclass (dom-new :mock/label {}))
   => :mock/element"
  {:added "3.0"}
  ([dom] 
   (-> dom dom-metaprops :metaclass)))

(defn component?
  "checks if metatype is of :dom/component
 
   (component? (dom-new :mock/label {}))
   => false"
  {:added "3.0"}
  ([dom] 
   (= :dom/component (dom-metatype dom))))

(defn element?
  "checks if metatype is of :dom/element
 
   (element? (dom-new :mock/label {}))
   => true"
  {:added "3.0"}
  ([dom] 
   (= :dom/element (dom-metatype dom))))

(defn value?
  "checks if metatype is of :dom/value
 
   (value? (dom-new :mock/insets {}))
   => true"
  {:added "3.0"}
  ([dom] 
   (= :dom/value (dom-metatype dom))))

(defn dom-item
  "returns itement associated with the dom
 
   (-> (dom-new :mock/label {:a 1})
       (impl/dom-render)
       (dom-item))
   => mock/mock?
 
   (-> (dom-new :mock/label)
       (dom-item))
   => nil"
  {:added "3.0"}
  ([dom] 
   (:item dom)))

(defn dom-item?
  "returns whether dom has associated itement
 
   (dom-item? (dom-new :mock/pane))
   => false
 
   (-> (dom-new :mock/pane)
       impl/dom-render
       dom-item?)
   => true"
  {:added "3.0"}
  ([dom] 
   (boolean (:item dom))))

(defn dom-top
  "returns the top-most dom element
 
   (def -a- (dom-create :mock/label))
   (def -b- (dom-create :mock/pane {} [(dom-create :mock/pane {} [-a-])]))
 
   (dom-top -a-) => -b-"
  {:added "3.0"}
  ([dom] 
   (if-let [parent (:parent dom)]
    (dom-top parent)
    dom)))

(defn dom-split-props
  "splits :on namespace keywords
 
   (dom-split-props {:on/item :event/item
                     :item \"hello\"})
   => [{:on/item :event/item} {:item \"hello\"}]"
  {:added "3.0"}
  ([props]
   (dom-split-props props #{"on"} ["on" nil]))
  ([props lookup select]
   (let [groups   (reduce (fn [out k]
                            (let [nsp (get lookup (namespace k))]
                              (update-in out [nsp] (fnil #(conj % k) #{}))))
                          {}
                          (keys props))
         result   (reduce-kv (fn [out nsp ks]
                               (assoc out nsp (select-keys props ks)))
                             {}
                             groups)]
     (mapv result select))))

(defn props-apply
  "applies an action to props map
 
   (props-apply {:a (dom-create :mock/label {:text \"hello\"})
                 :b (dom-create :mock/pane {} [\"world\"])}
                (comp :props dom-item impl/dom-render))
   => {:a {:text \"hello\"},
       :b {:children [\"world\"]}}"
  {:added "3.0"}
  ([props action] 
   (std.lib.collection/map-vals (fn prop-apply [v]
                  (cond (dom? v)
                        (action v)
                        
                        (sequential? v)
                        (mapv prop-apply v)
                        
                        :else v))
                props)))

(defn dom-new
  "creates a new dom type
 
   (dom-new :mock/label {})
   => dom?"
  {:added "3.0"}
  ([]
   (dom-new nil))
  ([obj]
   (cond (keyword? obj)
         (dom-new obj nil)

         (map? obj)
         (let [{:keys [tag props item parent handler shadow cache extra]} obj]
           (dom-new tag props item parent handler shadow cache extra))))
  ([tag props]
   (dom-new tag props nil nil nil nil nil nil))
  ([tag props item parent handler shadow cache extra]
   (if (not (type/metaprops tag))
     (throw (ex-info "No tag available" {:tag tag})))
   (->Dom tag props item parent handler shadow cache extra)))

(defn dom-children
  "retrieves children of dom object
 
   (dom-children (dom-create :mock/pane {} [\"hello\"]))
   => {:key :children
       :children [\"hello\"]}"
  {:added "3.0"}
  ([{:keys [tag props] :as dom}] 
   (if-let [metaprops (type/metaprops tag)] 
    (let [{:keys [key none single]
           :or {key :children}} (:children metaprops)
          children (cond none ()
                         single [(get props key)] 
                         :else  (vec (get props key)))]
      {:key key :children children})
    (throw (ex-info "Cannot find tag" {:tag tag})))))

(defn children->props
  "converts children array to props
 
   (children->props [\"hello\"] :mock/label)
   => {:text \"hello\"}
 
   (children->props [\"hello\"] :mock/pane)
   => {:children [\"hello\"]}"
  {:added "3.0"}
  ([children tag] 
   (if-let [children (seq children)]
    (let [metaprops (type/metaprops tag)
          {:keys [key single none]
           :or {key :children}} (:children metaprops)]
      (cond none nil
            single (if-let [chd (first children)] {key chd})
            :else {key (vec children)})))))

(defn dom-create
  "creates an dom
 
   (dom-create :mock/pane {} [\"hello\"])
   ;; [:- :label \"hello\"]
   => dom?"
  {:added "3.0"}
  ([tag]
   (dom-create tag {}))
  ([tag props]
   (dom-create tag props nil))
  ([tag props children]
   (let [[extra props] (dom-split-props props #{"dom"} ["dom" nil])
         props  (merge props (children->props children tag))
         dom    (dom-new tag props)
         _      (props-apply (:props dom)
                            (fn [child-dom]
                              (doto child-dom
                                (mut/mutable:set :parent dom))))]
     (doto dom (mut/mutable:set :extra extra)))))

(defn dom-format
  "formats dom for printing
 
   (dom-format (dom-create :mock/label {} [\"hello\"]))
   => [:- :mock/label \"hello\"]"
  {:added "3.0"}
  ([{:keys [tag props] :as dom}] 
   (let [fmt-fn   (fn [obj] (if (dom? obj) (dom-format obj) obj))
        {:keys [key children]} (dom-children dom)
        props      (dissoc props key)
        status   (if (:item dom) :+ :-)
        children (map fmt-fn children)
        props    (std.lib.collection/map-vals (fn [p]
                                 (cond (sequential? p)
                                       (mapv fmt-fn p)

                                       :else (fmt-fn p)))
                               props)]
    (if (empty? props)
      (apply vector status tag children)
      (apply vector status tag props children)))))

(declare dom-equal?)

(defn dom-tags-equal?
  "checks if two dom nodes have the same tags
 
   (dom-tags-equal? (dom-create :mock/label)
                    (dom-create :mock/label))
   => true"
  {:added "3.0"}
  ([obj-a obj-b] 
   (and (dom? obj-a)
       (dom? obj-b)
       (= (:tag obj-a) (:tag obj-b)))))

(defn dom-props-equal?
  "checks if two dome nodes have the same props
 
   (dom-props-equal? {:a 1 :b 2}
                     {:a 1 :b 2})
   => true"
  {:added "3.0"}
  ([props-a props-b] 
   (let [ks-a    (set (keys props-a))
        ks-b    (set (keys props-b))]
    (and (= ks-a ks-b)
         (->> (map (fn [k]
                     (dom-equal? (get props-a k)
                                 (get props-b k)))
                   ks-a)
              (every? true?))))))

(defn dom-equal?
  "checks if two dom elements are equal
 
   (dom-equal? (dom-create :mock/label {} [\"hello\"])
               (dom-create :mock/label {} [\"hello\"]))
   => true"
  {:added "3.0"}
  ([obj-a obj-b] 
   (cond (dom-tags-equal? obj-a obj-b)
        (dom-props-equal? (:props obj-a) (:props obj-b))

        (and (sequential? obj-a) (sequential? obj-b))
        (and (= (count obj-a)
                (count obj-b))
             (every? true? (map dom-equal? obj-a obj-b)))
        
        :else
        (= obj-a obj-b))))

(defn dom-clone
  "creates shallow copy of a node and its data
 
   (def -a- (dom-create :mock/label))
   (def -b- (dom-clone -a-))
 
   (= -a- -b-) => false
   (dom-equal? -a- -b-) => true"
  {:added "3.0"}
  ([dom] 
   (mut/mutable:clone dom)))

(defn dom-vector?
  "checks if a vector can be a dom representation
 
   (dom-vector? [:fx/label \"\"])
   =>  true
 
   (dom-vector? ^:data [:fx/label \"\"])
   => false"
  {:added "3.0"}
  ([obj]
   (and (vector? obj)
        (let [head (first obj)]
          (or (and (keyword? head)
                   (boolean (namespace head)))
              (fn? head)))
        (-> obj meta :data not))))

(defn dom-compile
  "compiles a tree structure into a dom
 
   (-> (dom-compile [:mock/pane \"hello\"])
       dom-format)
   => [:- :mock/pane \"hello\"]
 
   (-> (dom-compile [:mock/pane
                     [:mock/label \"hello\"]
                     [:mock/label \"world\"]])
       dom-format)
   => [:- :mock/pane [:- :mock/label \"hello\"] [:- :mock/label \"world\"]]"
  {:added "3.0"}
  ([form]
   (dom-compile form dom-create))
  ([[tag props? & children :as form] create-fn]
   (let [[props children] (cond (nil? props?)
                                [{} children]
                                
                                (map? props?)
                                [props? children]
                                
                                :else
                                [{} (cons props? children)])
         elem-fn    (fn elem-fn [elem]
                      (cond (dom-vector? elem)
                            (dom-compile elem create-fn)

                            (vector? elem)
                            (mapv elem-fn elem)
                            
                            :else elem))
         props      (std.lib.collection/map-vals elem-fn props)
         children   (mapv elem-fn children)]
     (create-fn tag props children))))

(def ^:dynamic *dom-handler* 
  (fn [dom m]
    (def ^:dynamic *dom-event* m)
    (println "Unhandled Event Saved:" #'*dom-event*)
    (println "Unhandled Event Data:" m)))

(defn dom-attach
  "attaches a handler to a dom node
 
   (-> (dom-attach (dom-create :mock/label)
                   (fn [dom event] event))
       :handler)
   => fn?"
  {:added "3.0"}
  ([dom handler] 
   (doto dom (mut/mutable:set :handler handler))))

(defn dom-detach
  "detaches a handler from a dom node
 
   (-> (dom-attach (dom-create :mock/label)
                   (fn [dom event] event))
       (dom-detach)
       :handler)
   => nil"
  {:added "3.0"}
  ([dom] 
   (doto dom (mut/mutable:set :handler nil))))

(defn dom-trigger
  "triggers an event, propogating up the dom hierarchy
 
   (def -p- (promise))
 
   (-> (dom-create :mock/label)
       (dom-attach (fn [dom event] (deliver -p- event) nil))
       (dom-trigger {:a 1 :b 2}))
 
   @-p- => {:a 1, :b 2}"
  {:added "3.0"}
  ([{:keys [parent handler] :as dom} event] 
   (if handler
    (handler dom event)
    (if parent
      (dom-trigger parent event)
      (*dom-handler* dom event)))))

(defn dom-assert
  "asserts that the props are valid
   (dom-assert {:a 1 :b 2} [:a])
 
   (dom-assert {:a 1 :b 2} [:c])
   => (throws)"
  {:added "3.0"}
  ([props keys] 
   (doseq [k keys]
    (if-not (get props k)
      (throw (ex-info "Key required" {:props props :key k}))))))
