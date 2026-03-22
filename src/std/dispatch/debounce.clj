(ns std.dispatch.debounce
  (:require [std.concurrent :as cc]
            [std.dispatch.common :as common]
            [std.dispatch.hooks :as hooks]
            [std.lib.atom]
            [std.lib.env]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.time]
            [std.protocol.component :as protocol.component]
            [std.protocol.dispatch :as protocol.dispatch]))

;; There are three types of debouncers
;;
;; - :delay waits to run the last remaining one input
;; - :eager runs the function and blocks all other functions 
;; - :notify runs the function at regular intervals and has a wait queue

(def +defaults+ {:group-fn (fn [_ entry] entry)})

(defn- wrap-min-time
  ([handler interval delay]
   (fn [dispatch entry]
     (try ((cc/wrap-min-time (fn []
                               (handler dispatch entry))
                             interval delay))
          (catch Throwable t
            (std.lib.env/prn t))))))

(defn submit-eager
  "submits and executes eagerly
 
   (test-scaffold +test-config+
                  10 2)
   => [{:id 0, :state 0} {:id 1, :state 0}]"
  {:added "3.0"}
  ([{:keys [handler runtime hooks options] :as dispatch} entry]
   (let [{:keys [interval group-fn delay run-final]
          :or {group-fn (:group-fn +defaults+)}} (:debounce options)
         {:keys [counter]} (:debounce runtime)
         group (group-fn dispatch entry)
         check-fn (fn [m]
                    (let [val (or (get m group) 0)]
                      (cond (zero? val)
                            [true (assoc m group 1)]
                            
                            :else
                            [false m])))
         submit? (std.lib.atom/swap-return! counter check-fn)]
     (cond submit?
           (let [handler     (wrap-min-time handler interval delay)
                 main-thunk  (-> (assoc dispatch :handler handler)
                                 (common/handle-fn entry))
                 debounce-fn (fn []
                               (main-thunk)
                               (if run-final (main-thunk))
                               (swap! counter (fn [m] (assoc m group 0))))]
             (try (hooks/on-queued dispatch entry)
                  (cc/submit @(:executor runtime) debounce-fn)
                  (catch Throwable t
                    (.printStackTrace t)
                    (hooks/on-error dispatch entry t))))
           
           :else
           (do (hooks/on-skip dispatch entry)
               nil)))))

