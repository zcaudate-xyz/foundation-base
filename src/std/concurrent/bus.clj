(ns std.concurrent.bus
  (:require [std.concurrent.queue :as queue]
            [std.concurrent.thread :as thread]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.foundation]
            [std.lib.future :as f]
            [std.lib.impl :as impl]
            [std.lib.resource :as res]
            [std.lib.time :as time]
            [std.protocol.component :as protocol.component])
  (:refer-clojure :exclude [send]))

(def +keys+
  {:send    [:id :op :entry]
   :receive [:id :status :data :exception]})

(defn bus:get-thread
  "gets thread given an id
 
   (bus:with-temp bus
                  (->> (bus:get-id bus)
                       (bus:get-thread bus)))
   => (cc/thread:current)"
  {:added "3.0"}
  ([{:keys [state]} id]
   (let [{:keys [threads]} @state]
     (:thread (get threads id)))))

(defn bus:get-id
  "gets registered id given thread
 
   (bus:with-temp bus
                  (bus:get-id bus))
   => string?"
  {:added "3.0"}
  ([bus]
   (bus:get-id bus (thread/thread:current)))
  ([{:keys [state]} thread]
   (let [{:keys [lookup]} @state]
     (get lookup thread))))

(defn bus:has-id?
  "checks that the bus has a given id"
  {:added "3.0"}
  ([{:keys [state]} id]
   (let [{:keys [threads]} @state]
     (boolean (get threads id)))))

(defn bus:get-queue
  "gets the message queue associated with the thread
 
   (bus:with-temp bus
                  (vec (bus:get-queue bus)))
   => []"
  {:added "3.0"}
  ([bus]
   (bus:get-queue bus (thread/thread:current)))
  ([{:keys [state] :as bus} obj]
   (let [{:keys [threads lookup]} @state]
     (:queue (if (instance? Thread obj)
               (get threads (get lookup obj))
               (get threads obj))))))

(defn bus:all-ids
  "returns all registered ids
 
   (bus:with-temp bus
                  (bus:all-ids bus))
   => (contains #{string?})"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (let [{:keys [threads]} @state]
     (set (keys threads)))))

(defn bus:all-threads
  "returns all registered threads
 
   (bus:with-temp bus
                  (= (first (vals (bus:all-threads bus)))
                     (Thread/currentThread)))
   => true"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (let [{:keys [threads]} @state]
     (collection/map-vals :thread threads))))

(defn bus:get-count
  "returns the number of threads registered"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (-> state deref :threads count)))

(defn bus:register
  "registers a thread to the bus"
  {:added "3.0"}
  ([bus]
   (bus:register bus (std.lib.foundation/sid)))
  ([bus id]
   (bus:register bus id (thread/thread:current)))
  ([{:keys [state] :as bus} id thread]
   (let [queue (queue/queue)]
     (swap! state (fn [m]
                    (-> m
                        (assoc-in [:threads id]
                                  {:id id :thread thread :queue queue})
                        (assoc-in [:lookup thread] id))))
     queue)))

(defn bus:deregister
  "deregisters from the bus"
  {:added "3.0"}
  ([bus]
   (bus:deregister bus (bus:get-id bus)))
  ([{:keys [state] :as bus} id]
   (swap! state (fn [m]
                  (let [{:keys [thread]} (get-in m [:threads id])]
                    (-> m
                        (update :threads dissoc id)
                        (update :lookup dissoc thread)))))))

(defn bus:send
  "sends a message to the given thread"
  {:added "3.0"}
  ([{:keys [results counters] :as bus} id msg]
   (if-let [queue  (bus:get-queue bus id)]
     (let [msg-id (or (:id msg) (std.lib.foundation/sid))
           return (f/incomplete)]
       (swap! results assoc msg-id return)
       (queue/put queue (assoc msg :id msg-id))
       (swap! (:sent counters) inc)
       return))))

