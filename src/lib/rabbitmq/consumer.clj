(ns lib.rabbitmq.consumer
  (:import [com.rabbitmq.client
            ConnectionFactory
            Consumer
            DefaultConsumer]))

(def ^:dynamic *default-options*
  {:username "guest"
   :password "guest"
   :vhost "/"
   :host "localhost"
   :heartbeat ConnectionFactory/DEFAULT_HEARTBEAT
   :timeout ConnectionFactory/DEFAULT_CONNECTION_TIMEOUT
   :port ConnectionFactory/DEFAULT_AMQP_PORT
   :recovery-interval 5000
   :topology-recovery true})

(defn adapt
  "Creates an adaptor for consuming input."
  {:added "4.1.4"}
  [{:keys [function]} channel]
  (proxy [DefaultConsumer] [channel]
    (handleDelivery [tag envelope properties body]
      (function (String. body)))
    (handleCancel [tag])
    (handleCancelOk [tag])))

(defn consume
  "Creates a consume function."
  {:added "4.1.4"}
  [channel queue autoack tag {:keys [id] :as handler}]
  (.basicConsume channel
                 ^String (name queue)
                 ^Boolean autoack
                 ^String (name (or id tag))
                 ^Consumer (adapt handler channel)))

(defn connect
  "Connects a consumer to the RabbitMQ instance."
  {:added "4.1.4"}
  [{:keys [host port username password vhost heartbeat timeout
           recovery-interval topology-recovery]}]
  (let [cfactory (ConnectionFactory.)]
    (doto cfactory
      (.setUsername ^String username)
      (.setPassword ^String password)
      (.setHost ^String host)
      (.setPort ^int port)
      (.setVirtualHost ^String vhost)
      (.setRequestedHeartbeat ^int heartbeat)
      (.setConnectionTimeout ^int timeout)
      (.setNetworkRecoveryInterval ^int recovery-interval)
      (.setTopologyRecoveryEnabled ^boolean topology-recovery))
    (.newConnection cfactory (str "lib.rabbitmq:" port))))
