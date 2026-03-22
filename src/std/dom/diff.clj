(ns std.dom.diff
  (:require [clojure.set]
            [std.dom.common :as base]
            [std.dom.type :as type]
            [std.lib.collection :as collection]
            [std.lib.diff.seq :as diff.seq]
            [std.lib.invoke :as invoke]))

(defmulti dom-ops
  "converts a set of props into operations
 
   (dom-ops :mock/pane
             {:a 1}
             {:b 2})
   => [[:set :b 2 nil]
       [:delete :a 1]]"
  {:added "3.0"}
  (fn [tag props-old props-new] (:metaclass (type/metaprops tag))))

(defn diff-list-element
  "diffs for elements within a list
 
   (diff-list-element 0 1 2)
   => [:list-set 0 2 1]"
  {:added "3.0"}
  ([i x-old x-new] 
   (cond (base/dom-tags-equal? x-old x-new)
        (cond (base/value? x-old)
              (if (base/dom-props-equal? (:props x-old) (:props x-new))
                nil
                [:list-set i x-new x-old])
              
              :else
              (let [result (dom-ops (:tag x-old)
                                    (:props x-old)
                                    (:props x-new))]
                (if-not (empty? result)
                  [:list-update i result])))
        
        (= x-old x-new) nil
        
        :else
        [:list-set i x-new x-old])))

(defn diff-list-elements
  "diff elements in array, limit to shortest array
 
   (diff-list-elements [:a :b :c] [:b])
   => [[:list-set 0 :b :a]]
 
   (diff-list-elements [(base/dom-create :mock/label {} [\"hello\"])]
                       [(base/dom-create :mock/label {} [\"world\"])])
   => [[:list-update 0 [[:set :text \"world\" \"hello\"]]]]"
  {:added "3.0"}
  ([list-old list-new] 
   (->> (map diff-list-element (range) list-old list-new)
       (filter identity))))

(defn diff-list-dom
  "simplified list comparison using :dom/key
 
   (diff-list-dom [(base/dom-compile [:mock/pane {:dom/key 3}])
                   (base/dom-compile [:mock/pane {:dom/key 1}])
                   (base/dom-compile [:mock/pane {:dom/key 2}])
                   (base/dom-compile [:mock/pane {:dom/key 4}])]
                  [(base/dom-compile [:mock/pane {:dom/key 1}])
                   (base/dom-compile [:mock/pane {:dom/key 2}])])
   => [[:list-remove 0 1] [:list-remove 2 1]]"
  {:added "3.0"}
  ([list-old list-new] 
   (let [key-fn   (fn [dom] (-> dom :extra :dom/key))
        keys-old (map key-fn list-old)
        keys-new (map key-fn list-new)
         _        (assert (and (collection/deduped? keys-old)
                               (collection/deduped? keys-new)))
        added    (clojure.set/difference (set keys-new)
                                 (set keys-old))
        lu       (merge (zipmap keys-old list-old)
                        (select-keys (zipmap keys-new list-new) added))
        moves    (->> (diff.seq/diff keys-old keys-new)
                      second
                      (mapv (fn [[op index val]]
                              (case op
                                :- [:list-remove index val]
                                :+ [:list-insert index (mapv lu val)]))))
        list-moved (mapv lu keys-new) 
        updates (diff-list-elements list-moved list-new)]
    (vec (concat moves updates)))))

(defn diff-list
  "constructs diffs for a lists
 
   (diff-list [:a :b :c] [:b])
   => [[:list-remove 0 1]
       [:list-remove 1 1]]"
  {:added "3.0"}
  ([list-old list-new] 
   (let [elem (first list-old)]
    (cond (base/dom? elem)
          (if (-> elem :extra :dom/key)
            (diff-list-dom list-old list-new)
            (let [cnt-old   (count list-old)
                  cnt-new   (count list-new)
                  diff-tail (cond (= cnt-new cnt-old) nil
                                  (> cnt-new cnt-old) (map (fn [x] [:list-append x])
                                                           (drop cnt-old list-new))
                                  (< cnt-new cnt-old) (map (fn [x] [:list-drop x])
                                                           (reverse (drop cnt-new list-old))))
                  diff-head (diff-list-elements list-old list-new)]
              (vec (concat diff-head diff-tail))))
          
          :else
          (if (= list-old list-new)
            []
            (->> (diff.seq/diff list-old list-new)
                 second
                 (mapv (fn [[op index val]]
                         (case op
                           :- [:list-remove index val]
                           :+ [:list-insert index val])))))))))

(defn diff-props-element
  "diffs for elements within a props map
   
   (diff-props-element :text \"hello\" \"world\")
   => [:set :text \"world\" \"hello\"]"
  {:added "3.0"}
  ([pk pv-old pv-new] 
   (cond (base/dom-tags-equal? pv-old pv-new)
        (cond (base/value? pv-old)
              (if (base/dom-props-equal? (:props pv-old) (:props pv-new))
                nil
                [:set pk pv-new pv-old])
              
              :else
              (let [result (dom-ops (:tag pv-old)
                                    (:props pv-old)
                                    (:props pv-new))]
                (if-not (empty? result)
                  [:update pk result]
                  nil)))
        
        (and (sequential? pv-new)
             (sequential? pv-old))
        (if-let [res (diff-list pv-old pv-new)]
          [:update pk res])
        
        (= pv-old pv-new) nil
        
        :else
        [:set pk pv-new pv-old])))

(defn diff-props
  "constructs diff for a set of props
 
   (diff-props {:top (base/dom-create :mock/label {} [\"hello\"])}
               {:top (base/dom-create :mock/label {} [\"world\"])})
   => [[:update :top [[:set :text \"world\" \"hello\"]]]]"
  {:added "3.0"}
  ([props-old props-new] 
   (let [props-added   (keep (fn [[pk pv-new]]
                              (let [pv-old (get props-old pk)]
                                (diff-props-element pk pv-old pv-new)))
                            props-new)
        props-removed (reduce (fn [out [pk v]]
                                (conj out [:delete pk v]))
                              []
                              (apply dissoc props-old (keys props-new)))
        props-changed (vec (concat props-added props-removed))]
    props-changed)))

(invoke/definvoke dom-ops-default
  "default implementation for diff-ops
   
   (dom-ops-default :mock/pane
                    {:a 1}
                    {:b 2})
   => [[:set :b 2 nil]
       [:delete :a 1]]"
  {:added "3.0"}
  [:method {:multi dom-ops
            :val :default}]
  ([tag props-old props-new]
   (cond (base/dom-props-equal? props-old props-new) []
         
         :else
         (diff-props props-old props-new))))

(defn dom-diff
  "returns ops for dom transform
 
   (dom-diff (base/dom-create :mock/pane {:hello 1} [:a :b :c])
             (base/dom-create :mock/pane {:hello 2} [:a :B :c]))
   => [[:set :hello 2 1]
       [:update :children [[:list-remove 1 1]
                           [:list-insert 1 [:B]]]]]"
  {:added "3.0"}
  ([old new] 
   (let [ops (cond (base/dom-tags-equal? old new)
                  (cond (base/value? old)
                        (if (base/dom-props-equal? (:props old) (:props new))
                          []
                          [[:replace new old]])

                        :else
                        (dom-ops (:tag old)
                                  (:props old)
                                  (:props new)))
                  
                  :else
                  [[:replace new old]])]
    ops)))
