(ns std.concurrent.executor
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.dispatch :as protocol.dispatch]
            [std.protocol.track :as protocol.track]
            [std.lib.component.track :as track]
            [std.lib.component :as component]
            [std.concurrent.queue :as q]
            [std.lib.future :as f]
            [std.lib.foundation :as h]
            [std.lib.impl :as impl]
            [std.lib.atom :as atom])
  (:import (java.util.concurrent Executors
                                 ExecutorService
                                 ThreadPoolExecutor
                                 ScheduledThreadPoolExecutor
                                 TimeUnit
                                 BlockingQueue
                                 RejectedExecutionHandler)
           (hara.lib.concurrent LimitedQueue)))

(defonce ^:dynamic ^:private *shared* (atom {}))

(defn ^Callable wrap-min-time
  "wraps a function with min-time and delay
 
   ((wrap-min-time (fn []) 20 0))
 
   ((wrap-min-time (fn []) 100 10))"
  {:added "3.0"}
  ([f total]
   (wrap-min-time f total 0))
  ([f total delay]
   (fn []
     (let [start (System/currentTimeMillis)
           _ (if (and delay (pos? delay)) (Thread/sleep (long delay)))
           _ (f)
           end (System/currentTimeMillis)
           duration (- end start)]
       (if (> total duration)
         (Thread/sleep (long (- total duration))))))))

(defn ^BlockingQueue exec:queue
  "contructs a raw queue in different ways
 
   (exec:queue)
 
   (exec:queue 1)
 
   (exec:queue {:size 1})
 
   (exec:queue {})
 
   (exec:queue (q/queue))"
  {:added "3.0"}
  ([]
   (exec:queue nil))
  ([arg]
   (cond (q/queue? arg) arg

         (nil? arg) (q/queue)

         (integer? arg) (q/queue:fixed arg)

         (map? arg)
         (cond (integer? (:size arg))
               (q/queue:fixed (:size arg))

               :else
               (q/queue))

         :else (throw (ex-info "Invalid input" {:input arg})))))

(defn ^ExecutorService executor:single
  "constructs a single executor
 
   ;; any sized pool
   (executor:single)
 
   ;; fixed pool
   (executor:single {:size 10})"
  {:added "3.0"}
  ([]
   (executor:single (q/queue)))
  ([size-or-queue]
   (doto (ThreadPoolExecutor. 0 1 0 TimeUnit/MILLISECONDS (exec:queue size-or-queue))
     (track/track))))

(defn ^ExecutorService executor:pool
  "constructs a pool executor
 
   (executor:pool 10 10 1000 {:size 10})"
  {:added "3.0"}
  ([size max keep-alive]
   (executor:pool size max keep-alive (q/queue)))
  ([^long size ^long max ^long keep-alive size-or-queue]
   (doto (ThreadPoolExecutor. size max keep-alive
                              TimeUnit/MILLISECONDS (exec:queue size-or-queue))
     (track/track))))

(defn ^ExecutorService executor:cached
  "creates a cached executor
 
   (executor:cached)"
  {:added "3.0"}
  ([]
   (doto (Executors/newCachedThreadPool)
     (track/track))))

(defn executor:virtual
  "creates a virtual thread executor"
  {:added "4.0"}
  []
  (try
    (let [method (-> (Class/forName "java.util.concurrent.Executors")
                     (.getMethod "newVirtualThreadPerTaskExecutor" (into-array Class [])))]
      (.invoke method nil (into-array Object [])))
    (catch Throwable _
      (throw (ex-info "Virtual threads not supported" {})))))

(defn exec:shutdown
  "shuts down executor"
  {:added "3.0"}
  ([^ExecutorService service]
   (doto service
     (track/untrack)
     (.shutdown))))

(defn exec:shutdown-now
  "shuts down executor immediately"
  {:added "3.0"}
  ([^ExecutorService service]
   (doto service
     (track/untrack)
     (.shutdownNow))))

