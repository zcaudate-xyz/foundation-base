(ns std.dispatch.hub
  (:require [std.protocol.dispatch :as protocol.dispatch]
            [std.protocol.component :as protocol.component]
            [std.concurrent :as cc]
            [std.dispatch.common :as common]
            [std.dispatch.hooks :as hooks]
            [std.dispatch.debounce :as debounce]
            [std.lib :as h :refer [defimpl]])
  (:import (com.google.common.cache Cache CacheBuilder)
           (java.util.concurrent TimeUnit)))

(def +defaults+ {:group-fn (fn [_ entry] entry)
                 :interval  1000
                 :max-batch 1000
                 :pool      {:size 4}
                 :cache     {:size 10000
                             :duration 10
                             :unit :minutes}})

(defn process-hub
  "activates on debounce submit hit"
  {:added "3.0"}
  ([{:keys [handler options] :as dispatch} group hub]
   (let [{:keys [max-batch]} (:hub options)]
     (cc/hub:process (fn [entries]
                       (let [_ (hooks/on-process-bulk dispatch entries)
                             _ (hooks/on-batch dispatch)
                             results (handler dispatch entries)
                             _ (hooks/on-complete-bulk dispatch entries results)]
                         results))
                     hub
                     max-batch))))

(defn put-hub
  "puts an entry into the group hubs"
  {:added "3.0"}
  ([{:keys [runtime options] :as dispatch} group entry]
   (let [^Cache groups (:groups runtime)
         hub (.get groups group (reify java.util.concurrent.Callable
                                  (call [_] (cc/hub:new))))]
     (cc/hub:add-entries hub [entry]))))

(defn create-hub-handler
  "creates the hub handler"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (let [^Cache groups (:groups runtime)]
     (fn [_ group]
       (try
         (let [hub (.getIfPresent groups group)]
           (if hub
             (do (hooks/on-poll dispatch group)
                 (process-hub dispatch group hub))))
         (catch Throwable t
           (.printStackTrace t)))))))

(defn update-debounce-handler!
  "updates the debounce handler"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (let [{:keys [debouncer]} runtime]
     (vswap! debouncer assoc :handler (create-hub-handler dispatch)))))

(defn create-debounce
  "creates the debounce executor"
  {:added "3.0"}
  ([{:keys [options] :as dispatch}]
   (let [{:keys [hub]} options
         {:keys [interval delay pool]} hub
         m  {:type :debounce
             :handler (create-hub-handler dispatch)
             :options (-> (dissoc options :hub)
                          (assoc :pool pool)
                          (assoc :counter false
                                 :debounce {:strategy :notify
                                            :group-fn (fn [_ group] group)
                                            :delay delay
                                            :interval interval}))}]
     (debounce/create-dispatch m))))

(defn start-dispatch
  "starts the hub executor"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (let [debouncer (->> (create-debounce dispatch)
                        (debounce/start-dispatch))]
     (vreset! (:debouncer runtime) debouncer)
     (hooks/on-startup dispatch)
     dispatch)))

(defn stop-dispatch
  "stops the hub executor"
  {:added "3.0"}
  ([{:keys [runtime hooks] :as dispatch}]
   (let [_ (common/stop-dispatch @(:debouncer runtime)
                                 (fn [_]
                                   (hooks/on-shutdown dispatch)))]
     (-> dispatch
         (update :runtime dissoc :hub)))))

(defn kill-dispatch
  "kills the hub executor"
  {:added "3.0"}
  ([{:keys [runtime hooks] :as dispatch}]
   (let [_ (common/kill-dispatch @(:debouncer runtime)
                                 (fn [_]
                                   (hooks/on-shutdown dispatch)))]
     (-> dispatch
         (update :runtime dissoc :groups)))))

(defn submit-dispatch
  "submits to the hub executor"
  {:added "3.0"}
  ([{:keys [options runtime] :as dispatch} entry]
   (let [_ (hooks/on-submit dispatch entry)
         {:keys [group-fn]} (:hub options)
         group  (group-fn dispatch entry)
         [ticket start count] (put-hub dispatch group entry)
         _ (hooks/on-queued dispatch entry)
         _ (debounce/submit-dispatch @(:debouncer runtime) group)]
     [ticket start count])))

(defn info-dispatch
  "returns dispatch info"
  {:added "3.0"}
  ([dispatch _]
   (let [debouncer (-> dispatch
                       :runtime
                       :debouncer
                       deref)
         executor  (if debouncer
                     (-> debouncer
                         :runtime
                         :executor
                         deref))]
     (common/info-dispatch debouncer))))

(defn- started?-dispatch
  ([dispatch]
   (if-let [debouncer @(-> dispatch :runtime :debouncer)]
     (common/started?-dispatch debouncer))))

(defn- stopped?-dispatch
  ([dispatch]
   (not (started?-dispatch dispatch))))

(defn- health-dispatch
  ([dispatch]
   (if-let [debouncer @(-> dispatch :runtime :debouncer)]
     (common/health-dispatch debouncer)
     {:status :error})))

(defn- props-dispatch
  ([dispatch]
   (if-let [debouncer @(-> dispatch :runtime :debouncer)]
     (common/props-dispatch debouncer))))

(defimpl HubDispatch [type runtime handler]
  :suffix "-dispatch"
  :string common/to-string
  :protocols  [std.protocol.component/IComponent
               protocol.component/IComponentProps
               protocol.component/IComponentQuery
               :body   {-remote?  false
                        -track-path [:dispatch :hub]}

               protocol.dispatch/IDispatch
               :body   {-bulk?  true}]

  :interfaces [clojure.lang.IFn
               :method  {invoke {[dispatch entry] submit-dispatch}}])

(def create-dispatch-typecheck identity
  #_(types/<dispatch:hub> :strict))

(defn create-groups-cache
  "creates the groups cache"
  {:added "4.0"}
  ([{:keys [size duration unit] :as config}]
   (let [unit (or (cc/->timeunit unit)
                  TimeUnit/MINUTES)]
     (-> (CacheBuilder/newBuilder)
         (.maximumSize size)
         (.expireAfterAccess (long duration) unit)
         (.build)))))

(defn create-dispatch
  "creates the hub executor
 
   ;; Non Sorted
   (->> (test-scaffold +test-config+ 20 5 5)
        second
        (map :id)
        (sort))
   => (range 25)"
  {:added "3.0"}
  ([{:keys [hooks options] :as m}]
   (let [{:keys [hub]} options
         {:keys [cache] :as hub-opts} (h/merge-nested +defaults+ hub)
         options (assoc options :hub hub-opts)
         runtime (-> {:debouncer (volatile! nil)
                      :groups (create-groups-cache cache)
                      :counter (hooks/counter)}
                     (assoc-in [:counter :poll]  (atom 0))
                     (assoc-in [:counter :batch] (atom 0)))]
     (-> (assoc m :type :hub)
         create-dispatch-typecheck
         (assoc :runtime runtime
                :options options)
         (map->HubDispatch)))))

(defmethod protocol.dispatch/-create :hub
  ([m]
   (create-dispatch m)))
