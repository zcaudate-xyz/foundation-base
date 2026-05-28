(ns lib.rabbitmq-test
  (:use code.test)
  (:require [lib.rabbitmq :refer :all]))

(def sample-rabbit
  (create-client {:host "localhost"
                  :vhost "/"
                  :username "guest"}))

^{:refer lib.rabbitmq/vhost-encode :added "4.1.4"}
(fact "encodes vhosts for the management api"
  (vhost-encode "/")
  => "%2F")

^{:refer lib.rabbitmq/create-client :added "4.1.4"}
(fact "creates a rabbitmq client map"
  (select-keys sample-rabbit [:protocol :host :management-port :vhost-encode])
  => {:protocol "http"
      :host "localhost"
      :management-port 15672
      :vhost-encode "%2F"})

^{:refer lib.rabbitmq/list-queues :added "4.1.4"}
(fact "lists queues as a keyed map"
  (with-redefs [lib.rabbitmq.api/list-queues (fn [_]
                                               [{:name "q1" :durable true}
                                                {:name "q2" :exclusive true}])]
    (list-queues sample-rabbit))
  => {"q1" {:durable true}
      "q2" {:exclusive true}})

^{:refer lib.rabbitmq/add-queue :added "4.1.4"}
(fact "adds a queue and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/add-queue (fn [_ name opts] (swap! calls conj [name opts]))]
      [(add-queue sample-rabbit "q1" {:durable true})
       @calls]))
  => [sample-rabbit
      [["q1" {:durable true}]]])

^{:refer lib.rabbitmq/delete-queue :added "4.1.4"}
(fact "deletes a queue and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/delete-queue (fn [_ name] (swap! calls conj name))]
      [(delete-queue sample-rabbit "q1")
       @calls]))
  => [sample-rabbit ["q1"]])

^{:refer lib.rabbitmq/list-exchanges :added "4.1.4"}
(fact "filters default exchanges from exchange listings"
  (with-redefs [lib.rabbitmq.api/list-exchanges (fn [_]
                                                  [{:name "amq.direct" :type "direct"}
                                                   {:name "ex1" :type "topic" :durable true}])]
    (list-exchanges sample-rabbit))
  => {"ex1" {:type "topic" :durable true}})

^{:refer lib.rabbitmq/add-exchange :added "4.1.4"}
(fact "adds an exchange and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/add-exchange (fn [_ name opts] (swap! calls conj [name opts]))]
      [(add-exchange sample-rabbit "ex1" {:type "topic"})
       @calls]))
  => [sample-rabbit
      [["ex1" {:type "topic"}]]])

^{:refer lib.rabbitmq/delete-exchange :added "4.1.4"}
(fact "deletes an exchange and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/delete-exchange (fn [_ name] (swap! calls conj name))]
      [(delete-exchange sample-rabbit "ex1")
       @calls]))
  => [sample-rabbit ["ex1"]])

^{:refer lib.rabbitmq/list-bindings :added "4.1.4"}
(fact "groups bindings by source and destination type"
  (with-redefs [lib.rabbitmq.api/list-bindings (fn [_]
                                                 [{:source "ex1" :destination "q1" :destination-type "queue" :routing-key ""}
                                                  {:source "ex1" :destination "ex2" :destination-type "exchange" :routing-key "a"}])]
    (list-bindings sample-rabbit))
  => {"ex1" {:queues {"q1" [{:routing-key ""}]}
             :exchanges {"ex2" [{:routing-key "a"}]}}})

^{:refer lib.rabbitmq/bind-exchange :added "4.1.4"}
(fact "binds an exchange and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/bind-exchange (fn [_ source dest opts]
                                                   (swap! calls conj [source dest opts]))]
      [(bind-exchange sample-rabbit "ex1" "ex2" {:routing-key "a"})
       @calls]))
  => [sample-rabbit
      [["ex1" "ex2" {:routing-key "a"}]]])

^{:refer lib.rabbitmq/bind-queue :added "4.1.4"}
(fact "binds a queue and returns the client"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/bind-queue (fn [_ source dest opts]
                                                (swap! calls conj [source dest opts]))]
      [(bind-queue sample-rabbit "ex1" "q1" {:routing-key ""})
       @calls]))
  => [sample-rabbit
      [["ex1" "q1" {:routing-key ""}]]])

