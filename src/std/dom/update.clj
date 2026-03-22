(ns std.dom.update
  (:require [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.impl :as impl]
            [std.dom.item :as item]
            [std.dom.type :as type]
            [std.lib.collection :as collection]
            [std.lib.invoke :as invoke]
            [std.lib.mutable :as mut]))

(defmulti dom-apply
  "applies operations to the dom
   (-> (base/dom-create :mock/pane {:a 1})
       (impl/dom-render)
       (dom-apply [[:set :b 2 nil]
                   [:delete :a 1]])
       (base/dom-item)
       :props)
   => {:b 2}"
  {:added "3.0"}
  (fn [dom ops] (base/dom-metaclass dom)))

(defn update-set
  "sets props given a transform
 
   (update-set {} [:set :a 1])
   => {:a 1}
 
   (update-set {} [:set :a (base/dom-compile [:mock/label \"hello\"])])
   => (contains {:a base/dom?})"
  {:added "3.0"}
  ([props [_ k new old]] 
   (if (base/dom? old) (impl/dom-remove old)) (assoc props k (impl/dom-init new))))

(defn update-list-insert
  "updates the list by inserting values
 
   (update-list-insert []
                       [:list-insert 0 [1 2 3 4]])
   => [1 2 3 4]"
  {:added "3.0"}
  ([list [_ i items]] 
   (apply collection/insert-at list i (map impl/dom-init items))))

(defn update-list-remove
  "updates the list by deleting values
 
   (update-list-remove [1 2 3 4]
                       [:list-remove 1 2])
   => [1 4]"
  {:added "3.0"}
  ([list [_ i number]] 
   (doseq [old  (->> list (drop i) (take number))]
    (if (base/dom? old) (impl/dom-remove old))) (collection/remove-at list i number)))

(defn update-list-update
  "updates :update changes to list
 
   (update-list-update [(-> (base/dom-compile [:mock/label])
                            (impl/dom-render))]
                       [:update 0 [[:set :text \"hello\"]]])
   => (contains [#(= \"hello\" (:text (:props %)))])"
  {:added "3.0"}
  ([list [_ k ops]] 
   (let [sdom (get list k)]
    (assoc list k (dom-apply sdom ops)))))

(defn update-list-append
  "updates :append changes to list
   
   (update-list-append [\"A\"]
                       [:list-append \"B\"])
   => [\"A\" \"B\"]"
  {:added "3.0"}
  ([list [_ new]] 
   (conj list (impl/dom-init new))))

(defn update-list-drop
  "updates :drop changes to list
 
   (update-list-drop [\"A\" \"B\"]
                     [:list-drop \"B\"])
   => [\"A\"]"
  {:added "3.0"}
  ([list [_ old]] 
   (if (base/dom? old) (impl/dom-remove old)) (vec (butlast list))))

(defn update-list
  "updates a list within props
 
   (update-list (impl/dom-render (base/dom-compile [:mock/pane \"a\"]))
                :items
                [\"a\"]
                [[:list-set 0 \"A\" \"a\"]])
   => [\"A\"]"
  {:added "3.0"}
  ([dom k list ops] 
   (let [results (reduce (fn [list [action :as op]]
                          (case action
                            :list-set    (update-set list op)
                            :list-insert (update-list-insert list op)
                            :list-remove (update-list-remove list op)
                            :list-update (update-list-update list op)
                            :list-append (update-list-append list op)
                            :list-drop   (update-list-drop list op)))
                        (vec list)
                        ops)
        item  (base/dom-item dom)
        vlist (map (fn [obj] (cond-> obj (base/dom? obj) base/dom-item)) results)
        item  (item/item-set-list (:tag dom) item k vlist)
        _     (doto dom (mut/mutable:set :item item))]
    results)))

(defn update-props-delete
  "updates props given an operation
   
   (update-props-delete {:text \"hello\"}
                        [:delete :text \"hello\"])
   => {}"
  {:added "3.0"}
  ([props [_ k old]] 
   (if (base/dom? old) (impl/dom-remove old)) (dissoc props k)))

(defn update-props-update
  "updates :update changes to props
 
   (update-props-update (doto (base/dom-compile [:mock/pane]) impl/dom-render)
                        {:items [\"A\"]}
                        [:update :items [[:list-set 0 \"B\"]]])
   => {:items [\"B\"]}"
  {:added "3.0"}
  ([dom props [_ k ops :as op]] 
   (let [pval (get props k)
        new-pval (cond (base/dom? pval)
                       (dom-apply pval ops)
                       
                       (sequential? pval)
                       (update-list dom k pval ops)
                       
                       :else
                       (throw (ex-info "Not Supported" {:dom dom
                                                        :val pval
                                                        :op op})))]
    (assoc props k new-pval))))

(defn update-props
  "updates props of doms
 
   (update-props (base/dom-compile [:mock/label]) {} [[:set :text \"hello\"]])
   => {:text \"hello\"}"
  {:added "3.0"}
  ([dom props ops] 
   (reduce (fn [props [action _ _ :as op]]
            (case action
              :set     (update-set props op)
              :delete  (update-props-delete props op)
              :update  (update-props-update dom props op) 
              (throw (ex-info "Not Supported" {:props props
                                               :op op}))))
          props
          ops)))

(invoke/definvoke dom-apply-default
  "default function for dom-apply
   
   (-> (dom-apply-default (doto (base/dom-compile [:mock/pane {:items [\"a\"]}])
                            (impl/dom-render))
                          [[:update :items [[:list-set 0 \"A\"]]]])
       (base/dom-item)
       (mock/mock-format))
   => [:mock/pane {:items [\"A\"]}]"
  {:added "3.0"}
  [:method {:multi dom-apply
            :val :default}]
  ([dom ops]
   (cond (-> ops first first (= :replace))
         (impl/dom-replace dom (-> ops first second))
         
         :else
         (let [item      (base/dom-item dom)    
               new-props (update-props dom (:props dom) ops)
               item      (item/item-update (:tag dom) item ops)]
           (doto dom 
             (mut/mutable:set {:props new-props :item item}))))))

(defn dom-update
  "updates current dom given new dom
   
   (-> (doto (base/dom-compile [:mock/pane {:a 1}
                                [:mock/pane {:b 2}]
                                [:mock/pane {:c 3}]])
         (impl/dom-render)
         (dom-update (base/dom-compile [:mock/pane {:a 1}
                                        [:mock/pane {:b 2}]
                                        [:mock/pane {:c 4}]])))
       (base/dom-item)
       str read-string)
   => [:mock/pane {:a 1} [:mock/pane {:b 2}] [:mock/pane {:c 4}]]"
  {:added "3.0"}
  ([dom new-dom] 
   (binding [base/*current-dom* dom]
    (let [ops (diff/dom-diff dom new-dom)]
      (dom-apply dom ops)))))

(defn dom-refresh
  "refreshes current dom, used for components
 
   (-> (base/dom-compile [:mock/pane {:b 2}])
       (impl/dom-render)
       (dom-refresh)
       str read-string)
   => [:+ :mock/pane {:b 2}]"
  {:added "3.0"}
  ([dom] 
   (dom-update dom (base/dom-new (:tag dom) (:props dom)))))
