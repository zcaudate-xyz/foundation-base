(ns net.openapi.util-test
  (:require [net.http.client :as client]
            [net.openapi.util :refer :all])
  (:use code.test))

^{:refer net.openapi.util/call-api :added "4.0"}
(fact "Call an API by making HTTP request and return its response."
  (with-redefs [client/request identity]
    (call-api "https://api.test"
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
