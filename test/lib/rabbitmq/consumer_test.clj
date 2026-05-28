(ns lib.rabbitmq.consumer-test
  (:use code.test)
  (:require [lib.rabbitmq.consumer :refer :all])
  (:import (com.rabbitmq.client DefaultConsumer Channel Consumer)
           (java.lang.reflect Proxy InvocationHandler)))

(defn proxy-channel
  []
  (let [calls (atom [])]
    {:calls calls
     :channel (Proxy/newProxyInstance
               (.getClassLoader Channel)
               (into-array Class [Channel])
               (reify InvocationHandler
                 (invoke [_ _ method args]
                   (let [name (.getName method)
                         args (vec (or args []))]
                     (swap! calls conj [name args])
                     (case name
                       "basicConsume" "consumer-tag"
                       nil)))))}))

^{:refer lib.rabbitmq.consumer/adapt :added "4.1.4"}
(fact "creates an adaptor for consuming input"
  (let [out (atom nil)
        consumer (adapt {:function #(reset! out %)} nil)]
    [(.handleDelivery ^DefaultConsumer consumer "tag" nil nil (.getBytes "hello"))
     @out
     (instance? Consumer consumer)])
  => [nil "hello" true])

^{:refer lib.rabbitmq.consumer/consume :added "4.1.4"}
(fact "creates a consume function"
  (let [{:keys [calls channel]} (proxy-channel)]
    [(consume channel :queue-a true :tag-a {:id :consumer-a
                                            :function identity})
     @calls])
  => #(let [[tag calls] %
            [[name args]] calls]
        (and (= "consumer-tag" tag)
             (= "basicConsume" name)
             (= "queue-a" (nth args 0))
             (= true (nth args 1))
             (= "consumer-a" (nth args 2))
             (instance? Consumer (nth args 3)))))

^{:refer lib.rabbitmq.consumer/connect :added "4.1.4"}
(fact "connects the consumer to the rabbitmq instance"
  (connect (assoc *default-options*
                  :host "127.0.0.1"
                  :port 1
                  :timeout 10))
  => (throws Exception))
