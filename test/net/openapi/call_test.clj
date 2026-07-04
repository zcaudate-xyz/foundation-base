(ns net.openapi.call-test
  (:require [net.openapi.call :as call])
  (:use code.test))

(def +entry+
  {:fn-name "create-user"
   :method :post
   :path "/auth/v1/admin/users"
   :body {:required true
          :content-types ["application/json"]}
   :response-content-types ["application/json"]})

(fact "builds a request from normalized OpenAPI entry data"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call (assoc +entry+
                        :path-params []
                        :query-params []
                        :header-params [])
                 {:base-url "http://localhost:54321"
                  :headers {"apikey" "service-key"}
                  :body {"email" "hello@example.com"
                         "password" "secret"}}))
    [(:uri @captured)
     (:method @captured)
     (get-in @captured [:headers "Accept"])
     (get-in @captured [:headers "Content-Type"])
     (:body @captured)])
  => ["http://localhost:54321/auth/v1/admin/users"
      :post
      "application/json"
      "application/json"
      "{\"email\":\"hello@example.com\",\"password\":\"secret\"}"])

(fact "fills path params, query params and cookie headers"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call {:fn-name "get-widget"
                  :method :get
                  :path "/widgets/{widget_id}"
                  :path-params [{:name "widget_id"}]
                  :query-params [{:name "include"}]
                  :cookie-params [{:name "session"}]}
                 {:base-url "https://example.com/api"
                  :path ["abc 123"]
                  :params {:include ["stats" "owner"]}
                  :cookies {:session "token"}}))
    [(:uri @captured)
     (:method @captured)
     (get-in @captured [:headers "Cookie"])])
  => ["https://example.com/api/widgets/abc%20123?include=stats%2Cowner"
      :get
      "session=token"])

(fact "accepts bucket-style request aliases that match normalized OpenAPI entries"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call {:fn-name "get-widget"
                  :method :get
                  :path "/widgets/{widget_id}"}
                 {:base-url "https://example.com/api"
                  :path-params {:widget_id "abc 123"}
                  :query-params {:include ["stats" "owner"]}
                  :header-params {:trace-id "trace-1"}
                  :cookie-params {:session "token"}}))
    [(:uri @captured)
     (get-in @captured [:headers "trace-id"])
     (get-in @captured [:headers "Cookie"])])
  => ["https://example.com/api/widgets/abc%20123?include=stats%2Cowner"
      "trace-1"
      "session=token"])

(fact "lets input override defaults passed as the last argument"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call {:fn-name "create-user"
                  :method :post
                  :path "/auth/v1/admin/users"
                  :body {:required true
                         :content-types ["application/json"]}}
                 {:headers {"apikey" "override-key"}
                  :body {"email" "hello@example.com"}}
                 {:base-url "http://localhost:54321"
                  :headers {"apikey" "default-key"
                            "x-client" "foundation"}
                  :body {"role" "admin"}}))
    [(:uri @captured)
     (get-in @captured [:headers "apikey"])
     (get-in @captured [:headers "x-client"])
     (:body @captured)])
  => ["http://localhost:54321/auth/v1/admin/users"
      "override-key"
      "foundation"
      "{\"role\":\"admin\",\"email\":\"hello@example.com\"}"])

(fact "overrides non-map bodies instead of trying to merge them"
  (call/merge-inputs {:body "{\"role\":\"admin\"}"}
                     {:body "{\"email\":\"hello@example.com\"}"})
  => {:body "{\"email\":\"hello@example.com\"}"})


^{:refer net.openapi.call/path-param-names :added "4.1"}
(fact "extracts path parameter names in order"
  (call/path-param-names "/widgets/{widget_id}/versions/{version_id}")
  => ["widget_id" "version_id"])

