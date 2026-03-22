(ns std.dom.local
  (:require [clojure.set]
            [std.dom.common :as base]
            [std.dom.diff :as diff]
            [std.dom.impl :as impl]
            [std.dom.react :as react]
            [std.dom.type :as type]
            [std.dom.update :as update]
            [std.lib.collection :as collection]
            [std.lib.mutable :as mut]
            [std.lib.watch :as watch]))

(defn local-dom
  "returns the local dom"
  {:added "3.0"}
  ([dom] 
   (if (-> (:tag dom) type/metaprops :class (= :local))
    dom
    (if-let [parent (:parent dom)]
      (recur parent)))))

(defn local-dom-state
  "returns the local dom state"
  {:added "3.0"}
  ([dom] 
   (if-let [local (local-dom dom)]
    (:local/state (:cache local))
    (throw (ex-info "no local dom" {:dom dom})))))

(defn local-parent
  "returns the local dom parent"
  {:added "3.0"}
  ([dom] 
   (if-let [parent (:parent dom)]
    (local-dom parent))))

(defn local-parent-state
  "returns the local dom parent state"
  {:added "3.0"}
  ([dom] 
   (if-let [local (local-parent dom)]
    (:local/state (:cache local))
    (throw (ex-info "no local parent" {:dom dom})))))

(defn dom-ops-local
  "creates setters for local properties
   
   (dom-ops-local {:a [1 2] :b 1}
                  {:a [1 2 3] :c 1})
   => [[:set :a [1 2 3] [1 2]]
       [:set :c 1 nil]
       [:delete :b 1]]"
  {:added "3.0"}
  ([props-old props-new] 
   (let [props-added   (reduce-kv (fn [out pk pv-new]
                                   (let [pv-old (get props-old pk)]
                                     (cond (= pv-old pv-new)
                                           out
                                           
                                           :else
                                           (conj out [:set pk pv-new pv-old]))))
                                 []
                                 props-new)
        props-removed (reduce-kv (fn [out pk v]
                                   (conj out [:delete pk v]))
                                 []
                                 (apply dissoc props-old (keys props-new)))
        props-changed (vec (concat props-added props-removed))]
    props-changed)))