(defn bus:wait
  "bus:waits on the message queue for message
 
   (bus:with-temp bus
                  (bus:send bus (bus:get-id bus)
                            {:op :hello :message \"world\"})
                  (bus:wait bus))
   => (contains {:op :hello, :message \"world\", :id string?})
 
   (bus:with-temp bus
                  (bus:wait bus {:timeout 100}))"
  {:added "3.0"}
  ([bus]
   (bus:wait bus {}))
  ([bus {:keys [timeout timeunit]
         :or {timeunit :ms}}]
   (queue/take (bus:get-queue bus) timeout timeunit)))

(defn handler-thunk
  "creates a thread loop for given message handler"
  {:added "3.0"}
  ([{:keys [output] :as bus} handler {:keys [stopped on-timeout] session-id :id :as opts}]
   (fn []
     (try
       (loop []
         (if-let [{:keys [id op] :as msg} (bus:wait bus opts)]
           (cond (not= op :exit)
                 (let [[status data exception]
                       (try
                         [:success (handler msg) nil]
                         (catch InterruptedException e (throw e))
                         (catch Throwable t [:error nil t]))]
                   (queue/put output {:id id :status status :data data :exception exception})
                   (recur))

                 :else
                 (do (queue/put output {:id id :status :success :data {:exit true :stopped stopped}})
                     {:exit :normal :id session-id :unprocessed (bus:get-queue bus session-id)}))
           (let [return (and on-timeout (on-timeout))]
             (if-not (true? return)
               {:exit :timeout :id session-id}
               (recur)))))
       (catch InterruptedException e
         {:exit :interrupted :id session-id :unprocessed (bus:get-queue bus session-id)})))))

(defn run-handler
  "runs the handler in a thread loop"
  {:added "3.0"}
  ([bus handler]
   (run-handler bus handler {}))
  ([bus handler {:keys [id started stopped on-start on-stop] :as opts}]
   (try
     (let [thunk (handler-thunk bus handler opts)
           id (or id (std.lib.foundation/sid))
           start (time/time-ns)
           thread (thread/thread:current)
           _ (bus:register bus id thread)
           _ (f/future:force started {:id id :thread thread :stopped stopped})
           _ (if on-start (on-start))
           output (thunk)
           _ (if on-stop (on-stop))
           _ (bus:deregister bus id)
           end (time/time-ns)
           output (assoc output :start start :end end)
           _ (f/future:force stopped output)]
       output)
     (catch Throwable t
       (.printStackTrace t)))))

(defn bus:send-all
  "bus:sends message to all thread queues"
  {:added "3.0"}
  ([bus msg]
   (collection/map-entries (fn [id] [id (bus:send bus id msg)]) (bus:all-ids bus))))

(defn bus:open
  "bus:opens a new handler loop given function
 
   (bus:with-temp bus
                  (let [{:keys [id]} @(bus:open bus (fn [m]
                                                      (update m :value inc)))]
                    (Thread/sleep 100)
                    @(bus:send bus id {:value 1})))
   => (contains {:value 2, :id string?})"
  {:added "3.0"}
  ([bus handler]
   (bus:open bus handler {}))
  ([bus handler {:keys [id] :as opts
                 :or {id (std.lib.foundation/sid)}}]
   (if-not (component/started? bus) (std.lib.foundation/error "Bus not started" {:bus bus}))
   
   (let [started  (f/incomplete)
         opts     (assoc opts :id id :started started :stopped (f/incomplete))
         _        (f/future
                    (run-handler bus handler opts))]
     started)))

(defn bus:close
  "bus:closes all bus:opened loops
 
   (bus:with-temp bus
                  (let [{:keys [stopped id]} @(bus:open bus (fn [m]
                                                              (update m :value inc)))]
                    (Thread/sleep 10)
                    (bus:close bus id)
                    [@stopped (bus:get-count bus)]))
   => (contains-in [{:exit :normal,
                     :id string?
                    :unprocessed empty?
                     :start number?
                     :end number?}
                    1])"
  {:added "3.0"}
  ([bus id]
   (bus:send bus id {:op :exit})))

(defn bus:close-all
  "stops all thread loops"
  {:added "3.0"}
  ([bus]
   (collection/map-entries (fn [id]
                    [id (bus:close bus id)])
                  (bus:all-ids bus))))

(defn bus:kill
  "bus:closes all bus:opened loops"
  {:added "3.0"}
  ([bus id]
   (let [thread (bus:get-thread bus id)]
     (if (and thread (not= thread (thread/thread:current)))
       (doto thread
         (thread/thread:interrupt))))))

(defn bus:kill-all
  "stops all thread loops"
  {:added "3.0"}
  ([bus]
   (collection/map-entries (fn [id]
                    [id (bus:kill bus id)])
                  (bus:all-ids bus))))

(defn main-thunk
  "creates main message return handler"
  {:added "3.0"}
  ([{:keys [results output counters] :as bus}]
   (fn []
     (try
       (loop [{:keys [id status data exception] :as result} (queue/take output)]
         (let [return (get @results id)
               _ (swap! results dissoc id)
               _ (if (= :success status)
                   (f/future:force return data)
                   (f/future:force return :exception exception))
               _ (swap! (:received counters) inc)]
           (recur (queue/take output))))
       (catch InterruptedException e)))))

(defn main-loop
  "creates a new message return loop"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (let [thunk (main-thunk bus)]
     (swap! state assoc :main (thread/thread:current))
     (thunk))))

(defn started?-bus
  "checks if bus is running
 
   (bus:with-temp bus
                  (Thread/sleep 10)
                  (started?-bus bus))
   => true"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (boolean (when-let [main ^Thread (:main @state)]
              (.isAlive main)))))

