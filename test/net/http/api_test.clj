(ns net.http.api-test
  (:require [net.http :as http]
            [net.http.api :as api]
            [net.http.client :as client])
  (:use code.test))

^{:refer net.http.api/call-api :added "4.1.4"}
(fact "builds request maps for generic API wrappers"
  (with-redefs [client/request identity]
    (api/call-api "https://api.test"
                  "/users/{id}"
                  :post
                  {:path-params {:id 42}
                   :query-params {:q "hello"}
                   :header-params {"X-Token" "abc"}
                   :cookie-params {"session" "s1"}
                   :content-type "application/json"
                   :accepts ["application/json"]
                   :body {:hello "world"}}))
  => {:uri "https://api.test/users/42"
      :method :post
      :query-params {:q "hello"}
      :headers {"X-Token" "abc"
                "Cookie" "session=s1"
                "Accept" "application/json"
                "Content-Type" "application/json"}
      :body "{\"hello\":\"world\"}"})

^{:refer net.http/call-api :added "4.1.4"}
(fact "interns call-api onto net.http"
  (with-redefs [client/request identity]
    (http/call-api "https://api.test"
                   "/users/{id}"
                   :get
                   {:path-params {:id 42}}))
  => {:uri "https://api.test/users/42"
      :method :get})