^{:refer lib.rabbitmq/list-consumers :added "4.1.4"}
(fact "lists consumers grouped by queue"
  (with-redefs [lib.rabbitmq.api/list-consumers (fn [_]
                                                  [{:queue {:name "q1"}
                                                    :consumer-tag "c1"
                                                    :channel-details {:peer-host "host"}}])]
    (list-consumers sample-rabbit))
  => {"q1" {:c1 {:peer-host "host"}}})

^{:refer lib.rabbitmq/publish :added "4.1.4"}
(fact "publishes a message through the management api"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/add-message (fn [_ exchange body]
                                                 (swap! calls conj [exchange body])
                                                 {:status "ok"})]
      [(publish sample-rabbit "ex1" "hello" {:key "a"})
       @calls]))
  => [{:status "ok"}
      [["ex1" {:routing-key "a"
               :payload "hello"
               :payload-encoding "string"
               :properties {}}]]])

^{:refer lib.rabbitmq/routing :added "4.1.4"}
(fact "returns the routing summary for a vhost"
  (with-redefs [list-queues (fn [_] {"q1" {:durable true}})
                list-exchanges (fn [_] {"ex1" {:type "topic"}})
                list-bindings (fn [_] {"ex1" {:queues {"q1" [{}]}}})]
    (routing sample-rabbit))
  => {:queues {"q1" {:durable true}}
      :exchanges {"ex1" {:type "topic"}}
      :bindings {"ex1" {:queues {"q1" [{}]}}}})

^{:refer lib.rabbitmq/network :added "4.1.4"}
(fact "returns the rabbitmq network summary"
  (with-redefs [lib.rabbitmq.api/list-vhosts (fn [_] [{:name "/"}])
                lib.rabbitmq.api/list-connections (fn [_] [{:name "conn" :peer-port 1 :peer-host "a" :host "b" :port 2}])
                lib.rabbitmq.api/list-channels (fn [_] [{:connection-details {:name "conn"} :name "ch1"}])
                lib.rabbitmq.api/cluster-name (fn [_] {:name "rabbit@node"})
                lib.rabbitmq.api/list-nodes (fn [_] [{:name "rabbit@node"}])]
    (network sample-rabbit))
  => {:cluster-name "rabbit@node"
      :nodes ["rabbit@node"]
      :vhosts ["/"]
      :connections [{:name "conn" :peer-port 1 :peer-host "a" :host "b" :port 2}]
      :channels {"conn" #{{:connection-details {:name "conn"} :name "ch1"}}}})

^{:refer lib.rabbitmq/routing-all :added "4.1.4"}
(fact "returns routing summaries for all vhosts"
  (with-redefs [lib.rabbitmq.api/list-vhosts (fn [_] [{:name "/"} {:name "app"}])
                routing (fn [mq] {:queues {(str (:vhost mq)) {}}})]
    (routing-all sample-rabbit {}))
  => {"/" {:queues {"/" {}}}
      "app" {:queues {"app" {}}}})

^{:refer lib.rabbitmq/install-vhost :added "4.1.4"}
(fact "installs a missing vhost and permissions"
  (let [calls (atom [])]
    (with-redefs [lib.rabbitmq.api/list-vhosts (fn [_] [{:name "existing"}])
                  lib.rabbitmq.api/add-vhost (fn [_ vhost body] (swap! calls conj [:vhost vhost body]))
                  lib.rabbitmq.api/add-permissions (fn [_ user body] (swap! calls conj [:permissions user body]))]
      [(install-vhost (assoc sample-rabbit :vhost "new" :username "guest"))
       @calls]))
  => [(assoc sample-rabbit :vhost "new" :username "guest")
      [[:vhost "new" {}]
       [:permissions "guest" {:configure ".*" :write ".*" :read ".*"}]]])

^{:refer lib.rabbitmq/rabbit :added "4.1.4"}
(fact "creates a rabbitmq record"
  (-> (rabbit {:host "rabbit"})
      (select-keys [:host :management-port :vhost-encode]))
  => {:host "rabbit"
      :management-port 15672
      :vhost-encode "%2F"})
