(ns std.dispatch.queue
  (:require [std.concurrent :as cc]
            [std.dispatch.common :as common]
            [std.dispatch.hooks :as hooks]
            [std.lib.collection]
            [std.lib.impl :refer [defimpl]]
            [std.protocol.component :as protocol.component]
            [std.protocol.dispatch :as protocol.dispatch]))

(def ^:dynamic *max-batch* 1000)

(def +defaults+
  {:pool   {:size 1 :max 1 :keep-alive 0}
   :queue  {:interval 10
            :max-batch 1000}})

(defn start-dispatch
  "starts a queue executor
 
   (-> (create-dispatch +test-config+)
       (start-dispatch)
      (h/stop))"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (let [executor (cc/executor:single 1)]
     (vreset! (:executor runtime) executor)
     (hooks/on-startup dispatch)
     dispatch)))

(defn handler-fn
  "creates a queue handler function"
  {:added "3.0"}
  ([{:keys [handler runtime options] :as dispatch}]
   (let [{:keys [queue]} runtime
         {:keys [max-batch]} (:queue options)]
     (fn []
       (let [results (try (cc/hub:process (fn [entries]
                                            (let [_ (hooks/on-process-bulk dispatch entries)
                                                  _ (hooks/on-batch dispatch)
                                                  results (handler dispatch entries)
                                                  _ (hooks/on-complete-bulk dispatch entries results)]
                                              results))
                                          queue
                                          (or max-batch *max-batch*))
                          (catch Throwable t
                            (hooks/on-error dispatch t)))]
         results)))))

(defn submit-dispatch
  "submits to a queue executor"
  {:added "3.0"}
  ([{:keys [handler runtime options] :as dispatch} entry]
   (let [{:keys [queue]} runtime
         {:keys [interval delay]} (:queue options)
         handler (handler-fn dispatch)
         _  (hooks/on-submit dispatch entry)
         [ticket start count] (cc/hub:add-entries queue [entry])
         hit (cc/submit-notify @(:executor runtime)
                               handler
                               {:min interval
                                :delay delay})
         _ (if (not hit) (hooks/on-poll dispatch entry))]
     [ticket start count])))

(defimpl QueueDispatch [type runtime handler]
  :prefix "common/"
  :suffix "-dispatch"
  :string common/to-string
  :protocols  [std.protocol.component/IComponent
               :method  {-start start-dispatch}
               protocol.component/IComponentQuery
               protocol.component/IComponentProps
               :body    {-get-track-path [:dispatch :queue]}

               protocol.dispatch/IDispatch
               :method  {-submit submit-dispatch}
               :body    {-bulk?  true}]

  :interfaces [clojure.lang.IFn
               :method {invoke {[dispatch entry] submit-dispatch}}])

(def create-dispatch-typecheck identity)

(defn create-dispatch
  "creates a queue executor
 
   ;; DELAY, MAX-BATCH 300
   (test-scaffold +test-config+)
   => (contains [300])
 
   ;; DELAY, MAX-BATCH 500
   (test-scaffold (-> +test-config+
                      (assoc-in [:options :queue :max-batch] 500)))
   => (contains [500])
 
   ;; NO DELAY, MAX-BATCH 300
   (test-scaffold (-> +test-config+
                      (update-in [:options :queue] dissoc :delay)))
   ;; [29 300 300 300 71]
   => #(-> % count (>= 4))
 
   ;; NO DELAY, MAX-BATCH 500
   (test-scaffold (-> +test-config+
                      (update-in [:options :queue] dissoc :delay)
                      (assoc-in  [:options :queue :max-batch] 500)))
   ;; [43 500 457]
   => #(-> % count (<= 3))"
  {:added "3.0"}
  ([{:keys [options] :as m}]
   (-> (assoc m
              :type :queue
              :options (std.lib.collection/merge-nested-new (:options m) +defaults+))
       create-dispatch-typecheck
       #_(types/<dispatch:queue> :strict)
       (common/create-map)
       (update-in [:runtime :counter] dissoc :queued)
       (std.lib.collection/merge-nested {:runtime {:queue   (cc/hub:new)
                                  :counter {:poll  (atom 0)
                                            :batch (atom 0)}}})

       (map->QueueDispatch))))

(defmethod protocol.dispatch/-create :queue
  ([m]
   (create-dispatch m)))
