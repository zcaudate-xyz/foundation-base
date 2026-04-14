(ns net.http.router-test
  (:require [net.http.router :refer :all]
            [std.lib.bin :as bin]
            [std.lib.env :as env])
  (:use code.test))

^{:refer net.http.router/compare-masks :added "4.0"}
(fact "compares two path masks for sorting"
  [(compare-masks ["GET" "*" "**"] ["GET" "users" "*"])
   (compare-masks ["GET" "users"] ["GET" "*"])]
  => [1 -1])

^{:refer net.http.router/make-matcher :added "4.0"}
(fact "Given set of routes, builds matcher structure."
  (make-matcher {"GET /users/*" :user
                 "GET /users" :users})
  => [[["GET" "users"] :users]
      [["GET" "users" "*"] :user]])

^{:refer net.http.router/match-impl :added "4.0"}
(fact "matches path against matcher structure"
  (match-impl (make-matcher {"GET /users/*" :user})
              ["GET" "users" "42"])
  => [:user ["42"]])

^{:refer net.http.router/match :added "4.0"}
(fact "matches a path against a matcher"
  (match (make-matcher {"GET /users/*" :user})
         "GET /users/42")
  => [:user ["42"]])

^{:refer net.http.router/router :added "4.0"}
(fact "creates a ring router from routes"
  ((router {"GET /users/*" (fn [req] (str (:path-params req)))})
   {:request-method :get
    :uri "/users/42"})
  => {:status 200
      :headers {"Content-Type" "application/json"}
      :body "[\"42\"]"})

^{:refer net.http.router/serve-resource :added "4.0"}
(fact "serves a static resource"
  (with-redefs [env/sys:resource (fn [_]
                                   (java.io.ByteArrayInputStream. (.getBytes "body")))]
    (let [resp (serve-resource "/index.html" "public")]
      [(select-keys resp [:status :headers])
       (slurp (:body resp))]))
  => [{:status 200
       :headers {"Content-Type" "text/html"}}
      "body"])

^{:refer net.http.router/split-path :added "4.0"}
(fact "splits a path into components"
  (split-path "GET /users/42")
  => ["GET" "users" "42"])

^{:refer net.http.router/match-path :added "4.0"}
(fact "checks if a path matches a mask"
  [(match-path ["GET" "users" "*"] ["GET" "users" "42"])
   (match-path ["GET" "**"] ["GET" "users" "42"])]
  => [["42"] ["users/42"]])
