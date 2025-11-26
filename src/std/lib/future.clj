(ns std.lib.future
  (:require [std.protocol.return :as protocol.return]
            [std.lib.function :as fn]
            [std.lib.impl :as impl])
  (:refer-clojure :exclude [future future? await])
  (:import (java.util.concurrent CompletableFuture
                                 CompletionStage
                                 Executor
                                 ForkJoinPool
                                 TimeUnit
                                 TimeoutException
                                 CancellationException
                                 LinkedBlockingQueue)))

;;
;; future
;;

(defonce ^:dynamic *pools*
  (delay {:default  clojure.lang.Agent/soloExecutor
          :pooled   clojure.lang.Agent/pooledExecutor
          :async    (ForkJoinPool/commonPool)}))

(defn completed
  "creates a completed stage"
  {:added "3.0"}
  ([v]
   (CompletableFuture/completedFuture v)))

(defn failed
  "creates a failed stage"
  {:added "3.0"}
  ([e]
   (CompletableFuture/failedFuture e)))

(defn incomplete
  "creates an incomplete stage (like promise)
 
   (incomplete)
   => (comp not future:complete?)"
  {:added "3.0"}
  ([]
   (CompletableFuture.)))

(defn future?
  "checks if object is `CompleteableFuture`
 
   (future? (future 1))
   => true"
  {:added "3.0"}
  ([obj]
   (instance? CompletableFuture obj)))

(defn ^CompletableFuture future:fn
  "creates a future with :init/future props"
  {:added "3.0"}
  ([f]
   (future:fn f {}))
  ([f {:keys [pool timeout delay default] :as m}]
   (doto (CompletableFuture.)
     (.complete (with-meta (merge {:fn f} m)
                  {:future/init true})))))

(defn ^CompletableFuture future:call
  "can create a future from a function or :init/future
 
   @(future:call (fn [] 1))
   => 1
 
   @(-> (future:fn (fn [] 1))
        (future:call))
   => 1"
  {:added "3.0"}
  ([obj]
   (future:call obj {}))
  ([obj {:keys [pool delay] :as m}]
   (let [[f m] (cond (fn? obj)
                     [obj m]

                     (instance? CompletableFuture obj)
                     (let [value (.get ^CompletableFuture obj)]
                       (if (and (map? value)
                                (:future/init (meta value)))
                         [(:fn value) (merge value m)]))

                     :else
                     (throw (ex-info "Not valid" {:data {:input obj
                                                         :args m}})))
         {:keys [pool delay]
          :or {pool :default}} m
         executor (if (keyword? pool)
                    (get @*pools* pool)
                    pool)]
     (cond (nil? delay)
           (CompletableFuture/supplyAsync (fn/fn:supplier [] (f)) executor)

           :else
           (let [executor (CompletableFuture/delayedExecutor delay
                                                             TimeUnit/MILLISECONDS
                                                             executor)]
             (CompletableFuture/supplyAsync (fn/fn:supplier [] (f)) executor))))))

(defn ^CompletableFuture future:timeout
  "adds a timeout to the completed future
 
   @(-> (future:call (fn [] (Thread/sleep 100)))
        (future:timeout 10 :ok))
   => :ok"
  {:added "3.0"}
  ([^CompletableFuture future interval]
   (future:timeout future interval nil))
  ([^CompletableFuture future interval default]
   (cond (and (nil? interval)
              (nil? default))
         future

         (nil? default)
         (.orTimeout future interval TimeUnit/MILLISECONDS)

         :else
         (.completeOnTimeout future default interval TimeUnit/MILLISECONDS))))

(defn ^CompletableFuture future:wait
  "waits for future to finish or 
 
   (-> (future:call (fn [] (Thread/sleep 100)))
       (future:timeout 10 :ok)
       (future:wait)
       (future:now))
   => :ok"
  {:added "3.0"}
  ([^CompletableFuture future]
   (try (doto future (.get))
        (catch Throwable t
          future)))
  ([^CompletableFuture future ^long interval]
   (Thread/sleep interval)
   future))

(defn ^CompletableFuture future:run
  "runs a function with additional settings
 
   (future:run (fn [] (Thread/sleep 100))
               {:timeout 10
               :default :ok
                :delay 100
                :pool :async})"
  {:added "3.0"}
  ([obj]
   (future:run obj {}))
  ([obj {:keys [pool delay timeout default] :as m}]
   (-> (future:call obj m)
       (future:timeout timeout default))))

(defn future:now
  "gets the value of a future at the current moment"
  {:added "3.0"}
  ([^CompletableFuture future]
   (future:now future nil))
  ([^CompletableFuture future default]
   (.getNow future default)))

