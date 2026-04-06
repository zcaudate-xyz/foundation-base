(ns std.concurrent.request-apply-test
  (:require [std.concurrent.request :as req]
            [std.concurrent.request-apply :refer :all])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.request-apply/req-call :added "3.0"}
(fact "extensible function for a request applicative"
  (with-redefs [req/req (fn [client command opts]
                          [client command opts])]
    (req-call {:type :single
               :function (fn [args _]
                           {:type :echo
                            :args args})}
              :client
              [:hello]
              {:async true}))
  => [:client {:type :echo, :args [:hello]} {:async true}])

^{:refer std.concurrent.request-apply/req-apply-in :added "3.0"}
(fact "runs a request applicative"
  (with-redefs [req/req:in identity
                req-call (fn [& args] args)]
    (req-apply-in {:options {:async true}}
                  nil
                  [:hello]))
  => [[:type nil] nil [:hello] {:async true}]

  (with-redefs [req-call (fn [_ _ _ opts]
                           opts)]
    (req-apply-in {:options {}
                   :transform {:out (fn [_ _ _ ret]
                                      [:wrapped ret])}}
                  :client
                  [:hello]))
  => (contains {:post [fn?]}))

^{:refer std.concurrent.request-apply/req:applicative :added "3.0"}
(fact "constructs a request applicative"
  (req:applicative {:type :single
                    :client :demo})
  => (contains {:type :single
                :client :demo}))
