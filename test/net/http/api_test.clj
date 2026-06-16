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
  => {:uri "https://api.test/users/42?q=hello"
      :method :post
      :headers {"X-Token" "abc"
                "Cookie" "session=s1"
                "Accept" "application/json"
                "Content-Type" "application/json"}
      :body "{\"hello\":\"world\"}"})

(fact "accepts ring-style params and forwards extra request options"
  (with-redefs [client/request identity]
    (api/call-api "https://api.test"
                 "/users/{id}"
                 :post
                 {:route-params {:id 42}
                  :query-params {:q "hello"}
                  :headers {"X-Token" "abc"
                            "Accept" "application/json"}
                  :cookies {"session" {:value "s1"}}
                  :form-params {:hello "world"}
                  :as :bytes
                  :type :async}))
  => {:uri "https://api.test/users/42?q=hello"
      :method :post
      :headers {"X-Token" "abc"
               "Accept" "application/json"
               "Cookie" "session=s1"
               "Content-Type" "application/x-www-form-urlencoded"}
      :body "hello=world"
      :as :bytes
      :type :async})

^{:refer net.http/call-api :added "4.1.4"}
(fact "interns call-api onto net.http"
  (with-redefs [client/request identity]
    (http/call-api "https://api.test"
                   "/users/{id}"
                   :get
                   {:path-params {:id 42}}))
  => {:uri "https://api.test/users/42"
      :method :get})


^{:refer net.http.api/normalize-key :added "4.1"}
(fact "TODO")

^{:refer net.http.api/param->str :added "4.1"}
(fact "TODO")

^{:refer net.http.api/normalize-param :added "4.1"}
(fact "TODO")

^{:refer net.http.api/normalize-params :added "4.1"}
(fact "TODO")

^{:refer net.http.api/cookie-header :added "4.1"}
(fact "TODO")

^{:refer net.http.api/json-content-type? :added "4.1"}
(fact "TODO")

^{:refer net.http.api/form-content-type? :added "4.1"}
(fact "TODO")

^{:refer net.http.api/url-encode :added "4.1"}
(fact "TODO")

^{:refer net.http.api/make-url :added "4.1"}
(fact "TODO")

^{:refer net.http.api/encode-query-params :added "4.1"}
(fact "TODO")

^{:refer net.http.api/append-query-string :added "4.1"}
(fact "TODO")

^{:refer net.http.api/map-lookup :added "4.1"}
(fact "TODO")

^{:refer net.http.api/normalize-cookie-params :added "4.1"}
(fact "TODO")

^{:refer net.http.api/canonical-opts :added "4.1"}
(fact "TODO")

^{:refer net.http.api/encode-body :added "4.1"}
(fact "TODO")