(defn future:value
  "gets the value of a future"
  {:added "3.0"}
  ([^CompletableFuture future]
   (.get future)))

(defn future:exception
  "accesses the exception in the future"
  {:added "3.0"}
  ([^CompletableFuture future]
   (try
     (.get future) nil
     (catch Throwable t t))))

(defn ^CompletableFuture future:cancel
  "cancels the execution of the future"
  {:added "3.0"}
  ([^CompletableFuture future]
   (doto future (.cancel true))))

(defmacro future:done
  "helper macro for status functions"
  {:added "3.0"}
  ([future & body]
   `(if-not (.isDone ~future)
      (throw (ex-info "Future not complete" {:future ~future}))
      (do ~@body))))

(defn future:cancelled?
  "checks if future has been cancelled"
  {:added "3.0"}
  ([^CompletableFuture future]
   (future:done future (.isCancelled future))))

(defn future:exception?
  "checks if future raised an exception"
  {:added "3.0"}
  ([^CompletableFuture future]
   (future:done future (.isCompletedExceptionally future))))

(defn future:timeout?
  "checks if future errored due to timeout"
  {:added "3.0"}
  ([^CompletableFuture future]
   (future:done future
                (and (.isCompletedExceptionally future)
                     (instance? TimeoutException
                                (.getCause ^Throwable (future:exception future)))))))

(defn future:success?
  "checks that future is successful"
  {:added "3.0"}
  ([^CompletableFuture future]
   (future:done future (not (.isCompletedExceptionally future)))))

(defn future:incomplete?
  "check that future is incomplete
 
   (-> (incomplete)
       (future:incomplete?))
   => true"
  {:added "3.0"}
  ([^CompletableFuture future]
   (not (.isDone future))))

(defn future:complete?
  "checks that future has successfully completed"
  {:added "3.0"}
  ([^CompletableFuture future]
   (.isDone future)))

(defn future:force
  "forces a value or exception as completed future"
  {:added "3.0"}
  ([^CompletableFuture future object]
   (future:force future :value object))
  ([^CompletableFuture future type object]
   (case type
     :value     (doto future (.complete object))
     :exception (doto future (.completeExceptionally object)))))

(defn future:obtrude
  "like force but uses obtrude and obtrudeException"
  {:added "4.0"}
  ([^CompletableFuture future object]
   (future:obtrude future :value object))
  ([^CompletableFuture future type object]
   (case type
     :value     (doto future (.obtrudeValue object))
     :exception (doto future (.obtrudeException object)))))

(defn future:dependents
  "returns number of steps waiting on current result"
  {:added "3.0"}
  ([^CompletableFuture future]
   (.getNumberOfDependents future)))

(def future:nil
  (memoize (fn []
             (completed nil))))

(defn future:lift
  "creates a future from a value"
  {:added "3.0"}
  ([obj]
   (cond (nil? obj)
         (future:nil)

         (future? obj)
         obj

         :else
         (completed obj))))

(defn ^CompletableFuture on:complete
  "process both the value and exception"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:complete future f {}))
  ([^CompletableFuture future f {:keys [timeout pool default delay] :as m}]
   (let [bin-fn  (fn/fn:lambda [success error]
                               (f success (if error (or (.getCause ^Throwable error)
                                                        error))))
         executor (if pool
                    (if (keyword? pool)
                      (get @*pools* pool)
                      pool))

         executor (if delay
                    (CompletableFuture/delayedExecutor delay
                                                       TimeUnit/MILLISECONDS
                                                       (or executor (:default @*pools*)))
                    executor)
         nfuture (cond (nil? executor)
                       (.handle future
                                bin-fn)

                       :else (.handleAsync future bin-fn ^Executor executor))]
     (future:timeout nfuture timeout default))))

(defn- exception-fn
  ([f]
   (fn [v e]
     (cond e (f e) :else v))))

(defn ^CompletableFuture on:timeout
  "processes a function on timeout"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:timeout future f {}))
  ([^CompletableFuture future f {:keys [timeout pool default delay] :as m}]
   (on:complete future (exception-fn
                        (fn [e]
                          (if (and e (instance? TimeoutException e))
                            (f e)
                            (throw e))))
                m)))

(defn ^CompletableFuture on:cancel
  "processes a function on cancel"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:cancel future f {}))
  ([^CompletableFuture future f m]
   (on:complete future (exception-fn
                        (fn [e]
                          (if (and e (instance? CancellationException e))
                            (f e)
                            (throw e))))
                m)))

(defn ^CompletableFuture on:exception
  "process a function on exception"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:exception future f {}))
  ([^CompletableFuture future f {:keys [timeout pool default delay] :as m}]
   (on:complete future (exception-fn (fn [e] (f e)))
                m)))

(defn ^CompletableFuture on:success
  "processes another step given successful operation"
  {:added "3.0"}
  ([^CompletableFuture future f]
   (on:success future f {}))
  ([^CompletableFuture future f {:keys [timeout pool default delay] :as m}]
   (on:complete future (fn [v e]
                         (if e (throw e) (f v)))
                m)))

(defn ^CompletableFuture on:all
  "calls a function when all futures are complete"
  {:added "3.0"}
  ([futures]
   (on:all futures vector))
  ([futures f]
   (on:all futures f {}))
  ([futures f {:keys [timeout pool default delay] :as m}]
   (let [futures (map future:lift futures)]
     (cond-> (into-array CompletableFuture futures)
       :then (CompletableFuture/allOf)
       f (on:complete (fn [_ _]
                        (apply f (map future:value futures)))
                      m)))))

(defn ^CompletableFuture on:any
  "calls a function when any future is completed"
  {:added "3.0"}
  ([futures f]
   (on:any futures f {}))
  ([futures f {:keys [timeout pool default delay] :as m}]
   (let [futures (map future:lift futures)
         select  (CompletableFuture/anyOf (into-array CompletableFuture
                                                      futures))]
     (on:complete select
                  (fn [_ _]
                    (f (future:value select)))
                  m))))

(defmacro future
  "constructs a completable future
 
   @(future (Thread/sleep 100)
            1)
   => 1"
  {:added "3.0"}
  ([]
   `(future:run (bound-fn []) {}))
  ([opts? & body]
   (let [[opts body] (cond (map? opts?) [opts? body]
                           :else [{} (cons opts? body)])]
     `(future:run (bound-fn [] ~@body)
                  ~opts))))

(defmacro then
  "shortcut for :on/success and :on/complete"
  {:added "3.0" :style/indent 1}
  ([future bindings & body]
   (let [len (count bindings)]
     (case len
       0 `(on:success  ~future (fn [~'_] ~@body))
       1 `(on:success  ~future (fn [~@bindings] ~@body))
       2 `(on:complete ~future (fn [~@bindings] ~@body))))))

(defmacro catch
  "shortcut for :on/exception"
  {:added "3.0" :style/indent 1}
  ([future bindings & body]
   (let [len (count bindings)]
     (case len
       0 `(on:exception  ~future (fn [~'_] ~@body))
       1 `(on:exception  ~future (fn [~@bindings] ~@body))))))

(defn fulfil
  "fulfils the return with a function
 
   (fulfil (incomplete) (fn [] (+ 1 2 3)))
 
   (fulfil (incomplete) (fn [] (throw (ex-info \"Hello\" {}))))"
  {:added "3.0"}
  ([future f]
   (fulfil future f false))
  ([future f print]
   (fulfil future f false false))
  ([future f print skip-success]
   (let [[status data exception] (try
                                   [:success (f) nil]
                                   (catch Throwable t
                                     (if print (.printStackTrace t))
                                     [:error nil t]))]
     (if-not (and (= status :success)
                  skip-success)
       (if (= status :success)
         (future:force future data)
         (future:force future :exception exception))
       data))))

(defn future:result
  "gets the result of the future
 
   (future:result (completed 1))
   => {:status :success, :data 1, :exception nil}
 
   (future:result (failed (ex-info \"\" {})))
   => (contains {:status :error
                 :data nil
                 :exception Throwable})"
  {:added "3.0"}
  ([future]
   (let [[status data exception]
         (if (and (future:wait future)
                  (future:exception? future))
           [:error nil (future:exception future)]
           [:success (future:value future) nil])]
     {:status status
      :data data
      :exception exception})))

(defn future:status
  "retrieves a status of either `:success`, `:error` or `waiting`"
  {:added "3.0"}
  ([future]
   (cond (not (future-done? future))
         :waiting

         (future:exception? future)
         :error

         :else :success)))

(defn future:chain
  "chains a set of functions
 
   @(future:chain (completed 1) [inc inc inc])
   => 4
 
   @(future:chain (failed (ex-info \"ERROR\" {})) [inc inc inc])
   => (throws)"
  {:added "3.0"}
  ([future chain]
   (reduce (fn [out f]
             (on:complete out
                          (fn [data error]
                            (if error
                              (throw error)
                              (f data)))))
           future
           chain))
  ([future chain marr]
   (reduce (fn [out [f m]]
             (on:complete out
                          (fn [data error]
                            (if error
                              (throw error)
                              (f data)))
                          m))
           future
           (map vector chain marr))))
