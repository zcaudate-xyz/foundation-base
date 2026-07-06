(ns documentation.lib-rabbitmq
  (:use code.test))

[[:hero {:title "lib.rabbitmq"
         :subtitle "RabbitMQ management, routing, publishing, and consumers"
         :lead "Manage queues, exchanges, bindings, vhosts, network state, publishing, and consumers through a component-friendly client."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The main client combines connection settings for RabbitMQ and its management API. High-level functions return compact maps for queues, exchanges, bindings, consumers, routing, and network state."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create a client"}]]

(comment
  (require '[lib.rabbitmq :as rabbitmq])

  (def mq
    (rabbitmq/rabbit
     {:host "localhost"
      :port 5672
      :management-port 15672
      :vhost "/"})))

[[:section {:title "Create routing"}]]

(comment
  (-> mq
      (rabbitmq/add-exchange
       "events"
       {:type "topic" :durable true})
      (rabbitmq/add-queue
       "events.audit"
       {:durable true})
      (rabbitmq/bind-queue
       "events"
       "events.audit"
       {:routing-key "audit.*"}))

  (rabbitmq/routing mq))

[[:section {:title "Publish and consume"}]]

(comment
  (rabbitmq/publish mq
                    "events"
                    "created"
                    {:key "audit.created"})

  (rabbitmq/connect mq)
  (rabbitmq/consume mq "events.audit" handle-message))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.rabbitmq"}]]
[[:api {:namespace "lib.rabbitmq.api"}]]
[[:api {:namespace "lib.rabbitmq.consumer"}]]
[[:api {:namespace "lib.rabbitmq.request"}]]
