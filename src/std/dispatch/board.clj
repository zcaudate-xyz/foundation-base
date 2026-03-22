(ns std.dispatch.board
  (:require [std.concurrent :as cc]
            [std.dispatch.common :as common]
            [std.dispatch.hooks :as hooks]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.future :as future]
            [std.lib.impl :as impl]
            [std.protocol.component :as protocol.component]
            [std.protocol.dispatch :as protocol.dispatch]))

(def +counter+
  (atom -1))

(defn get-ticket
  "gets a ticket"
  {:added "3.0"}
  ([]
   (swap! +counter+ inc)))

(defn new-board
  "creates a new board
 
   (new-board {})"
  {:added "3.0"}
  ([{:keys [limit]}]
   (atom {:return       {}
          :lookup       {}
          :queues       {}
          :busy         {}
          :dependent    {}
          :submitted    (if limit (cc/queue:fixed limit) (cc/queue))})))

(defn submit-ticket
  "adds ticket to the board
 
   (submit-ticket {} [\"a\" \"b\"] \"t1\")
   => {\"a\" [\"t1\"], \"b\" [\"t1\"]}"
  {:added "3.0"}
  ([m groups ticket]
   (reduce (fn [m group]
             (update m group (fnil #(conj % ticket) (collection/queue))))
           m
           groups)))

(defn submit-board
  "submits an entry to a board
 
   (def -ex- (create-dispatch +test-config+))
 
   (-> -ex-
       (submit-board {:id \"a\"} \"t0\"))
   => (contains [\"t0\" [\"a\"] h/future?])"
  {:added "3.0"}
  ([dispatch entry]
   (submit-board dispatch entry nil))
  ([{:keys [runtime options] :as dispatch} entry ticket]
   (let [{:keys [groups-fn]} (:board options)
         {:keys [board]} runtime
         groups (vec (set (groups-fn dispatch entry)))
         ticket (or ticket (str (get-ticket)))
         _      (cc/put (:submitted @board) ticket)
         return (future/incomplete)
         entry  (swap! board (fn [{:keys [queues lookup busy] :as m}]
                               (-> m
                                   (update :queues submit-ticket groups ticket)
                                   (update :lookup assoc ticket entry)
                                   (update :return assoc ticket return))))]
     [ticket groups return])))

(defn clear-board
  "clears the board
 
   (def -ex- (doto (-> (create-dispatch +test-config+))
               (submit-board {:id \"a\"} \"t0\")
               (poll-board \"a\")))
 
   (clear-board -ex- \"a\" \"t0\")
   => {:group \"a\", :dependent #{\"a\"}}"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch} group ticket]
   (let [{:keys [board]} runtime
         clear-fn (fn [{:keys [dependent] :as m}]
                    (let [out {:group group
                               :dependent (get dependent group)}
                          new (-> m
                                  (update :dependent dissoc group)
                                  (update :busy #(apply dissoc % (get dependent group)))
                                  (update :return dissoc ticket))]
                      [out new]))
         out (atom/swap-return! board clear-fn)]
     out)))

(defn add-dependents
  "add dependents to a given lookup
 
   (add-dependents {:a #{:d}} :a #{:b :c})
   => {:a #{:c :b :d}}"
  {:added "3.0"}
  ([m group dependents]
   (update m group (fnil #(apply conj % dependents) #{}))))

(defn poll-board
  "polls the board for job entry
 
   (def -ex- (doto (create-dispatch +test-config+)
               (submit-board {:id \"a\"} \"t0\")))
 
   (poll-board -ex- \"a\")
   => (contains {:groups [\"a\"], :entry {:id \"a\"}, :ticket \"t0\"})"
  {:added "3.0"}
  ([{:keys [runtime options] :as dispatch} group]
   (let [{:keys [groups-fn]} (:board options)
         {:keys [board]} runtime
         poll-fn (fn [{:keys [return queues lookup busy dependent submitted] :as m}]
                   (if-let [ticket (first (get queues group))]
                     (if-let [entry (lookup ticket)]
                       (let [groups (vec (set (groups-fn dispatch entry)))]
                         (if (and (every? (comp not true?) (map busy groups))
                                  (every? #{ticket}
                                          (map (comp first queues) groups)))
                           (let [run {:groups groups :entry entry :ticket ticket :return (get return ticket)}
                                 new {:submitted submitted
                                      :queues (reduce (fn [queues group]
                                                        (let [new-queue (pop (get queues group))]
                                                          (if (empty? new-queue)
                                                            (dissoc queues group)
                                                            (assoc queues group new-queue))))
                                                      queues
                                                      groups)
                                      :return return
                                      :lookup (dissoc lookup ticket)
                                      :busy   (merge busy (zipmap groups (repeat true)))
                                      :dependent (add-dependents dependent group groups)}]
                             [run new])
                           [nil m]))
                       [nil (update-in m [:queues group] pop)])
                     [nil m]))
         [{:keys [ticket] :as run?} state] (atom/swap-return! board poll-fn true)]
     (if ticket (cc/remove (:submitted state) ticket))
     run?)))

(defn poll-dispatch
  "polls the executor for more work"
  {:added "3.0"}
  ([{:keys [handler runtime] :as dispatch} groups]
   (->> groups
        (keep (fn [group]
                (hooks/on-poll dispatch group)
                (when-let [{:keys [entry ticket return]} (poll-board dispatch group)]
                  (let [handle-fn (fn [] (future/fulfil return (common/handle-fn dispatch entry)))]
                    (try (cc/submit @(:executor runtime) (fn []
                                                           (let [_    (handle-fn)
                                                                 next (clear-board dispatch group ticket)]
                                                             (poll-dispatch dispatch (:dependent next)))))
                         (catch Throwable t
                           (hooks/on-error dispatch entry t)))
                    return))))
        (doall))))

(defn submit-dispatch
  "submits to the board executor"
  {:added "3.0"}
  ([dispatch entry]
   (hooks/on-submit dispatch entry)
   (let [[ticket groups return] (submit-board dispatch entry)
         _ (hooks/on-queued dispatch entry)
         _ (poll-dispatch dispatch groups)]
     return)))

(defn start-dispatch
  "starts the board executor
 
   (test-scaffold +scaffold-config+
                  [[:a :b :c]
                   [:a :b]
                   [:c]
                   [:b :d]
                   [:b :c :d]
                   [:c]
                   [:a]
                  [:b :c]
                   [:c]
                   [:d]
                   [:a]])
   => (contains
       [{:id 0,  :groups [:a :b :c]}
        {:id 2,  :groups [:c]}
        {:id 1,  :groups [:a :b]}
        {:id 3,  :groups [:b :d]}
        {:id 6,  :groups [:a]}
        {:id 10, :groups [:a]}
        {:id 4,  :groups [:b :c :d]}
        {:id 5,  :groups [:c]}
        {:id 9,  :groups [:d]}
        {:id 7,  :groups [:b :c]}
        {:id 8,  :groups [:c]}]
       :in-any-order)"
  {:added "3.0"}
  ([dispatch]
   (-> dispatch
       (common/start-dispatch)
       (assoc-in [:runtime :counter :poll] (atom 0)))))

(defn stop-dispatch
  "stops the board executor"
  {:added "3.0"}
  ([dispatch]
   (-> dispatch
       (update :runtime dissoc :board)
       (common/stop-dispatch))))

(impl/defimpl BoardDispatch [type runtime handler]
  :prefix "common/"
  :suffix "-dispatch"
  :string common/to-string
  :protocols  [std.protocol.component/IComponent
               :method  {-start start-dispatch
                         -stop  stop-dispatch}
               protocol.component/IComponentProps
               protocol.component/IComponentQuery
               :body    {-track-path [:dispatch :board]}

               protocol.dispatch/IDispatch
               :method  {-submit submit-dispatch}
               :body    {-bulk?  false}]

  :interfaces [clojure.lang.IFn
               :method  {invoke {[dispatch entry] submit-dispatch}}])

(def create-dispatch-typecheck identity
  #_(types/<dispatch:board> :strict))

(defn create-dispatch
  "creates the board executor"
  {:added "3.0"}
  ([{:keys [hooks options] :as m}]
   (let [m (-> (assoc m :type :board)
               create-dispatch-typecheck
               (common/create-map))
         board (new-board (:board options))]
     (-> (assoc-in m [:runtime :board] board)
         (map->BoardDispatch)))))

(defmethod protocol.dispatch/-create :board
  ([m]
   (create-dispatch m)))
