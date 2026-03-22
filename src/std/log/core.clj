(ns std.log.core
  (:require [std.concurrent :as cc]
            [std.lib.component]
            [std.lib.env]
            [std.lib.time]
            [std.log.common :as common]
            [std.log.match :as match]
            [std.protocol.component :as protocol.component]
            [std.protocol.log :as protocol.log])
  (:import (java.text SimpleDateFormat)))

(defn logger-submit
  "basic submit function"
  {:added "3.0"}
  ([{:keys [instance] :as logger} entry]
   (let [{:keys [executor]} @instance]
     ((:submit executor) entry)
     :logger/enqueued)))

(defn logger-process
  "a special :fn key for processing the input"
  {:added "3.0"}
  ([{:log/keys [value] :fn/keys [process] :as item}]
   (if process
     (merge item (process value))
     item)))

(defn logger-enqueue
  "submits a task to the logger job queue"
  {:added "3.0"}
  ([{:keys [instance] :as logger} entry]
   (let [{:keys [context] :as params} @instance]
     (if (match/match params entry)
       (logger-submit logger (logger-process (merge context entry)))
       :logger/ignored))))

(defn process-exception
  "converts an exception into a map
 
   (process-exception (ex-info \"Error\" {}))
   => (contains-in {:cause \"Error\",
                    :via [{:message \"Error\", :data {}}],
                    :trace [], :data {}, :message \"Error\"})"
  {:added "3.0"}
  ([^Throwable ex]
   (-> (Throwable->map ex)
       (assoc :message (.getMessage ex))
       (update :trace (fn [trace]
                        (->> trace
                             (filter (fn [[sym]]
                                       (let [sym (name sym)]
                                         (not (or (.startsWith sym "clojure.")
                                                  (.startsWith sym "std.log.")
                                                  (.startsWith sym "java.")
                                                  (.startsWith sym "nrepl."))))))
                             (vec)))))))

(defn logger-message
  "constructs a logger message
 
   (logger-message :debug {} nil)
   => map?"
  {:added "3.0"}
  ([level {:log/keys [timestamp] :as context} exception]
   (cond-> context
     (nil? (:log/level context)) (assoc :log/level level)
     (nil? timestamp) (assoc :log/timestamp (std.lib.time/time-ns))
     exception (assoc :log/exception (if (instance? Throwable exception)
                                       (process-exception exception)
                                       exception))
     :then (merge common/*overwrite*))))

(defn logger-start
  "starts the logger, initating a queue and executor"
  {:added "3.0"}
  ([{:keys [instance] :as logger}]
   (let [opts (assoc @instance
                     :handler protocol.log/-logger-process
                     :target logger)
         executor (cc/aq:executor opts)]
     (swap! instance merge {:executor executor})
     logger)))

(defn logger-info
  "returns information about the logger
 
   (logger-info (common/default-logger))
   => {:level :debug, :type :console}"
  {:added "3.0"}
  ([{:keys [instance] :as logger}]
   (let [{:keys [queue] :as m} @instance]
     (-> (dissoc m :executor)
         (merge (select-keys logger [:type :level :interval :max-batch]))))))

(defn logger-stop
  "stops the logger, queue and executor"
  {:added "3.0"}
  ([{:keys [instance] :as logger}]
   (cc/exec:shutdown (-> @instance :executor :executor))
   (swap! instance dissoc :executor) logger))

(defn logger-init
  "sets defaults for the logger
 
   (logger-init {:type :basic})
   => (contains {:type :basic,
                 :instance (stores {:level :debug})})"
  {:added "3.0"}
  ([{:keys [type level interval max-batch] :as m}]
   (let [ks [:level :interval :max-batch :namespace :function :filter]
         instance (select-keys m ks)]
     (merge {:type type
             :instance (atom (merge {:level :debug}
                                    instance))}
            (apply dissoc m ks)))))

(defrecord IdentityLogger [instance]

  Object
  (toString [logger] (str "#log.identity" (logger-info logger)))

  protocol.log/ILogger
  (-logger-write [{:keys [instance]} entry]
    (if (match/match @instance entry)
      entry)))

(defmethod print-method IdentityLogger
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defmethod protocol.log/-create :identity
  ([m]
   (-> (logger-init m)
       (map->IdentityLogger))))

(defn identity-logger
  "creates a identity logger
 
   (identity-logger)"
  {:added "3.0"}
  ([] (identity-logger nil))
  ([m]
   (protocol.log/-create (assoc m :type :identity))))

(defrecord MultiLogger [instance loggers]

  Object
  (toString [_] (str "#log.multi" (assoc @instance :loggers loggers)))

  protocol.log/ILogger
  (-logger-write [{:keys [loggers instance]} entry]
    (let [{:keys [context]} @instance]
      (doseq [logger loggers]
        (protocol.log/-logger-write logger (merge context entry)))))

  protocol.component/IComponent
  (-start [logger]
    (update logger :loggers (fn [loggers]
                              (mapv (fn [{:keys [import as] :as m}]
                                      (cond-> (protocol.log/-create m)
                                        :then (std.lib.component/start)
                                        import (assoc as (get logger import))))
                                    loggers))))

  (-stop [{:keys [instance] :as logger}]
    (update logger :loggers (fn [loggers]
                              (mapv std.lib.component/stop loggers)
                              (:loggers @instance)))))

(defmethod print-method MultiLogger
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defmethod protocol.log/-create :multi
  ([{:keys [type loggers] :as m}]
   (-> {:type type
        :instance (atom (dissoc m :type))
        :loggers loggers}
       (map->MultiLogger))))

(defn multi-logger
  "creates multiple loggers
 
   (multi-logger {:loggers [{:type :console
                             :interval 500
                            :max-batch 10000}
                            {:type :basic
                             :interval 500
                             :max-batch 10000}]})"
  {:added "3.0"}
  ([m]
   (-> (protocol.log/-create (assoc m :type :multi))
       (std.lib.component/start))))

(defn log-raw
  "sends raw data to the logger"
  {:added "3.0"}
  ([data]
   log-raw (common/default-logger) data)
  ([logger data]
   (logger-submit logger {:log/raw data})))

(defn basic-write
  "writes to the logger
 
   (print/with-out-str
     (basic-write [{:a 1 :b 2}] false))
   => \"[{:a 1, :b 2}]\\n\""
  {:added "3.0"}
  ([item pretty]
   (let [item (if (std.lib.component/component? item)
                (std.lib.component/info item)
                item)]
     (if (false? pretty)
       (std.lib.env/local :prn item)
       (do (std.lib.env/local :pprint item)
           (std.lib.env/local :println))))))

(defrecord BasicLogger [instance]

  Object
  (toString [logger] (str "#log.basic" (logger-info logger)))

  protocol.log/ILogger
  (-logger-write [{:keys [pretty]} entry]
    (if (match/match @instance entry)
      (basic-write entry pretty))))

(defmethod print-method BasicLogger
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defmethod protocol.log/-create :basic
  ([m]
   (-> (logger-init m)
       (map->BasicLogger))))

(defn basic-logger
  "constructs a basic logger
 
   (basic-logger)
   => std.log.core.BasicLogger"
  {:added "3.0"}
  ([] (basic-logger nil))
  ([m]
   (-> (protocol.log/-create (assoc m :type :basic))
       (std.lib.component/start))))

(defmacro step
  "conducts a step that is logged"
  {:added "3.0"}
  ([message & body]
   `(do (log-raw (str ~message "... "))
        ~@body
        (log-raw "DONE.\n"))))