(defn local-watch-create
  "creates a watch for local component
 
   (-> (base/dom-create :test/carrot)
       (impl/dom-render)
       (local-watch-create :color {:id :event/color}))
   => fn?"
  {:added "3.0"}
  ([dom k m] 
   (let [local (-> (:id m) namespace (= "local"))
        event (assoc m :type :change)
        watch-fn (cond local
                       (let [handler (or (:local/handler (:cache dom))
                                         base/*dom-handler*)]
                         (fn [_ _ p n]
                           (handler dom (assoc event :new n :old p))))
                       
                       :else
                       (let [events (:local/events (:cache dom))]
                         (fn [_ _ p n]
                           (swap! events conj (assoc event :new n :old p)))))]
    (-> watch-fn
        (watch/wrap-diff)
        (watch/wrap-select k)))))

(defn local-watch-add
  "adds a watch to the local component
 
   (-> (base/dom-create :test/carrot)
       (impl/dom-render)
       (local-watch-add :on/color {:id :event/color}))
   => base/dom?"
  {:added "3.0"}
  ([dom k m] 
   (let [m (if (keyword? m) {:id m} m)
        state  (:local/state (:cache dom))
        parent (local-dom dom)
        watch-fn (local-watch-create parent (keyword (name k)) (assoc m :dom dom))]
    (watch/watch:add state k watch-fn)
    dom)))

(defn local-watch-remove
  "removes a watch from the local component
 
   (-> (base/dom-create :test/carrot)
       (impl/dom-render)
       (local-watch-add :on/color {:id :event/color})
       (local-watch-remove :on/color))
   => base/dom?"
  {:added "3.0"}
  ([dom k] 
   (let [state (:local/state (:cache dom))]
    (watch/watch:remove state k)
    dom)))

(defn local-trigger-add
  "adds a trigger to the local component
 
   (-> (base/dom-create :test/carrot)
       (impl/dom-render)
       (local-trigger-add :on/top {:id :event/top}))
   => base/dom?"
  {:added "3.0"}
  ([dom k m] 
   (let [active (:local/active (:cache dom))
        key (keyword (name k))]
    (swap! active assoc key m)
    dom)))

(defn local-trigger-remove
  "removes a trigger from the local component
 
   (-> (base/dom-create :test/carrot)
       (impl/dom-render)
      (local-trigger-add :on/top {:id :event/top})
       (local-trigger-remove :on/top))"
  {:added "3.0"}
  ([dom k] 
   (let [active (:local/active (:cache dom))
        key (keyword (name k))]
    (swap! active dissoc key)
    dom)))

(defn local-split-props
  "splits props between change events and trigger events
 
   (local-split-props (type/metaprops :test/carrot)
                      {:on/top  :trigger
                       :on/color :event})
   => [{:on/color :event} {:on/top :trigger}]"
  {:added "3.0"}
  ([{:keys [props trigger]} on-props] 
   (let [watch-props   (collection/filter-keys #(contains? props (keyword (name %))) on-props)
        trigger-props (apply dissoc on-props (concat (keys watch-props)))]
    [watch-props trigger-props])))

(defn local-set
  "sets the local 
 
   (-> (base/dom-create :test/carrot {:color \"purple\"})
       (impl/dom-render)
       (local-set {:length 100} {:color \"orange\"})
       (local-dom-state)
       deref)
   => {:color \"orange\", :length 100}"
  {:added "3.0"}
  ([dom set-props del-props] 
   (let [meta (type/metaprops (:tag dom))
        [add-on-props set-props] (base/dom-split-props set-props)
        [del-on-props del-props] (base/dom-split-props del-props)
        [add-watch-props add-trig-props] (local-split-props meta add-on-props)
        [del-watch-props del-trig-props] (local-split-props meta del-on-props)
        new-props (merge set-props del-props)
        state (:local/state (:cache dom))]
    (doseq [[k m] add-watch-props]
      (local-watch-add dom k m))
    (doseq [[k _] del-watch-props]
      (local-watch-remove dom k))
    (doseq [[k m] add-trig-props]
      (local-trigger-add dom k m))
    (doseq [[k _] del-trig-props]
      (local-trigger-remove dom k))
    (if state
      (swap! state merge new-props))
    dom)))

(defn dom-send-local
  "sends events that are in :local/events"
  {:added "3.0"}
  ([dom]
   (let [events (:local/events (:cache dom))
         unsent (if events @events)
         _  (if-not (empty? unsent) (reset! events []))]
     (doseq [ev unsent]
       (base/dom-trigger dom ev))
     dom)))

(defn dom-apply-local
  "applies operations to the dom
 
   (-> (base/dom-create :test/carrot {:color \"purple\"})
       (impl/dom-render)
       (dom-apply-local (type/metaprops :test/carrot) [[:set :length 100]
                                                       [:delete :color \"purple\"]])
       (local-dom-state)
       deref)
   => {:color \"orange\", :length 100}"
  {:added "3.0"}
  ([dom {:keys [props]} ops] 
   (binding [react/*react-mute* true]
    (let [set-props (->> (keep (fn [[op k new]]
                                 (if (= op :set) [k new])) ops)
                         (into {}))
          del-props (->> (keep (fn [[op k old]]
                                 (if (= op :delete) [k (get props k)])) ops)
                         (into {}))]
      (mut/mutable:update dom :props (fn [m]
                                   (apply dissoc
                                          (merge m set-props)
                                          (keys del-props))))
      (local-set dom set-props del-props)
      (impl/dom-replace dom dom)
      (dom-send-local dom))) dom))

(defn localized-watch
  "sets up the initial watch and triggers"
  {:added "3.0"}
  ([dom props] 
   (let [meta (type/metaprops (:tag dom))
        [on-props _] (base/dom-split-props props)
        [watch-props trig-props] (local-split-props meta on-props)]
    (doseq [[k m] watch-props]
      (local-watch-add dom k m))
    (doseq [[k m] trig-props]
      (local-trigger-add dom k m)))))

(defn dom-set-local
  "function for setting local dom"
  {:added "3.0"}
  ([dom {:keys [id new key cursor transform mute] :as m}]
   (let [key    (or key (keyword (name id)))
         cursor (if key
                  (cond (keyword? key) [key]
                        :else key)
                  cursor)
         state (:local/state (:cache dom))
         m     (assoc m :state state :cursor cursor)]
     (react/dom-set-state m))))

(defn localized-handler
  "distinct handler for a given component
 
   (localized-handler (type/metaprops :test/carrot))
   => fn?"
  {:added "3.0"}
  ([{:keys [props handle trigger]}] 
   (let [props-map  (-> (clojure.set/difference (set (keys props)) (set (keys handle)))
                       (zipmap (repeat dom-set-local)))
        handle-map (merge handle props-map)]
    (fn [dom {:keys [id] :as m}]
      (let [key    (keyword (name id))]
        (or (when-let [result (get trigger key)]
              (reset! (:local/exchange (:cache dom))
                      (dissoc m :listener))
              true)
            (when-let [handler (get handle-map key)]
              (handler dom m)
              (dom-send-local dom))
            (throw (ex-info "Cannot access handle key" {:key key
                                                        :event m}))))))))

(defn localized-pre-render
  "sets up the local state pre-render
 
   (-> (doto (base/dom-create :mock/label)
         (localized-pre-render))
       :cache)
   => (contains {:local/state clojure.lang.Atom})"
  {:added "3.0"}
  ([{:keys [tag] :as dom}] 
   (let [{:keys   [props handle trigger] :as meta} (type/metaprops tag)
        initial  (merge props (select-keys (:props dom) (keys props)))
        state    (atom initial)
        handler  (localized-handler meta)
        exchange (atom nil)
        active   (atom {})]
    (add-watch exchange
               :trigger
               (fn [_ _ _ {:keys [id] :as m}]
                 (let [key (keyword (name id))
                       out (get @active key)
                       out (if (keyword? out) {:id out} out)]
                   (if out
                     (base/dom-trigger dom (merge m out))))))
    (doto dom
      (mut/mutable:update :cache
                      assoc
                      :local/events   (atom [])
                      :local/state    state
                      :local/handler  handler
                      :local/exchange exchange
                      :local/active   active)
      (localized-watch (:props dom))
      (react/reactive-pre-render)))))

(defn localized-wrap-template
  "localized wrapper function for :template"
  {:added "3.0"}
  ([template-fn] 
   (let [template-fn (react/reactive-wrap-template template-fn)]
    (fn [dom props]
      (binding [base/*local-dom* dom]
        (template-fn dom props))))))

(defn localized-pre-remove
  "localized function called on removal
 
   (-> (doto (base/dom-create :mock/label)
         (localized-pre-render)
         (localized-pre-remove))
       :cache)
   => {}"
  {:added "3.0"}
  ([dom] 
   (doto dom
    (react/reactive-pre-remove)
    (mut/mutable:update :cache dissoc
                    :local/state
                    :local/handler
                    :local/events
                    :local/exchange
                    :local/active))))

(def localized
  {:pre-render localized-pre-render
   :wrap-template localized-wrap-template
   :pre-remove localized-pre-remove})

(defn local
  "accesses current local state
 
   (binding [base/*local-dom* (doto (base/dom-create :mock/label)
                                (mut/mutable:set :cache {:local/state (atom {:hello 1})}))
             react/*react* (volatile! #{})]
     (local :hello))
   => 1"
  {:added "3.0"}
  ([key]
   (assert base/*local-dom* "Method can only be used if local dom is available")
   (local  base/*local-dom* key))
  ([dom key]
   (let [cursor (cond (keyword? key) [key]
                      :else key)
         local-state (:local/state (:cache dom))]
     (if local-state
       (react/react local-state cursor)))))
