(ns std.concurrent.request-apply-test
  (:require [std.concurrent.request :as req]
            [std.concurrent.request-apply :refer :all])
  (:use [code.test :exclude [run]]))

^{:refer std.concurrent.request-apply/req-call :added "3.0"}
(fact "extensible function for a request applicative"
  (with-redefs [req/req-fn (fn [client command opts]
                             [client command opts])]
    (let [[client command opts] (req-call {:type :single
                                           :function (fn [args _]
                                                       {:type :echo
                                                        :args args})}
                                          :client
                                          [:hello]
                                          {:async true})]
      [client
       command
       (:async opts)
       (contains? opts :context)]))
  => [:client {:type :echo, :args [:hello]} true true])

^{:refer std.concurrent.request-apply/req-apply-in :added "3.0"}
(fact "runs a request applicative"
  (with-redefs [req-call (fn [& args]
                           (swap! req/*inputs* conj args))]
    (let [captured (req-apply-in {:options {:async true}}
                                 nil
                                 [:hello])]
      [(= 1 (count captured))
       (= [{:options {:async true}} nil [:hello] {:async true}]
          (vec (first captured)))]))
  => [true true]

  (with-redefs [req-call (fn [_ _ _ opts]
                            opts)]
    (let [opts (req-apply-in {:options {}
                              :transform {:out (fn [_ _ _ ret]
                                                 [:wrapped ret])}}
                             :client
                             [:hello])]
      [(map? opts)
       (= 1 (count (:post opts)))
       (fn? (first (:post opts)))]))
  => [true true true])

^{:refer std.concurrent.request-apply/req:applicative :added "3.0"}
(fact "constructs a request applicative"
  (req:applicative {:type :single
                    :client :demo})
  => (contains {:type :single
                :client :demo}))
