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
(fact "converts keywords, symbols and other values to strings"
  (api/normalize-key :foo) => "foo"
  (api/normalize-key 'bar) => "bar"
  (api/normalize-key "baz") => "baz"
  (api/normalize-key 42) => "42")

^{:refer net.http.api/param->str :added "4.1"}
(fact "converts params to strings, joining sequences with commas"
  (api/param->str :a) => "a"
  (api/param->str 'b) => "b"
  (api/param->str "c") => "c"
  (api/param->str 10) => "10"
  (api/param->str [:a 'b "c"]) => "a,b,c")

^{:refer net.http.api/normalize-param :added "4.1"}
(fact "normalizes a param value to a comma-separated string"
  (api/normalize-param :a) => "a"
  (api/normalize-param [1 2 3]) => "1,2,3"
  (api/normalize-param "x") => "x")

^{:refer net.http.api/normalize-params :added "4.1"}
(fact "removes nil values and normalizes each value"
  (api/normalize-params {:a 1 :b [1 2] :c nil :d :kw})
  => {:a "1" :b "1,2" :d "kw"}
  (api/normalize-params {}) => {})

^{:refer net.http.api/cookie-header :added "4.1"}
(fact "builds a Cookie header string or returns nil when empty"
  (api/cookie-header {}) => nil
  (api/cookie-header {:session "s1"}) => "session=s1"
  (api/cookie-header {:tags ["a" "b"]}) => "tags=a,b")

^{:refer net.http.api/json-content-type? :added "4.1"}
(fact "detects JSON content types case-insensitively"
  (api/json-content-type? "application/json") => "json"
  (api/json-content-type? "application/JSON; charset=utf-8") => "JSON"
  (api/json-content-type? "text/plain") => nil
  (api/json-content-type? nil) => false)

^{:refer net.http.api/form-content-type? :added "4.1"}
(fact "detects form-urlencoded content types"
  (api/form-content-type? "application/x-www-form-urlencoded")
  => "application/x-www-form-urlencoded"
  (api/form-content-type? "application/X-www-form-urlencoded")
  => "application/X-www-form-urlencoded"
  (api/form-content-type? "application/json") => nil
  (api/form-content-type? nil) => false)

^{:refer net.http.api/url-encode :added "4.1"}
(fact "URL-encodes strings, using %20 for spaces"
  (api/url-encode "hello world") => "hello%20world"
  (api/url-encode "a&b") => "a%26b"
  (api/url-encode "foo/bar") => "foo%2Fbar"
  (api/url-encode "a+b") => "a%2Bb")

^{:refer net.http.api/make-url :added "4.1"}
(fact "substitutes path parameters and URL-encodes values"
  (api/make-url "https://api.test" "/users/{id}" {:id 42})
  => "https://api.test/users/42"
  (api/make-url nil "/users/{id}" {:id "a b"})
  => "/users/a%20b"
  (api/make-url "https://api.test" "/users/{id}/posts/{pid}" {:id 1 :pid 2})
  => "https://api.test/users/1/posts/2")

^{:refer net.http.api/encode-query-params :added "4.1"}
(fact "encodes query parameters, indexing vector values"
  (api/encode-query-params {:q "hello world"}) => "q=hello%20world"
  (api/encode-query-params {:items ["a" "b"]}) => "items[1]=a&items[2]=b"
  (api/encode-query-params {}) => ""
  (api/encode-query-params {:a nil}) => "")

^{:refer net.http.api/append-query-string :added "4.1"}
(fact "appends a normalized query string to a URI"
  (api/append-query-string "https://api.test/users" {:q "hello"})
  => "https://api.test/users?q=hello"
  (api/append-query-string "https://api.test/users?foo=bar" {:q "hello"})
  => "https://api.test/users?foo=bar&q=hello"
  (api/append-query-string "https://api.test/users" {})
  => "https://api.test/users")

^{:refer net.http.api/map-lookup :added "4.1"}
(fact "looks up keys with string, keyword, lower-case and upper-case fallback"
  (api/map-lookup {:a 1} "a") => 1
  (api/map-lookup {"foo" 1} "foo") => 1
  (api/map-lookup {"foo" 1} "FOO") => 1
  (api/map-lookup {"FOO" 1} "foo") => 1
  (api/map-lookup {:bar 2} "bar") => 2
  (api/map-lookup {:bar 2} "BAR") => nil)

^{:refer net.http.api/normalize-cookie-params :added "4.1"}
(fact "extracts cookie values from map values"
  (api/normalize-cookie-params {:session {:value "s1"}}) => {:session "s1"}
  (api/normalize-cookie-params {:session "s1"}) => {:session "s1"}
  (api/normalize-cookie-params {:x {:path "/" :value "v"}}) => {:x "v"}
  (api/normalize-cookie-params nil) => {})

^{:refer net.http.api/canonical-opts :added "4.1"}
(fact "canonicalizes request options"
  (api/canonical-opts {:route-params {:id 1}
                       :query-params {:q "hello"}
                       :headers {"X-Token" "abc"}
                       :cookies {"session" {:value "s1"}}
                       :body {:hello "world"}
                       :content-type "application/json"
                       :accepts ["application/json"]
                       :as :bytes})
  => {:path-params {:id 1}
      :query-params {:q "hello"}
      :header-params {"X-Token" "abc"}
      :cookie-params {"session" "s1"}
      :body {:hello "world"}
      :content-type "application/json"
      :accepts ["application/json"]
      :passthrough {:as :bytes}}

  (api/canonical-opts {:headers {"Content-Type" "application/json"
                                 "Accept" "application/json"}
                       :form-params {:hello "world"}
                       :type :async})
  => {:path-params {}
      :query-params {}
      :header-params {"Content-Type" "application/json"
                      "Accept" "application/json"}
      :cookie-params {}
      :body {:hello "world"}
      :content-type "application/json"
      :accepts ["application/json"]
      :passthrough {:type :async}}

  (api/canonical-opts {:body-params {:a 1}})
  => {:path-params {}
      :query-params {}
      :header-params {}
      :cookie-params {}
      :body {:a 1}
      :content-type nil
      :accepts nil
      :passthrough {}})

^{:refer net.http.api/encode-body :added "4.1"}
(fact "encodes bodies based on content type"
  (api/encode-body nil "application/json") => nil
  (api/encode-body "raw" "application/json") => "raw"
  (api/encode-body {:hello "world"} "application/json") => "{\"hello\":\"world\"}"
  (api/encode-body {:hello "world"} "application/JSON") => "{\"hello\":\"world\"}"
  (api/encode-body (array-map :a 1 :b "x") "application/x-www-form-urlencoded")
  => "a=1&b=x"
  (api/encode-body {:a 1} "text/plain") => {:a 1})