(defn ^BlockingQueue exec:get-queue
  "gets the queue from the executor
 
   (-> (executor:pool 10 10 1000 (q/queue))
       (exec:get-queue))"
  {:added "3.0"}
  ([service]
   (if (instance? ThreadPoolExecutor service)
     (.getQueue ^ThreadPoolExecutor service)
     (throw (ex-info "Cannot access queue" {:service service})))))

(defn submit
  "submits a task to an executor
 
   @(submit (executor:single)
            (fn [])
            {:min 100}) ^hidden
 
   @(submit (executor:single)
            (fn []
              (Thread/sleep 1000))
            {:max 100})
   => (throws)"
  {:added "3.0"}
  ([^ExecutorService service ^Callable f]
   (submit service f nil))
  ([^ExecutorService service f {:keys [min max delay default] :as m}]
   (let [^Callable f (cond-> f
                       min (wrap-min-time min (or delay 0)))
         opts (cond-> {:pool service}
                max     (assoc :timeout max)
                default (assoc :default default)
                delay   (assoc :delay delay))]
     (f/future:run f opts))))

(defn submit-notify
  "submits a task (generally to a fixed size queue)
 
   (doto (executor:single 1)
     (submit-notify (fn [])
                   1000)
     (submit-notify (fn [])
                    1000)
     (submit-notify (fn [])
                    1000))"
  {:added "3.0"}
  ([^ExecutorService service ^Callable f]
   (submit-notify service f nil))
  ([^ExecutorService service f {:keys [min max delay default] :as m}]
   (if (pos? (q/remaining-capacity (exec:get-queue service)))
     (h/suppress (submit service f m)))))

(defn ^ScheduledThreadPoolExecutor executor:scheduled
  "constructs a scheduled executor
 
   (executor:scheduled 10)"
  {:added "3.0"}
  ([^long size]
   (doto (ScheduledThreadPoolExecutor. size)
     (track/track))))

(defn schedule
  "schedules task for execution"
  {:added "3.0"}
  ([^ScheduledThreadPoolExecutor service ^Callable f ^long interval]
   (schedule service f interval nil))
  ([^ScheduledThreadPoolExecutor service ^Callable f ^long interval {:keys [min]}]
   (let [^Callable f (cond-> f
                       min (wrap-min-time min))]
     (.schedule service f interval TimeUnit/MILLISECONDS))))

(defn schedule:fixed-rate
  "schedules task at a fixed rate"
  {:added "3.0"}
  ([^ScheduledThreadPoolExecutor service ^Runnable f ^long interval]
   (schedule:fixed-rate service f interval nil))
  ([^ScheduledThreadPoolExecutor service ^Runnable f ^long interval {:keys [initial min]}]
   (let [^Runnable f (cond-> f
                       min (wrap-min-time min))]
     (.scheduleAtFixedRate service f (or initial 0) interval TimeUnit/MILLISECONDS))))

(defn schedule:fixed-delay
  "schedules task at fixed delay"
  {:added "3.0"}
  ([^ScheduledThreadPoolExecutor service ^Runnable f ^long delay]
   (schedule:fixed-delay service f delay nil))
  ([^ScheduledThreadPoolExecutor service ^Runnable f ^long delay {:keys [initial min]}]
   (let [^Runnable f (cond-> f
                       min (wrap-min-time min))]
     (.scheduleWithFixedDelay service f (or initial 0) delay TimeUnit/MILLISECONDS))))

(defn exec:await-termination
  "await termination for executor service"
  {:added "3.0"}
  ([^ExecutorService service]
   (exec:await-termination service Long/MAX_VALUE))
  ([^ExecutorService service ^long ms]
   (.awaitTermination service ms TimeUnit/MILLISECONDS)))

(defn exec:shutdown?
  "checks if executor is shutdown"
  {:added "3.0"}
  ([^ExecutorService service]
   (.isShutdown service)))

