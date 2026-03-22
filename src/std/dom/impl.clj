(ns std.dom.impl
  (:require [std.dom.common :as base]
            [std.dom.item :as item]
            [std.dom.type :as type]
            [std.lib.invoke :refer [definvoke]]
            [std.lib.mutable :as mut]))

(defmulti dom-render
  "enables rendering of dom ui
 
   (-> (base/dom-create :mock/pane {} [\"hello\"])
       (dom-render)
       (base/dom-format))
   => [:+ :mock/pane \"hello\"]"
  {:added "3.0"}
  base/dom-metaclass)

(definvoke dom-render-default
  "default implementation of dom-render. throws exception"
  {:added "3.0"}
  [:method {:multi dom-render
            :val :default}]
  ([{:keys [tag props] :as dom}]
   (binding [base/*current-dom* dom]
     (let [props (-> (merge props (-> dom :extra :dom/init))
                     (base/props-apply dom-render)
                     (base/props-apply base/dom-item))
           construct (item/item-constructor tag)
           item      (construct props)
           {:keys [metatype no-check execute]} (type/metaprops tag)
           item   (if (and (= :dom/element metatype) (not no-check))
                    (let [setters (item/item-setters tag)]
                      (reduce (fn [item [k v]]
                                (let [setter (or (get setters k)
                                                 (throw (ex-info "Invalid setter: " {:tag tag :key k})))]
                                  (doto item ((:fn setter) v))))
                              item
                              props))
                    item)
           item      (if-let [execute (or execute (-> dom :extra :dom/execute))]
                       (doto item execute)
                       item)]
       (doto dom (mut/mutable:set :item item))))))

(defn dom-init
  "renders the dom element if input is dom and not rendered
 
   (dom-init 1) => 1
   
   (base/dom-format (dom-init (base/dom-create :mock/pane)))
   => [:+ :mock/pane]"
  {:added "3.0"}
  ([obj] 
   (if (and (base/dom? obj)
           (not (base/dom-item? obj)))
    (dom-render obj)
    obj)))

(defn dom-rendered
  "renders the dom and returns the actual element
 
   (dom-rendered [:mock/pane])
   => mock/mock?"
  {:added "3.0"}
  ([form] 
   (-> form
      (base/dom-compile)
      (dom-render)
      (base/dom-item))))

(defmulti dom-remove
  "provides an extensible interface removing rendered elem from dom
 
   (-> (base/dom-create :mock/pane {} [\"hello\"])
       (dom-render)
       (dom-remove)
       (base/dom-format))
   => [:- :mock/pane \"hello\"]"
  {:added "3.0"}
  base/dom-metaclass)

(definvoke dom-remove-default
  "default implementation of dom-remove."
  {:added "3.0"}
  [:method {:multi dom-remove
            :val :default}]
  ([{:keys [tag props] :as dom}]
   (let [item  (base/dom-item dom)
         _     (item/item-cleanup tag item)]
     (doto dom (mut/mutable:set :item nil)))))

(defmulti dom-replace
  "replaces one dom element with another
 
   (-> (base/dom-create :mock/pane {} [\"hello\"])
       (dom-render)
       (dom-replace (base/dom-create :mock/pane {} [\"world\"]))
       (base/dom-item)
       :props)
   => {:children [\"world\"]}"
  {:added "3.0"}
  (fn [dom-old dom-new] (base/dom-metaclass dom-old)))

(definvoke dom-replace-default
  "default implementation of dom-remove."
  {:added "3.0"}
  [:method {:multi dom-replace
            :val :default}]
  ([dom-old dom-new]
   (let [parent (:parent dom-old)]
     (cond (base/dom-item? dom-old)
           (doto dom-old
             (dom-remove)
             (mut/mutable:copy dom-new [:tag :props])
             (mut/mutable:set :parent parent)
             (dom-render))
           
           :else
           (mut/mutable:copy dom-old dom-new [:tag :props])))))
