(ns code.ai.jules-test
  (:require [code.ai.jules :as jules]
            [code.test :refer :all]
            [net.http.client :as client]))

^{:refer code.ai.jules/get-sources :added "0.1"}
(fact "lists available sources"
  (with-redefs [client/request (fn [url opts]
                                 {:status 200
                                  :body (str {:status :return
                                              :data {:sources [{:name "test"}]}})
                                  :headers {}})]
    (jules/get-sources {:api-key "test"})
    => {:sources [{:name "test"}]}))

^{:refer code.ai.jules/create-session :added "0.1"}
(fact "creates a session"
  (with-redefs [client/request (fn [url opts]
                                 {:status 200
                                  :body (str {:status :return
                                              :data {:name "sessions/123"}})
                                  :headers {}})]
    (jules/create-session {:prompt "hello"} {:api-key "test"})
    => {:name "sessions/123"}))

^{:refer code.ai.jules/get-session :added "0.1"}
(fact "gets a session"
  (with-redefs [client/request (fn [url opts]
                                 {:status 200
                                  :body (str {:status :return
                                              :data {:name "sessions/123"}})
                                  :headers {}})]
    (jules/get-session "sessions/123" {:api-key "test"})
    => {:name "sessions/123"}))

^{:refer code.ai.jules/send-message :added "0.1"}
(fact "sends a message"
  (with-redefs [client/request (fn [url opts]
                                 {:status 200
                                  :body (str {:status :return
                                              :data {}})
                                  :headers {}})]
    (jules/send-message "sessions/123" {:prompt "hi"} {:api-key "test"})
    => {}))

^{:refer code.ai.jules/list-activities :added "0.1"}
(fact "lists activities"
  (with-redefs [client/request (fn [url opts]
                                 {:status 200
                                  :body (str {:status :return
                                              :data {:activities []}})
                                  :headers {}})]
    (jules/list-activities "sessions/123" {:api-key "test"})
    => {:activities []}))