(defn exec:terminated?
  "checks if executor is shutdown and all threads have finished"
  {:added "3.0"}
  ([^ExecutorService service]
   (.isTerminated service)))

(defn exec:terminating?
  "check that executor is terminating"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.isTerminating service)))

(defn exec:current-size
  "returns number of threads in pool"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getPoolSize service)))

(defn exec:current-active
  "returns number of active threads in pool"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getActiveCount service)))

(defn exec:current-submitted
  "returns number of submitted tasks"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getTaskCount service)))

(defn exec:current-completed
  "returns number of completed tasks"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getCompletedTaskCount service)))

(defn exec:pool-size
  "gets and sets the core pool size"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getCorePoolSize service))
  ([^ThreadPoolExecutor service size]
   (.setCorePoolSize service (int size))))

(defn exec:pool-max
  "gets and sets the core pool max"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getMaximumPoolSize service))
  ([^ThreadPoolExecutor service size]
   (.setMaximumPoolSize service size)))

(defn exec:keep-alive
  "gets and sets the keep alive time"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getKeepAliveTime service TimeUnit/MILLISECONDS))
  ([^ThreadPoolExecutor service length]
   (.setKeepAliveTime service length TimeUnit/MILLISECONDS)))

(defn exec:rejected-handler
  "sets the rejected task handler"
  {:added "3.0"}
  ([^ThreadPoolExecutor service]
   (.getRejectedExecutionHandler service))
  ([^ThreadPoolExecutor service f]
   (.setRejectedExecutionHandler service
                                 (reify RejectedExecutionHandler
                                   (rejectedExecution [_ task executor]
                                     (f task executor))))))

(defn exec:at-capacity?
  "checks if executor is at capacity"
  {:added "3.0"}
  ([executor]
   (= (exec:current-active executor)
      (exec:pool-max executor))))

(defn exec:increase-capacity
  "increases the capacity of the executor"
  {:added "3.0"}
  ([executor]
   (exec:increase-capacity executor (* 2 (exec:pool-max executor))))
  ([executor n]
   (let [max (exec:pool-max executor)]
     (if (< max n) (exec:pool-max executor n))
     (exec:pool-size executor n))))

(defn executor:type
  "returns executor service type"
  {:added "3.0"}
  ([^ExecutorService executor]
   (cond (instance? ScheduledThreadPoolExecutor executor)
         :scheduled

         (not (instance? ThreadPoolExecutor executor))
         :virtual

         (= 1 (exec:pool-max executor))
         :single

         (and (= Integer/MAX_VALUE (exec:pool-max executor))
              (let [queue (exec:get-queue executor)]
                (and (zero? (q/remaining-capacity queue))
                     (zero? (count queue)))))
         :cached

         :else :pool)))

