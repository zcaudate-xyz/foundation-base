(ns std.dom.component
  (:require [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.impl :as impl]
            [std.dom.local :as local]
            [std.dom.react :as react]
            [std.dom.type :as type]
            [std.dom.update :as update]
            [std.lib.collection :as collection]
            [std.lib.function :as fn]
            [std.lib.invoke :as invoke]
            [std.lib.mutable :as mut]))

(defonce +init+ (type/metaclass-add :dom/component {:metatype :dom/component}))

(defn dom-component?
  "checks if dom is that of a component
 
   (dom-component? (base/dom-compile [:mock/pane-static]))
   => true"
  {:added "3.0"}
  ([obj] 
   (and (base/dom? obj)
       (= :dom/component (base/dom-metaclass obj)))))

(def +mixin-keys+ [:pre-render
                   :post-render
                   :pre-update
                   :post-update
                   :wrap-template
                   :pre-remove
                   :post-remove])

(defn component-options
  "prepare component options given template and mixin maps
 
   (component-options (fn [dom props]
                        (base/dom-compile [:mock/label \"hello\"]))
                      [{:pre-render (fn [dom] dom)}
                       {:post-render (fn [dom] dom)}])
   => (contains {:pre-render fn?
                 :post-render fn?
                 :template fn?})"
  {:added "3.0"}
  ([template mixins]
   (component-options template mixins +mixin-keys+))
  ([template mixins ks]
   (let [collected (->> ks
                        (reduce (fn [out k]
                                  (let [coll (keep k mixins)]
                                    (if (empty? coll)
                                      out
                                      (assoc out k coll))))
                                {}))
         {:keys [wrap-template]} collected
         template (reduce #(%2 %1) template wrap-template)
         options  (-> (collection/map-vals (fn [fns]
                                    (fn [dom] (doseq [f fns] (f dom))))
                                  (dissoc collected :wrap-template))
                      (assoc :template template))]
     options)))

(defn component-install
  "installs component given template and mixins"
  {:added "3.0"}
  ([tag class template {:keys [mixins] :as meta}] 
   (let [main   (case class
                   :static {}
                   :local  local/localized
                   :react  react/reactive)
          opts (component-options template (cons main mixins))
        opts (assoc (merge meta opts)
                    :tag tag
                    :class class)
        opts (type/metaprops-add :dom/component (merge meta opts))]
      [tag opts])))

(defmacro defcomp
  "defines a component"
  {:added "3.0"}
  ([tag doc? attr? & [bindings & body]] 
   (let [[doc attr [class params] & body]
        (fn/fn:create-args (apply vector doc? attr? bindings body))]
    `(let [~'template (fn ~@body)]
       (component-install ~tag ~class ~'template ~params)))))

(invoke/definvoke dom-render-component
  "component dom element rendering function
   
   (-> (base/dom-compile [:mock/pane-static
                          [:mock/box-static {:title \"a\" :content \"A\"}]
                          [:mock/box-static {:title \"b\" :content \"B\"}]])
       (dom-render-component)
       (base/dom-format))
   => [:+ :mock/pane-static
       [:+ :mock/box-static {:title \"a\", :content \"A\"}]
       [:+ :mock/box-static {:title \"b\", :content \"B\"}]]"
  {:added "3.0"}
  [:method {:multi impl/dom-render
            :val :dom/component}]
  ([{:keys [tag props] :as dom}]
   (let [{:keys [template pre-render post-render] :as opts}
         (type/metaprops tag)
         _         (if pre-render (pre-render dom))
         shadow    (template dom props)
         new-item  (binding [base/*current-dom* shadow]
                     (-> shadow
                         (doto (mut/mutable:set :parent dom))
                         impl/dom-render
                         base/dom-item))
         _  (mut/mutable:set dom :item new-item)
         _  (mut/mutable:set dom :shadow shadow)
         _  (if post-render (post-render dom))]
     dom)))

(defn child-components
  "collects child components of component tree"
  {:added "3.0"}
  ([dom] 
   (cond (dom-component? dom) [dom]
        
        (base/dom? dom)
        (mapcat (fn [[k v]]
                  (cond (and (sequential? v)
                             (base/dom? (first v)))
                        (mapcat child-components v)
                        
                        (base/dom? v)
                        (child-components v)
                        
                        :else nil))
                (:props dom)))))

(invoke/definvoke dom-remove-component
  "removes rendered component from dom"
  {:added "3.0"}
  [:method {:multi impl/dom-remove
            :val :dom/component}]
  ([{:keys [tag] :as dom}]
   (let [{:keys [pre-remove post-remove]} (type/metaprops tag)
         shadow (:shadow dom)]
     #_(comment (when shadow
                (if pre-remove (pre-remove dom))
                (do (impl/dom-remove shadow)
                    (doseq [child (child-components shadow)]
                      (impl/dom-remove child)))
                (mut/mutable:set shadow :parent nil)
                (if post-remove (post-remove dom)))
       (mut/mutable:set dom :item nil)
       (mut/mutable:set dom :shadow nil))
     dom)))

(invoke/definvoke dom-ops-component
  "constructs transform operations for component dom"
  {:added "3.0"}
  [:method {:multi diff/dom-ops
            :val :dom/component}]
  ([tag props-old props-new]
   (let [{:keys [class]} (type/metaprops tag)]
     (case class
       :static  (if (base/dom-props-equal? props-old props-new)
                  []
                  [[:refresh (base/dom-new tag props-new)]])
       :local     (local/dom-ops-local props-old props-new)
       [[:refresh (base/dom-new tag props-new)]]))))

(invoke/definvoke dom-apply-component
  "applies operations to component dom"
  {:added "3.0"}
  [:method {:multi update/dom-apply
            :val :dom/component}]
  ([{:keys [tag] :as dom} ops]
   (let [{:keys [class template] :as metaprops} (type/metaprops tag)]
     (cond (= class :local)
           (local/dom-apply-local dom metaprops ops)
           
           :else
           (doseq [op ops]
             (case (first op)
               :refresh (impl/dom-replace dom (second op))
               (throw (ex-info "Not Supported" {:dom dom
                                                :op op}))))))
   dom))

(invoke/definvoke dom-replace-component
  "default replace operation for components"
  {:added "3.0"}
  [:method {:multi impl/dom-replace
              :val :dom/component}]
    ([{:keys [tag] :as dom} new-dom]
     (let [{:keys [template pre-update post-update]} (type/metaprops tag)
           _   (if pre-update (pre-update dom))
           _   (mut/mutable:copy dom new-dom [:tag :props])
           shadow     (:shadow dom)
           new-shadow (-> (template dom (:props dom))
                          (mut/mutable:set :parent (:parent shadow)))
           ops (diff/dom-diff shadow new-shadow)
           _   (binding [base/*local-dom* dom]
                 (update/dom-apply shadow ops))
           _   (if post-update (post-update dom))]
       dom)))

(defn dom-state-handler
  "updates the state with given value
 
   (dom-state-handler nil {:state (atom {})
                           :cursor [:data]
                           :new \"hello\"
                           :transform keyword})
   => {:data :hello}"
  {:added "3.0"}
  ([_ {:keys [cursor state new transform] :as event
      :or {transform identity}}] 
   (if (and new
           (not= (transform new) (get-in @state cursor)))
    (swap! state assoc-in cursor (transform new)))))
