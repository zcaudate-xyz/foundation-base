(ns std.dom.react
  (:require [std.dom.common :as base]
            [std.dom.impl :as impl]
            [std.dom.update :as update]
            [std.dom.type :as type]
            [std.lib :as h]
            [std.lib.mutable :as mut]))

(def ^:dynamic *react* nil)

(def ^:dynamic *react-mute* nil)

;; Scheduler Infrastructure
(def ^:dynamic *scheduler* nil)

(defn schedule-update
  "schedules a component update"
  {:added "4.0"}
  [dom]
  (if *scheduler*
    (swap! *scheduler* conj dom)
    (update/dom-refresh dom)))

(defn flush-updates
  "flushes all pending updates"
  {:added "4.0"}
  []
  (when *scheduler*
    (let [dirty @*scheduler*]
      (reset! *scheduler* #{})
      (doseq [dom dirty]
        (update/dom-refresh dom)))))

(defmacro with-scheduler
  "executes body with a scheduler bound"
  {:added "4.0"}
  [& body]
  `(binding [*scheduler* (atom #{})]
     (try
       ~@body
       (finally
         (flush-updates)))))

(defn reactive-pre-render
  "sets up the react key and react store
   
   (-> (doto (base/dom-create :mock/label)
         (reactive-pre-render :hello))
       :cache)
   => {:react/key :hello, :react/store #{}}"
  {:added "3.0"}
  ([dom]
   (reactive-pre-render dom (keyword (str (java.util.UUID/randomUUID)))))
  ([dom key]
   (doto dom
     (mut/mutable:update :cache assoc
                       :react/key   key
                       :react/store #{}))))

(defn reactive-wrap-template
  "reactive wrapper function for :template"
  {:added "3.0"}
  ([template-fn] 
   (fn [dom props]
    (binding [*react*     (volatile! #{})]
      (let [old-store     (:react/store (:cache dom))
            shadow        (template-fn dom props)
            new-store     @*react*
            key           (:react/key (:cache dom))
            _             (mut/mutable:update dom :cache assoc :react/store new-store)]
       (doseq [ref old-store]
          (when-not (contains? new-store ref)
            (remove-watch ref key)))
        (doseq [ref new-store]
          (when-not (contains? old-store ref)
            (add-watch ref key
                       (fn [_ _ _ _]
                         (if-not *react-mute*
                           (schedule-update dom))))))
        shadow)))))

(defn reactive-pre-remove
  "removes the react key and react store
 
   (-> (doto (base/dom-create :mock/label)
         (reactive-pre-render :hello)
         (reactive-pre-remove))
       :cache)
   => {}"
  {:added "3.0"}
  ([dom] 
   (let [key   (:react/key   (:cache dom))
        store (:react/store (:cache dom))]
    (doseq [ref store] (remove-watch ref key))
    (doto dom (mut/mutable:update :cache dissoc :react/key :react/store)))))

(def reactive
  {:pre-render    reactive-pre-render
   :wrap-template reactive-wrap-template
   :pre-remove    reactive-pre-remove})

(defn react
  "call to react, for use within component
 
   (binding [*react* (volatile! #{})]
     (react (atom {:data 1}) [:data]))
   => 1"
  {:added "3.0"}
  ([ref]
   (react ref []))
  ([ref path & more]
   (assert *react* "Method can only be used if component is reactive")
   (vswap! *react* conj ref)
   (cond-> @ref
     path (get-in (apply conj path more)))))

(defn dom-set-state
  "sets a state given function params
   
   (def -state- (atom {}))
 
   (do (dom-set-state {:state -state-
                       :key :hello
                       :new 1
                       :transform str})
       @-state-)
   => {:hello \"1\"}"
  {:added "3.0"}
  ([{:keys [state key cursor new transform mute]
     :or {transform identity}}]
   (binding [*react-mute* mute]
     (let [cursor (or cursor [key])
           data  (and new (transform new))]
       (when (not= data (get-in @state cursor))
         (swap! state assoc-in cursor data))))))
