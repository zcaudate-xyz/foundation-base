(ns lib.rabbitmq
  (:require [lib.rabbitmq.api :as api]
            [lib.rabbitmq.consumer :as consumer]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.protocol.component :as protocol.component])
  (:import java.net.URLEncoder))

(def ^:dynamic *default-options*
  {:protocol "http"
   :host "localhost"
   :port 5672
   :management-port 15672
   :username "guest"
   :password "guest"
   :vhost "/"})

(def ^:dynamic *default-exchanges*
  #{"" "amq.direct" "amq.fanout" "amq.headers"
    "amq.match" "amq.rabbitmq.trace" "amq.rabbitmq.log" "amq.topic"})

(defn vhost-encode
  "Encodes a vhost for the management API."
  {:added "4.1.4"}
  [vhost]
  (URLEncoder/encode (str vhost) "UTF-8"))

(defn create-client
  "Creates a RabbitMQ management client."
  {:added "4.1.4"}
  ([] (create-client {}))
  ([m]
   (let [m (merge *default-options* m)]
     (assoc m :vhost-encode (vhost-encode (:vhost m))))))

(defrecord RabbitMQ []
  Object
  (toString [mq]
    (str "#rabbitmq" (select-keys mq [:host :port :vhost])))

  protocol.component/IComponent
  (-start [mq] mq)
  (-stop [mq] mq)
  (-kill [mq] mq))

(defn rabbit
  "Creates a RabbitMQ client instance."
  {:added "4.1.4"}
  ([] (rabbit {}))
  ([m]
   (-> (map->RabbitMQ (create-client m))
       (component/start))))

(defn list-queues
  "Lists queues in the current vhost."
  {:added "4.1.4"}
  [mq]
  (->> (api/list-queues mq)
       (reduce (fn [out {:keys [name] :as data}]
                 (assoc out name (select-keys data [:exclusive :auto-delete :durable])))
               {})))

(defn add-queue
  "Adds a queue and returns the client."
  {:added "4.1.4"}
  ([mq name]
   (add-queue mq name {}))
  ([mq name opts]
   (api/add-queue mq name opts)
   mq))

(defn delete-queue
  "Deletes a queue and returns the client."
  {:added "4.1.4"}
  [mq name]
  (api/delete-queue mq name)
  mq)

(defn list-exchanges
  "Lists non-default exchanges in the current vhost."
  {:added "4.1.4"}
  [mq]
  (->> (api/list-exchanges mq)
       (remove #(-> % :name *default-exchanges*))
       (reduce (fn [out {:keys [name] :as data}]
                 (assoc out name (select-keys data [:type :internal :auto-delete :durable])))
               {})))

(defn add-exchange
  "Adds an exchange and returns the client."
  {:added "4.1.4"}
  ([mq name]
   (add-exchange mq name {}))
  ([mq name opts]
   (api/add-exchange mq name opts)
   mq))

(defn delete-exchange
  "Deletes an exchange and returns the client."
  {:added "4.1.4"}
  [mq name]
  (api/delete-exchange mq name)
  mq)

(defn list-bindings
  "Lists bindings grouped by source exchange."
  {:added "4.1.4"}
  [mq]
  (->> (api/list-bindings mq)
       (remove #(-> % :source empty?))
       (reduce (fn [out {:keys [source destination destination-type] :as data}]
                 (update-in out [source (keyword (str destination-type "s")) destination]
                            (fnil #(conj % (dissoc data :source :vhost :destination :destination-type))
                                  [])))
               {})))

(defn bind-exchange
  "Creates an exchange binding and returns the client."
  {:added "4.1.4"}
  ([mq source dest]
   (bind-exchange mq source dest {}))
  ([mq source dest opts]
   (api/bind-exchange mq source dest opts)
   mq))

(defn bind-queue
  "Creates a queue binding and returns the client."
  {:added "4.1.4"}
  ([mq source dest]
   (bind-queue mq source dest {}))
  ([mq source dest opts]
   (api/bind-queue mq source dest opts)
   mq))

(defn list-consumers
  "Lists consumers grouped by queue."
  {:added "4.1.4"}
  [mq]
  (->> (api/list-consumers mq)
       (map (fn [m]
              {:queue (-> m :queue :name)
               :id (:consumer-tag m)
               :details (:channel-details m)}))
       (reduce (fn [out {:keys [queue id details]}]
                 (assoc-in out [queue (keyword id)] details))
               {})))

(defn publish
  "Publishes a message to an exchange."
  {:added "4.1.4"}
  ([mq exchange message]
   (publish mq exchange message {}))
  ([mq exchange message opts]
   (api/add-message mq exchange {:routing-key (or (:key opts) "")
                                 :payload message
                                 :payload-encoding (or (:encoding opts) "string")
                                 :properties (dissoc opts :key :encoding)})))

(defn routing
  "Returns the queue, exchange, and binding summary for a single vhost."
  {:added "4.1.4"}
  [mq]
  {:queues (list-queues mq)
   :exchanges (list-exchanges mq)
   :bindings (list-bindings mq)})

(defn routing-all
  "Lists routing information for all vhosts."
  {:added "4.1.4"}
  [rabbitmq opts]
  (let [vhosts (->> (api/list-vhosts rabbitmq)
                    (mapv :name))]
    (->> vhosts
         (map (fn [vhost]
                [vhost (routing (assoc rabbitmq
                                       :vhost vhost
                                       :vhost-encode (vhost-encode vhost)))]))
         (into {}))))

(defn network
  "Returns the RabbitMQ network summary."
  {:added "4.1.4"}
  [rabbitmq]
  (let [vhosts (->> (api/list-vhosts rabbitmq)
                    (mapv :name))
        connections (->> (api/list-connections rabbitmq)
                         (map #(select-keys % [:name :peer-port :peer-host :host :port])))
        channels (->> (api/list-channels rabbitmq)
                      (reduce (fn [out data]
                                (update-in out [(-> data :connection-details :name)] (fnil conj #{}) data))
                              {}))
        cluster-name (:name (api/cluster-name rabbitmq))
        nodes (->> (api/list-nodes rabbitmq)
                   (mapv :name))]
    {:cluster-name cluster-name
     :nodes nodes
     :vhosts vhosts
     :connections connections
     :channels channels}))

(defn install-vhost
  "Installs a vhost and adds user permissions."
  {:added "4.1.4"}
  [{:keys [vhost username] :as rabbitmq}]
  (let [curr (->> (api/list-vhosts rabbitmq)
                  (map :name)
                  set)]
    (when-not (curr vhost)
      (api/add-vhost rabbitmq vhost {})
      (api/add-permissions rabbitmq username {:configure ".*"
                                              :write ".*"
                                              :read ".*"})))
  rabbitmq)

(f/intern-in consumer/adapt
             consumer/consume
             consumer/connect)
