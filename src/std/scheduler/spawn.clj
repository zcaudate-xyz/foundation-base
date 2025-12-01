(ns std.scheduler.spawn
  (:require [std.concurrent :as cc]
            [std.lib :as h :refer [defimpl]]))

(def ^:dynamic *spawn-id* nil)

(def ^:dynamic *delay* nil)

(def ^:dynamic *spawn* nil)

(def ^:dynamic *job* nil)

(def ^:dynamic *interval* 4000)

(def ^:dynamic *group* false)

(def ^:dynamic *timing* nil)

(def ^:dynamic *defaults*
  {:policy      :first
   :time-fn     (fn [t] t)
   :main-fn     (fn [args t] nil)
   :args-fn     (fn [] nil)
   :create-fn   (fn [] nil)
   :merge-fn    (fn [m result] (merge m result))
   :success-fn  (fn [result] result)
   :error-fn    (fn [error] nil)
   :continue-fn (fn [props] true)})

(defn spawn-status
  "returns the spawn status"
  {:added "3.0"}
  ([{:keys [props exit] :as spawn}]
   (let [{:keys [terminate started]} @props]
     (cond (realized? exit) :stopped

           terminate :terminating

           (boolean started) :running

           :else :created))))

(defn spawn-info
  "returns the spawn info"
  {:added "3.0"}
  ([{:keys [id board props output state exit] :as spawn}]
   (let [{:keys [succeeded errored submitted completed started updated error]} @props]
     (cond-> {:status (spawn-status spawn)
              :duration  (h/format-ms (if (and updated started)
                                        (quot (- updated started) 1000000)
                                        0))
              :jobs {:submitted submitted
                     :succeeded succeeded
                     :errored errored
                     :waiting (count @board)}}
       (not *group*) (assoc :id id :state @state)
       error (assoc :error error)))))

(defn- spawn-string
  ([spawn]
   (str "#spawn " (spawn-info spawn))))

(defimpl Spawn [id board props output state exit]
  :string spawn-string)

(defn spawn?
  "checks that object is a spawn"
  {:added "3.0"}
  ([obj]
   (instance? Spawn obj)))