(defn submit-delay
  "submits and executes after delay
 
   (test-scaffold (-> +test-config+
                      (assoc-in [:options :debounce :strategy] :delay))
                  10 2)
   => [{:id 0, :state 9} {:id 1, :state 9}]"
  {:added "3.0"}
  ([{:keys [handler runtime hooks options] :as dispatch} entry]
   (let [{:keys [interval group-fn delay]
          :or {group-fn (:group-fn +defaults+)}} (:debounce options)
         {:keys [counter waiting]} (:debounce runtime)
         group    (group-fn dispatch entry)
         handler  (wrap-min-time handler 0 delay)
         main-fn  (fn [ret]
                    (std.lib.future/fulfil ret (-> (assoc dispatch :handler handler)
                                      (common/handle-fn entry))))
         start (std.lib.time/time-ms)
         _   (swap! counter (fn [m] (assoc m group start)))
         debounce-fn (fn [ret]
                       (let [start (get @counter group)
                             current (std.lib.time/time-ms)
                             diff (long (- (+ start interval) current))]
                         (if (pos? diff)
                           (do (Thread/sleep diff)
                               (recur ret))
                           (do (main-fn ret)
                               (swap! dissoc waiting group)))))
         check-fn (fn [m]
                    (if (get m group)
                      [false m]
                      [true (assoc m group (std.lib.future/incomplete))]))
         [submit? out] (std.lib.atom/swap-return! waiting check-fn true)
         ret  (get out group)]
     (cond submit?
           (try (cc/submit @(:executor runtime) #(debounce-fn ret))
                (hooks/on-queued dispatch entry)
                (catch Throwable t
                  (.printStackTrace t)
                  (hooks/on-error dispatch entry t)))

           :else (hooks/on-skip dispatch entry))
     ret)))

(defn submit-notify
  "submits and executes on as well and after delay"
  {:added "3.0"}
  ([{:keys [runtime hooks options handler] :as dispatch} entry]
   (let [{:keys [interval group-fn delay run-final]
          :or {group-fn (:group-fn +defaults+)}} (:debounce options)
         {:keys [waiting counter]} (:debounce runtime)
         group (group-fn dispatch entry)
         check-fn (fn [m]
                    (let [val (or (get m group) 0)]
                      (cond (not (pos? val))
                            [[true false] (assoc m group 1)]

                            (= 1 val)
                            [[false true] (update m group inc)]

                            :else [[false false] m])))
         [submit? wait?] (std.lib.atom/swap-return! counter check-fn)]
     (cond (or submit? wait?)
           (let [ret         (std.lib.future/incomplete)
                 handler     (wrap-min-time handler interval delay)
                 main-thunk   (fn []
                                (std.lib.future/fulfil ret (-> (assoc dispatch :handler handler)
                                                  (common/handle-fn entry))))
                 debounce-fn (fn []
                               (try
                                 (main-thunk)
                                 (if run-final (main-thunk))
                                 (finally
                                   (swap! counter (fn [m] (update m group dec))))))
                 submit-fn  (cond submit?
                                  (let [_ (swap! waiting assoc group ret)]
                                    debounce-fn)

                                  wait?
                                  (fn []
                                    (let [_ @(get @waiting group)
                                          _ (swap! waiting assoc group ret)]
                                      (debounce-fn))))]
             (try (cc/submit @(:executor runtime) submit-fn)
                  (hooks/on-queued dispatch entry)
                  (catch Throwable t
                    (.printStackTrace t)
                    (hooks/on-error dispatch entry t)))
             ret)

           :else
           (do (hooks/on-skip dispatch entry)
               nil)))))

(defn submit-dispatch
  "submits to the debounce executor"
  {:added "3.0"}
  ([{:keys [options runtime] :as dispatch} entry]
   (let [{:keys [strategy]} (:debounce options)
         _ (hooks/on-submit dispatch entry)]
     (case strategy
       :eager  (submit-eager dispatch entry)
       :delay  (submit-delay dispatch entry)
       :notify (submit-notify dispatch entry)))))

(defn start-dispatch
  "starts the debounce executor"
  {:added "3.0"}
  ([dispatch]
   (->  dispatch
        (common/start-dispatch)
        (assoc-in [:runtime :counter :skip] (atom 0)))))

(defn stop-dispatch
  "stops the debounce executor"
  {:added "3.0"}
  ([dispatch]
   (-> dispatch
       (update :runtime dissoc :debounce)
       (common/stop-dispatch))))

(defimpl DebounceDispatch [type runtime handler]
  :prefix "common/"
  :suffix "-dispatch"
  :string common/to-string
  :protocols  [std.protocol.component/IComponent
               :method  {-start start-dispatch
                         -stop  stop-dispatch}
               protocol.component/IComponentProps
               protocol.component/IComponentQuery
               :body    {-track-path [:dispatch :debounce]}

               protocol.dispatch/IDispatch
               :method  {-submit submit-dispatch}
               :body    {-bulk?  false}]
  :interfaces [clojure.lang.IFn
               :method {invoke {[dispatch entry] submit-dispatch}}])

(def create-dispatch-typecheck identity
  #_(types/<dispatch:debounce> :strict))

(defn create-dispatch
  "creates a debource executor"
  {:added "3.0"}
  ([{:keys [hooks options] :as m}]
   (let [m (-> (assoc m :type :debounce)
               create-dispatch-typecheck
               (common/create-map))
         debounce {:counter  (atom {})
                   :waiting  (atom {})}]
     (-> (assoc-in m [:runtime :debounce] debounce)
         (map->DebounceDispatch)))))

(defmethod protocol.dispatch/-create :debounce
  ([m]
   (create-dispatch m)))
