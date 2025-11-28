(ns std.dispatch.common
  (:require [std.protocol.dispatch  :as protocol.dispatch]
            [std.protocol.component :as protocol.component]
            [std.dispatch.hooks :as hooks]
            [std.concurrent :as cc]
            [std.lib :as h]))

(def +pool-defaults+
  {:keep-alive 1000})

(defn to-string
  "returns the executorstring"
  {:added "3.0"}
  ([{:keys [type display] :as dispatch}]
   (if display
     (str (display dispatch))
     (str (format "#%s.dispatch %s" (name type)
                  (dissoc (protocol.component/-info dispatch :default)
                          :type))))))

(defn info-base
  "returns base executor info"
  {:added "3.0"}
  ([{:keys [runtime type options] :as dispatch}]
   {:type type
    :running false
    :counter (h/map-vals deref (:counter runtime))
    :options options}))

(defn create-map
  "creates the base executor map"
  {:added "3.0"}
  ([{:keys [hooks options] :as m}]
   (let [{:keys [type] :as pool} (:pool options)
         pool (case type
                :shared pool
                :cached pool
                (let [{:keys [size max] :as pool
                       :or {max size}} (merge +pool-defaults+ pool)]
                  (assoc pool :max max)))
         options (assoc options :pool pool)
         runtime {:executor  (volatile! nil)
                  :counter (hooks/counter)}]
     (assoc m
            :runtime runtime
            :options options))))

(defn handle-fn
  "generic handle function for entry"
  {:added "3.0"}
  ([{:keys [handler] :as dispatch} entry]
   (fn []
     (let [_  (hooks/on-process dispatch entry)]
       (try (let [result (handler dispatch entry)]
              (hooks/on-complete dispatch entry result)
              result)
            (catch Throwable t
              (hooks/on-error dispatch entry t)
              (throw t)))))))

(defn await-termination
  "generic await termination function for executor"
  {:added "3.0"}
  ([dispatch]
   (await-termination dispatch nil))
  ([{:keys [runtime] :as dispatch} callback]
   (let [executor @(:executor runtime)]
     (cond (cc/exec:terminated? executor)
           :pass

           (cc/exec:terminating? executor)
           (cc/exec:await-termination executor))

     (if callback (callback dispatch))
     (hooks/on-shutdown dispatch)
     (vreset! (:executor runtime) nil))))

(defn start-dispatch
  "generic start function for executor"
  {:added "3.0"}
  ([{:keys [runtime options] :as dispatch}]
   (let [{:keys [type] :or {type :pool} :as opts}  (:pool options)
         executor (cc/executor (assoc opts :type type))]
     (vreset! (:executor runtime) executor)
     (hooks/on-startup dispatch)
     dispatch)))

(defn stop-dispatch
  "generic stop function for executor"
  {:added "3.0"}
  ([dispatch]
   (stop-dispatch dispatch nil))
  ([{:keys [runtime options] :as dispatch} callback]
   (if (-> options :pool :type (not= :shared))
     (let [executor @(:executor runtime)]
       (h/future (await-termination dispatch callback))
       (cc/exec:shutdown executor)))
   dispatch))

(defn kill-dispatch
  "generic force kill function for executor"
  {:added "3.0"}
  ([dispatch]
   (kill-dispatch dispatch nil))
  ([{:keys [runtime options] :as dispatch} callback]
   (if (-> options :pool :type (not= :shared))
     (let [executor @(:executor runtime)]
       (h/future (await-termination dispatch callback))
       (cc/exec:shutdown-now executor)))
   dispatch))

(defn started?-dispatch
  "checks if executor has started"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (let [{:keys [executor]} runtime]
     (boolean (and executor
                   @executor
                   (not (cc/exec:shutdown? @executor)))))))

(defn stopped?-dispatch
  "checks if executor has stopped"
  {:added "3.0"}
  ([dispatch]
   (not (started?-dispatch dispatch))))

(defn info-dispatch
  "returns generic executor info"
  {:added "3.0"}
  ([dispatch]
   (info-dispatch dispatch :default))
  ([{:keys [runtime] :as dispatch} level]
   (let [executor @(:executor runtime)]
     (cond-> (info-base dispatch)
       executor (merge (cc/executor:info executor #{:current :running}))))))

(defn health-dispatch
  "returns the health of the executor
 
   (let [d (create-map {:options {:pool {:size 1}}})]
     (health-dispatch d))
   => {:status :ok}"
  {:added "3.0"}
  ([dispatch]
   {:status :ok}))

(defn remote?-dispatch
  "returns whether executor is remote"
  {:added "3.0"}
  ([dispatch] false))

(defn props-dispatch
  "returns the props of the executor"
  {:added "3.0"}
  ([{:keys [runtime] :as dispatch}]
   (-> @(:executor runtime)
       cc/executor:props)))

(def +args+
  {:on-startup  []
   :on-shutdown []
   :on-poll     [:group]
   :on-skip     [:skip]
   :on-submit   [:entry]
   :on-queued   [:entry]
   :on-process  [:entry]
   :on-error    [:entry :exception]
   :on-complete [:entry :result]})

(defn check-hooks
  "Checks that hooks conform to arguments"
  {:added "3.0"}
  ([hooks]
   (->> hooks
        (h/map-entries
         (fn [[k func]]
           (if-let [args (get +args+ k)]
             (do (h/arg-check func (inc (count args))
                              (str "Required inputs " k " - " (cons :dispatch args)))
                 [k func])
             (throw (ex-info "Key not available" {:key k}))))))))