^{:refer net.openapi.call/normalize-key :added "4.1"}
(fact "normalizes keyword, symbol and string-like keys"
  [(call/normalize-key :widget_id)
   (call/normalize-key 'version_id)
   (call/normalize-key "trace-id")]
  => ["widget_id" "version_id" "trace-id"])

^{:refer net.openapi.call/normalize-map :added "4.1"}
(fact "normalizes a map to string keys"
  (call/normalize-map {:widget_id 1
                       'version_id 2
                       "trace-id" 3})
  => {"widget_id" 1
      "version_id" 2
      "trace-id" 3})

^{:refer net.openapi.call/normalize-path :added "4.1"}
(fact "accepts path values as either vectors or maps"
  [(call/normalize-path {:path "/widgets/{widget_id}/versions/{version_id}"}
                        ["w1" "v2"])
   (call/normalize-path {:path "/widgets/{widget_id}"}
                        {:widget_id "w9"})]
  => [{"widget_id" "w1"
       "version_id" "v2"}
      {"widget_id" "w9"}])

^{:refer net.openapi.call/append-query-string :added "4.1"}
(fact "appends encoded query params to urls"
  [(call/append-query-string "https://example.com/widgets"
                             {"include" ["stats" "owner"]})
   (call/append-query-string "https://example.com/widgets?active=true"
                             {"limit" 10})]
  => ["https://example.com/widgets?include=stats%2Cowner"
      "https://example.com/widgets?active=true&limit=10"])

^{:refer net.openapi.call/merge-inputs :added "4.1"}
(fact "merges defaults first and lets input override nested request maps"
  (call/merge-inputs {:base-url "http://localhost:54321"
                      :headers {"apikey" "default-key"
                                "x-client" "foundation"}
                      :body {"role" "admin"}
                      :params {"limit" 10}}
                     {:headers {"apikey" "override-key"}
                      :body {"email" "hello@example.com"}
                      :params {"offset" 20}})
  => {:base-url "http://localhost:54321"
      :headers {"apikey" "override-key"
                "x-client" "foundation"}
      :body {"role" "admin"
             "email" "hello@example.com"}
      :params {"limit" 10
               "offset" 20}})

^{:refer net.openapi.call/canonical-input :added "4.1.4"}
(fact "maps bucket-style aliases onto the canonical call input keys"
  (call/canonical-input {:path-params {:widget_id "w1"}
                         :query-params {:include "stats"}
                         :header-params {:trace-id "abc"}
                         :cookie-params {:session "token"}})
  => {:path-params {:widget_id "w1"}
      :query-params {:include "stats"}
      :header-params {:trace-id "abc"}
      :cookie-params {:session "token"}
      :path {:widget_id "w1"}
      :query {:include "stats"}
      :headers {:trace-id "abc"}
      :cookies {:session "token"}})

^{:refer net.openapi.call/call :added "4.1"}
(fact "uses entry defaults in the two-argument arity"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call (assoc +entry+
                        :defaults {:base-url "http://localhost:54321"
                                   :headers {"apikey" "default-key"}
                                   :body {"role" "admin"}})
                 {:body {"email" "hello@example.com"}}))
    [(:uri @captured)
     (get-in @captured [:headers "apikey"])
     (:body @captured)])
  => ["http://localhost:54321/auth/v1/admin/users"
      "default-key"
      "{\"role\":\"admin\",\"email\":\"hello@example.com\"}"])

(fact "supports a one-argument arity when the entry carries defaults"
  (let [captured (atom nil)]
    (with-redefs [net.http.client/request (fn [req]
                                            (reset! captured req)
                                            req)]
      (call/call (assoc +entry+
                        :defaults {:base-url "http://localhost:54321"
                                   :headers {"apikey" "default-key"}})))
    [(:uri @captured)
     (get-in @captured [:headers "apikey"])])
  => ["http://localhost:54321/auth/v1/admin/users"
      "default-key"])

^{:refer net.openapi.call/merge-value :added "4.1"}
(fact "merges maps and lets input override scalar defaults"
  [(call/merge-value {:a 1 :b 2} {:b 3 :c 4})
   (call/merge-value {:a 1} nil)
   (call/merge-value nil {:a 1})
   (call/merge-value nil nil)
   (call/merge-value "default" "input")
   (call/merge-value "default" nil)
   (call/merge-value "default" false)
   (call/merge-value "default" 0)
   (call/merge-value "default" "")]
  => [{:a 1 :b 3 :c 4}
      {:a 1}
      {:a 1}
      nil
      "input"
      "default"
      false
      0
      ""])