(ns net.http.client-test
  (:use code.test)
  (:require [net.http.client :refer :all])
  (:refer-clojure :exclude [get]))

^{:refer net.http.client/wrap-get-optional :added "3.0"}
(fact "wraps function, nil-punning outputs for java.util.Optional"

  ((wrap-get-optional identity) (java.util.Optional/of "hello"))
  => "hello")

^{:refer net.http.client/getter-methods :added "3.0"}
(fact "returns methods of one argument"

  (keys (getter-methods java.net.http.HttpResponse))
  => [:body :headers :previous-response :request
      :ssl-session :status-code :uri :version])

^{:refer net.http.client/http-client-builder :added "3.0"}
(fact "constructor for java.net.http.HttpClientBuilder"

  (http-client-builder)
  => java.net.http.HttpClient$Builder)

^{:refer net.http.client/http-client :added "3.0"}
(fact "constructor for java.net.http.HttpClient"

  (http-client)
  => java.net.http.HttpClient)

^{:refer net.http.client/request-body :added "3.0"}
(fact "constructs a relevant body publisher"

  (request-body "")
  => java.net.http.HttpRequest$BodyPublisher)

^{:refer net.http.client/request-headers :added "3.0"}
(fact "constructs a relevant request header"

  (vec (request-headers {"Content" "html"
                         "Tags" ["a" "b" "c"]}))
  => ["Content" "html"
      "Tags" "a" "Tags" "b" "Tags" "c"])

^{:refer net.http.client/http-request-builder :added "3.0"}
(fact "constructor for java.net.http.HttpRequestBuilder"

  (http-request-builder {:uri "http://www.yahoo.com"})
  => java.net.http.HttpRequest$Builder)

^{:refer net.http.client/http-request :added "3.0"}
(fact "constructor for java.net.http.HttpRequest"

  (http-request "http://www.yahoo.com")
  => java.net.http.HttpRequest)

^{:refer net.http.client/response-map :added "3.0"}
(fact "constucts a map from java.net.http.HttpResponse")

^{:refer net.http.client/request :added "3.0"}
(fact "performs a http request"
  ^:hidden
  
  (comment
    (request "http://www.yahoo.com")
    => (contains {:status number?
                  :body string?
                  :version "HTTP_1_1"
                  :headers map?})))

^{:refer net.http.client/create-http-methods :added "3.0"}
(fact "creates the standard http get, post, etc requests ")

^{:refer net.http.client/endpoint-data :added "3.0"}
(fact "gets the data returned from endpoint"

  (endpoint-data {:status 200
                  :body (str {:data [1 2 3 4]
                              :status :return})})
  => [1 2 3 4])

^{:refer net.http.client/remote :added "3.0"}
(fact "creates a remote access to an endpoint")

^{:refer net.http.client/stream-lines :added "4.0"}
(fact "reads lines from a stream")

^{:refer net.http.client/mock-endpoint :added "3.0"}
(fact "creates a mock-endpoint for testing"

  ((mock-endpoint (fn [req] {:status 200
                             :body (str {:status :return
                                         :data req})}) "/endpoint")
   {:id   :add
    :args [1 2 3]})
  => {:method :post, :route "/endpoint", :body "{:id :add, :args [1 2 3]}"})
