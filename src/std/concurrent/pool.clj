(ns std.concurrent.pool
  (:require [std.concurrent.executor :as e]
            [std.concurrent.thread :as t]
            [std.lib.atom]
            [std.lib.collection]
            [std.lib.component :as component]
            [std.lib.foundation]
            [std.lib.future :as f]
            [std.lib.impl :refer [defimpl]]
            [std.lib.time]
            [std.protocol.component :as protocol.component]
            [std.protocol.track :as protocol.track]))

(def ^:dynamic *current* nil)

(def ^:dynamic *dispose* nil)

(def ^:dynamic *stop-fn* e/exec:shutdown-now)

(def ^:dynamic *defaults*
  {:size 10
   :keep-alive 10000
   :poll 20000})

(def +opts+
  [:size :max :keep-alive :poll])

(defn resource-info
  "returns info about the pool resource
 
   (->  (pool-resource \"hello\" |pool|)
        (resource-info :full))
   => (contains {:total number?,
                 :busy 0.0,
                 :count 0,
                 :utilization 0.0,
                 :duration 0})"
  {:added "3.0"}
  ([res]
   (resource-info res :basic))
  ([{:keys [create-time update-time busy used status]} mode]
   (let [current (or *current* (std.lib.time/time-ns))
         total-time   (- current create-time)
         busy-time  (case status
                      :busy (+ busy (- current update-time))
                      :idle busy)]
     (cond-> {:total       total-time
              :busy        busy-time}
       (= mode :full) (assoc :count       used
                             :utilization (float (/ busy-time (+ total-time 1)))
                             :duration    (long (/ busy-time (+ used 1))))))))

(defn resource-string
  "returns a string describing the resource"
  {:added "3.0"}
  ([res]
   (str "#res" (-> (resource-info res :full)
                   (update :total std.lib.time/format-ns)
                   (update :duration std.lib.time/format-ns)))))

(defimpl PoolResource [id thread-id object status create-time update-time busy used]
  :stream resource-string)

(defn pool-resource
  "creates a pool resource
 
   (pool-resource \"hello\"
                  {:resource {:create (fn [] '<RESOURCE>)}})
   => (contains {:id \"hello\",
                 :object '<RESOURCE>
                 :status :idle,
                 :create-time number?
                 :update-time number?
                 :busy 0.0, :used 0})"
  {:added "3.0"}
  ([id {:keys [resource] :as pool}]
   (let [{:keys [create start]
          :or {start identity}} resource
         current (std.lib.time/time-ns)]
     (PoolResource. id
                    nil
                    (-> (create)
                        (start))
                    :idle
                    current
                    current
                    0.0
                    0))))

(defn pool:acquire
  "acquires a resource from the pool
 
   (pool:acquire |pool|)
   => (contains [string? '<RESOURCE>])"
  {:added "3.0"}
  ([{:keys [state resource] :as pool}]
   (let [{:keys [thread-local]} resource
         resource-fn (fn [resource thread-id]
                       (-> resource
                           (update :used inc)
                           (assoc  :update-time (std.lib.time/time-ns)
                                   :status :busy
                                   :thread-id thread-id)))
         update-fn   (fn [m thread-id id resource]
                       (-> m
                           (update :busy assoc id
                                   (resource-fn resource thread-id))
                           (update-in [:stats :acquire] inc)
                           (update-in [:lookup thread-id]
                                      (fnil #(conj % id) #{}))))
         acquire-fn  (fn [{:keys [busy idle options lookup] :as m}]
                       (let [{:keys [max]} options
                             [id resource] (first idle)
                             thread-id (t/thread:id)
                             local-id (first (get lookup thread-id))]
                         (cond (and thread-local local-id)
                               [[:success local-id
                                 (f/completed (:object (get busy local-id)))]
                                m]

                               id
                               [[:success id
                                 (f/completed (:object resource))]
                                (-> (update-fn m thread-id id resource)
                                    (update :idle dissoc id))]

                               (and max
                                    (<= max
                                        (+ (count busy)
                                           (count idle))))
                               [[:reject nil (ex-info "Maximum limit reached" {:max max})]
                                (update-in m [:stats :reject] inc)]

                               :else
                               [[:success (std.lib.foundation/sid) (f/incomplete)]
                                (update-in m [:stats :create] inc)])))
         [status id boxed] (std.lib.atom/swap-return! state acquire-fn)
         allocate-fn (fn [state id boxed]
                       (f/fulfil boxed
                                 (fn []
                                   (let [resource (pool-resource id pool)]
                                     (swap! state (fn [m] (update-fn m (t/thread:id) id resource)))
                                     (:object resource)))))
         object  (if (f/future? boxed)
                   (do (if (not (f/future:complete? boxed))
                         (allocate-fn state id boxed))
                       @boxed)
                   boxed)]
     (case status
       :success [id object]
       :reject  (throw object)))))

(defn dispose-fn
  "helper function for `dispose` and `cleanup`"
  {:added "3.0"}
  ([m id]
   (dispose-fn m id nil))
  ([m id stop-fn]
   (let [update-fn   (fn [m id resource]
                       (let [current (std.lib.time/time-ns)
                             {:keys [create-time busy]} resource]
                         (-> m
                             (update :idle dissoc id)
                             (update-in [:stats :total-time] + (- current create-time))
                             (update-in [:stats :busy-time] + busy)
                             (update-in [:stats :dispose] inc))))
         {:keys [idle options]} m
         resource (get idle id)
         stop-fn (or stop-fn identity)]
     (if resource
       (let [_ (stop-fn (:object resource))]
         (update-fn m id resource))
       m))))

(defn pool:dispose
  "disposes an idle object
 
   (pool:dispose |pool| (first (keys (pool:resources:idle |pool|))))"
  {:added "3.0"}
  ([{:keys [state resource] :as pool} id]
   (swap! state dispose-fn id (:stop resource)) id))

(defn pool:dispose-over
  "disposes if idle and busy are over size limit"
  {:added "3.0"}
  ([{:keys [state resource] :as pool} id]
   (std.lib.atom/swap-return! state (fn [{:keys [idle options] :as m}]
                           (if (get idle id)
                             [id  (dispose-fn m id (:stop resource))]
                             [nil m])))))

(defn pool:release
  "releases a resource back to the pool
 
   (let [[id _] (pool:acquire |pool|)]
     (pool:release |pool| id))
   => string?"
  {:added "3.0"}
  ([pool id]
   (pool:release pool id false))
  ([{:keys [state] :as pool} id dispose?]
   (let [resource-fn (fn [{:keys [update-time busy] :as resource}]
                       (let [current (std.lib.time/time-ns)
                             diff (- current update-time)]
                         (assoc resource
                                :update-time current
                                :busy (+ busy diff)
                                :status :idle
                                :thread-id nil)))
         update-fn   (fn [m thread-id id resource]
                       (-> m
                           (update :idle assoc id (resource-fn resource))
                           (update :busy dissoc id)
                           (update-in [:stats :release] inc)
                           (update :lookup
                                   (fn [lu]
                                     (let [nentries (disj (get lu thread-id) id)]
                                       (if (empty? nentries)
                                         (dissoc lu thread-id)
                                         (assoc  lu thread-id nentries)))))))
         release-fn  (fn [{:keys [busy options] :as m}]
                       (let [{:keys [thread-id] :as resource} (get busy id)]
                         (cond (nil? resource)
                               [[:error (ex-info "Cannot find id" {:id id})]
                                m]

                               :else
                               [[:success id]
                                (update-fn m thread-id id resource)])))
         [[status id] {:keys [idle busy options executor]}] (std.lib.atom/swap-return! state release-fn true)
         _  (if dispose? (pool:dispose pool id))
         _  (if (and executor
                     (< (:size options)
                        (+ (count idle)
                           (count busy))))
              (f/future:run (fn []
                              (pool:dispose-over pool id))
                            {:pool executor
                             :delay (:keep-alive options)}))]
     (case status
       :success id
       :error   nil))))

(defn pool:cleanup
  "runs cleanup on the pool"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (let [current (std.lib.time/time-ns)
         {:keys [stop]} (:resource pool)
         {:keys [health] :or {health (constantly {:status :ok})}} (:resource pool)
         {:keys [idle]} @state
         dead-ids (keep (fn [[id resource]]
                          (if-not (-> (health resource)
                                      (:status)
                                      (= :ok))
                            id))
                        idle)
         dispose (fn [m id] (dispose-fn m id stop))
         _    (swap! state (fn [m] (reduce dispose  m dead-ids)))
         cleanup-fn  (fn [{:keys [idle options] :as m}]
                       (let [{:keys [size keep-alive]} options
                             over (- (count idle) size)
                             [ids m] (cond (pos? over)
                                           (let [ids (->> idle
                                                          (keep (fn [[id {:keys [update-time]}]]
                                                                  (if (< (+ update-time keep-alive)
                                                                         current)
                                                                    id)))
                                                          (take over))]
                                             [ids (reduce dispose m ids)])

                                           :else
                                           [[] m])]
                         [ids (update-in m [:stats :cleanup] inc)]))
         ids  (std.lib.atom/swap-return! state cleanup-fn)]
     ids)))

(defn pool-handler
  "creates a handler loop for cleanup"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (fn pool-handler-fn []
     (let [{:keys [executor options]} @state]
       (pool:cleanup pool)
       (Thread/sleep (long (:poll options)))
       (if executor
         (e/submit executor pool-handler-fn))))))

(defn pool:started?
  "checks if pool has started"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (let [{:keys [executor]} @state]
     (boolean (and executor (not (e/exec:shutdown? executor)))))))

(defn pool:stopped?
  "checks if pool has stopped"
  {:added "3.0"}
  ([pool]
   (not (pool:started? pool))))

(defn pool:start
  "starts the pool"
  {:added "3.0"}
  ([{:keys [state resource poll] :as pool}]
   (if-not (pool:started? pool)
     (swap! state (fn [{:keys [options] :as m}]
                    (let [{:keys [size]} options
                          idle    (->> (range (long (* size (or (:initial resource)
                                                                0))))
                                       (map (fn [_]
                                              (let [id (std.lib.foundation/sid)]
                                                [id (pool-resource id pool)])))
                                       (into {}))
                          executor (e/executor:pool 2 2 1000)
                          _  (e/submit executor (pool-handler pool))]
                      (assoc m :executor executor :idle idle)))))
   pool))

(defn pool:stop
  "stops the pool
 
   (pool:stop |pool|)
   => pool:stopped?"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (if (pool:started? pool)
     (let [_ (swap! state (fn [{:keys [executor] :as m}]
                            (*stop-fn* executor)
                            (dissoc m :executor)))
           busy-ids (keys (:busy @state))
           _ (doseq [id busy-ids]
               (pool:release pool id))
           idle-ids (keys (:idle @state))
           _  (doseq [id idle-ids]
                (pool:dispose pool id))]))
   pool))

(defn pool:kill
  "kills the pool
 
   (pool:kill |pool|)
   => pool:stopped?"
  {:added "3.0"}
  ([pool]
   (binding [*stop-fn* e/exec:shutdown-now]
     (pool:stop pool))))

(defn pool:info
  "returns information about the pool
 
   (pool:info |pool|)
   => (contains-in {:running true,
                    :idle 2, :busy 0,
                    :resource {:count 0, :total number?
                               :busy 0.0, :utilization 0.0,
                               :duration 0}})"
  {:added "3.0"}
  ([pool]
   (pool:info pool :default))
  ([{:keys [state] :as pool} _]
   (let [{:keys [stats idle busy executor options]} @state
         {:keys [total-time busy-time acquire]} stats
         util (binding [*current* (std.lib.time/time-ns)]
                (->> (concat (vals busy) (vals idle))
                     (mapv resource-info)
                     (apply merge-with + {:total (or total-time 0)
                                          :busy (or busy-time 0)})
                     (merge {:count (or acquire 0)})))
         util (assoc util
                     :utilization (float (/ (:busy util) (+ (:total util) 1)))
                     :duration    (long  (/ (:busy util) (+ (:count util) 1))))]
     {:running (boolean (and executor (not (e/exec:shutdown? executor))))
      :idle (count idle)
      :busy (count busy)
      :resource util})))

(defn- pool-props-fn
  ([key]
   (fn
     ([{:keys [state]}]
      (get-in @state [:options key]))
     ([{:keys [state]} input]
      (swap! state assoc-in [:options key] input)))))

(def pool:props:size (pool-props-fn :size))
(def pool:props:max (pool-props-fn :max))
(def pool:props:keep-alive (pool-props-fn :keep-alive))
(def pool:props:poll (pool-props-fn :poll))

(defn pool:props
  "gets props for the pool
 
   (keys (pool:props |pool|))
   => (contains [:size :max :keep-alive :poll])"
  {:added "3.0"}
  ([pool]
   {:size {:get pool:props:size
           :set pool:props:size}
    :max  {:get pool:props:max
           :set pool:props:max}
    :keep-alive {:get pool:props:keep-alive
                 :set pool:props:keep-alive}
    :poll  {:get pool:props:poll
            :set pool:props:poll}}))

(defn pool:health
  "returns health of the pool
 
   (pool:health |pool|)
   => {:status :ok}"
  {:added "3.0"}
  ([pool]
   (if (pool:started? pool)
     {:status :ok}
     {:status :not-healthy})))

(defn pool:track-path
  "gets props for the pool
 
   (pool:track-path |pool|)
   => [:raw :pool]"
  {:added "3.0"}
  ([pool]
   (or (get-in pool [:track :path])
       [:raw :pool])))

(defn- pool-string
  ([{:keys [tag] :as pool}]
   (let [str-fn (comp std.lib.time/format-ns long)]
     (str "#" (or tag "pool") " "
          (-> (pool:info pool)
              (update :resource dissoc :total :busy)
              (update-in [:resource :duration] str-fn))))))

(defimpl Pool [size max state]
  :prefix "pool:"
  :string pool-string
  :protocols [std.protocol.track/ITrack
              protocol.component/IComponent
              :body {-remote?  false}])

(defn pool?
  "checks that object is a pool
 
   (pool? |pool|)
   => true"
  {:added "3.0"}
  ([obj]
   (instance? Pool obj)))

(defn pool:create
  "creates an initial pool
 
   (pool:create {:size 5
                 :max 8
                :keep-alive 10000
                 :poll 20000
                 :resource {:create (fn [] '<RESOURCE>)
                            :initial 0.3
                            :thread-local true}})"
  {:added "3.0"}
  ([m]
   (-> (assoc (apply dissoc m +opts+)
              :state (atom {:stats {:create  0
                                    :reject  0
                                    :dispose 0
                                    :acquire 0
                                    :release 0
                                    :history []
                                    :cleanup 0
                                    :total-time 0.0
                                    :busy-time  0.0}
                            :busy    {}
                            :idle    {}
                            :lookup  {}
                            :options (merge *defaults* (select-keys m +opts+))}))
       (map->Pool))))

(defn pool
  "creates and starts the pool
 
   (def -p- (pool {:size 2
                   :max 10
                  :keep-alive 10000
                   :poll 20000
                   :resource {:create (fn [] (rand))
                              :initial 0.3
                              :thread-local true}}))"
  {:added "3.0"}
  ([{:keys [size max keep-alive poll resource] :as m}]
   (-> (pool:create m)
       (pool:start))))

(defn pool:resources:thread
  "returns acquired resources for a given thread"
  {:added "3.0"}
  ([pool]
   (pool:resources:thread pool (t/thread:current)))
  ([{:keys [state] :as pool} thread]
   (let [thread-id (t/thread:id thread)
         {:keys [busy lookup]} @state
         ids (get lookup thread-id)]
     (std.lib.collection/map-juxt [identity (comp :object #(get busy %))] ids))))

(defn pool:resources:busy
  "returns all the busy resources"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (std.lib.collection/map-vals :object (:busy @state))))

(defn pool:resources:idle
  "returns all the idle resources"
  {:added "3.0"}
  ([{:keys [state] :as pool}]
   (std.lib.collection/map-vals :object (:idle @state))))

(defn pool:dispose:mark
  "marks the current resource for dispose"
  {:added "3.0"}
  ([]
   (vreset! *dispose* true)))

(defn pool:dispose:unmark
  "unmarks the current resource for dispose"
  {:added "3.0"}
  ([]
   (vreset! *dispose* nil)))

(defn wrap-pool-resource
  "wraps a function to operate on a pool resource"
  {:added "3.0"}
  ([f pool]
   (fn [& args]
     (let [[id obj] (pool:acquire pool)
           [out exception dispose?]
           (binding [*dispose* (volatile! nil)]
             (try
               [(apply f obj args) nil @*dispose*])
             (catch Throwable t
               [nil t @*dispose*]))]
       (pool:release pool id dispose?)
       (if exception
         (throw exception)
         out)))))

(defmacro pool:with-resource
  "takes an object from the pool, performs operation then returns it"
  {:added "3.0" :style/indent 1}
  ([[obj pool] & body]
   `((wrap-pool-resource
      (fn [obj#]
        (let [~obj obj#]
          ~@body))
      ~pool))))