(defn create-spawn
  "creates a new spawn
 
   (-> (create-spawn (merge *defaults* {:interval 1})
                     \"spawn.1\")
       (spawn-info))
   => {:id \"spawn.1\", :status :created, :duration \"0ms\",
       :jobs {:submitted 0 :succeeded 0 :errored 0 :waiting 0}, :state nil}"
  {:added "3.0"}
  ([]
   (create-spawn *defaults* nil))
  ([{:keys [create-fn continue-fn interval
            global full state] :as program} spawn-id]
   (let [spawn-id (or spawn-id (h/sid))]
     (binding [*spawn-id* spawn-id]
       (map->Spawn
        {:id      spawn-id
         :exit    (promise)
         :board   (atom {})
         :props   (atom {:started nil
                         :updated nil
                         :succeeded 0
                         :errored   0
                         :submitted 0
                         :completed 0
                         :terminate false
                         :interval interval
                         :error nil})
         :output (if full
                   (cc/queue)
                   (atom nil))
         :state  (if global
                   state
                   (atom (create-fn)))})))))

(defn set-props
  "updates the spawn props
 
   (-> (create-spawn)
       (set-props :submitted inc :errored 4 :started true)
       (select-keys [:submitted :errored :started]))
   => {:submitted 1, :errored 4, :started true}"
  {:added "3.0"}
  ([spawn k v & more]
   (swap! (:props spawn)
          (fn [m]
            (reduce (fn [m [k v]]
                      (cond (fn? v)
                            (update m k v)

                            :else (assoc m k v)))
                    m
                    (cons [k v] (partition 2 more)))))))

(defn get-props
  "gets the spawn prop given key
 
   (-> (create-spawn)
       (get-props :submitted))
   => 0"
  {:added "3.0"}
  ([spawn]
   @(:props spawn))
  ([spawn k]
   (get @(:props spawn) k)))

(defn get-job
  "retrieves a job given key
 
   (-> (doto (create-spawn)
         (add-job \"j.1\" {:id \"j.1\"}))
 
       (get-job \"j.1\"))
   => {:id \"j.1\"}"
  {:added "3.0"}
  ([spawn job-id]
   (-> spawn
       :board
       deref
       (get job-id)))
  ([spawn job-id ks]
   (-> (get-job spawn job-id)
       (get-in ks))))

(defn update-job
  "updates a job given key"
  {:added "3.0"}
  ([spawn job-id f & args]
   (-> (swap! (:board spawn)
              #(apply update % job-id f args))
       (get job-id))))

(defn remove-job
  "removes a job given key"
  {:added "3.0"}
  ([spawn job-id]
   (let [out (h/swap-return! (:board spawn)
                             (fn [m]
                               [(get m job-id) (dissoc m job-id)]))]
     out)))

(defn add-job
  "adds job to the spawn"
  {:added "3.0"}
  ([spawn job-id job]
   (-> (swap! (:board spawn) assoc job-id job)
       (get job-id))))

(defn list-jobs
  "list all jobs"
  {:added "3.0"}
  ([spawn]
   (sort-by :time (vals @(:board spawn)))))

(defn list-job-ids
  "lists all job ids"
  {:added "3.0"}
  ([spawn]
   (set (keys @(:board spawn)))))

(defn count-jobs
  "returns the number of jobs"
  {:added "3.0"}
  ([spawn]
   (count @(:board spawn))))

(defn send-result
  "sends the result to spawn output"
  {:added "3.0"}
  ([{:keys [output] :as spawn} result]
   (cond (h/atom? output)
         (reset! output result)

         (cc/queue? output)
         (cc/put output result))))

(defn handler-run
  "handles the 
 
   (let [ret (h/incomplete)
         job {:id \"j.0\"
              :time 0
              :return ret}
         spawn (doto (create-spawn)
                 (add-job \"j.0\" job))
         result {:task (handler-run *defaults* spawn job)}]
     (assoc result :return @ret :meta @(:output spawn)))
  => (contains-in {:task [\"j.0\" 0]
                    :return nil
                    :meta {:status :success,
                           :data nil, :exception nil,
                           :time 0,
                           :start integer?
                           :end integer?}})"
  {:added "3.0"}
  ([{:keys [args-fn main-fn merge-fn success-fn error-fn]}
    {:keys [state] :as spawn}
    {:keys [time return] job-id :id :as job}]
   (binding [*job* (get-job spawn job-id)
             *spawn* spawn
             *spawn-id* (:id spawn)]
     (try
       (let [start (h/time-ns)
             args (args-fn)
             _ (h/fulfil return (fn [] (main-fn args time)) true)
             {:keys [status data exception] :as m} (h/future:result return)
             [status-key output] (cond exception [:errored  (error-fn exception)]
                                       :else     [:succeeded (success-fn data)])
             end (h/time-ns)
             _ (binding [*timing* [start end]]
                 (swap! state merge-fn output))
             _ (remove-job spawn job-id)
             _ (set-props spawn
                          :completed inc
                          :updated end
                          status-key inc
                          :error (fn [current] (or exception current)))
             _ (send-result spawn (assoc m
                                         :time time
                                         :start start
                                         :end end))]
         [job-id time])
       (catch Throwable t
         (.printStackTrace t))))))

(defn create-handler-basic
  "creates a basic handler"
  {:added "3.0"}
  ([program spawn]
   (fn [_ job-id]
     (let [job (get-job spawn job-id)]
       (handler-run program spawn job)))))

(defn create-handler-constant
  "creates a constant handler"
  {:added "3.0"}
  ([{:keys [args-fn] :as program} spawn]
   (let [handler-fn (create-handler-basic program spawn)]
     (fn [runtime job-id]
       (let [{:keys [core]} @runtime
             task   (cc/submit core #(handler-fn runtime job-id))
             {:keys [time]} (update-job spawn job-id assoc :task task)]
         [job-id time])))))

(defn create-handler
  "creates a run handler
 
   (create-handler (assoc *defaults* :type :basic)
                   (create-spawn))
   => fn?"
  {:added "3.0"}
  ([{:keys [type] :as program} spawn]
   (case type
     :basic    (create-handler-basic program spawn)
     :constant (create-handler-constant program spawn))))

(defn schedule-timing
  "calculates the timing for the next schedule
 
   (schedule-timing :constant {:interval 1000} 500 635)
   => [1500 865]"
  {:added "3.0"}
  ([type props last]
   (schedule-timing type props last (h/time-ms)))
  ([type props last now]
   (let [{:keys [interval] :as m} props
         interval (or *delay*
                      (if (fn? interval) (interval) interval)
                      *interval*)]
     (case type
       :basic [(+ now interval)
               interval]
       :constant [(+ last interval)
                  (- interval (- now last))]))))

(defn wrap-schedule
  "wrapper for the schedule function"
  {:added "3.0"}
  ([{:keys [type] :as program}
    {:keys [props] :as spawn}
    schedule-fn]
   (fn [scheduler loop-fn job-id return last]
     (let [[time interval] (schedule-timing type @props last)
           task (schedule-fn scheduler #(loop-fn job-id return) interval)]
       (add-job spawn job-id {:id job-id
                              :time time
                              :task task
                              :scheduled task
                              :return return})
       [job-id time]))))

(defn spawn-save-past
  "helper function to move spawn from :running to :past"
  {:added "3.0"}
  ([runtime program-id {spawn-id :id :as spawn}]
   (swap! runtime (fn [m]
                    (-> m
                        (update-in [:running program-id] dissoc spawn-id)
                        (update-in [:past program-id] (fnil #(conj % spawn) ())))))
   spawn))

(defn spawn-loop
  "creates a spawn loop"
  {:added "3.0"}
  ([runtime
    {:keys [time-fn  continue-fn] :as program}
    {:keys [props exit board] :as spawn}]
   (let [{:keys [scheduler]} @runtime
         handler-fn  (create-handler program spawn)
         schedule-fn (wrap-schedule program spawn cc/schedule)
         prep-fn (fn prep-fn
                   ([loop-fn last]
                    (let [job-id  (str (h/uuid))
                          return  (h/incomplete)
                          out (schedule-fn scheduler loop-fn job-id return last)
                          _   (set-props spawn :submitted inc)]
                      out)))
         loop-fn (fn loop-fn
                   ([job-id return]
                    (let [[job-id end] (handler-fn runtime job-id)
                          continue (let [{:keys [terminate] :as m} @props]
                                     (and (not terminate) (continue-fn m)))]
                      (if continue
                        (prep-fn loop-fn end)
                        (when (empty? (dissoc @board job-id))
                          (deliver exit true)
                          nil)))))]
     (fn []
       (let [now (h/time-ns)]
         (set-props spawn :started now)
         (prep-fn loop-fn (time-fn (quot now 1000000))))))))

(declare count-spawn)

(defn run
  "constructs and starts a spawn loop"
  {:added "3.0"}
  ([runtime program-id]
   (run runtime program-id {} nil))
  ([runtime program-id opts spawn-id]
   (let [program (merge *defaults*
                        (get-in @runtime [:programs program-id])
                        opts)]
     (let [{:keys [global state interval create-fn delay]} program
           {spawn-id :id :as spawn} (create-spawn program spawn-id)
           spawn-fn (spawn-loop runtime program spawn)
           _  (binding [*delay* (cond (= :rand delay)
                                      (rand-int interval)

                                      :else delay)]
                (spawn-fn))]
       (swap! runtime assoc-in [:running program-id spawn-id] spawn)
       spawn))))

(defn get-all-spawn
  "returns all running spawns"
  {:added "3.0"}
  ([runtime program-id]
   (get-in @runtime [:running program-id])))

(defn get-spawn
  "gets running spawn with id"
  {:added "3.0"}
  ([runtime program-id spawn-id]
   (get-in @runtime [:running program-id spawn-id])))

(defn count-spawn
  "counts all running spawns"
  {:added "3.0"}
  ([runtime program-id]
   (count (get-in @runtime [:running program-id]))))

(defn list-spawn
  "lists all running spawns"
  {:added "3.0"}
  ([runner program-id]
   (list-spawn runner program-id :asc))
  ([runtime program-id order]
   (->> (get-in @runtime [:running program-id])
        (vals)
        (sort-by (fn [{:keys [props]}]
                   (:started (deref props)))
                 (case order :asc < :desc >)))))

(defn latest-spawn
  "returns latest created spawn"
  {:added "3.0"}
  ([runtime program-id]
   (->> (list-spawn runtime program-id :desc)
        first)))

(defn earliest-spawn
  "returns earliest created spawn"
  {:added "3.0"}
  ([runtime program-id]
   (->> (list-spawn runtime program-id :asc)
        first)))

(defn list-stopped
  "lists all stopped spawns"
  {:added "3.0"}
  ([runtime program-id]
   (get-in @runtime [:past program-id])))

(defn latest-stopped
  "returns the most recently stopped spawn"
  {:added "3.0"}
  ([runtime program-id]
   (->> (get-in @runtime [:past program-id])
        first)))

(defn latest
  "returns the latest active or past spawn"
  {:added "3.0"}
  ([runtime program-id]
   (or (latest-spawn runtime program-id)
       (latest-stopped runtime program-id))))

(defn stop-spawn
  "stop a spawn and waits for jobs to finish"
  {:added "3.0"}
  ([runtime program-id spawn-id]
   (when-let [spawn (get-spawn runtime program-id spawn-id)]
     (set-props spawn :terminate true)
     (spawn-save-past runtime program-id spawn)
     spawn)))

(defn kill-spawn
  "stops a spawn and all jobs
 
   (kill-spawn |rt3| :test-program \"s1\")
   => spawn?"
  {:added "3.0"}
  ([runtime program-id spawn-id]
   (when-let [{:keys [exit] :as spawn} (get-spawn runtime program-id spawn-id)]
     (set-props spawn :terminate true)
     (doseq [m (vals @(:board spawn))]
       (let [{:keys [id return task scheduled]} m
             _ (if scheduled (future-cancel scheduled))
             _ (if task (future-cancel task))
             _ (if return
                 (h/future:force return :exception
                                 (ex-info "" {:interrupted spawn-id
                                              :id id})))]
         id))
     (spawn-save-past runtime program-id spawn)
     (deliver exit true)
     spawn)))

(defn stop-all
  "stops all the running tasks
 
   (-> (doto |rt3|
         (stop-all :test-program))
       (count-spawn :test-program))
   => 0"
  {:added "3.0"}
  ([runtime program-id]
   (doseq [{:keys [id]} (list-spawn runtime program-id)]
     (stop-spawn runtime program-id id))))

(defn kill-all
  "kills all the running tasks
 
   (-> (doto |rt3|
         (kill-all :test-program))
       (count-spawn :test-program))
   => 0"
  {:added "3.0"}
  ([runtime program-id]
   (doseq [{:keys [id]} (list-spawn runtime program-id)]
     (kill-spawn runtime program-id id))))

(defn clear
  "clears the program and past spawn information"
  {:added "3.0"}
  ([runtime program-id]
   (swap! runtime (fn [m]
                    (-> m
                        (update :programs dissoc program-id)
                        (update :past dissoc program-id)
                        (update :running dissoc program-id))))))

(defn get-state
  "gets the global state for the program-id
 
   (get-state |rt3| :test-program)
   => {}"
  {:added "3.0"}
  ([runtime program-id]
   (if-let [state (get-in @runtime [:programs program-id :state])]
     @state)))

(defn get-program
  "gets the program given runtime and program-id
 
   (get-program |rt3| :test-program)
   => {}"
  {:added "3.0"}
  ([runtime program-id]
   (get-in @runtime [:programs program-id])))
