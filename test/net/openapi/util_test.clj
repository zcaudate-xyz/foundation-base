(ns net.openapi.util-test
  (:require [net.http :as http]
            [net.openapi.util :refer :all])
  (:use code.test))

^{:refer net.openapi.util/call-api :added "4.0"}
(fact "Call an API by making HTTP request and return its response."
  (with-redefs [http/request identity]
    (call-api "https://api.test/users/{id}" :get
              {:path-params {:id 42}
               :query-params {:q "hello"}
               :header-params {:x-token "abc"}}))
  => {:url "https://api.test/users/42"
      :method :get
      :content-type nil
      :query-params {:q "hello"}
      :headers {:x-token "abc"}})
