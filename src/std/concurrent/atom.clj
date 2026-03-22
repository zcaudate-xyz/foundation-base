(ns std.concurrent.atom
  (:require [std.concurrent.executor :as exe]
            [std.lib.atom :as at]
            [std.lib.env]
            [std.lib.future :as f]
            [std.lib.resource :as res]))

(defn aq:new
  "creates an atom with a vec as queue"
  {:added "3.0"}
  ([]
   (atom [])))

(defn aq:process
  "processes 
 
   (def +state+ (atom []))
   
   (aq:process (fn [elems] (swap! +state+ conj elems))
               (atom [1 2 3 4 5]) 3)
 
   @+state+
   => [[1 2 3] [4 5]]"
  {:added "3.0"}
  ([f aq max-batch]
   (loop []
     (let [queue (at/swap-return! aq
                                  (fn [queue] [queue []]))]
       (->> (partition-all max-batch queue)
            (mapv f))))))

(defn aq:submit
  "submission function for one or multiple entries to aq"
  {:added "3.0"}
  ([executor queue {:keys [target handler max-batch interval] :as opts}]
   (let [bulk-fn (fn []
                   (aq:process (fn [items]
                                 (-> (handler target items)
                                     (std.lib.env/explode)))
                               queue
                               max-batch))]
     (fn [& entries]
       (do (apply swap! queue conj entries)
           (exe/submit-notify executor
                              bulk-fn
                              interval)
           nil)))))

(defn aq:executor
  "creates a executor that takes in an atom queue"
  {:added "3.0"}
  ([{:keys [target] :as opts}]
   (let [queue    (aq:new)
         executor (exe/executor:single 1)
         submit   (aq:submit executor queue opts)]
     {:queue queue
      :executor executor
      :submit submit
      :options opts})))

(std.lib.resource/res:spec-add
 {:type :hara/concurrent.atom.executor
  :config   {:interval 50 :max-batch 1000}
  :instance {:create   aq:executor}})


;;
;; hub executor
;;


(defn hub-state
  "creates a hub state"
  {:added "4.0"}
  [elems]
  {:ticket (f/incomplete)
   :queue  elems})

(defn hub:new
  "creates a trackable atom queue"
  {:added "3.0"}
  ([]
   (hub:new []))
  ([elems]
   (atom (hub-state elems))))

(defn hub:process
  "like aq:process but with a hub
 
   (def +state+ (atom []))
   
   (hub:process (fn [elems] (swap! +state+ conj elems))
                (hub:new [1 2 3 4 5]) 3)
   
   @+state+
   => [[1 2 3] [4 5]]"
  {:added "3.0"}
  ([f hub max-batch]
   (let [[ticket queue] (std.lib.atom/swap-return! hub
                                        (fn [{:keys [ticket queue]}]
                                          [[ticket queue] (hub-state [])]))]
     (->> (partition-all max-batch queue)
          (mapv f)
          (f/future:force ticket)))))

(defn hub:add-entries
  "adds entries to the hub
 
   (hub:add-entries (hub:new) [1 2 3 4 5])
   => (contains [f/future? 0 5])"
  {:added "3.0"}
  ([hub entries]
   (std.lib.atom/swap-return! hub
                   (fn [{:keys [ticket queue] :as m}]
                     (let [start (count queue)]
                       [[ticket start (count entries)] {:ticket ticket
                                                        :queue (into queue entries)}])))))

(defn hub:submit
  "submission function for the hub"
  {:added "3.0"}
  ([object executor hub {:keys [handler max-batch interval]}]
   (let [bulk-fn (fn [] (hub:process (fn [items]
                                       (handler object items))
                                     hub
                                     max-batch))]
     (fn [& entries]
       (let [[ticket start count] (hub:add-entries hub entries)
             hit  (exe/submit-notify executor
                                     bulk-fn
                                     interval)]
         [ticket start count hit])))))

(defn hub:executor
  "creates a hub based executor
   
   (def -exe- (hub:executor nil {:handler (fn [& args]
                                            args)
                                 :interval 50
                                 :max-batch 1000}))
   
   (do (def -res- ((:submit -exe-) 1 2 3 4 5 6))
       @(first -res-))
   => '[(nil (1 2 3 4 5 6))]
 
   (hub:wait (:queue -exe-))
   => nil"
  {:added "3.0"}
  ([object {:keys [handler max-batch interval]
            :as opts}]
   (let [queue    (hub:new)
         executor (exe/executor:single 1)
         submit   (hub:submit object executor queue opts)]
     {:queue queue
      :executor executor
      :submit submit})))

(defn hub:wait
  "waits for the hub executor to be ready"
  {:added "4.0"}
  [hub & [timeout]]
  (let [{:keys [ticket queue]} @hub]
    (if (not (zero? (count queue)))
      (deref ticket (or timeout 1000) :timeout))))