(defn executor:info
  "returns executor service info"
  {:added "3.0"}
  ([^ExecutorService executor]
   (executor:info executor #{:type :running :current :counter :options}))
  ([^ExecutorService executor k]
   (if-not (instance? ThreadPoolExecutor executor)
     (cond-> {}
       :always (assoc :type (executor:type executor)
                      :running (not (exec:shutdown? executor))))
     (let [[return items]  (cond (keyword? k)
                                 [k #{k}]

                                 :else
                                 [identity (set k)])
           queue  (.getQueue ^ThreadPoolExecutor executor)]
       (cond-> {}
         (:type items)    (assoc :type (executor:type executor))
         (:running items) (assoc :running (not (exec:shutdown? executor)))
         (:current items) (assoc :current {:threads (exec:current-size executor)
                                           :active  (exec:current-active executor)
                                           :queued  (count queue)
                                           :terminated (exec:terminated? executor)})
         (:counter items) (assoc :counter {:submit   (exec:current-submitted executor)
                                           :complete (exec:current-completed executor)})
         (:options items) (assoc :options {:pool {:size (exec:pool-size executor)
                                                  :max (exec:pool-max executor)
                                                  :keep-alive (exec:keep-alive executor)}
                                           :queue {:remaining (q/remaining-capacity queue)
                                                   :total (count queue)}})
         :then return)))))

(defn executor:props
  "returns props for getters and setters"
  {:added "3.0"}
  ([^ThreadPoolExecutor executor]
   {:pool {:size       {:get exec:pool-size
                        :set exec:pool-size}
           :max        {:get exec:pool-max
                        :set exec:pool-max}
           :keep-alive {:get exec:keep-alive
                        :set exec:keep-alive}}
    :rejected-handler  {:get exec:rejected-handler
                        :set exec:rejected-handler}}))

(defn executor:health
  "returns health of the executor"
  {:added "3.0"}
  ([executor]
   (if (exec:shutdown? executor)
     {:status :not-healthy}
     {:status :ok})))

(def executor:start  identity)
(def executor:stop  exec:shutdown)
(def executor:kill  exec:shutdown-now)
(def executor:started? (comp not exec:shutdown?))
(def executor:stopped? exec:shutdown?)
(def executor:submit submit)

(defn- executor-string
  [executor]
  (let [tag (if (instance? ScheduledThreadPoolExecutor executor)
              "scheduled"
              (if (instance? ThreadPoolExecutor executor)
                "raw"
                "virtual"))]
    (str "#" tag ".executor" (executor:info executor [:type :counter :current]))))

(impl/extend-impl java.util.concurrent.ExecutorService
                  :string executor-string
                  :prefix "executor:"
                  :protocols [std.protocol.component/IComponent
                              :body {-remote? false}
                              protocol.dispatch/IDispatch
                              :body {-bulk? false}
                              protocol.track/ITrack
                              :body {-track-path  [:raw :executor]}])

(impl/extend-impl java.util.concurrent.ThreadPoolExecutor
  :string executor-string
  :prefix "executor:"
  :protocols [std.protocol.component/IComponent
              :body {-remote? false}
              protocol.dispatch/IDispatch
              :body {-bulk? false}
              protocol.track/ITrack
              :body {-track-path  [:raw :executor]}])

(defmulti executor
  "creates an executor
 
   (executor {:type :pool
              :size 3
              :max 3
             :keep-alive 1000})"
  {:added "3.0"}
  :type)

(defmethod executor :pool
  ([{:keys [size max keep-alive queue] :as m}]
   (executor:pool size max keep-alive queue)))

(defmethod executor :single
  ([{:keys [queue] :as m}]
   (executor:single queue)))

(defmethod executor :scheduled
  ([{:keys [size] :as m}]
   (executor:scheduled size)))

(defmethod executor :cached
  ([_]
   (executor:cached)))

(defmethod executor :virtual
  ([_]
   (executor:virtual)))

(defmethod executor :shared
  ([{:keys [id] :as m}]
   (or (get @f/*pools* id)
       (get @*shared* id)
       (throw (ex-info "Cannot find executor" {:id id
                                               :options (concat (keys @*shared*)
                                                                (keys @f/*pools*))})))))

(defn executor:shared
  "lists all shared executors
 
   (-> (executor:shared) keys sort)
   => [:async :default :pooled]"
  {:added "3.0"}
  ([]
   (merge @*shared* @f/*pools*)))

(defn executor:share
  "registers a shared executor"
  {:added "3.0"}
  ([id ^ExecutorService executor]
   (swap! *shared* (fn [m]
                     (if (or (get @f/*pools* id)
                             (get m id))
                       (throw (ex-info "Already exists" {:id id}))
                       (assoc m id executor))))))

(defn executor:unshare
  "deregisters a shared executor"
  {:added "3.0"}
  ([id]
   (atom/swap-return! *shared* (fn [m] [(get m id) (dissoc m id)]))))
