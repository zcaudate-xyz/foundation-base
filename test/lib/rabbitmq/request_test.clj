(ns lib.rabbitmq.request-test
  (:use code.test)
  (:require [lib.rabbitmq.request :refer :all]
            [std.json :as json]))

(def sample-rabbit
  {:protocol "http"
   :host "localhost"
   :management-port 15672
   :username "guest"
   :password "guest"})

^{:refer lib.rabbitmq.request/create-url :added "4.1.4"}
(fact "creates the management url"
  (create-url sample-rabbit "hello")
  => "http://localhost:15672/api/hello")

^{:refer lib.rabbitmq.request/wrap-parse-json :added "4.1.4"}
(fact "parses successful json responses"
  ((wrap-parse-json identity)
   {:status 200
    :body (json/write {:a 1 :b 2})})
  => {:a 1 :b 2})

^{:refer lib.rabbitmq.request/update-nested-keys :added "4.1.4"}
(fact "updates nested keys recursively"
  (update-nested-keys {:a {:b {:c 1}}}
                      #(keyword (str (name %) "-boo")))
  => {:a-boo {:b-boo {:c-boo 1}}})

^{:refer lib.rabbitmq.request/wrap-generate-json :added "4.1.4"}
(fact "encodes nested request bodies as snake_case json"
  ((wrap-generate-json identity)
   {:body {:helloWorld {:queueName "q1"}}})
  => {:body "{\"hello_world\":{\"queue_name\":\"q1\"}}"})

^{:refer lib.rabbitmq.request/basic-auth-header :added "4.1.4"}
(fact "creates the basic auth header"
  (basic-auth-header sample-rabbit)
  => "Basic Z3Vlc3Q6Z3Vlc3Q=")

^{:refer lib.rabbitmq.request/request :added "4.1.4"}
(fact "creates a management api request"
  (let [captured (atom nil)]
    (with-redefs [net.http/request (fn [req]
                                     (reset! captured req)
                                     {:status 200
                                      :body (json/write {:name "rabbit@node"})})]
      [(request sample-rabbit "cluster-name")
       @captured]))
  => [{:name "rabbit@node"}
      {:uri "http://localhost:15672/api/cluster-name"
       :method :get
       :headers {"Accept" "application/json"
                 "Content-Type" "application/json"
                 "Authorization" "Basic Z3Vlc3Q6Z3Vlc3Q="}}])
