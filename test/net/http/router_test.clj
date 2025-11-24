(ns net.http.router-test
  (:use code.test)
  (:require [net.http.router :refer :all]))

^{:refer net.http.router/compare-masks :added "4.0"}
(fact "compares two path masks for sorting")

^{:refer net.http.router/make-matcher :added "4.0"}
(fact "Given set of routes, builds matcher structure.")

^{:refer net.http.router/match-impl :added "4.0"}
(fact "matches path against matcher structure")

^{:refer net.http.router/match :added "4.0"}
(fact "matches a path against a matcher")

^{:refer net.http.router/router :added "4.0"}
(fact "creates a ring router from routes")

^{:refer net.http.router/serve-resource :added "4.0"}
(fact "serves a static resource")

^{:refer net.http.router/split-path :added "4.0"}
(fact "splits a path into components")

^{:refer net.http.router/match-path :added "4.0"}
(fact "checks if a path matches a mask")