(defn start-bus
  "starts the bus"
  {:added "3.0"}
  ([bus]
   (if-not (started?-bus bus)
     (thread/thread {:handler #(main-loop bus)
                     :daemon true
                     :start true}))
   bus))

(defn stop-bus
  "stops the bus"
  {:added "3.0"}
  ([{:keys [state] :as bus}]
   (if (started?-bus bus)
     (let [main ^Thread (:main @state)
           _ (swap! state dissoc :main)]
       (thread/thread:interrupt main)
       (bus:kill-all bus)))
   bus))

(defn info-bus
  "returns info about the bus"
  {:added "3.0"}
  ([{:keys [state results output counters] :as bus}]
   {:running  (started?-bus bus)
    :threads  (count (bus:all-ids bus))
    :waiting  (count @results)
    :queued   (collection/map-vals (comp count :queue) (:threads @state))
    :sent     @(:sent counters)
    :received @(:received counters)}))

(defn- string-bus
  ([bus]
   (str "#bus" (info-bus bus))))

(impl/defimpl Bus [state results output counters]
  :suffix "-bus"
  :string string-bus
  :protocols [std.protocol.component/IComponent
              :include [-start -stop -started? -info]])

(defn bus:create
  "creates a bus"
  {:added "3.0"}
  ([] (bus:create nil))
  ([m]
   (map->Bus (merge {:state   (atom {})
                     :results (atom {})
                     :output  (queue/queue)
                     :counters {:sent (atom 0)
                                :received (atom 0)}}
                    m))))

(defn bus
  "creates and starts a bus"
  {:added "3.0"}
  ([] (bus nil))
  ([m]
   (-> (bus:create m)
       (component/start))))

(defn bus?
  "checks if object is instance of Bus"
  {:added "3.0"}
  ([obj]
   (instance? Bus obj)))

(defmacro bus:with-temp
  "checks if object is instance of Bus"
  {:added "3.0" :style/indent 1}
  ([var & body]
   `(let [~var   (bus)
          ~'_    (bus:register ~var)]
      (try ~@body
           (finally (bus:deregister ~var)
                    (stop-bus ~var))))))

(defn bus:reset-counters
  "resets the counters for a bus"
  {:added "4.0"}
  [bus]
  (collection/map-vals #(atom/swap-return! % (fn [v] [v 0]))
              (:counters bus)))

(def +resource+
  (res/res:spec-add
   {:type :hara/concurrent.bus
    :instance {:create bus:create}}))
